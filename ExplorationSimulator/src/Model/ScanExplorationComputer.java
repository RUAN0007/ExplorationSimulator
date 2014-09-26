package Model;

import application.GlobalUtil;
import Model.ArenaTemplate.CellState;

public class ScanExplorationComputer extends ExplorationComputer{
	//Parameters
	private Block startSouthWestBlock = new Block(GlobalUtil.southWestStartRowIndex, GlobalUtil.southWestStartColIndex);

	///////////////////////////////////

	public ScanExplorationComputer(int rowCount, int colCount,
			ExplorationEnvironment env) {
		super(rowCount, colCount, env);
	}

	//As the arena is scanned column by column,
	//headingOrientation should either be NORTH or SOUTH. 
	private Orientation headingOrientation = Orientation.NORTH;
	private int trackColID = 0;
	@Override
	public Action getNextStep(Robot robot) {
		//Stage 1: Moves to start block
		if(!robot.getSouthWestBlock().equals(startSouthWestBlock)){
			//TODO hardcode the orientation preferences here
			Orientation firstOrientation = Orientation.SOUTH;
			Orientation secondOrientation = Orientation.WEST;
			return moveToOrientation(robot, firstOrientation, secondOrientation);
		}
		
		//Stage 2: Scans the arena column by column
		if(robotOnArenaEdge(robot, headingOrientation)){
			headingOrientation = headingOrientation.toOppsite();
			trackColID += robot.getDiameterInCellNum();
			
			//Whole arena has been explored
			if(trackColID >= this.exploredMap.getColumnCount()) return null;
		}
		
		int currentSouthWestColID = robot.getSouthWestBlock().getColID();
		if(currentSouthWestColID < trackColID){
			return moveToOrientation(robot, Orientation.EAST, headingOrientation);
		}else if(currentSouthWestColID == trackColID){
			return moveToOrientation(robot, headingOrientation, Orientation.EAST);
		}else{
			//currentSouthWestColID > trackColID
			return moveToOrientation(robot, Orientation.WEST, headingOrientation);
		}

	}

	

	private Action moveToOrientation(Robot robot,Orientation firstOrientation,Orientation secondOrientation) {

		Direction preferedDirection = getPreferedDirection(firstOrientation,secondOrientation);

		Orientation currentOrientation = robot.getCurrentOrientation();
		//		System.out.println("Current Orientation = " + currentOrientation.toString());
		//		System.out.println("First Orientation = " + firstOrientation.toString());
		//		System.out.println("Second Orientation = " + secondOrientation.toString());

		if(currentOrientation.equals(firstOrientation)){

			return moveTowardsDirectionInOrder(Direction.AHEAD,
					preferedDirection,
					oppoDirection(preferedDirection), 
					robot);
		}else if(currentOrientation.equals(secondOrientation)){

			return moveTowardsDirectionInOrder(oppoDirection(preferedDirection),
					Direction.AHEAD,
					preferedDirection, 
					robot);
		}else if(currentOrientation.toOppsite().equals(firstOrientation)){

			return moveTowardsDirectionInOrder(oppoDirection(preferedDirection),
					Direction.AHEAD,
					preferedDirection, 
					robot);
		}else if(currentOrientation.toOppsite().equals(secondOrientation)){

			return moveTowardsDirectionInOrder(preferedDirection,
					Direction.AHEAD,
					oppoDirection(preferedDirection), 
					robot);		
		}
		assert(false):"Should not reach here. No other circumstance";
		return null; 
	}

	private static Direction oppoDirection(Direction old){
		if(old == Direction.LEFT){
			return Direction.RIGHT;
		}else if(old == Direction.RIGHT){
			return Direction.LEFT;
		}else if(old == Direction.AHEAD){
			return Direction.BACK;
		}else if(old == Direction.BACK){
			return Direction.AHEAD;
		}else{
			assert(false):"Should not reach here. No more directions";
		}
		return Direction.NULL;
	}

	private Direction getPreferedDirection(Orientation firstOrientation,Orientation secondOrientation) {
		Direction preferedDirection = Direction.NULL;
		if(firstOrientation.relativeToLeft().equals(secondOrientation)){
			preferedDirection = Direction.LEFT;
		}else if(firstOrientation.relativeToRight().equals(secondOrientation)){
			preferedDirection = Direction.RIGHT;
		}else{
			assert(false):"Two Prefered Orientation should be adjacent";
		}
		return preferedDirection;
	}

	private Action moveTowardsDirectionInOrder(Direction firstChoice,
			Direction secondChoice,
			Direction thirdChoice,
			Robot robot){

		Action defaultAction = Action.TURN_LEFT;

		//Ensure no pair of direction choices are equal
		assert(firstChoice != secondChoice);
		assert(firstChoice != thirdChoice);
		assert(secondChoice != thirdChoice);
		///////////////////////////////////////////////
		Orientation currentOrientation = robot.getCurrentOrientation();
		Orientation firstChoiceOrientation = orientationOnDirection(currentOrientation, firstChoice);
		Orientation secondChoiceOrientation = orientationOnDirection(currentOrientation, secondChoice);
		Orientation thirdChoiceOrientation = orientationOnDirection(currentOrientation, thirdChoice);

		if(robotSurroundingStatus(robot, firstChoiceOrientation) <= 0){
			return directionToAction(firstChoice);
		}
		if(robotSurroundingStatus(robot, secondChoiceOrientation) <= 0){
			return directionToAction(secondChoice);
		}
		if(robotSurroundingStatus(robot, thirdChoiceOrientation) <= 0){
			return directionToAction(thirdChoice);
		}
		return defaultAction;
	}
	//Map Direction Left to Action Turn Left
	//Map Direction Right to Action Turn Right
	//Map Direction Ahead to Action Move_Forward
	//Map Direction Back to Action draw_back
	private static Action directionToAction(Direction direction){
		if(direction == Direction.AHEAD){
			return Action.MOVE_FORWARD;
		}else if(direction == Direction.LEFT){
			return Action.TURN_LEFT;
		}else if(direction == Direction.RIGHT){
			return Action.TURN_RIGHT;
		}else if(direction == Direction.BACK){
			return Action.DRAW_BACK;
		}else{
			assert(false):"Should not reach here";
		}
		return null;
	}

	//Return the new orientation based on the original orientation and its relative direction
	private static Orientation orientationOnDirection(Orientation original,Direction direction){
		if(direction == Direction.AHEAD){
			return original.clone();
		}else if(direction == Direction.LEFT){
			return original.relativeToLeft();
		}else if(direction == Direction.RIGHT){
			return original.relativeToRight();
		}else{
			assert(false):"Should not reach here";
		}
		return null;

	}

	//return 1 if on ori side exists obstacle or on the arena edge
	//return 0 if on ori side no unexplored cell or obstacle
	//return -1 if on ori side there is unexplored cell
	private int robotSurroundingStatus(Robot robot,Orientation ori){
		if(robotOnArenaEdge(robot, ori)) return 1;
		if(existsCellOnOrientaion(robot, ori, CellState.OBSTACLE)) return 1;
		//The following condition only applies when no obstacle around;
		if(existsCellOnOrientaion(robot, ori, CellState.UNEXPLORED)) return -1;
		//No unexplored cell as well as no obstacle
		return 0;
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

}
