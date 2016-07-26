/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zmbh.commands;

import io.scif.services.DatasetIOService;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.imagej.Dataset;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.command.CommandModule;
import org.scijava.command.CommandService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import zmbh.commands.annotation.RunAnnotationCommand_allDir;
import zmbh.commands.annotation.RunComposeImg;
import zmbh.commands.correction.ChromaCorrect;
import zmbh.commands.correction.GetFFImg;
import zmbh.commands.measure.GetRatioImage;
import zmbh.commands.measure.RunMeasurementsV4_allDir;
import zmbh.commands.util.AddSliceToStack;
import zmbh.config.Get4ImgStack;
import zmbh.config.LoadJSON2;

/**
 *
 * @author Potier Guillaume, 2016
 */

@Plugin(type = Command.class, menuPath = "Dev-commands>CMD Process", label="")
public class Process implements Command {
    
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
    String landMarkFilePath;
    
    @Parameter(type = ItemIO.INPUT)
    File rScript;
    
    @Parameter(type = ItemIO.INPUT)
    String rLibPath;
    
    @Parameter(type = ItemIO.INPUT)
    File jsonFile;
    
    @Override
    public void run() {
        long t0 = System.nanoTime();
        
        File rawStackDir = new File(rawStackDirPath);
        File segResultDir = new File(segResultDirPath);
        File resultDir = new File(resultDirPath);
        
        Future<CommandModule> promise;
        CommandModule promiseContent;
        
        if(rawStackDir.isDirectory() && segResultDir.isDirectory() && resultDir.isDirectory()){
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
                File[] stackFileList = resultDir_STACKS_correctedStacks.listFiles((File pathname) -> pathname.getName().endsWith(".tif"));
                for(File stack : stackFileList){
                    Dataset inStack = ioService.open(stack.getPath());
                    promise = cmdService.run(ChromaCorrect.class, true,
                            "stack", inStack,
                            "sourceSlice", 1,
                            "targetSlice", 0,
                            "landMarkFilePath", landMarkFilePath);
                    promiseContent = promise.get();                    
                    Dataset tmpStack = (Dataset) promiseContent.getOutput("outStack");
                    /*
                    promise = cmdService.run(ChromaCorrect.class, true,
                            "stack", tmpStack,
                            "sourceSlice", 3,
                            "targetSlice", 1,
                            "landMarkFilePath", landMarkFilePath);
                    promiseContent = promise.get();
                    tmpStack = (Dataset) promiseContent.getOutput("outStack");
                    */
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
                    
                    /*
                    promise = cmdService.run(GetRatioImage.class, true,
                            "stack", inDataset,
                            "sliceNum1", 1,
                            "sliceNum2", 3);
                    promiseContent = promise.get();
                    ratioDataset  = (Dataset) promiseContent.getOutput("ratioDataset");

                    promise = cmdService.run(AddSliceToStack.class, true,
                            "stack", extentedStack,
                            "slice", ratioDataset);
                    promiseContent = promise.get();
                    extentedStack = (Dataset) promiseContent.getOutput("extendedStack");
                    */
                    
                    ioService.save(extentedStack, resultDir_STACKS_extendedStacks + "/" + extentedStack.getName());
                }
                
                File resultDir_MEASURE = new File(resultDir.getPath() + "/1_MEASURE");
                resultDir_MEASURE.mkdir();
                
                //File resultDir_Measure_rawMeasurements = new File(resultDir_MEASURE.getPath() + "/raw measurements");
                //resultDir_Measure_rawMeasurements.mkdir();
                
                // Make measurements on all extended stacks
                promise = cmdService.run(RunMeasurementsV4_allDir.class, true,
                        "inputStackDir", resultDir_STACKS_extendedStacks,
                        "cellDir", segResultDir,
                        "saveDir", resultDir_MEASURE);
                promise.get();
                
                // Run R processing script               
                ProcessBuilder builder = new ProcessBuilder(
                        "Rscript",
                        rScript.getAbsolutePath(),
                        resultDir_MEASURE.getAbsolutePath(),
                        rLibPath);                
                builder.inheritIO();
                try {
                    java.lang.Process process = builder.start();
                    process.waitFor();
                } catch (IOException ex) {
                    Logger.getLogger(RunRScript.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                
                File resultDir_ANNOTATION = new File(resultDir.getPath() + "/2_ANNOTATION");
                resultDir_ANNOTATION.mkdir();
                
                File resultDir_ANNOTATION_annotatedStacks = new File(resultDir_ANNOTATION.getPath() + "/annotated stacks");
                resultDir_ANNOTATION_annotatedStacks.mkdir();
                
                 File resultDir_Measure_annotatedRecords = new File(resultDir_MEASURE.getPath() + "/annotatedRecords");
                
                // Run image annotation
                promise = cmdService.run(RunAnnotationCommand_allDir.class, true,
                        "inputStackDir", resultDir_STACKS_extendedStacks,
                        "annotedRecordDir", resultDir_Measure_annotatedRecords,
                        "cellDir", segResultDir,
                        "saveDir", resultDir_ANNOTATION_annotatedStacks);
                promise.get();
                
                
                File resultDir_ANNOTATION_compositeImg = new File(resultDir_ANNOTATION.getPath() + "/composite Images");
                resultDir_ANNOTATION_compositeImg.mkdir();
                
                // Run composite image generation
                promise = cmdService.run(RunComposeImg.class, true,
                        "stackDirPath", resultDir_STACKS_extendedStacks.getPath(),
                        "cellDirPath", segResultDir.getPath(),
                        "recordClassDirPath", resultDir_Measure_annotatedRecords.getPath(),
                        "sliceNumber", 0,
                        "isWell", true,
                        "saveDir", resultDir_ANNOTATION_compositeImg.getPath());
                promise.get();
                
                long t1 = System.nanoTime();
                long sec = TimeUnit.NANOSECONDS.toSeconds(t1 - t0);
                System.out.println(String.format("Process DONE: %d s", sec));
                
            } catch (InterruptedException ex) {
                Logger.getLogger(Process.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ExecutionException ex) {
                Logger.getLogger(Process.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(Process.class.getName()).log(Level.SEVERE, null, ex);
            }         
        }
        else{
            
        }
        
    }
    
}