package Model;

import java.util.ArrayList;

import Model.ArenaTemplate.CellState;

public abstract class FastestPathComputer {
	
	
	public abstract ArrayList<Action> compute(Integer[][]map,int rowCount,int colCount,
							int startRowID,int startColID,Orientation startOrientation,
							int goalRowID,int goalColID);
	
	public ArrayList<Action> computeForFastestPath(CustomizedArena arena,Robot robot,
										int goalRowID,int goalColID){
		//Assume the robot is at the start area
		int rowCount = arena.getRowCount();
		int colCount = arena.getColumnCount();
		
		int startRowID = robot.getSouthWestBlock().getRowID();
		int startColID = robot.getSouthWestBlock().getColID();
		Orientation startOrientation = robot.getCurrentOrientation();

		int robotDiameterInCellNum = robot.getDiameterInCellNum();
		Integer[][] logicalMap = getLogicalMap(arena,robotDiameterInCellNum);
		return compute(logicalMap,
						rowCount, 
						colCount, 
						startRowID, 
						startColID, 
						startOrientation, 
						goalRowID, 
						goalColID);
	}
	
	
	//Return a logical map so that 
	//if the lower-left corner of the robot can not reach to the cell at Row i and Column j,
	//then logicalMap[i][j] = 1, else logicalMap[i][j] = 0
	private Integer[][] getLogicalMap(CustomizedArena arena,
			int robotDiameterInCellNum) {
		int rowCount = arena.getRowCount();
		int columnCount = arena.getColumnCount();
		Integer[][] logicalMap = new Integer[rowCount][columnCount];
		
		//For each obstacle cell at Row i, Col j,
		//Mark logicalMap[i ~ (i + robotDiameterInCellNum - 1)][(j - robotDiameterInCellNum + 1) ~ j] to 1
		//Mark the top two rows and rightmost columns of logicalMap to 1
		//Else Mark to 0
		
		for(int rowID = 0;rowID < rowCount;rowID++){
			
			for(int colID = 0;colID < columnCount;colID++){
				if(rowID < robotDiameterInCellNum - 1){
					logicalMap[rowID][colID] = new Integer(1);
					continue;
				}
				if(colID > columnCount - robotDiameterInCellNum){
					logicalMap[rowID][colID] = new Integer(1);
					continue;
				}
				boolean obstacleInArea = false;
				for(int rowSpan = 0;rowSpan < robotDiameterInCellNum;rowSpan++){
					for(int colSpan = 0;colSpan < robotDiameterInCellNum;colSpan++){
						if(arena.getCell(rowID - rowSpan,colID + colSpan) == CellState.OBSTACLE){
							obstacleInArea = true;
						}
					}
				}
				if(obstacleInArea){
					logicalMap[rowID][colID] = new Integer(1);

				}else{
					logicalMap[rowID][colID] = new Integer(0);

				}
			}
		}
		
		
		return logicalMap;
	}
}
