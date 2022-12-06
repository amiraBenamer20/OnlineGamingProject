/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GreyWolf;

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
public class GreyWolfOptimizer_Static 
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
    double [] obj,objI;
//        double lamC=0.0,  lamD=0.0, mult=4., cons=0.0,beta2=0.7;
//        boolean pen1C,pen2C,pen1D,pen2D;
        int ctrC,ctrD,milestone;
    public double GmE=0.2;
    public int bestGen;
    
   public List<Double>serversData; List<Integer>selected;
   int[]ff;
  
    
    //Grey wolf 
    int []alpha; double alpha_score=Double.MAX_VALUE;double alpha_px=Double.MAX_VALUE;double alpha_pe=Double.MAX_VALUE;double alpha_firstPart,alpha_fitness;
    int []beta;  double beta_score=Double.MAX_VALUE;double beta_px=Double.MAX_VALUE;double beta_pe=Double.MAX_VALUE;
    int []delta; double delta_score=Double.MAX_VALUE;double delta_px=Double.MAX_VALUE;double delta_pe=Double.MAX_VALUE;
    
    int activeBins;
    List<Double>convergence_curve;
    double a;
    String path;
     public GreyWolfOptimizer_Static (int sets, int nbOfPlayersPerSet, int servers,double tick,int Dmax) {
        this.path = "/nfs/homes/amira.rayane.benamer/Static";
        this.sets = sets;
        this.nbOfPlayersPerSet = nbOfPlayersPerSet;
        this.servers = servers;
        this.tick=tick;
        this.Dmax=Dmax;
        }
     public void GW_optimize(int C[][],int C_all[][], double W[][][],double Dn[][][],double CDH[],int[][] Dn_eligibility,double[][] Dn_closeness, String str,int id,int threshold,int rerun)
     {
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
        
//         for(int i=0;i<population.length;i++)
//         {
//             for(int j=0;j<population[i].length;j++)
//             {
//                 System.out.print(" "+population[i][j]);
//             }
//             System.out.println("");
//         }

//        milestone = sets/2;
//        lamC=0.0005;  lamD=0.0005;
//        pen1C = (px[0]==0.0);
//        pen1D = (pe[0]==0.0);
        
        int l=0;// Loop counter
        // Main loop
        while (l<maxIter)
        {
             a=2.0-l*((2.0)/maxIter); // a decreases linearly fron 2 to 0
             System.out.println("a: "+a);
             nonDominatedSorting();
//             for(int i=0;i<population.length;i++)
//            {
//                for(int j=0;j<population[i].length;j++)
//                {
//                    System.out.print(" "+population[i][j]);
//                }
//                System.out.println("| "+obj[i]+" viol: "+violation[i]);
//                System.out.println("");
//            }


            
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
//                    if(X1>servers)X1=servers;
//                    if(X1<0)X1=0.0;
                    
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
//                    int[]positionM=new int[population[i].length];
//                    int point1=rand.nextInt(population[i].length);
//                    int point2=rand.nextInt(population[i].length);
//                    if(point2<point1)
//                    {
//                        int temp=point1;l
//                        point1=point2;
//                        point2=temp;
//                    }
//                    System.arraycopy(population[i], 0, positionM, 0, point1);
//                    for(int k=point2;k<population[i].length;k++)
//                        positionM[k]=population[i][k];
//                    for(int k=point1, h=point2;k<point2 && h>point1;k++,h--)
//                        positionM[k]=population[i][h];
//                    System.arraycopy(positionM, 0, population[i], 0, population[i].length);
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
                    obj[i]=Double.MAX_VALUE;
               }
            }
            
            System.out.println("*********generation: "+l);


             System.out.println("____________________________________________________");
            for(int i=0;i<population.length;i++)
            {
                //Calculate objective function for each search agent
                double fitness=eval(population[i],i,px,pe,C,Dn,Dn_closeness,W,CDH, i);
                obj[i]=fitness;
                //Update Alpha, Beta, and Delta
                if (fitness<alpha_score  )
                {
                    alpha_score=fitness; //Update alpha
                    alpha_px=px[i];
                    alpha_pe=pe[i];
                    bestGen=l;
                    System.arraycopy(population[i], 0, alpha, 0, population[i].length);
                }
           

                if (fitness>alpha_score && fitness<beta_score )
                {
                     beta_score=fitness; //Update beta
                     beta_px=px[i];
                     beta_pe=pe[i];
                    System.arraycopy(population[i], 0, beta, 0, population[i].length);
                }
        
                if (fitness>alpha_score && fitness>beta_score && fitness<delta_score )
                {
                     delta_score=fitness; //Update delta
                     delta_px=px[i];
                     delta_pe=pe[i];
                    System.arraycopy(population[i], 0, delta, 0, population[i].length);
                }
            }
            System.out.println("alpha Score: "+alpha_score+" beta_score: "+beta_score+" delta_score: "+delta_score);
//            System.out.println("lamdaC: "+lamC+" lamdaD: "+lamD);
//            
//                pen2C = (px[0]==0.0);
//                pen2D = (pe[0]==0.0);
//                if (!(pen1C^pen2C))
//                {
//                    if (ctrC < milestone) ctrC++; else ctrC = 0;
//                }
//                else
//                {
//                    pen1C = pen2C; ctrC = 0;
//                }
//                
//                 if (!(pen1D^pen2D))
//                {
//                    if (ctrD < milestone) ctrD++; else ctrD = 0;
//                }
//                else
//                {
//                    pen1D = pen2D; ctrD = 0;
//                }
//            
//         
//                 double valueC=1.0;
//        if (ctrC == milestone)
//        {
//          if (pen1C)
//          {
//            lamC /= (beta2*mult);
//            valueC=lamC*(1-beta2*mult);
//          }
//          else 
//          {
//            lamC *= mult;
//            valueC=lamC-(lamC/mult);
//          }
//        }
//        
//        double valueD=1.0;
//        if (ctrD == milestone)
//        {
//          if (pen1D)
//          {
//            lamD /= (beta2*mult);
//            valueD=lamD*(1-beta2*mult);
//          }
//          else 
//          {
//            lamD *= mult;
//            valueD=lamD-(lamD/mult);
//          }
//        }
//        
//        //update fitness value with new lamdaC & lamdaD
//        for(int i=0;i<3;i++) 
//             obj[i] += (valueC*px[i])+(valueD*pe[i]);
//        
                 

            if(l==0)
            {
                int[][]Rt=new int[2*searchAgents][sets];
                double[] px_copy=new double[2*searchAgents];
                double[] pe_copy=new double[2*searchAgents];
                double[] obj_copy=new double[2*searchAgents];
                int co=0;
                for(int i=0;i<searchAgents;i++)
                {
                    System.arraycopy(population_copy[i], 0,Rt[i] , 0, sets);
                    px_copy[i]=pxI[i];
                    pe_copy[i]=peI[i];
                    obj_copy[i]=objI[i];
                    co++;
                }
                for(int i=0;i<searchAgents;i++)
                {
                    System.arraycopy(population[i], 0,Rt[co] , 0, sets);
                    px_copy[co]=px[i];
                    pe_copy[co]=pe[i];
                    obj_copy[co]=obj[i];
                    co++;
                }
                population=new int[2*searchAgents][sets];
                px=new double[2*searchAgents];
                pe=new double[2*searchAgents];
                obj=new double[2*searchAgents];
                px=px_copy;
                obj=obj_copy;
                pe=pe_copy;
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
       System.out.println("Execution time in seconds: " +
                               executionFinal);
         System.out.println("Best Generation: "+bestGen);
       System.out.println("************Solution**************");
         howMuchPlacedServers(alpha);
         display(alpha,Dn_closeness,C);
  
         try
      {

        writeSolution(alpha, id,str,rerun,C,threshold);
      }
      catch (Exception e)
      {
          
      }
        writeLatencyDistribution(alpha, id, str, threshold, rerun, W, C, Dn, Dmax);
 
        try {
            writeSolutionOnOutputFiles(id, rerun,str,C,threshold);
        } catch (IOException ex) {
            Logger.getLogger(GreyWolfOptimizer_Static.class.getName()).log(Level.SEVERE, null, ex);
        }

        
     }
     
       void writeSolutionOnOutputFiles(int id,int rerun,String str,int[][]C,int threshold) throws IOException 
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
       sortieQuality.print(alpha_fitness);
       sortieQuality.println();
       sortieObj.print(alpha_firstPart);
       sortieObj.println();
       sortieNbofUS.print(activeBins);
       sortieNbofUS.println();
       sortieBestIter.print(bestGen);
       sortieBestIter.println();
       sortieExec.print(executionFinal);
       sortieExec.println();
       sortieViolationCapr.print(alpha_px);
       sortieViolationCapr.println();
       sortieViolationDead.print(alpha_pe);
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
    
   void writeSolution(int[]items,int id,String str,int rerun,int[][]C,int threshold) throws IOException
   {
        PrintWriter sortieSolution = null;
        PrintWriter sortieServerData = null;
        PrintWriter sortieActiveServers = null;
        sortieSolution = new PrintWriter(new FileWriter(path+"/Solution/"+sets+"/"+sets+"_"+id+"_"+str+"_"+rerun+"_"+threshold+"_"+C[0][0]+"vcpu.txt"));
        sortieServerData = new PrintWriter(new FileWriter(path+"/ServerData/"+sets+"/"+sets+"_"+id+"_"+str+"_"+rerun+"_"+C[0][0]+"vcpu.txt"));
        sortieActiveServers = new PrintWriter(new FileWriter(path+"/ActiveServers/"+sets+"/"+sets+"_"+id+"_"+str+"_"+rerun+"_"+C[0][0]+"vcpu.txt"));
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
            
            sortieActiveServers.print( selected.get(e));
            sortieActiveServers.println();
        }
        sortieSolution.flush();
        sortieSolution.close();
        sortieServerData.flush();
        sortieServerData.close();
        
        sortieActiveServers.flush();
        sortieActiveServers.close();
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
   void writeLatencyDistribution(int[] best,int id,String str,int threshold,int rerun,double W[][][],int C[][],double Dn[][][],int Dmax)
   {
        String filename=path+"/CDF/"+sets+"/"+str+" Output"+sets+"_"+maxIter+"_"+id+"_"+threshold+"_"+rerun+"_"+C[0][0]+"vcpu.txt";

        try {
            resume(best, W, C, Dn, Dmax,filename);
        } catch (IOException ex) {
            Logger.getLogger(GreyWolfOptimizer_Static.class.getName()).log(Level.SEVERE, null, ex);
        }

   }
      public void resume(int[]items,double W[][][],int C[][],double Dn[][][],int Dmax,String filename) throws IOException
   {
       PrintWriter sortie= new PrintWriter(new FileWriter(filename));
       for(int s=0;s<items.length;s++)
       {
           double pr=calculateProcessing(W, s);//,chr.capRel,count);
           for(int p=0;p<W[s].length;p++)
           {
               double delay=Dn[s][p][items[s]]+pr;
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
     public void initialization(int C[][],int[][]Dn_eligibility,int[]sumWP,double Dn[][][],double[][][]W,double[]CDH,double[][] Dn_closeness)
   {
       population=new int[searchAgents][sets];
       population_copy=new int[searchAgents][sets];
       px=new double[searchAgents];pxI=new double[searchAgents];
       pe=new double[searchAgents];peI=new double[searchAgents];
       obj=new double[searchAgents];objI=new double[searchAgents];
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
          obj[i]=Double.MAX_VALUE;
          peI[i]=-1;
          pxI[i]=-1;
          objI[i]=Double.MAX_VALUE;
        }
   }
     
    void nonDominatedSorting()
    {
        int ii[]=new int[population.length];

        for(int i=0;i<population.length;i++)
            ii[i]=i;
        quickSort(population,px,pe, obj,ii , 0, population.length-1);
    }
     
    
    double mutationProbability(int current)
    {
        if(current<=w)
            return h1*  (1-Math.pow((double)(current/maxIter),2));
        else
            return h2*  (1-Math.pow((double)(current/maxIter),2));
    }
    double eval(int[]elem,int k,double px[],double pe[],int[][]C,double Dn[][][],double [][]Dn_closeness,double[][][]W,double[]CDH,int l)
    { 
        int i,j; 
        double pxX=0.0, peE=0.0;
        int use[]=new int[servers]; 
        int demandVCPU[]=new int[servers]; 

        for (i=0;i<sets;i++) 
        {
            use[elem[i]] += (double)sumWP[i]; 
            demandVCPU[elem[i]] +=Math.round((double)sumWP[i]/1000);
            peE+=(Dn_closeness[i][elem[i]]);//*(Dn_closeness[i][elem[i]]);
        }
          

        double firstPart=0.0;
        for (j=0;j<servers;j++)
        {
            firstPart+=demandVCPU[j]*CDH[j];
            if (use[j] > C[j][0]) 
                pxX += (use[j]-C[j][0]);//*(use[j]-C[j][0]);  //*2
        }
       
         px [k]=pxX;
         pe[k]=peE;
         
         return firstPart+ pxX+ peE; 
    } 
     
       public int pick(int n)  /* generate a pseudorandom integer number between 1 and n */
    {
        int p;

        p = (int) (Math.random()*n);
        return p;
    }
       
     int partition(int array[][],double[]px,double pe[],double[]obj,int ii[],int begin,int end) 
    {
    int pivot = end;
  
 
    int counter = begin;
    for (int i = begin; i < end; i++) {
        if (px[i]+pe[i] < px[pivot]+pe[pivot]) {
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

    return counter;
    }

    public  void quickSort(int array[][],double[]px,double []pe,double[]obj,int ii[],int begin,int end) 
    {
        if (end <= begin) return;
        int pivot = partition(array,px,pe,obj,ii, begin, end);
        quickSort(array,px,pe,obj,ii, begin, pivot-1);
        quickSort(array,px,pe,obj,ii, pivot+1, end);
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
     
      public void display(int elem[],double[][]Dn_closeness,int[][]C)
    {
        for(int s=0;s<elem.length;s++)
           {
               System.out.print(" "+elem[s]);
           }
           System.out.println(""); 
           System.out.println(" | "+alpha_score+" --fitnes: "+"  --px: "+alpha_px+" --pe: "+alpha_pe);
           System.out.println("Number of active servers is: "+activeBins);
           
            int i,j; 
        double px=0.0, pe=0.0;
        int use[]=new int[servers]; 
        int demandVCPU[]=new int[servers]; 

        for (i=0;i<sets;i++) 
        {
            use[elem[i]] += (double)sumWP[i]; 
            demandVCPU[elem[i]] +=Math.round((double)sumWP[i]/1000);
            pe+=(Dn_closeness[i][elem[i]]);
        }
          

        double firstPart=0.0;
        for (j=0;j<servers;j++)
        {
            firstPart+=demandVCPU[j];//*CDH[j]
            if (use[j] > C[j][0]) 
                px += (use[j]-C[j][0]);  //*2
        }
        
        for (i=0;i<sets;i++) 
        {
            System.out.print(" "+elem[i]);
        }
        System.out.println("");
        for (j=0;j<servers;j++)
        {
            System.out.print(" "+use[j]);
        }
        alpha_firstPart=firstPart;
        alpha_fitness=firstPart+ px+ pe;
        System.out.println("");
        System.out.println("first: "+firstPart);
        System.out.println("px: "+px+" pe: "+pe);
        System.out.println("fitness:  "+(firstPart+ px+ pe));
    }
}
