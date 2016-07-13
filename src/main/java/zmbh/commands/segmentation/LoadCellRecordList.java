/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zmbh.commands.segmentation;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.command.CommandService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author User
 */

@Plugin(type = Command.class)
public class LoadCellRecordList implements Command {
    
    @Parameter
    CommandService cmdService;
    
    @Parameter(type = ItemIO.INPUT)
    File recordClassFile;
    
    @Parameter(type = ItemIO.OUTPUT)
    List<CellRecord> cellRecordList;
    
    @Override
    public void run() {
        try {
            
            Reader in = new FileReader(recordClassFile.getPath());
            CSVFormat csvFileFormat = CSVFormat.DEFAULT.withHeader();
            CSVParser csvFileParser = new CSVParser(in, csvFileFormat);
            List<CSVRecord> records = csvFileParser.getRecords();
            records.remove(0);
            cellRecordList  = new ArrayList<>();
            for (CSVRecord record : records) {
                int index = Integer.parseInt(record.get("index"));
                float X = Float.parseFloat(record.get("X"));
                float Y = Float.parseFloat(record.get("Y"));
                float boundingSquareWidth = Float.parseFloat(record.get("Width"));
                float boundingSquareHeight = Float.parseFloat(record.get("Height"));
                String label = record.get("Label");
                boolean isBlue = false;
                String blueLabel = record.get("bfp300_class");
                if(blueLabel.equals("Blue")){
                    isBlue = true;
                }
                cellRecordList.add(new CellRecord(index, X, Y, boundingSquareWidth, boundingSquareHeight, label, isBlue));
            }
        } catch (IOException ex) {
            Logger.getLogger(LoadCellRecordList.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
    }
    
}