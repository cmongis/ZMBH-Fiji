package zmbh.commands;

import net.imagej.Dataset;
import net.imagej.types.DataType;
import net.imagej.types.DataTypeService;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import zmbh.services.ConvertDatasetEncodingService;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * @author Guillaume Potier
 */


@Plugin(type = Command.class, menuPath = "Dev-commands>CMD convert encoding")
public class ConvertDatasetEncodingCommand implements Command {
    
    @Parameter
    DataTypeService dataTypeService;

    @Parameter(type = ItemIO.INPUT)
    Dataset inDataset;
    
    @Parameter(type = ItemIO.OUTPUT)
    Dataset outDataset;
      
    @Parameter
    ConvertDatasetEncodingService convertDatasetEncodingService;
    
    @Parameter(type = ItemIO.INPUT)
    String targetType;
    
    @Override
    public void run() {

        DataType<?> typeByName = dataTypeService.getTypeByName(targetType);
        if(typeByName != null){
            outDataset = convertDatasetEncodingService.convert(inDataset, typeByName);
        }
        else{
            System.err.println("Unknown type " + targetType);
        }         
    }   
}
