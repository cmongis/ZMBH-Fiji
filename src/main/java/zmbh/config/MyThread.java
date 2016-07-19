/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zmbh.config;

import java.awt.image.BufferedImage;
import java.util.Map;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;
import net.imagej.Dataset;
import net.imagej.display.DatasetView;
import net.imagej.display.ImageDisplay;
import net.imglib2.RandomAccess;
import net.imglib2.type.numeric.RealType;

/**
 *
 * @author User
 */
public class MyThread implements Runnable {
    
    StructureInfo structureInfo;
    ImageDisplay imageDisplay;    
    Number newValue;
    Number oldValue;
    Map.Entry<Integer, AxisInfo> entry;    
    RootLayoutController controller;
    
    
    public MyThread (StructureInfo structureInfo, ImageDisplay imageDisplay, Number newValue, Number oldValue, Map.Entry<Integer, AxisInfo> entry, RootLayoutController controller){
        this.structureInfo = structureInfo;
        this.imageDisplay = imageDisplay;
        this.newValue = newValue;
        this.oldValue = oldValue;
        this.entry = entry;
        this.controller = controller;
    }
    
    
    @Override
    public void run() {
        
        //System.out.println(Math.round(newValue.floatValue()));
        //System.out.println(Math.round(oldValue.floatValue()));
        //System.out.println("");
        long nbChan = -1;
        long chanDimIndex = -1;

        for(Map.Entry<Integer, AxisInfo> entry : structureInfo.getAxisMap().entrySet()){
            if(entry.getValue().getAxisType().equals("Channel")){
                nbChan = entry.getValue().getAxeDim();
                chanDimIndex = entry.getKey();
            }
        }

        imageDisplay.setPosition(Math.round(newValue.floatValue()) , entry.getKey());                            
        imageDisplay.update();

        int[] pos = new int[(int)((DatasetView) imageDisplay.getActiveView()).numDimensions()];
        ((DatasetView) imageDisplay.getActiveView()).localize(pos);
        /*
        for(int k = 0; k < pos.length; k++){
            System.out.print(pos[k] + " ");  
            System.out.println("");
        }
        */

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
        //System.out.println(min);
        //System.out.println(max);
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
    }
    
}
