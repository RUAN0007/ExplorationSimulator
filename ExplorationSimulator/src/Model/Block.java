package Model;

public class Block {
	private int rowID;
	private int colID;
	
	public int getRowID() {
		return rowID;
	}
	public int getColID() {
		return colID;
	}
	public Block(int rowID, int colID) {
		super();
		this.rowID = rowID;
		this.colID = colID;
	}
	
	public Block toLeftOf(Orientation face){
		int leftRowID = this.rowID;
		int leftColID = this.colID;
		
		if(face.equals(Orientation.NORTH)){
			leftColID--;
		}else if(face.equals(Orientation.EAST)){
			leftRowID--;
		}else if(face.equals(Orientation.SOUTH)){
			leftColID++;
		}else{
			//face to left
			leftRowID++;
		}
		return new Block(leftRowID, leftColID);
	}
	
	public Block toRightOf(Orientation face){
		int rightRowID = this.rowID;
		int rightColID = this.colID;
		
		if(face.equals(Orientation.NORTH)){
			rightColID++;
		}else if(face.equals(Orientation.EAST)){
			rightRowID++;
		}else if(face.equals(Orientation.SOUTH)){
			rightColID--;
		}else{
			//face to left
			rightRowID--;
		}
		return new Block(rightRowID, rightColID);
	}
	
	public Block aheadOf(Orientation face){
		int aheadRowID = this.rowID;
		int aheadColID = this.colID;
		
		if(face.equals(Orientation.NORTH)){
			aheadRowID--;

		}else if(face.equals(Orientation.EAST)){
			aheadColID++;

		}else if(face.equals(Orientation.SOUTH)){
			aheadRowID++;

		}else{
			//face to left
			aheadColID--;		
		}
		return new Block(aheadRowID, aheadColID);
	}
	
	public Block rearOf(Orientation face){
		int rearRowID = this.rowID;
		int rearColID = this.colID;
		
		if(face.equals(Orientation.NORTH)){
			rearRowID++;

		}else if(face.equals(Orientation.EAST)){
			rearColID--;

		}else if(face.equals(Orientation.SOUTH)){
			rearRowID--;

		}else{
			//face to left
			rearColID++;		
		}
		return new Block(rearRowID, rearColID);
	}
	
	public boolean equals(Block b){
		return this.rowID == b.rowID && this.colID == b.colID;
	}
	
	@Override
	public String toString(){
		return "RowID: " + rowID + " ColID: " + colID;
	}
	
	public Block clone(){
		return new Block(this.rowID, this.colID);
	}
	
}
