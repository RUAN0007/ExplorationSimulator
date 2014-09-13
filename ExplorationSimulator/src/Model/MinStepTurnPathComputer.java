package Model;

import java.util.ArrayList;

public class MinStepTurnPathComputer extends FastestPathComputer {
	
	private static int OREITATION_NULL = -1;
	private static int OREITATION_MIN = 0;
	private static int NORTH_INDEX = 0;
	private static int WEST_INDEX = 1;
	private static int SOUTH_INDEX = 2;
	private static int EAST_INDEX = 3;
	private static int OREITATION_MAX = 3;
	
	
	
	private int turnWeight;
	private int stepWeight;
	
	
	
	
	public MinStepTurnPathComputer(int turnWeight, int stepWeight) {
		super();
		this.turnWeight = turnWeight;
		this.stepWeight = stepWeight;
	}

	private int distance[][][];
	private Action preAction[][][];
	private boolean explored[][][];
	
	private int minRowID ;
	private int minColID;
	private int minDrcID;
	private int minDist;
	

	@Override
	public ArrayList<Action> compute(Integer[][] map, int rowCount, int colCount,
			int startRowID, int startColID, Orientation startOrientation,
			int goalRowID, int goalColID) {
		
		 initDataStructure(map, rowCount, colCount);
		
		int startDrcID = IndexOfOrientation(startOrientation);
		distance[startRowID][startColID][startDrcID] = 0;
		int goalDrcID = OREITATION_NULL;
		while(true){
			
			 this.minRowID = -1;
			 this.minColID = -1;
			 this.minDrcID = OREITATION_NULL;
			 this.minDist = Integer.MAX_VALUE;
			
			if(!findUnexploredCellWithDist(rowCount, colCount)) return null;
			
			
			assert(!explored[minRowID][minColID][minDrcID]);
			explored[minRowID][minColID][minDrcID] = true;
			
			goalDrcID = minDistGoalDrcID(goalRowID, goalColID);
			if(goalDrcID != OREITATION_NULL) break;
			
			
			//Update its three adjacent node
			
			int leftOrientationID = (minDrcID + OREITATION_MAX) % (OREITATION_MAX + 1);
			if(updateNodeDist(minRowID,minColID,leftOrientationID,turnWeight)){
				preAction[minRowID][minColID][leftOrientationID] = Action.TURN_LEFT;				
			};
			
			int rightOrientationID = (minDrcID + 1) % (OREITATION_MAX + 1);
			if(updateNodeDist(minRowID,minColID,rightOrientationID,turnWeight)){
				preAction[minRowID][minColID][rightOrientationID] = Action.TURN_RIGHT;				
			};
			
			
			int adjacentRowID = minRowID;
			int adjacentColID = minColID;
			
			if(minDrcID == NORTH_INDEX){
				adjacentRowID--;
			}else if(minDrcID == SOUTH_INDEX){
				adjacentRowID++;
			}else if(minDrcID == EAST_INDEX){
				adjacentColID--;
			}else{
				//RIGHT Orientation
				adjacentColID++;
			}
			if(0 <= adjacentRowID && adjacentRowID < rowCount &&
					0 <= adjacentColID && adjacentColID < colCount){
				if(updateNodeDist(adjacentRowID,adjacentColID,minDrcID,stepWeight)){
					preAction[adjacentRowID][adjacentColID][minDrcID] = Action.MOVE_FORWARD;				
				};
			}
			

		}// END of infinite WHILE
		
		
		for(int drcID = OREITATION_MIN;drcID <= OREITATION_MAX;drcID ++){
			assert(explored[goalRowID][goalColID][drcID]);
			assert(preAction[goalRowID][goalColID][drcID] != null);
		}//END of loop on orientation		
	
		
		
		
		return findPathsFromActionMap(
									goalRowID,
									goalColID,
									goalDrcID
//									startRowID,
//									startColID,
//									startDrcID
									);
		
	}



	//Each cell has 4 orientation nodes. 
	//Return whether the update the applicable
	private boolean updateNodeDist(int rowID,int colID, int drcID, int weight) {
		if(!explored[rowID][colID][drcID] &&
				distance[rowID][colID][drcID]
						> distance[minRowID][minColID][minDrcID] + weight){
			
			distance[rowID][colID][drcID]
					= distance[minRowID][minColID][minDrcID] + weight;
			return true;
		}
		return false;
	}




	private int minDistGoalDrcID(int goalRowID, int goalColID) {
		//All the orientations at goal state has been explored
		int goalDrcID = OREITATION_NULL;
		int minDistForDrcOnGoal = Integer.MAX_VALUE;
		for(int drcID = OREITATION_MIN;drcID <= OREITATION_MAX;drcID ++){
			if(!explored[goalRowID][goalColID][drcID]){
				goalDrcID = OREITATION_NULL;
				break;
			}else{
				if(distance[goalRowID][goalColID][drcID] < minDistForDrcOnGoal){
					minDistForDrcOnGoal = distance[goalRowID][goalColID][drcID];
					goalDrcID = drcID;
				}
			}
			
		}//END of loop on orientation
		return goalDrcID;
	}




	private boolean findUnexploredCellWithDist(int rowCount, int colCount) {
		boolean foundMin = false;
		 for(int rowID = 0;rowID < rowCount ; rowID++){
			for(int colID = 0;colID < colCount;colID++){
				for(int drcID = OREITATION_MIN;drcID <= OREITATION_MAX;drcID ++){
					if(!explored[rowID][colID][drcID] && 
							distance[rowID][colID][drcID] < minDist){
						minRowID = rowID;
						minColID = colID;
						minDrcID = drcID;
						minDist = distance[rowID][colID][drcID];
						foundMin = true;
					}
				}//END of loop on orientation
			}// END of loop on columns
		}//End of loop on rows
		return foundMin;
	}




	private void initDataStructure(Integer[][] map, int rowCount, int colCount) {
		distance = new int[rowCount][colCount][Orientation.OrientationCount];
		 preAction = new Action[rowCount][colCount][Orientation.OrientationCount];
		 explored = new boolean[rowCount][colCount][Orientation.OrientationCount];
		
		for(int rowID = 0;rowID < rowCount ; rowID++){
			for(int colID = 0;colID < colCount;colID++){
				boolean isObstacle = false;
				if(map[rowID][colID].equals(new Integer(1))){
					isObstacle = true;
				}
				for(int drcID = OREITATION_MIN;drcID <= OREITATION_MAX;drcID ++){
					distance[rowID][colID][drcID] = Integer.MAX_VALUE;
					preAction[rowID][colID][drcID] = null;
					explored[rowID][colID][drcID] = isObstacle;
					
				}//END of loop on orientation
			}// END of loop on columns
		}//End of loop on rows
	}
	
	


	//TODO
	//Remove the last three parameters for testing
	private ArrayList<Action> findPathsFromActionMap(
			int goalRowID, int goalColID, int goalDrcID) {
			//int startRowID,int startColID,int startDrcID) {
		
		ArrayList<Action> actions = new ArrayList<>();
		//Find the action min distance
		int rowID = goalRowID;
		int colID = goalColID;
		int drcID = goalDrcID;
		
		while(preAction[rowID][colID][drcID] != null){
			Action currentAction = preAction[rowID][colID][drcID];
			actions.add(0, currentAction);
			if(currentAction.equals(Action.TURN_LEFT)){
				 drcID = (drcID + 1) % (OREITATION_MAX + 1);

			}else if(currentAction.equals(Action.TURN_RIGHT)){
				drcID = (drcID + OREITATION_MAX) % (OREITATION_MAX + 1);

			}else{
				//currentAction == MOVE_FORWARD
				if(drcID == NORTH_INDEX){
					rowID++;
				}else if(drcID == SOUTH_INDEX){
					rowID--;
				}else if(drcID == EAST_INDEX){
					colID++;
				}else{
					//RIGHT Orientation
					colID--;
				}
				
			}
		}
	//	assert(rowID == startRowID && colID == startColID && drcID == startDrcID);

		return actions;
	}



	private static int IndexOfOrientation(Orientation orientation){
		if(orientation.equals(Orientation.NORTH)){
			return NORTH_INDEX;
		}else if(orientation.equals(Orientation.EAST)){
			return WEST_INDEX;
		}else if(orientation.equals(Orientation.SOUTH)){
			return SOUTH_INDEX;
		}else{
			//LEFT DIRECTION
			return EAST_INDEX;
		}
	}

}
