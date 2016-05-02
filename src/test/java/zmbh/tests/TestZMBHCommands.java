package zmbh.tests;


import org.junit.Assert;
import org.junit.Test;
import org.scijava.command.CommandService;
import org.scijava.plugin.Parameter;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author User
 */
public class TestZMBHCommands extends BaseSciJavaTest{
    
    @Parameter
    CommandService cmdService;
    
    @Test
    public void testCmdService() {        
        Assert.assertNotNull("cmdService should not be null", cmdService);   
    }
            
    @Test
    public void testConvertDatasetEncodingCommand(){
        
    }
    
}
