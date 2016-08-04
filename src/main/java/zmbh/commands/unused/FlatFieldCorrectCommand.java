/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zmbh.commands.unused;

import io.scif.services.DatasetIOService;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.imagej.Dataset;
import net.imagej.display.ImageDisplay;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * @author Guillaume
 */

@Plugin(type = Command.class)
public class FlatFieldCorrectCommand implements Command {

    @Parameter
    DatasetIOService datasetIoService;
    
    @Parameter
    FlatFieldCorrectionService flatFieldCorrectionService;
    
    @Parameter(type = ItemIO.BOTH)
    Dataset dataset;
    
    @Parameter(label = "FlatField to open")
    File flatFieldFile;
    
    @Parameter
    ImageDisplay currentDisplay;
    
    @Parameter
    int darkCurrentValue;
            
    
    @Override
    public void run() {
        Dataset flatFieldDataset = null;
        try {
            flatFieldDataset = datasetIoService.open(flatFieldFile.getPath());
        } catch (IOException ex) {
            Logger.getLogger(FlatFieldCorrectCommand.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        /*
        Dataset mCherryFlatFieldDataset = null;
        Dataset gfpFlatFieldDataset = null;
        
        try {
            mCherryFlatFieldDataset = datasetIoService.open("src/main/resources/Flat_field/FF_Img_mCherry.tif");
        } catch (IOException ex) {
            Logger.getLogger(FlatFieldCorrectCommand.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        try {
            gfpFlatFieldDataset = datasetIoService.open("src/main/resources/Flat_field/FF_Img_mCherry.tif");
        } catch (IOException ex) {
            Logger.getLogger(FlatFieldCorrectCommand.class.getName()).log(Level.SEVERE, null, ex);
        }
        */
        
        flatFieldCorrectionService.substractDarkSignal(currentDisplay, dataset, darkCurrentValue);
        //flatFieldCorrectionService.divideBymCherryFlatField(dataset, mCherryFlatFieldDataset);
        //flatFieldCorrectionService.divideByGfpFlatField(dataset, gfpFlatFieldDataset);
        
        flatFieldCorrectionService.divideByFlatField(currentDisplay, dataset, flatFieldDataset);
    }
}
