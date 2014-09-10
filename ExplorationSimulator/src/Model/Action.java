package Model;

public class Action {
	private String action;
	
	public static Action TURN_LEFT= new Action("TURN_LEFT");
	public static Action TURN_RIGHT = new Action("TURN_RIGHT");
	public static Action MOVE_FORWARD = new Action("MOVE_FORWARD");
	public static Action DRAW_BACK = new Action("DRAW_BACK");
	
	
	private Action(String action){
		this.action = action;	
	}
	
	@Override
	public String toString(){
		return this.action;
	}
	
	public boolean equals(Action act){
		return this.action.equals(act.action);
	}
	
	public Orientation orientationAfterAction(Orientation dir){
		if(this.equals(TURN_LEFT)){
			return dir.relativeToLeft();
//			if(dir.equals(Orientation.LEFT)){
//				return Orientation.DOWN;
//			}else if(dir.equals(Orientation.RIGHT)){
//				return Orientation.UP;
//			}else if(dir.equals(Orientation.UP)){
//				return Orientation.LEFT;
//			}else{
//				//Current Orientation is DOWN
//				return Orientation.RIGHT;
//			}
		}else if(this.equals(TURN_RIGHT)){
			return dir.relativeToRight();
//			if(dir.equals(Orientation.LEFT)){
//				return Orientation.UP;
//			}else if(dir.equals(Orientation.RIGHT)){
//				return Orientation.DOWN;
//			}else if(dir.equals(Orientation.UP)){
//				return Orientation.RIGHT;
//			}else{
//				//Current Orientation is DOWN
//				return Orientation.LEFT;
//			}
		}else{
			return dir;
		}
	}

	public static Action revert(Action action) {
		// TODO Auto-generated method stub
		
		if(action.equals(Action.TURN_LEFT)){
			return Action.TURN_RIGHT;
		}else if(action.equals(Action.TURN_RIGHT)){
			return Action.TURN_LEFT;
		}else if(action.equals(Action.MOVE_FORWARD)){
			return Action.DRAW_BACK;
		}else if(action.equals(Action.DRAW_BACK)){
			return Action.MOVE_FORWARD;
		}else{
			return null;
		}
	}
}
