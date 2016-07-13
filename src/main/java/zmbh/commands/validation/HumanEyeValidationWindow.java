/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zmbh.commands.validation;

import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author User
 */


@Plugin(type = Command.class)
public class HumanEyeValidationWindow implements Command {
    
    
    
    @Parameter(type = ItemIO.INPUT, persist = false)
    boolean dead;
    
    @Parameter(type = ItemIO.INPUT, persist = false)
    boolean overlap;
    
    @Parameter(type = ItemIO.INPUT, persist = false)
    boolean outfocus;
    
    @Parameter(type = ItemIO.INPUT, persist = false)
    boolean good;
    
    @Parameter(type = ItemIO.INPUT, persist = false)
    boolean stop;
    
    
    
    @Parameter(type = ItemIO.OUTPUT)
    boolean outdead;
    
    @Parameter(type = ItemIO.OUTPUT)
    boolean outoverlap;
    
    @Parameter(type = ItemIO.OUTPUT)
    boolean outoutfocus;
    
    @Parameter(type = ItemIO.OUTPUT)
    boolean outgood;
    
    @Parameter(type = ItemIO.OUTPUT)
    boolean outstop;
    
    @Override
    public void run() {
        outdead = dead;
        outoverlap = overlap;
        outoutfocus = outfocus;
        outgood = good;
        outstop = stop;
        
    }
}
