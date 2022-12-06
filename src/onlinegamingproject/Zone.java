package onlinegamingproject;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Amira BENAMER
 */
public class Zone 
{
    double x,y,cellSize,step;
    List<Integer>sets;
    List<Integer>servers;

    public Zone(double x, double y,double cellSize,double step) {
        this.x = x;
        this.y = y;
        this.step=step;
        this.cellSize=cellSize;
        this.sets = new ArrayList<>();
        this.servers = new ArrayList<>();
    }
    
}
