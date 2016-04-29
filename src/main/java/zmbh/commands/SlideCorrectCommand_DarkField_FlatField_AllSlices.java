package zmbh.commands;

import io.scif.services.DatasetIOService;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.imagej.Dataset;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.command.CommandModule;
import org.scijava.command.CommandService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = Command.class, menuPath = "Dev-commands>CMD darkField & flatfield correct slice (all slices)", label="")
public class SlideCorrectCommand_DarkField_FlatField_AllSlices implements Command {
    
    @Parameter
    CommandService cmdService;
    
    @Parameter
    DatasetIOService ioService;
    
    @Parameter(type = ItemIO.BOTH)
    Dataset inputDataset;
    
    @Parameter(type = ItemIO.INPUT)
    float darkfieldValue;
    
    @Parameter(type = ItemIO.INPUT)
    File mCherryFlatFieldFile;
    
    @Parameter(type = ItemIO.INPUT)
    File gfpFlatFieldFile;
    
    @Parameter(type = ItemIO.INPUT)
    File bfpFlatFieldFile;
    
    @Override
    public void run() {
        
        try {
            
            Future<CommandModule> promise;
            CommandModule promiseContent;
            
            Dataset mcherryFlatFieldDataset = ioService.open(mCherryFlatFieldFile.getPath());
            Dataset gfpFlatFieldDataset = ioService.open(gfpFlatFieldFile.getPath());
            Dataset bfpFlatFieldDataset = ioService.open(bfpFlatFieldFile.getPath());
            

            promise = cmdService.run(SliceCorrectCommand_DarkField_FlatField.class, true,
                    "inputDataset", inputDataset,
                    "darkfieldValue", darkfieldValue,
                    "sliceNumber", 0,
                    "flatFieldDataset", mcherryFlatFieldDataset);
            promiseContent = promise.get();
            inputDataset = (Dataset) promiseContent.getOutput("inputDataset");
            
            promise = cmdService.run(SliceCorrectCommand_DarkField_FlatField.class, true,
                    "inputDataset", inputDataset,
                    "darkfieldValue", darkfieldValue,
                    "sliceNumber", 1,
                    "flatFieldDataset", mcherryFlatFieldDataset);
            promiseContent = promise.get();
            inputDataset = (Dataset) promiseContent.getOutput("inputDataset");
            
            promise = cmdService.run(SliceCorrectCommand_DarkField_FlatField.class, true,
                    "inputDataset", inputDataset,
                    "darkfieldValue", darkfieldValue,
                    "sliceNumber", 2,
                    "flatFieldDataset", gfpFlatFieldDataset);
            promiseContent = promise.get();
            inputDataset = (Dataset) promiseContent.getOutput("inputDataset");
            
            promise = cmdService.run(SliceCorrectCommand_DarkField_FlatField.class, true,
                    "inputDataset", inputDataset,
                    "darkfieldValue", darkfieldValue,
                    "sliceNumber", 3,
                    "flatFieldDataset", gfpFlatFieldDataset);
            promiseContent = promise.get();
            inputDataset = (Dataset) promiseContent.getOutput("inputDataset");
            
            promise = cmdService.run(SliceCorrectCommand_DarkField_FlatField.class, true,
                    "inputDataset", inputDataset,
                    "darkfieldValue", darkfieldValue,
                    "sliceNumber", 4,
                    "flatFieldDataset", bfpFlatFieldDataset);
            promiseContent = promise.get();
            inputDataset = (Dataset) promiseContent.getOutput("inputDataset");
        } catch (IOException ex) {
            Logger.getLogger(SlideCorrectCommand_DarkField_FlatField_AllSlices.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(SlideCorrectCommand_DarkField_FlatField_AllSlices.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            Logger.getLogger(SlideCorrectCommand_DarkField_FlatField_AllSlices.class.getName()).log(Level.SEVERE, null, ex);
        }  
    }   
}
