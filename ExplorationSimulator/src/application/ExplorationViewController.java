package application;

import Model.Robot;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

import Model.Block;
import Model.Cell;
import Model.CustomizedArena;
import Model.CustomizedArena.ArenaException;
import Model.ExplorationModel;
import Model.FastestPathComputer;
import Model.MinStepTurnPathComputer;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.FileChooser.ExtensionFilter;

public class ExplorationViewController implements Initializable{
	private static Color OBSTACLE_COLOR = Color.BLACK;
	private static Color EMPTY_COLOR = Color.WHITE;
	private static Color ROBOT_COLOR = Color.BLUE;
	private static Color DIRECTION_COLOR = Color.RED;
	private static Color PATH_COLOR = Color.AQUA;
	private static Color UNEXPLORED_COLOR = Color.GRAY;
	private static Color START_COLOR = Color.ALICEBLUE;
	private static Color GOAL_COLOR = Color.ANTIQUEWHITE;
	
	@FXML
	GridPane arena;
	@FXML
	Label msgLabel;
	@FXML
	Label timeLabel;
	
	@FXML
	Label currentCoverageLabel;
	
	@FXML
	ChoiceBox<String> timeLimitChoiceBox;
	
	@FXML
	ChoiceBox<String> coverageLimitChoiceBox;

	@FXML
	Label rowIndexLabel;
	
	@FXML
	Label colIndexLabel;
	
	@FXML
	Label cellTypeLabel;
	
	@FXML
	Label stepCountLabel;
	
	@FXML
	Label turnCountLabel;
	
	@FXML
	ChoiceBox<String> secondsPerStepChoiceBox;
	

	@FXML
	ToggleButton startpausedButton;
	
	@FXML
	Button resetButton;
	
	@FXML
	Rectangle demoCell;

	private Stage stage;
	
	private Rectangle[][] recs;

	private int timeCount = 0;
	public void setStage(Stage stage){
		this.stage = stage;
	}
	
	boolean isReset = false;
	private ExplorationModel model;
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		addBlocks();
		
		initSecondPerStepChoiceBox();
		
		initTimeLimitChoiceBox();
		
		initCoverageLimitChoiceBox();
		
		timeCount = 0;
		
		this.secondsPerStepChoiceBox.setDisable(true);
		this.timeLimitChoiceBox.setDisable(true);

		this.coverageLimitChoiceBox.setDisable(true);
		this.startpausedButton.setDisable(true);
		this.resetButton.setDisable(true);

	}

	private void initCoverageLimitChoiceBox() {
		coverageLimitChoiceBox.getItems().add("10");
		coverageLimitChoiceBox.getItems().add("20");
		coverageLimitChoiceBox.getItems().add("30");
		coverageLimitChoiceBox.getItems().add("40");
		coverageLimitChoiceBox.getItems().add("50");
		coverageLimitChoiceBox.getItems().add("60");
		coverageLimitChoiceBox.getItems().add("70");
		coverageLimitChoiceBox.getItems().add("80");
		coverageLimitChoiceBox.getItems().add("90");
		coverageLimitChoiceBox.getItems().add("100");
		coverageLimitChoiceBox.setValue("100");
		
		
		

//		coverageLimitChoiceBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
//			public void changed(javafx.beans.value.ObservableValue<? extends String> observable, String oldValue, String newValue) {
//				//TODO
//				onResetPressed(null);
//			};
//		});
	}

	private void initTimeLimitChoiceBox() {
		timeLimitChoiceBox.getItems().add("30");
		timeLimitChoiceBox.getItems().add("60");
		timeLimitChoiceBox.getItems().add("90");
		timeLimitChoiceBox.getItems().add("120");
		timeLimitChoiceBox.getItems().add("N.A.");
		timeLimitChoiceBox.setValue("N.A.");
//		timeLimitChoiceBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
//			public void changed(javafx.beans.value.ObservableValue<? extends String> observable, String oldValue, String newValue) {
//				//TODO
//				onResetPressed(null);
//			};
//		});
	}

	private void initSecondPerStepChoiceBox() {
		secondsPerStepChoiceBox.getItems().add("0.25");
		secondsPerStepChoiceBox.getItems().add("0.5");
		secondsPerStepChoiceBox.getItems().add("1");
		secondsPerStepChoiceBox.getItems().add("2");
		secondsPerStepChoiceBox.setValue("0.25");
	}
	
	
	private void addBlocks() {
		recs = new Rectangle[GlobalUtil.rowCount][GlobalUtil.columnCount];
		double blockWidth = arena.getPrefWidth() / (GlobalUtil.columnCount + 1) * 0.9;
		double blockHeight = arena.getPrefHeight() / (GlobalUtil.rowCount + 1) * 0.9;
		
		for(int rowLabelIndex = 0;rowLabelIndex <= GlobalUtil.rowCount;rowLabelIndex++){
			RowConstraints row = new RowConstraints();
			row.setPercentHeight(100.0 / (GlobalUtil.rowCount + 1));
			arena.getRowConstraints().add(row);
		}
		
		for(int colLabelIndex = 0;colLabelIndex <= GlobalUtil.columnCount;colLabelIndex++){
			ColumnConstraints col = new ColumnConstraints();
			col.setPercentWidth(100.0 / (GlobalUtil.columnCount + 1));
			arena.getColumnConstraints().add(col);
		}
		
		
		for(int colLabelIndex = 1;colLabelIndex <= GlobalUtil.columnCount;colLabelIndex++){
			Label colLabel = new Label("" + colLabelIndex);
			colLabel.setFont(new Font(GlobalUtil.indexFont));
			colLabel.setMinSize(blockWidth, blockHeight);
			colLabel.setAlignment(Pos.CENTER);
			arena.add(colLabel, colLabelIndex, 0);
			
		}

		for(int rowLabelIndex = 1;rowLabelIndex <= GlobalUtil.rowCount;rowLabelIndex++){
			
			Label colLabel = new Label("" + rowLabelIndex);
			colLabel.setFont(new Font(GlobalUtil.indexFont));

			colLabel.setMinSize(blockWidth, blockHeight);
			colLabel.setAlignment(Pos.CENTER);
			arena.add(colLabel, 0, rowLabelIndex);
			
		}
	
		
		
		for(int rowIndex = 1; rowIndex <= GlobalUtil.rowCount;rowIndex++){
			for(int colIndex = 1; colIndex <= GlobalUtil.columnCount;colIndex++){
				Rectangle rec = new Rectangle(blockWidth, blockHeight);
				rec.setFill(OBSTACLE_COLOR);
				recs[rowIndex - 1][colIndex - 1] = rec;
				rec.setArcHeight(20);
				rec.setArcWidth(20);
				arena.add(rec,colIndex, rowIndex);
			}
		}
		
	}
	
	@FXML
	public void onArenaClicked(MouseEvent me){
		if(GlobalUtil.ViewDEBUG){
			System.out.println("onArenaHovered");
		}
		if(!isReset) return;
		double xCdn = me.getSceneX();
		double yCdn = me.getSceneY();
		
		int rowIndex = computeArenaRowIndex(yCdn);
		int columnIndex = computeArenaColumnIndex(xCdn);

		Robot robot = new Robot(rowIndex,
								columnIndex,
								GlobalUtil.robotDiameterInCellNumber,
								Model.Orientation.NORTH,
								GlobalUtil.explorationRange);
		if(!this.model.setRobot(robot)){
			this.setMessageBoxText("Can not put the robot here");
			return;
		}
		isReset = false;
		this.secondsPerStepChoiceBox.setDisable(false);
		this.timeLimitChoiceBox.setDisable(false);

		this.coverageLimitChoiceBox.setDisable(false);
		this.startpausedButton.setDisable(false);
		this.resetButton.setDisable(false);
		refreshView();
	}

	@FXML
	public void onArenaHovered(MouseEvent me){
		if(GlobalUtil.ViewDEBUG){
			System.out.println("onArenaHovered");
		}
		double xCdn = me.getSceneX();
		double yCdn = me.getSceneY();
		
		int rowIndex = computeArenaRowIndex(yCdn);
		int columnIndex = computeArenaColumnIndex(xCdn);

		updateCellIndexDisplay(rowIndex, columnIndex);
		if(this.model != null){
			updateCellStateDisplay(rowIndex, columnIndex);
		}
	}
	
  private int computeArenaRowIndex(double yCdn){ //xCdn = Coordinate X on the scene
  		double arenaYCdn = this.arena.getLayoutY();
  		double cellHeight = this.arena.getPrefHeight() / (GlobalUtil.rowCount + 1);
  		int rowIndex = (int)((yCdn - arenaYCdn) / cellHeight);
  		rowIndex--;

  		return rowIndex;
  		
  }
  
  private FileChooser getMapDescriptorFileChooser() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.getExtensionFilters().add(
				new ExtensionFilter("Map Descripter", "*.txt"));
		return fileChooser;
	}
  
  private String getDescriptorFromFile() {
		FileChooser fileChooser = getMapDescriptorFileChooser();
		File descriptorFile = fileChooser.showOpenDialog(this.stage);
		StringBuilder mapDescriptors = new StringBuilder();
		if(descriptorFile != null){
			
			try(BufferedReader br = new BufferedReader(
					new FileReader(descriptorFile))) {
				mapDescriptors.append(br.readLine());
				mapDescriptors.append("\n");
				mapDescriptors.append(br.readLine());
				
			} catch (IOException e) {
				//e.printStackTrace();
				this.msgLabel.setText("Open up " + descriptorFile.getName() + " failed...");
				return null;
			}
			return mapDescriptors.toString();
			
		}
		return null;
	}
  
  private void updateCellStateDisplay(int rowIndex, int columnIndex) {
	  
	  if((0 <= rowIndex && rowIndex <= GlobalUtil.rowCount - 1 ) &&
			     (0 <= columnIndex && columnIndex <= GlobalUtil.columnCount - 1)){
			Cell cell = this.model.getCellStatus(rowIndex, columnIndex);
		  	paintRectBasedOnStatus(this.demoCell, cell);
		  	updateDemoLabel(cell);
		}else{
			this.cellTypeLabel.setText("---");
			this.demoCell.setFill(UNEXPLORED_COLOR);
		}
	  
	}
  
  private void paintRectBasedOnStatus(Rectangle rectToPaint, Cell cellModel) {
		if(cellModel == Cell.UNEXMPLORED){
			rectToPaint.setFill(UNEXPLORED_COLOR);
			
		}else if(cellModel == Cell.EMPTY){
			rectToPaint.setFill(EMPTY_COLOR);
			
		}else if(cellModel == Cell.OBSTACLE){
			rectToPaint.setFill(OBSTACLE_COLOR);
			
		}else if(cellModel == Cell.ROBOT){
			rectToPaint.setFill(ROBOT_COLOR);
			
		}else if(cellModel == Cell.ROBOT_DIRECTION){
			rectToPaint.setFill(DIRECTION_COLOR);
			
		}else if(cellModel == Cell.PATH){
			rectToPaint.setFill(PATH_COLOR);
			
		}else if(cellModel == Cell.EMPTY){
			rectToPaint.setFill(EMPTY_COLOR);
		}else if(cellModel == Cell.START){
			rectToPaint.setFill(START_COLOR);
		}else if(cellModel == Cell.GOAL){
			rectToPaint.setFill(GOAL_COLOR);
		}
	}
  
  
  private void updateDemoLabel(Cell cell) {
	  if(cell == Cell.UNEXMPLORED){
		  	this.cellTypeLabel.setText("UNEXPLORED");
		  	
		}else if(cell == Cell.EMPTY){
		  	this.cellTypeLabel.setText("EMPTY");
			
		}else if(cell == Cell.OBSTACLE){
		  	this.cellTypeLabel.setText("OBSTACLE");
			
		}else if(cell == Cell.ROBOT){
		  	this.cellTypeLabel.setText("ROBOT");
			
		}else if(cell == Cell.ROBOT_DIRECTION){
		  	this.cellTypeLabel.setText("DIRECTION");
			
		}else if(cell == Cell.PATH){
		  	this.cellTypeLabel.setText("PATH");
			
		}else if(cell == Cell.GOAL){
			this.cellTypeLabel.setText("GOAL");
		}else if(cell == Cell.START){
			this.cellTypeLabel.setText("START");
		}
  }
  
	  private int computeArenaColumnIndex(double xCdn){ //xCdn = Coordinate X on the scene
			double arenaXCdn = this.arena.getLayoutX();
			double cellWidth = this.arena.getWidth() / (GlobalUtil.columnCount + 1);
			int columnIndex = (int)((xCdn - arenaXCdn) / cellWidth);
			columnIndex--;
			
			return columnIndex;
			
	  }
  
	  private void updateCellIndexDisplay(int rowIndex, int columnIndex) {
			if((0 <= rowIndex && rowIndex <= GlobalUtil.rowCount - 1 ) &&
			     (0 <= columnIndex && columnIndex <= GlobalUtil.columnCount - 1)){
				this.rowIndexLabel.setText("" + (rowIndex + 1));
				this.colIndexLabel.setText("" + (columnIndex + 1));
	
			}else{
				this.rowIndexLabel.setText("-");
				this.colIndexLabel.setText("-");
	
			}
		}
	  
	  //TODO DEUG
	  private String getTestDescriptor(){
		  return "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF" +
				  "\n" +
				  "00400080010000000000003F0400000000000001000401000000000380000000080010002000";
	  }
	  
	  @FXML
	  public void onDescriptorLoaded(){
			if(GlobalUtil.ViewDEBUG){
				System.out.println("onDescriptorLoaded");
			}
			
			//TODO DEBUG
			String descriptor = getTestDescriptor();
			CustomizedArena realMap = null;
			try {
				realMap = new CustomizedArena(GlobalUtil.rowCount, GlobalUtil.columnCount);
				realMap.setDescriptor(descriptor);

			} catch (ArenaException e) {
				setMessageBoxText(e.getMessage());
			}
			
			FastestPathComputer pathComputer = new MinStepTurnPathComputer(1,1);
			Block startBlock = new Block(GlobalUtil.southWestStartRowIndex,GlobalUtil.southWestStartColIndex);
			Block goalBlock = new Block(GlobalUtil.southWestGoalRowIndex,GlobalUtil.southWestGoalColIndex);
			this.model = new ExplorationModel(realMap, pathComputer, startBlock, goalBlock);
			
			isReset = true;
			setMessageBoxText("Click the arena for the robot's initial position...");
			
	  }
	  
	  private void setMessageBoxText(String msg){
		  this.msgLabel.setText(msg);
	  }
	  
	  @FXML 
	  void onDescriptorSaved(){
		  
		  if(this.model == null) {
			  this.setMessageBoxText("The model is null");
			  return;
		  }
		  String description = this.model.getExploredDescriptor();
		  
		  FileChooser fileChooser = getMapDescriptorFileChooser();
			File savedFile = fileChooser.showSaveDialog(this.stage);
			if(savedFile != null){
				try(BufferedWriter bw = new BufferedWriter(
						new FileWriter(savedFile))) {
					
					bw.write(description);
				    this.msgLabel.setText("Save to " + savedFile.getName());
					
				} catch (IOException e) {
					//e.printStackTrace();
					this.msgLabel.setText("Save to  " + savedFile.getName() + "failed...");
				}
			}
	  }
	  
	  //TODO DEBUG
	  @FXML
	  public void forward(){
		  if(this.model.isFinished()) {
			  this.setMessageBoxText("Finish");
		  }
		  String desc = this.model.forward();
		  this.setMessageBoxText(desc);
		  
		  this.refreshView();
	  }
	  
	  private static int TimerPeriodInMS = 500;
	  
	  Timer timer;
	  private int executionCount = 0;
	  @FXML
	public void onStartPausedPressed(ActionEvent e ){
			if (startpausedButton.isSelected()){
				//Start button is pressed
				this.secondsPerStepChoiceBox.setDisable(true);
				this.timeLimitChoiceBox.setDisable(true);
				this.coverageLimitChoiceBox.setDisable(true);
				this.resetButton.setDisable(true);
				this.startpausedButton.setText("Pause");

				
				int msPerStep = 1000 * (int)Double.parseDouble(this.secondsPerStepChoiceBox.getValue());
				double coverageLimit = Double.parseDouble(this.coverageLimitChoiceBox.getValue()) / 100.0;
				int timeLimitInS = 0;
				try{
					timeLimitInS = Integer.parseInt(this.timeLimitChoiceBox.getValue());
				}catch(NumberFormatException ex){
					timeLimitInS = Integer.MAX_VALUE;
				}
				
				final int  timeLimitInSecond = timeLimitInS;
				int multipleForStep = msPerStep / TimerPeriodInMS;
				int multipleForTimeCounter = 1000 / TimerPeriodInMS;
				
				timer = new Timer();
				timer.scheduleAtFixedRate(new TimerTask() {
					@Override
					public void run() {

						if(executionCount % multipleForTimeCounter == 0){
							timeCount++;
						}
						if(executionCount % multipleForStep == 0){
							String actionDesc = model.forward();
							if(actionDesc != null){
								Platform.runLater(new Runnable() {
									
									@Override
									public void run() {
										refreshView();
										setMessageBoxText(actionDesc);
									} //END of run method
								});//END of runLater method
							}//ENd of if
							if(model.isFinished() ||
								timeCount >= timeLimitInSecond ||
								model.getCoverage() > coverageLimit){
									
								timer.cancel();
								Platform.runLater(new Runnable() {
									
									@Override
									public void run() {
										setMessageBoxText("Finish");
										startpausedButton.setDisable(true);
										resetButton.setDisable(false);
									} //END of run method
								});//END of runLater method
							}
						}
						executionCount ++;

					} //End of RUN
				}, TimerPeriodInMS, TimerPeriodInMS);//And of scheuleAtFixedRate
			
				
			}else{
				//Pause button is pressed
				timer.cancel();
				startpausedButton.setText("Start");

				setMessageBoxText("Pause");
				startpausedButton.setDisable(true);
				resetButton.setDisable(false);
			}
	 }
  
	  @FXML
	public void onResetPressed(ActionEvent e){
		  this.model.reset();
		  this.timeCount = 0;
		  this.executionCount = 0;
		   this.secondsPerStepChoiceBox.setDisable(false);
			this.timeLimitChoiceBox.setDisable(false);

			this.coverageLimitChoiceBox.setDisable(false);
			this.startpausedButton.setDisable(false);
			this.resetButton.setDisable(false);
			refreshView();
		  
	}
	  
	private void refreshView() {
		int rowCount = GlobalUtil.rowCount;
		int colCount = GlobalUtil.columnCount;
		
		for(int rowID = 0;rowID < rowCount; rowID++){
			for(int colID = 0;colID < colCount;colID++){
				Rectangle rectToPaint = recs[rowID][colID];
				assert(rectToPaint != null);
				Cell cellModel = this.model.getCellStatus(rowID, colID);
				paintRectBasedOnStatus(rectToPaint,cellModel);
			}
		}
		
		this.stepCountLabel.setText("" + this.model.getCurrentStepCount());
		this.turnCountLabel.setText("" + this.model.getCurrentTurnCount());
		
		double approximateCoverage = (int)(this.model.getCoverage() * 10000) / 100.0;
		this.currentCoverageLabel.setText("" + approximateCoverage + "%");
		
		this.timeLabel.setText("" + timeCount + "s");
	}
}
