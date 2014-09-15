package Model;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

import Model.CustomizedArena.ArenaException;

public class ExplorationTest {

	@Test
	public void test() {
		Robot robot = new Robot(11,6,3,Orientation.NORTH,3);
		FastestPathComputer pathComputer = new MinStepTurnPathComputer(1, 1);
		CustomizedArena arena = null;
		try {
			arena = new CustomizedArena(20, 15);
			arena.setDescriptor("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF" +
					  "\n" +
					  "0000000000200400080010000070000000000000007E00FC0000000100000100000020000000"
		  );
		} catch (ArenaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//11,6
		ArrayList<Action> actions = pathComputer.computeForFastestPath(arena, robot, 2, 12);
				assert(actions != null);
	}

}
