/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Benchmarks.GreyWolf;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author ASUS
 */
 class Data
{
    double cost, pm;
    int migrated;
}

public class GreyWolfOptimizer_Dynamic 
{
    int maxIter=50;
    int searchAgents=100;
    double w=30,h1=0.8,h2=0.1;
    double executionFinal;
    public int sets; 
    public int nbOfPlayersPerSet;
    public int servers;
    //----------
    int Dmax;
    public double tick;
    static int sumWP[];
    List<List<Integer>> eligibleServers;
    int[][]population,population_copy;
    double[] px,pxI;
    double[] pe,peI;
    double[] pm,pmI;
    double [] fitness,objI;
    double []allocationCost, allocationCostI;
    double []migrationCost, migrationCostI;
    int []migrated,migratedI;
    double [] revenue, revenueI;
    public double GmE=0.2;
    public int bestGen;
    
   public List<Double>serversData; List<Integer>selected;
   int[]ff;
   double cummulative_cost,cummulative_clientPaidFee;
   private double paidFee = 2;
    
    //Grey wolf 
    int []alpha; double alpha_score=Double.MAX_VALUE;double alpha_px=Double.MAX_VALUE;double alpha_pe=Double.MAX_VALUE;double alpha_pm=Double.MAX_VALUE;double alpha_revenue, alpha_allocationCost, alpha_migrationCost,alpha_fitness;int alpha_migrated;
    int []beta;  double beta_score=Double.MAX_VALUE;double beta_px=Double.MAX_VALUE;double beta_pe=Double.MAX_VALUE, beta_pm = Double.MAX_VALUE;double beta_revenue, beta_allocationCost, beta_migrationCost;
    int []delta; double delta_score=Double.MAX_VALUE;double delta_px=Double.MAX_VALUE;double delta_pe=Double.MAX_VALUE, delta_pm = Double.MAX_VALUE;double delta_revenue, delta_allocationCost, delta_migrationCost;
    
    int activeBins;
    List<Double>convergence_curve;
    double a;
    
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
    double groupsAcceptanceRate;
     float variation;
    
     public GreyWolfOptimizer_Dynamic (int sets, int nbOfPlayersPerSet, int servers,double tick,int Dmax,String scenario, String path, int cap,int initial_config,int t, String velocity,float variation) 
     {
            this.variation = variation;
            this.sets = sets;
            this.nbOfPlayersPerSet = nbOfPlayersPerSet;
            this.servers = servers;
            this.tick=tick;
            this.Dmax=Dmax;
            this.velocity=velocity;
            this.t = t;
  
            this.scenario = scenario;
            this.path = path;
            this.cap = cap;
            this.initial_config=initial_config;
        }
     public void GW_optimize(int C[][], double W[][][],double Dn[][][],double CDH[],int[][] Dn_eligibility,double[][] Dn_closeness, String str,String cross,int id,int threshold,int rerun,List<Integer>sol_mig,double [][]BW,int[]VMS,double cummulative_cost,double cummulative_paidFee) 
     {
         this.cummulative_cost = cummulative_cost;
         this.cummulative_clientPaidFee = cummulative_paidFee;
         Random rand=new Random();
         alpha=new int[sets];
         beta=new int[sets];
         delta=new int[sets];
         
         sumWP=new int[sets];
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

        eligibleServers=new ArrayList<>();
        for(int i=0;i<sets;i++)
        {
            List<Integer>serversE=getEligibleServers(i, Dn_eligibility);
            eligibleServers.add(serversE);
        }
        
        long startTime=System.nanoTime();
        Instant start=Instant.now();
        //Initialize the positions of search agents
        initialization(C, Dn_eligibility, sumWP, Dn, W, CDH, Dn_closeness);
        convergence_curve=new ArrayList<>();
        
        
        int l=0;// Loop counter
        // Main loop
        while (l<maxIter)
        {
             a=2.0-l*((2.0)/maxIter); // a decreases linearly fron 2 to 0
//             System.out.println("a: "+a);
             nonDominatedSorting();

            
               //Update the Position of search agents including omegas
            for (int i=3;i<population.length;i++)
            {
                 for (int j=0;j<population[i].length;j++)    
                 {
                    double r1=Math.random(); // r1 is a random number in [0,1]
                    double r2=Math.random(); // r2 is a random number in [0,1]

                    double A1=(2.0*a*r1)-a; //Equation 21
                    double C1=2.0*r2;     //Equation 22

                    double D_alpha=Math.abs(C1*alpha[j]-population[i][j]); //Equation 19
                    double X1=Math.abs(alpha[j]-A1*D_alpha); // Equation (24
                    
                    r1=Math.random();
                    r2=Math.random();

                    double A2=(2.0*a*r1)-a; 
                    double C2=2.0*r2; 

                    double D_beta=Math.abs(C2*beta[j]-population[i][j]); //Equation 19
                    double X2=Math.abs(beta[j]-A2*D_beta); // Equation (24      

                    r1=Math.random();
                    r2=Math.random();

                    double A3=(2.0*a*r1)-a; 
                    double C3=2.0*r2; 

                    double D_delta=Math.abs(C3*delta[j]-population[i][j]); //Equation 19
                    double X3=Math.abs(delta[j]-A3*D_delta); // Equation (24                 
                
                    int val=(int)Math.floor(X1+X2+X3)/3;
                     if(val>=servers) val=servers-1;
                     if(val<0) val=0;
                    population[i][j]=val;// Equation 23
               }
            }
            
            //Mutation
            for (int i=0;i<population.length;i++)
            {
                if(Math.random()>mutationProbability(l))
                {
                    int[] position =new int[sets];
                    for (int j=0;j<sets;j++) 
                    {
                        List<Integer>eligibleServersSet=eligibleServers.get(j);
                        if(Math.random()>GmE && !eligibleServersSet.isEmpty())//80%
                             position[j] = eligibleServersSet.get(pick(eligibleServersSet.size()));
                        else
                         {
                             int sv=pick(servers);
                             position[j] = sv;
                         }
                    }
                    population[i]=position;
                    pe[i]=-1;
                    px[i]=-1;
                    pm[i]=-1;
                    fitness[i]=Double.MAX_VALUE;
               }
            }
            
//            System.out.println("*********generation: "+l);


//            System.out.println("____________________________________________________");
            for(int i=0;i<population.length;i++)
            {
                //Calculate objective function for each search agent
                double fit=eval(population[i],i,px,pe,pm,C,Dn,Dn_closeness,W,CDH, i,sol_mig,BW,VMS);
                fitness[i]=fit;
                //Update Alpha, Beta, and Delta
                if (fit<alpha_score  )
                {
                    alpha_score=fit; //Update alpha
                    alpha_fitness=fit; //Update alpha
                    alpha_px=px[i];
                    alpha_pe=pe[i];
                    alpha_pm=pm[i];
                    alpha_allocationCost=allocationCost[i];
                    alpha_migrationCost=migrationCost[i];
                    alpha_migrated=migrated[i];
                    alpha_revenue=revenue[i];
                    bestGen=l;
                    System.arraycopy(population[i], 0, alpha, 0, population[i].length);
                }
           

                if (fit>alpha_score && fit<beta_score )
                {
                     beta_score=fit; //Update beta
                     beta_px=px[i];
                     beta_pe=pe[i];
                     beta_pm=pm[i];
                    beta_allocationCost=allocationCost[i];
                    beta_migrationCost=migrationCost[i];
                    beta_revenue=revenue[i];
                    System.arraycopy(population[i], 0, beta, 0, population[i].length);
                }
        
                if (fit>alpha_score && fit>beta_score && fit<delta_score )
                {
                     delta_score=fit; //Update delta
                     delta_px=px[i];
                     delta_pe=pe[i];
                    delta_allocationCost=allocationCost[i];
                    delta_migrationCost=migrationCost[i];
                    delta_revenue=revenue[i];
                    System.arraycopy(population[i], 0, delta, 0, population[i].length);
                }
            }
//            System.out.println("alpha Score: "+alpha_score+" beta_score: "+beta_score+" delta_score: "+delta_score);
    
                 

            if(l==0)
            {
                int[][]Rt=new int[2*searchAgents][sets];
                double[] px_copy=new double[2*searchAgents];
                double[] pe_copy=new double[2*searchAgents];
                double[] pm_copy=new double[2*searchAgents];
                double[] obj_copy=new double[2*searchAgents];
                double[] allocationCost_copy=new double[2*searchAgents];
                double[] migrationCost_copy=new double[2*searchAgents];
                int[] migrated_copy=new int[2*searchAgents];
                double[] revenue_copy=new double[2*searchAgents];
                int co=0;
                for(int i=0;i<searchAgents;i++)
                {
                    System.arraycopy(population_copy[i], 0,Rt[i] , 0, sets);
                    px_copy[i]=pxI[i];
                    pe_copy[i]=peI[i];
                    pm_copy[i]=pmI[i];
                    obj_copy[i]=objI[i];
                    allocationCost_copy[co]=allocationCostI[i];
                    migrationCost_copy[co]=migrationCostI[i];
                    migrated_copy[co]=migratedI[i];
                    revenue_copy[co]=revenueI[i];
                    co++;
                }
                for(int i=0;i<searchAgents;i++)
                {
                    System.arraycopy(population[i], 0,Rt[co] , 0, sets);
                    px_copy[co]=px[i];
                    pe_copy[co]=pe[i];
                    pm_copy[co]=pm[i];
                    obj_copy[co]=fitness[i];
                    allocationCost_copy[co]=allocationCost[i];
                    migrationCost_copy[co]=migrationCost[i];
                    migrated_copy[co]=migrated[i];
                    revenue_copy[co]=revenue[i];
                    co++;
                }
                population=new int[2*searchAgents][sets];
                px=new double[2*searchAgents];
                pe=new double[2*searchAgents];
                pm=new double[2*searchAgents];
                fitness=new double[2*searchAgents];
                allocationCost=new double[2*searchAgents];
                migrationCost=new double[2*searchAgents];
                migrated=new int[2*searchAgents];
                revenue=new double[2*searchAgents];
                px=px_copy;
                pe=pe_copy;
                pm=pm_copy;
                fitness=obj_copy;
                allocationCost=allocationCost_copy;
                migrationCost=migrationCost_copy;
                migrated=migrated_copy;
                revenue=revenue_copy;
                
                population=Rt;
            }
            l=l+1;    
            convergence_curve.add(alpha_score);
        }
        
        Instant end=Instant.now();
       long endTime=System.nanoTime();
       long duration=(endTime-startTime);
       System.out.println("____________");
       Duration interval = Duration.between(start, end);
       executionFinal= interval.getSeconds();
       System.out.println("Execution time in seconds: " +  executionFinal);
       System.out.println("Best Generation: "+bestGen);
       System.out.println("************Solution**************");
       howMuchPlacedServers(alpha);
       display(alpha, Dn_closeness, C, alpha_fitness, alpha_allocationCost, alpha_migrationCost, alpha_revenue, alpha_px, alpha_pe, alpha_pm);
  
       try {
            writeSolution(alpha, id,str,rerun,C,threshold,path,variation);
        } catch (IOException ex) {
            Logger.getLogger(GreyWolfOptimizer_Dynamic.class.getName()).log(Level.SEVERE, null, ex);
        }

  //      best.displayViolations();
        
//        displayPlayerSets(best, W, C, Dn, Dmax,"");
     
        writeLatencyDistribution(alpha, id, str, threshold, rerun, W, C, Dn, Dmax,variation);
        try {
            writeSolutionOnOutputFiles(id, rerun,str,C,threshold,path,variation);
        } catch (IOException ex) {
            Logger.getLogger(GreyWolfOptimizer_Dynamic.class.getName()).log(Level.SEVERE, null, ex);
        }
        
       
        
        try 
        {
            save_Output( alpha_migrated, initial_config, id, scenario,threshold, velocity, variation, cap);
        } 
        catch (IOException ex) 
        {
            Logger.getLogger(GreyWolfOptimizer_Dynamic.class.getName()).log(Level.SEVERE, null, ex);
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
//            sortieQuality = new PrintWriter(new FileWriter("/nfs/homes/amira.rayane.benamer/Results/Static/Quality/"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+C[0][0]+"vcpu.txt",true));
//            sortieObj = new PrintWriter(new FileWriter("/nfs/homes/amira.rayane.benamer/Results/Static/Obj/"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+C[0][0]+"vcpu.txt",true));
//            sortieExec = new PrintWriter(new FileWriter("/nfs/homes/amira.rayane.benamer/Results/Static/Exec/"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+C[0][0]+"vcpu.txt",true));
//            sortieNbofUS = new PrintWriter(new FileWriter("/nfs/homes/amira.rayane.benamer/Results/Static/NumberOfUS/"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+C[0][0]+"vcpu.txt",true));
//            sortieBestIter = new PrintWriter(new FileWriter("/nfs/homes/amira.rayane.benamer/Results/Static/BestIter/"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+C[0][0]+"vcpu.txt",true));
//            sortieViolationCapr = new PrintWriter(new FileWriter("/nfs/homes/amira.rayane.benamer/Results/Static/ViolatedCap/"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+C[0][0]+"vcpu.txt",true));
//            sortieViolationDead = new PrintWriter(new FileWriter("/nfs/homes/amira.rayane.benamer/Results/Static/ViolatedDead/"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+C[0][0]+"vcpu.txt",true));
       
            
//            //-------------------MAGI----------------------------------------------
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
//      
//            
//            -----------------------------------------------------------
//            
            
            
            
//            sortieQuality = new PrintWriter(new FileWriter(path+"\\Quality\\"+scenario+"_"+initial_config+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+velocity+"_"+cap+"vcpu.txt",true));
//            sortieObj = new PrintWriter(new FileWriter(path+"\\Obj\\"+scenario+"_"+initial_config+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+velocity+"_"+cap+"vcpu.txt",true));
//            sortieAllocationCost = new PrintWriter(new FileWriter(path+"\\AllocationCost\\"+scenario+"_"+initial_config+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+velocity+"_"+cap+"vcpu.txt",true));
//            sortieExec = new PrintWriter(new FileWriter(path+"\\Exec\\"+scenario+"_"+initial_config+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+velocity+"_"+cap+"vcpu.txt",true));
//            sortieNbofUS = new PrintWriter(new FileWriter(path+"\\NumberOfUS\\"+scenario+"_"+initial_config+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+velocity+"_"+cap+"vcpu.txt",true));
//            sortieBestIter = new PrintWriter(new FileWriter(path+"\\BestIter\\"+scenario+"_"+initial_config+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+cap+"vcpu.txt",true));
//            sortieViolationCapr = new PrintWriter(new FileWriter(path+"\\ViolatedCap\\"+scenario+"_"+initial_config+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+velocity+"_"+cap+"vcpu.txt",true));
//            sortieViolationDead = new PrintWriter(new FileWriter(path+"\\ViolatedDead\\"+scenario+"_"+initial_config+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+velocity+"_"+cap+"vcpu.txt",true));
//            //sortiePlacedServers = new PrintWriter(new FileWriter(path+"\\ActiveServers\\"+initial_config+"\\"+scenario+"_"+initial_config+"_"+id+"_"+str+"_"+rerun+"_"+threshold+"_"+velocity+"_"+cap+"vcpu_"+t+".txt"));
//            sortieViolationMig = new PrintWriter(new FileWriter(path+"\\ViolatedMig\\"+scenario+"_"+initial_config+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+velocity+"_"+cap+"vcpu.txt",true));
//            sortieMigration = new PrintWriter(new FileWriter(path+"\\Migration\\"+scenario+"_"+initial_config+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+velocity+"_"+cap+"vcpu.txt",true));
//            sortieSRate= new PrintWriter(new FileWriter( path+"\\GroupsAcceptanceRate\\"+scenario+"_"+initial_config+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+velocity+"_"+cap+"vcpu.txt",true));
//                
//            
//            sortieQualityT = new PrintWriter(new FileWriter(path+"\\Quality\\t"+t+"\\"+scenario+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+velocity+"_"+cap+"vcpu.txt",true));
//            sortieObjT = new PrintWriter(new FileWriter(path+"\\Obj\\t"+t+"\\"+scenario+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+velocity+"_"+cap+"vcpu.txt",true));
//            sortieAllocationCostT = new PrintWriter(new FileWriter(path+"\\AllocationCost\\t"+t+"\\"+scenario+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+velocity+"_"+cap+"vcpu.txt",true));
//            sortieExecT = new PrintWriter(new FileWriter(path+"\\Exec\\t"+t+"\\"+scenario+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+velocity+"_"+cap+"vcpu.txt",true));
//            sortieNbofUST = new PrintWriter(new FileWriter(path+"\\NumberOfUS\\t"+t+"\\"+scenario+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+velocity+"_"+cap+"vcpu.txt",true));
//            sortieViolationCaprT = new PrintWriter(new FileWriter(path+"\\ViolatedCap\\t"+t+"\\"+scenario+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+velocity+"_"+cap+"vcpu.txt",true));
//            sortieViolationDeadT = new PrintWriter(new FileWriter(path+"\\ViolatedDead\\t"+t+"\\"+scenario+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+velocity+"_"+cap+"vcpu.txt",true));
//            sortiePlacedServersT = new PrintWriter(new FileWriter(path+"\\ActiveServers\\"+initial_config+"\\t"+t+"\\"+scenario+"_"+initial_config+"_"+id+"_"+str+"_"+rerun+"_"+threshold+"_"+velocity+"_"+cap+"vcpu_"+t+".txt"));
//            sortieViolationMigT = new PrintWriter(new FileWriter(path+"\\ViolatedMig\\t"+t+"\\"+scenario+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+velocity+"_"+cap+"vcpu.txt",true));
//            sortieMigrationT = new PrintWriter(new FileWriter(path+"\\Migration\\t"+t+"\\"+scenario+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+velocity+"_"+cap+"vcpu.txt",true));
//            sortieSRateT= new PrintWriter(new FileWriter( path+"\\GroupsAcceptanceRate\\t"+t+"\\"+scenario+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+velocity+"_"+cap+"vcpu.txt",true));
//      
        } catch (IOException ex) {
//            sortieQuality = new PrintWriter(new FileWriter("/nfs/homes/amira.rayane.benamer/Results/Static/Quality/"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+C[0][0]+"vcpu.txt"));
//            sortieObj = new PrintWriter(new FileWriter("/nfs/homes/amira.rayane.benamer/Results/Static/Obj/"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+C[0][0]+"vcpu.txt"));
//            sortieExec = new PrintWriter(new FileWriter("/nfs/homes/amira.rayane.benamer/Results/Static/Exec/"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+C[0][0]+"vcpu.txt"));
//            sortieNbofUS = new PrintWriter(new FileWriter("/nfs/homes/amira.rayane.benamer/Results/Static/NumberOfUS/"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+C[0][0]+"vcpu.txt"));
//            sortieBestIter = new PrintWriter(new FileWriter("/nfs/homes/amira.rayane.benamer/Results/Static/BestIter/"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+C[0][0]+"vcpu.txt"));
//            sortieViolationCapr = new PrintWriter(new FileWriter("/nfs/homes/amira.rayane.benamer/Results/Static/ViolatedCap/"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+C[0][0]+"vcpu.txt"));
//            sortieViolationDead = new PrintWriter(new FileWriter("/nfs/homes/amira.rayane.benamer/Results/Static/ViolatedDead/"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+C[0][0]+"vcpu.txt"));
           



//             //----------------MAGI-----------------------------------
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
//      
             
             //---------------------------------------------------------




//            sortieQuality = new PrintWriter(new FileWriter(path+"\\Quality\\"+scenario+"_"+initial_config+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+velocity+"_"+cap+"vcpu.txt"));
//            sortieObj = new PrintWriter(new FileWriter(path+"\\Obj\\"+scenario+"_"+initial_config+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+velocity+"_"+cap+"vcpu.txt"));
//            sortieAllocationCost = new PrintWriter(new FileWriter(path+"\\AllocationCost\\"+scenario+"_"+initial_config+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+velocity+"_"+cap+"vcpu.txt"));
//            sortieExec = new PrintWriter(new FileWriter(path+"\\Exec\\"+scenario+"_"+initial_config+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+velocity+"_"+cap+"vcpu.txt"));
//            sortieNbofUS = new PrintWriter(new FileWriter(path+"\\NumberOfUS\\"+scenario+"_"+initial_config+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+velocity+"_"+cap+"vcpu.txt"));
//            sortieBestIter = new PrintWriter(new FileWriter(path+"\\BestIter\\"+scenario+"_"+initial_config+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+velocity+"_"+cap+"vcpu.txt"));
//            sortieViolationCapr = new PrintWriter(new FileWriter(path+"\\ViolatedCap\\"+scenario+"_"+initial_config+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+velocity+"_"+cap+"vcpu.txt"));
//            sortieViolationDead = new PrintWriter(new FileWriter(path+"\\ViolatedDead\\"+scenario+"_"+initial_config+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+velocity+"_"+cap+"vcpu.txt"));
//            //sortiePlacedServers = new PrintWriter(new FileWriter(path+"\\ActiveServers\\"+initial_config+"\\"+scenario+"_"+initial_config+"_"+id+"_"+str+"_"+rerun+"_"+threshold+"_"+velocity+"_"+cap+"vcpu_"+t+".txt"));
//            sortieViolationMig = new PrintWriter(new FileWriter(path+"\\ViolatedMig\\"+scenario+"_"+initial_config+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+velocity+"_"+cap+"vcpu.txt"));
//            sortieMigration = new PrintWriter(new FileWriter(path+"\\Migration\\"+scenario+"_"+initial_config+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+velocity+"_"+cap+"vcpu.txt"));
//            sortieSRate= new PrintWriter(new FileWriter( path+"\\GroupsAcceptanceRate\\"+scenario+"_"+initial_config+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+velocity+"_"+cap+"vcpu.txt"));
//      
//            
//            sortieQualityT = new PrintWriter(new FileWriter(path+"\\Quality\\t"+t+"\\"+scenario+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+velocity+"_"+cap+"vcpu.txt"));
//            sortieObjT = new PrintWriter(new FileWriter(path+"\\Obj\\t"+t+"\\"+scenario+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+velocity+"_"+cap+"vcpu.txt"));
//            sortieAllocationCostT = new PrintWriter(new FileWriter(path+"\\AllocationCost\\t"+t+"\\"+scenario+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+velocity+"_"+cap+"vcpu.txt"));
//            sortieExecT = new PrintWriter(new FileWriter(path+"\\Exec\\t"+t+"\\"+scenario+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+velocity+"_"+cap+"vcpu.txt"));
//            sortieNbofUST = new PrintWriter(new FileWriter(path+"\\NumberOfUS\\t"+t+"\\"+scenario+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+velocity+"_"+cap+"vcpu.txt"));
//            sortieViolationCaprT = new PrintWriter(new FileWriter(path+"\\ViolatedCap\\t"+t+"\\"+scenario+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+velocity+"_"+cap+"vcpu.txt"));
//            sortieViolationDeadT = new PrintWriter(new FileWriter(path+"\\ViolatedDead\\t"+t+"\\"+scenario+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+velocity+"_"+cap+"vcpu.txt"));
//            sortiePlacedServersT = new PrintWriter(new FileWriter(path+"\\ActiveServers\\"+initial_config+"\\t"+t+"\\"+scenario+"_"+initial_config+"_"+id+"_"+str+"_"+rerun+"_"+threshold+"_"+velocity+"_"+cap+"vcpu_"+t+".txt"));
//            sortieViolationMigT = new PrintWriter(new FileWriter(path+"\\ViolatedMig\\t"+t+"\\"+scenario+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+velocity+"_"+cap+"vcpu.txt"));
//            sortieMigrationT = new PrintWriter(new FileWriter(path+"\\Migration\\t"+t+"\\"+scenario+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+velocity+"_"+cap+"vcpu.txt"));
//            sortieSRateT= new PrintWriter(new FileWriter( path+"\\GroupsAcceptanceRate\\t"+t+"\\"+scenario+"_"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+velocity+"_"+cap+"vcpu.txt"));
      
        }

       
        
          
          
       sortieQuality.print(alpha_fitness);
       sortieQuality.println();
       sortieQuality.flush();
       sortieQuality.close();
       
       sortieAllocationCost.print(alpha_allocationCost);
       sortieAllocationCost.println();
       sortieAllocationCost.flush();
       sortieAllocationCost.close();
       
       sortieObj.print(alpha_revenue);
       sortieObj.println();
       sortieObj.flush();
       sortieObj.close();
       
       sortieNbofUS.print(activeBins);
       sortieNbofUS.println();
       sortieNbofUS.flush();
       sortieNbofUS.close();
               
       sortieBestIter.print(bestGen);
       sortieBestIter.println();
       sortieBestIter.flush();
       sortieBestIter.close();
       
       sortieExec.print(executionFinal);
       sortieExec.println();
       sortieExec.flush();
       sortieExec.close();
       
       sortieViolationCapr.print(alpha_px);
       sortieViolationCapr.println();
        sortieViolationCapr.flush();
       sortieViolationCapr.close();
       
       sortieViolationDead.print(alpha_pe);
       sortieViolationDead.println();
       sortieViolationDead.flush();
       sortieViolationDead.close();
       
       
       sortieMigration.print(alpha_migrationCost);
       sortieMigration.println();
       sortieMigration.flush();
       sortieMigration.close();
       
       sortieViolationMig.print(alpha_pm);
       sortieViolationMig.println();
       sortieViolationMig.flush();
       sortieViolationMig.close();
         
       
       
       sortieQualityT.print(alpha_fitness);
       sortieQualityT.println();
       sortieQualityT.flush();
       sortieQualityT.close();
       
       sortieAllocationCostT.print(alpha_allocationCost);
       sortieAllocationCostT.println();
       sortieAllocationCostT.flush();
       sortieAllocationCostT.close();
       
       sortieObjT.print(alpha_revenue);
       sortieObjT.println();
       sortieObjT.flush();
       sortieObjT.close();
       
       sortieNbofUST.print(activeBins);
       sortieNbofUST.println();
       sortieNbofUST.flush();
       sortieNbofUST.close();
       
       
       sortieExecT.print(executionFinal);
       sortieExecT.println();
       sortieExecT.flush();
       sortieExecT.close();
       
       sortieViolationCaprT.print(alpha_px);
       sortieViolationCaprT.println();
       sortieViolationCaprT.flush();
       sortieViolationCaprT.close();
       
       sortieViolationDeadT.print(alpha_pe);
       sortieViolationDeadT.println();
       sortieViolationDeadT.flush();
       sortieViolationDeadT.close();
       
       sortieMigrationT.print(alpha_migrationCost);
       sortieMigrationT.println();
       sortieMigrationT.flush();
       sortieMigrationT.close();
       
       sortieViolationMigT.print(alpha_pm);
       sortieViolationMigT.println();
       sortieViolationMigT.flush();
       sortieViolationMigT.close();
    
       for(int i = 0;i < selected.size(); i++)
       {
           sortiePlacedServersT.print(selected.get(i));
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
        
       
       
    
   void writeSolution(int[]items,int id,String str,int rerun,int [][]C,int threshold,String path,float variation) throws IOException
   {
        PrintWriter sortieSolution = null;
        PrintWriter sortieServerData = null;

        sortieSolution = new PrintWriter(new FileWriter(path+"/Solution/"+initial_config+"/"+scenario+"_"+initial_config+"_"+id+"_"+str+"_"+rerun+"_"+threshold+"_"+cap+"vcpu_"+t+"_"+velocity+"_"+variation+".txt"));
        sortieServerData = new PrintWriter(new FileWriter(path+"/ServerData/"+initial_config+"/"+scenario+"_"+initial_config+"_"+id+"_"+str+"_"+rerun+"_"+threshold+"_"+cap+"vcpu_"+t+"_"+velocity+"_"+variation+".txt"));
       
        
        System.out.println(" "+initial_config);
//        sortieSolution = new PrintWriter(new FileWriter(path+"\\Solution\\"+initial_config+"\\"+scenario+"_"+initial_config+"_"+id+"_"+str+"_"+rerun+"_"+threshold+"_"+cap+"vcpu_"+t+"_"+velocity+".txt"));
//        sortieServerData = new PrintWriter(new FileWriter(path+"\\ServerData\\"+initial_config+"\\"+scenario+"_"+initial_config+"_"+id+"_"+str+"_"+rerun+"_"+threshold+"_"+cap+"vcpu_"+t+"_"+velocity+".txt"));
             sortieSolution.println();
        for(int s=0;s<sets;s++)
        {
            sortieSolution.print(" "+items[s]);
            sortieSolution.println();
        }
        
        serverCapacityUsageCalculus(C);
        
        for(int e=0;e<activeBins;e++)
        {
            sortieServerData.print( serversData.get(e));
            sortieServerData.println();
        }
        sortieSolution.flush();
        sortieSolution.close();
        sortieServerData.flush();
        sortieServerData.close();
      
   }
   
    public void serverCapacityUsageCalculus(int[][]C)
   {
       serversData=new ArrayList<>();
      ff=new int[activeBins];
       for(int i=0;i<activeBins;i++)
           ff[i]=0;
//    List<Integer>bins=new ArrayList<>();
        for(int s=0;s<alpha.length;s++)
           {
//               if(!bins.contains(alpha[s]))
//               {
//                   bins.add(alpha[s]);
//                   
//               }
               ff[selected.indexOf(alpha[s])]+=1;
           }

        for(int e=0;e<activeBins;e++)
        {
            int u=ff[e]*sumWP[0];
            Double frac=((double)u/C[selected.get(e)][0])*100;
            serversData.add(frac);
        }
   }
   void writeLatencyDistribution(int[] best,int id,String str,int threshold,int rerun,double W[][][],int C[][],double Dn[][][],int Dmax,float variation)
   {
        String filename=path+"/CDF/"+initial_config+"/"+scenario+"_"+str+" Output"+initial_config+"_"+maxIter+"_"+id+"_"+threshold+"_"+rerun+"_"+cap+"vcpu_"+t+"_"+velocity+"_"+variation+".txt";
     

//          String filename=path+"\\CDF\\"+initial_config+"\\"+scenario+"_"+str+" Output"+initial_config+"_"+maxIter+"_"+id+"_"+threshold+"_"+rerun+"_"+cap+"vcpu_"+t+"_"+velocity+".txt";
        
        try {
            resume(best, W, C, Dn, Dmax,filename);
        } catch (IOException ex) {
            Logger.getLogger(GreyWolfOptimizer_Dynamic.class.getName()).log(Level.SEVERE, null, ex);
        }

   }
      public void resume(int[]items,double W[][][],int C[][],double Dn[][][],int Dmax,String filename) throws IOException
   {
       PrintWriter sortie= new PrintWriter(new FileWriter(filename));
       int satisfactedGroups = 0;
       
       for(int s=0;s<items.length;s++)
       {
           double pr=calculateProcessing(W, s);//,chr.capRel,count);
           boolean satisfacted =true;
           for(int p=0;p<W[s].length;p++)
           {
               double delay=Dn[s][p][items[s]]+pr;
               if(Dmax - delay < 0 ) satisfacted = false;  
               sortie.print(delay);
               sortie.println();
           }
           if (satisfacted)
               satisfactedGroups++;
       }
       
       System.out.println("Paid Fee: "+(satisfactedGroups*paidFee +cummulative_clientPaidFee));
      groupsAcceptanceRate = (double) satisfactedGroups/items.length;
       
      sortie.flush();
      sortie.close();
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
     public void initialization(int C[][],int[][]Dn_eligibility,int[]sumWP,double Dn[][][],double[][][]W,double[]CDH,double[][] Dn_closeness)
   {
       population=new int[searchAgents][sets];
       population_copy=new int[searchAgents][sets];
       px=new double[searchAgents];pxI=new double[searchAgents];
       pe=new double[searchAgents];peI=new double[searchAgents];
       pm=new double[searchAgents];pmI=new double[searchAgents];
       fitness=new double[searchAgents];objI=new double[searchAgents];
       allocationCost=new double[searchAgents];allocationCostI=new double[searchAgents];
       migrationCost=new double[searchAgents];migrationCostI=new double[searchAgents];
       migrated=new int[searchAgents];migratedI=new int[searchAgents];
       revenue=new double[searchAgents];revenueI=new double[searchAgents];
        for (int i=0;i<searchAgents;i++)
        {
            int use[]=new int[servers];
            
          int[] position =new int[sets];
          for (int j=0;j<sets;j++) 
          {
              boolean bool=false;
              List<Integer>eligibleServersSet=eligibleServers.get(j);
              if( !eligibleServersSet.isEmpty())
              {
                  for(int e=0;e<eligibleServersSet.size();e++)
                  {
                    int server=eligibleServersSet.get(e);//eligibleServersSet.get(pick(eligibleServersSet.size()));
                    if(use[server]+sumWP[j]<=C[server][0])
                    {
                        bool=true;
                        position[j] = server;
                        use[server]=use[server]+sumWP[j];
                    }
                  }
              }
             if(eligibleServersSet.isEmpty() || !bool)
               {
                   int sv=pick(servers);
                   position[j] = sv;
               }
          }
          population[i]=position;
          population_copy[i]=position;
          pe[i]=-1;
          px[i]=-1;
          pm[i]=-1;
          fitness[i]=Double.MAX_VALUE;
          allocationCost[i]=Double.MAX_VALUE;
          migrationCost[i]=Double.MAX_VALUE;
          revenue[i]=Double.MAX_VALUE;
          peI[i]=-1;
          pxI[i]=-1;
          pmI[i]=-1;
          objI[i]=Double.MAX_VALUE;
          allocationCostI[i]=Double.MAX_VALUE;
          migrationCostI[i]=Double.MAX_VALUE;
          revenueI[i]=Double.MAX_VALUE;
        }
   }
     
    void nonDominatedSorting()
    {
        int ii[]=new int[population.length];

        for(int i=0;i<population.length;i++)
            ii[i]=i;
        quickSort(population,px,pe,pm, fitness,ii , 0, population.length-1);
    }
     
    
    double mutationProbability(int current)
    {
        if(current<=w)
            return h1*  (1-Math.pow((double)(current/maxIter),2));
        else
            return h2*  (1-Math.pow((double)(current/maxIter),2));
    }
    double eval(int[]elem,int k,double px[],double pe[],double[]pm,int[][]C,double Dn[][][],double [][]Dn_closeness,double[][][]W,double[]CDH,int l,List<Integer>sol_mig,double[][]BW,int[]VMS)
    { 
        int i,j; 
        int satisfactedGroups = 0;
        double pxX=0.0, peE=0.0, pmM=0.0;
        int use[]=new int[servers]; 
        int demandVCPU[]=new int[servers]; 

        for (i=0;i<sets;i++) 
        {
            use[elem[i]] += (double)sumWP[i]; 
            demandVCPU[elem[i]] +=Math.round((double)sumWP[i]/1000);
            peE+=(Dn_closeness[i][elem[i]]);//*(Dn_closeness[i][elem[i]]);
            if(Dn_closeness[i][elem[i]]== 0)
                satisfactedGroups+= 1;
        }
          

        double firstPart=0.0;
        for (j=0;j<servers;j++)
        {
            firstPart+=demandVCPU[j]*CDH[j];
            if (use[j] > C[j][0]) 
                pxX += (use[j]-C[j][0]);//*(use[j]-C[j][0]);  //*2
        }
       
        //dynamic
        double clientPaidFee = paidFee*  satisfactedGroups;
        Data data = migrationCost(sol_mig, elem, BW, VMS);
        pmM = data.pm;
        double MigC = data.cost * GmE;

         double r =  (clientPaidFee + cummulative_clientPaidFee) - (firstPart+ cummulative_cost + MigC);
                 
         px [k]=pxX;
         pe[k]=peE;
         pm[k]=pmM;
         allocationCost[k]=firstPart + cummulative_cost;
         migrationCost[k]=MigC;
         migrated[k]=data.migrated;
         revenue[k]=r;
         
         return -(r)+ pxX+ peE+pmM; 
    } 
    
    
    //**dynamic
      public Data migrationCost(List<Integer> sol, int[]elem, double[][]BW,int[]VMS)
      {
//          System.out.println("--------------Migration------");

           double MC = 0.0;
          double  pm = 0.0;
           int co = 0;
           for(int s=0;s<sol.size();s++)
           {
               if((elem[s])!=sol.get(s))
               {
//                   System.out.println("set: "+s);
//                   System.out.println("already hosted on: "+sol.get(s)+" migrated to: "+items[s].hostBin);
//                   System.out.println("VM size: "+VMS[s]+" and bandwidth: "+BW[items[s].hostBin][sol.get(s)]);
                   double bandwidth = get_BW(BW[elem[s]][sol.get(s)]);
                   double migrationTime=(VMS[s]/bandwidth);
//                   System.out.println("migration Time: "+migrationTime);
//                   System.out.println("bandwidth: "+bandwidth);
//                   System.out.println("max M: "+M);
//                   pm += M - migrationTime > 0 ? 0 : Math.abs(M - migrationTime);
//                   System.out.println("pm: "+pm);
                   co++;
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
    
    
    
     
       public int pick(int n)  /* generate a pseudorandom integer number between 1 and n */
    {
        int p;

        p = (int) (Math.random()*n);
        return p;
    }
       
     int partition(int array[][],double[]px,double pe[],double[]pm,double[]obj,int ii[],int begin,int end) 
    {
    int pivot = end;
  
 
    int counter = begin;
    for (int i = begin; i < end; i++) {
        if (obj[i]+px[i]+pe[i]+pm[i] < obj[pivot]+px[pivot]+pe[pivot]+pm[pivot]) {
            int[] temp = new int[sets];
            System.arraycopy(array[counter], 0, temp, 0, array[counter].length);
            int j=ii[counter];
            System.arraycopy(array[i], 0, array[counter], 0, array[i].length);
            ii[counter]=ii[i];
            System.arraycopy(temp, 0, array[i], 0,temp.length);
            ii[i]=j;
            
            double tempObj=obj[counter];
            obj[counter]=obj[i];
            obj[i]=tempObj;
            
            double temppx=px[counter];
            px[counter]=px[i];
            px[i]=temppx;
            
            double temppe=pe[counter];
            pe[counter]=pe[i];
            pe[i]=temppe;
            
            double temppm=pm[counter];
            pm[counter]=pm[i];
            pm[i]=temppm;
            
            counter++;
        }
    }
    
    int[] temp = new int[sets];
    System.arraycopy(array[pivot], 0, temp, 0, array[pivot].length);
    int j=ii[pivot];
    System.arraycopy(array[counter], 0, array[pivot], 0, array[counter].length);
    ii[pivot]=ii[counter];
    System.arraycopy(temp, 0, array[counter], 0,temp.length);
    ii[counter]=j;
            
    double tempObj=obj[pivot];
    obj[pivot]=obj[counter];
    obj[counter]=tempObj;
            
    double temppx=px[pivot];
    px[pivot]=px[counter];
    px[counter]=temppx;
    
    double temppe=pe[pivot];
    pe[pivot]=pe[counter];
    pe[counter]=temppe;
    
    double temppm=pm[pivot];
    pm[pivot]=pm[counter];
    pm[counter]=temppm;

    return counter;
    }

    public  void quickSort(int array[][],double[]px,double []pe,double []pm,double[]obj,int ii[],int begin,int end) 
    {
        if (end <= begin) return;
        int pivot = partition(array,px,pe,pm,obj,ii, begin, end);
        quickSort(array,px,pe,pm,obj,ii, begin, pivot-1);
        quickSort(array,px,pe,pm,obj,ii, pivot+1, end);
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
      
      
     public  void howMuchPlacedServers(int[]items)
     {
         selected=new ArrayList<>();
         for(int s=0;s<items.length;s++)
           {
               if(!selected.contains(items[s]))
               {
                   selected.add(items[s]);
               }
           }
         activeBins=selected.size();
     }
     
      public void display(int elem[],double[][]Dn_closeness,int[][]C,double alpha_fitness,double alpha_allocationCost,double alpha_migrationCost, double alpha_revenue,double alpha_px,double alpha_pe,double alpha_pm)
    { 
        int i=0,j=0;
        for (i=0;i<sets;i++) 
        {
            System.out.print(" "+elem[i]);
        }
        System.out.println("");
     
        
           System.out.print(" | "+alpha_fitness+"  --pen: "+alpha_px+" --pE: "+alpha_pe+" --pm: "+alpha_pm);
           System.out.println(""); 
           System.out.println(" |revenue "+alpha_revenue +" |Opex = "+(alpha_allocationCost+alpha_migrationCost));
           System.out.println("Migration Cost: "+alpha_migrationCost);
           System.out.println("Number of active servers is: "+activeBins);
    }
      
       void save_Output(int migrated, int instance, int id, String scenario,int threshold,String velocity, float variation, int cap) throws IOException
    {
        PrintWriter sortieMigCan = new PrintWriter(new FileWriter(path+"Input_Output/Migration/"+instance+"/"+"GW_Migrated_"+scenario+"_"+id+"_"+threshold+"_"+velocity+"_"+variation+"_"+cap+"vcpu.txt",true));
      
//        PrintWriter sortieMigCan = new PrintWriter(new FileWriter(path+"Input_Output\\Migration\\"+instance+"\\"+"GW_Migrated_"+scenario+"_"+id+".txt",true));
        sortieMigCan.print(migrated);
        sortieMigCan.println();
        
        sortieMigCan.flush();
        sortieMigCan.close();
    }
}
