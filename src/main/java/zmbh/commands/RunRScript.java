/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zmbh.commands;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
/**
 *
 * @author Potier Guillaume, 2016
 */

@Plugin(type = Command.class, menuPath = "Dev-commands>CMD Run Rscript", label="")
public class RunRScript implements Command {
    
    @Parameter(type = ItemIO.INPUT)
    File rScript;
    
    @Parameter(type = ItemIO.INPUT)
    String rLibPath;
    
    @Parameter(type = ItemIO.INPUT)
    File resultDir_MEASURE;
    
    @Parameter(type = ItemIO.INPUT)
    File resultDir_MEASURE_blueControl;
    
    @Parameter(type = ItemIO.INPUT)
    File resultDir_MEASURE_wtControl;
    
    @Parameter(type = ItemIO.INPUT)
    File phantomJS_dir;
    
    @Override
    public void run() {
        
        
        // Run R processing script               
        ProcessBuilder builder = new ProcessBuilder(
                "Rscript",
                rScript.getAbsolutePath(),
                resultDir_MEASURE.getAbsolutePath(),
                rLibPath,
                resultDir_MEASURE_blueControl.getAbsolutePath(),
                resultDir_MEASURE_wtControl.getAbsolutePath(),
                phantomJS_dir.getAbsolutePath());                
        builder.inheritIO();
        try {
            java.lang.Process process = builder.start();
            process.waitFor();
        } catch (IOException ex) {
            Logger.getLogger(RunRScript.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(RunRScript.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        
        
    }
    
}