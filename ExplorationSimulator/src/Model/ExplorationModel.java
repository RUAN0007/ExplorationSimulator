package Model;

import java.util.ArrayList;

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
		this.updateStatus();
	}

	private void updateStatus() {
		updateForArenaMap();
		updateForStart();
		updateForGoal();
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
			// TODO Auto-generated catch block
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
		
		
		int diameterInCellNum = this.robot.getDiameterInCellNum();
		Robot newRobot = new Robot(this.robotInitSouthWestBlock.clone(),
								   diameterInCellNum, 
								   this.robotInitStartOrientation);
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
		robot.move(next);
		explore();
		updateStatus();
		
		this.actions.add(next);
		return next.toString();
		
	}
	
	public boolean isFinished() {
		if(robot == null) return false;
		if(getCoverage() < 1.0) return false;
		if(!this.robot.getSouthWestBlock().equals(goalSouthWestBlock)) return false;
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
		return (double)unExploredCount/(double)totalCount;
		
	}

	public String getExploredDescriptor(){
		return this.exploredMap.getDescriptors();
	}
	
	private Action getNextStep(){
		//TODO
		return null;
	}
	
	private void explore(){
		//TODO
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
