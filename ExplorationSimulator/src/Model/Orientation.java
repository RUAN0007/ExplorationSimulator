package Model;


public class Orientation{
	
	public static Orientation NORTH = new Orientation(0);
	public static Orientation EAST = new Orientation(1);

	public static Orientation SOUTH = new Orientation(2);
	public static Orientation WEST = new Orientation(3);
	public static int OrientationCount = 4;
	
	private int dirSymbol;
	
	private Orientation(int dir){
		this.dirSymbol = dir;
	}
	
	
	
	@Override
	public String toString(){
		switch(this.dirSymbol){
			case 0:
				return "UP";
			case 1:
				return "RIGHT";
			case 2:
				return "DOWN";
			case 3:
				return "RIGHT";
			default:
				return "NULL";
		}
	}
	
	public boolean equals(Orientation dir){
		return this.dirSymbol == dir.dirSymbol;
	}
	
	public  Orientation relativeToLeft(){

		return new Orientation((this.dirSymbol + OrientationCount - 1) % OrientationCount);
	}
	
	public Orientation relativeToRight(){
		return new Orientation((this.dirSymbol + 1) % OrientationCount);
	}
	
	public Orientation toOppsite(){
		return new Orientation((this.dirSymbol - OrientationCount / 2) % OrientationCount);		
	}
	
	public Orientation clone(){
		return new Orientation(this.dirSymbol);
	}
}
