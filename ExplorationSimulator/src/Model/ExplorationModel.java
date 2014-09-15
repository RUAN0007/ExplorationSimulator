package Model;

import java.util.ArrayList;
import java.util.LinkedList;

import Model.ArenaTemplate.CellState;
import Model.CustomizedArena.ArenaException;
import Model.ExplorationComputer.ExplorationEnvironment;

public class ExplorationModel implements ExplorationEnvironment{
	private CustomizedArena realMap;
	private Robot robot;
	private ArrayList<Action> actions;
	
	private Block startSouthWestBlock;
	private Block goalSouthWestBlock;
	private Block robotInitSouthWestBlock;
	private Orientation robotInitStartOrientation;
	private ExplorationComputer explorationComputer;
	private Cell[][] status;

	public ExplorationModel(CustomizedArena realMap, Block startBlock, Block goalBlock) {
		super();
		
		this.realMap = realMap;
		this.startSouthWestBlock = startBlock;
		this.goalSouthWestBlock = goalBlock;
		
		this.actions = new ArrayList<>();
		this.explorationComputer = new ExplorationComputer(this.realMap.getRowCount(),this.realMap.getColumnCount(),this);
		this.status = new Cell[this.realMap.getRowCount()][this.realMap.getColumnCount()];
	}


	private void updateStatus() {
		Cell[][] mapStatus = this.explorationComputer.getExploredStatus();
		updateForStart(mapStatus);
		updateForGoal(mapStatus);
		updateForRobot(mapStatus);
		this.status = mapStatus;
	}
	
	private void updateForGoal(Cell[][] mapStatus) {
		int robotDiameterInCellNum = this.robot.getDiameterInCellNum();
		int southWestGoalRowID = this.goalSouthWestBlock.getRowID();
		int southWestGoalColID = this.goalSouthWestBlock.getColID();
		
		for(int rowID = 0; rowID < robotDiameterInCellNum; rowID++){
			for(int colID = 0;colID < robotDiameterInCellNum; colID++){
				if(mapStatus[southWestGoalRowID - rowID][southWestGoalColID + colID] == Cell.UNEXMPLORED) continue;
				mapStatus[southWestGoalRowID - rowID][southWestGoalColID + colID]
						= Cell.GOAL;
			}
		}
	}
	
	
	private void updateForStart(Cell[][] mapStatus) {
		int robotDiameterInCellNum = this.robot.getDiameterInCellNum();
		int southWestStartRowID = this.startSouthWestBlock.getRowID();
		int southWestStartColID = this.startSouthWestBlock.getColID();
		
		for(int rowID = 0; rowID < robotDiameterInCellNum; rowID++){
			for(int colID = 0;colID < robotDiameterInCellNum; colID++){
				if(mapStatus[southWestStartRowID - rowID][southWestStartColID + colID] == Cell.UNEXMPLORED) continue;
				mapStatus[southWestStartRowID - rowID][southWestStartColID + colID]
						= Cell.START;
			}
		}
	}
		
	
	
	private void updateForRobot(Cell[][] mapStatus) {
		int robotDiameterInCellNum = this.robot.getDiameterInCellNum();
		int cellRowIndex, cellColIndex;
		for(int rowOffset = 0;rowOffset < robotDiameterInCellNum;rowOffset++){
			cellRowIndex = this.robot.getSouthWestBlock().getRowID() - rowOffset;
			for(int colOffset = 0;colOffset < robotDiameterInCellNum;colOffset++){
				cellColIndex = this.robot.getSouthWestBlock().getColID() + colOffset;
				
				assert(mapStatus[cellRowIndex][cellColIndex] != Cell.OBSTACLE);
				mapStatus[cellRowIndex][cellColIndex] = Cell.ROBOT;		
			}
		}
		
		//Draw the Direction Cell
		
		if(this.robot.getCurrentOrientation().equals(Orientation.WEST)){
		
			cellRowIndex = this.robot.getSouthWestBlock().getRowID();
			cellColIndex = this.robot.getSouthWestBlock().getColID();

			for(int offset = 0;offset < robotDiameterInCellNum;offset++){
				mapStatus[cellRowIndex][cellColIndex] = Cell.ROBOT_DIRECTION;
				cellRowIndex --;
			}
		}else if(this.robot.getCurrentOrientation().equals(Orientation.EAST)){
		
			cellRowIndex = this.robot.getSouthWestBlock().getRowID();
			cellColIndex = this.robot.getSouthWestBlock().getColID() + robotDiameterInCellNum - 1;
			
			for(int offset = 0;offset < robotDiameterInCellNum;offset++){
				mapStatus[cellRowIndex][cellColIndex] = Cell.ROBOT_DIRECTION;
				cellRowIndex --;
			}
		}else if(this.robot.getCurrentOrientation().equals(Orientation.NORTH)){
			cellRowIndex = this.robot.getSouthWestBlock().getRowID() - robotDiameterInCellNum + 1;
			cellColIndex = this.robot.getSouthWestBlock().getColID();

			for(int offset = 0;offset < robotDiameterInCellNum;offset++){
				mapStatus[cellRowIndex][cellColIndex] = Cell.ROBOT_DIRECTION;
				cellColIndex ++;
			}
			
		}else if(this.robot.getCurrentOrientation().equals(Orientation.SOUTH)){
			cellRowIndex = this.robot.getSouthWestBlock().getRowID();
			cellColIndex = this.robot.getSouthWestBlock().getColID();

			for(int offset = 0;offset < robotDiameterInCellNum;offset++){
				mapStatus[cellRowIndex][cellColIndex] = Cell.ROBOT_DIRECTION;
				cellColIndex ++;
			}
		}
		
	}

	
	
	public boolean setRobot(Robot robot){
		if(obstacleInArea(robot.getSouthWestBlock().getRowID(),
						robot.getSouthWestBlock().getColID(),
						robot.getDiameterInCellNum())){
			return false;
		}else{
			
			this.initRobot(robot);
			this.explorationComputer.explore();
			updateStatus();
			return true;
		}
	}
	
	private void initRobot(Robot robot) {
		this.robot = robot;
		this.robotInitSouthWestBlock = this.robot.getSouthWestBlock().clone();
		this.robotInitStartOrientation = this.robot.getCurrentOrientation().clone();
		
		this.explorationComputer.setRobotsInitialCell(robot);
		
		
	}
	
	public void reset(){
		if(robot == null) {
			assert(false):"Should not reach here";
		}
		
		int rowCount = this.realMap.getRowCount();
		int columnCount = this.realMap.getColumnCount();
	
		this.explorationComputer = new ExplorationComputer(rowCount, columnCount,this);
		
		int diameterInCellNum = this.robot.getDiameterInCellNum();
		int explorationRange = this.robot.getExplorationRange();
		Robot newRobot = new Robot(this.robotInitSouthWestBlock.clone(),
								   diameterInCellNum, 
								   this.robotInitStartOrientation.clone(),
								   explorationRange);
		initRobot(newRobot);
		
		this.actions.clear();
		updateStatus();
	}

	private boolean obstacleInArea(int southWestRowID,
			int southWestColID, int span) {
		
		
		for(int rowID = 0;rowID < span;rowID++){
			for(int colID = 0;colID < span;colID++){
				if(this.realMap.getCell(southWestRowID - rowID,southWestColID + colID)
						== ArenaTemplate.CellState.OBSTACLE){
					return true;
				}
			}
		}
		return false;
	}
	
	public Cell getCellStatus(int rowID,int colID){
		return this.status[rowID][colID];
	}
	
	//Return the description of the action or NULL if no action 
	public String forward(){
		if(this.explorationComputer.isFinished(this.robot)) return null;
		
		Action next = this.explorationComputer.getNextStep(this.robot);
		if(next.equals(Action.MOVE_FORWARD)) {
			markBorderAsPseudoObstacle(Direction.LEFT);
		}
		robot.move(next);
		this.explorationComputer.explore();
		updateStatus();
		
		this.actions.add(next);
		return next.toString();
		
	}
	
	//Mark the cell occupied by the robot's left border as pseudo obstacles
	private void markBorderAsPseudoObstacle(Direction direction) {
//		int robotDiamterInCellNum = this.robot.getDiameterInCellNum();
//		Orientation borderOrientation = this.robot.getCurrentOrientation().relativeToLeft();
//
//		if(borderOrientation.equals(Orientation.NORTH)){
//			int rowID = this.robot.getSouthWestBlock().getRowID() - robotDiamterInCellNum + 1;
//			for(int colOffset = 0;colOffset < robotDiamterInCellNum;colOffset++){
//				int colID = this.robot.getSouthWestBlock().getColID() + colOffset;			
//				this.pseudoObstacleExists[rowID][colID] = true;
//			}
//		}else if(borderOrientation.equals(Orientation.EAST)){
//			int colID = this.robot.getSouthWestBlock().getColID() + robotDiamterInCellNum - 1;
//			for(int rowOffset = 0;rowOffset < robotDiamterInCellNum;rowOffset++){
//				int rowID = this.robot.getSouthWestBlock().getRowID() - rowOffset;
//				this.pseudoObstacleExists[rowID][colID] = true;
//			}
//		}else if(borderOrientation.equals(Orientation.SOUTH)){
//			int rowID = this.robot.getSouthWestBlock().getRowID();
//			for(int colOffset = 0;colOffset < robotDiamterInCellNum;colOffset++){
//				int colID = this.robot.getSouthWestBlock().getColID() + colOffset;
//				this.pseudoObstacleExists[rowID][colID] = true;
//			}
//		}else if(borderOrientation.equals(Orientation.WEST)){
//			int colID = this.robot.getSouthWestBlock().getColID();
//			for(int rowOffset = 0;rowOffset < robotDiamterInCellNum;rowOffset++){
//				int rowID = this.robot.getSouthWestBlock().getRowID() - rowOffset;
//				this.pseudoObstacleExists[rowID][colID] = true;
//
//			}
//		}else{
//			assert(false):"No other orientation";
//		}
	}


	public String getExploredDescriptor(){
		return this.explorationComputer.getMapDescriptor();
	}
	
	
	
	
	//Explore the front explorationRange * explorationRange block along the direction
	public void explore(CustomizedArena exploredMap){
		
		int robotLeftFrontRowID;
		int robotLeftFrontColID;
		
		int robotRightFrontRowID;
		int robotRightFrontColID;
		
		int robotDiameterInCellNum = this.robot.getDiameterInCellNum();
		int robotExplorationRange = this.robot.getExplorationRange();
		if(this.robot.getCurrentOrientation().equals(Orientation.NORTH)){
			robotLeftFrontRowID = this.robot.getSouthWestBlock().getRowID() - robotDiameterInCellNum + 1;
			robotLeftFrontColID = this.robot.getSouthWestBlock().getColID();
			robotRightFrontRowID = robotLeftFrontRowID;
			robotRightFrontColID = robotLeftFrontColID + robotDiameterInCellNum - 1;
			
			for(int colID = robotLeftFrontColID;colID <= robotRightFrontColID;colID++){
				for(int rowOffset = 1;rowOffset <= robotExplorationRange;rowOffset++){
					int rowID = robotLeftFrontRowID - rowOffset;
					if(!withInArenaRange(rowID, colID)) break;
					
					CellState state = this.exploreBlock(rowID, colID);
					exploredMap.setCellState(rowID, colID, state);
					
					if(exploredMap.getCell(rowID, colID) == CellState.OBSTACLE) break;
				}
			}
		}else if(this.robot.getCurrentOrientation().equals(Orientation.EAST)){
			robotLeftFrontRowID = this.robot.getSouthWestBlock().getRowID() - robotDiameterInCellNum + 1;
			robotLeftFrontColID = this.robot.getSouthWestBlock().getColID() + robotDiameterInCellNum - 1;
			robotRightFrontRowID = this.robot.getSouthWestBlock().getRowID();
			robotRightFrontColID = robotLeftFrontColID;
			
			for(int rowID = robotLeftFrontRowID;rowID <= robotRightFrontRowID;rowID++){
				for(int colOffset = 1;colOffset <= robotExplorationRange;colOffset++){
					int colID = robotLeftFrontColID + colOffset;
					if(!withInArenaRange(rowID, colID)) break;
					CellState state = this.exploreBlock(rowID, colID);
					exploredMap.setCellState(rowID, colID, state);
					if(exploredMap.getCell(rowID, colID) == CellState.OBSTACLE) break;
				
				}
			}
			
		}else if(this.robot.getCurrentOrientation().equals(Orientation.SOUTH)){
			robotLeftFrontRowID = this.robot.getSouthWestBlock().getRowID();
			robotLeftFrontColID = this.robot.getSouthWestBlock().getColID() + robotDiameterInCellNum - 1;
			robotRightFrontRowID = robotLeftFrontRowID;
			robotRightFrontColID = this.robot.getSouthWestBlock().getColID();
			
			for(int colID = robotRightFrontColID; colID <= robotLeftFrontColID;colID++){
				for(int rowOffset = 1;rowOffset <= robotExplorationRange;rowOffset++){
					int rowID = robotLeftFrontRowID + rowOffset;
					
					if(!withInArenaRange(rowID, colID)) break;
					CellState state = this.exploreBlock(rowID, colID);
					exploredMap.setCellState(rowID, colID, state);
					if(exploredMap.getCell(rowID, colID) == CellState.OBSTACLE) break;
				
				}
			}
		}else if(this.robot.getCurrentOrientation().equals(Orientation.WEST)){
			robotLeftFrontRowID = this.robot.getSouthWestBlock().getRowID();
			robotRightFrontRowID = robotLeftFrontRowID - robotDiameterInCellNum + 1;
			robotLeftFrontColID = this.robot.getSouthWestBlock().getColID();
			robotRightFrontColID = robotLeftFrontColID;
			
			for(int rowID = robotRightFrontRowID;rowID <= robotLeftFrontRowID;rowID++){
				for(int colOffset = 1;colOffset <= robotExplorationRange;colOffset++){
					int colID = robotLeftFrontColID - colOffset;
					if(!withInArenaRange(rowID, colID)) break;
					CellState state = this.exploreBlock(rowID, colID);
					exploredMap.setCellState(rowID, colID, state);
					if(exploredMap.getCell(rowID, colID) == CellState.OBSTACLE) break;
				
				}
			}
			
		}else{
			assert(false):"No other ORIENTAIN AVAILABLE...";
		}
	}
	
	private CellState exploreBlock(int rowID,int colID){
		return this.realMap.getCell(rowID, colID);
	}
	
	private boolean withInArenaRange(int rowID,int colID){
		int rowCount = this.realMap.getRowCount();
		int colCount = this.realMap.getColumnCount();
		
		if(rowID < 0 || rowID >= rowCount) return false;
		if(colID < 0 || colID >= colCount) return false;
		return true;
	}
	
	public int getCurrentTurnCount(){
		int count = 0;
		for (Action action:this.actions){
			if(action == Action.TURN_LEFT 
				|| action == Action.TURN_RIGHT){
				count++;
			}
		}
		return count;
	}
	
	public int getCurrentStepCount(){
		int count = 0;
		for (Action action:this.actions){
			if(action == Action.MOVE_FORWARD 
				|| action == Action.DRAW_BACK){
				count++;
			}
		}
		return count;
	}


	public boolean isFinished() {
		//	if(!this.robot.getSouthWestBlock().equals(goalSouthWestBlock)) return false;

		return this.explorationComputer.isFinished(this.robot);
	}


	public double getCoverage() {
		return this.explorationComputer.getCoverage();
	}
	
	
	
	
	
	
	
}
