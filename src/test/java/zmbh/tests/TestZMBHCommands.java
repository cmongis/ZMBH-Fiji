package zmbh.tests;


import io.scif.services.DatasetIOService;
import org.junit.Assert;
import org.junit.Test;
import org.scijava.command.CommandService;
import org.scijava.plugin.Parameter;
import zmbh.commands.util.ConvertDatasetEncodingCommand;

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
    
    @Parameter
    DatasetIOService ioService;
    
    
    @Test
    public void testCmdService() { 
        Assert.assertNotNull("cmdService should not be null", cmdService);   
    }
    
    @Test
    public void testIoService() {   
        Assert.assertNotNull("ioService should not be null", ioService);   
    }
            
    @Test
    public void testConvertDatasetEncodingCommand(){
        //Dataset inDataset = ioService.open(source)
        //cmdService.run(ConvertDatasetEncodingCommand.class, true, )
    }
    
}
