/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zmbh.commands;

import java.io.File;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author User
 */

@Plugin(type = Command.class, menuPath = "Dev-commands>CMD read Phenorip", label="")
public class ReadPhenorip implements Command {
    
    @Parameter
    File matFile;
    
    @Override
    public void run() {
        
    }
    
    
}