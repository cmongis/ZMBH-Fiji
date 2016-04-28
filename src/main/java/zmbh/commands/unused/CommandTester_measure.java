/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zmbh.commands.unused;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.scijava.command.Command;
import org.scijava.plugin.Plugin;

/**
 * @author User
 */

@Plugin(type = Command.class, menuPath = "Dev-commands>CMD measure", label="")
public class CommandTester_measure implements Command {

    @Override
    public void run() {
        
        DescriptiveStatistics stats = new DescriptiveStatistics();
        
    }
}
