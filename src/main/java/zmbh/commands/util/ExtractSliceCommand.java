/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zmbh.commands.util;

import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imagej.display.DatasetView;
import net.imagej.display.ImageDisplay;
import net.imglib2.RandomAccess;
import net.imglib2.type.numeric.RealType;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.display.DisplayService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;



@Plugin(type = Command.class)
public class ExtractSliceCommand implements Command {
    
    @Parameter
    DatasetService datasetService;   
    
    @Parameter
    DisplayService displayService;
    
    @Parameter(type = ItemIO.INPUT, persist = false)
    Dataset inputDataset;
    
    @Parameter
    int sliceNumber;
    
    @Parameter(type = ItemIO.OUTPUT, persist = false)
    Dataset outputDataset;
    
    
    @Override
    public void run() {
           
        long[] dimensions = new long[2];
        dimensions[0] = inputDataset.dimension(0);
        dimensions[1] = inputDataset.dimension(1);
        String inputDatasetName = inputDataset.getName();
        String[] split = inputDatasetName.split("\\.");
        String outputDatasetName = split[0] + "_slice" + sliceNumber + "." + split[1];
        AxisType[] axisArray = new AxisType[]{Axes.X, Axes.Y};
        outputDataset = datasetService.create(dimensions, outputDatasetName, axisArray, inputDataset.getType().getBitsPerPixel(), inputDataset.isSigned(), !inputDataset.isInteger());
        RandomAccess<RealType<?>> inputRandomAccess = inputDataset.randomAccess();
        RandomAccess<RealType<?>> outputRandomAccess = outputDataset.randomAccess();

        inputRandomAccess.setPosition(new long[]{0, 0, sliceNumber});
        outputRandomAccess.setPosition(new long[]{0, 0});

        for(int x = 0; x < dimensions[0]; x++){
            for(int y = 0; y < dimensions[1]; y++){
                inputRandomAccess.setPosition(x, 0);
                inputRandomAccess.setPosition(y, 1);

                outputRandomAccess.setPosition(x, 0);
                outputRandomAccess.setPosition(y, 1);

                outputRandomAccess.get().setReal(inputRandomAccess.get().getRealDouble());
            }
        }
       
    }
}