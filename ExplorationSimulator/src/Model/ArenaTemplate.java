package Model;

import java.util.ArrayList;
import java.util.List;

public class ArenaTemplate {
	public enum CellState {UNEXPLORED,EMPTY,OBSTACLE};
	
	public class ArenaTemplateException extends Exception{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private int ID;
		private String msg;
		public String toString(){
			return "" + ID + ": "+ msg;
		}
		
		public  ArenaTemplateException(int ID,String msg){
			this.ID = ID;
			this.msg = msg;
		}
	}
	
	private CellState[][] arena ;
	
	private int rowCount = 20;
	private int columnCount = 15;
	private int startPlaceDiameterInCellNum = 3; 
	
	public ArenaTemplate(){
//		this.rowCount = rowCount;
//		this.columnCount = columnCount;
//		this.robotDiameter = robotDiameter;
		arena = new CellState[this.rowCount][this.columnCount];
		for(int rowIndex = 0;rowIndex < this.rowCount;rowIndex++){
			for(int colIndex = 0;colIndex < this.columnCount;colIndex++){
				this.arena[rowIndex][colIndex] = CellState.UNEXPLORED;
			}
		}
		
		//Set the bottom-left and upper-right corner to be empty for start and goal
		for(int row = 0;row < this.startPlaceDiameterInCellNum;row ++){
			for(int col = 0;col < this.startPlaceDiameterInCellNum;col ++){
				this.arena[row + this.rowCount - this.startPlaceDiameterInCellNum][col] 
						= CellState.EMPTY;
				this.arena[row][col + this.columnCount - this.startPlaceDiameterInCellNum]
						= CellState.EMPTY;
						
			}
		}
	}
	

	
	public int getRowCount() {
		return rowCount;
	}



	public int getColumnCount() {
		return columnCount;
	}



	public int getCellTypeNum(CellState state){
		//TODO
		
		int count = 0;
		for(int rowIndex = 0;rowIndex < this.rowCount;rowIndex++){
			for(int colIndex = 0;colIndex < this.columnCount;colIndex++){
				if(this.arena[rowIndex][colIndex] == state){
					count ++;
				}
			}
		}
		return count;
	}

	
	public void setArenaMapFromDescriptor(String descriptor) throws ArenaTemplateException{
		
		//TODO TESTING
		String lines[] = descriptor.split("\\r?\\n");
		if(lines.length != 2){
			throw new ArenaTemplateException(1,"The descriptor does not contain two line...");
		}
		
		//TODO DEBUG
//		if(GlobalUtil.DEBUG){
//			System.out.println("Descriptor: " + descriptor);
//			System.out.println("First Descriptor: " + lines[0]);
//			System.out.println("Second Descriptor: " + lines[1]);
//
//		}
//		
		markExploredCell(lines[0]);
		//TODO DEBUG

//		if(GlobalUtil.DEBUG){
//			System.out.println(lines[0]);
//		}
		markObstacleCell(lines[1]);
//		if(GlobalUtil.DEBUG){
//			System.out.println("Finish parsing Line 2");
//		}
	}
	
	private void markExploredCell(String descriptor) throws ArenaTemplateException {
		String binStr = null;
		try{
			 binStr = hexToBinaryStr(descriptor);			
		}catch(NumberFormatException e){
			throw new ArenaTemplateException(2, "The descriptor contains some non-hex digit");
		}
		if(binStr.length() != this.rowCount * this.columnCount + 4){
			throw new ArenaTemplateException(3, "Invalid Map Descriptor");
		}
		
		if(!(binStr.charAt(0) == '1' && binStr.charAt(0) == '1' && 
			binStr.charAt(302) == '1' && binStr.charAt(303) == '1')){
			throw new ArenaTemplateException(3, "Invalid Map Descriptor");

		}
		
//		System.out.println("Original in Hex: " + descriptor);
//		System.out.println("Original in Binary: " + binStr);
//		System.out.println("No header & trailer: " + binStr);

		//Cut the header and trailer 11
		binStr = binStr.substring(2, 302);


		for(int rowIndex = this.rowCount - 1; rowIndex >= 0;rowIndex--){
			for(int colIndex = 0;colIndex < this.columnCount;colIndex++){
				int strIndex = this.columnCount * (this.rowCount - 1 - rowIndex) + colIndex;
				if(binStr.charAt(strIndex) == '1'){
					this.arena[rowIndex][colIndex] = CellState.EMPTY;
				}else{
					this.arena[rowIndex][colIndex] = CellState.UNEXPLORED;
				}
			}
		}
	}

	private void markObstacleCell(String descriptor) throws ArenaTemplateException {
		String binStr = null;
		try{
			 binStr = hexToBinaryStr(descriptor);			
		}catch(NumberFormatException e){
			throw new ArenaTemplateException(2, "The descriptor contains some non-hex digit");
		}
		
	//	System.out.println("binStr = " + binStr);
		
		if(binStr.length() < getCellTypeNum(CellState.EMPTY)){
			throw new ArenaTemplateException(4, "The number of bit in the descriptor is not enough");
		}
		
		int rowIndex = this.rowCount - 1;
		int colIndex = -1 ;
		boolean finishCheckingCell = false;
		for(int strIndex = 0;strIndex < binStr.length();strIndex++){
			
			//Traverse the cell from bottom-left to top right 
			//Until an empty cell has been found
			//If the traversal has finished, ensure the latter padding bits are all 0
			do{
				colIndex++;
				if(colIndex == this.columnCount){
					colIndex = 0;
					rowIndex --;
				}
				if(rowIndex < 0){
					finishCheckingCell = true;
				}
			}while(!finishCheckingCell && 
					this.arena[rowIndex][colIndex] == CellState.UNEXPLORED);
					
//			System.out.println("RowID = " + rowIndex + "ColID = " + colIndex);
//			System.out.println("String Index: " + strIndex);
//
//			System.out.println("Char = " + binStr.charAt(strIndex));
//			
			if(!finishCheckingCell){
				if(binStr.charAt(strIndex) == '1'){
					//System.out.println("Obstacle at RowID = " + rowIndex + " ColID = " + colIndex);

					this.arena[rowIndex][colIndex] = CellState.OBSTACLE;
				}else{
					this.arena[rowIndex][colIndex] = CellState.EMPTY;
				}
			}else{
				//Ensure the padding bit are all zeros
				if(binStr.charAt(strIndex) != '0'){
					throw new ArenaTemplateException(3, "Invalid Map Descriptor");

				}
			}
		}
	}

	public CellState[][] getArenaTemplateMap(){
		return this.arena;
	}
	
	public void setCellState(int rowIndex,int colIndex,CellState newState){
		this.arena[rowIndex][colIndex] = newState;
	}
	
	public CellState getCellState(int rowIndex,int colIndex){
		return this.arena[rowIndex][colIndex];
	}
	
	public String retrieveArenaDescriptor(){
		StringBuilder explorationDescriptor = new StringBuilder("11");
		StringBuilder exploredDescriptor = new StringBuilder();
		
		//Traverse the arena map from bottom-left and up-right
		for(int rowIndex = this.rowCount - 1; rowIndex >= 0;rowIndex--){
			for(int colIndex = 0;colIndex < this.columnCount;colIndex++){
				if(this.arena[rowIndex][colIndex] == CellState.UNEXPLORED){
					explorationDescriptor.append("0");
				}else{
					explorationDescriptor.append("1");
					if(this.arena[rowIndex][colIndex] == CellState.OBSTACLE){
						exploredDescriptor.append("1");
					}else{
						//The cell is known to be empty
						exploredDescriptor.append("0");
					}
				}
			}
		}
		explorationDescriptor.append("11");
		assert(explorationDescriptor.length() == 304);
		
		
		
		return binToHexStr(explorationDescriptor.toString()) + "\n"
		       + binToHexStr(exploredDescriptor.toString());
	}
	
	private static String hexToBinaryStr(String hexStr)
			throws NumberFormatException{
  		char hex = ' ';
  		StringBuilder binStr = new StringBuilder();
  		for(int id = 0;id < hexStr.length();id++){
  			hex = hexStr.charAt(id);
  			int num = Integer.parseInt("" + hex, 16);
  			String numStr = Integer.toBinaryString(num);
  			
  			//Padding 0 in front to be 4 digits
  			while(numStr.length() < 4){
  				numStr = "0" + numStr;
  			}
  			binStr.append(numStr);
  		}
  		return binStr.toString();
  		
	  }
	  
	private static String binToHexStr(String binStr) throws NumberFormatException{
		StringBuilder hexStr = new StringBuilder();
		//Pad 0 to full byte stream
		while(binStr.length() % 8 != 0	){
			binStr = binStr + "0";
		}
		List<String> numStrs = splitEqually(binStr,4);
		for(String numStr:numStrs){
			int num = Integer.parseInt(numStr,2);
			hexStr.append(Integer.toHexString(num).toUpperCase());
		}
		return hexStr.toString();
	}
  

	private static List<String> splitEqually(String text,int size) {
	    // Give the list the right capacity to start with. You could use an array
	    // instead if you wanted.
	    List<String> ret = new ArrayList<String>((text.length() + size - 1) / size);
	
	    for (int start = 0; start < text.length(); start += size) {
	        ret.add(text.substring(start, Math.min(text.length(), start + size)));
	    }
	    return ret;
	}
  
	  
}
