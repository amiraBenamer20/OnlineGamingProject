package onlinegamingproject;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import Benchmarks.GroupingGeneticPenalty.GroupingGeneticAlgorithm;
import Benchmarks.NewGGAPenalty.GroupingPenalty;
import GeneticAlgorithmPenalty.GeneticPenalty_Static;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author Amira BENAMER
 */
public class Data_Generation
{

    static String str,cross;
    //Set of sets 
    static int sets ; //Number of multiplayer groups 
    static int nbOfplayersPerSet = 20; //Number of players within each group

    static int smallS = 20;static int addedR=0; //presents the weight of players within the group
    static int mediumS = 0;
    static int largeS = 0;
    static int xtraLargeS = 0;
    //---------------  
    static int edges; //Number of Fog nodes
    static int smallE = 0; //presents the weight of resources within the group
    static int mediumE = 0; static int addedC;
    static int largeE = 0;
    static int xtraLargeE = 0;
    //--------------
    //Dmax in ms
    static int threshold;
    static int Dmax = 20;
    //---------------
    static double phi=0.8;
    static double gamma;
    static Map<Double,Double>alphaRange=new LinkedHashMap<Double,Double>();
    static double alpha=5;
    static int beta=1;
    static double sigma;
    static double radiusTemporal;//ms
    static double tick=0.02;//s
    
    
    static int rangeX;//km
    static int rangeY;//km
    static int radius;//km
    static double windowD;//km

    static double densityGroups,densityServers,densityPlayers;
    static int dim=1;
    static int[][] edgeCap = {{1000},{5000}};
    static double[] cost = {0.1};//, 0.3, 0.5, 0.8};// $/h

    static double[][] serviceGameReq = {{0.25}, {0.5}, {1}};
    
    //**dynamic
    static int[]vmSizes={512,1024,2048};
   
    
    //zoning
    static Zone[] zones;
    
    //structure de données
    static double[][] EdgesLocation = null;
    static double W[][][] =null;//Resource Requirements
    static int C[][] =null;//CPU, Memory   +1
    static double CDH[] = null; // corresponding cost usage /hour   +1
    static double[][][] Dn = null;
    static int [][] Dn_constraint=null;
    static double [][] Dn_closeness=null;
    static double[][][] PlayersLocation = null;
    static int[][] Dn_eligibility=null;

    static int id;
    static int rerun;
    static String path;
    public Data_Generation(String path,int sets,int id,int threshold,String str,String cross,int rerun,int addC) throws IOException
    {
        this.path = path;
        this.sets=sets;
        this.id=id;
        this.str=str;
        this.cross=cross;
        this.threshold=threshold;
        this.addedC=addC;
        gamma=(double)(100-threshold)/100;
        sigma=(2.0/3);
        alpha=4.0;
        this.rerun=rerun;
            fillData(path+"DataSet\\"+sets+"\\data"+sets+"_"+id+"_"+edgeCap[addedC][0]+"_"+threshold+".txt"
            ,path+"DataSet\\"+sets+"\\dataOPT"+sets+"_"+id+"_"+threshold+"_"+edgeCap[addedC][0]+".txt");
    
   }

    
    static synchronized void fillData(String fileName1, String fileName2) throws FileNotFoundException, IOException {
        PrintWriter sortie = new PrintWriter(new FileWriter(new File(fileName1).getAbsolutePath()));
        PrintWriter sortieOPT = new PrintWriter(new FileWriter(fileName2));
        PrintWriter sortieP = new PrintWriter(new FileWriter(path+"CoordinatesVIS\\"+sets+"\\playersUP_"+sets+"_"+id+"_"+threshold+"_"+rerun+"_"+edgeCap[addedC][0]+"vcpu.txt"));
        PrintWriter sortieE = new PrintWriter(new FileWriter(path+"CoordinatesVIS\\"+sets+"\\edgesUP_"+sets+"_"+id+"_"+threshold+"_"+rerun+"_"+edgeCap[addedC][0]+"vcpu.txt"));
        
        PrintWriter sortiePP = new PrintWriter(new FileWriter(path+"CoordinatesSet\\"+sets+"\\players_"+sets+"_"+id+"_"+threshold+"_"+rerun+"_"+edgeCap[addedC][0]+"vcpu.txt"));
        PrintWriter sortieEE = new PrintWriter(new FileWriter(path+"CoordinatesSet\\"+sets+"\\edges_"+sets+"_"+id+"_"+threshold+"_"+rerun+"_"+edgeCap[addedC][0]+"vcpu.txt"));
    
        PrintWriter sortieInoutDS = new PrintWriter(new FileWriter(path+"Input\\densityServer"+edgeCap[addedC][0]+"vcpu_"+id+"_"+threshold+".txt",true));
        PrintWriter sortieInoutDG = new PrintWriter(new FileWriter(path+"Input\\densityGroup"+edgeCap[addedC][0]+"vcpu_"+id+"_"+threshold+".txt",true));
        PrintWriter sortieInoutDP = new PrintWriter(new FileWriter(path+"Input\\densityPlayer"+edgeCap[addedC][0]+"vcpu_"+id+"_"+threshold+".txt",true));
        PrintWriter sortieInoutSurface = new PrintWriter(new FileWriter(path+"Input\\Surface"+edgeCap[addedC][0]+"vcpu_"+id+"_"+threshold+".txt",true));
        PrintWriter sortieInoutServers = new PrintWriter(new FileWriter(path+"Input\\ServerCand"+edgeCap[addedC][0]+"vcpu_"+id+"_"+threshold+".txt",true));
        

        //see paper @Scaling in the space-time of the Internet
        alphaRange.put(25.0, 4.47);
        alphaRange.put(50.0, 3.12243439);
        alphaRange.put(100.0, 2.6574);
        alphaRange.put(200.0, 2.8744);
        alphaRange.put(500.0, 2.898);
        alphaRange.put(1400.0, 2.8428);
        alphaRange.put(24576.0, 2.7);


        W= new double[sets][nbOfplayersPerSet][dim];
        //CPU, Memory
        //generateGamingServiceWeight(W, players);
        generateServiceRequirmentSML(W, sets, smallS, mediumS, largeS, xtraLargeS);
  

        //**Edge's capacity**
        double Sw=((double)nbOfplayersPerSet*serviceGameReq[addedR][0])/tick;
            if(Sw<1000)
                Sw=1000;
            if(Sw>1000 && Sw<=2000)
                Sw=2000;
        Sw=Sw*sets;
        edges=(int)((phi*Sw)/edgeCap[addedC][0]);
        System.out.println("Number of basic servers is: "+edges);
        calculateDimensions( edges);
        
        initializeZoning();
        
        double [][]centroid=new double[sets][2];
        PlayersLocation = generatePlayersCoordinates(centroid,PlayersLocation, sets, nbOfplayersPerSet,radius);
        calculateMeanDistanceGroups(centroid, sets);
        
       
          //coordinates: EdgesLoaction(e,(x,y))
        EdgesLocation = generateEdgeCoordinates(EdgesLocation, edges);

        repair();
        System.out.println("---edges: "+edges);
        calculateMeanDistanceServers(EdgesLocation, edges);
        
        
        if(addedC==0)
        {
            smallE=edges;
            mediumE= 0;
        }
        if(addedC==1)
        {
            smallE=0;
            mediumE=edges;
        }
        if(addedC==2)
            largeE=edges;
        if(addedC==3)
            xtraLargeE=edges;
   
        
       C= new int[edges ][dim];//CPU, Memory   +1
       CDH = new double[edges ]; // corresponding cost usage /hour   +1
        //generateEdgeCapacities(C,CDH, edges);
        generateEdgeCapacitiesSML(C, CDH, smallE, mediumE, largeE, xtraLargeE);
        
        System.out.println("sets: "+sets+" edges: "+edges);
       
        
      
        
        Dn = claculateNetworkDelay(Dn, PlayersLocation, EdgesLocation, sets, nbOfplayersPerSet, edges);//distance: Dn(p,e)
        calculateDeadConstraintPlayers( Dn,W,C);    
        
        Dn_eligibility=new int[Dn_constraint.length][edges];
        checkElegibility(Dn_eligibility,Dn_closeness,Dn_constraint,threshold);
        
     //_________________Read & Write ___________________________
           //**Sets**
        sortie.print("sets=" + sets + ";");
        sortie.println();
        
        sortieOPT.print("sets=" + sets + ";");
        sortieOPT.println();
        //**Edges**
        sortie.print("edges=" + (edges ) + ";");//+1
        sortie.println();//+Cloud
        
        sortieOPT.print("edges=" + (edges ) + ";");//+1
        sortieOPT.println();//+Cloud
        //**Players**
        sortie.print("players=" + nbOfplayersPerSet + ";");
        sortie.println();
        
        sortieOPT.print("players=" + nbOfplayersPerSet + ";");
        sortieOPT.println();
        //**Dmax**
        sortie.print("Dmax=" + Dmax + ";");
        sortie.println();
        
        sortieOPT.print("Dmax=" + Dmax + ";");
        sortieOPT.println();
        //**Tick**
        sortie.print("tick=" + String.valueOf(tick) + ";");
        sortie.println();
        
        sortieOPT.print("tick=" + String.valueOf(tick) + ";");
        sortieOPT.println();
        
        //**print players coordinates**
        sortiePP.print("xyp=[");
        for (int s = 0; s < sets; s++) 
        {
            sortiePP.print("[");
            for (int p = 0; p < nbOfplayersPerSet; p++) 
            {
               sortiePP.print("[");
               sortieP.print(PlayersLocation[s][p][0]);
               sortiePP.print(PlayersLocation[s][p][0]+" ");
               sortieP.print(',');
               sortiePP.print(',');
               sortieP.print(PlayersLocation[s][p][1]);
               sortiePP.print(PlayersLocation[s][p][1]);
               sortieP.println();
               sortiePP.print("]");
            }
            sortiePP.print("],");
        }
        sortiePP.print("];");
        sortieP.flush();
        sortieP.close();
        
        sortiePP.flush();
        sortiePP.close();
        
        //**print edges coordinates
        sortieEE.print("xyE=[");
        for(int e=0;e<edges;e++)
        {
            sortieEE.print("[");
            sortieE.print(EdgesLocation[e][0]);
            sortieEE.print(EdgesLocation[e][0]+" ");
            sortieE.print(',');
            sortieE.print(EdgesLocation[e][1]);
            sortieEE.print(EdgesLocation[e][1]);
            sortieE.println();
            sortieEE.print("],");
        }
        sortieEE.print("];");
        sortieE.flush();
        sortieE.close();
        
        sortieEE.flush();
        sortieEE.close();
        //**Network Delay**
        sortie.print("Dn=");
        sortieOPT.print("Dn=");
        sortie.print('[');
        sortieOPT.print('[');
        for (int s = 0; s < sets; s++) {
            sortie.print('[');
            sortieOPT.print('[');
            for (int p = 0; p < nbOfplayersPerSet; p++) {
                sortie.print('[');
                sortieOPT.print('[');
                for (int e = 0; e < edges; e++) {
                    sortie.print(Dn[s][p][e] + " ");
                    sortieOPT.print(Dn[s][p][e] + " ");
                }
                //**cloud**
                sortie.print(']');
                sortieOPT.print(']');
                if (p + 1 < nbOfplayersPerSet) {
                    sortie.print(',');
                    sortieOPT.print(',');
                }
            }
            sortie.print(']');
            sortieOPT.print(']');
            if (s + 1 < sets) {
                sortie.print(',');
                sortieOPT.print(',');
            }
        }
        sortie.print(']');
        sortie.print(';');
        sortie.println();
        
        sortieOPT.print(']');
        sortieOPT.print(';');
        sortieOPT.println();
        

        
//        System.out.println("Dn= "+Arrays.deepToString(Dn));
        
        sortie.print("Dn_closeness=");
        sortie.print('[');
        for (int s = 0; s < sets; s++) {
            sortie.print('[');
            for (int e = 0; e < edges; e++) {
                sortie.print(Dn_closeness[s][e] + " ");
            }
            sortie.print(']');
            if (s + 1 < sets) {
                sortie.print(',');
            }
        }
        sortie.print(']');
        sortie.print(';');
        sortie.println();
        
//        System.out.println("Dn_closeness= "+Arrays.deepToString(Dn_closeness));
        
        sortie.print("Dn_eligibility=");
        sortieOPT.print("Dn_eligibility=");
        sortie.print('[');
        sortieOPT.print('[');
        for (int s = 0; s < sets; s++) {
            sortie.print('[');
            sortieOPT.print('[');
            for (int e = 0; e < edges; e++) {
                sortie.print(Dn_eligibility[s][e] + " ");
                sortieOPT.print(Dn_eligibility[s][e] + " ");
            }
            sortie.print(']');
            sortieOPT.print(']');
            if (s + 1 < sets) {
                sortie.print(',');
                sortieOPT.print(',');
            }
        }
        sortie.print(']');
        sortie.print(';');
        sortie.println();
        
        sortieOPT.print(']');
        sortieOPT.print(';');
        sortieOPT.println();
        
        

        //**Requirement weight**
        sortie.print("W=");
        sortieOPT.print("W=");
        sortie.print('[');
        sortieOPT.print('[');
        for (int s = 0; s < sets; s++) {
            sortie.print('[');
            sortieOPT.print('[');
            for (int p = 0; p < nbOfplayersPerSet; p++) {
                sortie.print('[');
                sortieOPT.print('[');
                for (int r = 0; r < dim; r++) {
                    sortie.print(W[s][p][r] + " ");
                    sortieOPT.print(W[s][p][r] + " ");
                }
                sortie.print(']');
                sortieOPT.print(']');
                if (p + 1 < nbOfplayersPerSet) {
                    sortie.print(',');
                    sortieOPT.print(',');
                }
            }
            sortie.print(']');
            sortieOPT.print(']');
            if (s + 1 < sets) {
                sortie.print(',');
                sortieOPT.print(',');
            }
        }
        sortie.print(']');
        sortie.print(';');
        sortie.println();
        
        sortieOPT.print(']');
        sortieOPT.print(';');
        sortieOPT.println();

        
        sortie.print("C=");
        sortieOPT.print("C=");
        sortie.print('[');
        sortieOPT.print('[');
        for (int e = 0; e < edges; e++) {
            sortie.print('[');
            sortieOPT.print('[');
            for (int r = 0; r < dim; r++) {
                sortie.print(C[e][r] + " ");
                sortieOPT.print(C[e][r] + " ");
            }
            sortie.print(']');
            sortieOPT.print(']');
            if (e + 1 < edges) {
                sortie.print(',');
                sortieOPT.print(',');
            }
        }
        //**Cloud**
//        int[] cloud = {10000, 256000}; //256GB
//        sortie.print(",[");
//        for (int j = 0; j < 2; j++) {
//            C[edges][j] = cloud[j];
//            sortie.print(cloud[j] + " ");
//        }
//        sortie.print(']');

        sortie.print(']');
        sortie.print(';');
        sortie.println();

        sortieOPT.print(']');
        sortieOPT.print(';');
        sortieOPT.println();

        //**Cost**
        sortie.print("CDH=");
        sortieOPT.print("CDH=");
        sortie.print('[');
        sortieOPT.print('[');
        for (int e = 0; e < edges; e++) {
            sortie.print(CDH[e] + " ");
            sortieOPT.print(CDH[e] + " ");
            if (e + 1 < edges) {
                sortie.print(',');
                sortieOPT.print(',');
            }
        }
        sortie.print(']');
        sortie.print(';');
        sortie.println();
        
        sortieOPT.print(']');
        sortieOPT.print(';');
        sortieOPT.println();
        
        
        
           //**dynamic
        double[][]BW=topology(edges);
        
        sortie.print("BW=");
        sortie.print('[');
        for (int e = 0; e < edges; e++) {
            sortie.print('[');
            for (int ed = 0; ed < edges; ed++) {
                sortie.print(BW[e][ed] + " ");
            }
            sortie.print(']');
            if (e + 1 < edges) {
                sortie.print(',');
            }
        }
        
        sortie.print(']');
        sortie.print(';');
        sortie.println();

        int []VMS=vmSize(sets);
        sortie.print("VMS=");
        sortie.print('[');
        for (int s = 0; s < sets; s++) {
            sortie.print(VMS[s] + " ");
            if (s + 1 < sets) {
                sortie.print(',');
            }
        }

        sortie.print(']');
        sortie.print(';');
        sortie.println();
      
        
        
        
        sortieInoutDG.print(densityGroups);
        sortieInoutDG.println();
        sortieInoutDG.flush();
        sortieInoutDG.close();
        
        sortieInoutDS.print(densityServers);
        sortieInoutDS.println();
        sortieInoutDS.flush();
        sortieInoutDS.close();
        
        sortieInoutDP.print(densityPlayers);
        sortieInoutDP.println();
        sortieInoutDP.flush();
        sortieInoutDP.close();
        
        sortieInoutServers.print(edges);
        sortieInoutServers.println();
        sortieInoutServers.flush();
        sortieInoutServers.close();
        
        sortieInoutSurface.print(rangeX);
        sortieInoutSurface.println();
        sortieInoutSurface.flush();
        sortieInoutSurface.close();
        
        

        //*****
        sortie.flush();
        sortie.close();
        
        sortieOPT.flush();
        sortieOPT.close();
  
       
        System.out.println("OPTIMIZATION "+rerun);
        System.out.println("----------------------Our Proposal---------------------");
        GeneticPenalty_Static gp=new GeneticPenalty_Static(path,sets, nbOfplayersPerSet, edges,tick,Dmax);
        gp.Genetic_pen(C, C, W, Dn,  CDH, Dn_eligibility,Dn_closeness,str,cross,id,threshold,rerun);
             
 
//         System.out.println("----------------------GGA penalty----------------------");
//         GroupingPenalty ggap=new  GroupingPenalty(path,sets, nbOfplayersPerSet, edges,tick,Dmax);
//         ggap.main(C, C, W, Dn,  CDH, Dn_eligibility,Dn_closeness,"GGA_Pen",id,threshold,rerun);
      
//        System.out.println("----------------------GGA Repair------------------------");
//        GroupingGeneticAlgorithm gga=new GroupingGeneticAlgorithm(path,sets, nbOfplayersPerSet,edges, C, C, W, Dn, Dmax, CDH, Dn_eligibility,tick);
//        try {
//            gga.GGA_Par(C, C, W, Dn,  CDH, Dn_eligibility,Dn_closeness,"GGA_Repair",id,threshold,rerun);
//
//        } catch (InterruptedException ex) {
//            Logger.getLogger(Data_Generation.class.getName()).log(Level.SEVERE, null, ex);
//        }
        
                    
            
//        System.out.println("----------------------Grey Wolf---------------------------");
//        GreyWolfOptimizer gw=new GreyWolfOptimizer(sets, nbOfplayersPerSet, edges,tick,Dmax);
//        gw.GW_optimize(C, C, W, Dn, CDH, Dn_eligibility, Dn_closeness, "GW",id,threshold,rerun);
        
    }
    

    static synchronized double[][][] generatePlayersCoordinates(double [][]centroid,double[][][] PlayersLocation, int sets, int nbOfplayersPerSet,int rayon) {
        PlayersLocation = new double[sets][nbOfplayersPerSet][2];
         
        for (int s=0;s<sets;s++) 
        {
            double uniform1=Math.random();
            double uniform2=Math.random();
            double xCenter = uniform1*rangeX;
            double yCenter = uniform2*rangeY;
            PlayersLocation[s][0][0] = xCenter;
            PlayersLocation[s][0][1] = yCenter;
//            System.out.println("Centroid: "+xCenter+" "+yCenter);
             for(Zone z:zones)
            {  
                    double x=z.x;
                    double y=z.y;
                    double zc=z.cellSize;
                    if((x<=xCenter  && xCenter  <=x+zc) && 
                            (y<=yCenter && yCenter<=y+zc))
                    {
//                        System.out.println("zone "+x+" "+y);
                        if(!z.sets.contains(s))
                             z.sets.add(s);
                    }
            }
           
            centroid[s][0]=xCenter;
            centroid[s][1]=yCenter;
            for (int p=1;p < nbOfplayersPerSet;p++) 
            {
              uniform1=Math.random();
              double r = rayon * Math.sqrt(uniform1);
              uniform2=Math.random();
              double theta = 2 * Math.PI*uniform2;
              
              double x = r * Math.cos(theta)+xCenter;
              double y = r * Math.sin(theta)+yCenter;
                      
              PlayersLocation[s][p][0] = x;
              PlayersLocation[s][p][1] = y;
            }
        }
        
//        System.out.println("PlayerLoc: "+Arrays.deepToString(PlayersLocation));
        double mean=(double)(nbOfplayersPerSet/(Math.pow(rayon, 2)*Math.PI));
        densityPlayers=mean;
        System.out.println("Mean density players: "+mean);
        return PlayersLocation;
    }
    
    static synchronized double calculateMeanDistanceGroups(double[][] centroidLocation, int sets)
    {
        double [] distance = new double[sets-1];
        double mean=0.0;
        for(int e=0;e<sets-1;e++)
        {
            double value =Math.round( Math.sqrt(Math.pow(centroidLocation[e+1][0] - centroidLocation[e][0], 2.00)
                            + Math.pow( centroidLocation[e+1][1] - centroidLocation[e][1], 2.00)));
            distance[e]=value;
            mean+=value;
        }
        mean=(mean/(sets));
        System.out.println("mean distance groups: "+mean);
        System.out.println(" density of groups: "+(sets/Math.pow(mean, 2)));
         System.out.println(" density of groups: "+(sets/Math.pow(rangeX, 2)));
        densityGroups=(sets/Math.pow(rangeX, 2));
        return mean;
    }

    static synchronized double[][] generateEdgeCoordinates(double[][] EdgesLocation, int edges) {
        EdgesLocation = new double[edges][2];
        int counter = 0;

        while (counter < edges) 
        {
            double x=rand();
            double y=rand();
            EdgesLocation[counter][0] = x;
            EdgesLocation[counter][1] = y;
            for(Zone z:zones)
            {
                double xx=z.x;
                double yy=z.y;
                double zc=z.cellSize;
                if((xx<=x && x<=xx+zc) && (yy<=y&& y<=yy+zc))
                {
                    z.servers.add(counter);
                    break;
                }
            }
            
            
            counter++;
        }

        
//        System.out.println("Edge Location:  "+Arrays.deepToString(EdgesLocation));
        
        return EdgesLocation;
    }
    
    static synchronized double rand()
    {
        Random rd=new Random();
        return Math.random()*rangeX;
    }
    
    static synchronized double calculateMeanDistanceServers(double[][] EdgesLocation, int edges)
    {
        double [] distance = new double[edges-1];
        double mean=0.0;
        for(int e=0;e<edges-1;e++)
        {
            double value =Math.round( Math.sqrt(Math.pow(EdgesLocation[e+1][0] - EdgesLocation[e][0], 2.00)
                            + Math.pow( EdgesLocation[e+1][1] - EdgesLocation[e][1], 2.00)));
            distance[e]=value;
            mean+=value;
        }
        mean=(mean/(edges));
       // System.out.println("mean distance servers: "+mean);
       // System.out.println(" density of servers: "+(edges/Math.pow(mean, 2)));
        densityServers=(edges/Math.pow(rangeX, 2));
        System.out.println(" densityServers: "+densityServers);
        return mean;
    }
    //**Calculate Distance between players and edge servers**

    static synchronized double[][][] claculateNetworkDelay(double[][][] Dn, double[][][] PlayersLocation, double[][] EdgesLocation, int sets, int nbOfplayersPerSet, int edges) {
        Dn = new double[sets][nbOfplayersPerSet][edges ];//+1
        for (int s = 0; s < sets; s++) {
//            System.out.println("set "+s);
            for (int p = 0; p < nbOfplayersPerSet; p++) {
//                 System.out.println("Player: ("+PlayersLocation[s][p][0]+","+PlayersLocation[s][p][1]+")"); 
                 
                for (int e = 0; e < edges; e++) 
                {
                    double distance =Math.round( Math.sqrt(Math.pow(PlayersLocation[s][p][0] - EdgesLocation[e][0], 2.00)
                            + Math.pow(PlayersLocation[s][p][1] - EdgesLocation[e][1], 2.00)));
                    for (Map.Entry<Double, Double> set : alphaRange.entrySet())
                        if(distance<=set.getKey())
                        {
                            alpha=set.getValue();break;
                        }
                    Dn[s][p][e]=alpha*Math.sqrt(distance);
//                    System.out.println("Server "+e+" Distance: "+distance+" alpha: "+alpha+" Dn: "+Dn[s][p][e]);
                }
            }
        }
        
               
        
        return Dn;
    }



    //**Generate edge resource capacities (small,Medium, Large)

    static synchronized void generateEdgeCapacitiesSML(int C[][], double CDH[], int smallE, int mediumE, int largeE, int xtraLargeE) {
        int edge = 0;
        int pos = 0;//CPU
        Random rn = new Random();
        System.out.println("smallE: "+smallE+ " mde: "+mediumE+" addedC "+addedC);
        System.out.println("C: "+C.length);
        System.out.println("edgeCap: "+edgeCap.length);
        
        for (int e = 0; e < smallE; e++) 
        {
            for(int r = 0; r < dim; r++)
                C[edge][r] =edgeCap[addedC][r];
            CDH[e] =cost[rn.nextInt(cost.length)];
            edge++;
        }
        for (int e = 0; e < mediumE; e++) {
            for(int r = 0; r < dim; r++)
                C[edge][r] = edgeCap[addedC][r];
            CDH[edge] = cost[rn.nextInt(cost.length)];
            edge++;
        }
        for (int e = 0; e < largeE; e++) {
            for(int r = 0; r < dim; r++)
                C[edge][r] = edgeCap[addedC][r];
            CDH[edge] =cost[rn.nextInt(cost.length)];
            edge++;
        }
        for (int e = 0; e < xtraLargeE; e++) {
            for(int r = 0; r < dim; r++)
                C[edge][r] = edgeCap[addedC][r];
            CDH[edge] = cost[rn.nextInt(cost.length)];
            edge++;
        }
    }

    //**Generate service requirements(small,Medium, Large)
    static synchronized void generateServiceRequirmentSML(double W[][][], int sets, int smallS, int mediumS, int largeS, int xtraLargeS) {
        int pos = 0;//CPU
        for (int set = 0; set < sets; set++) {
            int service = 0;
            for (int s = 0; s < smallS; s++) {
                for(int r = 0; r < dim; r++)
                    W[set][service][r] = serviceGameReq[addedR][pos];
                service++;
            }
            for (int e = 0; e < mediumS; e++) {
                for(int r = 0; r < dim; r++)
                    W[set][service][r] = serviceGameReq[addedR][pos];
                service++;
            }
            for (int e = 0; e < largeS; e++) {
                for(int r = 0; r < dim; r++)
                    W[set][service][r] = serviceGameReq[addedR][pos];
                service++;
            }
            for (int e = 0; e < xtraLargeS; e++) {
                for(int r = 0; r < dim; r++)
                    W[set][service][r] = serviceGameReq[addedR][pos];
                service++;
            }

        }
    }
    
    //generate topology
    static synchronized double[][] topology(int edges)
    {
        double [][]BW=new double[edges][edges];
        for(int e=0;e<edges;e++)
        {
            for(int ed=0;ed<edges;ed++)
            {
                BW[e][ed]=0;
            }
        }

        for(int e=0;e<edges;e++)
        {
            for(int ed=0;ed<edges;ed++)
            {
                if(e!=ed & BW[e][ed]==0)
                {
                    double distance =Math.round( Math.sqrt(Math.pow(EdgesLocation[ed][0] - EdgesLocation[e][0], 2.00)
                            + Math.pow(EdgesLocation[ed][1] - EdgesLocation[e][1], 2.00)));
                    for (Map.Entry<Double, Double> set : alphaRange.entrySet())
                        if(distance<=set.getKey())
                        {
                            alpha=set.getValue();break;
                        }
                    double latency=alpha*Math.sqrt(distance);
                    //int val=BW_[rand.nextInt(BW_.length)];
                    BW[e][ed]=(double)1/latency;
                    BW[ed][e]=(double)1/latency;
                }
            }
        }
        return BW;
    }
    
    static synchronized int[] vmSize(int sets)
    {
        Random rand=new Random();
        int[]vms=new int[sets];
        for(int s=0;s<sets;s++)
            vms[s]=vmSizes[rand.nextInt(vmSizes.length)];
        return vms;
    }

     static synchronized void calculateDeadConstraintPlayers( double[][][] Dn,double W[][][], int C[][]) 
    {
       Dn_closeness=new double[Dn.length][edges];
       Dn_constraint=new int[Dn.length][edges];
       for(int s=0;s<Dn_constraint.length;s++)
       {
           double pr=opitimisticProcessingD();
           for(int e=0;e<Dn_constraint[s].length;e++)
           {
               double closeness=0.0;
               int count=0;
               for(int p=0;p<Dn[s].length;p++)
               {
                   if(Dn[s][p][e]+pr>Dmax)
                   {
                       count++;
                       closeness+=((Dn[s][p][e]+pr)-Dmax);
                   }
               }
               Dn_closeness[s][e]=closeness;
               Dn_constraint[s][e]=count;
           }
       }
       
    }

    static synchronized void checkElegibility(int [][]Dn_eligibility,double [][]Dn_closeness,int[][] Dn_constraint, int threshold) 
    {
        int co=0;
        for(int s=0;s<Dn_constraint.length;s++)
        {
            int count=0;
            for(int e=0;e<Dn_constraint[s].length;e++)
            {
                if((int)(Dn_constraint[s][e]*100/nbOfplayersPerSet)<=threshold)
                {
                    
                    Dn_eligibility[s][e]=1;
                    Dn_closeness[s][e]=0;
                    count++;
                }
                else
                    Dn_eligibility[s][e]=0;
            }
            if(count==0) co++;
        }
        System.out.println("non eligible: "+co);
//        System.out.println(" "+Arrays.deepToString(Dn_eligibility));

    }
    
    static synchronized  void calculateDimensions(int edges)
    {
        double pr=opitimisticProcessingD();
        System.out.println("Optimistic pr: "+pr);
   
        System.out.println("gamma: "+gamma);
        double rtt=(Dmax-pr)/2;
        double p1=((double)(rtt)/Math.sqrt(gamma));
        radiusTemporal=(p1);//-pr);
        System.out.println("Dr: "+radiusTemporal);
        double radiusTemp=(radiusTemporal);//pr+   /2
        System.out.println("radius temp: "+radiusTemp);
        radius=(int)(Math.pow(radiusTemp, 2)/Math.pow(alpha, 2));
        rangeX=(int)(beta*radius*Math.sqrt(Math.PI*edges));
        rangeY=rangeX;

        System.out.println("radius: "+radius+" rangesX: "+rangeX+" rangeY: "+rangeY);     
        

        windowD=radius;
//        radiusTemp=(pr+rTemp);
//        System.out.println("2*radiusTemp: "+(radiusTemp));
//        windowD=(int)(Math.pow(radiusTemp, 2)/Math.pow(alpha, 2));
//        System.out.println("window: "+windowD);
        
    }
    
    static synchronized double opitimisticProcessingD()
    {
        int VMcpu=(int)((serviceGameReq[addedR][0]*nbOfplayersPerSet)/tick);
        if(VMcpu<1000)
            VMcpu=1000;
        if(VMcpu>1000 && VMcpu<2000 )
            VMcpu=2000;

        return (((double)(serviceGameReq[addedR][0]))/(VMcpu))*1000;
    }
    
    static synchronized void updateC(int addedServers)
    {
         edges=edges+1;
         C = new int[edges][dim];//CPU, Memory   +1
         CDH= new double[edges ];
         
         if(addedC==0)
            generateEdgeCapacitiesSML(C, CDH, (smallE+addedServers), mediumE, largeE, xtraLargeE);
         if(addedC==1)
            generateEdgeCapacitiesSML(C, CDH, smallE, (mediumE+addedServers), largeE, xtraLargeE);
         if(addedC==2)
            generateEdgeCapacitiesSML(C, CDH, smallE, mediumE, (largeE+addedServers), xtraLargeE);
         if(addedC==3)
            generateEdgeCapacitiesSML(C, CDH, smallE, mediumE, largeE, (xtraLargeE+addedServers));
         
    }
    
    static synchronized void updateCoos(double x,double y)
    {
        double edgeLocation_copy[][]=new double[EdgesLocation.length][2];
        for(int e=0;e<EdgesLocation.length;e++)
        {
            edgeLocation_copy[e][0]=EdgesLocation[e][0];
            edgeLocation_copy[e][1]=EdgesLocation[e][1];
        }
        
        EdgesLocation=new double[edges][2];
        for(int e=0;e<edgeLocation_copy.length;e++)
        { 
            EdgesLocation[e][0]=edgeLocation_copy[e][0];
            EdgesLocation[e][1]=edgeLocation_copy[e][1];
        }
//        for (Map.Entry<Double, Double> set : coordinates.entrySet())
//        {
            EdgesLocation[edgeLocation_copy.length][0]=x;
            EdgesLocation[edgeLocation_copy.length][1]=y;
//        }
    }
    
    static synchronized void check()
    {
        int added=0;
        for(int s=0;s<sets;s++)
        {
            while(!checkFeasible(Dn_eligibility, s) )
            {
                added+=1;
                //generate new Server
                double x=rand();
                double y=rand();
                //update
                updateC(added);
                System.out.println("edges: "+edges+" C.length: "+C.length+" = "+CDH.length);
//                System.out.println("C: "+Arrays.deepToString(C));
                updateCoos(x, y);
                System.out.println(" to be sure "+EdgesLocation.length);
                Dn = claculateNetworkDelay(Dn, PlayersLocation, EdgesLocation, sets, nbOfplayersPerSet, edges);
                calculateDeadConstraintPlayers( Dn,W,C);   
                checkElegibility(Dn_eligibility,Dn_closeness, Dn_constraint, threshold);
            }
        }
        System.out.println("Number of added servers to gurantee at list one feasible solution is: "+added);
    }
    
    static synchronized boolean checkFeasible(int[][]Dn_eligibility,int set)
    {
        for(int e=0;e<edges;e++)
        {
            if(Dn_eligibility[set][e]==1)
                return true;
        }
        return false;
    }
    
    static synchronized void cplex(int[][]Dn_eligibility)
    {
        for(int s=0;s<Dn_eligibility.length;s++)
        {
            int count=0;
            for(int e=0;e<Dn_eligibility[s].length;e++)
            {
                count++;
            }
            if(count ==0)
            {
                for(int e=0;e<Dn_eligibility[s].length;e++)
                    Dn_eligibility[s][e]=1;
            }
        }
    }
    
    static synchronized void initializeZoning()
    {
        double cellSize=radius;//windowD;
        double step=radius;
        double ymin;
        System.out.println("cellSize: "+cellSize);
                
        int box=(int)Math.ceil((double)(rangeX)/step);//cellsize

        if(box==0 || box==1)
        {
            cellSize=rangeX+radius; box=1;//consider if one server is situated at the borderline
        }
        System.out.println("cellsize: "+cellSize);
        zones=new Zone[(box+2)*(box+1)];
//        System.out.println("zones size: "+zones.length);
//        System.out.println("nbr of zones: "+(box)+" * "+(box));

        int co=0;

        for(int by=0;by<box+1;by++)
        {
            if(by==0)
                ymin=-radius;
            else
                ymin=0;
            double y=(by*step)+ymin;
            boolean bool=false;
//            System.out.println("boxY: "+by);
            for(int bx=0;bx<box+1;bx++)
            {
               
                if(bx==0 && !bool)
                {
                    bx=-radius;
                }
                if(!bool)
                {
                    double x=bx;
                    Zone z=new Zone(x,y,cellSize,step);
                    zones[co]=z;co++;
                    bool=true;
//                    System.out.println("x: "+x+" y: "+y);
                    bx=0;

                }
                double x=bx*step;
                Zone z=new Zone(x,y,cellSize,step);
//                 System.out.println("x: "+x+" y: "+y);
                zones[co]=z;co++;
            }
        }
     
    }
    static synchronized void repair ()
    { 
        System.out.println("zones: "+zones.length);
       for(int i=0;i<zones.length;i++)
        {
            Zone zone=zones[i];
//            System.out.println("Zone "+i);
            List<Integer>servers=zone.servers;
            List<Integer>sets=zone.sets;
//            System.out.println("server: "+servers.size()+" sets: "+sets.size());
            
            double rq, c;
            c=edgeCap[addedC][0];
            rq=serviceGameReq[addedR][0];
            int demanded=(int)((rq*nbOfplayersPerSet)/tick);
            if(demanded<1000)
                demanded=1000;
            if(demanded>1000 & demanded<2000)
                demanded=2000;
            demanded=demanded*sets.size();
//            System.out.println("zone "+i+" x,y "+zone.x+" "+zone.y);
//            System.out.println("sigma*c*servers.size(): "+sigma*c*servers.size());
//            System.out.println("demanded "+demanded);
           while((demanded)>sigma*c*servers.size())
            {
//                System.out.println("new server");
                //add new server
                double x,y;
                 if(zone.x!=0)
                    x = zone.x+Math.random()*zone.cellSize;
                    else
                    x = zone.x+Math.random()*(zone.x+zone.cellSize);
                  if(zone.y!=radius)
                    y = zone.y+Math.random()*zone.cellSize;
                    else
                    y = Math.random()*(zone.y+zone.cellSize);
                  
//                 System.out.println("server: "+x+" "+y);
                 zone.servers.add(edges);
                 edges++;
                 updateCoos(x, y);


            }
            
        }
       
       
    }
}
