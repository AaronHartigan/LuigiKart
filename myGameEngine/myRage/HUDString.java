package myGameEngine.myRage;

import com.jogamp.opengl.util.gl2.GLUT;

public class HUDString {
	private String str;
	private int HUDfont = GLUT.BITMAP_TIMES_ROMAN_24;
	private int x;
	private int y;
	
	public HUDString(String str, int x, int y) {
		this.str = str;
		this.x = x;
		this.y = y;
	}
	
	public String getStr() {
		return str;
	}
	public void setStr(String str) {
		this.str = str;
	}
	public int getX() {
		return x;
	}
	public void setX(int x) {
		this.x = x;
	}
	public int getY() {
		return y;
	}
	public void setY(int y) {
		this.y = y;
	}
	public int getHUDfont() {
		return HUDfont;
	}
	
	public void setAll(String str, int x, int y) {
		this.str = str;
		this.x = x;
		this.y = y;
	}
}