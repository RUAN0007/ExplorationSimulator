package Model;

public class Robot {
	
	private Block southWestBlock;
	private int diameterInCellNum;
	private Orientation currentOrientation;
	private int explorationRange;
	
	public Robot(Block southWestBlock,int diameterInCellNum, Orientation currentOrientation,int explorationRange){
		this.southWestBlock = southWestBlock;
		this.diameterInCellNum = diameterInCellNum;
		this.currentOrientation = currentOrientation;
		this.explorationRange = explorationRange;
	}
	
	public Robot(int southWestRowIndex, int southWestColIndex,
			int diameterInCellNum, Orientation currentOrientation,int explorationRange) {
		 this.southWestBlock = new Block(southWestRowIndex, southWestColIndex);
		 this.diameterInCellNum = diameterInCellNum;
			this.currentOrientation = currentOrientation;
			this.explorationRange = explorationRange;
	}
	
	
	
//	public int getLowerLeftRowIndex() {
//		return this.southWestBlock.getRowID();
//	}
//	public void setLowerLeftRowIndex(int southWestRowIndex) {
//		int southWestcolID = this.southWestBlock.getColID();
//		this.southWestBlock = new Block(southWestRowIndex, southWestcolID);
//	}
//	public int getLowerLeftColIndex() {
//		return this.southWestBlock.getColID();
//	}
//	
//	public void setLowerLeftColIndex(int southWestColIndex) {
//		int southWestRowID = this.southWestBlock.getRowID();
//		this.southWestBlock = new Block(southWestRowID, southWestColIndex);
//	}
	
	public int getExplorationRange() {
		return explorationRange;
	}

	public Block getSouthWestBlock() {
		return southWestBlock;
	}



	public void setSouthWestBlock(Block southWestBlock) {
		this.southWestBlock = southWestBlock;
	}



	public Orientation getCurrentOrientation() {
		return currentOrientation;
	}
	public void setCurrentOrientation(Orientation currentOrientation) {
		this.currentOrientation = currentOrientation;
	}
	public int getDiameterInCellNum() {
		return diameterInCellNum;
	}
	
	@Override
	public String toString(){
		return "Robot:\n"
				+"South West Block: " + this.southWestBlock.toString() + "\n"
				+"Diameter: " + this.diameterInCellNum + " Orientation: " + this.currentOrientation;
	}
	
	public void move(Action act){
		if(act.equals(Action.MOVE_FORWARD)){
			this.southWestBlock = this.southWestBlock.aheadOf(currentOrientation);
		}else if(act.equals(Action.DRAW_BACK)){
			this.southWestBlock = this.southWestBlock.rearOf(currentOrientation);

		}
		this.currentOrientation = act.orientationAfterAction(this.currentOrientation);

	}
	 
	
}
