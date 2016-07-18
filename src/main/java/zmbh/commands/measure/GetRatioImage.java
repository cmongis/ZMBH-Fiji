/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zmbh.commands.measure;

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
import org.scijava.ui.UIService;

/**
 *
 * @author Potier Guillaume, 2016
 */

@Plugin(type = Command.class, menuPath = "Dev-commands>Measure>CMD getRatioImage", label="")
public class GetRatioImage implements Command {
    
    @Parameter
    DatasetService datasetService;
    
    @Parameter
    UIService ui;
    
    @Parameter(type = ItemIO.INPUT)
    Dataset stack;
    
    @Parameter
    int sliceNum1;
    
    @Parameter
    int sliceNum2;
    
    @Parameter(type = ItemIO.OUTPUT)
    Dataset ratioDataset;
    
    @Override
    public void run() {
        
        long[] dimensions = new long[2];
        dimensions[0] = stack.dimension(0);
        dimensions[1] = stack.dimension(1);
        String inputDatasetName = stack.getName();
        String[] split = inputDatasetName.split("\\.");
        String outputDatasetName = split[0] + "_ratio" + "." + split[1];
        AxisType[] axisArray = new AxisType[]{Axes.X, Axes.Y};
        ratioDataset = datasetService.create(dimensions, outputDatasetName, axisArray, stack.getType().getBitsPerPixel(), stack.isSigned(), !stack.isInteger());
        
        RandomAccess<RealType<?>> inputRandomAccess1 = stack.randomAccess();
        RandomAccess<RealType<?>> inputRandomAccess2 = stack.randomAccess();
        RandomAccess<RealType<?>> outputRandomAccess = ratioDataset.randomAccess();

        inputRandomAccess1.setPosition(new long[]{0, 0, sliceNum1});
        inputRandomAccess2.setPosition(new long[]{0, 0, sliceNum2});
        outputRandomAccess.setPosition(new long[]{0, 0});

        for(int x = 0; x < dimensions[0]; x++){
            for(int y = 0; y < dimensions[1]; y++){
                inputRandomAccess1.setPosition(x, 0);
                inputRandomAccess1.setPosition(y, 1);
                
                inputRandomAccess2.setPosition(x, 0);
                inputRandomAccess2.setPosition(y, 1);

                outputRandomAccess.setPosition(x, 0);
                outputRandomAccess.setPosition(y, 1);

                outputRandomAccess.get().setReal(inputRandomAccess1.get().getRealDouble() / inputRandomAccess2.get().getRealDouble());
            }
        }
        //ui.show(ratioDataset);
        
    }
    
}