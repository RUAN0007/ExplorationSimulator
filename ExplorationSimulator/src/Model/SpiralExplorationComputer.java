package Model;

import java.util.ArrayList;
import java.util.LinkedList;

import Model.ArenaTemplate.CellState;

public class SpiralExplorationComputer extends ExplorationComputer {


	private boolean[][] pseudoObstacleExists;
	private Block loopStartSouthWestBlock;
	private int loopNum = 0;
	

	public SpiralExplorationComputer(int rowCount,int colCount,ExplorationEnvironment env){
		super(rowCount, colCount, env);
		this.initExploredMap(rowCount, colCount);
		
	}


	private void initPseudoObstacles(int rowCount,int colCount) {
	
		this.pseudoObstacleExists = new boolean[rowCount][colCount];
		for(int rowID = 0;rowID < rowCount ; rowID++){
			for(int colID = 0;colID < colCount;colID++){
				this.pseudoObstacleExists[rowID][colID] = false;
			}
		}
	}
public Action getNextStep(Robot robot) {
		
		Orientation ori = robot.getCurrentOrientation();
		
		if(nextActions.size() == 0 && robotInExploreState){
			robotInExploreState = false;
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
	
		if(nextActions.size() == 0 && !robotInExploreState){
			robotInExploreState = true;
			
			
			Direction nextDirection = Direction.NULL;
			if(!this.hasRobotTouchedOnEdge){
				Orientation nearestEdgeOrientation = getArenaNearestEdgeToRobot(robot);
				if(robotOnArenaEdge(robot, nearestEdgeOrientation)){
					
					//Make sure when beginning to explore along the left wall, 
					//The left side of the robot must have an obstacle. 
					if(!robotOnArenaEdge(robot, robot.getCurrentOrientation().relativeToLeft())){
						return Action.TURN_RIGHT;
					}
					this.hasRobotTouchedOnEdge = true;
	
					
				}else{
					//Move towards the nearest edge
					nextDirection = moveTowardsOrientation(robot, nearestEdgeOrientation);
				
				}
			}
			
			if(this.hasRobotTouchedOnEdge){
				//Exploration Stage 2: Follow the left wall
				checkForLoop(robot);
				nextDirection = computeNextDirection(robot);
					
			}
			
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
				assert(false):"Should not reach here";
			}
		}
	
	
		Action nextAction = nextActions.pollFirst();
		
		return nextAction;
	}

	boolean hasRobotTouchedOnEdge = false;
	
	LinkedList<Action> nextActions = new LinkedList<Action>();
	//When exploreStage = true,robot's next steps are to turn left and right to explore the surrounding
	//When exploreStage = false, robot's next step is to move.
	boolean robotInExploreState = true;

	Orientation getArenaNearestEdgeToRobot(Robot robot) {
		int distanceToNorth = robot.getSouthWestBlock().getRowID() - robot.getDiameterInCellNum() + 1;
		int distanceToEast = this.exploredMap.getColumnCount() - robot.getSouthWestBlock().getColID() - robot.getDiameterInCellNum();
		int distanceToSouth = this.exploredMap.getRowCount() - robot.getSouthWestBlock().getRowID() + 1;
		int distanceToWest = robot.getSouthWestBlock().getColID();
		
		if(distanceToNorth <= distanceToEast &&
				distanceToNorth <= distanceToSouth && 
				distanceToNorth <= distanceToWest) {
			return Orientation.NORTH;
		}
		if(distanceToEast <= distanceToNorth &&
				distanceToEast <= distanceToSouth && 
				distanceToEast <= distanceToWest) {
			return Orientation.EAST;
		}
		if(distanceToSouth <= distanceToNorth &&
				distanceToSouth <= distanceToEast && 
				distanceToSouth <= distanceToWest){
			return Orientation.SOUTH;
		}
		if(distanceToWest <= distanceToNorth &&
				distanceToWest <= distanceToEast && 
				distanceToWest <= distanceToNorth){ 
			return Orientation.WEST;
		}
		
		assert(false):"Should not reach here";
		return null;
	}

	//For loopStartSouthWestBlock == null, a loop has not started yet.
	
	//When loopStartSouthWestBlock == null, set loopStartSouthWestBlock and start a loop 
	//When the robot does not occupy the pseudo-obstacles
	
	//If a robot is about to finished a loop,
	//Then mark the outermost border block as the pseudo-obstacles
	//Set the loopStartSouthWestBlock to null
	void checkForLoop(Robot robot) {
		if(this.loopStartSouthWestBlock == null){
			if(!occupyPseudoObstacles(robot)){
				this.loopStartSouthWestBlock = robot.getSouthWestBlock().clone();
				this.loopNum++;
				
			}
		
		}else{
			
			Block nextOccupiedSouthWestBlock = getNextOccupiedSouthWestBlock(robot);
			
			if(this.loopStartSouthWestBlock.equals(nextOccupiedSouthWestBlock)){
				this.loopStartSouthWestBlock = null;
				this.markPseudoObstacles();
			}
		}
	}

	
	
	private boolean occupyPseudoObstacles(Robot robot) {
		
		int rowID = robot.getSouthWestBlock().getRowID();
		int colID = robot.getSouthWestBlock().getColID();
		int diameter = robot.getDiameterInCellNum();
		
		for(int rowOffset = 0;rowOffset < diameter;rowOffset++){
			for(int colOffset = 0;colOffset < diameter;colOffset++){
				if(pseudoObstacleExists[rowID - rowOffset][colID + colOffset]) return true;
			}
		}
		return false;
	}


	private Block getNextOccupiedSouthWestBlock(Robot robot) {
		Orientation ori = robot.getCurrentOrientation();
		int rowID = robot.getSouthWestBlock().getRowID();
		int colID = robot.getSouthWestBlock().getColID();
		
		Block nextBlk = null;
		if(ori.equals(Orientation.NORTH)){
			nextBlk = new Block(rowID - 1, colID);
		}else if(ori.equals(Orientation.EAST)){
			nextBlk = new Block(rowID, colID + 1);

		}else if(ori.equals(Orientation.SOUTH)){
			nextBlk = new Block(rowID + 1, colID);

		}else{
			nextBlk = new Block(rowID, colID - 1);

		}
		return nextBlk;
	}


	private void markPseudoObstacles() {
		int rowCount = this.exploredMap.getRowCount();
		int colCount = this.exploredMap.getColumnCount();
		for(int rowID = 0;rowID < rowCount ; rowID++){
			this.pseudoObstacleExists[rowID][loopNum - 1] = true;
			this.pseudoObstacleExists[rowID][colCount - loopNum] = true;
		}
		
		for(int colID = 0;colID < colCount ; colID++){
			this.pseudoObstacleExists[loopNum - 1][colID] = true;
			this.pseudoObstacleExists[rowCount - loopNum][colID] = true;
		}
	}


	private boolean needsExplore(Robot robot, Orientation ori) {
		return !robotOnArenaEdge(robot, ori) &&
				existsCellOnOrientaion(robot, ori, CellState.UNEXPLORED);
	}

//	private boolean canMove(Robot robot, Orientation ori) {
//		return !robotOnArenaEdge(robot, ori) &&
//				!existsCellOnOrientaion(robot, ori, CellState.OBSTACLE);
//	}
	
	//Return the direction to move towards the orientation 
	Direction moveTowardsOrientation(Robot robot, Orientation targetOrientaiton){
		Orientation currentOrientation = robot.getCurrentOrientation();
		if(currentOrientation.relativeToLeft().equals(targetOrientaiton)){
			return moveLeftAheadRight(robot);
		}else if(currentOrientation.equals(targetOrientaiton)){
			return moveAheadLeftRight(robot);
		}else{
			return moveRightAheadLeft(robot);
		}
	}
	
	private ArrayList<Direction> prevDirections = new ArrayList<>();
	Direction computeNextDirection(Robot robot) {
		Orientation leftOrientation = robot.getCurrentOrientation().relativeToLeft();
		Orientation rightOrientation = robot.getCurrentOrientation().relativeToRight();
		Orientation aheadOrientation = robot.getCurrentOrientation().clone();
		
		if(!robotOnArenaEdge(robot, leftOrientation)) assert(!existsCellOnOrientaion(robot, leftOrientation, CellState.UNEXPLORED));
		if(!robotOnArenaEdge(robot, rightOrientation)) assert(!existsCellOnOrientaion(robot, rightOrientation, CellState.UNEXPLORED));
		if(!robotOnArenaEdge(robot, aheadOrientation)) assert(!existsCellOnOrientaion(robot, aheadOrientation, CellState.UNEXPLORED));

//		if(canMove(robot, leftOrientation)) return Direction.LEFT;
//		if(canMove(robot, aheadOrientation)) return Direction.AHEAD;
//		if(canMove(robot, rightOrientation)) return Direction.RIGHT;
		
		///////////////////////////////
		//If there exists 6 consecutive previous turn, go ahead
		if(lastSixSameDirectionTurn()) {
			return moveRightAheadLeft(robot);	
		}
		
		
		
		return moveLeftAheadRight(robot);
	}
	
	//If the robot can move right, then move right
	//If the robot can move ahead, then move ahead
	//If the robot can move left, then move left
	private Direction moveRightAheadLeft(Robot robot) {
		Orientation aheadOrientation = robot.getCurrentOrientation().clone();
		Orientation leftOrientation = robot.getCurrentOrientation().relativeToLeft();
		Orientation rightOrientation = robot.getCurrentOrientation().relativeToRight();

		
		////////////////////////////////////////////
		if(!robotOnArenaEdge(robot, rightOrientation) &&
				!existsPseudoObstacleOnOrientation(robot, rightOrientation) &&
				!existsCellOnOrientaion(robot, rightOrientation, CellState.OBSTACLE)){
			this.prevDirections.add(Direction.RIGHT);

			return Direction.RIGHT;
		}
		
		
		
		if(!robotOnArenaEdge(robot, aheadOrientation) &&
				!existsPseudoObstacleOnOrientation(robot, aheadOrientation) &&
				!existsCellOnOrientaion(robot, aheadOrientation, CellState.OBSTACLE)){
			this.prevDirections.add(Direction.AHEAD);

			return Direction.AHEAD;
		}
	
		if(!robotOnArenaEdge(robot, leftOrientation) &&
				!existsPseudoObstacleOnOrientation(robot, leftOrientation) &&
				!existsCellOnOrientaion(robot, leftOrientation, CellState.OBSTACLE)){
				this.prevDirections.add(Direction.LEFT);
				return Direction.LEFT;
			}
		////////////////////////////////////////////////
		
		/////////////////////////////////////////////////////////////////////////////////////////
		//Check from right to left whether exists a side without any obstacle 
		if(!robotOnArenaEdge(robot, rightOrientation) &&
				!existsCellOnOrientaion(robot, rightOrientation, CellState.OBSTACLE)){
			this.prevDirections.add(Direction.RIGHT);

			return Direction.RIGHT;
		}
		
		
		if(!robotOnArenaEdge(robot, aheadOrientation) &&
				!existsCellOnOrientaion(robot, aheadOrientation, CellState.OBSTACLE)){
			this.prevDirections.add(Direction.AHEAD);

			return Direction.AHEAD;
		}
	
		
		if(!robotOnArenaEdge(robot, leftOrientation) &&
				!existsCellOnOrientaion(robot, leftOrientation, CellState.OBSTACLE)){
			this.prevDirections.add(Direction.LEFT);

			return Direction.LEFT;
		}
		////////////////////////////////////////////////
		return Direction.BACK;
	}
	
	//If the robot can move left, then move left. 
	//Else If the robot can move ahead, then move ahead
	//Else If the robot can move right, then move right
	private Direction moveLeftAheadRight(Robot robot) {
		
		Orientation aheadOrientation = robot.getCurrentOrientation().clone();
		Orientation leftOrientation = robot.getCurrentOrientation().relativeToLeft();
		Orientation rightOrientation = robot.getCurrentOrientation().relativeToRight();

		////////////////////////////////////////////
		//Check from left to right whether exists a side without any obstacle and pseudo-obstacle
		if(!robotOnArenaEdge(robot, leftOrientation) &&
			!existsPseudoObstacleOnOrientation(robot, leftOrientation) &&
			!existsCellOnOrientaion(robot, leftOrientation, CellState.OBSTACLE)){
			this.prevDirections.add(Direction.LEFT);
			return Direction.LEFT;
		}
		
		if(!robotOnArenaEdge(robot, aheadOrientation) &&
				!existsPseudoObstacleOnOrientation(robot, aheadOrientation) &&
				!existsCellOnOrientaion(robot, aheadOrientation, CellState.OBSTACLE)){
			this.prevDirections.add(Direction.AHEAD);

			return Direction.AHEAD;
		}
	
		if(!robotOnArenaEdge(robot, rightOrientation) &&
				!existsPseudoObstacleOnOrientation(robot, rightOrientation) &&
				!existsCellOnOrientaion(robot, rightOrientation, CellState.OBSTACLE)){
			this.prevDirections.add(Direction.RIGHT);

			return Direction.RIGHT;
		}
		////////////////////////////////////////////////
		
		/////////////////////////////////////////////////////////////////////////////////////////
		//Check from left to right whether exists a side without any obstacle 
		if(!robotOnArenaEdge(robot, leftOrientation) &&
				!existsCellOnOrientaion(robot, leftOrientation, CellState.OBSTACLE)){
			this.prevDirections.add(Direction.LEFT);

			return Direction.LEFT;
		}
		
		if(!robotOnArenaEdge(robot, aheadOrientation) &&
				!existsCellOnOrientaion(robot, aheadOrientation, CellState.OBSTACLE)){
			this.prevDirections.add(Direction.AHEAD);

			return Direction.AHEAD;
		}
	
		if(!robotOnArenaEdge(robot, rightOrientation) &&
				!existsCellOnOrientaion(robot, rightOrientation, CellState.OBSTACLE)){
			this.prevDirections.add(Direction.RIGHT);

			return Direction.RIGHT;
		}
		////////////////////////////////////////////////
		return Direction.BACK;
	}

	private Direction moveAheadLeftRight(Robot robot) {
		Orientation aheadOrientation = robot.getCurrentOrientation().clone();
		Orientation leftOrientation = robot.getCurrentOrientation().relativeToLeft();
		Orientation rightOrientation = robot.getCurrentOrientation().relativeToRight();

		////////////////////////////////////////////
		
		if(!robotOnArenaEdge(robot, aheadOrientation) &&
				!existsPseudoObstacleOnOrientation(robot, aheadOrientation) &&
				!existsCellOnOrientaion(robot, aheadOrientation, CellState.OBSTACLE)){
			this.prevDirections.add(Direction.AHEAD);

			return Direction.AHEAD;
		}
		
		if(!robotOnArenaEdge(robot, leftOrientation) &&
			!existsPseudoObstacleOnOrientation(robot, leftOrientation) &&
			!existsCellOnOrientaion(robot, leftOrientation, CellState.OBSTACLE)){
			this.prevDirections.add(Direction.LEFT);
			return Direction.LEFT;
		}
	
		if(!robotOnArenaEdge(robot, rightOrientation) &&
				!existsPseudoObstacleOnOrientation(robot, rightOrientation) &&
				!existsCellOnOrientaion(robot, rightOrientation, CellState.OBSTACLE)){
			this.prevDirections.add(Direction.RIGHT);

			return Direction.RIGHT;
		}
		////////////////////////////////////////////////
		
		/////////////////////////////////////////////////////////////////////////////////////////

		if(!robotOnArenaEdge(robot, aheadOrientation) &&
				!existsCellOnOrientaion(robot, aheadOrientation, CellState.OBSTACLE)){
			this.prevDirections.add(Direction.AHEAD);

			return Direction.AHEAD;
		}
	
		
		if(!robotOnArenaEdge(robot, leftOrientation) &&
				!existsCellOnOrientaion(robot, leftOrientation, CellState.OBSTACLE)){
			this.prevDirections.add(Direction.LEFT);

			return Direction.LEFT;
		}
		
		if(!robotOnArenaEdge(robot, rightOrientation) &&
				!existsCellOnOrientaion(robot, rightOrientation, CellState.OBSTACLE)){
			this.prevDirections.add(Direction.RIGHT);

			return Direction.RIGHT;
		}
		////////////////////////////////////////////////
		return Direction.BACK;
	}

	private boolean lastSixSameDirectionTurn() {
		int size = this.prevDirections.size();
		if(size < 6) return false;
		Direction last = this.prevDirections.get(size - 1);
		if(last == Direction.AHEAD || last == Direction.BACK) return false;
		for(int preID = 1;preID < 6;preID++){
			if(this.prevDirections.get(size - 1 - preID) != last) return false;
		}
		
	//	displayPObs();
		
		return true;
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

	private boolean existsPseudoObstacleOnOrientation(Robot robot, Orientation ori){
		int robotDiamterInCellNum = robot.getDiameterInCellNum();
		if(ori.equals(Orientation.NORTH)){
			int rowID = robot.getSouthWestBlock().getRowID() - robotDiamterInCellNum;
			for(int colOffset = 0;colOffset < robotDiamterInCellNum;colOffset++){
				int colID = robot.getSouthWestBlock().getColID() + colOffset;
				if(this.pseudoObstacleExists[rowID][colID]) return true;
			}
			return false;
		}
		
		if(ori.equals(Orientation.EAST)){
			int colID = robot.getSouthWestBlock().getColID() + robotDiamterInCellNum;
			for(int rowOffset = 0;rowOffset < robotDiamterInCellNum;rowOffset++){
				int rowID = robot.getSouthWestBlock().getRowID() - rowOffset;
				if(this.pseudoObstacleExists[rowID][colID]) return true;
			}
			return false;
		}
		
		if(ori.equals(Orientation.SOUTH)){
			int rowID = robot.getSouthWestBlock().getRowID() + 1;
			for(int colOffset = 0;colOffset < robotDiamterInCellNum;colOffset++){
				int colID = robot.getSouthWestBlock().getColID() + colOffset;
				if(this.pseudoObstacleExists[rowID][colID]) return true;
			}
			return false;
		}
		
		if(ori.equals(Orientation.WEST)){
			int colID = robot.getSouthWestBlock().getColID() - 1;
			for(int rowOffset = 0;rowOffset < robotDiamterInCellNum;rowOffset++){
				int rowID = robot.getSouthWestBlock().getRowID() - rowOffset;

				if(this.pseudoObstacleExists[rowID][colID]) return true;
			}
			return false;
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

	
}
