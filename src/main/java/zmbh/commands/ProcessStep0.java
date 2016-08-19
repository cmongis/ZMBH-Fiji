/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zmbh.commands;

import bunwarpj.MiscTools;
import bunwarpj.Param;
import bunwarpj.Transformation;
import bunwarpj.bUnwarpJ_;
import io.scif.services.DatasetIOService;
import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.imagej.Dataset;
import net.imagej.axis.Axes;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.command.CommandModule;
import org.scijava.command.CommandService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import zmbh.commands.correction.ChromaCorrect;
import zmbh.commands.correction.GetBackgroundCorrectedImages;
import zmbh.commands.correction.GetFFImg;
import zmbh.commands.measure.GetRatioImage;
import zmbh.commands.util.AddSliceToStack;
import zmbh.config.Get4ImgStack;
import zmbh.config.LoadJSON2;

/**
 *
 * @author Potier Guillaume, 2016
 */

@Plugin(type = Command.class, menuPath = "Dev-commands>CMD Process Step 0 (darkfield + flatfield + chroma + ratio)", label="")
public class ProcessStep0 implements Command {
    
    @Parameter
    CommandService cmdService;
    
    @Parameter
    DatasetIOService ioService;
    
    @Parameter(type = ItemIO.INPUT)
    String rawStackDirPath;
    
    @Parameter(type = ItemIO.INPUT)
    String segResultDirPath;
    
    @Parameter(type = ItemIO.INPUT)
    String resultDirPath;
    
    @Parameter(type = ItemIO.INPUT)
    float darkfieldValue;
    
    @Parameter(type = ItemIO.INPUT)
    File mCherryFlatFieldFile;
    
    @Parameter(type = ItemIO.INPUT)
    File gfpFlatFieldFile;
    
    @Parameter(type = ItemIO.INPUT)
    File bfpFlatFieldFile;
    
    @Parameter(type = ItemIO.INPUT)
    File landMarkFile;   
    
    @Parameter(type = ItemIO.INPUT)
    File jsonFile;
    
    @Parameter(type = ItemIO.OUTPUT)
    File correctedDir;
    
    @Override
    public void run() {
        long t0 = System.nanoTime();
        
        File rawStackDir = new File(rawStackDirPath);
        File resultDir = new File(resultDirPath);
        
        Future<CommandModule> promise;
        CommandModule promiseContent;
        
        if(rawStackDir.isDirectory() && resultDir.isDirectory()){
            try {
                
                File resultDir_STACKS = new File(resultDir.getPath() + "/0_STACKS");
                resultDir_STACKS.mkdir();
                
                File resultDir_STACKS_rawStacks = new File(resultDir_STACKS.getPath() + "/raw stacks");
                resultDir_STACKS_rawStacks.mkdir();
                
                promise = cmdService.run(LoadJSON2.class, true, "jsonFile", jsonFile);
                promiseContent = promise.get();
                Map<String, Map<String, Integer>> imageMap = (Map<String, Map<String, Integer>>) promiseContent.getOutput("outObject");
                
                // Get 4 image stacks
                File[] fileStackList = rawStackDir.listFiles((File pathname) -> pathname.getName().endsWith(".tif"));
                for(File stack : fileStackList){
                    Dataset inStack = ioService.open(stack.getPath());
                    promise = cmdService.run(Get4ImgStack.class, true,
                        "inStack", inStack,
                        "imageMap", imageMap);
                    promiseContent = promise.get();
                    Dataset dataset = (Dataset) promiseContent.getOutput("outStack");
                    ioService.save(dataset, resultDir_STACKS_rawStacks + "/" + dataset.getName());
                }
                
                File resultDir_STACKS_correctedStacks = new File(resultDir_STACKS.getPath() + "/corrected stacks");
                resultDir_STACKS_correctedStacks.mkdir();
                
                // FlatField & DarkField corrections
                promise = cmdService.run(GetFFImg.class, true,
                        "inputStackDir", resultDir_STACKS_rawStacks,
                        "saveDir", resultDir_STACKS_correctedStacks,
                        "targetType", "32-bit signed float",
                        "darkfieldValue", 100,
                        "mCherryFlatFieldFile", mCherryFlatFieldFile,
                        "gfpFlatFieldFile", gfpFlatFieldFile,
                        "bfpFlatFieldFile", bfpFlatFieldFile);
                promiseContent = promise.get();

                // Chromatic aberration correction on 300ms images exposure and 1s image expose
                // 0(mCherry300) & 2(sfGFP300)
                // 1(mCherry1000) & 3(sfGFP1000)
                Stack<Point> sourcePoints = new Stack<>();
                Stack<Point> targetPoints = new Stack<>();
                Param parameter = new Param(2, 0, 3, 4, 0, 0, 1, 0, 0, 0.01);
                MiscTools.loadPoints(landMarkFile.getPath(), sourcePoints, targetPoints);
                Dataset crapstack = ioService.open(mCherryFlatFieldFile.getPath());
                Transformation warp = bUnwarpJ_.computeTransformationBatch((int)crapstack.dimension(0), (int)crapstack.dimension(1), (int)crapstack.dimension(0), (int)crapstack.dimension(1), sourcePoints, targetPoints, parameter);
                crapstack = null;
                
                File[] stackFileList = resultDir_STACKS_correctedStacks.listFiles((File pathname) -> pathname.getName().endsWith(".tif"));
                for(File stack : stackFileList){
                    Dataset inStack = ioService.open(stack.getPath());                    
                    promise = cmdService.run(ChromaCorrect.class, true,
                            "stack", inStack,
                            "sourceSlice", 1,
                            "targetSlice", 0,
                            "warp", warp);
                    promiseContent = promise.get();                    
                    Dataset tmpStack = (Dataset) promiseContent.getOutput("outStack");
                    ioService.save(tmpStack, resultDir_STACKS_correctedStacks + "/" + tmpStack.getName());
                }
                
                File resultDir_STACKS_extendedStacks = new File(resultDir_STACKS.getPath() + "/extended stacks");
                resultDir_STACKS_extendedStacks.mkdir();
                
                // Compute mCherry300/sfGFP300 and mCherry1000/sfGFP1000 images
                // add ratio images to stack and save it in "extended stacks" directory
                for(File stack : stackFileList){
                    Dataset inDataset = ioService.open(stack.getPath());
                    promise = cmdService.run(GetRatioImage.class, true,
                            "stack", inDataset,
                            "sliceNum1", 0,
                            "sliceNum2", 1);
                    promiseContent = promise.get();
                    Dataset ratioDataset  = (Dataset) promiseContent.getOutput("ratioDataset");

                    promise = cmdService.run(AddSliceToStack.class, true,
                            "stack", inDataset,
                            "slice", ratioDataset);
                    promiseContent = promise.get();
                    Dataset extentedStack = (Dataset) promiseContent.getOutput("extendedStack");
                                      
                    ioService.save(extentedStack, resultDir_STACKS_extendedStacks + "/" + extentedStack.getName());
                }
                
                File resultDir_STACKS_extendedStacks_rmBckgrd = new File(resultDir_STACKS.getPath() + "/extended stacks_removed background");
                resultDir_STACKS_extendedStacks_rmBckgrd.mkdir();
                
                promise = cmdService.run(GetBackgroundCorrectedImages.class, true,
                        "stackDir", resultDir_STACKS_extendedStacks,
                        "cellDir", segResultDirPath,
                        "saveDir", resultDir_STACKS_extendedStacks_rmBckgrd);
                promise.get();
                
                correctedDir = resultDir_STACKS_extendedStacks_rmBckgrd;
                
                long t1 = System.nanoTime();
                long sec = TimeUnit.NANOSECONDS.toSeconds(t1 - t0);
                System.out.println(String.format("Process DONE: %d s", sec));
                
            } catch (InterruptedException ex) {
                Logger.getLogger(ProcessStep0.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ExecutionException ex) {
                Logger.getLogger(ProcessStep0.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(ProcessStep0.class.getName()).log(Level.SEVERE, null, ex);
            }         
        }
        else{
            
        }
        
    }
    
}