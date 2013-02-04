package cell;

public class Cell {
	private int i;
	public void set(int i) {this.i = i;}
	public final int get() {return i;}
	public int getPlusOne() { return i+1; }
}