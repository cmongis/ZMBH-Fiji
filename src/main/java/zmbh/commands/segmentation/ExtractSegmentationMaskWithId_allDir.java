/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zmbh.commands.segmentation;

import io.scif.services.DatasetIOService;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.imagej.Dataset;
import net.imagej.DatasetService;
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

@Plugin(type = Command.class, menuPath = "Dev-commands>Segmentation>CMD Extract segmentation mask with IDs(CellX) alldir", label="")
public class ExtractSegmentationMaskWithId_allDir implements Command {
    
    @Parameter
    CommandService cmdService;
    
    @Parameter
    DatasetIOService ioService;
    
    @Parameter(type = ItemIO.INPUT)
    File maskDir;
    
    @Parameter(type = ItemIO.INPUT)
    File saveDir;
    
    @Override
    public void run() {
        
        if(maskDir.isDirectory() && saveDir.isDirectory()){
            Future<CommandModule> promise;
            CommandModule promiseContent;
            File[] masksFileList = maskDir.listFiles((File pathname) -> pathname.getName().endsWith(".mat"));
            for(File mask : masksFileList){
                try {
                    promise = cmdService.run(LoadSegmentationMaskWithIdCommand.class, true, "maskFile", mask);
                    promiseContent = promise.get();
                    Dataset maskdataset = (Dataset) promiseContent.getOutput("maskDataset");
                    ioService.save(maskdataset, saveDir.getPath() + "/" + maskdataset.getName() + ".tif");
                    ij.WindowManager.closeAllWindows();
                    
                } catch (InterruptedException ex) {
                    Logger.getLogger(ExtractSegmentationMaskWithId_allDir.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ExecutionException ex) {
                    Logger.getLogger(ExtractSegmentationMaskWithId_allDir.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(ExtractSegmentationMaskWithId_allDir.class.getName()).log(Level.SEVERE, null, ex);
                }
                
            }
        }
        
    }
    
}