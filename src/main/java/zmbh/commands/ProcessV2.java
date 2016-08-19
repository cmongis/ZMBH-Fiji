/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zmbh.commands;

import io.scif.services.DatasetIOService;
import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.command.CommandModule;
import org.scijava.command.CommandService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author User
 */

@Plugin(type = Command.class, menuPath = "Dev-commands>CMD Process FULL V2", label="")
public class ProcessV2 implements Command {
    
    @Parameter
    CommandService cmdService;
    
    @Parameter
    DatasetIOService ioService;
    
    @Parameter(type = ItemIO.INPUT)
    String rawStackDirPath;
    
    @Parameter(type = ItemIO.INPUT)
    String segResultDirPath;
    
    @Parameter(type = ItemIO.INPUT)
    String resultDirPath;
    
    @Parameter(type = ItemIO.INPUT)
    float darkfieldValue;
    
    @Parameter(type = ItemIO.INPUT)
    File mCherryFlatFieldFile;
    
    @Parameter(type = ItemIO.INPUT)
    File gfpFlatFieldFile;
    
    @Parameter(type = ItemIO.INPUT)
    File bfpFlatFieldFile;
    
    @Parameter(type = ItemIO.INPUT)
    File landMarkFile;
    
    @Parameter(type = ItemIO.INPUT)
    File rScript;
    
    @Parameter(type = ItemIO.INPUT)
    String rLibPath;
    
    @Parameter(type = ItemIO.INPUT)
    File jsonFile;
    
    @Parameter(type = ItemIO.INPUT)
    File resultDir_MEASURE_blueControl;
    
    @Parameter(type = ItemIO.INPUT)
    File resultDir_MEASURE_wtControl;
    
    
    @Override
    public void run() {
        
        try {
            Future<CommandModule> promise;
            CommandModule promiseContent;
            
            promise = cmdService.run(ProcessStep0.class, true,
                    "rawStackDirPath", rawStackDirPath,
                    "segResultDirPath", segResultDirPath,
                    "resultDirPath", resultDirPath,
                    "darkfieldValue", darkfieldValue,
                    "mCherryFlatFieldFile", mCherryFlatFieldFile,
                    "gfpFlatFieldFile", gfpFlatFieldFile,
                    "bfpFlatFieldFile", bfpFlatFieldFile,
                    "landMarkFile", landMarkFile,
                    "jsonFile", jsonFile);
            promiseContent = promise.get();
            
            File correctedDir = (File) promiseContent.getOutput("correctedDir");
            
            promise = cmdService.run(ProcessStep1.class, true,
                    "extendedStackDirPath", correctedDir.getPath(),
                    "segResultDirPath", segResultDirPath,
                    "resultDirPath", resultDirPath,
                    "rScript", rScript,
                    "rLibPath", rLibPath,
                    "resultDir_MEASURE_blueControl", resultDir_MEASURE_blueControl,
                    "resultDir_MEASURE_wtControl", resultDir_MEASURE_wtControl);
            promiseContent = promise.get();
            
            File measureDir = (File) promiseContent.getOutput("measureDir");
            
            promise = cmdService.run(ProcessStep2.class, true,
                    "extendedStackDirPath", correctedDir.getPath(),
                    "segResultDirPath", segResultDirPath,
                    "measureDirPath", measureDir.getPath(),
                    "resultDirPath", resultDirPath);
            promise.get();
            
            
        } catch (InterruptedException ex) {
            Logger.getLogger(ProcessV2.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            Logger.getLogger(ProcessV2.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        
    }
    
}