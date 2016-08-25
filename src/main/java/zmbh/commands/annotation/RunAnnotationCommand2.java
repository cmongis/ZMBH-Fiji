/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zmbh.commands.annotation;

import zmbh.commands.segmentation.LoadCellXseedList;
import zmbh.commands.segmentation.CellXseed;
import io.scif.services.DatasetIOService;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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

/**
 *
 * @author Potier Guillaume, 2016
 */

@Plugin(type = Command.class, menuPath = "Dev-commands>Annotation>RUN Annotation 2 (1 color)", label="")
public class RunAnnotationCommand2 implements Command {
    
    @Parameter
    CommandService cmdService;
    
    @Parameter
    DatasetIOService ioService;
    
    @Parameter(type = ItemIO.INPUT)
    File inDatasetFile;
    
    @Parameter(type = ItemIO.INPUT)
    File cellFile;
    
    
    @Override
    public void run() {
        
        Future<CommandModule> promise;
        CommandModule promiseContent;
        try {
            Dataset inDataset = ioService.open(inDatasetFile.getPath());
            
            // Load list of segmented cells
            promise = cmdService.run(LoadCellXseedList.class, false, "cellFile", cellFile);
            promiseContent = promise.get();
            ArrayList<CellXseed> cellxSeedList = (ArrayList<CellXseed>) promiseContent.getOutput("cellxSeedList");
                        
            promise = cmdService.run(AnnotationCommand2.class, true,"inDataset", inDataset, "cellxSeedList", cellxSeedList);
            promiseContent = promise.get();
            
        } catch (InterruptedException | ExecutionException | IOException ex) {
            Logger.getLogger(RunAnnotationCommand2.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
}