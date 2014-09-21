package Model;

import Model.ArenaTemplate.CellState;
import Model.CustomizedArena.ArenaException;

public abstract class ExplorationComputer {

	public interface ExplorationEnvironment{
		public void explore(CustomizedArena exploredMap);
	}
	
	protected CustomizedArena exploredMap;
	protected ExplorationEnvironment env;

	public ExplorationComputer() {
		super();
	}

	protected void initExploredMap(int rowCount, int columnCount) {
		try {
			this.exploredMap = new CustomizedArena(rowCount, columnCount);
		} catch (ArenaException e) {
			e.printStackTrace();
			assert(false):"Should not reach here";
		}
		for(int rowID = 0;rowID < rowCount;rowID++){
			for(int colID = 0;colID < columnCount;colID++){
				this.exploredMap.setCellState(rowID, colID, CellState.UNEXPLORED);
			}
	
		}
	}

	public Cell[][] getExploredStatus() {
		int rowCount = this.exploredMap.getRowCount();
		int colCount = this.exploredMap.getColumnCount();
		Cell[][] status = new Cell[rowCount][colCount];
	
		for(int rowID = 0;rowID < this.exploredMap.getRowCount();rowID++){
			for(int colID = 0;colID < this.exploredMap.getColumnCount();colID++){
				if(this.exploredMap.getCell(rowID,colID) == CellState.OBSTACLE){
					status[rowID][colID] = Cell.OBSTACLE;
				}else if(this.exploredMap.getCell(rowID,colID) == CellState.EMPTY){
					status[rowID][colID] = Cell.EMPTY;
				}else if(this.exploredMap.getCell(rowID,colID) == CellState.UNEXPLORED){
					status[rowID][colID] = Cell.UNEXMPLORED;
				}else{
					assert(false):"Should not reach here";
				}
			}
		}
		return status;
	}

	public void setRobotsInitialCell(Robot robot) {
	
		int span = robot.getDiameterInCellNum();
		int southWestColID = robot.getSouthWestBlock().getColID();
		int southWestRowID = robot.getSouthWestBlock().getRowID();
	
	
		for(int rowID = 0;rowID < span;rowID++){
			for(int colID = 0;colID < span;colID++){
				this.exploredMap.setCellState(southWestRowID - rowID,southWestColID + colID,CellState.EMPTY); 
			}
		}			
	}

	public String getMapDescriptor() {
		return this.exploredMap.getDescriptors();
	}

	public void explore() {
		this.env.explore(this.exploredMap);
	}
	
	public double getCoverage() {
		int unExploredCount = 0;
		int totalCount = 0;
		for(int rowID = 0;rowID < this.exploredMap.getRowCount();rowID++){
			for(int colID = 0;colID < this.exploredMap.getColumnCount();colID++){
				totalCount++;
				if(this.exploredMap.getCell(rowID,colID) == CellState.UNEXPLORED){
					unExploredCount++;
				}
			}
		}
		double coverageRate = 1.0 -  (double)unExploredCount/(double)totalCount;
		assert(-0.1 < coverageRate && coverageRate < 1.1):"Illegail Coverage: " + coverageRate;
		return coverageRate;
	}
	
	//Return null if no further action needed
	public abstract Action getNextStep(Robot robot);

}