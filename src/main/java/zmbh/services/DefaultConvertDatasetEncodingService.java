package zmbh.services;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.util.Arrays;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.axis.AxisType;
import net.imagej.types.BigComplex;
import net.imagej.types.DataType;
import net.imagej.types.DataTypeService;
import net.imglib2.Cursor;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.SciJavaService;

/**
 * @author Guillaume Potier
 */

@Plugin(type = SciJavaService.class)
public class DefaultConvertDatasetEncodingService extends AbstractService implements ConvertDatasetEncodingService {
    
    @Parameter
    DatasetService datasetService;
    
    @Parameter
    DataTypeService dataTypeService;

    @Override
    public Dataset convert(Dataset inputDataset, DataType targetEncoding) {
        Dataset outputDataset = null;
        System.out.println("Input dataset encoding: " + inputDataset.getTypeLabelLong());
        
        long[] dimensions = new long[inputDataset.numDimensions()];
        AxisType[] axisTypeArray = new AxisType[inputDataset.numDimensions()];
        for(int i = 0; i < dimensions.length; i++){
            dimensions[i] = inputDataset.dimension(i);
            axisTypeArray[i] = inputDataset.axis(i).type();
        }
        System.out.println("Input dataset dimensions: " + Arrays.toString(dimensions));
        System.out.println("Output dataset encoding: " + targetEncoding.longName());
        String inputDatasetName = inputDataset.getName();
        String convertedDatasetName = "new_" + inputDatasetName;
        System.out.println("Output dataset name: " + convertedDatasetName);
        System.out.println("Output type bitCount: " + targetEncoding.bitCount());
        //outputDataset = datasetService.create(dimensions, convertedDatasetName, axisTypeArray, targetEncoding.bitCount(), targetEncoding.isSigned(), targetEncoding.isFloat());
        outputDataset = datasetService.create(dimensions, inputDatasetName, axisTypeArray, targetEncoding.bitCount(), targetEncoding.isSigned(), targetEncoding.isFloat());

        DataType inputEncoding = dataTypeService.getTypeByName(inputDataset.getTypeLabelLong());
        
        Cursor<RealType<?>> inputDatasetCursor = inputDataset.cursor();
        Cursor<RealType<?>> targetDatasetCursor = outputDataset.cursor();
        
        RealType inputCurrentPixel;
        RealType outputCurrentPixel;
        BigComplex tmp = new BigComplex();
        
        int counter = 0;
                
        while(inputDatasetCursor.hasNext() && targetDatasetCursor.hasNext()){       
            counter++;
            inputDatasetCursor.next();
            targetDatasetCursor.next();
            inputCurrentPixel = inputDatasetCursor.get();
            outputCurrentPixel = (RealType) targetEncoding.getType().createVariable();
            
            inputEncoding.cast((NumericType) inputCurrentPixel, tmp);
            targetEncoding.cast(tmp, (NumericType) outputCurrentPixel);
            targetDatasetCursor.get().setReal(outputCurrentPixel.getRealDouble());        
        }

        System.out.println("Elements processed: " + counter);
        System.out.println("CONVERT PASSED");       
        return outputDataset;
    }
}
