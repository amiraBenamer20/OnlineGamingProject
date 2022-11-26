/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Benchmarks.GroupingGeneticPenalty;


import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Amira BENAMER
 */
public class Chromosome implements Cloneable {
   public int [][] C;
   public int [][] C_original;
   public int[][]C_self;
    //item part
    public Set[] items;
     public int activeServers;
    //group part
    public List<Server> groups;
    public int [] sumWP;
     int[]ii;
   public List<Integer>used;
   public List<Double>serversData;
   
    //fitness
   public double fitness=Integer.MAX_VALUE;
   public double violated=Integer.MAX_VALUE;
   public double capV;
   public double delV;
   public int flag=-1;
   public double mainFunc=Integer.MAX_VALUE;
    //delay violation
    public Double [] delayRel;
    //cap violation
    public int [][] capRel;
    public double migrationCost=0.0;
    public double objectiveFunc;
    public double costFunc;
    public double secondPart;
    public double costResources=0.0;
    public double costResourceNoViol=0.0;
    public double costDeadline=0.0;
    public double execTime=0.0;
    public double[]latency_cdf_pdf;
    
    public Chromosome(int ietmSetLength) 
    {
        this.groups=new ArrayList<>();
        this.items=new Set[ietmSetLength];
    }

    
      @Override
    public Object clone() throws CloneNotSupportedException
    {
        return (Chromosome)super.clone();
    }
    public Chromosome(Set[] items, List<Server> groups, double fitness) {
        this.items = items;
        this.groups = groups;
        this.fitness = fitness;

    }
    
    
    public boolean checkCapacity()
    {
        boolean valid=true;
        List<Server>groups=this.groups;
        
         for(int g=0;g<groups.size();g++)
        {
//            List<Integer>items=itemSet(this, groups.get(g));
            List<Set>items=groups.get(g).sets;
            int cpu=0;int ram=0;
            for(int s=0;s<items.size();s++)
            {
                cpu+=this.items[s].sizeItem;
            }
     
            if(cpu>(C[groups.get(g).serverID][0]+this.capRel[groups.get(g).serverID][0]))
            {
                valid=false;
                break;
            }
        }
        return valid;
    }
    
    public boolean migrationConstraint()
    {
         Set[]items=this.items;
         for(int s=0;s<items.length;s++)
         {
             if(items[s].migrationTime*1000>100000)
                 return false;
         }
         return true;
    }
    
    
    
    
    
      public void display()
    {
         for(int s=0;s<items.length;s++)
            System.out.print(" "+items[s].hostBin.serverID);
        System.out.print(" | ");
        for(int g=0;g<groups.size();g++) 
           System.out.print(" "+groups.get(g).serverID);
         System.out.println(" | "+objectiveFunc+" --fitnes: "+fitness+"  --pen: "+capV+" --pE: "+delV);
         howMuchPlacedServers();
         System.out.println("Number of active servers is: "+activeServers);
    }
     public  void howMuchPlacedServers()
     {
       
         activeServers=this.groups.size();
     }
    
//    public void display()
//    {
//        for(int s=0;s<items.length;s++)
//            System.out.print(" "+items[s].hostBin.serverID);
//        System.out.print(" | ");
//       for(int g=0;g<groups.size();g++) 
//           System.out.print(" "+groups.get(g).serverID);
//       System.out.print(" | "+fitness+"  --pen: "+pen+" --pE: "+pE);
//        System.out.println("Migration Cost: "+migrationCost);
//        System.out.println("");
//     
//        System.out.println("----Migration Time violation----");
//        for(int s=0;s<items.length;s++)
//            System.out.print(" "+items[s].migrationTime);
//        System.out.println("");
//    }
    public void displayViolations()
    {
         double r2=0.0;

        for(int i=0;i<delayRel.length;i++)
        {
            r2=r2+delayRel[i];
        }
        r2=r2*5;
        
       
         double r1=0.0;
         for(int i=0;i<capRel.length;i++)
              r1=r1+(capRel[i][0]*100)/C_original[i][0];
         
         costResources=r1+costResources;
         costDeadline=r2+costDeadline;
         System.out.println("r1= "+r1+ " Ressource Cost= "+(costResources+costResourceNoViol));
         System.out.println("Delay Cost= "+(costDeadline));
         
         System.out.println("Delay Relaxation: ");
          for(int i=0;i<delayRel.length;i++)
            System.out.println("edge "+i+" with violation: "+delayRel[i]);
        System.out.println("----------------------------");
        
         System.out.println("Resource Relaxation:");
          for(int i=0;i<capRel.length;i++)
            System.out.println("edge "+i+" with violation: "+capRel[i][0]);//+" | "+capRel[i][1]); memory
         
    
    }
    
    public void serverCapacityUsageCalculus()
   {
      serversData=new ArrayList<>();
      ii=new int[activeServers];
       for(int i=0;i<activeServers;i++)
           ii[i]=0;
      used=new ArrayList<>();
        for(int s=0;s<items.length;s++)
           {
               if(!used.contains(items[s].hostBin.serverID))
               {
                   used.add(items[s].hostBin.serverID);
                   
               }
               ii[used.indexOf(items[s].hostBin.serverID)]+=1;
           }

        for(int e=0;e<activeServers;e++)
        {
            int u=ii[e]*sumWP[0];
            Double frac=((double)u/C[used.get(e)][0])*100;
            serversData.add(frac);
        }
   }
    
    public void fillData(String fileName, double [][]latency) throws FileNotFoundException, IOException 
    {
        PrintWriter sortie = new PrintWriter(new FileWriter(fileName,true));
        
        sortie.print("Ps=[");
        for(int s=0;s<items.length;s++)
        {
            sortie.print((items[s].hostBin.serverID+1)+" ");
            if(s+1<items.length)sortie.print(",");
        }
        sortie.print("];");
        sortie.println("");
        
        
        sortie.print("C=");
        sortie.print('[');
        for (int e = 0; e < capRel.length; e++) {
            sortie.print('[');
            sortie.print(capRel[e][0] + " ");
            sortie.print(']');
            if (e + 1 < capRel.length) {
                sortie.print(',');
            }
        }
        sortie.print(']');
        sortie.print(';');
        sortie.println();
        
        sortie.print("delay=");
        sortie.print('[');
        for (int s = 0; s <latency.length; s++) {
            sortie.print('[');
            for (int p = 0; p < latency[s].length; p++) {
                sortie.print('[');
                sortie.print(latency[s][p] + " ");
                sortie.print(']');
                if (p + 1 < latency[s].length) {
                    sortie.print(',');
                }
            }
            sortie.print(']');
            if (s + 1 < latency.length) {
                sortie.print(',');
            }
        }
        sortie.print(']');
        sortie.print(';');
        sortie.println();
        
//        sortie.print("First: "+firstPart);
        sortie.println();
        sortie.print("Second: "+secondPart);
        sortie.println();
        
        sortie.flush();
        sortie.close();
    }

    public void fillMetrics( String cost,String dead, String exec, String mig) throws IOException
    {
        PrintWriter sortie1 = new PrintWriter(new FileWriter(cost,true));
        sortie1.print(costResources);
        sortie1.println();
        sortie1.flush();
        sortie1.close();
        
        PrintWriter sortie2 = new PrintWriter(new FileWriter(dead,true));
        sortie2.print(costDeadline);
        sortie2.println();
        sortie2.flush();
        sortie2.close();
        
        PrintWriter sortie3 = new PrintWriter(new FileWriter(exec,true));
        sortie3.print(execTime);
        sortie3.println();
        sortie3.flush();
        sortie3.close();
        
        PrintWriter sortie4 = new PrintWriter(new FileWriter(mig,true));
        sortie4.print(migrationCost);
        sortie4.println();
        sortie4.flush();
        sortie4.close();
        
//        PrintWriter sortie5 = new PrintWriter(new FileWriter(cdf_pdf,true));
//        for(int i=0;i<latency_cdf_pdf.length;i++)
//        {
//            sortie5.print(latency_cdf_pdf[i]);
//            sortie5.println();
//        }
//        sortie5.flush();
//        sortie5.close();
    }
    
//    public List<Integer>itemSet(Chromosome crh, int g)
//    {
//        Set sets[]=crh.items;
//        List<Integer>positions=new ArrayList<>();
//        for(int s=0;s<sets.length;s++)
//        {
//            if(sets[s].hostBin==g)
//            {
//                positions.add(s);
//            }
//        }
//        return positions;
//    }
}
