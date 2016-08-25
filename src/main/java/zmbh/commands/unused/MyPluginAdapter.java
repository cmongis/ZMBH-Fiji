/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zmbh.commands.unused;

import ij.ImagePlus;
import ij.plugin.filter.Analyzer;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;

/**
 *
 * @author User
 */
public class MyPluginAdapter implements Command {

    @Parameter (type = ItemIO.INPUT)
    ImagePlus imagePlusInput;
    
    
    @Override
    public void run() {
        
        Analyzer analyzer = new Analyzer();
        System.out.println("WORKKS");
        
    }
    
}
