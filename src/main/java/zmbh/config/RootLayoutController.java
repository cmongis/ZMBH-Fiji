/*
 * The MIT License
 *
 * Copyright 2016 Fiji.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package zmbh.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import net.imagej.display.DatasetView;
import net.imagej.display.ImageDisplay;
import org.scijava.Context;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;

/**
 * FXML Controller class
 *
 * @author Hadrien Mary
 */
public class RootLayoutController{

    @Parameter
    private LogService log;
    
    DatasetView datasetView;
    
    ImageDisplay imageDisplay;
    
    StructureInfo structureInfo;
    
    Map<String, Integer> fluoTandem1Coord;
    Map<String, Integer> fluoTandem2Coord;
    Map<String, Integer> fluoDiscriminatorCoord;
    Map<String, Integer> brigthfieldCoord;
    Map<String, Map<String, Integer>> coordList;
    
    public RootLayoutController(){
        super();
        fluoTandem1Coord = new HashMap<>();
        fluoTandem2Coord = new HashMap<>();
        fluoDiscriminatorCoord = new HashMap<>();
        brigthfieldCoord = new HashMap<>();
        
        coordList = new HashMap<>();
        coordList.put("fluoTandem1", fluoTandem1Coord);
        coordList.put("fluoTandem2", fluoTandem2Coord);
        coordList.put("fluoDiscriminator", fluoDiscriminatorCoord);
        coordList.put("brigthfield", brigthfieldCoord);
    }
    
    
    @FXML
    ImageView imageView;
    
    @FXML
    TextArea textArea;
    
    @FXML
    Button button;
    
    @FXML
    GridPane gridPane;
    
    @FXML
    VBox vbox;
    
    @FXML
    ChoiceBox choiceBox;
    
    @FXML
    protected void doact(ActionEvent event) {
        
        Map<String, Integer> coord = coordList.get(choiceBox.getSelectionModel().getSelectedItem());
        int[] pos = new int[(int)((DatasetView) imageDisplay.getActiveView()).numDimensions()];
        ((DatasetView) imageDisplay.getActiveView()).localize(pos);
        
        String coordString = "[ ";
        for(int i = 0; i < pos.length; i++){
            AxisInfo axisInfo = structureInfo.getAxisMap().get(i);
            if(!axisInfo.getAxisType().equals("X") && !axisInfo.getAxisType().equals("Y")){
                coord.put(axisInfo.getAxisType(), pos[i]);
                coordString += axisInfo.getAxisType() + ":" + pos[i] + " ";
            }
        }
        coordString += "]";
        writeLog("Image at coord " + coordString + " is set as " + choiceBox.getSelectionModel().getSelectedItem());
        
    }
    
    @FXML
    protected void saveConfig(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Image");        
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("JSON files (*.json)", "*.json");
        fileChooser.getExtensionFilters().add(extFilter);
        File file = fileChooser.showSaveDialog(new Stage());
        
        writeLog("Saving to " + file.getPath());
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.writeValue(file, coordList);
        } catch (JsonProcessingException ex) {
            Logger.getLogger(GetStackStructure.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SaveJSON.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    @FXML
    protected void unset(ActionEvent event){
        Map<String, Integer> coord = coordList.get(choiceBox.getSelectionModel().getSelectedItem());
        coord.clear();
        
        writeLog("Unset " + choiceBox.getSelectionModel().getSelectedItem() + " mapping");
    }
    
    protected void writeLog(String text){
        if(textArea.getText().isEmpty()){
            textArea.setText(text);
        }
        else{
            textArea.setText(textArea.getText() + "\n" + text);
        }    
    }
    

    public void setContext(Context context) {
        context.inject(this);
    }

    public ImageView getImageView() {
        return imageView;
    }

    public void setImageView(ImageView imageView) {
        this.imageView = imageView;
    }

    public TextArea getTextArea() {
        return textArea;
    }

    public void setTextArea(TextArea textArea) {
        this.textArea = textArea;
    }

    public Button getButton() {
        return button;
    }

    public void setButton(Button button) {
        this.button = button;
    }

    public DatasetView getDatasetView() {
        return datasetView;
    }

    public void setDatasetView(DatasetView datasetView) {
        this.datasetView = datasetView;
    }

    public GridPane getGridPane() {
        return gridPane;
    }

    public void setGridPane(GridPane gridPane) {
        this.gridPane = gridPane;
    }

    public ImageDisplay getImageDisplay() {
        return imageDisplay;
    }

    public void setImageDisplay(ImageDisplay imageDisplay) {
        this.imageDisplay = imageDisplay;
    }

    public VBox getVbox() {
        return vbox;
    }

    public void setVbox(VBox vbox) {
        this.vbox = vbox;
    }

    public ChoiceBox getChoiceBox() {
        return choiceBox;
    }

    public void setChoiceBox(ChoiceBox choiceBox) {
        this.choiceBox = choiceBox;
    }

    public StructureInfo getStructureInfo() {
        return structureInfo;
    }

    public void setStructureInfo(StructureInfo structureInfo) {
        this.structureInfo = structureInfo;
    }


    
    

    
    
    
}
