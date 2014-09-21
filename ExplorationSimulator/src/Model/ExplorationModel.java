package Model;

import java.util.ArrayList;

import Model.ArenaTemplate.CellState;
import Model.ExplorationComputer.ExplorationEnvironment;

public class ExplorationModel implements ExplorationEnvironment{
	private CustomizedArena realMap;
	private Robot robot;
	private ArrayList<Action> actions;
	
	private Block startSouthWestBlock;
	private Block goalSouthWestBlock;
	private Block robotInitSouthWestBlock;
	private Orientation robotInitStartOrientation;
	private FastestPathComputer pathComputer = new MinStepTurnPathComputer(1, 1);
	private ExplorationComputer explorationComputer;
	private Cell[][] status;
	
	public ExplorationModel(CustomizedArena realMap, Block startBlock, Block goalBlock) {
		super();
		
		this.realMap = realMap;
		this.startSouthWestBlock = startBlock;
		this.goalSouthWestBlock = goalBlock;
	
		this.actions = new ArrayList<>();
		this.explorationComputer = new SpiralExplorationComputer(this.realMap.getRowCount(),this.realMap.getColumnCount(),this);
		this.status = new Cell[this.realMap.getRowCount()][this.realMap.getColumnCount()];
		updateStatus();
	}


	private void updateStatus() {
		Cell[][] mapStatus = this.explorationComputer.getExploredStatus();
		if(robot != null){

			updateForStart(mapStatus);
			updateForGoal(mapStatus);
			updateForRobot(mapStatus);

		}
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
		if(!availableArea(robot.getSouthWestBlock().getRowID(),
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
	
		this.explorationComputer = new SpiralExplorationComputer(rowCount, columnCount,this);
		
		int diameterInCellNum = this.robot.getDiameterInCellNum();
		int explorationRange = this.robot.getExplorationRange();
		Robot newRobot = new Robot(this.robotInitSouthWestBlock.clone(),
								   diameterInCellNum, 
								   this.robotInitStartOrientation.clone(),
								   explorationRange);
		initRobot(newRobot);
		
		this.actions.clear();
		this.explorationComputer.explore();
		updateStatus();
	}

	private boolean availableArea(int southWestRowID,
			int southWestColID, int span) {
		
		int rowCount = this.realMap.getRowCount();
		int colCount = this.realMap.getColumnCount();
		for(int rowID = 0;rowID < span;rowID++){
			if(!(0 <= rowID && rowID < rowCount)){
				return false;
			}

			for(int colID = 0;colID < span;colID++){
				
				if(!(0 <= colID && colID < colCount)){
					return false;
				}
				
				if(this.realMap.getCell(southWestRowID - rowID,southWestColID + colID)
						== ArenaTemplate.CellState.OBSTACLE){
					return false;
				}
			}
		}
		return true;
	}
	
	public Cell getCellStatus(int rowID,int colID){
		return this.status[rowID][colID];
	}
	
	private ArrayList<Action> actionsToGoal = null;
	//Return the description of the action or NULL if no action 
	
	public String forward(){
		Action next = null;
		if(this.isFinished()) return null;
		if(this.explorationComputer.getCoverage() < 0.999999){
			next = this.explorationComputer.getNextStep(this.robot);
			if(next == null) return null;
			robot.move(next);
			this.explorationComputer.explore();
		}else{
			//Finish exploration, move to goal
			if(this.actionsToGoal == null){
				this.actionsToGoal = this.pathComputer.computeForFastestPath(this.realMap,
																			robot, 
																			this.goalSouthWestBlock.getRowID(),
																			this.goalSouthWestBlock.getColID());

			}
			
			next = this.actionsToGoal.remove(0);
			robot.move(next);

		}
		
		updateStatus();
		
		this.actions.add(next);
		return next.toString();
		
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
		if(this.explorationComputer.getCoverage() < 0.9999) return false;
		if(!this.robot.getSouthWestBlock().equals(goalSouthWestBlock)) return false;

		return true;
	}


	public double getCoverage() {
		return this.explorationComputer.getCoverage();
	}
	
	
	
	
	
	
	
}
