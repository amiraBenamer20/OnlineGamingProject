/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GeneticAlgorithmPenalty;

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
 


/**
 *
 * @author Amira BENAMER
 */
import org.apache.commons.lang3.ArrayUtils;
public class GeneticPenalty_Static 
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
    public double GmE=0.2;
    
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
    int C_all[][];
    int W[][][];
    double Dn[][][];
    int Dmax;
    double CDH[];
    int[][] Dn_eligibility ;
    double lamdaC=0.0,lamdaD=0.0, mult, cons=0.0;
    boolean pen1C,pen2C,pen1D,pen2D;
    int ctrC,ctrD;
    

    int sumWP[];
    List<List<Integer>> eligibleServers;

    Population pop;
    
    String path;
    public GeneticPenalty_Static(String path,int sets, int nbOfPlayersPerSet, int servers,double tick,int Dmax) {
        this.path = path;
        this.sets = sets;
        this.nbOfPlayersPerSet = nbOfPlayersPerSet;
        this.servers = servers;
        this.tick=tick;
        this.Dmax=Dmax;
        try {
            GetData(new File("inputParametersGA.txt").getAbsolutePath());//read input parameters
        } catch (IOException ex) {
            Logger.getLogger(GeneticPenalty_Static.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
   public void Genetic_pen(int C[][],int C_all[][], double W[][][],double Dn[][][],double CDH[],int[][] Dn_eligibility,double[][] Dn_closeness, String str,String cross,int id,int threshold,int rerun )
   {
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
        
       System.out.println("***********************Execution********************");
       System.out.println("CONFIGURATION: ");
       System.out.println("Number of servers: "+servers+" Number of sets: "+sets);
       System.out.println("Start");
       
       
       long startTime=System.nanoTime();
       Instant start=Instant.now();
       setup(C,Dn_eligibility,Dn,W,CDH,Dn_closeness);
       genetic(pop, Dn_eligibility,C,Dn,W,CDH,Dn_closeness,startTime,cross,id,threshold,rerun);
       Instant end=Instant.now();
       Duration interval = Duration.between(start, end);
       executionFinal= interval.getSeconds();
       System.out.println("Execution time in seconds: " +executionFinal);
       
       
       System.out.println("************Solution**************");
        best.sumWP=sumWP;
        best.C=C;
      //**Display & Save 

        best.howMuchPlacedServers();
        best.display();
        try {
            writeSolution(best, id,str,rerun,C,threshold);
        } catch (IOException ex) {
            Logger.getLogger(GeneticPenalty_Static.class.getName()).log(Level.SEVERE, null, ex);
        }

  //      best.displayViolations();
        
//        displayPlayerSets(best, W, C, Dn, Dmax,"");
     
        writeLatencyDistribution(best, id, str, threshold, rerun, W, C, Dn, Dmax);
        try {
            writeSolutionOnOutputFiles(id, rerun,str,C,threshold);
        } catch (IOException ex) {
            Logger.getLogger(GeneticPenalty_Static.class.getName()).log(Level.SEVERE, null, ex);
        }
   }
   
       void writeSolutionOnOutputFiles(int id,int rerun,String str,int C[][],int threshold) throws IOException 
   {
       PrintWriter sortieQuality = null;
       PrintWriter sortieObj = null;
       PrintWriter sortieExec = null;
       PrintWriter sortieNbofUS = null;
       PrintWriter sortieBestIter=null;
       PrintWriter sortieViolationDead=null;
       PrintWriter sortieViolationCapr=null;

          try
        {
            sortieQuality = new PrintWriter(new FileWriter(path+"/Quality/"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+C[0][0]+"vcpu.txt",true));
            sortieObj = new PrintWriter(new FileWriter(path+"/Obj/"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+C[0][0]+"vcpu.txt",true));
            sortieExec = new PrintWriter(new FileWriter(path+"/Exec/"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+C[0][0]+"vcpu.txt",true));
            sortieNbofUS = new PrintWriter(new FileWriter(path+"/NumberOfUS/"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+C[0][0]+"vcpu.txt",true));
            sortieBestIter = new PrintWriter(new FileWriter(path+"/BestIter/"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+C[0][0]+"vcpu.txt",true));
            sortieViolationCapr = new PrintWriter(new FileWriter(path+"/ViolatedCap/"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+C[0][0]+"vcpu.txt",true));
            sortieViolationDead = new PrintWriter(new FileWriter(path+"/ViolatedDead/"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+C[0][0]+"vcpu.txt",true));
       
        } catch (IOException ex) {
            sortieQuality = new PrintWriter(new FileWriter(path+"/Quality/"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+C[0][0]+"vcpu.txt"));
            sortieObj = new PrintWriter(new FileWriter(path+"/Obj/"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+C[0][0]+"vcpu.txt"));
            sortieExec = new PrintWriter(new FileWriter(path+"/Exec/"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+C[0][0]+"vcpu.txt"));
            sortieNbofUS = new PrintWriter(new FileWriter(path+"/NumberOfUS/"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+C[0][0]+"vcpu.txt"));
            sortieBestIter = new PrintWriter(new FileWriter(path+"/BestIter/"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+C[0][0]+"vcpu.txt"));
            sortieViolationCapr = new PrintWriter(new FileWriter(path+"/ViolatedCap/"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+C[0][0]+"vcpu.txt"));
            sortieViolationDead = new PrintWriter(new FileWriter(path+"/ViolatedDead/"+str+"_"+id+"_"+rerun+"_"+threshold+"_"+C[0][0]+"vcpu.txt"));
       
        }

       
       sortieQuality.print(best.fitness);
       sortieQuality.println();
       sortieObj.print(best.objectiveFunc);
       sortieObj.println();
       sortieNbofUS.print(best.activeServers);
       sortieNbofUS.println();
       sortieBestIter.print(bestgen);
       sortieBestIter.println();
       sortieExec.print(executionFinal);
       sortieExec.println();
       sortieViolationCapr.print(best.penality_capacity);
       sortieViolationCapr.println();
       sortieViolationDead.print(best.penality_latency);
       sortieViolationDead.println();
       
       sortieBestIter.flush();
       sortieBestIter.close();
       sortieExec.flush();
       sortieExec.close();
       sortieNbofUS.flush();
       sortieNbofUS.close();
       sortieQuality.flush();
       sortieQuality.close();
       sortieObj.flush();
       sortieObj.close();
       sortieViolationCapr.flush();
       sortieViolationCapr.close();
       sortieViolationDead.flush();
       sortieViolationDead.close();
   }
    
   void writeSolution(Chromosome best,int id,String str,int rerun,int [][]C,int threshold) throws IOException
   {
        PrintWriter sortieSolution = null;
        PrintWriter sortieServerData = null;
        PrintWriter sortieActiveServers = null;
         sortieSolution = new PrintWriter(new FileWriter(path+"Solution\\"+sets+"\\"+sets+"_"+id+"_"+str+"_"+rerun+"_"+threshold+"_"+C[0][0]+"vcpu.txt"));
        sortieServerData = new PrintWriter(new FileWriter(path+"ServerData\\"+sets+"\\"+sets+"_"+id+"_"+str+"_"+rerun+"_"+threshold+"_"+C[0][0]+"vcpu.txt"));
        sortieActiveServers = new PrintWriter(new FileWriter(path+"ActiveServers\\"+sets+"\\"+sets+"_"+id+"_"+str+"_"+rerun+"_"+threshold+"_"+C[0][0]+"vcpu.txt"));
       
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
            
            sortieActiveServers.print( best.selected.get(e));
            sortieActiveServers.println();
        }

        sortieServerData.flush();
        sortieServerData.close();
        
        sortieActiveServers.flush();
        sortieActiveServers.close();
   }
   void writeLatencyDistribution(Chromosome best,int id,String str,int threshold,int rerun,double W[][][],int C[][],double Dn[][][],int Dmax)
   {
        String filename=path+"/CDF/"+sets+"/"+str+" Output"+sets+"_"+generations+"_"+id+"_"+threshold+"_"+rerun+"_"+C[0][0]+"vcpu.txt";
        try {
            resume(best, W, C, Dn, Dmax,filename);
        } catch (IOException ex) {
            Logger.getLogger(GeneticPenalty_Static.class.getName()).log(Level.SEVERE, null, ex);
        }
   }
   
   public Population setup(int C[][],int[][]Dn_eligibility,double Dn[][][],double[][][]W,double[]CDH,double[][] Dn_closeness)
   {
       
       int z,zD,j,cnt=0,cntD=0;
       ctrC=0;ctrD=0;
       lamdaC=0.0005;  lamdaD=0.0005;//initialize beta1
        
        z=0;
        for (int i=0;i<popSize;i++)
        {
          Chromosome chr=new Chromosome(sets);
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
                   int server=pick(servers);//choose randomly a fog node
                   chr.groups[j].fognode = server;
               }
          }
          pop.setIndividual(i, chr);
          eval(pop.getIndividual(i),C,Dn,Dn_closeness,CDH);
        }
        pen1C = (pop.getIndividual(0).penality_capacity==0.0);
        pen1D = (pop.getIndividual(0).penality_latency==0.0);
        best=new Chromosome(sets);

        
        return pop;
   }
   
   public void genetic(Population pop,int[][]Dn_eliigibility,int[][]C,double Dn[][][],double[][][]W,double[]CDH,double[][] Dn_closeness,long start,String str,int id,int threshold,int rerun)
   {
       PrintWriter sortie=null;
       PrintWriter sortieAnalytics=null;
       PrintWriter sortieSynthese=null;
       PrintWriter sortieProfiling=null;
       PrintWriter sortiePercent=null;
        try {
            int count=0;
            milestone = sets/2;
            sortie = new PrintWriter(new FileWriter(path+"/Output/"+sets+"/"+str+" Output"+sets+"_"+generations+"_"+id+"_"+threshold+"_"+rerun+"_"+C[0][0]+"vcpu.txt"));
            sortieSynthese = new PrintWriter(new FileWriter(path+"/Synthese/"+sets+"/"+str+" Synthese"+sets+"_"+generations+"_"+id+"_"+threshold+"_"+rerun+"_"+C[0][0]+"vcpu.txt"));
            sortieProfiling = new PrintWriter(new FileWriter(path+"/Profiling/"+sets+"/"+str+" Profiling"+sets+"_"+generations+"_"+id+"_"+threshold+"_"+rerun+"_"+C[0][0]+"vcpu.txt"));
            sortiePercent = new PrintWriter(new FileWriter(path+"/Percent/"+sets+"/"+str+" Percent"+sets+"_"+generations+"_"+id+"_"+threshold+"_"+rerun+"_"+C[0][0]+"vcpu.txt"));
        
            sortieAnalytics = new PrintWriter(new FileWriter(path+"/Analytics/GA_2P_ "+id+"_"+threshold+"_"+rerun+"_"+C[0][0]+"vcpu.txt"));
         
             long endTime=System.nanoTime();
             long duration=(endTime-start);
             double exec=duration/1000000;
             sortieProfiling.print("end generate Population: "+exec);
             sortieProfiling.println();
           
            do
            {
                count++;
//                System.out.println("count: "+count);
                long now=System.nanoTime();
                pop=repro(pop,Dn_eliigibility,C,Dn,W,CDH,Dn_closeness,sortieProfiling,str);
                endTime=System.nanoTime();
                duration=(endTime-now);
                exec=duration/1000000;
                sortieProfiling.print("end reproduction: "+exec);
                sortieProfiling.println();


                pen2C = (pop.getIndividual(0).penality_capacity==0.0);
                pen2D = (pop.getIndividual(0).penality_latency==0.0);
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

                if (pop.getIndividual(0).penality_capacity == 0.0 && (pop.getIndividual(0).penality_latency <=best.penality_latency || best.flag==-1) && pop.getIndividual(0).fitness<best.fitness)
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
                        Logger.getLogger(GeneticPenalty_Static.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    best.flag=1;
                    sortiePercent.print(percent);
                    sortiePercent .println();
                    bestgen= count;
                    System.out.println("best Fitness: "+best.fitness+" percent: "+percent+" penalty_Capacity= "+best.penality_capacity+" penalty_latency= "+best.penality_latency+" bestgen "+bestgen+" exec: "+exec);
                    sortie.print("best Fitness: "+best.fitness+" percent: "+percent+" penalty_capacity: "+best.penality_capacity+" penalty_latency= "+best.penality_latency+ " bestgen "+bestgen+" exec: "+exec);
                    sortie.println();
                    System.out.println("lamdaC: "+lamdaC+" lamdaD: "+lamdaD);
                    sortieSynthese.print("lamdaC: "+lamdaC+" lamdaD: "+lamdaD);
                    sortieSynthese.println();
                    if (best.penality_latency== 0.0)
                    {
                        sortieAnalytics.print(bestgen);
                        sortieAnalytics.println();
                        break;
                    }
                    
                }

                endTime=System.nanoTime();
                duration=(endTime-start);
                exec=duration/1000000;
                sortieProfiling.print("end iteration: "+exec);
                sortieProfiling.println();
                sortieProfiling.println();
               
            } while (count < generations);
        } catch (IOException ex) {
            Logger.getLogger(GeneticPenalty_Static.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            sortie.flush();
            sortieSynthese.flush();
            sortieProfiling.flush();
            sortieAnalytics.flush();
            sortie.close();
            sortieSynthese.close();
            sortieProfiling.close();
            sortiePercent.flush();
            sortiePercent.close();
            sortieAnalytics.close();
        }
   }
   
   public Population repro(Population pop,int[][]Dn_eliigibility,int[][]C,double Dn[][][],double[][][]W,double[]CDH,double[][] Dn_closeness,PrintWriter sortieProfiling,String str)
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
               Logger.getLogger(GeneticPenalty_Static.class.getName()).log(Level.SEVERE, null, ex);
           }

          ii[i] = i;
          ff[i] = oldPop.getIndividual(i) ;
        }
        
        long endTime=System.nanoTime();
        long duration=(endTime-now);
        double exec=duration/1000000;
        sortieProfiling.print("end initialization: "+exec);
        sortieProfiling.println();
        
        /* bubble sort population */
        now=System.nanoTime();
        sorted =true;
        quickSort(ff, ii, 0, popSize-1);
        endTime=System.nanoTime();
        duration=(endTime-now);
        exec=duration/1000000;
        sortieProfiling.print("end sort: "+exec);
        sortieProfiling.println();

        
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
                Logger.getLogger(GeneticPenalty_Static.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        endTime=System.nanoTime();
        duration=(endTime-now);
        exec=duration/1000000;
        sortieProfiling.print("end replicate top nrep solutions: "+exec);
        sortieProfiling.println();
        
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
        
        //update fitness value with new lamdaC & lamdaD
        now=System.nanoTime();
        for(i=0;i<nrep;i++) 
             pop.getIndividual(i).fitness += (valueC*pop.getIndividual(i).penality_capacity)+(valueD*pop.getIndividual(i).penality_latency);
        
        endTime=System.nanoTime();
        duration=(endTime-now);
        exec=duration/1000000;
        sortieProfiling.print("end update fitness value with new lamdaC & lamdaD: "+exec);
        sortieProfiling.println();
        
        /* mate npop-nrep random pairs */
        now=System.nanoTime();
        
        if(str.equals("GC"))
             crossoverGenePerGene(oldPop, pop, Dn_eliigibility, C, Dn, CDH, Dn_closeness);
        if(str.equals("1P"))
             crossoverOnePoint(oldPop, pop, Dn_eliigibility, C, Dn,  CDH, Dn_closeness);
        if(str.equals("2P"))
             crossoverTwoPoints(oldPop, pop, Dn_eliigibility, C, Dn,  CDH, Dn_closeness);
        if(str.equals("4P"))
             crossoverFourPoints(oldPop, pop, Dn_eliigibility, C, Dn, CDH, Dn_closeness);
        
        endTime=System.nanoTime();
        duration=(endTime-now);
        exec=duration/1000000;
        sortieProfiling.print("end crossover: "+exec);
        sortieProfiling.println();
       
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
//                    if(!server.contains(sv))
//                    {
//                        pop.getIndividual(k).pE+=Dn_closeness[j][sv];
//                    }
                }
          }
            endTime=System.nanoTime();
            duration=(endTime-now);
            exec=duration/1000000;
            sortieProfiling.print("end mutation: "+exec);
            sortieProfiling.println();

            now=System.nanoTime();
            pop.setIndividual(k, offspring);
            eval(pop.getIndividual(k),C,Dn,Dn_closeness,CDH);  
        }
        endTime=System.nanoTime();
        duration=(endTime-now);
        exec=duration/1000000;
        sortieProfiling.print("end evaluation: "+exec);
        sortieProfiling.println();
        
//        System.out.println("_____MUTATION_______");
//        pop.display(nrep);
//        System.out.println("pop: "+pop);
        return pop;
   }
   
   public void crossoverGenePerGene(Population oldPop,Population pop,int[][]Dn_eliigibility,int[][]C,double Dn[][][],double[]CDH,double[][] Dn_closeness)
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
                  Logger.getLogger(GeneticPenalty_Static.class.getName()).log(Level.SEVERE, null, ex);
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
//  System.out.println("____________During_____CROSSOVER_______________"+i);
//        System.out.println("");System.out.println("");System.out.println("");
//        pop.display(nrep);
          pop.setIndividual(i, offspring1);
          pop.setIndividual((i+1), offspring2);
         
          eval(pop.getIndividual(i),C,Dn,Dn_closeness,CDH); eval(pop.getIndividual(i+1),C,Dn,Dn_closeness,CDH);
//          if (pop.getIndividual(i+1).fitness < pop.getIndividual(i).fitness)
//          {
//              pop.setIndividual(i, (Chromosome)SerializationUtils.clone(pop.getIndividual(i+1)));
//          }

          i=i+2;
        }
   }
   
    public void crossoverTwoPoints(Population oldPop,Population pop,int[][]Dn_eliigibility,int[][]C,double Dn[][][],double[]CDH,double[][] Dn_closeness)
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
//           Chromosome offs[]= replicate(sequences, set, offspring1, offspring2, k, j, oldPop);
//           offspring1=offs[0];
//           offspring2=offs[1];

            
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
          eval(pop.getIndividual(i),C,Dn,Dn_closeness,CDH); eval(pop.getIndividual(i+1),C,Dn,Dn_closeness,CDH);
           i=i+2;
        }
   }
    
    public void crossoverFourPoints(Population oldPop,Population pop,int[][]Dn_eliigibility,int[][]C,double Dn[][][],double[]CDH,double[][] Dn_closeness)
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
          eval(pop.getIndividual(i),C,Dn,Dn_closeness,CDH); eval(pop.getIndividual(i+1),C,Dn,Dn_closeness,CDH);
           i=i+2;
        }
   }
    
     public void crossoverOnePoint(Population oldPop,Population pop,int[][]Dn_eliigibility,int[][]C,double Dn[][][],double[]CDH,double[][] Dn_closeness)
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
          eval(pop.getIndividual(i),C,Dn,Dn_closeness,CDH); eval(pop.getIndividual(i+1),C,Dn,Dn_closeness,CDH);
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
                           Logger.getLogger(GeneticPenalty_Static.class.getName()).log(Level.SEVERE, null, ex);
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
   
   void eval(Chromosome chr,int[][]C,double Dn[][][],double [][]Dn_closeness,double[]CDH)
    { 
        int i,j; 
        double px=0.0, pe=0.0;
        int use[]=new int[servers]; 
        int demandVCPU[]=new int[servers]; 
        for (i=0;i<sets;i++) 
        {
            use[chr.groups[i].fognode] += (double)chr.groups[i].sizeItem; 
            demandVCPU[chr.groups[i].fognode] +=Math.round((double)chr.groups[i].sizeItem/1000);
            pe+=(Dn_closeness[i][chr.groups[i].fognode])*(Dn_closeness[i][chr.groups[i].fognode]);
        }
          

        double firstPart=0.0;
        for (j=0;j<servers;j++)
        {
            firstPart+=demandVCPU[j]*CDH[j];
            if (use[j] > C[j][0]) 
                px += (use[j]-C[j][0])*(use[j]-C[j][0]);  //*2
        }
        
               
        chr.penality_capacity = px;
        chr.penality_latency=pe;
        chr.objectiveFunc=firstPart;
        chr.fitness = (chr.objectiveFunc+ (lamdaC*px)+ (lamdaD*pe)); 

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
 
    
      public static List<Set>itemSet(Chromosome crh, int g)
    {
        Set sets[]=crh.groups;
        List<Set>positions=new ArrayList<>();
        for(int s=0;s<sets.length;s++)
        {
            if(sets[s].fognode==g)
            {
                positions.add(sets[s]);
            }
        }
        return positions;
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
       for(int s=0;s<chr.groups.length;s++)
       {
           double pr=calculateProcessing(W, s);//calculateProcessing(W, C, s,chr.groups[s].fognode);//,chr.capRel,count);
           for(int p=0;p<W[s].length;p++)
           {
               double delay=Dn[s][p][chr.groups[s].fognode]+pr;
               sortie.print(delay);
               sortie.println();
           }
       }
      sortie.flush();
      sortie.close();
   }  
     
     public void displayPlayerSets(Chromosome chr,double W[][][],int C[][],double Dn[][][],int Dmax,String filename) throws IOException
   {
       
       int count=0;
       
       for(int s=0;s<chr.groups.length;s++)
       {    
           System.out.println("Set: "+(s+1)+" placed on "+(chr.groups[s].fognode+1));
            double pr=calculateProcessing(W, s);//calculateProcessing(W, C, s,chr.groups[s].fognode);//,chr.capRel,countT);
           for(int p=0;p<nbOfPlayersPerSet;p++)
           {
               double delay=Math.round(Dn[s][p][chr.groups[s].fognode]+pr);
//               System.out.println("Player: "+p+" processing: "+pr+" network: "+Dn[s][p][chr.groups[s].fognode]+" violation: "+((Dmax-delay)<0?Math.abs(Dmax-delay):0)+" "+delay); 

              
                if(Dmax-delay<0)
                   count++;
           }
//           System.out.println("Number of violated players: "+count);
       }
   }
     
     public boolean isValid(Chromosome chr)
    {
       for(int i=0;i<chr.groups.length;i++)
           if(chr.groups[i].fognode==-1)
               return false;
       return true;
    }
     
//      public void sequentialInteger(int arr[],int ii[])
//        {
//            boolean sorted = true;
//            while(sorted)
//            {
//              sorted = false;
//              for(int i=0;i<ii.length-1;i++)
//              {
//                if (arr[i]>arr[i+1])
//                {
//                    int z = arr[i]; int zz=ii[i];
//                    arr[i]=arr[i+1];ii[i]=ii[i+1];
//                    arr[i+1]=z;ii[i+1]=zz; 
//                    sorted = true;
//                }
//              }
//            }
//        }
     
      public static void sequentialInteger(int arr[],int ii[])
        {
            boolean sorted = true;
            while(sorted)
            {
              sorted = false;
              for(int i=0;i<ii.length-1;i++)
              {
                if ((arr[i]>arr[i+1]&& arr[i+1]!=0) || (arr[i]<arr[i+1]&& arr[i]==0))
                {
                    int z = arr[i]; int zz=ii[i];
                    arr[i]=arr[i+1];ii[i]=ii[i+1];
                    arr[i+1]=z;ii[i+1]=zz; 
                    sorted = true;
                }
              }
            }
        }
      
       static void sequentialIntegerHEAP(Chromosome arr[],int[] ii)
    { 
        int n = arr.length; 
        // Build heap (rearrange array) 
        for (int i = n / 2 - 1; i >= 0; i--) 
            heapifyHeap(arr, n, i, ii); 
  
        // One by one extract an element from heap 
        for (int i=n-1; i>=0; i--) 
        { 
            // Move current root to end 
            Chromosome temp=arr[0];int zz=ii[0];
            arr[0]=arr[i];
            ii[0]= ii[i];
            arr[i]=temp;
            ii[i]=zz;
    
            // call max heapify on the reduced heap 
            heapifyHeap(arr, i, 0, ii); 
        } 
    } 
    static void heapifyHeap(Chromosome arr[], int n, int i, int[]ii) 
    { 
        int largest = i; // Initialize largest as root 
        int l = 2*i + 1; // left = 2*i + 1 
        int r = 2*i + 2; // right = 2*i + 2 
   //System.out.println("l "+l+" r: "+r+" n "+n);
        // If left child is larger than root 
        if (l < n && arr[l].fitness>arr[largest].fitness) 
            largest = l; 
  
        // If right child is larger than largest so far 
        if (r < n && arr[r].fitness>arr[largest].fitness) 
            largest = r; 
  
        // If largest is not root 
        if (largest != i) 
        { 
           Chromosome temp=arr[i];int zz=ii[i];
            arr[i]=arr[i+1];
            ii[i]= ii[largest];
            arr[largest]=temp;
            ii[largest]=zz;
  
            // Recursively heapify the affected sub-tree 
            heapifyHeap(arr, n, largest, ii); 
        } 
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
        System.out.println("generations: "+generations);
        
         if (sets >= 3000) 
            generations = 7500;
        if(sets == 5000 ) 
            generations = 10000;
        System.out.println("generations: "+generations);
        
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
