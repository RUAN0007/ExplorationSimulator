package application;

public class GlobalUtil {
	public static boolean ViewDEBUG = false;
	public String[] romanNumerals = {"I","II","III","IV","V",
									 "VI","VII","VIII","IX","X",
									 "XI","XII","XIII","XIV","XV",
									 "XVI","XVII","XVIII","XIX","XX",
									 "XXI","XXII","XXIII","XXIV","XXV",
									 "XXVI","XXVII","XXVIII","XXIX","XXX",
									 "XXXI","XXXII","XXXIII","XXXIV","XXXV",
									 "XXXVI","XXXVII","XXXVIII","XXXIX","XXXX"
									};
	public static int rowCount = 20;
	public static int columnCount = 15;
	public static int robotDiameterInCellNumber = 3;
	
	public static int southWestStartRowIndex = rowCount - 1;
	public static int southWestStartColIndex = 0;
	
	public static int southWestGoalRowIndex = robotDiameterInCellNumber - 1;
	public static int southWestGoalColIndex = columnCount - robotDiameterInCellNumber;
	
	public static int indexFont = rowCount / 2;
	
}
