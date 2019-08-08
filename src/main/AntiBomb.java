package main;

import java.awt.AWTException;
import java.awt.Container;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;

@SuppressWarnings("serial")
public class AntiBomb extends JFrame implements Runnable{
	Robot rb;
	Thread t;//扫雷线程
	JButton b1,b2,b3;
	JButton[] bg;
	static final int[] wg = {9,16,30};
	static final int[] hg = {9,16,16};
	static final String[] tab= {"初级","中级","高级"};
    static final int [] lg = {10,40,99};
	static final int[] crg={192,  0,  0,255,  0,128};
	static final int[] cgg={192,  0,128,  0,  0,  0};
	static final int[] cbg={192,255,  0,  0,128,  0}; 
	int[][] pg;
	int w,h;
	JLabel jl,sl;
	Container con;
	int x,y;
	boolean b;
	
	public AntiBomb() throws Exception{
		x = 679;
		y = 517;
		w = h = 9;
		b = true;
		initFrame();
		rb = new Robot();
		
		//more
		t = new Thread(this);
		t.start();
	}
	
	private void initFrame(){
		setTitle("AntiBombV1.0");
		setBounds(836,416, 200,262);
		setVisible(true);
	    setDefaultCloseOperation(EXIT_ON_CLOSE);
	    setLayout(null);
	    //addKeyListener(new JFCL());
	    con = getContentPane();
	    jl = new JLabel("将鼠标放在左上角，按回车开始！");
	    jl.setBounds(0, 10, 240, 20);
	    con.add(jl);
	    sl = new JLabel("设置："+tab[0]+"("+w+"X"+h+")");
	    sl.setBounds(0, 40, 240, 20);
	    con.add(sl);
	    bg = new JButton[3];
	    initButton();
	}
	
	private void initButton(){
		bg[0] = b1;
		bg[1] = b2;
		bg[2] = b3;
		for(int i = 0;i<3;i++){
			bg[i] = new JButton(tab[i]+"("+wg[i]+"X"+hg[i]+")");
			bg[i].setBounds(30, 70+50*i, 140, 40);
			bg[i].addActionListener(new BAL(i));
			bg[i].addKeyListener(new JFCL());
			con.add(bg[i]);
		}
		setButtonEnabled(0);
	}
	
	private void setButtonEnabled(int index){
		bg[0].setEnabled(true);
		bg[1].setEnabled(true);
		bg[2].setEnabled(true);
		bg[index].setEnabled(false);
	}
	
	private class BAL implements ActionListener{
		private int id;
		public BAL(int i){
			id = i;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			w = wg[id];
			h = hg[id];
			setButtonEnabled(id);
			sl.setText("设置："+tab[id]+"("+w+"X"+h+")");
		}
		
	}
	
	private class JFCL implements KeyListener{
		@Override
		public void keyTyped(KeyEvent e) {
			dowhat(e);
		}
		@Override
		public void keyPressed(KeyEvent e) {
			dowhat(e);
			
		}
		@Override
		public void keyReleased(KeyEvent e) {
			// TODO Auto-generated method stub
			
		}
		
		private void dowhat(KeyEvent e){
			if(b == true){
				if(e.getKeyCode() == KeyEvent.VK_ENTER){
					Point p = MouseInfo.getPointerInfo().getLocation();
					x = p.x;
					y = p.y;
					initPG();
					synchronized (t) {
						t.notify();
					}
					//System.out.println("VK_ENTER is down!");
				}
			}
		}

		
	}
	
	private void initPG(){
		pg = new int[w][h];
		for(int i = 0;i<w;i++){
			for(int j = 0;j<h;j++){
				pg[i][j] = -1;
				/* -3疑似雷
				 * -2有雷
				 * -1未按过
				 * 0非边缘
				 * 1边缘一颗雷
				 * 。。。
				 */
			}
		}
	}
	
	private int getAbs(int index){
		return 16*index-8;
	}
	
	private void RobotClick(int mask){
		rb.mousePress(mask);
		rb.mouseRelease(mask);
	}
	
	private List<int[]> getList(int index){
		List<int[]> list = new ArrayList<int[]>();
		for(int i = 0;i<w;i++){
			for(int j = 0;j<h;j++){
				if(pg[i][j] == index){
					int[] k = new int[2];
					k[0] = i;
					k[1] = j;
					list.add(k);
				}
			}
		}
		return list;
	}
	
	private void getRandom(List<int[]> list){
		Random r = new Random();
		int index = r.nextInt(list.size());
		int[] loc =list.get(index);
		int boxX = loc[0];
		int boxY = loc[1];
		rb.mouseMove(x+getAbs(boxX), y+getAbs(boxY));
		RobotClick(InputEvent.BUTTON1_MASK);
		System.out.println("RobotClick(getRandom):("+boxX+","+boxY+");");
	}
	
	private void pressRandomBox()throws Exception{;
		List<int[]> notEdgeList = getList(-1);
		if(notEdgeList.isEmpty()){
			List<int[]> edgeList = getList(-3);
			getRandom(edgeList);
		}else{
			getRandom(notEdgeList);
		}
	}
	
	private BufferedImage takePhoto(){
		Robot rbt = null;
		try {
			rbt = new Robot();
		} catch (AWTException e) {
			e.printStackTrace();
		}
		BufferedImage im = rbt.createScreenCapture(new Rectangle(x,y,16*w,16*h));
		try {
			ImageIO.write(im,"JPG",new File("i.jpg"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return im;
	}
	
	private int[] getRGB(int rgb){
		int[] rgbg = new int[3];
		rgbg[0] = (rgb & 0xff0000) >> 16;
		rgbg[1] = (rgb & 0xff00) >> 8;
		rgbg[2] = (rgb & 0xff);
		//System.out.println("color:"+rgbg[0]+rgbg[1]+rgbg[2]);
		return rgbg;
	}
	
	private boolean isColor(int[]rgb,int r,int g,int b){
		//System.out.println("color:"+rgb[0]+rgb[1]+rgb[2]);
		if(rgb[0] == r){
			if(rgb[1] == g){
				if(rgb[2] == b){
					return true;
				}
			}
		}
		return false;
	}
	
	private boolean testphoto(BufferedImage im){//识别
		System.out.println("识别-------");
		//刷新
		for(int i = 0;i<w;i++){
			for(int j = 0;j<h;j++){
				if(pg[i][j] != -2){
					pg[i][j] = 0;
				}
			}
		}
		for(int i = 0;i<16*w;i++){
			for(int j = 0;j<16*h;j++){
				int [] rgb = getRGB(im.getRGB(i,j));
				int ColId = getColorIndex(rgb);
				int x = i/16;
				int y = j/16;
				if(ColId != 0){
					pg[x][y] = ColId;
					//System.out.println("Location:("+x+","+y+"),ColorId:("+ColId+")");
				}else{
					if(pg[x][y] == 0){
						if(isColor(rgb,255,255,255)){
						    pg[x][y] = -1;
						}
					}
					if(isColor(rgb,0,0,0)){//触雷
						return true;//break;
					}
				}
				//System.out.println("Location:("+i+","+j+"),Color:("+rgb[0]+","+rgb[1]+","+rgb[2]+")");
			}
		}
		//输出
		outPrint();
		findBomb();
		return false;
	}
	
	int id,k,bomb,kg[][];
	String str;
	
	void doit(int x,int y){
		id = pg[x][y];
		if(id == -1||id == -3){
			k++;
			int [] location =new int[2];
			location[0] = x;
			location[1] = y;
			kg[k-1] = location;
		}
		if(id == -2){
			bomb--;
		}
	}
	
	private void findBomb(){//扫雷
		boolean haveResult = false;
		for(int i = 0;i<w;i++){
			for(int j = 0;j<h;j++){
				bomb = pg[i][j];//雷数
				if(bomb<1){//非数字格
					//System.out.println("("+(i+1)+","+(j+1)+"):break;");
				}else{
				boolean aw = true;//左上
				boolean dw = true;//右上
				boolean ax = true;//左下
				boolean dx = true;//右下
				k = 0;//空格数
				kg = new int[8][]; 
				str = "";
				if(i!=0){//左侧
					doit(i-1,j);
				}else{//a
					aw = false;//左侧无格
					ax = false;
				}
				if(i!=8){//右侧
					doit(i+1,j);
				}else{//d
					dw = false;//右侧无格
					dx = false;
				}
				if(j!=0){//上面
					doit(i,j-1);
				}else{//w
					aw = false;//上面无格
					dw = false;
				}
				if(j!=8){//下面
					doit(i,j+1);
				}else{//x
					ax = false;//下面无格
					dx = false;
				}
				if(aw){//左上
					doit(i-1,j-1);
				}
				if(dw){//右上
					doit(i+1,j-1);
				}
				if(ax){//左下
					doit(i-1,j+1);
				}
				if(dx){//右下
					doit(i+1,j+1);
				}
				//System.out.println("("+(i+1)+","+(j+1)+"),("+bomb+","+k+");");
				//标识雷
				if(k == bomb&&bomb !=0){
					for(int index = 0;index<k;index++){
						int mx = kg[index][0];
						int my = kg[index][1];
						if(pg[mx][my] != -2){
							haveResult = true;
						}
						pg[mx][my] = -2;
						//System.out.println("状态：标识确定雷:("+mx+","+my+");");
					}
				}
				//清空安全格
				else if(k>0&&bomb == 0){
					haveResult = true;
					for(int index = 0;index<k;index++){
						int mx = kg[index][0];
						int my = kg[index][1];
						if(pg[mx][my] != -2&&pg[i][j] != 0){
							pg[mx][my] = 0;
							int absX = getAbs(mx+1);
							int absY = getAbs(my+1);
							rb.mouseMove(x+absX,y+absY);
							RobotClick(InputEvent.BUTTON1_MASK);
							System.out.println("RobotClick(清空安全格):("+mx+","+my+");");
							//System.out.println("状态：清空安全格:("+mx+","+my+");");
						}
						//RobotClick(InputEvent.BUTTON3_MASK);
					}
				}//标识疑似雷
				else if(k>0){
					for(int index = 0;index<k;index++){
						int mx = kg[index][0];
						int my = kg[index][1];
						pg[mx][my] = -3;
						//System.out.println("状态：标识疑似雷:("+mx+","+my+");");
					}
				}
				}
			}
		}
		if(haveResult == false){
			try {
				pressRandomBox();
				System.out.println("状态：无结果随机！");
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
		System.out.println("扫后-------");
		//outPrint();
	}
	
	private boolean outPrint(){//输出&&检查退出
		boolean isNaNK = true;
		for(int i = 0;i<h;i++){
			String str = "";
			for(int j = 0;j<w;j++){
				String s = ""+pg[j][i];
				if(s.equals("-1")){
					s = "K";
					isNaNK = false;
				}
				if(s.equals("-2")){
					s = "B";
				}
				if(s.equals("-3")){
					s = "N";
					isNaNK = false;
				}
				str = str + s;
			}
			System.out.println(str);
		}
		return isNaNK;
	}
	
	private int getColorIndex(int[] rgb){
		for(int index =0;index<crg.length;index++){
			int r = crg[index];
			int g = cgg[index];
			int b = cbg[index];
			if(isColor(rgb,r,g,b)){
				if(index != 0){
					return index;
				}
			}
		}
		return 0;
	}	
	
	/* void infoPhoto(BufferedImage im){
		System.out.println("info:--------------");
		for(int i = 0;i<w;i++){
			for(int j = 0;j<h;j++){
				int[] rgb1 = getRGB(im.getRGB(16*i, 16*j+1));
				int[] rgb2 = getRGB(im.getRGB(16*i+1, 16*j));
				System.out.println("Location1:("+16*i+","+(16*j+1)+")");
				System.out.println("Location2:("+(16*i+1)+","+16*j+")");
				boolean b1 = isColor(rgb1,128,128,128);
				boolean b2 = isColor(rgb2,128,128,128);
				if(b1&&b2 == false){
					int colId = getColorIndex(im,16*i,16*j);
					pg[i][j] = colId;
					System.out.println("Location:("+i+","+j+")Color:"+colId);
				}
			}
		}
	}*/
	
	public void run(){
		synchronized (t) {
			while(true){
				try {
					t.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				//System.out.println("Thread start!");
				b = false;
				rb.mouseMove(x,y);
				RobotClick(InputEvent.BUTTON1_MASK);
				try {
					pressRandomBox();
					Thread.sleep(100);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				for(int k = 0;k<w*h;k++){
					BufferedImage im = takePhoto();
					if(testphoto(im)){
						System.out.println("意外触雷！");
						break;
					}
					if(outPrint()){
						System.out.println("扫雷结束！");
						break;
					}
					try {
						Thread.sleep(50);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
				b = true;
			}
		}
	}
	
	public static void main(String[] args) throws Exception {
		new AntiBomb();
	}

}
