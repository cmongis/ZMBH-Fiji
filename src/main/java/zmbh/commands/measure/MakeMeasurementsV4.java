/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zmbh.commands.measure;

import ij.ImagePlus;
import ij.gui.Roi;
import ij.measure.ResultsTable;
import ij.plugin.filter.Analyzer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.imagej.Dataset;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.command.CommandModule;
import org.scijava.command.CommandService;
import org.scijava.display.DisplayService;
import org.scijava.module.ModuleService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginService;
import org.scijava.ui.UIService;
import zmbh.commands.util.ExtractSliceCommand;
import zmbh.commands.ImageJ1PluginAdapter;

/**
 *
 * @author Potier Guillaume, 2016
 */

@Plugin(type = Command.class)
public class MakeMeasurementsV4 implements Command {
    
    @Parameter
    CommandService cmdService;
    
    @Parameter
    ModuleService mService;
    
    @Parameter
    UIService uiService;
    
    @Parameter
    PluginService pluginService;
    
    @Parameter(type = ItemIO.INPUT)
    Dataset roiDataset;
    
    @Parameter(type = ItemIO.INPUT)
    ImagePlus inDatasetImp;
    
    @Parameter
    DisplayService displayService;
    
    @Parameter(type = ItemIO.INPUT)
    int nbSlice;
    
    @Parameter(type = ItemIO.INPUT)
    Roi roi;
    
    @Parameter(type = ItemIO.INPUT)
    Analyzer analyzer;
    
    
    @Parameter(type = ItemIO.OUTPUT, persist = false)
    ResultsTable res;
    
    
    
    @Override
    public void run() {
        
        inDatasetImp.setRoi(roi);       
        analyzer.setup("", inDatasetImp);
        ResultsTable resultTable = Analyzer.getResultsTable();
        resultTable.showRowNumbers(false);
        resultTable.update(Analyzer.getMeasurements(), inDatasetImp, null);
        resultTable.reset();
        
        GLCM_Texture glcm = new GLCM_Texture();
        glcm.showDialog();
        glcm.rt.reset();
                
        for(int sliceNumber = 0; sliceNumber < nbSlice; sliceNumber++){
            try {                
                ImagePlus sliceDatasetImp = null;
                if(roiDataset != null){
                    Future<CommandModule> promise = cmdService.run(ExtractSliceCommand.class, true, "inputDataset", roiDataset, "sliceNumber", sliceNumber);
                    CommandModule promiseContent = promise.get();
                    Dataset sliceDataset = (Dataset) promiseContent.getOutput("outputDataset");

                    sliceDatasetImp = ImageJ1PluginAdapter.unwrapDataset(sliceDataset);
                    sliceDatasetImp.resetDisplayRange();
                }
                inDatasetImp.setPosition(sliceNumber+1, 1, 1);
                analyzer.measure();
                resultTable.show("Results");
                if(sliceDatasetImp != null){ 
                    // Get Haralick features
                    glcm.run(sliceDatasetImp.getProcessor().convertToByteProcessor(true));  
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(MakeMeasurementsV4.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ExecutionException ex) {
                Logger.getLogger(MakeMeasurementsV4.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        inDatasetImp.deleteRoi();
        res = (ResultsTable) resultTable.clone();
    }     
}
    