/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GeneticAlgorithmPenalty;

import GeneticAlgorithmPenalty_Dynamic.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.ArrayUtils;



/**
 *
 * @author Amira BENAMER
 */
 class Data
{
    double cost, pm;
    int migrated;
}

public class GeneticPenalty_Dynamic 
{
    public int sets; 
    public int nbOfPlayersPerSet;
    public int servers;
    public double tick;
    //-------------
    public int popSize;
    public int generations;
    public int tournamentSize;
    public int m;
    public double GmE=0.02; //probability of pop
    public double M = 120;//Migration Time Threshold (ms)
   
    
    //algorithmInput
    public int milestone,nrep,nmut;
    public double mut,rep,beta2;
    
    
    //algorithmOutput
    public Chromosome best;
    long executionFinal;
    public int bestgen;
    
    
    //result
    int [][]C_copy;
    static int [][]C_original;
    double[]latency_cdf_pdf;
    
    int C[][];
//    int C_all[][];
    int W[][][];
    double Dn[][][];
    int Dmax;
    double CDH[];
    int[][] Dn_eligibility ;
    double lamdaC=0.0,lamdaD=0.0,lamdaM, mult, cons=0.0;
    boolean pen1C,pen2C,pen1D,pen2D,pen1M,pen2M;
    int ctrC,ctrD,ctrM;
    double cummulative_cost,cummulative_clientPaidFee;

    int sumWP[];
    List<List<Integer>> eligibleServers;

    Population pop;
    
    
    /*Dynamic*/
    List<Integer>sol_mig;
    double[][]BW;
    int[]VMS;
    String scenario;
    String path;
    int cap;
    int initial_config;
    int t;
    String velocity;
    float variation;
    double groupsAcceptanceRate;
    private double paidFee = 2;
    public GeneticPenalty_Dynamic(int sets, int nbOfPlayersPerSet, int servers,double tick,int Dmax,String scenario, String path, int cap,int initial_config,int t, String velocity,float variation) {
        this.path = path;
        this.sets = sets;
        this.nbOfPlayersPerSet = nbOfPlayersPerSet;
        this.servers = servers;
        this.tick=tick;
        this.Dmax=Dmax;
        this.velocity=velocity;
        this.t = t;//at timestamp t
        this.variation = variation;
        try 
        {
            GetData(new File("inputParametersGA.txt").getAbsolutePath());//read input parameters
         } catch (IOException ex) {
            Logger.getLogger(GeneticPenalty_Dynamic.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.scenario = scenario;

        this.cap = cap;
        this.initial_config=initial_config;
    }
   public void Genetic_pen(int C[][], double W[][][],double Dn[][][],double CDH[],int[][] Dn_eligibility,double[][] Dn_closeness, String str,String cross,int id,int threshold,int rerun,List<Integer>sol_mig,double [][]BW,int[]VMS,double cummulative_cost,double cummulative_paidFee )
   {
       this.cummulative_cost = cummulative_cost;
       this.cummulative_clientPaidFee = cummulative_paidFee;
       System.out.println("--*-*-*-**-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*OPTIMIZATION-*-*-*-*-*-*-*-*-*-*-*-*-**-*-**-");
        if(scenario.equals("dynamic"))
        {
            this.sol_mig = sol_mig;
            this.BW = BW;
            this.VMS = VMS;
        }
       
       
       popSize=sets*m;
       sumWP=new int[sets];
       compute_groups_requirements(W);

        eligibleServers=new ArrayList<>();
        for(int i=0;i<sets;i++)
        {
            List<Integer>serversE=getEligibleServers(i, Dn_eligibility);
            eligibleServers.add(serversE);
        }
 
        pop=new Population(popSize,sets,C,W);
        nrep = (int)(rep*popSize); if (nrep == 0) nrep++;
        nmut = (int)(mut*popSize); if (nmut == 0) nmut++;
        

        System.out.println("CONFIGURATION: ");
        System.out.println("Number of servers: "+servers+" Number of sets: "+sets);
       System.out.println("Start");
       long startTime=System.nanoTime();
       Instant start=Instant.now();
       setup(C,Dn_eligibility,Dn,W,CDH,Dn_closeness,sol_mig,BW,VMS);   
       genetic(pop, Dn_eligibility,C,Dn,W,CDH,Dn_closeness,startTime,cross,id,threshold,rerun,sol_mig,BW,VMS,variation);
       Instant end=Instant.now();
       long endTime=System.nanoTime();
       long duration=(endTime-startTime);
       System.out.println("____________");
       Duration interval = Duration.between(start, end);
       executionFinal= interval.getSeconds();
       double exec= TimeUnit.NANOSECONDS.toSeconds(endTime - startTime);

       System.out.println("Execution time in seconds: " +
                               executionFinal);
     
       System.out.println("*****************************Solution******************************");
        best.sumWP=sumWP;
        best.C=C;
      //**Display & Save 

        best.howMuchPlacedServers();
        best.display();
        System.out.println(" Migration time cost: "+best.migrationCost);
  
        try {
            writeSolution(best, id,str,rerun,C,threshold,path,variation);
        } catch (IOException ex) {
            Logger.getLogger(GeneticPenalty_Dynamic.class.getName()).log(Level.SEVERE, null, ex);
        }

  //      best.displayViolations();
        
//        displayPlayerSets(best, W, C, Dn, Dmax,"");
     
        writeLatencyDistribution(best, id, str, threshold, rerun, W, C, Dn, Dmax,path,variation);
        try {
            writeSolutionOnOutputFiles(id, rerun,str,C,threshold,path,variation);
        } catch (IOException ex) {
            Logger.getLogger(GeneticPenalty_Dynamic.class.getName()).log(Level.SEVERE, null, ex);
        }
        
       
        
        try 
        {
            save_Output( best.migrated, initial_config, id, scenario,threshold, velocity, variation, cap);
        } 
        catch (IOException ex) 
        {
            Logger.getLogger(GeneticPenalty_Dynamic.class.getName()).log(Level.SEVERE, null, ex);
        }
   }
   
       void writeSolutionOnOutputFiles(int id,int rerun,String str,int C[][],int threshold,String path,float variation) throws IOException 
   {
       PrintWriter sortieQuality=null, sortieQualityT = null;
       PrintWriter sortieObj=null, sortieObjT = null;
       PrintWriter sortieExec=null, sortieExecT = null;
       PrintWriter sortieNbofUS=null,sortieNbofUST = null;
       PrintWriter sortieBestIter=null;
       PrintWriter sortieViolationDead=null,sortieViolationDeadT=null;
       PrintWriter sortieViolationCapr=null, sortieViolationCaprT=null;
       PrintWriter sortieViolationMig=null, sortieViolationMigT=null;
       PrintWriter sortiePlacedServers=null, sortiePlacedServersT=null;
       PrintWriter sortieMigration=null, sortieMigrationT=null;
       PrintWriter sortieSRate=null, sortieSRateT=null;
       PrintWriter sortieAllocationCost=null, sortieAllocationCostT=null;
          try
        {
            
            sortieQuality = new PrintWriter(new FileWriter(path+"/Quality/"+scenario+"_"+initial_config+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+velocity+"_"+variation+"_"+cap+"vcpu.txt",true));
            sortieObj = new PrintWriter(new FileWriter(path+"/Obj/"+scenario+"_"+initial_config+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+velocity+"_"+variation+"_"+cap+"vcpu.txt",true));
            sortieAllocationCost = new PrintWriter(new FileWriter(path+"/AllocationCost/"+scenario+"_"+initial_config+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+velocity+"_"+variation+"_"+cap+"vcpu.txt",true));
            sortieExec = new PrintWriter(new FileWriter(path+"/Exec/"+scenario+"_"+initial_config+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+velocity+"_"+variation+"_"+cap+"vcpu.txt",true));
            sortieNbofUS = new PrintWriter(new FileWriter(path+"/NumberOfUS/"+scenario+"_"+initial_config+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+velocity+"_"+variation+"_"+cap+"vcpu.txt",true));
            sortieBestIter = new PrintWriter(new FileWriter(path+"/BestIter/"+scenario+"_"+initial_config+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+variation+"_"+cap+"vcpu.txt",true));
            sortieViolationCapr = new PrintWriter(new FileWriter(path+"/ViolatedCap/"+scenario+"_"+initial_config+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+velocity+"_"+variation+"_"+cap+"vcpu.txt",true));
            sortieViolationDead = new PrintWriter(new FileWriter(path+"/ViolatedDead/"+scenario+"_"+initial_config+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+velocity+"_"+variation+"_"+cap+"vcpu.txt",true));
            //sortiePlacedServers = new PrintWriter(new FileWriter(path+"/ActiveServers/"+initial_config+"/"+scenario+"_"+initial_config+"_"+id+"_"+str+"_"+rerun+"_"+threshold+"_"+velocity+"_"+cap+"vcpu_"+t+".txt"));
            sortieViolationMig = new PrintWriter(new FileWriter(path+"/ViolatedMig/"+scenario+"_"+initial_config+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+velocity+"_"+variation+"_"+cap+"vcpu.txt",true));
            sortieMigration = new PrintWriter(new FileWriter(path+"/Migration/"+scenario+"_"+initial_config+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+velocity+"_"+variation+"_"+cap+"vcpu.txt",true));
            sortieSRate= new PrintWriter(new FileWriter( path+"/GroupsAcceptanceRate/"+scenario+"_"+initial_config+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+velocity+"_"+variation+"_"+cap+"vcpu.txt",true));
                
            
            sortieQualityT = new PrintWriter(new FileWriter(path+"/Quality/t"+t+"/"+scenario+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+velocity+"_"+variation+"_"+cap+"vcpu.txt",true));
            sortieObjT = new PrintWriter(new FileWriter(path+"/Obj/t"+t+"/"+scenario+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+velocity+"_"+variation+"_"+cap+"vcpu.txt",true));
            sortieAllocationCostT = new PrintWriter(new FileWriter(path+"/AllocationCost/t"+t+"/"+scenario+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+velocity+"_"+variation+"_"+cap+"vcpu.txt",true));
            sortieExecT = new PrintWriter(new FileWriter(path+"/Exec/t"+t+"/"+scenario+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+velocity+"_"+variation+"_"+cap+"vcpu.txt",true));
            sortieNbofUST = new PrintWriter(new FileWriter(path+"/NumberOfUS/t"+t+"/"+scenario+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+velocity+"_"+variation+"_"+cap+"vcpu.txt",true));
            sortieViolationCaprT = new PrintWriter(new FileWriter(path+"/ViolatedCap/t"+t+"/"+scenario+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+velocity+"_"+variation+"_"+cap+"vcpu.txt",true));
            sortieViolationDeadT = new PrintWriter(new FileWriter(path+"/ViolatedDead/t"+t+"/"+scenario+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+velocity+"_"+variation+"_"+cap+"vcpu.txt",true));
            sortiePlacedServersT = new PrintWriter(new FileWriter(path+"/ActiveServers/"+initial_config+"/t"+t+"/"+scenario+"_"+initial_config+"_"+id+"_"+str+"_"+rerun+"_"+threshold+"_"+velocity+"_"+variation+"_"+cap+"vcpu_"+t+".txt",true));
            sortieViolationMigT = new PrintWriter(new FileWriter(path+"/ViolatedMig/t"+t+"/"+scenario+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+velocity+"_"+variation+"_"+cap+"vcpu.txt",true));
            sortieMigrationT = new PrintWriter(new FileWriter(path+"/Migration/t"+t+"/"+scenario+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+velocity+"_"+variation+"_"+cap+"vcpu.txt",true));
            sortieSRateT= new PrintWriter(new FileWriter( path+"/GroupsAcceptanceRate/t"+t+"/"+scenario+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+velocity+"_"+variation+"_"+cap+"vcpu.txt",true));
  
        } 
          catch (IOException ex)
          {

            sortieQuality = new PrintWriter(new FileWriter(path+"/Quality/"+scenario+"_"+initial_config+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+velocity+"_"+variation+"_"+cap+"vcpu.txt"));
            sortieObj = new PrintWriter(new FileWriter(path+"/Obj/"+scenario+"_"+initial_config+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+velocity+"_"+variation+"_"+cap+"vcpu.txt"));
            sortieAllocationCost = new PrintWriter(new FileWriter(path+"/AllocationCost/"+scenario+"_"+initial_config+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+velocity+"_"+variation+"_"+cap+"vcpu.txt"));
            sortieExec = new PrintWriter(new FileWriter(path+"/Exec/"+scenario+"_"+initial_config+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+velocity+"_"+variation+"_"+cap+"vcpu.txt"));
            sortieNbofUS = new PrintWriter(new FileWriter(path+"/NumberOfUS/"+scenario+"_"+initial_config+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+velocity+"_"+variation+"_"+cap+"vcpu.txt"));
            sortieBestIter = new PrintWriter(new FileWriter(path+"/BestIter/"+scenario+"_"+initial_config+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+variation+"_"+cap+"vcpu.txt"));
            sortieViolationCapr = new PrintWriter(new FileWriter(path+"/ViolatedCap/"+scenario+"_"+initial_config+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+velocity+"_"+variation+"_"+cap+"vcpu.txt"));
            sortieViolationDead = new PrintWriter(new FileWriter(path+"/ViolatedDead/"+scenario+"_"+initial_config+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+velocity+"_"+variation+"_"+cap+"vcpu.txt"));
            //sortiePlacedServers = new PrintWriter(new FileWriter(path+"/ActiveServers/"+initial_config+"/"+scenario+"_"+initial_config+"_"+id+"_"+str+"_"+rerun+"_"+threshold+"_"+velocity+"_"+cap+"vcpu_"+t+".txt"));
            sortieViolationMig = new PrintWriter(new FileWriter(path+"/ViolatedMig/"+scenario+"_"+initial_config+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+velocity+"_"+variation+"_"+cap+"vcpu.txt"));
            sortieMigration = new PrintWriter(new FileWriter(path+"/Migration/"+scenario+"_"+initial_config+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+velocity+"_"+variation+"_"+cap+"vcpu.txt"));
            sortieSRate= new PrintWriter(new FileWriter( path+"/GroupsAcceptanceRate/"+scenario+"_"+initial_config+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+velocity+"_"+variation+"_"+cap+"vcpu.txt"));
                
            
            sortieQualityT = new PrintWriter(new FileWriter(path+"/Quality/t"+t+"/"+scenario+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+velocity+"_"+variation+"_"+cap+"vcpu.txt"));
            sortieObjT = new PrintWriter(new FileWriter(path+"/Obj/t"+t+"/"+scenario+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+velocity+"_"+variation+"_"+cap+"vcpu.txt"));
            sortieAllocationCostT = new PrintWriter(new FileWriter(path+"/AllocationCost/t"+t+"/"+scenario+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+velocity+"_"+variation+"_"+cap+"vcpu.txt"));
            sortieExecT = new PrintWriter(new FileWriter(path+"/Exec/t"+t+"/"+scenario+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+velocity+"_"+variation+"_"+cap+"vcpu.txt"));
            sortieNbofUST = new PrintWriter(new FileWriter(path+"/NumberOfUS/t"+t+"/"+scenario+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+velocity+"_"+variation+"_"+cap+"vcpu.txt"));
            sortieViolationCaprT = new PrintWriter(new FileWriter(path+"/ViolatedCap/t"+t+"/"+scenario+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+velocity+"_"+variation+"_"+cap+"vcpu.txt"));
            sortieViolationDeadT = new PrintWriter(new FileWriter(path+"/ViolatedDead/t"+t+"/"+scenario+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+velocity+"_"+variation+"_"+cap+"vcpu.txt"));
            sortiePlacedServersT = new PrintWriter(new FileWriter(path+"/ActiveServers/"+initial_config+"/t"+t+"/"+scenario+"_"+initial_config+"_"+id+"_"+str+"_"+rerun+"_"+threshold+"_"+velocity+"_"+variation+"_"+cap+"vcpu_"+t+".txt"));
            sortieViolationMigT = new PrintWriter(new FileWriter(path+"/ViolatedMig/t"+t+"/"+scenario+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+velocity+"_"+variation+"_"+cap+"vcpu.txt"));
            sortieMigrationT = new PrintWriter(new FileWriter(path+"/Migration/t"+t+"/"+scenario+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+velocity+"_"+variation+"_"+cap+"vcpu.txt"));
            sortieSRateT= new PrintWriter(new FileWriter( path+"/GroupsAcceptanceRate/t"+t+"/"+scenario+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+velocity+"_"+variation+"_"+cap+"vcpu.txt"));

        }

       
       sortieQuality.print(best.fitness);
       sortieQuality.println();
       sortieQuality.flush();
       sortieQuality.close();
       
       sortieAllocationCost.print(best.allocationCost);
       sortieAllocationCost.println();
       sortieAllocationCost.flush();
       sortieAllocationCost.close();
       
       sortieObj.print(best.objectiveFunc);
       sortieObj.println();
       sortieObj.flush();
       sortieObj.close();
       
       sortieNbofUS.print(best.activeServers);
       sortieNbofUS.println();
       sortieNbofUS.flush();
       sortieNbofUS.close();
               
       sortieBestIter.print(bestgen);
       sortieBestIter.println();
       sortieBestIter.flush();
       sortieBestIter.close();
       
       sortieExec.print(executionFinal);
       sortieExec.println();
       sortieExec.flush();
       sortieExec.close();
       
       sortieViolationCapr.print(best.penality_capacity);
       sortieViolationCapr.println();
        sortieViolationCapr.flush();
       sortieViolationCapr.close();
       
       sortieViolationDead.print(best.penality_latency);
       sortieViolationDead.println();
       sortieViolationDead.flush();
       sortieViolationDead.close();
       
       
       sortieMigration.print(best.migrationCost);
       sortieMigration.println();
       sortieMigration.flush();
       sortieMigration.close();
       
       sortieViolationMig.print(best.penalty_migration);
       sortieViolationMig.println();
       sortieViolationMig.flush();
       sortieViolationMig.close();
         
       
       
       sortieQualityT.print(best.fitness);
       sortieQualityT.println();
       sortieQualityT.flush();
       sortieQualityT.close();
       
       sortieAllocationCostT.print(best.allocationCost);
       sortieAllocationCostT.println();
       sortieAllocationCostT.flush();
       sortieAllocationCostT.close();
       
       sortieObjT.print(best.objectiveFunc);
       sortieObjT.println();
       sortieObjT.flush();
       sortieObjT.close();
       
       sortieNbofUST.print(best.activeServers);
       sortieNbofUST.println();
       sortieNbofUST.flush();
       sortieNbofUST.close();
       
       
       sortieExecT.print(executionFinal);
       sortieExecT.println();
       sortieExecT.flush();
       sortieExecT.close();
       
       sortieViolationCaprT.print(best.penality_capacity);
       sortieViolationCaprT.println();
       sortieViolationCaprT.flush();
       sortieViolationCaprT.close();
       
       sortieViolationDeadT.print(best.penality_latency);
       sortieViolationDeadT.println();
       sortieViolationDeadT.flush();
       sortieViolationDeadT.close();
       
       sortieMigrationT.print(best.migrationCost);
       sortieMigrationT.println();
       sortieMigrationT.flush();
       sortieMigrationT.close();
       
       sortieViolationMigT.print(best.penalty_migration);
       sortieViolationMigT.println();
       sortieViolationMigT.flush();
       sortieViolationMigT.close();
    
       for(int i = 0;i < best.selected.size(); i++)
       {
           sortiePlacedServersT.print(best.selected.get(i));
           sortiePlacedServersT.println();
       }
       sortiePlacedServersT.flush();
       sortiePlacedServersT.close();
       
      sortieSRate.print(groupsAcceptanceRate);
      sortieSRate.println();
      sortieSRate.flush();
      sortieSRate.close();
      
      sortieSRateT.print(groupsAcceptanceRate);
      sortieSRateT.println();
      sortieSRateT.flush();
      sortieSRateT.close();
       
   }
    
   void writeSolution(Chromosome best,int id,String str,int rerun,int [][]C,int threshold,String path,float variation) throws IOException
   {
        PrintWriter sortieSolution = null;
        PrintWriter sortieServerData = null;

        sortieSolution = new PrintWriter(new FileWriter(path+"/Solution/"+initial_config+"/"+scenario+"_"+initial_config+"_"+id+"_"+str+"_"+rerun+"_"+threshold+"_"+cap+"vcpu_"+t+"_"+velocity+"_"+variation+".txt"));
        sortieServerData = new PrintWriter(new FileWriter(path+"/ServerData/"+initial_config+"/"+scenario+"_"+initial_config+"_"+id+"_"+str+"_"+rerun+"_"+threshold+"_"+cap+"vcpu_"+t+"_"+velocity+"_"+variation+".txt"));
       
                                                                                           
        System.out.println(" "+initial_config);
        sortieSolution.println();
        for(int s=0;s<sets;s++)
        {
            sortieSolution.print(" "+best.groups[s].fognode);
            sortieSolution.println();
        }
        sortieSolution.flush();
        sortieSolution.close();
        
        best.serverCapacityUsageCalculus();
        
        for(int e=0;e<best.activeServers;e++)
        {
            sortieServerData.print( best.serversData.get(e));
            sortieServerData.println();
        }

        sortieServerData.flush();
        sortieServerData.close();
   }
   void writeLatencyDistribution(Chromosome best,int id,String str,int threshold,int rerun,double W[][][],int C[][],double Dn[][][],int Dmax,String path,float variation)
   {
        String filename=path+"/CDF/"+initial_config+"/"+scenario+"_"+str+" Output"+initial_config+"_"+generations+"_"+id+"_"+threshold+"_"+rerun+"_"+cap+"vcpu_"+t+"_"+velocity+"_"+variation+".txt";
     
       
         try 
         {
            resume(best, W, C, Dn, Dmax,filename);
         } 
       catch (IOException ex) {
            Logger.getLogger(GeneticPenalty_Dynamic.class.getName()).log(Level.SEVERE, null, ex);
        }
   }
   
   public Population setup(int C[][],int[][]Dn_eligibility,double Dn[][][],double[][][]W,double[]CDH,double[][] Dn_closeness,List<Integer>sol_mig,double[][]BW,int[]VMS)
   {
       int z,j;
       ctrC=0;ctrD=0;ctrM=0;
       lamdaC=0.0005;  lamdaD=0.0005; lamdaM=0.0005;
        
          //Inject the solution of the previous session
       Chromosome chr=new Chromosome(sets);
       for (j=0;j<sol_mig.size();j++) 
       {
            Set set=new Set();
            set.nbPlayers=nbOfPlayersPerSet;
            set.setId=j;
            set.setSizeItem(sumWP[j]);
//              set.assignPlayer(W);
            chr.groups[j]=set;
            chr.groups[j].fognode =sol_mig.get(j);
  
       }
       for (j=sol_mig.size();j<sets;j++) 
       {
            Set set=new Set();
            set.nbPlayers=nbOfPlayersPerSet;
            set.setId=j;
            set.setSizeItem(sumWP[j]);
//              set.assignPlayer(W);
            chr.groups[j]=set;
            List<Integer>eligibleServersSet=eligibleServers.get(j);
             if(Math.random()>GmE && !eligibleServersSet.isEmpty())//80%
                  chr.groups[j].fognode = eligibleServersSet.get(pick(eligibleServersSet.size()));
             else
             {
                 int sv=pick(servers);
                 chr.groups[j].fognode = sv;
             }   
       }
       pop.setIndividual(0, chr);
       eval(pop.getIndividual(0),C,Dn,Dn_closeness,CDH,sol_mig,BW,VMS);

      
        z=0;
        for (int i=1;i<popSize;i++)
        {
           chr=new Chromosome(sets);
          for (j=0;j<sets;j++) 
          {
              Set set=new Set();
              set.nbPlayers=nbOfPlayersPerSet;
              set.setId=j;
              set.setSizeItem(sumWP[j]);
//              set.assignPlayer(W);
              chr.groups[j]=set;
              List<Integer>eligibleServersSet=eligibleServers.get(j);
               if(Math.random()>GmE && !eligibleServersSet.isEmpty())//80%
                    chr.groups[j].fognode = eligibleServersSet.get(pick(eligibleServersSet.size()));
               else
               {
                   int sv=pick(servers);
                   chr.groups[j].fognode = sv;
               }
          }
          pop.setIndividual(i, chr);
          eval(pop.getIndividual(i),C,Dn,Dn_closeness,CDH,sol_mig,BW,VMS);
          
        }

        pen1C = (pop.getIndividual(0).penality_capacity==0.0);
        pen1D = (pop.getIndividual(0).penality_latency==0.0);
        pen1M = (pop.getIndividual(0).penalty_migration==0.0);
        best=new Chromosome(sets);

        return pop;
   }
   
   public void genetic(Population pop,int[][]Dn_eliigibility,int[][]C,double Dn[][][],double[][][]W,double[]CDH,double[][] Dn_closeness,long start,String str,int id,int threshold,int rerun,List<Integer>sol_mig,double[][]BW,int[]VMS,float variation)
   {
       PrintWriter sortie=null;
//       PrintWriter sortieSynthese=null;
//       PrintWriter sortieProfiling=null;
       PrintWriter sortiePercent=null;
        try {
            int count=0;
            milestone = sets/2;
//            sortie = new PrintWriter(new FileWriter("/nfs/homes/amira.rayane.benamer/Results/Static/Output/"+sets+"/"+str+" Output"+sets+"_"+generations+"_"+id+"_"+threshold+"_"+rerun+"_"+C[0][0]+"vcpu.txt"));
//            sortieSynthese = new PrintWriter(new FileWriter("/nfs/homes/amira.rayane.benamer/Results/Static/Synthese/"+sets+"/"+str+" Synthese"+sets+"_"+generations+"_"+id+"_"+threshold+"_"+rerun+"_"+C[0][0]+"vcpu.txt"));
//            sortieProfiling = new PrintWriter(new FileWriter("/nfs/homes/amira.rayane.benamer/Results/Static/Profiling/"+sets+"/"+str+" Profiling"+sets+"_"+generations+"_"+id+"_"+threshold+"_"+rerun+"_"+C[0][0]+"vcpu.txt"));
//            sortiePercent = new PrintWriter(new FileWriter("/nfs/homes/amira.rayane.benamer/Results/Static/Percent/"+sets+"/"+str+" Percent"+sets+"_"+generations+"_"+id+"_"+threshold+"_"+rerun+"_"+C[0][0]+"vcpu.txt"));
        
             sortie = new PrintWriter(new FileWriter(path+"/Synthese/"+initial_config+"/"+scenario+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+velocity+"_"+variation+"_"+cap+"vcpu.txt"));
             sortiePercent = new PrintWriter(new FileWriter(path+"/Percent/"+initial_config+"/"+scenario+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+velocity+"_"+variation+"_"+cap+"vcpu.txt"));
        


//            sortie = new PrintWriter(new FileWriter(path+"\\Synthese\\"+initial_config+"\\"+scenario+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+velocity+"_"+cap+"vcpu.txt"));
//            sortiePercent = new PrintWriter(new FileWriter(path+"\\Percent\\"+initial_config+"\\"+scenario+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+velocity+"_"+cap+"vcpu.txt"));
        
             long endTime=System.nanoTime();
             long duration=(endTime-start);
             double exec=duration/1000000;
//             sortieProfiling.print("end generate Population: "+exec);
//             sortieProfiling.println();
          
            do
            {
                count++;
//                System.out.println("--------------------------------------count: "+count);

                long now=System.nanoTime();
                pop=repro(pop,Dn_eliigibility,C,Dn,W,CDH,Dn_closeness,str,sol_mig,BW,VMS);
                endTime=System.nanoTime();
                duration=(endTime-now);
                exec=duration/1000000;
//                sortieProfiling.print("end reproduction: "+exec);
//                sortieProfiling.println();
//                    pop.display(pop.size());

                pen2C = (pop.getIndividual(0).penality_capacity==0.0);
                pen2D = (pop.getIndividual(0).penality_latency==0.0);
                pen2M = (pop.getIndividual(0).penalty_migration==0.0);
                if (!(pen1C^pen2C))
                {
                    if (ctrC < milestone) ctrC++; else ctrC = 0;
                }
                else
                {
                    pen1C = pen2C; ctrC = 0;
                }
                
                 if (!(pen1D^pen2D))
                {
                    if (ctrD < milestone) ctrD++; else ctrD = 0;
                }
                else
                {
                    pen1D = pen2D; ctrD = 0;
                }
                 
                 if (!(pen1M^pen2M))
                {
                    if (ctrM < milestone) ctrM++; else ctrM = 0;
                }
                else
                {
                    pen1M = pen2M; ctrM = 0;
                }

                if (pop.getIndividual(0).penality_capacity == 0.0 && (pop.getIndividual(0).penality_latency <=best.penality_latency ||pop.getIndividual(0).penalty_migration <=best.penalty_migration  || best.flag==-1) && pop.getIndividual(0).fitness<best.fitness)
                {
                   
                    endTime=System.nanoTime();
                    duration=(endTime-start);
                    exec=duration/1000000;
                    double percent=0.0;
                    if(best.fitness!=Double.MAX_VALUE)
                        percent=((best.fitness-pop.getIndividual(0).fitness)/best.fitness)*100;
                    try 
                    {
                        best=(Chromosome)pop.getIndividual(0).clone();
                    }
                    catch (CloneNotSupportedException ex) 
                    {
                        Logger.getLogger(GeneticPenalty_Dynamic.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    best.flag=1;
                    sortiePercent.print(percent);
                    sortiePercent .println();
                    bestgen= count;
                    System.out.println("best Fitness: "+best.fitness+" percent: "+percent+" penalty_capacity= "+best.penality_capacity+" penalty_latency= "+best.penality_latency+" penalty_migration= "+best.penalty_migration+" cummulativeCost= "+best.cummulative_cost+" MigrationCost= "+best.migrationCost +" bestgen "+bestgen+" exec: "+exec);
                    sortie.print("best Fitness: "+best.fitness+" percent: "+percent+" penalty_capacity: "+best.penality_capacity+" penalty_latency= "+best.penality_latency+ " bestgen "+bestgen+" exec: "+exec);
                    sortie.println();
                    System.out.println("lamdaC: "+lamdaC+" lamdaD: "+lamdaD+" lamdaM: "+lamdaM);
//                    sortieSynthese.print("lamdaC: "+lamdaC+" lamdaD: "+lamdaD+" lamdaM: "+lamdaM);
//                    sortieSynthese.println();
                    
//                    
                }
                endTime=System.nanoTime();
                duration=(endTime-start);
                exec=duration/1000000;
//                sortieProfiling.print("end iteration: "+exec);
//                sortieProfiling.println();
//                sortieProfiling.println();
            } while (count < generations);
        } catch (IOException ex) {
            Logger.getLogger(GeneticPenalty_Dynamic.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            sortie.flush();
//            sortieSynthese.flush();
//            sortieProfiling.flush();
            sortie.close();
//            sortieSynthese.close();
//            sortieProfiling.close();
            sortiePercent.flush();
            sortiePercent.close();
        }
   }
   
   public Population repro(Population pop,int[][]Dn_eliigibility,int[][]C,double Dn[][][],double[][][]W,double[]CDH,double[][] Dn_closeness,String str,List<Integer>sol_mig,double[][]BW,int[]VMS)
   {
       Population oldPop=new Population(popSize,sets,C,W);
       int i,j,ii[]=new int[popSize],k,m,n,stop;
       boolean sorted;
       Chromosome ff[]=new Chromosome[popSize],z;
       /* initialize */
       long now=System.nanoTime();
       for (i=0;i<popSize;i++)
        {
           try 
           {
               oldPop.setIndividual(i, (Chromosome)pop.getIndividual(i).clone());
           } 
           catch (CloneNotSupportedException ex) 
           {
               Logger.getLogger(GeneticPenalty_Dynamic.class.getName()).log(Level.SEVERE, null, ex);
           }
//           oldPop.setIndividual(i, (Chromosome)SerializationUtils.clone(pop.getIndividual(i)));
          ii[i] = i;
          ff[i] = oldPop.getIndividual(i) ;
        }
        
        long endTime=System.nanoTime();
        long duration=(endTime-now);
        double exec=duration/1000000;
//        sortieProfiling.print("end initialization: "+exec);
//        sortieProfiling.println();
        
        /* bubble sort population */
        now=System.nanoTime();
        sorted =true;
        quickSort(ff, ii, 0, popSize-1);
        endTime=System.nanoTime();
        duration=(endTime-now);
        exec=duration/1000000;
//        sortieProfiling.print("end sort: "+exec);
//        sortieProfiling.println();
        
         
        
        /* replicate top nrep solutions */
        now=System.nanoTime();
        for(i=0;i<nrep;i++)  
        {
             try 
           {
                pop.setIndividual(i,(Chromosome)oldPop.getIndividual(ii[i]).clone());
           } 
            catch (CloneNotSupportedException ex) 
            {
                Logger.getLogger(GeneticPenalty_Dynamic.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        endTime=System.nanoTime();
        duration=(endTime-now);
        exec=duration/1000000;
//        sortieProfiling.print("end replicate top nrep solutions: "+exec);
//        sortieProfiling.println();
        

//         System.out.println("-----------------------------SORTING--------------"+nrep);
//         

     
        double valueC=1.0;
        if (ctrC == milestone)
        {
          if (pen1C)
          {
            lamdaC /= (beta2*mult);
            valueC=lamdaC*(1-beta2*mult);
          }
          else 
          {
            lamdaC *= mult;
            valueC=lamdaC-(lamdaC/mult);
          }
        }
        
        double valueD=1.0;
        if (ctrD == milestone)
        {
          if (pen1D)
          {
            lamdaD /= (beta2*mult);
            valueD=lamdaD*(1-beta2*mult);
          }
          else 
          {
            lamdaD *= mult;
            valueD=lamdaD-(lamdaD/mult);
          }
        }
        
        double valueM=1.0;
        if (ctrM == milestone)
        {
          if (pen1M)
          {
            lamdaM /= (beta2*mult);
            valueM=lamdaM*(1-beta2*mult);
          }
          else 
          {
            lamdaM *= mult;
            valueM=lamdaM-(lamdaM/mult);
          }
        }
        
        //update fitness value with new lamdaC & lamdaD
        now=System.nanoTime();
        for(i=0;i<nrep;i++) 
             pop.getIndividual(i).fitness += (valueC*pop.getIndividual(i).penality_capacity)+(valueD*pop.getIndividual(i).penality_latency)+(valueM*pop.getIndividual(i).penalty_migration);
        
        endTime=System.nanoTime();
        duration=(endTime-now);
        exec=duration/1000000;
//        sortieProfiling.print("end update fitness value with new lamdaC & lamdaD: "+exec);
//        sortieProfiling.println();
        
        /* Crossover */
        now=System.nanoTime();
        
        if(str.equals("GC"))
             crossoverGenePerGene(oldPop, pop, Dn_eliigibility, C, Dn, CDH, Dn_closeness,sol_mig,BW,VMS);
        if(str.equals("1P"))
             crossoverOnePoint(oldPop, pop, Dn_eliigibility, C, Dn,  CDH, Dn_closeness,sol_mig,BW,VMS);
        if(str.equals("2P"))
             crossoverTwoPoints(oldPop, pop, Dn_eliigibility, C, Dn,  CDH, Dn_closeness,sol_mig,BW,VMS);
        if(str.equals("4P"))
             crossoverFourPoints(oldPop, pop, Dn_eliigibility, C, Dn, CDH, Dn_closeness,sol_mig,BW,VMS);
        
        endTime=System.nanoTime();
        duration=(endTime-now);
        exec=duration/1000000;
//        sortieProfiling.print("end crossover: "+exec);
//        sortieProfiling.println();
       
//          System.out.println("_________________CROSSOVER_______________");
//        System.out.println("");System.out.println("");System.out.println("");
//        pop.display(nrep);
        
        
        /* create mutations */
        now=System.nanoTime();
//        pop=mutation(Dn_closeness, Dn_eligibility, pop, sortieProfiling);
        for (i=0;i<nmut;i++)
        {
          k=popSize-nmut+i;
          Chromosome offspring=new Chromosome(sets);
           pop.setIndividual(k, offspring);
          for (j=0;j<sets;j++) 
          {
              Set set=new Set();
              set.nbPlayers=nbOfPlayersPerSet;
              set.setId=j;
              set.setSizeItem(sumWP[j]);
//              set.assignPlayer(W);
              offspring.groups[j]=set;
               List<Integer>server=eligibleServers.get(j);
                if(Math.random()>GmE && !server.isEmpty())//80%
                {
                    offspring.groups[j].fognode  = server.get(pick(server.size()));
                }
                else
                {
                    Random rnd=new Random();
                    int sv=rnd.nextInt(servers);
                    offspring.groups[j].fognode  =sv;
                }
          }
            endTime=System.nanoTime();
            duration=(endTime-now);
            exec=duration/1000000;
//            sortieProfiling.print("end mutation: "+exec);
//            sortieProfiling.println();

            now=System.nanoTime();
            pop.setIndividual(k, offspring);
            eval(pop.getIndividual(k),C,Dn,Dn_closeness,CDH,sol_mig,BW,VMS);  
        }
        endTime=System.nanoTime();
        duration=(endTime-now);
        exec=duration/1000000;
//        sortieProfiling.print("end evaluation: "+exec);
//        sortieProfiling.println();
        
//        System.out.println("_____MUTATION_______");
//        pop.display(nrep);
//        System.out.println("pop: "+pop);
        return pop;
   }
   
   public void crossoverGenePerGene(Population oldPop,Population pop,int[][]Dn_eliigibility,int[][]C,double Dn[][][],double[]CDH,double[][] Dn_closeness,List<Integer>sol_mig,double[][]BW,int[]VMS)
   {
        int i = nrep,j,k,n;
        int stop = (popSize - nmut);

        while (i < stop) 
        {
          Chromosome offspring1=new Chromosome(sets);
          Chromosome offspring2 =new Chromosome(sets);
          j = pick(popSize);
          k = pick(popSize);
          for(m=0;m<sets;m++)
          {
              Set set=new Set();
              set.nbPlayers=nbOfPlayersPerSet;
              set.setId=m;
              set.setSizeItem(sumWP[m]);
              try {
                  //              set.assignPlayer(W);
                  offspring1.groups[m]=(Set)set.clone();//(Set)SerializationUtils.clone(set);
                  offspring2.groups[m]=(Set)set.clone();//(Set)SerializationUtils.clone(set);
              } catch (CloneNotSupportedException ex) {
                  Logger.getLogger(GeneticPenalty_Dynamic.class.getName()).log(Level.SEVERE, null, ex);
              }
             
              n = pick(100);
             if (n<=70)
            {
              offspring1.groups[m].fognode=oldPop.getIndividual(j).groups[m].fognode;
              offspring2.groups[m].fognode=oldPop.getIndividual(k).groups[m].fognode;
            }
            else 
            {
              offspring1.groups[m].fognode=oldPop.getIndividual(k).groups[m].fognode;
              offspring2.groups[m].fognode=oldPop.getIndividual(j).groups[m].fognode;
            }
          }

          pop.setIndividual(i, offspring1);
          pop.setIndividual((i+1), offspring2);
      
          eval(pop.getIndividual(i),C,Dn,Dn_closeness,CDH,sol_mig,BW,VMS); eval(pop.getIndividual(i+1),C,Dn,Dn_closeness,CDH,sol_mig,BW,VMS);
          i=i+2;
        }
   }
   
    public void crossoverTwoPoints(Population oldPop,Population pop,int[][]Dn_eliigibility,int[][]C,double Dn[][][],double[]CDH,double[][] Dn_closeness,List<Integer>sol_mig,double[][]BW,int[]VMS)
   {
    
        int i = nrep,j,k;
        int stop = (popSize - nmut);

        while (i < stop) 
        {
          j = pick(popSize);
          k = pick(popSize);
          Chromosome offspring1,offspring2;
          
          offspring1=new Chromosome(sets);
          offspring2 =new Chromosome(sets);
       
          int point1=pick(sets);
          int point2=pick(sets);
          if(point2<point1)
          {
              int temp=point1;
              point1=point2;
              point2=temp;
          }
           int []sequences={0,point1,point2,sets};
            Set set=new Set();
            set.nbPlayers=nbOfPlayersPerSet;
           for(int s=0;s<sequences.length-1;s++)
           { 
               int n = pick(100);
                if (n<=70)
                {
                    Object []slice1=ArrayUtils.clone(Arrays.copyOfRange(oldPop.getIndividual(j).groups,sequences[s], sequences[s+1]));
                    Object []slice2=ArrayUtils.clone(Arrays.copyOfRange(oldPop.getIndividual(k).groups,sequences[s], sequences[s+1]));
                    System.arraycopy(slice1, 0, offspring1.groups, sequences[s], (sequences[s+1]-sequences[s]));
                     System.arraycopy(slice2, 0, offspring2.groups, sequences[s], (sequences[s+1]-sequences[s]));

//                   for(int m=sequences[s];m<sequences[s+1];m++)
//                    {
//                        set.setId=m;
//                        set.setSizeItem(sumWP[m]);
//                        try 
//                        {
//                            offspring1.groups[m]=(Set)set.clone();//(Set)SerializationUtils.clone(set);
//                            offspring2.groups[m]=(Set)set.clone();//(Set)SerializationUtils.clone(set);
//                        } catch (CloneNotSupportedException ex) 
//                        {
//                            Logger.getLogger(GeneticPenalty.class.getName()).log(Level.SEVERE, null, ex);
//                        }
//                         offspring1.groups[m].fognode=oldPop.getIndividual(j).groups[m].fognode;
//                         offspring2.groups[m].fognode=oldPop.getIndividual(k).groups[m].fognode;
//                    }
                }
                else
                {
                    Object []slice1=ArrayUtils.clone(Arrays.copyOfRange(oldPop.getIndividual(k).groups,sequences[s], sequences[s+1]));
                    Object []slice2=ArrayUtils.clone(Arrays.copyOfRange(oldPop.getIndividual(j).groups,sequences[s], sequences[s+1]));
                    System.arraycopy(slice1, 0, offspring1.groups, sequences[s], (sequences[s+1]-sequences[s]));
                     System.arraycopy(slice2, 0, offspring2.groups, sequences[s], (sequences[s+1]-sequences[s]));
//                     for(int m=sequences[s];m<sequences[s+1];m++)
//                    {
//                        set.setId=m;
//                        set.setSizeItem(sumWP[m]);
//                        try 
//                        {
//                            offspring1.groups[m]=(Set)set.clone();//(Set)SerializationUtils.clone(set);
//                            offspring2.groups[m]=(Set)set.clone();//(Set)SerializationUtils.clone(set);
//                        } catch (CloneNotSupportedException ex) 
//                        {
//                            Logger.getLogger(GeneticPenalty.class.getName()).log(Level.SEVERE, null, ex);
//                        }
//                         offspring1.groups[m].fognode=oldPop.getIndividual(k).groups[m].fognode;
//                         offspring2.groups[m].fognode=oldPop.getIndividual(j).groups[m].fognode;
//                    }
                 }
           }

          pop.setIndividual(i, offspring1);
          pop.setIndividual((i+1), offspring2);
          eval(pop.getIndividual(i),C,Dn,Dn_closeness,CDH,sol_mig,BW,VMS); eval(pop.getIndividual(i+1),C,Dn,Dn_closeness,CDH,sol_mig,BW,VMS);
           i=i+2;
        }
   }
    
    public void crossoverFourPoints(Population oldPop,Population pop,int[][]Dn_eliigibility,int[][]C,double Dn[][][],double[]CDH,double[][] Dn_closeness,List<Integer>sol_mig,double[][]BW,int[]VMS)
   {
        int i = nrep,j,k;
        int stop = (popSize - nmut);

        while (i < stop) 
        {
          j = pick(popSize);
          k = pick(popSize);
          Chromosome offspring1,offspring2;
          
          offspring1=new Chromosome(sets);
          offspring2 =new Chromosome(sets);
       

          int points[]={pick(sets),pick(sets),pick(sets),pick(sets)};
          boolean bool=true;
          while(bool)
          {
              bool=false;
              for(int p=0;p<points.length-1;p++)
              {
                  if(points[p]>points[p+1])
                  {
                      int temp=points[p+1];
                      points[p+1]=points[p];
                      points[p]=temp;
                      bool=true;
                  }
              }
          }
           int []sequences={0,points[0],points[1],points[2],points[3],sets};    
           for(int s=0;s<sequences.length-1;s++)
           { 
               int n = pick(100);
                if (n<=70)
                {
                    Object []slice1=ArrayUtils.clone(Arrays.copyOfRange(oldPop.getIndividual(j).groups,sequences[s], sequences[s+1]));
                    Object []slice2=ArrayUtils.clone(Arrays.copyOfRange(oldPop.getIndividual(k).groups,sequences[s], sequences[s+1]));
                    System.arraycopy(slice1, 0, offspring1.groups, sequences[s], (sequences[s+1]-sequences[s]));
                    System.arraycopy(slice2, 0, offspring2.groups, sequences[s], (sequences[s+1]-sequences[s]));
                }
                else
                {
                    Object []slice1=ArrayUtils.clone(Arrays.copyOfRange(oldPop.getIndividual(k).groups,sequences[s], sequences[s+1]));
                    Object []slice2=ArrayUtils.clone(Arrays.copyOfRange(oldPop.getIndividual(j).groups,sequences[s], sequences[s+1]));
                    System.arraycopy(slice1, 0, offspring1.groups, sequences[s], (sequences[s+1]-sequences[s]));
                    System.arraycopy(slice2, 0, offspring2.groups, sequences[s], (sequences[s+1]-sequences[s]));
                 }
           }

          pop.setIndividual(i, offspring1);
          pop.setIndividual((i+1), offspring2);
          eval(pop.getIndividual(i),C,Dn,Dn_closeness,CDH,sol_mig,BW,VMS); eval(pop.getIndividual(i+1),C,Dn,Dn_closeness,CDH,sol_mig,BW,VMS);
           i=i+2;
        }
   }
    
     public void crossoverOnePoint(Population oldPop,Population pop,int[][]Dn_eliigibility,int[][]C,double Dn[][][],double[]CDH,double[][] Dn_closeness,List<Integer>sol_mig,double[][]BW,int[]VMS)
   {
        int i = nrep,j,k;
        int stop = (popSize - nmut);

        while (i < stop) 
        {
          j = pick(popSize);
          k = pick(popSize);
          Chromosome offspring1,offspring2;
          
          offspring1=new Chromosome(sets);
          offspring2 =new Chromosome(sets);
       

          int point=pick(sets);
        
           int []sequences={0,point,sets};    
           for(int s=0;s<sequences.length-1;s++)
           { 
               int n = pick(100);
                if (n<=70)
                {
                    Object []slice1=ArrayUtils.clone(Arrays.copyOfRange(oldPop.getIndividual(j).groups,sequences[s], sequences[s+1]));
                    Object []slice2=ArrayUtils.clone(Arrays.copyOfRange(oldPop.getIndividual(k).groups,sequences[s], sequences[s+1]));
                    System.arraycopy(slice1, 0, offspring1.groups, sequences[s], (sequences[s+1]-sequences[s]));
                    System.arraycopy(slice2, 0, offspring2.groups, sequences[s], (sequences[s+1]-sequences[s]));
                }
                else
                {
                    Object []slice1=ArrayUtils.clone(Arrays.copyOfRange(oldPop.getIndividual(k).groups,sequences[s], sequences[s+1]));
                    Object []slice2=ArrayUtils.clone(Arrays.copyOfRange(oldPop.getIndividual(j).groups,sequences[s], sequences[s+1]));
                    System.arraycopy(slice1, 0, offspring1.groups, sequences[s], (sequences[s+1]-sequences[s]));
                    System.arraycopy(slice2, 0, offspring2.groups, sequences[s], (sequences[s+1]-sequences[s]));
                 }
           }

          pop.setIndividual(i, offspring1);
          pop.setIndividual((i+1), offspring2);
          eval(pop.getIndividual(i),C,Dn,Dn_closeness,CDH,sol_mig,BW,VMS); eval(pop.getIndividual(i+1),C,Dn,Dn_closeness,CDH,sol_mig,BW,VMS);
           i=i+2;
        }
   }
    
   
    
    Chromosome[] help(ExecutorService threadPool,int start,int limit,Set set,Chromosome offspring1,Chromosome offspring2,Population oldPop,int j,int k)
    {
        Chromosome offs[]=new Chromosome[2];
        threadPool.submit(new Runnable() {
            @Override
            public void run()
            {
                for(m=start;m<limit;m++)
                {
                    set.setId=m;
                    set.setSizeItem(sumWP[m]);
                    System.out.println("m: "+m);
                    try 
                     {
                        offspring1.groups[m]=(Set)set.clone();//(Set)SerializationUtils.clone(set);
                        offspring2.groups[m]=(Set)set.clone();//(Set)SerializationUtils.clone(set);
                        } catch (CloneNotSupportedException ex) 
                        {
                           Logger.getLogger(GeneticPenalty_Dynamic.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        offspring1.groups[m].fognode=oldPop.getIndividual(j).groups[m].fognode;
                        offspring2.groups[m].fognode=oldPop.getIndividual(k).groups[m].fognode;
                     }
            }
        });
        offs[0]=offspring1;
        offs[1]=offspring2;
        return offs;
    }
    
   
   
   public int pick(int n)  /* generate a pseudorandom integer number between 1 and n */
    {
        int p;

        p = (int) (Math.random()*n);
        return p;
    }
   
   void eval(Chromosome chr,int[][]C,double Dn[][][],double [][]Dn_closeness,double[]CDH,List<Integer>sol_mig,double[][]BW,int[]VMS)
    { 
        int i,j; 
        int satisfactedGroups = 0;
        double px=0.0, pe=0.0, pmig=0.0;
        int use[]=new int[servers]; 
        int demandVCPU[]=new int[servers]; 
        for (i=0;i<sets;i++) 
        {
            use[chr.groups[i].fognode] += (double)chr.groups[i].sizeItem; 
            demandVCPU[chr.groups[i].fognode] +=Math.round((double)chr.groups[i].sizeItem/1000);
            pe+=(Dn_closeness[i][chr.groups[i].fognode])*(Dn_closeness[i][chr.groups[i].fognode]);
            if(Dn_closeness[i][chr.groups[i].fognode]== 0)
                satisfactedGroups+= 1;
        }
//        System.out.println("satisfied groups: "+satisfactedGroups+" pe: "+pe);
          

        double firstPart=0.0;
        for (j=0;j<servers;j++)
        {
            firstPart+=demandVCPU[j]*CDH[j];
            if (use[j] > C[j][0]) 
                px += (use[j]-C[j][0])*(use[j]-C[j][0]);  //*2
        }
        
        //dynamic
        double clientPaidFee = paidFee*  satisfactedGroups;
        Data data = migrationCost(sol_mig, chr, BW, VMS);
        double MigC = data.cost * GmE;
        chr.cummulative_cost = cummulative_cost;
//        chr.pmig=MigC;
        chr.penality_capacity = px;
        chr.penality_latency= pe;
        chr.penalty_migration= data.pm;
        chr.clientPaidFee = clientPaidFee + cummulative_clientPaidFee;
        chr.migrated = data.migrated;
        chr.migrationCost = MigC;
        chr.allocationCost = firstPart+ cummulative_cost;
        chr.objectiveFunc=chr.clientPaidFee -(chr.allocationCost + MigC);//revenue
        chr.fitness = -(chr.objectiveFunc)+ (lamdaC*px)+ (lamdaD*pe) + (lamdaM*data.pm) ; 
      
//        System.out.println("pe: "+pe+" pen: "+px);
//        System.out.println("lamdaC: "+lamdaC+" (lamdaC*px): "+(lamdaC*px)+" lamdaD: "+lamdaD+" (lamdaD*pe): "+(lamdaD*pe));
    } 
   

    double calculateProcessing(double [][][]W,int set)
    {
        int VMcpu=(int)((W[set][0][0]*nbOfPlayersPerSet)/tick);
        if(VMcpu<1000)
            VMcpu=1000;
        if(VMcpu>1000 && VMcpu<2000 )
            VMcpu=2000;
        return (((double)(W[set][0][0]))/(VMcpu))*1000;
    }
   
    
    static List<Integer> getEligibleServers(int set, int[][]Dn_eliigibility)
    {
        List<Integer>eligibleServers=new ArrayList<>();
        for(int e=0;e<Dn_eliigibility[set].length;e++)
        {
            if(Dn_eliigibility[set][e]==1)
                eligibleServers.add(e);
        }
        return eligibleServers;
    }  
    
     public void resume(Chromosome chr,double W[][][],int C[][],double Dn[][][],int Dmax,String filename) throws IOException
   {
       PrintWriter sortie= new PrintWriter(new FileWriter(filename));
      

       
       int satisfactedGroups = 0;
       for(int s=0;s<chr.groups.length;s++)
       {
           boolean satisfacted = true;
           double pr=calculateProcessing(W, s);//calculateProcessing(W, C, s,chr.groups[s].fognode);//,chr.capRel,count);
           for(int p=0;p<W[s].length;p++)
           {
               double delay=Dn[s][p][chr.groups[s].fognode]+pr;
               
               if(Dmax - delay < 0 ) satisfacted = false;            
               sortie.print(delay);
               sortie.println();
           }
           if (satisfacted)
               satisfactedGroups++;
       }
      sortie.flush();
      sortie.close();
      
      groupsAcceptanceRate = (double) satisfactedGroups/chr.groups.length;
  
   }  
     
  
     
     public boolean isValid(Chromosome chr)
    {
       for(int i=0;i<chr.groups.length;i++)
           if(chr.groups[i].fognode==-1)
               return false;
       return true;
    }
     
     
   void GetData(String fileName) throws FileNotFoundException, IOException 
    {

        Scanner s = new Scanner(new FileReader(fileName));
        String got=s.next();
        Scanner in = new Scanner(got).useDelimiter("[^0-9]+");
        mult=in.nextInt();
        System.out.println("mult: "+mult);
            
        got=s.next();
        in = new Scanner(got).useDelimiter("[^,0-9]+");
        beta2=in.nextDouble();
        System.out.println("beta2: "+beta2);
            
        got=s.next();
        in = new Scanner(got).useDelimiter("[^,0-9]+");
        rep=in.nextDouble();
        System.out.println("rep: "+rep);
            
        got=s.next();
        in = new Scanner(got).useDelimiter("[^,0-9]+");
        mut=in.nextDouble();
        System.out.println("mut: "+mut);
            
            
        got=s.next();
        in = new Scanner(got).useDelimiter("[^0-9]+");
        generations=in.nextInt();
      
        
        if (sets >= 3000) 
            generations = 7500;
        if(sets == 5000 ) 
            generations = 10000;
        System.out.println("generations: "+generations);
//        
        got=s.next();
        in = new Scanner(got).useDelimiter("[^,0-9]+");
        m=in.nextInt();
        System.out.println("m: "+m);
        if(sets >= 750)
            m = 1;
            
        s.close();
    }
     
   
   
   
    int partition(Chromosome array[],int ii[],int begin,int end) 
    {
    int pivot = end;

 
    int counter = begin;
    for (int i = begin; i < end; i++) {
        if (array[i].fitness < array[pivot].fitness) {
            Chromosome temp = array[counter];int j=ii[counter];
            array[counter] = array[i];ii[counter]=ii[i];
            array[i] = temp;ii[i]=j;
            counter++;
        }
    }
    Chromosome temp = array[pivot];
    array[pivot] = array[counter];
    array[counter] = temp;

    return counter;
    }

    public  void quickSort(Chromosome array[],int ii[],int begin,int end) 
    {
        if (end <= begin) return;
        int pivot = partition(array,ii, begin, end);
        quickSort(array,ii, begin, pivot-1);
        quickSort(array,ii, pivot+1, end);
    }
    
      //**dynamic
      public Data migrationCost(List<Integer> sol, Chromosome chr, double[][]BW,int[]VMS)
      {
//          System.out.println("--------------Migration------");
           Set[] groups = chr.groups;
           double MC = 0.0;
          double  pm = 0.0;
           int co = 0;
           for(int s=0;s<sol.size();s++)
           {
               if((groups[s].fognode)!=sol.get(s))
               {
//                   System.out.println("set: "+s);
//                   System.out.println("already hosted on: "+sol.get(s)+" migrated to: "+groups[s].fognode);
//                   System.out.println("VM size: "+VMS[s]+" and bandwidth: "+BW[groups[s].fognode][sol.get(s)]);
                   double bandwidth = get_BW(BW[groups[s].fognode][sol.get(s)]);
                   double migrationTime=(VMS[s]/bandwidth);
//                   System.out.println("migration Time: "+migrationTime);
//                   System.out.println("bandwidth: "+bandwidth);
//                   System.out.println("max M: "+M);
                   pm += M - migrationTime > 0 ? 0 : Math.abs(M - migrationTime);
//                   System.out.println("pm: "+pm);
                   co++;
                   groups[s].migrationTime=migrationTime;
                   MC=MC+migrationTime;
               }
           }
           
           Data data = new Data();
           data.cost = MC;
           data.pm = pm;
           data.migrated = co;
           return data;
      }
    
      static double get_BW(double bandwidth)
      {
          double latency = (double)1/bandwidth;
       
          if( latency < 3)
              bandwidth = 262;
          if( latency >= 3)
              bandwidth = 174;
          if(latency < 6)
              bandwidth = 104;
          if( latency <8 )
              bandwidth = 64;
          if( latency < 9)
              bandwidth = 58;
          if( latency>10)
              bandwidth = 21;
          
          
          return bandwidth;
      }
      
    void save_Output(int migrated, int instance, int id, String scenario,int threshold,String velocity, float variation, int cap) throws IOException
    {
        PrintWriter sortieMigCan = new PrintWriter(new FileWriter(path+"Input_Output/Migration/"+instance+"/"+"GA_Migrated_"+scenario+"_"+id+"_"+threshold+"_"+velocity+"_"+variation+"_"+cap+"vcpu.txt",true));
      
        
//        PrintWriter sortieMigCan = new PrintWriter(new FileWriter(path+"Input_Output\\Migration\\"+instance+"\\"+"GA_Migrated_"+scenario+"_"+id+".txt",true));
        sortieMigCan.print(migrated);
        sortieMigCan.println();
        
        sortieMigCan.flush();
        sortieMigCan.close();
    }
    public void compute_groups_requirements(double W[][][]) 
    {
         for (int s=0;s<sets;s++)
        {
            double cpu=0;
            for(int p=0;p<nbOfPlayersPerSet;p++)
            {
                cpu=cpu+W[s][p][0];
            }
            sumWP[s]=(int)((double)cpu/tick);
            if(sumWP[s]<1000)
                sumWP[s]=1000;
            if(sumWP[s]>1000 && sumWP[s]<2000)
                sumWP[s]=2000;
            
        }
    }
    
    
}

 