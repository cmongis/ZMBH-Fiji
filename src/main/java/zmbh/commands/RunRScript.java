/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zmbh.commands;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.scijava.command.Command;
import org.scijava.plugin.Plugin;
/**
 *
 * @author Potier Guillaume, 2016
 */

@Plugin(type = Command.class, menuPath = "Dev-commands>RUN Rscript", label="")
public class RunRScript implements Command {

    @Override
    public void run() {
        
        
        ProcessBuilder builder = new ProcessBuilder(
                "Rscript",
                "--default-packages=stats,graphics,grDevices,utils,datasets,base,methods,base,ggplot2,ggrepel,coin,plotly",
                "C:\\Users\\User\\Desktop\\R scripts\\myLib2.R",
                "C:\\Users\\User\\Documents\\testProcess\\Out\\1_MEASURE");                
        builder.inheritIO();
        try {
            builder.start();
        } catch (IOException ex) {
            Logger.getLogger(RunRScript.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        
        
    }
    
}