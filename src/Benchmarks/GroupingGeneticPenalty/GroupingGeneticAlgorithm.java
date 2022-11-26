/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Benchmarks.GroupingGeneticPenalty;


import Benchmarks.GroupingGeneticPenalty.Chromosome;
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
import java.util.logging.Level;
import java.util.logging.Logger;



/**
 *
 * @author Amira Rayane Benamer
 */


public class GroupingGeneticAlgorithm {
    //Number of sets to place, nb of players per set, number of bins 
    public int sets; 
    public int nbOfPlayersPerSet;
    public int servers;
    public double tick;//ms
    //-------------
    
    public int popSize;

    public int tournamentSize;
    public double GmE=0.2;
    public int elitism;//10%
    //Factors----
    int cr1=1;
    int cr2=5;
    int dim=1;

  
//calculus 
   int sumWP[];
   List<List<Integer>> eligibleServers;

   
//genetic penalty
   public int milestone,ctrC,ctrD;
   double lamC=0.0,lamD=0.0, mult=4, cons=0.0,beta2=0.7;    public int m=2;    public int generations=5000;
   boolean pen1C,pen2C,pen1D,pen2D;
   Chromosome best;
   long executionFinal;
   public int bestgen;
    
//result
    double latency[][]=null;
    int [][]C_copy;
    static int [][]C_original;
    double[]latency_cdf_pdf;
    
    int C[][];
    int C_all[][];
    double W[][][];
    double Dn[][][];
    int Dmax;
    double CDH[];
    int[][] Dn_eligibility ;

     String path;
     
   public GroupingGeneticAlgorithm(String path,int sets, int nbOfPlayersPerSet, int servers,int C[][],int C_all[][], double W[][][],double Dn[][][],int Dmax,double CDH[],int[][] Dn_eligibility,double tick) 
    {
        this.path = path;
        this.sets = sets;
        this.nbOfPlayersPerSet = nbOfPlayersPerSet;
        this.servers = servers;
        this.C=C;
        this.Dmax=Dmax;
        this.W=W;
        this.CDH=CDH;
        this.tick=tick;
        this.Dn_eligibility=Dn_eligibility;
        try {
            GetData(new File("inputParametersGA.txt").getAbsolutePath());
            System.out.println("-----------");
        } catch (IOException ex) {
            Logger.getLogger(GroupingGeneticAlgorithm.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    
    
    public Chromosome GGA_Par(int C[][],int C_all[][], double W[][][],double Dn[][][],double CDH[],int[][] Dn_eligibility,double[][] Dn_closeness, String str,int id,int threshold,int rerun

    ) throws IOException, InterruptedException
    {
        

        int generation=0;
        double pci=0.999;double pcf=0.00001;
        double pmi=0.00001;double pmf=0.999;
        int round=1;

        popSize=sets*m;
        sumWP=new int[sets];
        for (int s=0;s<sets;s++)
        {
            double cpu=0;
            for(int p=0;p<nbOfPlayersPerSet;p++)
            {
                cpu=cpu+W[s][p][0];
            }

            sumWP[s]=(int)(cpu/tick);
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
        
        elitism=(int)(popSize*0.05);
        tournamentSize=(int)(popSize*0.05);

        System.out.println("Elitism: "+elitism);
        System.out.println("Start");
        long startTime=System.nanoTime();
        Instant start=Instant.now();
        //**create population
        Population population= populate(C, W, Dn, Dmax, Dn_eligibility, popSize, CDH, Dn_closeness);
       //**evaluate population
       // evaluate(population, C, W, Dn, Dmax, CDH,Dn_eligibility,Dn_closeness,true,elitism);//,sol,BW,VMS,cummulativeCost,costDeadline,costResources);
        pen1C = (population.getIndividual(0).capV==0.0);
        pen1D = (population.getIndividual(0).delV==0.0);
        milestone = sets/2;
        best=new Chromosome(sets);

       while(generation<=generations)
       {


            try {
                pen2C = (population.getIndividual(0).capV==0.0);
                pen2D = (population.getIndividual(0).delV==0.0);
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
                 
                 
                //**Crossover
                double probCross=crossOverProbability(pci,pcf, generation, generations);
                population= crossover(population, probCross,C, CDH, Dn,W,Dn_eligibility,Dn_closeness,elitism,tournamentSize);

                
                //**Mutation
                double probMut=mutationProbability(pmi,pmf, generation, generations);
                population=MIS_swap(population, C, W,Dn,CDH, probMut, Dn_eligibility, Dn_closeness, elitism);


//                evaluate(population, C, W, Dn, Dmax, CDH,Dn_eligibility,Dn_closeness,false,elitism);//,sol,BW,VMS,cummulativeCost,costDeadline,costResources);
                
                if (population.getIndividual(0).capV == 0.0 && (population.getIndividual(0).delV <=best.delV || best.flag==-1) && population.getIndividual(0).fitness<best.fitness   )
                {
                    double percent=0.0;
                    if(best.fitness!=Double.MAX_VALUE)
                        percent=((best.fitness-population.getIndividual(0).fitness)/best.fitness)*100;
                    try 
                    {
                        best=(Chromosome)population.getIndividual(0).clone();
                    }
                    catch (CloneNotSupportedException ex) 
                    {
                        Logger.getLogger(GroupingGeneticAlgorithm.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    best.flag=1;
                    bestgen=generation;
                    best.display();
//                    sortiPercente.print(percent);
//                    sortiPercente.println();
                    System.out.println("best Fitness: "+best.fitness+" percent: "+percent+" penCapacity= "+best.capV+" penCloseness= "+best.delV+" bestgen "+generation);
//                     System.out.println("");
//                     int sum=0;
//                    for(Server server:best.groups)
//                    {
//                        sum+=best.C_self[server.serverID][0];
//                        System.out.println("-server "+server.serverID+" hosts: "+best.C_self[server.serverID][0]+" filled: "+server.filled);
//                    }
//                    System.out.println("Total: "+sum);
                }
                
                generation++;
            } catch (CloneNotSupportedException ex) {
                Logger.getLogger(GroupingGeneticAlgorithm.class.getName()).log(Level.SEVERE, null, ex);
            }
       }
       
       long endTime=System.nanoTime();
       long duration=(endTime-startTime);
       Instant end=Instant.now();
       System.out.println("____________");
       Duration interval = Duration.between(start, end);
       executionFinal= interval.getSeconds();
 
//       sortiPercente.flush();
//       sortiPercente.close();
       
        System.out.println("Execution time in seconds: " +
                               executionFinal);
        System.out.println("************Solution**************");
      //**Display & Save 
      C_copy=new int[servers][1];
      for(int e=0;e<servers;e++)
          C_copy[e][0]=C[e][0];
        updateC(best, C_copy, W, Dn, Dmax);
        System.out.println(" C_copy: "+Arrays.deepToString(C_copy));
        best.display();
//        displayPlayerSets(best, W, C, Dn, Dmax, "");
        
        best.sumWP=sumWP;
        best.C=C;

         writeSolution(best, id,str,rerun,C,threshold);
         writeSolutionOnOutputFiles(id, rerun,str,C,threshold);
        writeLatencyDistribution(best, id, str, threshold, rerun, W, C, Dn, Dmax);
      
      return population.getIndividual(0);
    }
    
     void writeSolution(Chromosome best,int id,String str,int rerun,int C[][],int threshold) throws IOException
   {
       PrintWriter sortieSolution = null;
        PrintWriter sortieServerData = null;
        PrintWriter sortieActiveServers = null;
        sortieSolution = new PrintWriter(new FileWriter(path+"/Solution/"+sets+"/"+sets+"_"+id+"_"+str+"_"+rerun+"_"+threshold+"_"+C[0][0]+"vcpu.txt"));
        sortieServerData = new PrintWriter(new FileWriter(path+"/ServerData/"+sets+"/"+sets+"_"+id+"_"+str+"_"+rerun+"_"+threshold+"_"+C[0][0]+"vcpu.txt"));
        sortieActiveServers = new PrintWriter(new FileWriter(path+"/ActiveServers/"+sets+"/"+sets+"_"+id+"_"+str+"_"+rerun+"_"+threshold+"_"+C[0][0]+"vcpu.txt"));
      sortieSolution.println();
        for(int s=0;s<sets;s++)
        {
            sortieSolution.print(" "+best.items[s].hostBin);
            sortieSolution.println();
        }
        
        
        best.serverCapacityUsageCalculus();
        
        for(int e=0;e<best.activeServers;e++)
        {
            sortieServerData.print( best.serversData.get(e));
            sortieServerData.println();
            
            sortieActiveServers.print( best.used.get(e));
            sortieActiveServers.println();
        }
        sortieSolution.flush();
        sortieSolution.close();
        sortieServerData.flush();
        sortieServerData.close();
        
        sortieActiveServers.flush();
        sortieActiveServers.close();
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
       sortieViolationCapr.print(best.capV);
       sortieViolationCapr.println();
       sortieViolationDead.print(best.delV);
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
       void writeLatencyDistribution(Chromosome chr,int id,String str,int threshold,int rerun,double W[][][],int C[][],double Dn[][][],int Dmax) throws IOException
   {
       String filename=path+"/CDF/"+sets+"/"+str+" Output"+sets+"_"+generations+"_"+id+"_"+threshold+"_"+rerun+"_"+C[0][0]+"vcpu.txt";
            resume(chr, W, C, Dn, Dmax,filename);

   }
    
    public  Population crossover(Population pop, double probCross, int [][]C,double []CDH,double[][][]Dn,double [][][]W, int[][]Dn_eligibility,double[][]Dn_closeness,int elitism,int tournament) throws CloneNotSupportedException
    {
       
       double valueC=1.0;
        if (ctrC == milestone)
        {

          if (pen1C)
          {
            lamC /= (beta2*mult);
            valueC=lamC*(1-beta2*mult);
          }
          else 
          {
            lamC *= mult;
            valueC=lamC-(lamC/mult);
          }
        }
        
        double valueD=1.0;
        if (ctrD == milestone)
        {
          if (pen1D)
          {
            lamD /= (beta2*mult);
            valueD=lamD*(1-beta2*mult);
          }
          else 
          {
            lamD *= mult;
            valueD=lamD-(lamD/mult);
          }
        } 
        pop.quickSort(pop, 0, popSize-1);
        Population newPop=new Population(pop.size(),sets,C,W);
        //update fitness value with new lamdaC & lamdaD
        for(int i=0;i<elitism;i++) 
        {
            newPop.setIndividual(i, (Chromosome)pop.getIndividual(i).clone());
            newPop.getIndividual(i).fitness += (valueC*pop.getIndividual(i).capV)+(valueD*pop.getIndividual(i).delV);
        }
        

        Random rand=new Random();
        int individual=0;
        while(individual<pop.size()-1)
        {
           
             Chromosome crh2=pop.getIndividual(individual);
             List<Server>parent2=crh2.groups;

            if(probCross>Math.random() && individual>elitism )//Math.random()
            {
               
                //**create offspring
                Chromosome offspring1=new Chromosome(sets);
                Chromosome offspring2=new Chromosome(sets);
                offspring1.C_self=new int[C.length][2];
                offspring2.C_self=new int[C.length][2];
                
                Chromosome crh1= selectParentTourmant(pop,C,W,tournament);
                 List<Server>parent1=crh1.groups;
                 
                //crossing sites
                int crossingSite1_1=rand.nextInt(parent1.size()); 
                int crossingSite1_2=rand.nextInt(parent1.size());
                    if(crossingSite1_2<crossingSite1_1) 
                    {
                        int temp=crossingSite1_1;
                        crossingSite1_1=crossingSite1_2;
                        crossingSite1_2=temp;
                    }

                 
                int crossingSite2_1=rand.nextInt(parent2.size());
                int crossingSite2_2=rand.nextInt(parent2.size());
                 if(crossingSite2_2<crossingSite2_1) 
                    {
                        int temp=crossingSite2_1;
                        crossingSite2_1=crossingSite2_2;
                        crossingSite2_2=temp;
                    }
                 
//                 System.out.println("Parent 1: ");
//                 crh1.display();
//                 
//                 System.out.println("Parent 2: ");
//                 crh2.display();
//                
//                System.out.println("Crossing site for parent 1: "+crossingSite1_1+" and "+crossingSite1_2);
//                System.out.println("Crossing site for parent 2: "+crossingSite2_1+" and "+crossingSite2_2);

                 //**initialize an offsrping
                 for(int s=0;s<sets;s++)
                 {
                        Set s1=new Set();
                        s1.setId=s;
                        s1.setSizeItem(sumWP[s]);
                        offspring1.items[s]=s1;
                        
                        Set s2=new Set();
                        s2.setId=s;
                        s2.setSizeItem(sumWP[s]);
                        offspring2.items[s]=s2;
                 }
                 
                for(int e=0;e<C.length;e++)
                {
                    offspring1.C_self[e][0]=0;
                    offspring1.C_self[e][1]=C[e][0];
                    offspring2.C_self[e][0]=0;
                    offspring2.C_self[e][1]=C[e][0];
                }
                
                //Insert the elements belonging to the selected groups of the first individual into the offspring

                for(int el=crossingSite1_1;el<=crossingSite1_2;el++)
                {
                    Server copy=new Server(parent1.get(el).serverID, parent1.get(el).capacity); 
                    for(int s=0;s<crh1.items.length;s++)
                    {

                        if(parent1.get(el).serverID==crh1.items[s].hostBin.serverID)
                        {
                            offspring1.items[s].hostBin=copy;
                            offspring1.C_self[parent1.get(el).serverID][0]+=Math.round((double)offspring1.items[s].sizeItem/1000);
                            copy.addElement(offspring1.items[s]);
                        }
                    }
                     offspring1.groups.add(copy);
                }
                
                
                for(int el=crossingSite2_1;el<=crossingSite2_2;el++)
                {
                    Server copy=new Server(parent2.get(el).serverID, parent2.get(el).capacity); 
                    for(int s=0;s<crh2.items.length;s++)
                    {
                        if(parent2.get(el).serverID==crh2.items[s].hostBin.serverID)
                        {
                            offspring2.items[s].hostBin=copy;
                            offspring2.C_self[parent2.get(el).serverID][0]+=Math.round((double)offspring2.items[s].sizeItem/1000);
                            copy.addElement(offspring2.items[s]);
                        }
                    }
                     offspring2.groups.add(copy);
                }
                
                
                
                
//                display offspring after the first crossover iteration
//                System.out.println("display offspring after the first crossover iteration: ");
//                offspring1.display();
//                offspring2.display(); 
//                System.out.println("");
//                 for(Server server:offspring1.groups)
//                 {
//                     System.out.println("server "+server.serverID+" hosts: "+offspring1.C_self[server.serverID][0]+" filled: "+server.filled);
//                  }
//                  for(Server server:offspring2.groups)
//                 {
//                      System.out.println("-server "+server.serverID+" hosts: "+offspring2.C_self[server.serverID][0]+" filled: "+server.filled);
//                 }

                for(int el=0;el<crossingSite1_1;el++)
                {
                    Server copy; 
                    for(int s=0;s<crh1.items.length;s++)
                    {
         
                        if((parent1.get(el)==crh1.items[s].hostBin)  && offspring2.items[s].hostBin.serverID==-1)
                        {
                             boolean existed=contains(offspring2.groups,parent1.get(el).serverID);
                                if(!existed)
                                    copy=new Server(parent1.get(el).serverID, parent1.get(el).capacity); 
                                else
                                    copy=(Server)(get(offspring2.groups,parent1.get(el).serverID)).clone();

                            offspring2.C_self[parent1.get(el).serverID][0]+=Math.round((double)offspring2.items[s].sizeItem/1000);

                            
                            if(!existed)
                                offspring2.groups.add(copy); 
                            else
                                offspring2.groups.set(indexOf(offspring2.groups,parent1.get(el).serverID), copy);
                          
                            copy.addElement(offspring2.items[s]);
                            offspring2.items[s].hostBin=copy;
                        }
                    }
                  
                }


                 for(int el=crossingSite1_2;el<parent1.size();el++)
                {
                    Server copy;
                    for(int s=0;s<crh1.items.length;s++)
                    {
                        if(parent1.get(el)==crh1.items[s].hostBin  && offspring2.items[s].hostBin.serverID==-1)
                        {
                             boolean existed=contains(offspring2.groups,parent1.get(el).serverID);
                                if(!existed)
                                    copy=new Server(parent1.get(el).serverID, parent1.get(el).capacity); 
                                else
                                    copy=(Server)(get(offspring2.groups,parent1.get(el).serverID)).clone();

                            offspring2.C_self[parent1.get(el).serverID][0]+=Math.round((double)offspring2.items[s].sizeItem/1000);

                            
                            if(!existed)
                                offspring2.groups.add(copy); 
                            else
                                offspring2.groups.set(indexOf(offspring2.groups,parent1.get(el).serverID), copy);
                          
                            copy.addElement(offspring2.items[s]);
                            offspring2.items[s].hostBin=copy;
                        }
                    }
                }
           

                
                //Insert the elements belonging to the selected groups of the second individual into the offspring, if they have not been assigned by the first individual.

                for(int el=crossingSite2_1;el<=crossingSite2_2;el++)
                {
                    for(int s=0;s<offspring1.items.length;s++)
                    {
                        if(offspring1.items[s].hostBin.serverID==-1)
                        {            
                            if(crh2.items[s].hostBin==parent2.get(el))
                            {
                                Server copy;
                                boolean existed=contains(offspring1.groups,parent2.get(el).serverID);
                                if(!existed)
                                    copy=new Server(parent2.get(el).serverID, parent2.get(el).capacity); 
                                else
                                    copy=(Server)(get(offspring1.groups,parent2.get(el).serverID)).clone();
                                copy.addElement(crh2.items[s]);
                                offspring1.C_self[parent2.get(el).serverID][0]+=Math.round((double)offspring1.items[s].sizeItem/1000);
                                if(!existed)
                                   offspring1.groups.add(copy); 
                                else
                                {
                                    offspring1.groups.set(indexOf(offspring1.groups,parent2.get(el).serverID), copy);
                          
                                }
                                offspring1.items[s].hostBin=copy;
                            }
                        }
                        
                    }
                }
                    

               
                
             

                
                List<Set>notAssignedSetPositions=new ArrayList<>();
                for(int s=0;s<offspring1.items.length;s++)
                {
                    if(offspring1.items[s].hostBin.serverID==-1)
                        notAssignedSetPositions.add(offspring1.items[s]);
                }
                
//                int[][]capMatrix=capCompare(offspring1,C);//-----------reduceTime
                if(!notAssignedSetPositions.isEmpty())
                {
                    //from current groups, uniformly, select one multiplayer group 
                    for(int s=0;s<notAssignedSetPositions.size();s++)
                    {
                        Set set=notAssignedSetPositions.get(s);
                        //repair
                        Server server=complete(set.setId, offspring1.groups, offspring1, Dn_eligibility, C);
                        offspring1.C_self[server.serverID][0]+=Math.round((double)offspring1.items[set.setId].sizeItem/1000);
                        server.addElement(set);

                        if(!contains(offspring1.groups,server.serverID))
                               offspring1.groups.add(server); 
                         else
                               offspring1.groups.set(indexOf(offspring1.groups,server.serverID), server);
//                        if(s+1<notAssignedSetPositions.size())-----------------reduceTime
//                            capMatrix=capCompare(offspring1,C);
                        set.hostBin=server;
                        offspring1.items[set.setId]=set; 

                    }
                } 
                                //display offspring after the first crossover iteration
//                System.out.println("display offspring1 after complete iteration: ");
//                offspring1.display();
////                System.out.println("");
//                for(Server server:offspring1.groups)
//                 {
//                     System.out.println("server "+server.serverID+" hosts: "+offspring1.C_self[server.serverID][0]+" filled: "+server.filled);
//                   }

//               
                
                    //___
                notAssignedSetPositions=new ArrayList<>();
                for(int s=0;s<offspring2.items.length;s++)
                {
                    if(offspring2.items[s].hostBin.serverID==-1)
                        notAssignedSetPositions.add(offspring2.items[s]);
                }
                
//                capMatrix=capCompare(offspring2,C);---------------reduceTime
                if(!notAssignedSetPositions.isEmpty())
                {
                    //from current groups, uniformly, select one multiplayer group 
                    for(int s=0;s<notAssignedSetPositions.size();s++)
                    {
                        Set set=notAssignedSetPositions.get(s);
                        //repair
                        Server server=complete(set.setId, offspring2.groups, offspring2, Dn_eligibility, C);
                        offspring2.C_self[server.serverID][0]+=Math.round((double)offspring2.items[set.setId].sizeItem/1000);
                        server.addElement(set);
                        if(!contains(offspring2.groups,server.serverID))
                               offspring2.groups.add(server); 
                         else
                               offspring2.groups.set(indexOf(offspring2.groups,server.serverID), server);

                        set.hostBin=server;
                        offspring2.items[set.setId]=set;   
                    }
                }

                        newPop.setIndividual(individual, offspring1);
                        newPop.setIndividual(individual+1, offspring2);



                                    
            }
            else
            {
                //add individual to the newPop without applying crosover
                newPop.setIndividual(individual, crh2);
                newPop.setIndividual(individual+1, pop.getIndividual(individual+1));
            }
            
            fitness(newPop.getIndividual(individual), C, W, Dn, Dmax, CDH, Dn_closeness);
            fitness(newPop.getIndividual(individual+1), C, W, Dn, Dmax, CDH, Dn_closeness);
            individual=individual+2;
        }

        return newPop;
    }  
    
     

      
    
    public  void fitness(Chromosome chr,int C[][], double W[][][],double Dn[][][],int Dmax,double CDH[],double[][]Dn_closeness)//,List<Integer> sol,int[][]BW,int[]VMS,double cummulativeCost,double costDeadline,double costResources)
    {
       int i,j; 
        double capV=0.0, delV=0.0 ;
        double serverUsage=0.0;
        List<Server>groups=chr.groups;

        int sum=0;
        for (i=0;i<groups.size();i++) 
        {
            double delVT=0.0,delCT=0.0;
            serverUsage +=groups.get(i).vCPU*CDH[groups.get(i).serverID];

            if(groups.get(i).filled>groups.get(i).capacity)
            {
                capV+=(groups.get(i).filled-groups.get(i).capacity)*(groups.get(i).filled-groups.get(i).capacity);
                delCT+=(groups.get(i).filled-groups.get(i).capacity);
            }
            List<Set>multiplayerSets=groups.get(i).sets;
            for(int s=0;s<multiplayerSets.size();s++)
            {
                delV+=(Dn_closeness[multiplayerSets.get(s).setId][groups.get(i).serverID])*(Dn_closeness[multiplayerSets.get(s).setId][groups.get(i).serverID]);
                delVT+=(Dn_closeness[multiplayerSets.get(s).setId][groups.get(i).serverID]);
            }
            groups.get(i).delV=delVT;
            groups.get(i).delC=delCT;
//            System.out.println("delV: "+delVT+" delC: "+delCT);
        }
  
       

        chr.costFunc=(capV+delV);
        chr.capV=capV;
        chr.delV=delV;
        chr.objectiveFunc=serverUsage;
        
        chr.fitness = (chr.objectiveFunc+ (lamC*capV)+ (lamD*delV)); 
        chr.violated=delV+capV;
    
    }
    

    public Population MIS_swap(Population pop,int C[][], double W[][][],double [][][]Dn,double[]CDH,double probM,int[][]Dn_eligibility,double[][]Dn_closeness,int elitism) throws CloneNotSupportedException
    {
        Population newPop=new Population(pop.size(), sets,C,W);

        for(int individual=0;individual<pop.size();individual++)
        {
             Chromosome chr = (Chromosome)pop.getIndividual(individual).clone();
             if(probM>Math.random() && individual>elitism)
                {
                   chr=create(C,W, Dn, Dmax, CDH, Dn_closeness);
                    
                }
                  
                newPop.setIndividual(individual, chr);
        }
        
        return newPop;
    }
    
    
    
    
    
   
    double calculateProcessing(double [][][]W,int set)
    {
        int VMcpu=(int)((W[set][0][0]*nbOfPlayersPerSet)/tick);
        if(VMcpu<1000)
            VMcpu=1000;
        if(VMcpu>1000 && VMcpu<2000 )
            VMcpu=2000;
        return (((W[set][0][0]))/(VMcpu))*1000;
    }
    
    
    Integer indexOf(List<Server>groups,int index)
    {
        for(int i=0;i<groups.size();i++)
        {
            if(groups.get(i).serverID==index)
                return i;
        }
        return -1;
    }
    Integer indexOfSet(List<Set>sets,int index)
    {
        for(int i=0;i<sets.size();i++)
        {
            if(sets.get(i).setId==index)
                return i;
        }
        return -1;
    }
     Server complete(int set, List<Server>currentServersS, Chromosome offspring, int[][]Dn_eliigibility,int[][]C)
    {
            Random rand=new Random();
            double uniform;
            Server server=new Server(-1, 0);
            boolean bool=false;
            
            List<Integer>currentServers=new ArrayList<Integer>();
            for(int e=0;e<currentServersS.size();e++)
                currentServers.add(currentServersS.get(e).serverID);
            List<Integer>eligibleServersSet=eligibleServers.get(set);
            List<Integer>eligibleNotPresent=new ArrayList<Integer>();
            eligibleNotPresent.addAll(eligibleServersSet);
        
            List<Integer>eligibleCurrentServers=new ArrayList<Integer>();
            
            eligibleCurrentServers.addAll(eligibleServersSet);
            eligibleCurrentServers.retainAll(currentServers);
            eligibleNotPresent.removeAll(eligibleCurrentServers);
            
            if(!eligibleCurrentServers.isEmpty())
            {
                //select the less fittest server -->high available
                int [][]C_released=capReleased(offspring,eligibleCurrentServers, C,set);  
                List<Integer>ii=new ArrayList<Integer>();
                for(int i=0;i<eligibleCurrentServers.size();i++)
                    ii.add(i);

                
                for(int i=0;i<eligibleCurrentServers.size();i++)
                {
                    if(C_released[ii.get(i)][0]>=0)
                        {
                            server=get(currentServersS, eligibleCurrentServers.get(ii.get(i)));
                            bool=true;
    //                        System.out.println("placed on: "+server.serverID+" "+offspring.C_self[server.serverID][0]);
                            break;
                        }
                }

                
            }
            if(!eligibleNotPresent.isEmpty()&& !bool)
            {
                //select one eligible server, uniformly, it not already used so no excess of capacity
                uniform=Math.random();
                int pos=rand.nextInt(eligibleNotPresent.size());
                server=new Server(eligibleNotPresent.get((int)(pos*uniform)), C[eligibleNotPresent.get((int)(pos*uniform))][0]);
                bool=true;
//                System.out.println("Not present case: placed on: "+server.serverID+" "+offspring.C_self[server.serverID][0]);
            }
        
           if(!bool)
            {
                //the group is far enough. So, just select one available server uniformly.
                uniform=Math.random();
                List<Integer>availServers=getAvailableServers(offspring,set,C);
//                System.out.println(" "+Arrays.deepToString(offspring.C_self));
                int pos=rand.nextInt(availServers.size());
                int index=availServers.get((int)(pos*uniform));
                server=get(currentServersS,index);
                if(server==null)
                    server=new Server(index, C[index][0]);
//                System.out.println("far enough: "+server+" "+offspring.C_self[server.serverID][0]);

            }
      
         return server;   
    }
    
     int[][] capReleased(Chromosome offspring,List<Integer>servers,int C[][],int setToPlace)
    {
        int [][]C_released=new int[servers.size()][1];
        for(int e=0;e<servers.size();e++)
        {
            double cpu;
            Server server=get(offspring.groups,servers.get(e));
            List<Set> items=server.getSets();
            try{
            cpu=items.get(0).sizeItem*items.size();
            cpu+=offspring.items[setToPlace].sizeItem;
            C_released[e][0]=(offspring.C_self[server.serverID][1])-(int)cpu;
            }
            catch(Exception ex)
            {
                offspring.display();
            }


        }
        return C_released;
    }
    
    
    public static List<Integer>getViolatedCapServers(Chromosome chr,int[][]capMatrix)
    {
        List<Integer>violated=new ArrayList<>();
        for(int e=0;e<chr.groups.size();e++)
        {
            if(capMatrix[e][0]>capMatrix[e][1])
                violated.add(chr.groups.get(e).serverID);
        }
        return violated;
    }
    
    public  List<Integer>getAvailableServers(Chromosome chr,int set,int [][]C)
    {
        List<Integer>available=new ArrayList<>();
        for(int e=0;e<chr.groups.size();e++)
        {
            int server=chr.groups.get(e).serverID;
            if((double)(chr.groups.get(e).filled+chr.items[set].sizeItem)<=chr.C_self[server][1])
            {
                available.add(server);
            }
        }
    
        for(int e=0;e<C.length;e++)
        {
            if(!contains(chr.groups,e))
                available.add(e);
        }
  
        return available;
    }
    
    public Double crossOverProbability(double pci, double pcf, int generation, int generations)
    {
         return pci- ((double)generation/generations)*(pci-pcf);
    }
     public Double mutationProbability(double pmi, double pmf, int generation, int generations)
    {
        return pmi+ ((double)generation/generations)*(pmf-pmi);
    }
    

     
    
     
    
     
    public Chromosome selectParentRoulette(Population population)
    {
        population.shuffle();
        // Get individuals
        Chromosome individuals[] = population.getIndividuals();
        // Spin roulette wheel
        double populationFitness = population.getPopulationFitness();
        double rouletteWheelPosition = Math.random() * populationFitness;
        // Find parent
        double spinWheel = 0;
        for (Chromosome individual : individuals) 
        {
            spinWheel += individual.fitness;
            if (spinWheel >= rouletteWheelPosition) 
            {
                return individual;
            }
        }
        return individuals[population.size() - 1];
    }
    
    public Chromosome selectParentTourmant(Population population,int C[][],double[][][]W,int tournamentSize) 
    {
        // Create tournament
        Population tournament = new Population(tournamentSize, sets,C,W);
        // Add random individuals to the tournament
        population.shuffle();
        for(int i=0;i<tournament.size();i++)
        {
            Chromosome tournamentIndividual = population.getIndividual(i);
            tournament.setIndividual(i, tournamentIndividual);
        }
        // Return the best
        tournament.quickSort(tournament, 0, tournamentSize-1);
        return tournament.getIndividual(0);
    }
    
    
    public Population populate(int C[][], double W[][][],double Dn[][][],int Dmax,int[][] Dn_eligibility,int popSize,double CDH[],double[][]Dn_closeness)
    {
        lamC=0.0005;  lamD=0.0005;
        System.out.println("popSize: "+popSize);

        Population pop=new Population(popSize,sets,C,W);

        for(int i=0;i<popSize;i++)
        {
            
            Chromosome crh=create(C, W, Dn, Dmax, CDH, Dn_closeness);
            pop.setIndividual(i, crh); 
        }
        
        return pop;
    }
    
    public Chromosome create(int [][]C,double [][][]W,double Dn[][][],int Dmax,double CDH[],double[][]Dn_closeness)
    {
        Chromosome crh=new Chromosome(sets);
        crh.C_self=new int[C.length][2];
            for(int e=0;e<C.length;e++)
            {
                crh.C_self[e][0]=0;
                crh.C_self[e][1]=C[e][0];
            }

             for( int s=0; s<sets;s++)
             {
                Set set=new Set();
                set.nbPlayers=nbOfPlayersPerSet;
                set.setSizeItem(sumWP[s]);
                set.setId=s;

                int index;
                List<Integer>eligibleServersSet=eligibleServers.get(s);

                if(Math.random()>GmE && !eligibleServersSet.isEmpty())//80%
                    index=eligibleServersSet.get(pick(eligibleServersSet.size()));
                else
                    index=pick(servers);

                
                Server server;

                boolean existed=true;
                    if(!contains(crh.groups,index))
                        {
                            existed=false;
                            server=new Server(index, C[index][0]);
                        }
                    else
                            server=get(crh.groups,index);
                set.hostBin=server;
                server.addElement(set);
                crh.C_self[index][0]+=Math.round((double)set.getSizeItem()/1000);
//                set.assignPlayer(W);
                if(!existed)
                     crh.groups.add(server);
                else
                     crh.groups.set(indexOf(crh.groups,server.serverID), server);
                crh.items[s]=set;
            }
             fitness(crh,C, W, Dn, Dmax, CDH, Dn_closeness);
             
             
             return crh;
    }
    
    public int pick(int n)  /* generate a pseudorandom integer number between 1 and n */
    {
        int p;
        p =(int)(Math.random()*n);
        return p;
    }
    
    public boolean contains(List<Server>groups,int index)
    {
        for(int s=0;s<groups.size();s++)
        {
            if(groups.get(s).serverID==index)
                return true;
        }
        return false;
    }
    public Server get(List<Server>groups,int index)
    {
        for(int s=0;s<groups.size();s++)
        {
            if(groups.get(s).serverID==index)
                return groups.get(s);
        }
        return null;
    }
    public Set getSet(List<Set>items,int index)
    {
        for(int s=0;s<items.size();s++)
        {
            if(items.get(s).setId==index)
                return items.get(s);
        }
        return null;
    }
    
     public void displayPlayerSets(Chromosome chr,double W[][][],int C[][],double Dn[][][],int Dmax,String filename) throws IOException
   {
       
       int count=0;
       
       for(int s=0;s<chr.items.length;s++)
       {    
           System.out.println("Set: "+(s+1)+" placed on "+(chr.items[s].hostBin.serverID+1));
           double pr=calculateProcessing(W, s);//calculateProcessing(W, C, s,chr.items[s].hostBin.serverID);//,chr.capRel,countT);
           for(int p=0;p<nbOfPlayersPerSet;p++)
           {
               double delay=Math.round(Dn[s][p][chr.items[s].hostBin.serverID]+pr);
               System.out.println("Player: "+p+" processing: "+pr+" network: "+Dn[s][p][chr.items[s].hostBin.serverID]+" violation: "+((Dmax-delay)<0?Math.abs(Dmax-delay):0)+" "+delay); 

              
                if(Dmax-delay<0)
                   count++;
           }
           System.out.println("Number of violated players: "+count);
       }
   }
   
    
      public void resume(Chromosome chr,double W[][][],int C[][],double Dn[][][],int Dmax,String filename) throws IOException
   {
       PrintWriter sortie= new PrintWriter(new FileWriter(filename));
       latency=new double[sets][nbOfPlayersPerSet];
       for(int s=0;s<chr.items.length;s++)
       {
           double pr=calculateProcessing(W, s);//,chr.capRel,count);
           for(int p=0;p<W[s].length;p++)
           {
               double delay=Dn[s][p][chr.items[s].hostBin.serverID]+pr;
               latency[s][p]=delay;
               sortie.print(delay);
               sortie.println();
           }
       }
      sortie.flush();
       sortie.close();
   } 
 
    //---------------Setters
    public void setNbOfPlayersPerSet(int nbOfPlayersPerSet) {
        this.nbOfPlayersPerSet = nbOfPlayersPerSet;
    }

    public void setServers(int servers) {
        this.servers = servers;
    }

    public void setSets(int sets) {
        this.sets = sets;
    }

    //------------Getters
    public int getNbOfPlayersPerSet() {
        return nbOfPlayersPerSet;
    }

    public int getServers() {
        return servers;
    }

    public int getSets() {
        return sets;
    }
   //_________
  
      //**dynamic
      public double migrationCost(List<Integer> sol, Chromosome chr, int[][]BW,int[]VMS)
      {

//          System.out.println("--------------Migration------");
           Set[] items=chr.items;
           double MC=0.0;
           for(int s=0;s<sol.size();s++)
           {
               if((items[s].hostBin.serverID)!=sol.get(s))
               {
//                   System.out.println("set: "+s);
//                   System.out.println("already hosted on: "+sol.get(s)+" migrated to: "+items[s].hostBin);
//                   System.out.println("VM size: "+VMS[s]+" and bandwidth: "+BW[items[s].hostBin][sol.get(s)]+ " value: "+(VMS[s]/BW[items[s].hostBin][sol.get(s)]));
                   double migrationTime=(VMS[s]/BW[items[s].hostBin.serverID][sol.get(s)]);
                   items[s].migrationTime=migrationTime;
                   MC=MC+migrationTime;
               }
           }
           return MC;
  
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
      
   

   boolean checkEligibility(Chromosome individual, int[][] Dn_eligibility) 
    {
        Set[]sets=individual.items;
        for(int s=0;s<sets.length;s++)
        {
            if(Dn_eligibility[sets[s].setId][sets[s].hostBin.serverID]==0)
                return false;
        }
        return true;
    }
   public void updateC(Chromosome chr,int C_copy[][], double W[][][],double Dn[][][],int Dmax)
    {
        List<Server>groups=chr.groups;

//        chr.display();
        for(int g=0;g<groups.size();g++)
        {
            List<Set>items= groups.get(g).getSets();
//            System.out.println("server: "+groups.get(g)+" placed: "+items.size());
            double cpu=0;int ram=0;
            for(int s=0;s<items.size();s++)
            {
                for(int p=0;p<nbOfPlayersPerSet;p++)//W[items.get(s)].length
                {
                    cpu+=W[items.get(s).setId][p][0];
                    //ram+=W[items.get(s)][p][1];memory
                }
            }
//            System.out.println("cpu: "+cpu);
            cpu=cpu/tick;
           C_copy[groups.get(g).serverID][0]= C_copy[groups.get(g).serverID][0]-(int)cpu; // && C[groups.get(g)][1]-ram >=0) memory
            
          
        }
        int total=0;
        for(int g=0;g<groups.size();g++)
        {
            List<Set>items= groups.get(g).getSets();
//            System.out.println("Server:  "+groups.get(g)+" nbr placed: "+items.size());
            total+=items.size();
        }
        System.out.println("total: "+total);
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
        double rep=in.nextDouble();
 
            
        got=s.next();
        in = new Scanner(got).useDelimiter("[^,0-9]+");
        double mut=in.nextDouble();
            
            
        got=s.next();
        in = new Scanner(got).useDelimiter("[^0-9]+");
        generations=in.nextInt();
        System.out.println("generations: "+generations);
        
        got=s.next();
        in = new Scanner(got).useDelimiter("[^,0-9]+");
        m=in.nextInt();
        System.out.println("m: "+m);
        if(sets >= 750)
            m = 1;
            
        s.close();
    }
}
