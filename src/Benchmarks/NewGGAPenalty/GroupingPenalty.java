package Benchmarks.NewGGAPenalty;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 *
 * @author ASUS
 */
public class GroupingPenalty 
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
    public int bestgen;
    long executionFinal;

    
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
    double lamC=0.0,lamD=0.0, mult, cons=0.0;
    boolean pen1C,pen2C,pen1D,pen2D;
    int ctrC,ctrD;
    
    int sumWP[];
    List<List<Integer>> eligibleServers;
    List<List<Integer>> eligibleSets;
//    List<Integer>allSets;
    Population pop;
    
    String path;
     public GroupingPenalty(String path,int sets, int nbOfPlayersPerSet, int servers,double tick,int Dmax) {
        this.path = path;
        this.sets = sets;
        this.nbOfPlayersPerSet = nbOfPlayersPerSet;
        this.servers = servers;
        this.tick=tick;
        this.Dmax=Dmax;
        try {
            GetData(new File("inputParametersGA.txt").getAbsolutePath());
        } catch (IOException ex) {
            Logger.getLogger(GroupingPenalty.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void main(int C[][],int C_all[][], double W[][][],double Dn[][][],double CDH[],int[][] Dn_eligibility,double[][] Dn_closeness, String str,int id,int threshold,int rerun)
    {
       
        
        popSize=sets*m;
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
        
        eligibleSets=new ArrayList<>();
        for(int i=0;i<servers;i++)
        {
            List<Integer>serversE=getEligibleSets(i,Dn_eligibility);
            eligibleSets.add(serversE);
        }

        
        pop=new Population(popSize,servers,sets,C,W);
        nrep = (int)(rep*popSize); if (nrep == 0) nrep++;
        nmut = (int)(mut*popSize); if (nmut == 0) nmut++;
        

        System.out.println("CONFIGURATION: ");
        System.out.println("Number of servers: "+servers+" Number of sets: "+sets);
       System.out.println("Start");
       Instant start=Instant.now();
       long startTime=System.nanoTime();
       setup(C,Dn_eligibility,sumWP,Dn,W,CDH,Dn_closeness); 
       
       genetic(pop, Dn_eligibility,C,Dn,W,CDH,Dn_closeness,startTime,str,id,threshold,rerun);
       Instant end=Instant.now();
       long endTime=System.nanoTime();
       long duration=(endTime-startTime);
       System.out.println("____________");
       Duration interval = Duration.between(start, end);
       executionFinal= interval.getSeconds();
       System.out.println("time execution metaheuristique: "+executionFinal+" s");
       
       System.out.println("************Solution**************");
        best.sumWP=sumWP;
        best.C=C;
        
      //**Display & Save 
        Integer items[]=best.display(id, str, rerun,threshold,path);
      try
      {
        writeSolutionOnOutputFiles(id, rerun,str,C,threshold);
        writeLatencyDistribution(items, id, str, threshold, rerun, W, C, Dn, Dmax);
      }
      catch (Exception e)
      {
          System.out.println("Oppppps");
          
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
       sortieViolationCapr.print(best.pen);
       sortieViolationCapr.println();
       sortieViolationDead.print(best.pE);
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
       void writeLatencyDistribution(Integer items[],int id,String str,int threshold,int rerun,double W[][][],int C[][],double Dn[][][],int Dmax) throws IOException
   {
       String filename=path+"/CDF/"+sets+"/"+str+" Output"+sets+"_"+generations+"_"+id+"_"+threshold+"_"+rerun+"_"+C[0][0]+"vcpu.txt";
         resume(items, W, C, Dn, Dmax,filename);

   }
    
       
       
    public void genetic(Population pop,int[][]Dn_eliigibility,int[][]C,double Dn[][][],double[][][]W,double[]CDH,double[][] Dn_closeness,long start,String str,int id,int threshold,int rerun)
   {
            int count=0;
            milestone = sets/2;
          
            do
            {
                count++;
                pop=repro(pop,Dn_eliigibility,C,Dn,W,CDH,Dn_closeness,str);

                pen2C = (pop.getIndividual(0).pen==0.0);
                pen2D = (pop.getIndividual(0).pE==0.0);

                
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

                 
                 
                if (pop.getIndividual(0).pen == 0.0 && (pop.getIndividual(0).pE <=best.pE || best.flag==-1) && pop.getIndividual(0).fitness<best.fitness)
                {
                    double percent=0.0;
                    if(best.fitness!=Double.MAX_VALUE)
                        percent=((best.fitness-pop.getIndividual(0).fitness)/best.fitness)*100;
                    try 
                    {
                        best=(Chromosome)pop.getIndividual(0).clone();
                        for(int e=0;e<servers;e++)
                            best.bins[e]=(Server)pop.getIndividual(0).bins[e].clone();
                    }
                    catch (CloneNotSupportedException ex) 
                    {
                        Logger.getLogger(GroupingPenalty.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    best.flag=1;
                    bestgen= count;
                    System.out.println("best Fitness: "+best.fitness+" percent: "+percent+" penCapacity= "+best.pen+" penCloseness= "+best.pE+" bestgen "+bestgen);
                    System.out.println("lamdaC: "+lamC+" lamdaD: "+lamD);  
                }
            } while (count < generations);
           
//        } catch (IOException ex) {
//            Logger.getLogger(GroupingPenalty.class.getName()).log(Level.SEVERE, null, ex);
//        } finally {
//            sortiPercente.flush();
//            sortiPercente.close();
//            sortie.flush();
//            sortieSynthese.flush();
//            sortieProfiling.flush();
//            sortie.close();
//            sortieSynthese.close();
//            sortieProfiling.close();
//        }
   }
   
    public Chromosome firstFit(int [][]C)
    {
        Chromosome chr=new Chromosome(servers, sets,C);
        for(int j=0;j<sets;j++)
        {
             List<Integer>eligibles=eligibleServers.get(j);
             boolean bool=false;

            for (int e=0;e<eligibles.size();e++)
            {
                int el=eligibles.get(e);
                if(chr.bins[el].filled+sumWP[j]<=C[el][0]  )
                {
                    Set set=new Set();
                    set.nbPlayers=nbOfPlayersPerSet;
                    set.setId=j;
                    set.setSizeItem(sumWP[j]);
                    chr.bins[el].addElement(set); 
                    bool=true;
                    break;
                }
            }
            if(!bool)
            {
                for (int e=0;e<servers;e++)
                {
                    if(chr.bins[e].filled+sumWP[j]<=C[e][0]  )
                    {
                        Set set=new Set();
                        set.nbPlayers=nbOfPlayersPerSet;
                        set.setId=j;
                        set.setSizeItem(sumWP[j]);
                        chr.bins[e].addElement(set); 
                        break;
                     }
                }
            }
           
        }
        return chr;
    }
    
    public Population setup(int C[][],int[][]Dn_eligibility,int[]sumWP,double Dn[][][],double[][][]W,double[]CDH,double[][] Dn_closeness)
   {
       System.out.println("popSize: "+popSize);
       int z,zD,j,cnt=0,cntD=0;
       ctrC=0;ctrD=0;
       lamC=0.0005;  lamD=0.0005;
       Chromosome chr;
        z=0;
        Chromosome first=firstFit(C);
         pop.setIndividual(0, first);
         eval(pop.getIndividual(0),C,Dn,Dn_closeness,W,CDH);


        for (int i=1;i<popSize;i++)
        {
               chr=new Chromosome(servers, sets,C);
               chr.sumWP=sumWP;
               for (j=0;j<sets;j++)
               {
                   Set set=new Set();
                   set.nbPlayers=nbOfPlayersPerSet;
                   set.setId=j;
                   set.setSizeItem(sumWP[j]);
                   
                   List<Integer>eligibleServersSet=eligibleServers.get(j);
                   int index;
                   if(Math.random()>GmE && !eligibleServersSet.isEmpty())//80%
                   {
                       index=eligibleServersSet.get(pick(eligibleServersSet.size()));
                   }
                   else
                   {
                       index=pick(servers);
                   }
                    chr.bins[index].addElement(set);
               }
               pop.setIndividual(i, chr);
               eval(pop.getIndividual(i),C,Dn,Dn_closeness,W,CDH);

        }
        pen1C = (pop.getIndividual(0).pen==0.0);
        pen1D = (pop.getIndividual(0).pE==0.0);
        best=new Chromosome(servers,sets,C);
   
        return pop;
   }
    
    
    
    public Population repro(Population pop,int[][]Dn_eliigibility,int[][]C,double Dn[][][],double[][][]W,double[]CDH,double[][] Dn_closeness,String str)
   {
       Population oldPop=new Population(popSize,servers,sets,C,W);
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
               Logger.getLogger(GroupingPenalty.class.getName()).log(Level.SEVERE, null, ex);
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
                Logger.getLogger(GroupingPenalty.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        endTime=System.nanoTime();
        duration=(endTime-now);
        exec=duration/1000000;
//        sortieProfiling.print("end replicate top nrep solutions: "+exec);
//        sortieProfiling.println();
        
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
//        System.out.println("lamC: "+lamC+" lamD: "+lamD);
        //update fitness value with new lamdaC & lamdaD
        now=System.nanoTime();
        for(i=0;i<nrep;i++) 
             pop.getIndividual(i).fitness += (valueC*pop.getIndividual(i).pen)+(valueD*pop.getIndividual(i).pE);
        
        endTime=System.nanoTime();
        duration=(endTime-now);
        exec=duration/1000000;
//        sortieProfiling.print("end update fitness value with new lamdaC & lamdaD: "+exec);
//        sortieProfiling.println();
        
        /* mate npop-nrep random pairs */
        now=System.nanoTime();
        
        crossoverGenePerGene(oldPop, pop, Dn_eliigibility, C, Dn, W, CDH, Dn_closeness);


        endTime=System.nanoTime();
        duration=(endTime-now);
        exec=duration/1000000;
//        sortieProfiling.print("end crossover: "+exec);
//        sortieProfiling.println();
       
        
        
        /* create mutations */
        now=System.nanoTime();
//        pop=mutation(Dn_closeness, Dn_eligibility, pop, sortieProfiling);
        for (i=0;i<nmut;i++)
        {
               k=popSize-nmut+i;
               Chromosome offspring=new Chromosome(servers, sets,C);
               pop.setIndividual(k, offspring);
               for (j=0;j<sets;j++)
               {
                   Set set=new Set();
                   set.nbPlayers=nbOfPlayersPerSet;
                   set.setId=j;
                   set.setSizeItem(sumWP[j]);
                   
                   List<Integer>eligibleServersSet=eligibleServers.get(j);
                   int index;
                   if(Math.random()>GmE && !eligibleServersSet.isEmpty())//80%
                   {
                       index=eligibleServersSet.get(pick(eligibleServersSet.size()));
                   }
                   else
                   {
                       index=pick(servers);
                   }
                    offspring.bins[index].addElement(set);
                   
               }

               endTime=System.nanoTime();
               duration=(endTime-now);
               exec=duration/1000000;
//            sortieProfiling.print("end mutation: "+exec);
//            sortieProfiling.println();

                now=System.nanoTime();
                pop.setIndividual(k, offspring);
                eval(pop.getIndividual(k),C,Dn,Dn_closeness,W,CDH);  
        }
        endTime=System.nanoTime();
        duration=(endTime-now);
        exec=duration/1000000;
//        sortieProfiling.print("end evaluation: "+exec);
//        sortieProfiling.println();
        

        return pop;
   }
   
    public void crossoverTwoPoints(Population oldPop,Population pop,int[][]Dn_eliigibility,int[][]C,double Dn[][][],int[][][]W,double[]CDH,double[][] Dn_closeness)
   { 
       int i = nrep,j,k;
        int stop = (popSize - nmut);

        while (i < stop) 
        {
          j = pick(popSize);
          k = pick(popSize);
          Chromosome offspring1,offspring2;
          
          offspring1=new Chromosome(servers,sets,C);
          offspring2 =new Chromosome(servers,sets,C);
       
          int point1=pick(servers);
          int point2=pick(servers);
          if(point2<point1)
          {
              int temp=point1;
              point1=point2;
              point2=temp;
          }
           int []sequences={0,point1,point2,sets};
           
           
        }
   }
    
    public void crossoverGenePerGene(Population oldPop,Population pop,int[][]Dn_eliigibility,int[][]C,double Dn[][][],double[][][]W,double[]CDH,double[][] Dn_closeness)
   {
        int i = nrep,j,k,n;
        int stop = (popSize - nmut);

        while (i < stop) 
        {
                Chromosome offspring1=new Chromosome(servers, sets, C);
                Chromosome offspring2=new Chromosome(servers, sets, C);
                j = pick(popSize);
                k = pick(popSize);
                 
                for(int e=0;e<servers;e++)
                {
                    n = pick(100);

                        if (n<=70)
                        {
                           offspring1.bins[e].sets.addAll(oldPop.getIndividual(j).bins[e].sets);
//                            for(int s=0;s<oldPop.getIndividual(j).bins[e].getSets().size();s++)
//                                offspring1.bins[e].sets.add(oldPop.getIndividual(j).bins[e].sets.get(s));

                           offspring2.bins[e].sets.addAll(oldPop.getIndividual(k).bins[e].sets);
//                            for(int s=0;s<oldPop.getIndividual(k).bins[e].getSets().size();s++)
//                                offspring2.bins[e].sets.add(oldPop.getIndividual(k).bins[e].sets.get(s));
                        }
                        else
                        {
                            offspring1.bins[e].sets.addAll(oldPop.getIndividual(k).bins[e].sets);
//                            for(int s=0;s<oldPop.getIndividual(k).bins[e].getSets().size();s++)
//                                offspring1.bins[e].sets.add(oldPop.getIndividual(k).bins[e].sets.get(s));
                            
                            offspring2.bins[e].sets.addAll(oldPop.getIndividual(j).bins[e].sets);
//                            for(int s=0;s<oldPop.getIndividual(j).bins[e].getSets().size();s++)
//                                offspring2.bins[e].sets.add(oldPop.getIndividual(j).bins[e].sets.get(s));
                        }
                }
                

                repair(offspring1); 
                pop.setIndividual(i, offspring1);

                repair(offspring2);
                pop.setIndividual((i+1), offspring2);

                eval(pop.getIndividual(i),C,Dn,Dn_closeness,W,CDH); 
                eval(pop.getIndividual(i+1),C,Dn,Dn_closeness,W,CDH);
            
                        
                 i=i+2;
        }

   }
    
    void repair(Chromosome offs)
    {
        List<Integer>placedSets=new ArrayList<>();
        //--remove
        for(int e=0;e<servers;e++)
        {

            List<Set>sets=offs.bins[e].getSets();
            int co=0;
            int vCPU=0;int filled=0;
            while(co<sets.size())
            {
//                System.out.println("sets size:--: "+sets.size()+" ----- "+co);
                Set elem=sets.get(co);
                if(!placedSets.contains(elem.setId))
                {
//                    System.out.println("not placed");
                     elem.hostBin=offs.bins[e].serverID;
                    placedSets.add(elem.setId);
                    vCPU+=Math.round((double)elem.getSizeItem()/1000);
                    filled+=elem.getSizeItem();
                    co++;
                }
                else
                {
//                    System.out.println("remove");
                    offs.bins[e].removeElement(elem);
                }
            }
           offs.bins[e].vCPU=vCPU;
           offs.bins[e].filled=filled;
        }

        
        //--add non placed
        List<Integer>notPlacedSet=new ArrayList<>();
        notPlacedSet = IntStream.rangeClosed(0, sets-1).boxed().collect(Collectors.toList());
//        notPlacedSet.addAll(allSets);
        notPlacedSet.removeAll(placedSets);
        


        for(int s=0;s<notPlacedSet.size();s++)
        {
            Set set=new Set();
            set.nbPlayers=nbOfPlayersPerSet;
            set.setId=notPlacedSet.get(s);
            set.setSizeItem(sumWP[notPlacedSet.get(s)]);
            List<Integer>eligibleServersSet=eligibleServers.get(notPlacedSet.get(s));
            int index;
            if(Math.random()>GmE && !eligibleServersSet.isEmpty())//80%
                 index=eligibleServersSet.get(pick(eligibleServersSet.size()));
               else
                   index=pick(servers);
 
            offs.bins[index].addElement(set);
        }  
    }
    
    Chromosome mutation(Chromosome chr)
    {
        int index;
        while(true)
        {
            index=pick(servers);
            if(!chr.bins[index].sets.isEmpty())
                break;
        }
        int setIn=eligibleSets.get(index).get(pick(eligibleSets.get(index).size()));
        int picked=pick(chr.bins[index].getSets().size());
        Set setOut=chr.bins[index].sets.get(picked);
        setOut.setId=setIn;
        chr.bins[index].sets.set(picked, setOut);
        return chr;
    }
    
    void eval(Chromosome chr,int[][]C,double Dn[][][],double [][]Dn_closeness,double[][][]W,double[]CDH)
    { 
        int i,j; 
        double px=0.0, pe=0.0;
        int use[]=new int[servers]; 
         double firstPart=0.0;

        for (i=0;i<servers;i++) 
        {
//            System.out.println(i+" server hosts: "+chr.bins[i].sets.size()+" vCPU: "+chr.bins[i].vCPU+" filled: "+chr.bins[i].filled);
            use[i] = chr.bins[i].filled; 
            firstPart+=chr.bins[i].vCPU*CDH[i];
//            System.out.println("vCPU: "+chr.bins[i].vCPU);
            if (use[i] > C[i][0]) 
                px += (use[i]-C[i][0])*(use[i]-C[i][0]); 
           for(int s=0;s<chr.bins[i].getSets().size();s++)
            {
                Set set=chr.bins[i].getSets().get(s);
                pe+=(Dn_closeness[set.setId][i])*(Dn_closeness[set.setId][i]);  
            }
        }
        

//        System.out.println("all: "+firstPart);

        
        chr.pen = px;
        chr.pE=pe;
        chr.objectiveFunc=firstPart;
        chr.fitness = (chr.objectiveFunc+ (lamC*px)+ (lamD*pe)); 
    } 
    
    
     public int pick(int n)  /* generate a pseudorandom integer number between 1 and n */
    {
        int p;

        p = (int) (Math.random()*n);
        return p;
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
     
    List<Integer> getEligibleSets(int server, int[][]Dn_eliigibility)
    {
        List<Integer>eligibleSets=new ArrayList<>();
         for(int set=0;set<sets;set++)
         {
            if(Dn_eliigibility[set][server]==1)
                 eligibleSets.add(set);
         }
        
        return eligibleSets;
    }
    
        public void resume(Integer []items,double W[][][],int C[][],double Dn[][][],int Dmax,String filename) throws IOException
   {
       PrintWriter sortie= new PrintWriter(new FileWriter(filename));
       latency=new double[sets][nbOfPlayersPerSet];
       for(int s=0;s<items.length;s++)
       {
           double pr=calculateProcessing(W, s);//,chr.capRel,count);
           for(int p=0;p<W[s].length;p++)
           {
               double delay=Dn[s][p][items[s]]+pr;
               latency[s][p]=delay;
               sortie.print(delay);
               sortie.println();
           }
       }
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
        //in.nextInt();
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
