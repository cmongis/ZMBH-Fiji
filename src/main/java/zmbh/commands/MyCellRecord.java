/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zmbh.commands;


/**
 *
 * @author User
 */

public class MyCellRecord {
    
    private int index;   
    private float X;   
    private float Y;  
    private String label;  
    private boolean dead;  
    private boolean overlap;   
    private boolean outfocus;   
    private boolean good;
    
    private boolean validated;
    
    public MyCellRecord(int index, float X, float Y, String label){
        this.index = index;
        this.X = X;
        this.Y = Y;
        this.label = label;
        validated = false;
    }
    
    
    public void setDead(boolean dead) {
        this.dead = dead;
    }

    public void setOverlap(boolean overlap) {
        this.overlap = overlap;
    }

    public void setOutfocus(boolean outfocus) {
        this.outfocus = outfocus;
    }

    public void setGood(boolean good) {
        this.good = good;
    }
    
    public void setValidated(boolean validated) {
        this.validated = validated;
    }

    public int getIndex() {
        return index;
    }

    public float getX() {
        return X;
    }

    public float getY() {
        return Y;
    }

    public String getLabel() {
        return label;
    }

    public boolean isDead() {
        return dead;
    }

    public boolean isOverlap() {
        return overlap;
    }

    public boolean isOutfocus() {
        return outfocus;
    }

    public boolean isGood() {
        return good;
    }
    public boolean isValidated() {
        return validated;
    }
    
    

    
    
    
}
