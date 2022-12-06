/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package onlinegamingproject;

import java.util.List;


/**
 *
 * @author Amira BENAMER
 */
public class Input_Data
{
    //static
    public int sets;
    public int nbPlayersSet;
    public int edges;
    public int[][] C;
    public double W[][][];
    public double [] CDH;
    public double [][][]Dn;
    public int[][]Dn_eligibility;
    public double [][] Dn_closeness_all;
    public int VMS[];
    public double BW[][];
    public double[][] edgesLocation_all;
    public double[][][] playersLocation_all;
    public int xMin_all, yMin_all,yMax_all,xMax_all;
    public List<Integer>sol_all;
    public Zone zones_all[];
    
    
    public int radius,org;
    
    
    //dynamic
    public int sets_mig;
    public int edges_mig;
    public int[][] C_mig;
    public double W_mig[][][];
    public double [] CDH_mig;
    public double [][][]Dn_mig;
    public int[][]Dn_eligibility_mig;
    public double [][] Dn_closeness_mig;
    public int VMS_mig[];
    public double cummulative_cost;
    public double cummulative_clientPaidFee;
    public double [][]BW_mig;
    public List<Integer>sol_mig;
    public double[][] edgesLocation_mig;
    public double[][][] playersLocation_mig;
    public int xMin_mig, yMin_mig,yMax_mig,xMax_mig;
    public Zone zones_mig[];
}
