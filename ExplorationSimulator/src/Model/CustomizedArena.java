package Model;

import Model.ArenaTemplate.ArenaTemplateException;
import Model.ArenaTemplate.CellState;

public class CustomizedArena {
	
	public class ArenaException extends Exception{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private int ID;
		private String msg;
		public String toString(){
			return "" + ID + ": "+ msg;
		}
		
		public  ArenaException(int ID,String msg){
			this.ID = ID;
			this.msg = msg;
		}
	}
	
	
	private ArenaTemplate template;
	
	private int rowCount;
	private int columnCount;
	private ArenaTemplate.CellState[][] cells;
	

	public CustomizedArena(int rowCount, int columnCount) throws ArenaException {
		super();
		this.rowCount = rowCount;
		this.columnCount = columnCount;
		this.cells = new ArenaTemplate.CellState[rowCount][columnCount];
		this.template = new ArenaTemplate();
		
		if((this.rowCount % this.template.getRowCount() != 0) 
			|| (this.columnCount % this.template.getColumnCount()) != 0){
			throw new ArenaException(
					0, "The customized arena's dimension is not a multiple of that of template"
					);
		}
	}
	
	
	
	public void setDescriptor(String descriptor) throws ArenaException{
		try {
			this.template.setArenaMapFromDescriptor(descriptor);
		} catch (ArenaTemplateException e) {
			throw new ArenaException(1,"The descriptor is invalid...");
		}
		if(unExploredCellExists()){
			throw new ArenaException(2, "The loading map has unexplored cell...");
		}
		mapTemplateToCustomized();
	}
	
	private void mapTemplateToCustomized() {
		// TODO Auto-generated method stub
		int rowScale = this.rowCount / this.template.getRowCount();
		int colScale = this.columnCount / this.template.getColumnCount();
		
		for(int rowID = 0;rowID < this.rowCount ; rowID++){
			for(int colID = 0;colID < this.columnCount ; colID++){
				this.cells[rowID][colID] =
						this.template.getCellState(rowID / rowScale,  colID / colScale);
			}
		}
	}



	public int getRowCount() {
		return rowCount;
	}



	public int getColumnCount() {
		return columnCount;
	}

//
//
//	public ArenaTemplate.CellState[][] getCells() {
//		return cells;
//	}

	public ArenaTemplate.CellState getCell(int rowID,int colID){
		return this.cells[rowID][colID];
	}

	private boolean unExploredCellExists(){
		int templateRowCount = this.template.getRowCount();
		int templateColCount = this.template.getColumnCount();
		
		for(int rowID = 0;rowID < templateRowCount;rowID++){
			for(int colID = 0; colID < templateColCount;colID++){
				if(this.template.getCellState(rowID, colID) == ArenaTemplate.CellState.UNEXPLORED){
					return true;
				}
			}
		}
		
		return false;
	}
	
	public String getDescriptors(){
		this.mapCustomizedToTemplate();
		return this.template.retrieveArenaDescriptor();
	}



	private void mapCustomizedToTemplate() {
		int rowScale = this.rowCount / this.template.getRowCount();
		int colScale = this.columnCount / this.template.getColumnCount();
		for (int templateRowID = 0;templateRowID < this.template.getRowCount();templateRowID++){
			for(int templateColID = 0;templateColID < this.template.getColumnCount();templateColID++){
				int rowID = rowScale * templateRowID;
				int colID = colScale * templateColID;
				CellState cellState = this.cells[rowID][colID];
				this.template.setCellState(templateRowID, templateColID, cellState);
			}
		}
	}
	
	public void setCellState(int rowID,int colID,CellState state){
		this.cells[rowID][colID] = state;
	}
	
	
}
