/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zmbh.commands.unused;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author User
 */


@Plugin(type = Command.class)
public class SortSingleImage implements Command {

    @Parameter(type = ItemIO.INPUT)
    File recordFile;
    
    @Parameter(type = ItemIO.INPUT)
    String inputDir;
    
    @Parameter(type = ItemIO.INPUT)
    String outDir;
    
    
    @Override
    public void run() {
        Reader in;     
        try {
            in = new FileReader(recordFile.getPath());
            /*
            CSVFormat csvFileFormat = CSVFormat.DEFAULT.withHeader("index","Label", "Area", "Mean", "StdDev", "Mode",
                    "Min", "Max", "X", "Y", "XM" ,"YM", "Perim.", "BX", "BY", "Width", "Height", "Major",
                    "Minor", "Angle", "Circ.", "Feret", "IntDen", "Median", "Skew", "Kurt", "X.Area", "RawIntDen", "Slice", "FeretX",
                    "FeretY", "FeretAngle", "MinFeret", "AR", "Round", "Solidity", "MinThr", "MaxThr",
                    "Well", "WellNumber", "WellLabel");
            */
            CSVFormat csvFileFormat = CSVFormat.DEFAULT.withHeader();
            
            CSVParser csvFileParser = new CSVParser(in, csvFileFormat);
            //Iterable<CSVRecord> records = CSVFormat.EXCEL.parse(in);
            List<CSVRecord> records = csvFileParser.getRecords();
            List<CSVRecord> toMoveList = new ArrayList<>();
            
            for(CSVRecord record : records){
                String fileName = record.get("Label").split(":")[0] + "_idCell_" + record.get("index") + ".tif";
                Path sourcePath = Paths.get(inputDir + "/" + fileName);
                File toFile = sourcePath.toFile();
                if(toFile.exists()){
                
                    if("true".equals(record.get("good"))){
                        Files.copy(sourcePath, Paths.get(outDir + "/" + "good" + "/" + fileName), REPLACE_EXISTING);
                        
                    }
                    else if("true".equals(record.get("dead"))){
                        Files.copy(sourcePath, Paths.get(outDir + "/" + "dead" + "/" + fileName), REPLACE_EXISTING);

                    }
                    else if("true".equals(record.get("outfocus"))){
                        Files.copy(sourcePath, Paths.get(outDir + "/" + "outfocus" + "/" + fileName), REPLACE_EXISTING);

                    }
                
                }
                
            }
            
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SortSingleImage.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SortSingleImage.class.getName()).log(Level.SEVERE, null, ex);
        }
            
    }
    
}