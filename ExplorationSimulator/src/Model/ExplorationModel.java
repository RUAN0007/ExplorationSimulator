package Model;

import java.util.ArrayList;
import java.util.LinkedList;

import Model.ArenaTemplate.CellState;
import Model.CustomizedArena.ArenaException;

public class ExplorationModel {
	private CustomizedArena realMap;
	private CustomizedArena exploredMap;
	private Robot robot;
	private FastestPathComputer pathComputer;
	private ArrayList<Action> actions;
	
	private Block startSouthWestBlock;
	private Block goalSouthWestBlock;
	private Block robotInitSouthWestBlock;
	private Orientation robotInitStartOrientation;
	private Cell[][] status;
	private boolean[][] pseudoObstacleExists;
	

	public ExplorationModel(CustomizedArena realMap,
			FastestPathComputer pathComputer, Block startBlock, Block goalBlock) {
		super();
		this.realMap = realMap;
		this.pathComputer = pathComputer;
		this.startSouthWestBlock = startBlock;
		this.goalSouthWestBlock = goalBlock;
		
		this.actions = new ArrayList<>();
		this.initExploredMap(this.exploredMap.getRowCount(),this.exploredMap.getColumnCount());
		this.status = new Cell[this.exploredMap.getRowCount()][this.exploredMap.getColumnCount()];
		this.initPseudoObstacle();
		this.updateStatus();
	}

	private void initPseudoObstacle() {
		int rowCount = this.realMap.getRowCount();
		int colCount = this.realMap.getColumnCount();
		this.pseudoObstacleExists = new boolean[rowCount][colCount];
		for(int rowID = 0;rowID < rowCount ; rowID++){
			for(int colID = 0;colID < colCount;colID++){
				this.pseudoObstacleExists[rowID][colID] = false;
			}
		}
	}

	private void updateStatus() {
		updateForArenaMap();
//		updateForStart();
//		updateForGoal();
		updateForRobot();
	}
	
	private void updateForGoal() {
		int robotDiameterInCellNum = this.robot.getDiameterInCellNum();
		int southWestGoalRowID = this.goalSouthWestBlock.getRowID();
		int southWestGoalColID = this.goalSouthWestBlock.getColID();
		
		for(int rowID = 0; rowID < robotDiameterInCellNum; rowID++){
			for(int colID = 0;colID < robotDiameterInCellNum; colID++){
				if(this.exploredMap.getCell(southWestGoalRowID - rowID,southWestGoalColID + colID) == CellState.UNEXPLORED) continue;
				this.status[southWestGoalRowID - rowID][southWestGoalColID + colID]
						= Cell.GOAL;
			}
		}
	}
	
	
	private void updateForStart() {
		int robotDiameterInCellNum = this.robot.getDiameterInCellNum();
		int southWestStartRowID = this.startSouthWestBlock.getRowID();
		int southWestStartColID = this.startSouthWestBlock.getColID();
		
		for(int rowID = 0; rowID < robotDiameterInCellNum; rowID++){
			for(int colID = 0;colID < robotDiameterInCellNum; colID++){
				if(this.exploredMap.getCell(southWestStartRowID - rowID,southWestStartColID + colID) == CellState.UNEXPLORED) continue;
				this.status[southWestStartRowID - rowID][southWestStartColID + colID]
						= Cell.START;
			}
		}
	}
		
	//Update the current status based on what the robot has explored
	private void updateForArenaMap() {
		for(int rowID = 0;rowID < this.exploredMap.getRowCount();rowID++){
			for(int colID = 0;colID < this.exploredMap.getColumnCount();colID++){
				if(this.exploredMap.getCell(rowID,colID) == CellState.OBSTACLE){
					this.status[rowID][colID] = Cell.OBSTACLE;
				}else if(this.exploredMap.getCell(rowID,colID) == CellState.EMPTY){
					this.status[rowID][colID] = Cell.EMPTY;
				}else if(this.exploredMap.getCell(rowID,colID) == CellState.UNEXPLORED){
					this.status[rowID][colID] = Cell.UNEXMPLORED;
				}else{
					assert(false):"Should not reach here";
				}
			}
		}
	}
	
	private void updateForRobot() {
		int robotDiameterInCellNum = this.robot.getDiameterInCellNum();
		int cellRowIndex, cellColIndex;
		for(int rowOffset = 0;rowOffset < robotDiameterInCellNum;rowOffset++){
			cellRowIndex = this.robot.getSouthWestBlock().getRowID() - rowOffset;
			for(int colOffset = 0;colOffset < robotDiameterInCellNum;colOffset++){
				cellColIndex = this.robot.getSouthWestBlock().getColID() + colOffset;
				
				assert(this.status[cellRowIndex][cellColIndex] != Cell.OBSTACLE);
				this.status[cellRowIndex][cellColIndex] = Cell.ROBOT;		
			}
		}
		
		//Draw the Direction Cell
		
		if(this.robot.getCurrentOrientation().equals(Orientation.WEST)){
		
			cellRowIndex = this.robot.getSouthWestBlock().getRowID();
			cellColIndex = this.robot.getSouthWestBlock().getColID();

			for(int offset = 0;offset < robotDiameterInCellNum;offset++){
				this.status[cellRowIndex][cellColIndex] = Cell.ROBOT_DIRECTION;
				cellRowIndex --;
			}
		}else if(this.robot.getCurrentOrientation().equals(Orientation.EAST)){
		
			cellRowIndex = this.robot.getSouthWestBlock().getRowID();
			cellColIndex = this.robot.getSouthWestBlock().getColID() + robotDiameterInCellNum - 1;
			
			for(int offset = 0;offset < robotDiameterInCellNum;offset++){
				this.status[cellRowIndex][cellColIndex] = Cell.ROBOT_DIRECTION;
				cellRowIndex --;
			}
		}else if(this.robot.getCurrentOrientation().equals(Orientation.NORTH)){
			cellRowIndex = this.robot.getSouthWestBlock().getRowID() - robotDiameterInCellNum + 1;
			cellColIndex = this.robot.getSouthWestBlock().getColID();

			for(int offset = 0;offset < robotDiameterInCellNum;offset++){
				this.status[cellRowIndex][cellColIndex] = Cell.ROBOT_DIRECTION;
				cellColIndex ++;
			}
			
		}else if(this.robot.getCurrentOrientation().equals(Orientation.SOUTH)){
			cellRowIndex = this.robot.getSouthWestBlock().getRowID();
			cellColIndex = this.robot.getSouthWestBlock().getColID();

			for(int offset = 0;offset < robotDiameterInCellNum;offset++){
				this.status[cellRowIndex][cellColIndex] = Cell.ROBOT_DIRECTION;
				cellColIndex ++;
			}
		}
		
	}

	//Init the exploreMap and set all its cell to be unexplored
	private void initExploredMap(int rowCount, int columnCount) {
		try {
			this.exploredMap = new CustomizedArena(rowCount, columnCount);
			this.exploredMap.setDescriptor("C000000000000000000000000000000000000000000000000000000000000000000000000003");

		} catch (ArenaException e) {
			assert(false):"Should not reach here...";
			e.printStackTrace();
		}
	}
	
	public boolean setRobot(Robot robot){
		if(obstacleInArea(robot.getSouthWestBlock().getRowID(),
						robot.getSouthWestBlock().getColID(),
						robot.getDiameterInCellNum())){
			return false;
		}else{
			
			this.initRobot(robot);
			updateStatus();
			return true;
		}
	}
	
	private void initRobot(Robot robot) {
		this.robot = robot;
		this.robotInitSouthWestBlock = this.robot.getSouthWestBlock().clone();
		this.robotInitStartOrientation = this.robot.getCurrentOrientation().clone();
		
		int span = this.robot.getDiameterInCellNum();
		int southWestColID = this.robot.getSouthWestBlock().getColID();
		int southWestRowID = this.robot.getSouthWestBlock().getRowID();

		
		for(int rowID = 0;rowID < span;rowID++){
			for(int colID = 0;colID < span;colID++){
				this.exploredMap.setCellState(southWestRowID - rowID,southWestColID + colID,CellState.EMPTY); 
			}
		}
	}
	
	public void reset(){
		if(robot == null) {
			assert(false):"Should not reach here";
		}
		
		int rowCount = this.realMap.getRowCount();
		int columnCount = this.realMap.getColumnCount();
	
		this.initExploredMap(rowCount, columnCount);
		initPseudoObstacle();
		
		int diameterInCellNum = this.robot.getDiameterInCellNum();
		int explorationRange = this.robot.getExplorationRange();
		Robot newRobot = new Robot(this.robotInitSouthWestBlock.clone(),
								   diameterInCellNum, 
								   this.robotInitStartOrientation.clone(),
								   explorationRange);
		initRobot(newRobot);
		
		this.actions.clear();
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
		if(isFinished()) return null;
		
		Action next = getNextStep();
		if(next.equals(Action.MOVE_FORWARD)) {
			markBorderAsPseudoObstacle(Direction.LEFT);
		}
		robot.move(next);
		explore();
		updateStatus();
		
		this.actions.add(next);
		return next.toString();
		
	}
	
	//Mark the cell occupied by the robot's left border as pseudo obstacles
	private void markBorderAsPseudoObstacle(Direction direction) {
		int robotDiamterInCellNum = this.robot.getDiameterInCellNum();
		Orientation borderOrientation = this.robot.getCurrentOrientation().relativeToLeft();

		if(borderOrientation.equals(Orientation.NORTH)){
			int rowID = this.robot.getSouthWestBlock().getRowID() - robotDiamterInCellNum + 1;
			for(int colOffset = 0;colOffset < robotDiamterInCellNum;colOffset++){
				int colID = this.robot.getSouthWestBlock().getColID() + colOffset;			
				this.pseudoObstacleExists[rowID][colID] = true;
			}
		}else if(borderOrientation.equals(Orientation.EAST)){
			int colID = this.robot.getSouthWestBlock().getColID() + robotDiamterInCellNum - 1;
			for(int rowOffset = 0;rowOffset < robotDiamterInCellNum;rowOffset++){
				int rowID = this.robot.getSouthWestBlock().getRowID() - rowOffset;
				this.pseudoObstacleExists[rowID][colID] = true;
			}
		}else if(borderOrientation.equals(Orientation.SOUTH)){
			int rowID = this.robot.getSouthWestBlock().getRowID();
			for(int colOffset = 0;colOffset < robotDiamterInCellNum;colOffset++){
				int colID = this.robot.getSouthWestBlock().getColID() + colOffset;
				this.pseudoObstacleExists[rowID][colID] = true;
			}
		}else if(borderOrientation.equals(Orientation.WEST)){
			int colID = this.robot.getSouthWestBlock().getColID();
			for(int rowOffset = 0;rowOffset < robotDiamterInCellNum;rowOffset++){
				int rowID = this.robot.getSouthWestBlock().getRowID() - rowOffset;
				this.pseudoObstacleExists[rowID][colID] = true;

			}
		}else{
			assert(false):"No other orientation";
		}
	}

	private LinkedList<Action> nextActions = new LinkedList<Action>();
	private boolean exploreStage = false;
	
	private Action getNextStep(){
		if(nextActions.size() == 0){
			if(exploreStage){
				exploreStage = false;
				
				nextActions.add(Action.TURN_LEFT);
				nextActions.add(Action.TURN_RIGHT);
				nextActions.add(Action.TURN_RIGHT);
				nextActions.add(Action.TURN_LEFT);
			}else{
				exploreStage = true;
				
				boolean moved = false;
				Orientation robotOrientation = this.robot.getCurrentOrientation();
			
				if(canMoveTowards(robotOrientation.relativeToLeft())){
					nextActions.add(Action.TURN_LEFT);
					nextActions.add(Action.MOVE_FORWARD);
					moved = true;
				}
				if(!moved && canMoveTowards(robotOrientation)){
					nextActions.add(Action.MOVE_FORWARD);
					moved = true;
				}
				
				if(!moved && canMoveTowards(robotOrientation.relativeToRight())){
					nextActions.add(Action.TURN_RIGHT);
					nextActions.add(Action.MOVE_FORWARD);
					moved = true;
				}
				if(!moved){
					nextActions.add(Action.TURN_RIGHT);
					nextActions.add(Action.TURN_RIGHT);
				}
			}
		}
				
		return nextActions.pollFirst();
	}
	
	private boolean canMoveTowards(Orientation ori){
		if(ori.equals(Orientation.NORTH)) return canMoveNorth();
		if(ori.equals(Orientation.WEST)) return canMoveWest();
		if(ori.equals(Orientation.SOUTH)) return canMoveSouth();
		if(ori.equals(Orientation.EAST)) return canMoveEast();
		assert(false):"no other orientaion...";
		return false;
	}

	
	private boolean canMoveNorth() {
		int robotDiamterInCellNum = this.robot.getDiameterInCellNum();
		int rowID = this.robot.getSouthWestBlock().getRowID() - robotDiamterInCellNum;
		for(int colOffset = 0;colOffset < robotDiamterInCellNum;colOffset++){
			int colID = this.robot.getSouthWestBlock().getColID() + colOffset;
			if (!withInArenaRange(rowID, colID)) return false;
			assert(this.exploredMap.getCell(rowID, colID) != CellState.UNEXPLORED);

			if(this.exploredMap.getCell(rowID, colID) == CellState.OBSTACLE) return false;
			if(this.pseudoObstacleExists[rowID][colID]) return false;
		}
		return true;
	}
	
	private boolean canMoveEast() {
		int robotDiamterInCellNum = this.robot.getDiameterInCellNum();
		int colID = this.robot.getSouthWestBlock().getColID() + robotDiamterInCellNum;
		for(int rowOffset = 0;rowOffset < robotDiamterInCellNum;rowOffset++){
			int rowID = this.robot.getSouthWestBlock().getRowID() - rowOffset;
			if (!withInArenaRange(rowID, colID)) return false;
			assert(this.exploredMap.getCell(rowID, colID) != CellState.UNEXPLORED);
			if(this.exploredMap.getCell(rowID, colID) == CellState.OBSTACLE) return false;
			if(this.pseudoObstacleExists[rowID][colID]) return false;
		}
		return true;
	}
	
	private boolean canMoveWest() {
		int robotDiamterInCellNum = this.robot.getDiameterInCellNum();
		int colID = this.robot.getSouthWestBlock().getColID() - 1;
		for(int rowOffset = 0;rowOffset < robotDiamterInCellNum;rowOffset++){
			int rowID = this.robot.getSouthWestBlock().getRowID() - rowOffset;
			if (!withInArenaRange(rowID, colID)) return false;
			assert(this.exploredMap.getCell(rowID, colID) != CellState.UNEXPLORED);

			if(this.exploredMap.getCell(rowID, colID) == CellState.OBSTACLE) return false;
			if(this.pseudoObstacleExists[rowID][colID]) return false;
		}
		return true;
	}
	
	private boolean canMoveSouth() {
		int robotDiamterInCellNum = this.robot.getDiameterInCellNum();
		int rowID = this.robot.getSouthWestBlock().getRowID() + 1;
		for(int colOffset = 0;colOffset < robotDiamterInCellNum;colOffset++){
			int colID = this.robot.getSouthWestBlock().getColID() + colOffset;
			if (!withInArenaRange(rowID, colID)) return false;
			assert(this.exploredMap.getCell(rowID, colID) != CellState.UNEXPLORED);

			if(this.exploredMap.getCell(rowID, colID) == CellState.OBSTACLE) return false;
			if(this.pseudoObstacleExists[rowID][colID]) return false;
		}
		return true;
	}

	public boolean isFinished() {
		if(robot == null) return false;
		if(getCoverage() < 1.0) return false;
	//TODO
		//	if(!this.robot.getSouthWestBlock().equals(goalSouthWestBlock)) return false;
		return true;
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
		double coverageRate =  (double)unExploredCount/(double)totalCount;
		assert(-0.1 < coverageRate && coverageRate < 1.1):"Illegail Coverage: " + coverageRate;
		return coverageRate;
	}

	public String getExploredDescriptor(){
		return this.exploredMap.getDescriptors();
	}
	
	
	
	
	//Explore the front explorationRange * explorationRange block along the direction
	private void explore(){
		
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
			
			for(int colID = robotLeftFrontColID;colID <= robotRightFrontRowID;colID++){
				for(int rowOffset = 1;rowOffset <= robotExplorationRange;rowOffset++){
					int rowID = robotLeftFrontRowID - rowOffset;
					if(!withInArenaRange(rowID, colID)) break;
					this.exploreBlock(rowID, colID);
					if(this.exploredMap.getCell(rowID, colID) == CellState.OBSTACLE) break;
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
					this.exploreBlock(rowID, colID);
					if(this.exploredMap.getCell(rowID, colID) == CellState.OBSTACLE) break;
				
				}
			}
			
		}else if(this.robot.getCurrentOrientation().equals(Orientation.SOUTH)){
			robotLeftFrontRowID = this.robot.getSouthWestBlock().getRowID();
			robotLeftFrontColID = this.robot.getSouthWestBlock().getColID() + robotDiameterInCellNum - 1;
			robotRightFrontRowID = robotLeftFrontRowID;
			robotRightFrontColID = this.robot.getSouthWestBlock().getColID();
			
			for(int colID = robotRightFrontColID; colID <= robotLeftFrontColID;colID++){
				for(int rowOffset = 1;rowOffset <= robotExplorationRange;rowOffset++){
					int rowID = robotLeftFrontColID + rowOffset;
					
					if(!withInArenaRange(rowID, colID)) break;
					this.exploreBlock(rowID, colID);
					if(this.exploredMap.getCell(rowID, colID) == CellState.OBSTACLE) break;
				
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
					this.exploreBlock(rowID, colID);
					if(this.exploredMap.getCell(rowID, colID) == CellState.OBSTACLE) break;
				
				}
			}
			
		}else{
			assert(false):"No other ORIENTAIN AVAILABLE...";
		}
	}
	
	private void exploreBlock(int rowID,int colID){
		CellState cellState = this.realMap.getCell(rowID, colID);
		this.exploredMap.setCellState(rowID, colID, cellState);
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
	
	
	
	
	
	
	
}
