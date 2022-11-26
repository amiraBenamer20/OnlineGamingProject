/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GeneticAlgorithmPenalty;


import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Amira BENAMER
 */
public class Chromosome implements  Cloneable
{
   public int [][] C;
   public int [][] C_original;
   public Set[] groups;
   public int activeServers;
   
   public int [] sumWP;
   public List<Double>serversData;
   int[]ii;

   
   public double fitness=Integer.MAX_VALUE;
   public int violated=Integer.MAX_VALUE;
   public double penality_capacity;
   public double penality_latency;
   public int flag=-1;
   public double mainFunc=Integer.MAX_VALUE;
    //delay violation
    public Double [] delayRel;
    //cap violation
    public int [][] capRel;
    public double migrationCost=0.0;
    public double objectiveFunc;
    public double secondPart;
    public double costResources=0.0;
    public double costResourceNoViol=0.0;
    public double costDeadline=0.0;
    public double execTime=0.0;
    public double[]latency_cdf_pdf;
    public int id;
    
    public List<Integer>selected;
    public Chromosome(int ietmSetLength) 
    {
        this.groups=new Set[ietmSetLength];
    }
    
    
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        return (Chromosome)super.clone();
    }
    
     public void display()
    {
        for(int s=0;s<groups.length;s++)
           {
               System.out.print(" "+groups[s].fognode);
           }
           System.out.print(" | "+fitness+"  --penality_capacity: "+penality_capacity+" --penality_latency: "+penality_latency);
           System.out.println(""); 
           System.out.println(" | "+objectiveFunc+" --fitnes: "+fitness+"  --penality_capacity: "+penality_capacity+" --penality_latency: "+penality_latency);
           System.out.println("Number of active servers is: "+activeServers);
    }
     public  void howMuchPlacedServers()
     {
         selected=new ArrayList<>();
         int count=0;
         for(int s=0;s<groups.length;s++)
           {
               if(!selected.contains(groups[s].fognode))
               {
                   selected.add(groups[s].fognode);
                   count++;
               }
           }
         activeServers=count;
     }
     
    
     
      public void serverCapacityUsageCalculus()
   {
       serversData=new ArrayList<>();
      ii=new int[activeServers];
       for(int i=0;i<activeServers;i++)
           ii[i]=0;
        for(int s=0;s<groups.length;s++)
           {
               ii[selected.indexOf(groups[s].fognode)]+=1;
           }

        for(int e=0;e<activeServers;e++)
        {
            int u=ii[e]*sumWP[0];
            Double frac=((double)u/C[selected.get(e)][0])*100;
            serversData.add(frac);
        }
   }
    
}
