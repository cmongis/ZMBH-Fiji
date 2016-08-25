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

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.embed.swing.JFXPanel;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.RowConstraints;
import javax.swing.JFrame;
import net.imagej.Dataset;
import net.imagej.ImageJ;
import net.imagej.display.DatasetView;
import net.imagej.display.ImageDisplay;
import net.imglib2.RandomAccess;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.CommandModule;
import org.scijava.command.CommandService;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;

/**
 * This class is called from the ImageJ plugin.
 *
 * @author Hadrien Mary
 */
public class MainAppFrame extends JFrame {

    @Parameter
    private LogService log;
    
    @Parameter
    CommandService cmdService;
    
    private ImageJ ij;
    private JFXPanel fxPanel;
    //DatasetView datasetView;
    ImageDisplay imageDisplay;

    /*
    public MainAppFrame(ImageJ ij, DatasetView datasetView) {
        ij.context().inject(this);
        this.ij = ij;
        this.datasetView = datasetView;
    }
    */
    
    public MainAppFrame(ImageJ ij, ImageDisplay imageDisplay) {
        ij.context().inject(this);
        this.ij = ij;
        this.imageDisplay = imageDisplay;
    }

    /**
     * Create the JFXPanel that make the link between Swing (IJ) and JavaFX plugin.
     */
    public void init() {
        this.fxPanel = new JFXPanel();
        this.add(this.fxPanel);
        

        // The call to runLater() avoid a mix between JavaFX thread and Swing thread.
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                initFX(fxPanel);
            }
        });
        this.setVisible(true);
    }

    public void initFX(JFXPanel fxPanel) {
        // Init the root layout
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/fxml/RootLayout.fxml"));
            BorderPane borderPane = (BorderPane) loader.load();
            
            // Get the controller and add an ImageJ context to it.
            RootLayoutController controller = loader.getController();
            controller.setContext(ij.context());   
            
            controller.setImageDisplay(imageDisplay);
            
            Future<CommandModule> promise = cmdService.run(GetStackStructure.class, true,
                    "stack", imageDisplay.getActiveView().getData());
            CommandModule promiseContent = promise.get();
            StructureInfo structureInfo = (StructureInfo) promiseContent.getOutput("structureInfo");
            
            controller.setStructureInfo(structureInfo);
            
            imageDisplay.update();
                        
            long nbChan = -1;
            long chanDimIndex = -1;
            
            for(Map.Entry<Integer, AxisInfo> entry : structureInfo.getAxisMap().entrySet()){
                if(entry.getValue().getAxisType().equals("Channel")){
                    nbChan = entry.getValue().getAxeDim();
                    chanDimIndex = entry.getKey();
                }
            }
            
            int[] pos = new int[(int)((DatasetView) imageDisplay.getActiveView()).numDimensions()];
            ((DatasetView) imageDisplay.getActiveView()).localize(pos);
            
            RandomAccess<RealType<?>> ra = ((Dataset) imageDisplay.getActiveView().getData()).randomAccess();
            ra.setPosition(pos);
            int width =  (int) ((Dataset) imageDisplay.getActiveView().getData()).dimension(0);
            int heigth =  (int) ((Dataset) imageDisplay.getActiveView().getData()).dimension(1);
            Double min = null;
            Double max = null;
            for(int x = 0; x < width; x++){
                for(int y = 0; y < heigth; y++){
                    ra.setPosition(x, 0);
                    ra.setPosition(y, 1);
                    double value = ra.get().getRealDouble();
                    if(min == null){
                        min = new Double(value);
                    }
                    else if(value < min.doubleValue()){
                        min = new Double(value);
                    }
                    if(max == null){
                        max = new Double(value);
                    }
                    else if(value > max.doubleValue()){
                        max = new Double(value);
                    }
                }
            }

            if(nbChan > -1){
                ((Dataset) imageDisplay.getActiveView().getData()).setChannelMaximum((int) pos[(int)chanDimIndex], max);
                ((Dataset) imageDisplay.getActiveView().getData()).setChannelMinimum((int) pos[(int)chanDimIndex], min);
            }
            else{
                ((Dataset) imageDisplay.getActiveView().getData()).setChannelMaximum(0, max);
                ((Dataset) imageDisplay.getActiveView().getData()).setChannelMinimum(0, min);
            }
            ((DatasetView) imageDisplay.getActiveView()).rebuild();
            imageDisplay.update();
            
            BufferedImage image = ((DatasetView) imageDisplay.getActiveView()).getScreenImage().image();             
            WritableImage writableImage = new WritableImage(image.getWidth(), image.getHeight());
            SwingFXUtils.toFXImage(image, writableImage);
            controller.getImageView().setImage(writableImage);
            
            structureInfo.getAxisMap().entrySet().stream().forEach((entry) -> {
                if(!entry.getValue().getAxisType().equals("X") && !entry.getValue().getAxisType().equals("Y")){
                    
                    Slider slider = new Slider();
                    slider.setValue(0);
                    slider.setMin(0);
                    slider.setMax(entry.getValue().getAxeDim() - 1);
                    slider.setShowTickLabels(true);
                    slider.setShowTickMarks(true);
                    slider.setMajorTickUnit(1);
                    slider.setBlockIncrement(1);
                    slider.setMinorTickCount(0);
                    
                    Label label = new Label();
                    label.setText(entry.getValue().getAxisType());
                    
                    Label labelValue = new Label();
                    
                    slider.valueProperty().addListener(new ChangeListener<Number>() {
                        @Override
                        public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {

                            int newV = Math.round(newValue.floatValue());
                            int oldV = Math.round(oldValue.floatValue());                        
                            slider.adjustValue(newV);
                            
                            if(newV != oldV){                              
                                Thread thread = new Thread(new MyThread(structureInfo, imageDisplay, newValue, oldValue, entry, controller));
                                thread.start();
                            }  
                        }                        
                    });
                    
                    controller.getGridPane().addRow(entry.getKey(), label, slider);
                }
            });

            for(RowConstraints element : controller.getGridPane().getRowConstraints()){
                element.setValignment(VPos.CENTER);
            }
            for(ColumnConstraints element : controller.getGridPane().getColumnConstraints()){
                element.setHalignment(HPos.LEFT);
            }

            controller.getGridPane().setVgap(10);
            controller.getChoiceBox().setItems(
                FXCollections.observableArrayList(
                    "fluoTandem1",
                    "fluoTandem2",
                    "fluoDiscriminator",
                    "brigthfield")
            );
            controller.getChoiceBox().setValue(controller.getChoiceBox().getItems().get(0));
            
            // Show the scene containing the root layout.
            Scene scene = new Scene(borderPane);
            
            this.fxPanel.setScene(scene);
            controller.getImageView().fitWidthProperty().bind(this.fxPanel.getScene().widthProperty());
            
            this.fxPanel.setVisible(true);
            

            // Resize the JFrame to the JavaFX scene
            this.setSize((int) scene.getWidth(), (int) scene.getHeight());

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(MainAppFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
