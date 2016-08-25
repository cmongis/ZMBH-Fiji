/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zmbh.commands.validation;

import zmbh.commands.segmentation.LoadCellXseedList;
import zmbh.commands.segmentation.CellXseed;
import zmbh.commands.segmentation.CellRecord;
import ij.ImagePlus;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.plugin.Animator;
import ij.plugin.Zoom;
import ij.plugin.frame.RoiManager;
import io.scif.services.DatasetIOService;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.imagej.Dataset;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.command.CommandModule;
import org.scijava.command.CommandService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;
import zmbh.commands.ImageJ1PluginAdapter;
import zmbh.commands.MyContrastAjuster;
import zmbh.commands.roi.ConvertPixelIndexToPoint;
import zmbh.commands.roi.jarvis.ConvexHull;
import zmbh.commands.roi.jarvis.JarvisMarcher;
import zmbh.commands.roi.jarvis.Model;
import zmbh.commands.segmentation.LoadCellRecordList;

/**
 *
 * @author User
 */

@Plugin(type = Command.class)
public class HumanEyeValidation2 implements Command {
    
    @Parameter
    CommandService commandService;
    
    @Parameter
    DatasetIOService ioService;
    
    @Parameter
    UIService uiService;
    
    @Parameter(type = ItemIO.INPUT)
    File recordFile;
    
    @Parameter(type = ItemIO.INPUT)
    String stackDirPath;
    
    @Parameter(type = ItemIO.INPUT)
    String maskDirPath;
    
    @Parameter(type = ItemIO.INPUT, required = false)
    String eyeValidationOutDir;
    
    @Override
    public void run() {
        
        try {
            /*
            Reader in = new FileReader(recordFile.getPath());
            /*
            CSVFormat csvFileFormat = CSVFormat.DEFAULT.withHeader("index","Label", "Area", "Mean", "StdDev", "Mode",
                    "Min", "Max", "X", "Y", "XM" ,"YM", "Perim.", "BX", "BY", "Width", "Height", "Major",
                    "Minor", "Angle", "Circ.", "Feret", "IntDen", "Median", "Skew", "Kurt", "X.Area", "RawIntDen", "Slice", "FeretX",
                    "FeretY", "FeretAngle", "MinFeret", "AR", "Round", "Solidity", "MinThr", "MaxThr",
                    "Well", "WellNumber", "WellLabel");
            */
            /*
            CSVFormat csvFileFormat = CSVFormat.DEFAULT.withHeader();
            CSVParser csvFileParser = new CSVParser(in, csvFileFormat);
            List<CSVRecord> records = csvFileParser.getRecords();
            records.remove(0);
            */
            List<CellRecord> cellRecordList  = new ArrayList<>();
            ArrayList availableRecords = new ArrayList();
            /*
            for (CSVRecord record : records) {
                int index = Integer.parseInt(record.get("index"));
                float X = Float.parseFloat(record.get("X"));
                float Y = Float.parseFloat(record.get("Y"));
                String label = record.get("Label");
                cellRecordList.add(new CellRecord(index, X, Y, label));
                availableRecords.add(availableRecords.size());
            }
            */
            Random random = new Random();
            Future<CommandModule> promise;
            CommandModule promiseContent;
            
            promise = commandService.run(LoadCellRecordList.class, true, "recordClassFile", recordFile);
            promiseContent = promise.get();
            cellRecordList = (List<CellRecord>) promiseContent.getOutput("cellRecordList");
            for(CellRecord record : cellRecordList){
                availableRecords.add(availableRecords.size());
            }
            
            for(int i = 0; i < cellRecordList.size(); i++){
            
                int randInt = random.nextInt(availableRecords.size());
                CellRecord record = cellRecordList.get((int) availableRecords.get(randInt));
                availableRecords.remove(randInt);
                
            
                String[] split = record.getLabel().split(":");
                String imageFile = split[0];
                int sliceNumber = Integer.parseInt(split[1]);
                
                String maskFileName = imageFile.split("\\.")[0] + ".mat";
                System.out.println(record.getLabel());
                System.out.println(maskFileName);
                File cellFile = new File(maskDirPath + "/" + maskFileName);
                
                promise = commandService.run(LoadCellXseedList.class, false, "cellFile", cellFile);
                promiseContent = promise.get();
                ArrayList<CellXseed> cellxSeedList = (ArrayList<CellXseed>) promiseContent.getOutput("cellxSeedList");
                
                Dataset dataset = ioService.open(stackDirPath + "/" + imageFile);
                ImagePlus inDatasetImp = ImageJ1PluginAdapter.unwrapDataset(dataset);
                
                for(CellXseed cellxSeed : cellxSeedList){
                    long startTime = System.nanoTime();
                    
                    
                    promise = commandService.run(ConvertPixelIndexToPoint.class, false, "imgHeigth", (int) dataset.dimension(1), "perimeterPixelListIndex", cellxSeed.getPerimeterPixelListIndex());
                    promiseContent = promise.get();
                    List<Point> pointArray = (List<Point>) promiseContent.getOutput("pointArray");

                    /*
                    // Graham scan
                    //promise = cmdService.run(ComputeConvexHullRoi.class, false, "imgHeigth", (int) inDataset.dimension(1), "perimeterPixelListIndex", cellxSeed.getPerimeterPixelListIndex());
                    promise = commandService.run(ComputeConvexHullRoi.class, false, "pointArray", pointArray);
                    //promise = commandService.run(ComputeConvexHullRoi.class, false, "imgHeigth", (int) dataset.dimension(1), "perimeterPixelListIndex", cellxSeed.getPerimeterPixelListIndex());
                    promiseContent = promise.get();
                    Roi roi = (Roi) promiseContent.getOutput("roi");
                    */
                    
                    // Jarvis
                    Model jarvisModel = new Model();
                    for(Point awtPoint:pointArray){
                        jarvisModel.addPoint(new zmbh.commands.roi.jarvis.Point(awtPoint.x, awtPoint.y));
                    }
                    JarvisMarcher jarvisMarcher = new JarvisMarcher(jarvisModel);
                    jarvisMarcher.solve();
                    ConvexHull hull = jarvisModel.getHull();
                    List<zmbh.commands.roi.jarvis.Point> points = hull.getPoints();
                    int[] xPoints = new int[points.size()];
                    int[] yPoints = new int[points.size()];
                    int nPoints = points.size();
                    for(int k = 0; k < points.size(); k++){
                        xPoints[k] = points.get(k).x;
                        yPoints[k] = points.get(k).y;
                    }

                    Polygon p = new Polygon(xPoints, yPoints, nPoints);
                    PolygonRoi roi = new PolygonRoi(p, Roi.POLYGON);
                    
                    /*
                    // TSP
                    HamiltonianCycleHelper helper = new HamiltonianCycleHelper();
                    helper.init(pointArray);
                    Roi roi = helper.run();
                    */
                    
                    // custom algo
                    //Roi roi = computeRoi((int) dataset.dimension(1), cellxSeed.getPerimeterPixelListIndex());
                    
                    long endTime = System.nanoTime();
                    
                    long duration = (endTime - startTime);
                    if(roi.contains((int) record.getX(), (int)record.getY())){
                        System.out.println(duration + "ns");
                        inDatasetImp.setRoi(roi);
                        break;
                    }
                    
                }
                
                uiService.show(dataset);
                Animator animator = new Animator();
                for(int j = 1; j < 7; j ++){
                    animator.run("next");
                }

                RoiManager roiManager =  RoiManager.getInstance();
                if (roiManager == null)
                    roiManager = new RoiManager();

                roiManager.addRoi(inDatasetImp.getRoi());
                roiManager.select(0);
                      
                Zoom zoom = new Zoom();
                zoom.run("to");
                zoom.run("out");
                zoom.run("out");
                zoom.run("out");
                zoom.run("out");

                MyContrastAjuster ca = new MyContrastAjuster();
                ca.run("");
                for(ActionListener a: ca.resetB.getActionListeners()) {
                    a.actionPerformed(new ActionEvent((Object) ca.resetB, ActionEvent.ACTION_PERFORMED, ""));
                }
                ca.done = true;
                ca.close();
                
                /*
                promise = commandService.run(HumanEyeValidationWindow.class, true);
                promiseContent = promise.get();
                boolean dead = (boolean) promiseContent.getOutput("outdead");
                boolean overlap = (boolean) promiseContent.getOutput("outoverlap");
                boolean outfocus = (boolean) promiseContent.getOutput("outoutfocus");
                boolean good = (boolean) promiseContent.getOutput("outgood");
                boolean stop = (boolean) promiseContent.getOutput("outstop");
                record.setDead(dead);
                record.setOverlap(overlap);
                record.setOutfocus(outfocus);
                record.setGood(good);
                record.setValidated(true);
                 */
                int stop = System.in.read();
                
                roiManager.deselect(inDatasetImp.getRoi());
                roiManager.removeAll();

                System.out.println(i);
                ij.WindowManager.closeAllWindows();
                
                if(stop == 1){
                    break;
                }
            }
            /*
            Writer writer = new FileWriter(eyeValidationOutDir + "/" + recordFile.getName() + "_validated.csv");
            
            CSVFormat format = CSVFormat.DEFAULT.withHeader("index", "X", "Y", "label", "dead", "overlap", "outfocus", "good");
            CSVPrinter csvFilePrinter = new CSVPrinter(writer, format);
            
            for(MyCellRecord record:cellRecordList){
                if(record.isValidated()){
                    List dataRecord = new ArrayList();
                    dataRecord.add(String.valueOf(record.getIndex()));
                    dataRecord.add(String.valueOf(record.getX()));
                    dataRecord.add(String.valueOf(record.getY()));
                    dataRecord.add(record.getLabel());
                    dataRecord.add(String.valueOf(record.isDead()));
                    dataRecord.add(String.valueOf(record.isOverlap()));
                    dataRecord.add(String.valueOf(record.isOutfocus()));
                    dataRecord.add(String.valueOf(record.isGood()));
                    csvFilePrinter.printRecord(dataRecord);
                }
                
            }

            
            writer.flush();
            writer.close();
            csvFilePrinter.close();
            */
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(HumanEyeValidation.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(HumanEyeValidation.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(HumanEyeValidation.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    private Roi computeRoi(int imgHeigth, double[] perimeterPixelListIndex){
        
        
        ArrayList<Point> pointArray = new ArrayList<>();

        for(int i = 0; i < perimeterPixelListIndex.length; i++){
            pointArray.add(new Point((int) (perimeterPixelListIndex[i] / imgHeigth), (int) (perimeterPixelListIndex[i] % imgHeigth)));
        }

        ArrayList<Point> sortedPointArray = new ArrayList<>();
        sortedPointArray.add(pointArray.remove(0));

        while(pointArray.size() > 0){
            Point currentPoint = sortedPointArray.get(sortedPointArray.size() - 1);
            Point nextPoint = null;
            ArrayList<Point> pixelArray = new ArrayList<>();
            for(int i = 0; i < pointArray.size(); i++){
                double dist = currentPoint.distance(pointArray.get(i));
                if(nextPoint == null){         
                    if(pointArray.size() == 1){
                        if(sortedPointArray.get(0).distance(pointArray.get(0)) < sortedPointArray.get(0).distance(sortedPointArray.get(sortedPointArray.size() - 1))){
                            nextPoint = pointArray.get(0);
                        }
                        else{
                            pixelArray.add(pointArray.get(0));
                        }
                    }
                    else{
                        nextPoint = pointArray.get(i);
                    }
                    
                    //pixelArray.add(nextPoint);
                }
                else if( dist < currentPoint.distance(nextPoint)){
                    pixelArray.clear();
                    nextPoint = pointArray.get(i);
                    //pixelArray.add(nextPoint);
                }
                else if(dist == currentPoint.distance(nextPoint)){
                    pixelArray.add(pointArray.get(i));
                }
            }
            if(nextPoint != null){
                sortedPointArray.add(pointArray.remove(pointArray.indexOf(nextPoint)));
            }
            
            pointArray.removeAll(pixelArray);
        }

        
        int[] xPoints = new int[sortedPointArray.size()];
        int[] yPoints = new int[sortedPointArray.size()];
        int nPoints = sortedPointArray.size();
        for(int i = 0; i < sortedPointArray.size(); i++){
            xPoints[i] = sortedPointArray.get(i).x;
            yPoints[i] = sortedPointArray.get(i).y;
        }

        Polygon p = new Polygon(xPoints, yPoints, nPoints);
        PolygonRoi roi = new PolygonRoi(p, Roi.POLYGON);
        return roi;
    }
}
