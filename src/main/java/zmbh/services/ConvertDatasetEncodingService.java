package zmbh.services;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import net.imagej.Dataset;
import net.imagej.types.DataType;
import org.scijava.service.SciJavaService;

/**
 *
 * @author ruvia
 */

public interface ConvertDatasetEncodingService extends SciJavaService{
    Dataset convert(Dataset input, DataType targetEncoding);
}
