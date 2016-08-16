/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zmbh.commands;

import io.scif.services.DatasetIOService;
import java.io.File;
import java.io.IOException;
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
import zmbh.commands.measure.RunMeasurementsV4_allDir;

/**
 *
 * @author Potier Guillaume, 2016
 */

@Plugin(type = Command.class, menuPath = "Dev-commands>CMD Process Step 1", label="")
public class ProcessStep1 implements Command {
    
    @Parameter
    CommandService cmdService;
    
    @Parameter
    DatasetIOService ioService;
    
    @Parameter(type = ItemIO.INPUT)
    String extendedStackDirPath;
    
    @Parameter(type = ItemIO.INPUT)
    String segResultDirPath;
    
    @Parameter(type = ItemIO.INPUT)
    String resultDirPath;
    
    @Parameter(type = ItemIO.INPUT)
    File rScript;
    
    @Parameter(type = ItemIO.INPUT)
    String rLibPath;
    
    @Parameter(type = ItemIO.INPUT)
    File resultDir_MEASURE_blueControl;
    
    @Parameter(type = ItemIO.INPUT)
    File resultDir_MEASURE_wtControl;
    
    
    @Override
    public void run() {
        long t0 = System.nanoTime();
        
        File extendedStackDir = new File(extendedStackDirPath);
        File segResultDir = new File(segResultDirPath);
        File resultDir = new File(resultDirPath);
        
        Future<CommandModule> promise;
        CommandModule promiseContent;
        
        if(extendedStackDir.isDirectory() && segResultDir.isDirectory() && resultDir.isDirectory()){
            try {                              
                File resultDir_MEASURE = new File(resultDir.getPath() + "/1_MEASURE");
                resultDir_MEASURE.mkdir();
                
                
                // Make measurements on all extended stacks
                promise = cmdService.run(RunMeasurementsV4_allDir.class, true,
                        "inputStackDir", extendedStackDirPath,
                        "cellDir", segResultDir,
                        "saveDir", resultDir_MEASURE);
                promise.get();
                
                // Run R processing script               
                ProcessBuilder builder = new ProcessBuilder(
                        "Rscript",
                        rScript.getAbsolutePath(),
                        resultDir_MEASURE.getAbsolutePath(),
                        rLibPath,
                        resultDir_MEASURE_blueControl.getAbsolutePath(),
                        resultDir_MEASURE_wtControl.getAbsolutePath());                
                builder.inheritIO();
                try {
                    java.lang.Process process = builder.start();
                    process.waitFor();
                } catch (IOException ex) {
                    Logger.getLogger(RunRScript.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                long t1 = System.nanoTime();
                long sec = TimeUnit.NANOSECONDS.toSeconds(t1 - t0);
                System.out.println(String.format("Process DONE: %d s", sec));
                
            } catch (InterruptedException ex) {
                Logger.getLogger(ProcessStep1.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ExecutionException ex) {
                Logger.getLogger(ProcessStep1.class.getName()).log(Level.SEVERE, null, ex);
            }         
        }
        
    }
    
}