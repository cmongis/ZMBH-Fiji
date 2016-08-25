/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zmbh.commands.unused;

import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.filter.Analyzer;
import io.scif.services.DatasetIOService;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.Format;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.imagej.Dataset;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.scijava.Context;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.command.CommandModule;
import org.scijava.command.CommandService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import zmbh.commands.segmentation.CellXseed;
import zmbh.commands.segmentation.LoadCellXseedList;

/**
 *
 * @author Potier Guillaume, 2016
 */

@Plugin(type = Command.class)
public class RunMeasurementsV3 implements Command {
    
    @Parameter
    CommandService cmdService;
    
    @Parameter
    DatasetIOService ioService;
    
    @Parameter(type = ItemIO.INPUT)
    File inDatasetFile;
    
    @Parameter(type = ItemIO.INPUT)
    File cellFile;
    
    @Parameter(type = ItemIO.INPUT)
    File saveDir;
    
    @Override
    public void run() {
        Date start = new Date();

        Future<CommandModule> promise;
        CommandModule promiseContent;
        try {
            Dataset inDataset = ioService.open(inDatasetFile.getPath());
            promise = cmdService.run(LoadCellXseedList.class, false, "cellFile", cellFile);
            promiseContent = promise.get();
            ArrayList<CellXseed> cellxSeedList = (ArrayList<CellXseed>) promiseContent.getOutput("cellxSeedList");
            
            Analyzer analyzer = new Analyzer();
            Analyzer.setMeasurement(Measurements.AREA, true);
            Analyzer.setMeasurement(Measurements.MEAN, true);
            Analyzer.setMeasurement(Measurements.STD_DEV, true);
            Analyzer.setMeasurement(Measurements.MODE, true);
            Analyzer.setMeasurement(Measurements.MIN_MAX, true);
            Analyzer.setMeasurement(Measurements.CENTROID, true);
            Analyzer.setMeasurement(Measurements.CENTER_OF_MASS, true);
            Analyzer.setMeasurement(Measurements.PERIMETER, true);
            Analyzer.setMeasurement(Measurements.LIMIT, true);
            Analyzer.setMeasurement(Measurements.RECT, true);
            Analyzer.setMeasurement(Measurements.LABELS, true);
            Analyzer.setMeasurement(Measurements.ELLIPSE, true);
            Analyzer.setMeasurement(Measurements.CIRCULARITY, true);
            Analyzer.setMeasurement(Measurements.SHAPE_DESCRIPTORS, true);
            Analyzer.setMeasurement(Measurements.FERET, true);
            Analyzer.setMeasurement(Measurements.INTEGRATED_DENSITY, true);
            Analyzer.setMeasurement(Measurements.MEDIAN, true);
            Analyzer.setMeasurement(Measurements.SKEWNESS, true);
            Analyzer.setMeasurement(Measurements.KURTOSIS, true);
            Analyzer.setMeasurement(Measurements.AREA_FRACTION, true);
            Analyzer.setMeasurement(Measurements.SLICE, true);
            Analyzer.setMeasurement(Measurements.STACK_POSITION, true);
            
            ArrayList<ArrayList<String>> results = new ArrayList<>();
            for(int sliceNumber = 0; sliceNumber < inDataset.dimension(2); sliceNumber++){
                results.add(new ArrayList<>());
            }
            
            
            for(CellXseed cellxSeed : cellxSeedList){
                promise = cmdService.run(MakeMeasurementsV3.class, true, "inDataset", inDataset, "cellxSeed", cellxSeed, "analyzer", analyzer);
                promiseContent = promise.get();
                ResultsTable resultsTable = (ResultsTable) promiseContent.getOutput("res");
                for(int i = 0; i < resultsTable.size(); i++){
                    results.get(i).add(resultsTable.getRowAsString(i));
                }

            }
         
            
            for(int i = 0; i < results.size(); i++){               
                Writer writer = new FileWriter(saveDir.getPath() + "/" + inDataset.getName() + "_slice" + i + ".csv");
                CSVFormat format = CSVFormat.DEFAULT.withHeader(Analyzer.getResultsTable().getHeadings());
                CSVPrinter csvFilePrinter = new CSVPrinter(writer, format);
                for(String records : results.get(i)){
                    String[] split = records.split("\\t");
                    /*
                    List dataRecord = new ArrayList();
                    for(String record : split){
                        dataRecord.add(record);
                    }*/
                    csvFilePrinter.printRecord(split);
                }
                //csvFilePrinter.printRecords(results.get(i));
                writer.flush();
                writer.close();
                csvFilePrinter.close();
            }
            
            
            
            
            
            Date stop = new Date();
            
            System.out.println("RunMeasurementsV3 exec time : " + (stop.getTime() - start.getTime())/1000 + "s");
            System.out.println("RunMeasurementsV3 blobs processed : " + cellxSeedList.size());

        } catch (IOException | InterruptedException | ExecutionException ex) {
            Logger.getLogger(RunMeasurementsV3.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}