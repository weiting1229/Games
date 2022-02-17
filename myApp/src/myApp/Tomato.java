package myApp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class Tomato extends JFrame{
	JButton Add, Delete, Strat, Stop, Load, List, Clear;
	JTextArea result;
	JScrollPane Scroll;
	JPanel top;
	String name,workTime,restTime,count;
	Mythread mythread;
	Timer timer;
	Properties prop;
	myPanel img;
	int work,rest;
	JPanel insert,interFace;
	JTextField TFname,TFworkTIme,TFrestTime,TFcount;
	JLabel Lname,LworkTIme,LrestTime,Lcount,countDown;
	
	public Tomato() {
		super("Tomato Clock");
		Add = new JButton("新增記錄檔");
		Delete = new JButton("刪除");
		Strat = new JButton("開始");
		Stop = new JButton("停止");
		Load = new JButton("載入");
		List = new JButton("顯示記錄檔");
		Clear = new JButton("清空列表");
		result = new JTextArea();
		result.setFont(new Font("標楷體",Font.PLAIN,20));
		Scroll = new JScrollPane(result);
		img = new myPanel();
		TFname = new JTextField(10);
		TFworkTIme = new JTextField(10);
		TFrestTime = new JTextField(10);
		TFcount = new JTextField(10);
		Lname = new JLabel("檔名:");
		LworkTIme = new JLabel("工作時間(分):");
		LrestTime = new JLabel("休息時間(分):");
		Lcount = new JLabel("次數:");
		countDown = new JLabel();
		
		prop = new Properties();
		prop.put("user", "root");
		prop.put("password", "root");
		
		Layout();
		
		setEvent();
		
		setSize(720, 426);
		setVisible(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
	
	//版面配置
	private void Layout() {
		setLayout(new BorderLayout());
		top = new JPanel(new FlowLayout(FlowLayout.CENTER,10,10));
		insert = new JPanel();
		interFace = new JPanel();
		countDown.setPreferredSize(new Dimension(120,30));
		countDown.setFont(new Font("標楷體",Font.PLAIN,20));
		
		top.add(Add);top.add(List);top.add(Load);top.add(Strat);
		top.add(Stop);top.add(Delete);top.add(Clear);
		
		interFace.setLayout(new BorderLayout());
		interFace.add(countDown,BorderLayout.NORTH);
		interFace.add(Scroll,BorderLayout.CENTER);
		
		add(top,BorderLayout.NORTH);
		add(img,BorderLayout.EAST);
//		add(countDown,BorderLayout.CENTER);
		add(interFace,BorderLayout.CENTER);
		
		insert.add(Lname);insert.add(TFname);insert.add(Box.createHorizontalStrut(2));
		insert.add(LworkTIme);insert.add(TFworkTIme);insert.add(Box.createHorizontalStrut(2));
		insert.add(LrestTime);insert.add(TFrestTime);insert.add(Box.createHorizontalStrut(2));
		insert.add(Lcount);insert.add(TFcount);insert.add(Box.createHorizontalStrut(2));
	}
	
	//設置按鈕功能
	private void setEvent() {
		Add.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				addTask();
			}
		});
		
		Strat.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				startRun();
			}
		});
		
		Stop.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				stopRun();
			}
		});
		
		Load.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				loadTask();
			}
		});
		
		List.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				showAll();
			}
		});
		
		Delete.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				deleteTask();
			}
		});
		
		Clear.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				clearAll();	
			}
		});
	}
	
	//新增時間
	private void addTask() {
		TFname.setText(null);TFworkTIme.setText(null);TFrestTime.setText(null);TFcount.setText(null);
		JOptionPane.showConfirmDialog(null,insert,"設定記錄檔",JOptionPane.OK_CANCEL_OPTION);
		name = TFname.getText();
		workTime = TFworkTIme.getText();			
		restTime = TFrestTime.getText();
		count = TFcount.getText();
		
		//若其中一欄沒有輸入,則新增失敗
		if(name.length() > 0 && workTime.length() > 0 && restTime.length() > 0 && count.length() > 0) {
		JOptionPane.showMessageDialog(null,"新增完成");
		}else {
			JOptionPane.showMessageDialog(null,"新增失敗,請輸入完整資訊");
		}
		
		//指令:新增資料列
		String sqlAdd = "INSERT INTO tomato (name,workTime,restTime,count) VALUES (?,?,?,?)";
		//連結資料庫,建立Connection
		try {
			Connection connection = DriverManager.getConnection(
					"jdbc:mysql://localhost:3306/eeit36",prop );
			PreparedStatement pstmt = connection.prepareStatement(sqlAdd);
			pstmt.setString(1, name);
			pstmt.setString(2, workTime);
			pstmt.setString(3, restTime);
			pstmt.setString(4, count);
			pstmt.executeUpdate();
		}catch(Exception e) {
			System.out.println(e.toString());
		}
	}
	
	//打開番茄鐘
	private void startRun() {
		result.setText(null);
		if(workTime!= null && restTime!=null) {
			//必須每次按開始都新建一個Thread物件,不可同一個Thread跑兩次
			mythread = new Mythread(workTime,restTime,count);
			mythread.start();
			
		}else {
			result.setText("請載入記錄檔");
		}
	}
	
	//倒數計時
	private void myCountDown(String workTime,String restTime){
		timer = new Timer();
		String nowText = result.getText();
		
		if("工作時間".equals(nowText.substring(0,4))) {
			timer.scheduleAtFixedRate(new TimerTask() {
				int i = Integer.parseInt(workTime) - 1;
				int j = 59;
				@Override
				public void run() {
					if(i == -01 && j == -01){
						timer.cancel();
					}else {
						countDown.setText(String.format("剩餘時間: %02d:%02d", i, j));
						j--;
						if(j == -01) {
							i--;
							j += 60;
						}
					}
				}
			}, 0, 1000);
			
			
		}else if("休息時間".equals(nowText.substring(0,4))) {
			timer.scheduleAtFixedRate(new TimerTask() {
				int i = Integer.parseInt(restTime) - 1;
				int j = 59;
				@Override
				public void run() {
					if(i == -01 && j == -01){
						timer.cancel();
					}else {
						countDown.setText(String.format("剩餘時間: %02d:%02d", i, j));
						j--;
						if(j == -01) {
							i--;
							j += 60;
						}
					}
				}
			}, 0, 1000);
		}
	}
	
	//工作時間、休息時間
	class Mythread extends Thread{
		int workSecond ,restSecond,round;
		Mythread(String work ,String rest ,String count){
			workSecond = (Integer.parseInt(work))*1000*60;
			restSecond = (Integer.parseInt(rest))*1000*60;
			round = (Integer.parseInt(count));
		}
		public void run(){	
			try {
				work = 0;
				rest = 0;
				for(int i=round ; i>0 ; i--) {
					result.setText(String.format("工作時間(第%d輪)",work+1));
					myCountDown(workTime,restTime);
					Thread.sleep(workSecond);
					timer.cancel();
					work++;
					
					result.setText(String.format("休息時間(第%d輪)",rest+1));
					myCountDown(workTime,restTime);
					Thread.sleep(restSecond);
					timer.cancel();
					rest++;
				}
				result.setText("結束,總共執行"+round+"次");
				timer.cancel();
			} catch (InterruptedException e) {
				System.out.println(e.toString());
			}
		}
	}
	
	//關闢番茄鐘
	private void stopRun() {
		mythread.stop();
		timer.cancel();
		result.setText("已停止,共工作"+work+"次,共休息"+rest+"次");
	}
	
	//讀取設定
	private void loadTask() {
		String selectTask = JOptionPane.showInputDialog("請輸入要讀取的紀錄檔:");
		
		//指令:搜尋單一列
		String sqlSelect = "SELECT * FROM tomato WHERE name = ?";
		try {
			Connection connection = DriverManager.getConnection(
					"jdbc:mysql://localhost:3306/eeit36",prop );
			PreparedStatement pstmt = connection.prepareStatement(sqlSelect);
			pstmt.setString(1, selectTask);
			ResultSet rs = pstmt.executeQuery();
			rs.next();
			if(rs.getRow() > 0) {	
				name = rs.getString(1);
				workTime = rs.getString(2);
				restTime = rs.getString(3);
				count = rs.getString(4);
				int workInt = Integer.parseInt(workTime);
				int restInt = Integer.parseInt(restTime);
				int CountInt = Integer.parseInt(count);
				result.setText("讀取成功! 載入設定檔:"+"\n\n名稱:"+name+"\n工作時間:"+workInt+"分鐘\n休息時間:"+restInt+"分鐘\n次數:"+CountInt
						+"次\n總共耗時"+((workInt+restInt)*CountInt)+"分鐘\n\n請按下\"開始\"鍵,開始計時");
			}else {
				result.setText("讀取失敗,請重新載入");
			}
		} catch (SQLException e) {
			System.out.println(e.toString());
		}
	}
	
	//顯示記錄檔
	private void showAll() {
		result.setText("");
		countDown.setText("");
		
		//指令:顯示全部列
		String sqlLIst = "SELECT * FROM tomato";
		try {
			Connection connection = DriverManager.getConnection(
					"jdbc:mysql://localhost:3306/eeit36",prop );
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(sqlLIst);
			int row = 0;
			if(rs != null) {
				while(rs.next()) {   
					row++;
					String name = rs.getString(1);
					String workTime = rs.getString(2);
					String restTime = rs.getString(3);
					String count = rs.getString(4);
					int workInt = Integer.parseInt(workTime);
					int restInt = Integer.parseInt(restTime);
					int CountInt = Integer.parseInt(count);
					result.append("名稱:"+name+"\n工作時間:"+workInt+"分鐘\n休息時間:"+restInt+"分鐘\n次數:"+CountInt
							+"次\n總共耗時"+((workInt+restInt)*CountInt)+"分鐘\n---------------------------------\n");
				}
				result.append("總共找到"+row+"筆資料,請載入設定檔");
			}else {
				result.setText("查無資料");
			}
		} catch (SQLException e) {
			System.out.println(e.toString());
		}
	}
	
	//刪除一列
	private void deleteTask() {
		result.setText("");
		countDown.setText("");
		String selectTask = JOptionPane.showInputDialog("請輸入要刪除的紀錄檔:");
		
		//指令:刪除單一列
		String sqlDelete = "DELETE FROM tomato WHERE name = ?";
			try {
				Connection connection = DriverManager.getConnection(
						"jdbc:mysql://localhost:3306/eeit36",prop );
				PreparedStatement pstmt = connection.prepareStatement(sqlDelete);
				pstmt.setString(1, selectTask);
				pstmt.executeUpdate();
				result.setText("刪除成功");
			} catch (SQLException e) {
				System.out.println(e.toString());
			}	
	}
	
	//清除列表
	private void clearAll() {
		result.setText("");
		countDown.setText("");
		
		//指令:清空資料表
		String sqlClear = "TRUNCATE TABLE tomato";
		try {
			Connection connection = DriverManager.getConnection(
					"jdbc:mysql://localhost:3306/eeit36",prop);
			Statement stmt = connection.createStatement();
			stmt.execute(sqlClear);
		} catch (SQLException e) {
			System.out.println(e.toString());
		}
		
		result.setText("清除完成");
	}
	
	//番茄圖片
	class myPanel extends JPanel{
		BufferedImage imgtomato;

		public myPanel() {
			setPreferredSize(new Dimension(336,250));
			setBackground(Color.yellow);
		}
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			try {
				imgtomato = ImageIO.read(new File("dir1/tomato3.png"));
				g.drawImage(imgtomato,0,0,null);
			} catch (IOException e) {
				System.out.println(e.toString());
			}
		}
	}
	
	//程式啟動
	public static void main(String[] args) {
		new Tomato();
	}

}