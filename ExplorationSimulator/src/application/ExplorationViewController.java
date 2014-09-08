package application;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
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
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.FileChooser.ExtensionFilter;

public class ExplorationViewController implements Initializable{
	
	@FXML
	GridPane arena;
	@FXML
	Label msgLabel;
	@FXML
	Label timeLeftLabel;
	
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

	public void setStage(Stage stage){
		this.stage = stage;
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		addBlocks();
		
		initSecondPerStepChoiceBox();
		
		initTimeLimitChoiceBox();
		
		initCoverageLimitChoiceBox();
		
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
		
		
		

		coverageLimitChoiceBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
			public void changed(javafx.beans.value.ObservableValue<? extends String> observable, String oldValue, String newValue) {
				//TODO
				onResetPressed(null);
			};
		});
	}

	private void initTimeLimitChoiceBox() {
		timeLimitChoiceBox.getItems().add("30");
		timeLimitChoiceBox.getItems().add("60");
		timeLimitChoiceBox.getItems().add("90");
		timeLimitChoiceBox.getItems().add("120");
		timeLimitChoiceBox.getItems().add("N.A.");
		timeLimitChoiceBox.setValue("30");
		timeLimitChoiceBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
			public void changed(javafx.beans.value.ObservableValue<? extends String> observable, String oldValue, String newValue) {
				//TODO
				onResetPressed(null);
			};
		});
	}

	private void initSecondPerStepChoiceBox() {
		secondsPerStepChoiceBox.getItems().add("0.5");
		secondsPerStepChoiceBox.getItems().add("1");
		secondsPerStepChoiceBox.getItems().add("2");
		secondsPerStepChoiceBox.setValue("1");
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
			//	rec.setId("" + (rowIndex - 1) + "Block" + (colIndex - 1));
			//TODO
				//	rec.setFill(UNEXPLORED_COLOR);
				recs[rowIndex - 1][colIndex - 1] = rec;
				rec.setArcHeight(20);
				rec.setArcWidth(20);
				arena.add(rec,colIndex, rowIndex);
			}
		}
		
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
		//TODO
//		if(this.model != null){
//			updateCellStateDisplay(rowIndex, columnIndex);
//		}
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
  
  //TODO
//  private void updateCellStateDisplay(int rowIndex, int columnIndex) {
//	  
//	  if((0 <= rowIndex && rowIndex <= GlobalUtil.rowCount - 1 ) &&
//			     (0 <= columnIndex && columnIndex <= GlobalUtil.columnCount - 1)){
//			Cell cell = this.model.getCellStatus(rowIndex, columnIndex);
//		  	paintRectBasedOnStatus(this.demoCell, cell);
//		  	updateDemoLabel(cell);
//		}else{
//			this.cellTypeLabel.setText("---");
//			this.demoCell.setFill(UNEXPLORED_COLOR);
//		}
//	  
//	}
  
  
//  
//  private void updateDemoLabel(Cell cell) {
//	  if(cell == Cell.UNEXMPLORED){
//		  	this.cellTypeLabel.setText("UNEXPLORED");
//		  	
//		}else if(cell == Cell.EMPTY){
//		  	this.cellTypeLabel.setText("EMPTY");
//			
//		}else if(cell == Cell.OBSTACLE){
//		  	this.cellTypeLabel.setText("OBSTACLE");
//			
//		}else if(cell == Cell.ROBOT){
//		  	this.cellTypeLabel.setText("ROBOT");
//			
//		}else if(cell == Cell.ROBOT_DIRECTION){
//		  	this.cellTypeLabel.setText("DIRECTION");
//			
//		}else if(cell == Cell.PATH){
//		  	this.cellTypeLabel.setText("PATH");
//			
//		}else if(cell == Cell.GOAL){
//			this.cellTypeLabel.setText("GOAL");
//		}else if(cell == Cell.START){
//			this.cellTypeLabel.setText("START");
//		}
//  }
  
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
	  
	  @FXML
	  public void onDescriptorLoaded(){
			if(GlobalUtil.ViewDEBUG){
				System.out.println("onDescriptorLoaded");
			}
			
			String descriptor = getDescriptorFromFile();
			//TODO
			//Testing
			System.out.println(descriptor);
	  }
	  
	  @FXML void onDescriptorSaved(){
		  FileChooser fileChooser = getMapDescriptorFileChooser();
			File savedFile = fileChooser.showSaveDialog(this.stage);
			if(savedFile != null){
				try(BufferedWriter bw = new BufferedWriter(
						new FileWriter(savedFile))) {
					
					bw.write("Hello World");
				    this.msgLabel.setText("Save to " + savedFile.getName());
					
				} catch (IOException e) {
					//e.printStackTrace();
					this.msgLabel.setText("Save to  " + savedFile.getName() + "failed...");
				}
			}
	  }
	  
	  @FXML
	public void onStartPausedPressed(ActionEvent e ){
	}
  
	  @FXML
	public void onResetPressed(ActionEvent e){
	}
}
