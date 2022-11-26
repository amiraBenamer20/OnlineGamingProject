/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Benchmarks.NewGGAPenalty;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;



/**
 *
 * @author ASUS
 */
public class Chromosome implements Cloneable
{
    public int [][] C;
   public int [] sumWP;
   public int [][] C_original;
   public Server[] bins;
   public List<Integer> selected;
//   private Integer[]sets;
   public int activeServers;

   int[]ii;
   List<Integer>used;
   public List<Double>serversData;

   
   public double fitness=Integer.MAX_VALUE;
   public int violated=Integer.MAX_VALUE;
   public double pen;
   public double pE;
   public double objectiveFunc;
   public int flag=-1;
   public double mainFunc=Integer.MAX_VALUE;
    //delay violation
    public Double [] delayRel;
    //cap violation
    public int [][] capRel;
    public double migrationCost=0.0;

    public double secondPart;
    public double costResources=0.0;
    public double costResourceNoViol=0.0;
    public double costDeadline=0.0;
    public double execTime=0.0;
    public double[]latency_cdf_pdf;
    public int id;
    public int sets;
    
     public Chromosome(int binsLength,int itemsLength,int[][]C) 
    {
        this.bins=new Server[binsLength];
         for(int e=0;e<binsLength;e++)
          {
              Server server=new Server(e, C[e][0]);
              this.bins[e]=server;
          }
         sets=itemsLength;
//        sets=new Integer[itemsLength];

    }
    
    
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        return (Chromosome)super.clone();
    }
    
     public Integer[] display(int id,String str,int rerun,int threshold,String path)
    {
        Integer[]sets=new Integer[this.sets];
        zoomOnservers(sets);
        howMuchPlacedServers();
//        for(int s=0;s<sets.length;s++)
//           {    
//              System.out.print(" "+sets[s]);      
//           }
//           System.out.println(""); 
//           System.out.println(" | "+objectiveFunc+" --fitnes: "+fitness+"  --pen: "+pen+" --pE: "+pE);
//           System.out.println("Number of active servers is: "+activeServers);
           
        try {
            writeSolution(id, str, rerun, sets,threshold,path);
        } catch (IOException ex) {
            Logger.getLogger(Chromosome.class.getName()).log(Level.SEVERE, null, ex);
        }
        return sets;
    }
     
       void writeSolution(int id,String str,int rerun,Integer []items,int threshold,String path) throws IOException
   {
        PrintWriter sortieSolution = null;
        PrintWriter sortieServerData = null;
        PrintWriter sortieActiveServers = null;
        sortieSolution = new PrintWriter(new FileWriter(path+"/Solution/"+sets+"/"+sets+"_"+id+"_"+str+"_"+rerun+"_"+threshold+"_"+C[0][0]+"vcpu.txt"));
        sortieServerData = new PrintWriter(new FileWriter(path+"/ServerData/"+sets+"/"+sets+"_"+id+"_"+str+"_"+rerun+"_"+threshold+"_"+C[0][0]+"vcpu.txt"));
        sortieActiveServers = new PrintWriter(new FileWriter(path+"/ActiveServers/"+sets+"/"+sets+"_"+id+"_"+str+"_"+rerun+"_"+threshold+"_"+C[0][0]+"vcpu.txt"));
         sortieSolution.println();
        for(int s=0;s<this.sets;s++)
        {
            sortieSolution.print(" "+items[s]);
            sortieSolution.println();
        }
        
        serverCapacityUsageCalculus(items);
        
        for(int e=0;e<activeServers;e++)
        {
            sortieServerData.print( serversData.get(e));
            sortieServerData.println();
            
            sortieActiveServers.print( used.get(e));
            sortieActiveServers.println();
        }
        sortieSolution.flush();
        sortieSolution.close();
        sortieServerData.flush();
        sortieServerData.close();
        
        sortieActiveServers.flush();
        sortieActiveServers.close();
   }
       
       
     
     public void zoomOnservers(Integer[]sets)
     {
         for(int e=0;e<bins.length;e++)
         {
//             System.out.print("S "+bins[e].serverID+": ");
             for(int s=0;s<bins[e].getSets().size();s++)
             {
                 sets[bins[e].getSets().get(s).setId]=e;
//                 System.out.print(" "+bins[e].getSets().get(s).setId);
             }
//             System.out.print(" | ");

         }
//         System.out.println("");
     }
     
      public  void howMuchPlacedServers()
     {
         selected = new ArrayList<>();
         int count=0;
         for(int e=0;e<bins.length;e++)
           {
               if(!bins[e].getSets().isEmpty() && !selected.contains(e))
               {
                   count++;
                   selected.add(e);
               }
           }
         activeServers=count;
     }
      
         public void serverCapacityUsageCalculus(Integer[]sets)
   {
      serversData=new ArrayList<>();
      ii=new int[bins.length];
       for(int i=0;i<ii.length;i++)
           ii[i]=0;
      used=new ArrayList<>();
        for(int s=0;s<sets.length;s++)
           {
               if(!used.contains(sets[s]))
               {
                   used.add(sets[s]);
                   
               }
               ii[used.indexOf(sets[s])]+=1;
           }

        for(int e=0;e<activeServers;e++)
        {
            int u=ii[e]*sumWP[0];
            Double frac=((double)u/C[used.get(e)][0])*100;
            serversData.add(frac);
        }
   }
}
