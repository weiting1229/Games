package myApp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

//視窗基本設定
public class Flappybird extends JFrame{
	MyPanel mypanel;
	
	public Flappybird() {
		super("Flappy Bird");
		
		mypanel = new MyPanel();

		//版面配置
		setLayout(new BorderLayout());
		add(mypanel,BorderLayout.CENTER);
		
		setSize(720, 640);
		setVisible(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setResizable(false);
		setLocationRelativeTo(null);
	}
	
	//JPanel面板,用來放置遊戲內容
	class MyPanel extends JPanel{
		Timer timer;						//宣告Timer作為執行緒
		int backX1,backX2;					//背景1的X座標、背景2的X座標 (兩者為同張圖,輪流放映)
		int birdX,birdY,upSpeed;			//小鳥的X座標、小鳥的Y座標、下降速度、上升加速度
		int pipeX[],pipeY1[],pipeY2[];		//水管X座標、水管(下)Y座標、水管(上)Y座標
		int pipeIniti,pipeSpace;			//水管起始X值、水管間距
		int score;							//分數
		
		//匯入 背景圖、小鳥圖、小鳥圖(揮翅膀)、水管圖(下)、水管圖(上)
		BufferedImage background,bird1,bird2,pipeDown,pipeUp;
		
		public MyPanel(){
			try {
				background = ImageIO.read(new File("dir1/bg1.png"));
				bird1 = ImageIO.read(new File("dir1/bird1.png"));
				bird2 = ImageIO.read(new File("dir1/bird2.png"));
				pipeDown = ImageIO.read(new File("dir1/pipe.png"));
				pipeUp = ImageIO.read(new File("dir1/pipe2.png"));
			} catch (Exception e) {
				System.out.println(e.toString());
			}
			
			//初始遊戲畫面
			newGame();

			timer = new Timer();
			timer.schedule(new backMov(), 1*1000, 30);
			timer.schedule(new birdDown(), 1*1000, 20);
			timer.schedule(new REPAINT(), 1*1000, 10);
			timer.schedule(new pipeMov(), 1*1000, 25);
			timer.schedule(new isCollision(), 1*1000, 100);
			addMouseListener(new MyMouseListener());
		}
		
		//初始水管座標
		//給定下水管Y座標為隨機280~480的值
		//上水管與下水管的Y座標固定相差520像素(縫隙間距固定)	
		private void newGame() {
			pipeX = new int[5];
			pipeY1 = new int[5];
			pipeY2 = new int[5];
			birdX = 150;
			birdY = 220;
			pipeIniti = 500;
			pipeSpace = 430;
			backX1 = 0;
			backX2 = 720;
			score = 0;
			
			for(int i=0; i<5; i++) {
				int pipedown = (int)(Math.random()*280+200);
				int pipeup = pipedown-530;
				pipeX[i] = pipeIniti + pipeSpace*i;
				pipeY1[i] = pipedown;
				pipeY2[i] = pipeup;	
			}
		}
		
		//水管每20毫秒左移3像素
		//水管寬100,當某水管完全消失於畫面(X <= -100)
		//則將它搬到"原本的第五根水管當前位置"往後推一個pipeSpace的位置
		private class pipeMov extends TimerTask{
			@Override
			public void run() {
				for(int i=0; i<5; i++) {
					pipeX[i] -= 3;
					if(pipeX[i] <= -100) {
						pipeX[i] = pipeIniti + pipeSpace*4 -
								(pipeIniti+100-pipeSpace);
					}
					if(birdX+2 > pipeX[i]+100 && birdX-2 < pipeX[i]+100) {
						score++;
					}
				}
				
			}
		}
		
		//背景移動,兩張相同背景輪流左移,第一張完全消失到畫面左側後,使其移動到畫面最右側
		private class backMov extends TimerTask{
			@Override
			public void run() {
				backX1 -= 2;
				backX2 -= 2;
				if(backX1 < -720) {
					backX1 = 710;	
				}else if(backX2 < -720) {
					backX2 = 710;
				}
			}
		}
		
		//每20毫秒刷新1次
		private class REPAINT extends TimerTask{
			@Override
			public void run() {
				repaint();
			}
		}
		
		//小鳥每30毫秒下降2個像素
		private class birdDown extends TimerTask{
			@Override
			public void run() {
				birdY += 2;
			}
		}
		
		//點擊螢幕,小鳥上升
		private class MyMouseListener extends MouseAdapter{
			@Override
			public void mousePressed(MouseEvent e) {
				upSpeed = 10;
				timer.schedule(new birdFly(), 0, 50);
			}
		}
		
		//上升動作,起始加速度25,每10毫秒減少7
		private class birdFly extends TimerTask{
			@Override
			public void run() {
				if(upSpeed > 0) {
					if(birdY > 0) {
						birdY -= upSpeed;
					}
					upSpeed -= 2;
				}
			}
		}
		
		//碰撞檢測
		private class isCollision extends TimerTask{
			@Override
			public void run() {
				if(birdY < getHeight()-78) {
					for(int i=0; i<5; i++) {
						if(birdX > pipeX[i]-88 && birdX < pipeX[i]+95 &&
								(birdY > pipeY1[i]-70 || birdY < pipeY2[i]+395)) {
							JOptionPane.showMessageDialog(null, 
									String.format("遊戲結束,你的分數為:%d",score));
							newGame();
						}
					}
				}else {
					JOptionPane.showMessageDialog(null, 
							String.format("遊戲結束,你的分數為:%d",score));
					newGame();
				}
				
			}
		}
		
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			
			g.drawImage(background, backX1, 0, null);
			g.drawImage(background, backX2, 0, null);
			
			if(upSpeed>0) {
				g.drawImage(bird2, birdX, birdY, null);
			}else {
				g.drawImage(bird1, birdX, birdY, null);
			}
			
			for(int i=0; i<5; i++) {
				g.drawImage(pipeDown, pipeX[i], pipeY1[i], null);
				g.drawImage(pipeUp, pipeX[i], pipeY2[i], null);
			}
			Font font = new Font("Times New Roman",Font.BOLD,40);
			g.setColor(Color.WHITE);
			g.setFont(font);
			String myscore = Integer.toString(score);
			g.drawString(myscore, 50, 50);
		}
	}
	
	public static void main(String args[]) {
		new Flappybird();
	}
}
