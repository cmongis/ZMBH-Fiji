/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zmbh.commands.roi;

import ij.gui.Roi;
import java.awt.Rectangle;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imglib2.RandomAccess;
import net.imglib2.type.numeric.RealType;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author Potier Guillaume, 2016
 */

@Plugin(type = Command.class)
public class RoiDataset implements Command {
    
    @Parameter
    DatasetService datasetService;
    
    @Parameter(type = ItemIO.INPUT)
    Dataset inDataset;
    
    @Parameter(type = ItemIO.INPUT)
    Roi roi;
    
    @Parameter(type = ItemIO.OUTPUT)
    Dataset outDataset;
    
    @Parameter(type = ItemIO.OUTPUT)
    long time1;
    
    //Get bounding square dataset arround a cell
    @Override
    public void run() {
        long startRoiDataset = System.nanoTime();
        
        Rectangle bounds = roi.getBounds();
        
        long[] dimensions = new long[inDataset.numDimensions()];
        dimensions[0] = bounds.width;
        dimensions[1] = bounds.height;
        dimensions[2] = inDataset.dimension(2);
        String inputDatasetName = inDataset.getName();
        String[] split = inputDatasetName.split("\\.");
        String outputDatasetName = split[0] + "crop_." + split[1];
        AxisType[] axisArray = new AxisType[]{Axes.X, Axes.Y, Axes.Z};
        outDataset = datasetService.create(dimensions, outputDatasetName, axisArray, inDataset.getType().getBitsPerPixel(), inDataset.isSigned(), !inDataset.isInteger());
        RandomAccess<RealType<?>> inputRandomAccess = inDataset.randomAccess();
        RandomAccess<RealType<?>> outputRandomAccess = outDataset.randomAccess();

        inputRandomAccess.setPosition(new long[]{0, 0, 0});
        outputRandomAccess.setPosition(new long[]{0, 0, 0});

        startRoiDataset = System.nanoTime();
        
        for(int x = bounds.x; x < (bounds.x + dimensions[0]); x++){
            for(int y = bounds.y; y < (bounds.y + dimensions[1]); y++){
                for(int slice = 0; slice < dimensions[2]; slice++){
                    
                    inputRandomAccess.setPosition(x, 0);
                    inputRandomAccess.setPosition(y, 1);
                    inputRandomAccess.setPosition(slice, 2);

                    outputRandomAccess.setPosition(x - bounds.x, 0);
                    outputRandomAccess.setPosition(y - bounds.y, 1);
                    outputRandomAccess.setPosition(slice, 2);

                    outputRandomAccess.get().setReal(inputRandomAccess.get().getRealFloat());                                      
                }                
            }
        } 
        long stopRoiDatset = System.nanoTime();
        time1 = stopRoiDatset - startRoiDataset;
    }    
}