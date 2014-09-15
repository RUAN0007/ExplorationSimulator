package Model;

import java.util.LinkedList;
import Model.ArenaTemplate.CellState;
import Model.CustomizedArena.ArenaException;

public class ExplorationComputer {

	public interface ExplorationEnvironment{
		public void explore(CustomizedArena exploredMap);
	}

	private FastestPathComputer pathComputer;
	private CustomizedArena exploredMap;
	private ExplorationEnvironment env;
	private boolean[][] pseudoObstacleExists;

	public ExplorationComputer(int rowCount,int colCount,ExplorationEnvironment env){
		this.pathComputer = new MinStepTurnPathComputer(1, 1);
		this.initExploredMap(rowCount, colCount);
		this.initPseudoObstacles(rowCount,colCount);
		this.env = env;
	}


	private void initPseudoObstacles(int rowCount,int colCount) {

		this.pseudoObstacleExists = new boolean[rowCount][colCount];
		for(int rowID = 0;rowID < rowCount ; rowID++){
			for(int colID = 0;colID < colCount;colID++){
				this.pseudoObstacleExists[rowID][colID] = false;
			}
		}
	}

	private void initExploredMap(int rowCount, int columnCount) {
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

	//Update the current status based on what the robot has explored
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


	private LinkedList<Action> nextActions = new LinkedList<Action>();
	private boolean exploreStage = true;

	public Action getNextStep(Robot robot){
		Orientation ori = robot.getCurrentOrientation();
		if(nextActions.size() == 0 && exploreStage){
			exploreStage = false;
			Orientation robotLeftOrientation = ori.relativeToLeft();
			if(needsExplore(robot,robotLeftOrientation)){
				nextActions.add(Action.TURN_LEFT);
				nextActions.add(Action.TURN_RIGHT);
			}
			Orientation robotRightOrientation = ori.relativeToRight();
			if(needsExplore(robot,robotRightOrientation)){
				nextActions.add(Action.TURN_RIGHT);
				nextActions.add(Action.TURN_LEFT);
			}
		}

		if(nextActions.size() == 0 && !exploreStage){
			exploreStage = true;
			Direction nextDirection = computeNextDirection(robot);
			if(nextDirection == Direction.LEFT){
				nextActions.add(Action.TURN_LEFT);
				nextActions.add(Action.MOVE_FORWARD);
			}else if(nextDirection == Direction.AHEAD){
				nextActions.add(Action.MOVE_FORWARD);
			}else if(nextDirection == Direction.RIGHT){
				nextActions.add(Action.TURN_RIGHT);
				nextActions.add(Action.MOVE_FORWARD);
			}else if(nextDirection == Direction.BACK){
				nextActions.add(Action.TURN_RIGHT);
				nextActions.add(Action.TURN_RIGHT);
			}else{
				assert(false):"Shoudl not reach here";
			}
		}


		return nextActions.pollFirst();
	}

	private boolean needsExplore(Robot robot, Orientation ori) {
		return !robotOnArenaEdge(robot, ori) &&
				existsCellOnOrientaion(robot, ori, CellState.UNEXPLORED);
	}

	private boolean canMove(Robot robot, Orientation ori) {
		return !robotOnArenaEdge(robot, ori) &&
				!existsCellOnOrientaion(robot, ori, CellState.OBSTACLE);
	}

	private Direction computeNextDirection(Robot robot) {
		Orientation leftOrientation = robot.getCurrentOrientation().relativeToLeft();
		Orientation rightOrientation = robot.getCurrentOrientation().relativeToRight();
		Orientation aheadOrientation = robot.getCurrentOrientation().clone();
		
		if(!robotOnArenaEdge(robot, leftOrientation)) assert(!existsCellOnOrientaion(robot, leftOrientation, CellState.UNEXPLORED));
		if(!robotOnArenaEdge(robot, rightOrientation)) assert(!existsCellOnOrientaion(robot, rightOrientation, CellState.UNEXPLORED));
		if(!robotOnArenaEdge(robot, aheadOrientation)) assert(!existsCellOnOrientaion(robot, aheadOrientation, CellState.UNEXPLORED));

		if(canMove(robot, leftOrientation)) return Direction.LEFT;
		if(canMove(robot, aheadOrientation)) return Direction.AHEAD;
		if(canMove(robot, rightOrientation)) return Direction.RIGHT;
		return Direction.BACK;
	}

	

	private boolean existsCellOnOrientaion(Robot robot, Orientation ori,CellState state){
		Boolean needExplore = null;
		if(robotOnArenaEdge(robot, ori)) return false;
		if(ori.equals(Orientation.NORTH))  needExplore = existsCellOnTheNorth(robot,state);
		if(ori.equals(Orientation.WEST))  needExplore = existsCellOnTheWest(robot,state);
		if(ori.equals(Orientation.SOUTH))  needExplore = existsCellOnTheSouth(robot,state);
		if(ori.equals(Orientation.EAST))  needExplore = existsCellOnTheEast(robot,state);

		return needExplore;

	}
	
	private boolean robotOnArenaEdge(Robot robot,Orientation ori){
		if(ori.equals(Orientation.NORTH)){
			return robot.getSouthWestBlock().getRowID() == robot.getDiameterInCellNum() - 1;
		}
		
		if(ori.equals(Orientation.EAST)){
			return robot.getSouthWestBlock().getColID() == this.exploredMap.getColumnCount() - robot.getDiameterInCellNum();
		}
		
		if(ori.equals(Orientation.SOUTH)){
			return robot.getSouthWestBlock().getRowID() == this.exploredMap.getRowCount() - 1;
		}
		
		if(ori.equals(Orientation.WEST)){
			return robot.getSouthWestBlock().getColID() == 0;
		}
		assert(false):"No other direction...";
		return false;
	}

	

	//If the north border of the robot is on the side of the arena, return null
	private boolean existsCellOnTheNorth(Robot robot, CellState cell) {
		int robotDiamterInCellNum = robot.getDiameterInCellNum();
		int rowID = robot.getSouthWestBlock().getRowID() - robotDiamterInCellNum;
		for(int colOffset = 0;colOffset < robotDiamterInCellNum;colOffset++){
			int colID = robot.getSouthWestBlock().getColID() + colOffset;
			if(this.exploredMap.getCell(rowID, colID) == cell) return true;
		}
		return false;
	}

	private Boolean existsCellOnTheEast(Robot robot, CellState cell) {
		int robotDiamterInCellNum = robot.getDiameterInCellNum();
		int colID = robot.getSouthWestBlock().getColID() + robotDiamterInCellNum;
		for(int rowOffset = 0;rowOffset < robotDiamterInCellNum;rowOffset++){
			int rowID = robot.getSouthWestBlock().getRowID() - rowOffset;

			if(this.exploredMap.getCell(rowID, colID) == cell) return true;
		}
		return false;
	}

	private Boolean existsCellOnTheWest(Robot robot, CellState cell) {
		int robotDiamterInCellNum = robot.getDiameterInCellNum();
		int colID = robot.getSouthWestBlock().getColID() - 1;
		for(int rowOffset = 0;rowOffset < robotDiamterInCellNum;rowOffset++){
			int rowID = robot.getSouthWestBlock().getRowID() - rowOffset;

			if(this.exploredMap.getCell(rowID, colID) == cell) return true;
		}
		return false;
	}

	private Boolean existsCellOnTheSouth(Robot robot, CellState cell) {
		int robotDiamterInCellNum = robot.getDiameterInCellNum();
		int rowID = robot.getSouthWestBlock().getRowID() + 1;
		for(int colOffset = 0;colOffset < robotDiamterInCellNum;colOffset++){
			int colID = robot.getSouthWestBlock().getColID() + colOffset;

			if(this.exploredMap.getCell(rowID, colID) == cell) return true;
		}
		return false;
	}

	public boolean isFinished(Robot robot) {
		if(robot == null) return false;
		if(getCoverage() < 1.0) return false;
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
		double coverageRate = 1.0 -  (double)unExploredCount/(double)totalCount;
		assert(-0.1 < coverageRate && coverageRate < 1.1):"Illegail Coverage: " + coverageRate;
		return coverageRate;
	}
}
