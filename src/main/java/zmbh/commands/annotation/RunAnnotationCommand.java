/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zmbh.commands.annotation;

import zmbh.commands.segmentation.LoadCellXseedList;
import zmbh.commands.segmentation.CellXseed;
import zmbh.commands.segmentation.CellRecord;
import zmbh.commands.annotation.AnnotationCommand;
import ij.plugin.ImagesToStack;
import io.scif.services.DatasetIOService;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.imagej.Dataset;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.command.CommandModule;
import org.scijava.command.CommandService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import zmbh.commands.segmentation.LoadCellRecordList;

/**
 *
 * @author Potier Guillaume, 2016
 */

@Plugin(type = Command.class, menuPath = "Dev-commands>Annotation>RUN Annotation", label="")
public class RunAnnotationCommand implements Command {
    
    @Parameter
    CommandService cmdService;
    
    @Parameter
    DatasetIOService ioService;
    
    @Parameter(type = ItemIO.INPUT)
    File inDatasetFile;
    
    @Parameter(type = ItemIO.INPUT)
    File cellFile;
    
    @Parameter(type = ItemIO.INPUT)
    File recordClassFile;
    
    @Parameter(type = ItemIO.INPUT)
    String saveDir;
    
    @Override
    public void run() {
        
        Future<CommandModule> promise;
        CommandModule promiseContent;
        try {
            
            // Load list of recorded cells
            List<CellRecord> cellRecordList  = new ArrayList<>();           
            promise = cmdService.run(LoadCellRecordList.class, true, "recordClassFile", recordClassFile);
            promiseContent = promise.get();
            cellRecordList = (List<CellRecord>) promiseContent.getOutput("cellRecordList");
            
            Dataset inDataset = ioService.open(inDatasetFile.getPath());
            
            // Load list of segmented cells
            promise = cmdService.run(LoadCellXseedList.class, false, "cellFile", cellFile);
            promiseContent = promise.get();
            ArrayList<CellXseed> cellxSeedList = (ArrayList<CellXseed>) promiseContent.getOutput("cellxSeedList");
            
            
            promise = cmdService.run(AnnotationCommand.class, true,"inDataset", inDataset, "cellxSeedList", cellxSeedList, "cellRecordList", cellRecordList, "saveDir", saveDir);
            promiseContent = promise.get();
            
        } catch (InterruptedException ex) {
            Logger.getLogger(RunAnnotationCommand.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            Logger.getLogger(RunAnnotationCommand.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(RunAnnotationCommand.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
}