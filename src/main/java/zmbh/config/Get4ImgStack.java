/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zmbh.config;

import java.util.Map;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imagej.axis.CalibratedAxis;
import net.imglib2.RandomAccess;
import net.imglib2.type.numeric.RealType;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author User
 */

@Plugin(type = Command.class)
public class Get4ImgStack implements Command {
    
    @Parameter
    DatasetService datasetService;
    
    @Parameter(type = ItemIO.INPUT)
    Dataset inStack;
    
    @Parameter(type = ItemIO.INPUT)
    Map<String, Map<String, Integer>> imageMap;
    
    @Parameter(type = ItemIO.OUTPUT)
    Dataset outStack;
    
    
    @Override
    public void run() {
        
        
        long width = inStack.dimension(inStack.dimensionIndex(Axes.X));
        long heigth = inStack.dimension(inStack.dimensionIndex(Axes.Y));
        long[] dims = new long[]{width, heigth, 4};
        String name = inStack.getName();
        AxisType[] axes = new AxisType[]{Axes.X, Axes.Y, Axes.Z};
        int bitsPerPixels = inStack.getType().getBitsPerPixel();
        boolean signed = inStack.isSigned();
        boolean floating = !inStack.isInteger();
        
        outStack = datasetService.create(dims, name, axes, bitsPerPixels, signed, floating, false);
        
        RandomAccess<RealType<?>> inRa = inStack.randomAccess();
        RandomAccess<RealType<?>> outRa = outStack.randomAccess();
        
        CalibratedAxis[] inStackAxes = new CalibratedAxis[inStack.numDimensions()];
        inStack.axes(inStackAxes);
        
        for(Map.Entry<String, Map<String, Integer>> entry : imageMap.entrySet()){
            long[] pos = new long[inStack.numDimensions()];
            for(Map.Entry<String, Integer> subEntry : entry.getValue().entrySet()){                
                for(CalibratedAxis axis : inStackAxes){
                    if(axis.type().getLabel().equals(subEntry.getKey())){
                        pos[inStack.dimensionIndex(axis.type())] = subEntry.getValue();
                    }
                }
            }
            inRa.setPosition(pos);
            switch(entry.getKey()){
                case "fluoTandem1": outRa.setPosition(0, 2);
                                    break;
                case "fluoTandem2": outRa.setPosition(1, 2);
                                    break;
                case "fluoDiscriminator": outRa.setPosition(2, 2);
                                          break;
                case "brigthfield": outRa.setPosition(3, 2);
                                    break;
            }
            
            for(int x = 0; x < width; x++){
                for(int y = 0; y < heigth; y++){
                    inRa.setPosition(x, 0);
                    inRa.setPosition(y, 1);
                    outRa.setPosition(x, 0);
                    outRa.setPosition(y, 1);
                    
                    outRa.get().setReal(inRa.get().getRealDouble());
                }
            }
            
            
        }
        
    }
    
}