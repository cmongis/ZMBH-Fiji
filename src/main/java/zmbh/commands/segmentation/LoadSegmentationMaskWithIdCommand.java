/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zmbh.commands.segmentation;

import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLDouble;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
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
 * @author User
 */


@Plugin(type = Command.class, menuPath = "Dev-commands>Segmentation>CMD Load segmentation mask with IDs(CellX)", label="")
public class LoadSegmentationMaskWithIdCommand implements Command {
    
    @Parameter
    DatasetService datasetService;
    
    @Parameter(type = ItemIO.INPUT)
    File maskFile;
    
    @Parameter(type = ItemIO.OUTPUT)
    Dataset maskDataset;

    @Override
    public void run() {
        
        try {
            
            MatFileReader reader = new MatFileReader(maskFile.getPath());
            Map<String, MLArray> content = reader.getContent();
            System.out.println(content);
            MLDouble dataArray = (MLDouble) content.get("segmMask");
            //MLDouble dataArray = (MLDouble) content.get("segmentationMask");
            
            long[] dimensions = new long[dataArray.getNDimensions()];
            
            for(int i = 0; i < dataArray.getNDimensions(); i++){
                dimensions[dataArray.getNDimensions() - i -1] = (long) dataArray.getDimensions()[i];  
            }
            
            System.out.println(Arrays.toString(dimensions));
            String name = maskFile.getName();
            AxisType[] axisArray = new AxisType[]{Axes.X, Axes.Y};
            
            maskDataset = datasetService.create(dimensions, name, axisArray, 32, false, false);
            
            RandomAccess<RealType<?>> randomAccess = maskDataset.randomAccess();

            for(int x = 0; x < dimensions[0]; x++){
                for(int y = 0; y < dimensions[1]; y++){
                    randomAccess.setPosition(x, 0);
                    randomAccess.setPosition(y, 1);
                    randomAccess.get().setReal(dataArray.get(y, x));
                }
            }  
        } catch (IOException ex) {
            Logger.getLogger(LoadSegmentationMaskCommand.class.getName()).log(Level.SEVERE, null, ex);
        }
    }   
}