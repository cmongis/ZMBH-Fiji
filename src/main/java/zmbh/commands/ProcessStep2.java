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
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.command.CommandModule;
import org.scijava.command.CommandService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import zmbh.commands.annotation.RunAnnotationCommand_allDir;
import zmbh.commands.annotation.RunComposeImg;

/**
 *
 * @author Potier Guillaume, 2016
 */

@Plugin(type = Command.class, menuPath = "Dev-commands>CMD Process Step 2", label="")
public class ProcessStep2 implements Command {
    
    @Parameter
    CommandService cmdService;
    
    @Parameter
    DatasetIOService ioService;
    
    @Parameter(type = ItemIO.INPUT)
    String extendedStackDirPath;
    
    @Parameter(type = ItemIO.INPUT)
    String segResultDirPath;
    
    @Parameter(type = ItemIO.INPUT)
    String measureDirPath;
    
    @Parameter(type = ItemIO.INPUT)
    String resultDirPath;
    
    
    @Override
    public void run() {
        long t0 = System.nanoTime();
        
        File extendedStackDir = new File(extendedStackDirPath);
        File segResultDir = new File(segResultDirPath);
        File resultDir = new File(resultDirPath);
        File measureDir = new File(measureDirPath);
        
        Future<CommandModule> promise;
        CommandModule promiseContent;
        
        if(extendedStackDir.isDirectory() && segResultDir.isDirectory() && resultDir.isDirectory()){
            try {
                                
                
                File resultDir_ANNOTATION = new File(resultDir.getPath() + "/2_ANNOTATION");
                resultDir_ANNOTATION.mkdir();
                
                File resultDir_ANNOTATION_annotatedStacks = new File(resultDir_ANNOTATION.getPath() + "/annotated stacks");
                resultDir_ANNOTATION_annotatedStacks.mkdir();
                
                 File resultDir_Measure_annotatedRecords = new File(measureDir.getPath() + "/annotatedRecords");
                
                // Run image annotation
                promise = cmdService.run(RunAnnotationCommand_allDir.class, true,
                        "inputStackDir", extendedStackDir,
                        "annotedRecordDir", resultDir_Measure_annotatedRecords,
                        "cellDir", segResultDir,
                        "saveDir", resultDir_ANNOTATION_annotatedStacks);
                promise.get();
                
                
                File resultDir_ANNOTATION_compositeImg = new File(resultDir_ANNOTATION.getPath() + "/composite Images");
                resultDir_ANNOTATION_compositeImg.mkdir();
                
                 // Run composite image generation
                for(int i = 0; i < 5; i++){
                    promise = cmdService.run(RunComposeImg.class, true,
                        "stackDirPath", extendedStackDir.getPath(),
                        "cellDirPath", segResultDir.getPath(),
                        "recordClassDirPath", resultDir_Measure_annotatedRecords.getPath(),
                        "sliceNumber", i,
                        "isWell", true,
                        "saveDir", resultDir_ANNOTATION_compositeImg.getPath());
                promise.get();
                }

                long t1 = System.nanoTime();
                long sec = TimeUnit.NANOSECONDS.toSeconds(t1 - t0);
                System.out.println(String.format("Process DONE: %d s", sec));
                
            } catch (InterruptedException ex) {
                Logger.getLogger(ProcessStep2.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ExecutionException ex) {
                Logger.getLogger(ProcessStep2.class.getName()).log(Level.SEVERE, null, ex);
            }         
        }
        else{
            
        }
        
    }
    
}