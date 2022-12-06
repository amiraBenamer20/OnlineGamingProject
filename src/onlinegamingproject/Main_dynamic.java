/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package onlinegamingproject;

import Benchmarks.GreyWolf.GreyWolfOptimizer_Dynamic;
import GeneticAlgorithmPenalty.GeneticPenalty_Dynamic;


/**
 *
 * @author Amira BENAMER
 */
public class Main_dynamic 
{
    static String f1,f2,f3,f4,f6,f7,f5;
   
     public static void main(String args[])
     {
         int [] all_instances = {100};//{20,40,60,80,100,250,500,750,1000,1250,1500,1750,2000,3000,4000,5000};

         String pathDyanmic = "Data/";//"/nfs/homes/amira.rayane.benamer/Dynamic/";
         String pathStatic = "Data/";//"/nfs/homes/amira.rayane.benamer/Static/";
         
         int []velocities = {10,20,40};
         float [] variations = {0.3f};//, 0.3f};
         String [] v_char = {"Low","Medium","High"};//Low
         String [] methods = {"GA","GW"};//, "GW"};
         int [] configurations = {100,250,500,750,1000,1250,1500,1750,2000,3000,4000,5000};
         int [] thresholds = {0};//,10}; //gamma
         int [] cap = {5000};
         int timestamps = 3;//144
         int instances=1;//number of instances
         int start=0;
         for(int id=start;id<instances;id++)
         {
             for(int change = 0; change< variations.length;change++)
             {
                for(int th = 0; th< thresholds.length;th++)
                {
                   for(int c = 0; c< cap.length;c++)
                   {

                   String str = "GA";
                   int rank = -1;
                   for(int g = 0;g< configurations.length;g++)
                   {
                       System.out.println("----------------Config: "+configurations[g]+"-----------------");
                       for (int rk = 0; rk< all_instances.length;rk++)
                       {
                           if(all_instances[rk]==configurations[g])
                           {
                               rank = rk;
                               break;
                           }
                       }

                       for(int v = 0; v <velocities.length; v++)
                       {
                           System.out.println("-------------Velocity: "+v_char[v]+"---------------");
                          for(int t=1;t < timestamps;t++)
                          {
                              f5 =  pathStatic+"Input/Surface"+cap[c]+"vcpu_"+id+"_"+thresholds[th]+".txt";
                             if(t-1 == 0)
                              {
                                 f1 =  pathStatic+"DataSet/"+configurations[g]+"/data"+configurations[g]+"_"+id+"_"+cap[c]+"_"+thresholds[th]+".txt";
                                                               
                                 //f2 =  pathStatic+"Solution/"+configurations[g]+"/GA_Impr_more_5000_4_65"+"_0"+"_"+thresholds[th]+"_"+cap[c]+"vcpu.txt";//configurations[g]+"_"+id+"_"+str+"_0"+"_"+thresholds[th]+"_"+cap[c]+"vcpu.txt";
                                 f2 = pathStatic+"Solution/"+configurations[g]+"/"+configurations[g]+"_"+id+"_GA_"+0+"_"+thresholds[th]+"_"+cap[c]+"vcpu.txt";
                                 f4 =  pathStatic+"ServerData/"+configurations[g]+"/"+configurations[g]+"_"+id+"_GA_"+0+"_"+thresholds[th]+"_"+cap[c]+"vcpu.txt";//+configurations[g]+"/"+configurations[g]+"_"+id+"_"+str+"_0"+"_"+thresholds[th]+"_"+cap[c]+"vcpu.txt";
                                 f3 =  pathStatic+"ActiveServers/"+configurations[g]+"/"+configurations[g]+"_"+id+"_GA_"+0+"_"+thresholds[th]+"_"+cap[c]+"vcpu.txt";//+configurations[g]+"/"+configurations[g]+"_"+id+"_"+str+"_0_"+thresholds[th]+"_"+cap[c]+"vcpu.txt"; 
                                 f6 =  pathStatic+"CoordinatesSet/"+configurations[g]+"/players_"+configurations[g]+"_"+id+"_"+thresholds[th]+"_0"+"_"+cap[c]+"vcpu.txt";
                                 f7 =  pathStatic+"CoordinatesSet/"+configurations[g]+"/edges_"+configurations[g]+"_"+id+"_"+thresholds[th]+"_0"+"_"+cap[c]+"vcpu.txt";

                             }
                             else
                             {
                                   f1 = pathDyanmic+"DataSet/"+configurations[g]+"/t_"+(t-1)+"_"+"Without_Q_"+id+"_"+v_char[v]+"_"+thresholds[th]+"_"+variations[change]+".txt";
                                   f2 = pathDyanmic+"Solution/"+configurations[g]+"/static"+"_"+configurations[g]+"_"+id+"_"+str+"_"+0+"_"+thresholds[th]+"_"+cap[c]+"vcpu_"+(t-1)+"_"+v_char[v]+"_"+variations[change]+".txt";
                                   f4 = pathDyanmic+"ServerData/"+configurations[g]+"/static_"+configurations[g]+"_"+id+"_"+str+"_"+0+"_"+thresholds[th]+"_"+cap[c]+"vcpu_"+(t-1)+"_"+v_char[v]+"_"+variations[change]+".txt";
                                   f3 = pathDyanmic+"ActiveServers/"+configurations[g]+"/t"+(t-1)+"/static_"+configurations[g]+"_"+id+"_"+str+"_"+0+"_"+thresholds[th]+"_"+v_char[v]+"_"+variations[change]+"_"+cap[c]+"vcpu_"+(t-1)+".txt";
                                   f6 = pathDyanmic+"CoordinatesSet/"+configurations[g]+"/players_t_"+(t-1)+"_Without_Q_"+id+"_"+v_char[v]+"_"+thresholds[th]+"_"+variations[change]+".txt";
                                   f7 = pathDyanmic+"CoordinatesSet/"+configurations[g]+"/edges_t_"+(t-1)+"_Without_Q_"+id+"_"+v_char[v]+"_"+thresholds[th]+"_"+variations[change]+".txt";
                             }





                              System.out.println("---*------Config: "+configurations[g]+"-------**----------");
                              boolean bool = true;
                              Input_Data ipt = null;
                              while(bool)
                              {
                                  try
                                  {
                                       bool = false;
                                       DynamicOperations dy = new DynamicOperations(false,t,variations[change]);//BW....
                                       ipt =  dy.apply(pathDyanmic,f1, f2, f3, f4,f6,f7,f5,configurations[g],thresholds[th],rank,id,velocities[v]); 
                                  }
                                  catch(Exception ex)
                                  {
                                      bool = true;
                                  }
                              }

                                 System.out.println(" ipt.sets "+ipt.sets);
                              for(int mth = 0; mth< methods.length;mth++)
                                {
                                     if(methods[mth].equals("GA"))
                                     {
                                         
                                           System.out.println("---------------------------<Our Proposal>----------------------------------------");
                                           System.out.println("----------------STATIC------------------");
                                           GeneticPenalty_Dynamic gp = new GeneticPenalty_Dynamic(ipt.sets, ipt.nbPlayersSet, ipt.edges,0.02,20,"static", pathDyanmic,cap[c],configurations[g],t,v_char[v],variations[change]);
                                           gp.Genetic_pen(ipt.C, ipt.W, ipt.Dn, ipt.CDH, ipt.Dn_eligibility, ipt.Dn_closeness_all, "GA", "2P", id, thresholds[th], 0, ipt.sol_all, ipt.BW, ipt.VMS,0.0, 0.0);

                                            System.out.println("----------------DYNAMIC------------------");
                                           gp = new GeneticPenalty_Dynamic(ipt.sets_mig, ipt.nbPlayersSet, ipt.edges_mig,0.02,20,"dynamic", pathDyanmic,cap[c],configurations[g],t,v_char[v],variations[change]);
                                           gp.Genetic_pen(ipt.C_mig, ipt.W_mig, ipt.Dn_mig, ipt.CDH_mig, ipt.Dn_eligibility_mig, ipt.Dn_closeness_mig, "GA", "2P", id, thresholds[th], 0, ipt.sol_mig, ipt.BW_mig, ipt.VMS_mig,ipt.cummulative_cost,ipt.cummulative_clientPaidFee);
                                      
                                           }
                                     if(methods[mth].equals("GW"))
                                     {
                                           System.out.println("-----------------------------<GREY WORLF>-----------------------------");
                                           System.out.println("----------------STATIC------------------");
                                           GreyWolfOptimizer_Dynamic gw=new GreyWolfOptimizer_Dynamic(ipt.sets, ipt.nbPlayersSet, ipt.edges,0.02,20,"static", pathDyanmic,cap[c],configurations[g],t,v_char[v],variations[change]);
                                           gw.GW_optimize(ipt.C, ipt.W, ipt.Dn, ipt.CDH, ipt.Dn_eligibility, ipt.Dn_closeness_all, "GW", "2P", id, thresholds[th], 0, ipt.sol_all, ipt.BW, ipt.VMS,0.0, 0.0);

                                           System.out.println("----------------DYNAMIC------------------");
                                           gw=new GreyWolfOptimizer_Dynamic(ipt.sets_mig, ipt.nbPlayersSet, ipt.edges_mig,0.02,20,"dynamic", pathDyanmic,cap[c],configurations[g],t,v_char[v],variations[change]);
                                           gw.GW_optimize(ipt.C_mig, ipt.W_mig, ipt.Dn_mig, ipt.CDH_mig, ipt.Dn_eligibility_mig, ipt.Dn_closeness_mig, "GW", "", id, thresholds[th], 0, ipt.sol_mig, ipt.BW_mig, ipt.VMS_mig,ipt.cummulative_cost,ipt.cummulative_clientPaidFee);

                                     }
                                  }
                               }
                           }
                       }
                   }
                }
             }
         }
        
     }
}
