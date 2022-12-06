/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package onlinegamingproject;


import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;



/**
 *
 * @author ASUS
 */
class element {

    double max;
    double percent;
}

class Data {

    double[][][] Dn;
    double[][] Dn_closeness;
    int [][] Dn_constraints;
    int C[][];
    int C_prev[][];
    double CDH[];
    int edges;
    Zone zones[];
    double [][]enabledServersCoordinates;
    int[] enabledServers;
    double W[][][];
    int[]VMS;
    double[][] BW;
    double cummulative_cost;
    double cummulative_clientPaidFee;
    double [][][] PlayerCoordinatesAll;
}

public class DynamicOperations 
{
    static int sessionRange = 10;//min
    static int direction = 35, velocity;//km
    static String velocity_char;
    static int directionPositive = 30, directionNegative = - 150;
    static double distance;
    
    static float change;// =0.1f;
    static float setProportionOfGroupsToAppear;//.1f;
    static float setProportionOfGroupsToDisappear ;
    static float setProportionOfPlayersToDisappear = 0.1f;
    static float setProportionOfPlayersToAppear = 0.1f;
    static float setProportionOfMovedPlayers = 0.1f;
//    static float setProportionOfMovedPlayersPositive = 0;
//    static float setProportionOfMovedPlayersNegative = 0;
    static float setProportionOfFogNodesToAppear;
    static float setProportionOfFogNodesToDisappear;
    static double fee = 2;
    
    static int sets, sets_org ;
    static int edges;
    static int nbOfplayersPerSet = 20;
    static int Dmax;
    static int dim =1;
    static double rangeX, rangeY;
    
    
    //required resources type
    static int addedR=0;
    static int smallS ;
    static int mediumS ;
    static int largeS;
    static int xtraLargeS;
    
    //input
    static double[][] serviceGameReq = {{0.25}, {2}, {3}};
    static double[] cost = {0.1, 0.1};
    static int[][] edgeCap = {{1000}, {5000}, {12000}, {15000}}; static int addedC;
    static double sigma=(2.0/3);
    static int[] vmSizes = {512,1024,2048};
//    static int []BW_choices={512,1024, 2048};
    static Map<Double,Double>alphaRange;
    static double alpha=4;
    static double rTemp;//ms
    static double tick=0.02;//s
    static int radius, org;//km
    //from t0


    static double [][][]PlayerCoordinates;//new+old
    static double[][][] Dn = null;
    static int [][] Dn_constraint=null;
    static double [][] Dn_closeness=null;
    static int[][] Dn_eligibility=null;
    static double[][] [] W;
    static int [] VMS;
        
    
    
    static double Wnew[][][] = null;// new added groups
    static double[][][] Dnew = null;
    static int[][] Dnew_eligibility = null;
    static int[] VMSnew = null;
    static int newSets;
    static double[][][] PlayersLocationNewGroups = null;
    
    
    static double Wold[][][] = null; //after disepeared groups
    static int[][] Dnold_eligibility = null;
    static int[] VMSold = null;
    static List<Integer> solold = null;
    static double PlayerCoordinatesold[][][] = null;

    //get data
    static double gamma;
    static int threshold;
    static double WI[][][] = null;//the first got before any change
    static double[][][] DnI = null;
    static double[][] DnI_closeness = null;
    static int[][] DnI_eligibility = null;
    static int[] VMSI = null;
    static double [][] BWI= null;
    static double[][]ServersCoordinatesI=null;
    static double[][][] PlayersCoordinatesI= null;
    static List<Integer>solution,solution_identique;
    static int [][] C_prev;
    static int [][] C_org;
    static double [] CDH_org;
    static double[][] BW_org;
    static double [] CDHt;
    static int[][] Ct;
    static int[][] C_tprev;
   
    
   
    //intermediaire
    static List<Integer> connectedGroupsToTn;
    static List<List<Integer>> mapping_connectedGroupOld_newMap;
    static List<Integer> LeftGroups;
    static double axesXmin = 0.0,axesYmin = 0.0,axesYmax,axesXmax;
    static double [][] enabledServersCoordinates;
    static int[] enabledServers;
    static List<List<Integer>> leftPlayersPerGroup;
    static List <Integer> changedOldConnectedGroups;
    static int candidateForMigration[];
     
    static double [][][]PlayerCoordinates_mig;//only To migrate
    static double[][][] Dn_mig = null;
    static int [][] Dn_constraint_mig=null;
    static double [][] Dn_closeness_mig=null;
    static int[][] Dn_eligibility_mig=null;
    static double[][] [] W_mig;
    static int [] VMS_mig;
    static List<Integer>sol_mig;
    static List<List<Integer>> mapping_connectedGroupmig_newMap;
    static double [][] enabledServersCoordinates_mig;
    static int sets_mig, edges_mig;
    static Zone [] zones_mig;
    static int[][] C_mig;
    static double CDH_mig[];
    static double BW_mig[][];
    static int thr_mig = 20;
    static List<Integer> disabledServers ;
    
    static double [][][]PlayerCoordinatesAll;//new+old
    static double[][][] Dn_all = null;
    static int [][] Dn_constraint_all=null;
    static double [][] Dn_closeness_all=null;
    static int[][] Dn_eligibility_all=null;
    static double[][] [] W_all;
    static int [] VMS_all;
    static int[][] C_all;
    static double CDH_all[];
    static double[][] BW_all;
    //zoning
    static Zone[] zones_all;
    static int withoutNew;

    static boolean animation;
    static int t;
    private double cummulative_cost;
    private double cummulative_clientPaidFee;
    public DynamicOperations(boolean bool, int t,float change) 
    {
        this.change = change;
        setProportionOfGroupsToAppear = change;//.1f;
        setProportionOfGroupsToDisappear = change;
        setProportionOfFogNodesToAppear = change;
        setProportionOfFogNodesToDisappear = change;
        
       System.out.println("___________________________Instant: "+t+"___________________________");
        alphaRange = alphaRange=new LinkedHashMap<Double,Double>();
        alphaRange.put(25.0, 4.47);// for more details see paper @Scaling in the space-time of the Internet
        alphaRange.put(50.0, 3.12243439);
        alphaRange.put(100.0, 2.6574);
        alphaRange.put(200.0, 2.8744);
        alphaRange.put(500.0, 2.898);
        alphaRange.put(1400.0, 2.8428);
        alphaRange.put(24576.0, 2.7);
        
        distance = (sessionRange * velocity);
        
        //------Animation--Visual----------------------------------
        this.t = t;
        this.animation = bool;
 
    }
    
    
    //read data from files
    public Input_Data apply(String dynamicPath,String f1, String f2, String f3, String f4, String f6, String f7, String f5, int config, int threshold,int rank, int id,int velocity)
    {
        this.threshold = threshold;
        this.velocity = velocity;
        if(velocity < 15)  velocity_char = "Low";
        if(velocity >= 15) velocity_char = "Medium";
        if(velocity >= 30) velocity_char = "High";
        
        gamma = (double)(100 - threshold)/100;
        try {
            GetData(f1);
             calculate_radius();
            getPlayersCoordinates(f6);
            getServersCoordinates(f7);
            retreiveSolution(f2);
          
            List<Integer> activeNodes = retreiveActiveServers(f3);
            List<Double> serversData = retreiveServersUsage(f4);
            retainRemainedOverallCap(edgeCap[addedC][0], edges, serversData, activeNodes);
            rangeX= rangeY = axesXmax = axesYmax= retainSurface(rank, f5);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(DynamicOperations.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(DynamicOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        
        if(Ct[0][0] == 1000) addedC =0;
        if(Ct[0][0] == 5000) addedC =1;
        
        sets_org = sets;
        String instance = String.valueOf(config);
        String type;
        String delimiter = "/";//local \\

        System.out.println("Start: ");
        Input_Data ipt = dynamic_scenario(animation);
        try 
        {
         

            save_cood(t,PlayerCoordinatesAll ,enabledServersCoordinates,dynamicPath, instance, delimiter, "Without_Q", id,threshold,change);
            save_cood(t,PlayerCoordinates_mig ,enabledServersCoordinates_mig,dynamicPath, instance, delimiter , "With_Q", id,threshold,change);
//            initialize(staticPath, dynamicPath, type, "data.txt", delimiter);
            type = "DataSet";
            save_t(t, sets, edges, nbOfplayersPerSet, Dn_all, Dn_closeness_all, Dn_eligibility_all, W_all, C_all, CDH_all, VMS_all, BW_all,solold, dynamicPath, delimiter, instance, type, "Without_Q", id,0,0,threshold,change);
            save_t(t, sets_mig, edges_mig, nbOfplayersPerSet, Dn_mig, Dn_closeness_mig, Dn_eligibility_mig, W_mig, C_mig, CDH_mig, VMS_mig, BW_mig,sol_mig, dynamicPath, delimiter, instance, type, "With_Q", id,ipt.cummulative_cost,ipt.cummulative_clientPaidFee,threshold,change);
            
            System.out.println("Save Input!");
            save_Input (dynamicPath, "Input_Output", delimiter, config, id, sets, sets_mig,  edges, edges_mig, sol_mig.size(), withoutNew,threshold,change);
            
            System.out.println("To ensure ");
            System.out.println("sets: "+ipt.sets);
        } 
        catch (IOException ex)
        {
            Logger.getLogger(DynamicOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return ipt;
    }
    
    public Input_Data dynamic_scenario(boolean bool)
    {
        //move_centroid();
        //Data data = put_shadow_on_servers_outOfArea(solution,C_tprev,Ct, CDHt,BWI);
        
       
        move_players();
       
        
        System.out.println("_____________Ok move players________");
        Data data = departure_in_fogNodes(solution,C_tprev,Ct, CDHt,BWI);
        System.out.println("_____________Ok departure_in_fogNodes________");
        C_org = data.C ;
        C_prev = data.C_prev;
        CDH_org = data.CDH;
        BW_org = data.BW;
        edges = data.edges;
      
        enabledServersCoordinates = data.enabledServersCoordinates;
        enabledServers = data.enabledServers;
        initializeZoning(bool);

        disAppear_Groups(solution);

        appear_Groups();
//        dynamic_in_Groups();
        
        
        System.out.println("---------------------------------STATIC------------------------------------------------");
        data = combine_old_new_groups_coordinates(PlayerCoordinatesold, PlayersLocationNewGroups);
        PlayerCoordinatesAll = data.PlayerCoordinatesAll;
        sets = PlayerCoordinatesAll.length;
        withoutNew = PlayerCoordinatesold.length;
        System.out.println("All: "+(PlayerCoordinatesold.length+newSets)+" == "+sets);
        data = combine_old_new_groups_data(Wold, Wnew, VMSold, VMSnew, sets);
        W_all = data.W;
        VMS_all = data.VMS;
        zones_all = map_zoning(enabledServersCoordinates, PlayerCoordinatesAll,zones_all);

//        for(Zone z:zones_all)
//            System.out.println("number of sets in "+z.sets.size());

        enabledServersCoordinates_mig = new double [enabledServersCoordinates.length][2];
        System.arraycopy(enabledServersCoordinates, 0, enabledServersCoordinates_mig, 0, enabledServersCoordinates.length);     
                
        data = repair(addedC, C_org, CDH_org,BW_org, zones_all, edges, enabledServersCoordinates,C_all,CDH_all,BW_all,W_all,PlayerCoordinatesAll);
           
        zones_all = data.zones;
        edges = data.edges;
        enabledServersCoordinates = data.enabledServersCoordinates;
        C_all = data.C;
        CDH_all= data.CDH;
        BW_all = data.BW;
        
//        display_sets_nodes_zone(PlayerCoordinatesAll, enabledServersCoordinates, sets, nbOfplayersPerSet, edges, zones_all, animation);
        
        
        System.out.println("old+new: "+sets+" == "+PlayerCoordinatesAll.length);
        System.out.println("edges: "+edges+" == "+enabledServersCoordinates.length);
        System.out.println("Sets: "+sets+" Servers: "+edges);
        
        //Network parameters
        Dn_all = calculateNetworkDelay(Dn_all, PlayerCoordinatesAll, enabledServersCoordinates, sets, nbOfplayersPerSet, edges, zones_all, animation);//distance: Dn(p,e)
            
        data = calculateDeadConstraintPlayers(Dn_all,  Dn_closeness_all, Dn_constraint_all,  edges);
        
        Dn_constraint_all = data.Dn_constraints;
        Dn_closeness_all = data.Dn_closeness;
        System.out.println(" "+ Dn_constraint_all.length+" "+Dn_all.length+" "+Dn_closeness_all.length);
        Dn_eligibility_all=new int[Dn_constraint_all.length][edges];
        Dn_eligibility_all = checkElegibility(Dn_eligibility_all,Dn_closeness_all,Dn_constraint_all,threshold,zones_all);
        

       
        
        Input_Data ipt = new Input_Data();
        ipt.sets = sets;
        ipt.edges = edges;
        ipt.nbPlayersSet = nbOfplayersPerSet;
        ipt.W = W_all;
        ipt.C = C_all;
        ipt.CDH = CDH_all;
        ipt.Dn = Dn_all;
        ipt.Dn_eligibility = Dn_eligibility_all;
        ipt.Dn_closeness_all = Dn_closeness_all;
        ipt.VMS = VMS_all;
        ipt.BW = BW_all;
        ipt.edgesLocation_all=enabledServersCoordinates;
        ipt.playersLocation_all=PlayerCoordinatesAll;
        ipt.xMin_all=(int)axesXmin;
        ipt.xMax_all=(int)axesXmax;
        ipt.yMin_all=(int)axesYmin;
        ipt.yMax_all=(int)axesYmax;
        ipt.zones_all=zones_all;
        ipt.radius = radius;
        ipt.org = org;
        ipt.sol_all = solold;
     
        System.out.println("--------------------------------Migration Case------------------------------------------------");
        migration_Decision_Making(solold,W_all);
        data = calculate_cummulative_cost(Wold, C_org, CDH_org, solold, candidateForMigration);
        C_prev = data.C_prev;
        cummulative_cost = data.cummulative_cost;
        cummulative_clientPaidFee = data.cummulative_clientPaidFee;
        System.out.println("cummulative Cost: "+cummulative_cost);
        System.out.println("cummulative Client paid fee: "+cummulative_clientPaidFee);
        
        update();
        data = combine_old_new_groups_coordinates(PlayerCoordinates_mig, PlayersLocationNewGroups);
        PlayerCoordinates_mig = data.PlayerCoordinatesAll;
     
        System.out.println("New sets: "+newSets);
        System.out.println(""+(sol_mig.size()+newSets)+"_______====____"+PlayerCoordinates_mig.length);
        data = combine_old_new_groups_data(W_mig, Wnew, VMS_mig, VMSnew,(sol_mig.size()+newSets));
        W_mig = data.W;
        VMS_mig = data.VMS;
        edges_mig = C_prev.length;
//        System.out.println("edges_mig "+edges_mig+" enabled: "+enabledServersCoordinates_mig.length);
        zones_mig = map_zoning(enabledServersCoordinates_mig, PlayerCoordinates_mig,zones_mig);
        data = repair(addedC, C_prev, CDH_org,BW_org, zones_mig, edges_mig, enabledServersCoordinates_mig, C_mig, CDH_mig,BW_mig,W_mig,PlayerCoordinates_mig);
        sets_mig = sol_mig.size() + newSets;
        zones_mig = data.zones;
        edges_mig= data.edges;
        enabledServersCoordinates_mig = data.enabledServersCoordinates;
        C_mig = data.C;
        CDH_mig= data.CDH;
        BW_mig = data.BW;
        
        System.out.println("To migrate +new: "+sets_mig+" == "+PlayerCoordinates_mig.length);
        System.out.println("edges: "+edges_mig+" == "+enabledServersCoordinates_mig.length);
        System.out.println("Sets: "+sets_mig+" Servers: "+edges_mig);
        //Network parameters
        Dn_mig = calculateNetworkDelay(Dn_mig, PlayerCoordinates_mig, enabledServersCoordinates_mig, sets_mig, nbOfplayersPerSet, edges_mig, zones_mig, false);//distance: Dn(p,e)
        data = calculateDeadConstraintPlayers(Dn_mig,  Dn_closeness_mig, Dn_constraint_mig,  edges_mig); 
        
        Dn_closeness_mig = data.Dn_closeness;
        Dn_constraint_mig = data.Dn_constraints;
        Dn_eligibility_mig=new int[Dn_constraint_mig.length][edges];
        Dn_eligibility_mig= checkElegibility(Dn_eligibility_mig,Dn_closeness_mig,Dn_constraint_mig,threshold,zones_mig);
           
        ipt.sets_mig = sets_mig;
        ipt.edges_mig = edges_mig;
        ipt.nbPlayersSet = nbOfplayersPerSet;
        ipt.W_mig= W_mig;
        ipt.C_mig = C_mig;
        ipt.CDH_mig = CDH_mig;
        ipt.Dn_mig = Dn_mig;
        ipt.Dn_eligibility_mig = Dn_eligibility_mig;
        ipt.Dn_closeness_mig = Dn_closeness_mig;
        ipt.VMS_mig = VMS_mig;
        ipt.BW_mig = BW_mig;
        ipt.sol_mig = sol_mig;
        ipt.cummulative_cost = cummulative_cost;
        ipt.cummulative_clientPaidFee = cummulative_clientPaidFee;
        ipt.edgesLocation_mig=enabledServersCoordinates_mig;
        ipt.playersLocation_mig=PlayerCoordinates_mig;
        ipt.xMin_mig=(int)axesXmin;
        ipt.xMax_mig=(int)axesXmax;
        ipt.yMin_mig=(int)axesYmin;
        ipt.yMax_mig=(int)axesYmax;
        ipt.zones_mig=zones_mig;

        
        return ipt;
    }
    
    
    
    static void migration_Decision_Making(List<Integer> sol,double[][][]W)
    {
        candidateForMigration = new int[sol.size()];
        sol_mig = new ArrayList<>();
        for (int i = 0; i < candidateForMigration.length; i++) 
            candidateForMigration[i] = -1;
        
        for (int i = 0; i < candidateForMigration.length; i++) 
        {

            int prev_placement = sol.get(i);
            double latency[] = calculate_latency(mapping_connectedGroupOld_newMap.get(i).get(1), prev_placement,W,PlayerCoordinatesold,enabledServersCoordinates);//1
            
            element el = MaxLatency(latency);

            if (el.percent <= thr_mig)//optimize
             {
//                System.out.println("set " + set + " won't be candidate for migration");
                candidateForMigration[i] = -1;
             }
            else 
            {
//                System.out.println("set " + set + " will be candidate for migration");
                candidateForMigration[i] = 1;
                sol_mig.add(prev_placement);//i
            }
        }
        
    }
    
    
    static void update()
    {
        W_mig = new double[sol_mig.size()][nbOfplayersPerSet][dim];
        VMS_mig = new int[sol_mig.size()];
        PlayerCoordinates_mig = new double[sol_mig.size()][nbOfplayersPerSet][2];
        mapping_connectedGroupmig_newMap = new ArrayList<>();
        
//        System.out.println("Cood Old: "+Arrays.deepToString(PlayerCoordinatesold));
        int count = 0;
        for (int i = 0; i < candidateForMigration.length; i++) 
        {
            if (candidateForMigration[i] == 1)
            {
                List<Integer> map = new ArrayList<>();
                for (int p = 0; p < nbOfplayersPerSet; p++) 
                {
                    for (int xy = 0; xy < 2; xy++) 
                        PlayerCoordinates_mig[count][p][xy] = PlayerCoordinatesold[i][p][xy];
                    for (int r = 0; r < dim; r++) 
                    {
                        W_mig[count][p][r] = Wold[i][p][r];
                    }
                    VMS_mig[count] = VMSold[i];
                }
                map.add(i); map.add(count);
                mapping_connectedGroupmig_newMap.add(map);
                count++;
            }
        }
    }
    
    
    static double [] calculate_latency(int set, int y,double [][][]W_all,double[][][]PlayerCoordinatesold,double[][]enabledServersCoordinates)
    {
        double latency [] =new double [nbOfplayersPerSet];
        double processing =   calculateProcessing(W_all, set);
        for (int p =0;p< nbOfplayersPerSet; p++)
        {
            double networking = claculateNetworkDelay(PlayerCoordinatesold, enabledServersCoordinates, set, p, y);
            latency[p] = processing + networking;
        }
        return latency;
    }
    
    static double calculateProcessing(double [][][]W,int set)
    {
        int VMcpu=(int)((W[set][0][0]*nbOfplayersPerSet)/tick);
        if(VMcpu<1000)
            VMcpu=1000;
        if(VMcpu>1000 && VMcpu<2000 )
            VMcpu=2000;
        return (((double)(W[set][0][0]))/(VMcpu))*1000;
    }
    
    static element MaxLatency(double [] latency ) 
    {
        double max = -1;
        element el = new element();
        double percent = 0;

        for (int p = 0; p < latency.length; p++) 
        {
            if (max < latency[p]) 
                max = latency[p];
 
            if (latency[p] > Dmax) 
                percent++;
        }
        el.max = max;
        el.percent = (percent * 100) / (nbOfplayersPerSet * gamma );

        return el;
    }
    
    
    static void appear_Groups()
    {
        System.out.println("newSet: "+newSets);
        newSets = (int) (sets_org * setProportionOfGroupsToDisappear);
        Wnew = new double[newSets][nbOfplayersPerSet][dim];
        
        if(addedR == 0)
            smallS = nbOfplayersPerSet;
        if(addedR == 1)
            mediumS = nbOfplayersPerSet;
        if(addedR == 2)
            largeS = nbOfplayersPerSet;
        if(addedR == 3)
            xtraLargeS = nbOfplayersPerSet;
        
        generateServiceRequirmentSML(Wnew, newSets, smallS, mediumS, largeS, xtraLargeS);

        double [][]centroid=new double[newSets][2];
        PlayersLocationNewGroups = generatePlayersCoordinates(centroid,PlayersLocationNewGroups, newSets, nbOfplayersPerSet,radius);
        VMSnew = vmSize(newSets);

        //print
        System.out.println("New sets to appear: " + newSets);
//        System.out.println(Arrays.deepToString(Wnew));
//        System.out.println("----"+Arrays.deepToString(Dnew));
//        System.out.println("New: "+ Arrays.deepToString(PlayersLocationNewGroups));
    }
    
     static int[] vmSize(int sets) {
        Random rand = new Random();
        int[] vms = new int[sets];
        for (int s = 0; s < sets; s++) {
            vms[s] = vmSizes[rand.nextInt(vmSizes.length)];
        }
        return vms;
    }
    
    
     static void disAppear_Groups(List<Integer> sol) {
        //Delete from W, Dn, Dn_eligibility, SizeVm , sets--, solution, remove charged resources from C
        //Create W_new, W_all, Dn_new, Dn_all, Dn_eligibility_new, Dn_eligibility_all, SizeVm_new and SizeVm_all, solution, C
       
        int setsToDisAppear = (int) (sets * setProportionOfGroupsToDisappear);

        Random rand = new Random();
        
        while (LeftGroups.size() < setsToDisAppear) 
        {
            int group = rand.nextInt(sets);
            if (!LeftGroups.contains(group)) 
            {
                LeftGroups.add(group);
            }
        }
        
        System.out.println(" groups to leave: "+LeftGroups.size()+" Predicted percentage: "+setsToDisAppear);
         System.out.println(" predicted percentage: "+sets+" * "+setProportionOfGroupsToDisappear);
        setProportionOfGroupsToDisappear = (float)((double)LeftGroups.size()/sets);
         System.out.println("disappeared: "+setProportionOfGroupsToDisappear);
            sets = sets - LeftGroups.size();


        //alter sol, w, Dn, VMS
        List<Integer> sol_copy = new ArrayList<>();
        for (int set = 0; set < sol.size(); set++) 
        {
            sol_copy.add(enabledServers[sol.get(set)]);
        }

        sol = new ArrayList<>();
        solold = new ArrayList<>();
        connectedGroupsToTn = new ArrayList<>();
        mapping_connectedGroupOld_newMap = new ArrayList<>();
        for (int set = 0; set < sol_copy.size(); set++) {
            if (!LeftGroups.contains(set)) 
            {
                sol.add(sol_copy.get(set));
                solold.add(sol_copy.get(set));
                connectedGroupsToTn.add(set);
            }

        }

        
        //print
        System.out.println("Number of sets to dispear is: " + setsToDisAppear + " sol size: " + solold.size() + " = " + sets);
        for (int i = 0; i < LeftGroups.size(); i++) 
        {
            System.out.print(" " + LeftGroups.get(i));
        }
        System.out.println("");
        System.out.println("Updated Sol after groups departure: "+sol.size());
        for (int i = 0; i < sol.size(); i++) 
        {
            System.out.print(" " + sol.get(i));
        }
         System.out.println("");
        System.out.println("Updated remaining groups after groups departure: "+connectedGroupsToTn.size());
        for (int i = 0; i < connectedGroupsToTn.size(); i++) 
        {
            System.out.print(" " + connectedGroupsToTn.get(i));
        }
        System.out.println("");

        int count = 0;
        Wold = new double[sets][nbOfplayersPerSet][dim];
        for (int set = 0; set < sol_copy.size(); set++) 
        {
            if (!LeftGroups.contains(set))
            {
                List<Integer> map = new ArrayList<>();
                for (int p = 0; p < nbOfplayersPerSet; p++) 
                {
                    for (int r = 0; r < dim; r++) 
                    {
                        Wold[count][p][r] = WI[set][p][r];
                    }
                }
                map.add(set); map.add(count);
                mapping_connectedGroupOld_newMap.add(map);
                count++;
            }
        }
//        System.out.println(Arrays.deepToString(Wold));

        System.out.println("number of sets = "+sets +" == "+connectedGroupsToTn.size());
        PlayerCoordinatesold = new double[sets][nbOfplayersPerSet][2];
        count = 0;
        for (int s = 0; s < connectedGroupsToTn.size(); s++) 
        {
            int set = connectedGroupsToTn.get(s);
            for (int p = 0; p < nbOfplayersPerSet; p++)
            {
                //surface
                double x = (PlayersCoordinatesI[set][p][0]<axesXmin? axesXmin: PlayersCoordinatesI[set][p][0]);
                double y = (PlayersCoordinatesI[set][p][1]<axesYmin? axesYmin: PlayersCoordinatesI[set][p][1]);

                x = (x>axesXmax? axesXmax: x);
                y = (y>axesYmax? axesYmax: y);

                PlayerCoordinatesold[count][p][0] = PlayersCoordinatesI[set][p][0];
                PlayerCoordinatesold[count][p][1] = PlayersCoordinatesI[set][p][1];
//                    if(x < axesXmin || x >axesXmax || y < axesYmin || y >axesYmax)
//                        d

            }
            count++;
        }
//        System.out.println(Arrays.deepToString(PlayerCoordinatesold));

        count = 0;
        VMSold = new int[sets];
        for (int set = 0; set < sol_copy.size(); set++) 
        {
            if (!LeftGroups.contains(set)) 
            {
                VMSold[count] = VMSI[set];
                count++;
            }
        }
    }
     
     
     static void dynamic_in_Groups()
     {
         leftPlayersPerGroup = new ArrayList<>();
         changedOldConnectedGroups = new ArrayList<>();
         //move centroids
         for(int g=0; g<connectedGroupsToTn.size(); g++)
         {
             int set = mapping_connectedGroupOld_newMap.get(g).get(1);
             players_in_group_departure_2(set, leftPlayersPerGroup);
         }
         
         for(int g=0; g<changedOldConnectedGroups.size(); g++)
         {
             players_in_group_arrival(changedOldConnectedGroups.get(g), leftPlayersPerGroup.get(g));
         }
     }
     
     static void players_in_group_departure(int set, List<List<Integer>> leftPlayersPerGroup)
     {
         double centroidX = PlayerCoordinatesold[set][0][0];
         double centroidY = PlayerCoordinatesold[set][0][0];
         List <Integer> leftPlayers = new ArrayList<>();
         boolean bool = false;
         for( int p=1; p<PlayerCoordinatesold[set].length; p++)
         {
              double value =Math.round( Math.sqrt(Math.pow(PlayerCoordinatesold[set][p][0] - centroidX, 2.00)
                            + Math.pow( PlayerCoordinatesold[set][p][1] - centroidY, 2.00)));
              if( value > radius)
              {
                  //the player is out of range and considered has left the session. 
                  leftPlayers.add(p);
                  bool = true;
              }
         }
         if (bool) 
         {
             leftPlayersPerGroup.add(leftPlayers);
             changedOldConnectedGroups.add(set);
         }
     }
     
     static void players_in_group_departure_2(int set, List<List<Integer>> leftPlayersPerGroup)
     {
         Random rand = new Random();
         List <Integer> leftPlayers = new ArrayList<>();
         
         int nbToLeave = (int) (nbOfplayersPerSet * setProportionOfPlayersToDisappear);
         for (int p =0; p< nbToLeave; p++)
         {
             int player = rand.nextInt(nbOfplayersPerSet);
             while (leftPlayers.contains(player) || player == 0)
                 player = rand.nextInt(nbOfplayersPerSet);
             
             leftPlayers.add(player);
         }
         
          leftPlayersPerGroup.add(leftPlayers);
          changedOldConnectedGroups.add(set);   
     }
     
     
     static void players_in_group_arrival(int set, List<Integer> leftPlayersPerGroup)
     {
         double uniform1;
         double uniform2;
         double xCenter = PlayerCoordinatesold[set][0][0];
         double yCenter = PlayerCoordinatesold[set][0][1];
         for (int p=0;p < leftPlayersPerGroup.size();p++) 
            {
              uniform1=Math.random();
              double r = radius * Math.sqrt(uniform1);
              uniform2=Math.random();
              double theta = 2 * Math.PI*uniform2;
              
              double x = (r * Math.cos(theta)+xCenter)% axesXmax;
              double y = (r * Math.sin(theta)+yCenter) % axesYmax;
                      
              PlayerCoordinatesold[set][leftPlayersPerGroup.get(p)][0] = x;
              PlayerCoordinatesold[set][leftPlayersPerGroup.get(p)][1] = y;
            }
     
     }
     
     
     static Zone[] map_zoning(double [][]enabledServersCoordinates, double [][][]PlayerCoordinatesAll, Zone [] zones)
     {
        int counter = 0;
        while (counter < enabledServersCoordinates.length) 
        {
            double x = enabledServersCoordinates[counter][0] ;
            double y = enabledServersCoordinates[counter][1];
            for(Zone z:zones)
            {
                double xx=z.x;
                double yy=z.y;
                double zc=z.cellSize;
                if((xx<=x && x<=xx+zc) && (yy<=y&& y<=yy+zc))
                {
                    z.servers.add(counter);
                    break;
                }
            }   
            counter++;
        }
         
        int count = 0;

        for (int set=0; set< PlayerCoordinatesAll.length;set++) 
        {
            double xCenter = PlayerCoordinatesAll[set][0][0] ;
            double yCenter = PlayerCoordinatesAll[set][0][1];
            boolean checked = false;
            for(Zone z:zones)
            {  
                    double x=z.x;
                    double y=z.y;
                    double zc=z.cellSize;
                    if((x<=xCenter  && xCenter  <=x+zc) && 
                            (y<=yCenter && yCenter<=y+zc))
                    {
                        if(!z.sets.contains(set))
                        {
                             z.sets.add(set);
                             checked = true;
                             break;
                        } 
                    }  
            }
            if (!checked)
                count++;
        }
      
        return zones;
     }

     static Data combine_old_new_groups_coordinates(double [][][]PlayerCoordinatesOld, double [][][]PlayerCoordinatesNew)
     {
         int all = PlayerCoordinatesOld.length + PlayerCoordinatesNew.length;
         //sets = all;
         double [][][] PlayerCoordinatesAll = new double [all] [nbOfplayersPerSet] [2];
         System.arraycopy(PlayerCoordinatesOld, 0, PlayerCoordinatesAll, 0, PlayerCoordinatesOld.length);  
         System.arraycopy(PlayerCoordinatesNew, 0, PlayerCoordinatesAll, PlayerCoordinatesOld.length, PlayerCoordinatesNew.length); 
         Data data =new Data();
         data.PlayerCoordinatesAll = PlayerCoordinatesAll;
         return data;
     }
     
     static Data combine_old_new_groups_data(double Wold[][][], double Wnew[][][], int []VMS, int[]VMSnew, int sets)
     {
         double [][][] W_all_ = new double [sets][nbOfplayersPerSet][dim];
         int []VMS_all_ =new int[sets];
         System.arraycopy(Wold, 0, W_all_, 0, Wold.length); 
         System.arraycopy(Wnew, 0, W_all_, Wold.length, Wnew.length); 
         
         System.arraycopy(VMS, 0, VMS_all_, 0, VMS.length); 
         System.arraycopy(VMSnew, 0, VMS_all_, VMS.length, VMSnew.length); 
         
         Data data = new Data();
         data.VMS = VMS_all_;
         data.W=W_all_;
         return data;
     }
     
     static void calculate_radius()
     {
        double pr=opitimisticProcessingD();
        System.out.println("Optimistic pr: "+pr);
        System.out.println("gamma: "+gamma);
        double rtt=(Dmax-pr)/2;
        double p1=((double)(rtt)/Math.sqrt(gamma));
        rTemp=(p1);//-pr);
        System.out.println("Dr: "+rTemp);
        double radiusTemp=(rTemp);
        radius=(int)(Math.pow(radiusTemp, 2)/Math.pow(alpha, 2));
        System.out.println("Radius: "+radius);
        
        double p1org=((double)(Dmax-pr)/Math.sqrt(1));
        double rTemporg=(p1org);//-pr);
        System.out.println("Dr: "+rTemporg);
        double radiusTemporg=(rTemporg);//pr+  /2
        System.out.println("radius temp: "+radiusTemporg);
        org=(int)(Math.pow(radiusTemporg, 2)/Math.pow(alpha, 2));
        System.out.println("orginal radius: "+org);
     }
     
     
      static synchronized double opitimisticProcessingD()
    {
        int VMcpu=(int)((serviceGameReq[addedR][0]*nbOfplayersPerSet)/tick);
        if(VMcpu<1000)
            VMcpu=1000;
        if(VMcpu>1000 && VMcpu<2000 )
            VMcpu=2000;
        return (((double)(serviceGameReq[addedR][0]))/(VMcpu))*1000;
    }
     
     
     static void move_centroid()
     {
         double [] cenroidsX = new double[sets];
         double [] cenroidsY = new double[sets];
         double a = (double)(direction * Math.PI) / 180;
         for(int g =0; g<sets;g++)
         {
            int set =g;// connectedGroupsToTn.get(g);
            double centroidX = PlayersCoordinatesI[set][0][0];
            double centroidY = PlayersCoordinatesI[set][0][1];
             //Compute the change in position
            double delta_x =   velocity * Math.cos(a); //*t
            double delta_y =   velocity * Math.sin(a);// *t
            // Add that to the existing position
            double new_x = centroidX + delta_x;
            double new_y = centroidY + delta_y;
            PlayersCoordinatesI[set][0][0] = new_x;
            PlayersCoordinatesI[set][0][1] = new_y;
            
            cenroidsX[set] = new_x;
            cenroidsY[set] = new_y;
         }
         define_new_base(cenroidsX, cenroidsY);
     }
     
     //Second_version
     static void move_players()
     {
         double step = radius *2.5;
         Random rand = new Random();
         double a = (double)(directionPositive * Math.PI) / 180;

         
         List<Integer> moved = new ArrayList<>();
         int nbToMoveOverall =(int)( sets * setProportionOfMovedPlayers);
         while(moved.size() < nbToMoveOverall)
         {
             int g = rand.nextInt(sets);
                while(moved.contains(g))
                    g = rand.nextInt(sets);
             double xcenter = PlayersCoordinatesI[g][0][0];
             double ycenter = PlayersCoordinatesI[g][1][0];
             
                //Compute the change in position
            double delta_x =   velocity * Math.cos(a); //*t
            double delta_y =   velocity * Math.sin(a);// *t
             //to ensure coherent deplacement of all the players beloging to the same group
             double distancex =axesXmax - (xcenter+delta_x );
             double distancey =axesXmax - (ycenter +delta_y );
             if( distancex <= radius || xcenter+ delta_x >= axesXmax)
                 delta_x += step; 
             if(distancey <= radius ||  ycenter+ delta_y >= axesYmax)
                 delta_y += step;
           
            double old_x = PlayersCoordinatesI[g][0][0];
            double old_y = PlayersCoordinatesI[g][0][1];

            double new_x = (old_x + delta_x)  % (axesXmax) ;
            double new_y = (old_y + delta_y) % (axesYmax);

            PlayersCoordinatesI[g][0][0] = new_x;
            PlayersCoordinatesI[g][0][1] = new_y;
            double uniform1=Math.random();
            double uniform2=Math.random();
             for (int p=1;p < nbOfplayersPerSet;p++) 
            {
              uniform1=Math.random();
              double r = radius * Math.sqrt(uniform1);
              uniform2=Math.random();
              double theta = 2 * Math.PI*uniform2;
              
              double x = (r * Math.cos(theta)+new_x) ;
              double y = (r * Math.sin(theta)+new_y) ;
            
             PlayersCoordinatesI[g][p][0] = x;
             PlayersCoordinatesI[g][p][1] = y;
            }
            
           moved.add(g);
         
         }
         System.out.println("Finish moving!!!! ");
     }
     
     
     
     static Data put_shadow_on_servers_outOfArea(List<Integer> solution, int [][]C_tprev, int [][] Ct, double CDHt[], int[][]BWI)
     {
         List<double[]> ServersCoordinates = new ArrayList<>();
         disabledServers = new ArrayList<>();
         int[] enabledServers = new int[edges];
         int count = 0;
         for( int s =0; s < edges; s++)
         {
//             System.out.println("Server: x = "+ServersCoordinatesI[s][0]+" ,y = "+ServersCoordinatesI[s][1]);
//             if ( ServersCoordinatesI[s][0] >= axesXmin && ServersCoordinatesI[s][1] >= axesYmin)
             if( solution.contains(s) || (ServersCoordinatesI[s][0] >= axesXmin && ServersCoordinatesI[s][1] >= axesYmin))
             {
                 double [] xy = {ServersCoordinatesI[s][0], ServersCoordinatesI[s][1]};
                 ServersCoordinates.add(xy);
                 enabledServers[s] = count;
                 count++;
//                 System.out.println("Yes!");
             }
             else
             {
//                 System.out.println("No!");
                 disabledServers.add(s);
                 enabledServers[s] = -1;
             }
         }
         
         System.out.println("Servers to remove: "+disabledServers.size());
         for( int s =0; s < disabledServers.size(); s++)
         {
             System.out.print(" "+disabledServers.get(s));
         }
         System.out.println("");
         
         //remove groups already assigned to disbled servers
         LeftGroups = new ArrayList<>();
         for (int g=0;g<solution.size(); g++)
         {
             if (disabledServers.contains(solution.get(g)))
             {
                 LeftGroups.add(g);
             }
         }
         
         enabledServersCoordinates = new double[ServersCoordinates.size()][2];
         count = 0;
         
         for( int s =0; s < ServersCoordinates.size(); s++)
         {
                enabledServersCoordinates[s][0] = ServersCoordinates.get(s)[0];
                enabledServersCoordinates[s][1] = ServersCoordinates.get(s)[1];
         }  

         C_org = new int [ServersCoordinates.size()][dim];
         C_prev = new int [ServersCoordinates.size()] [dim];
         CDH_org = new double[ServersCoordinates.size()];
         BW_org = new double[ServersCoordinates.size()][ServersCoordinates.size()];
         count = 0;
         for(int s=0; s < edges; s++)
         {
             if(!disabledServers.contains(s))
             {
                 C_org [count][0] = Ct[s][0];
                 C_prev [count][0] = C_tprev[s][0];
                 CDH_org[count] = CDHt[s];
                 count++;
             }
         }
         
         count = 0;
         for(int s=0; s < edges; s++)
         {
             if(!disabledServers.contains(s))
             {
                 int j = 0;
                 for(int e=0; e < edges; e++)
                 {
                     if(!disabledServers.contains(e))
                     {
                         BW_org[count][j] = BWI[s][e];
                         BW_org[j][count] = BWI[s][e];
                         j++;
                     }
                 }
                 count ++;
             }
         }
         
         
         edges = ServersCoordinates.size();
         
         Data data = new Data();
         data.C = C_org;
         data.C_prev = C_prev;
         data.CDH = CDH_org;
         data.BW = BW_org;
         data.edges = edges;
         data.enabledServersCoordinates = enabledServersCoordinates;
         data.enabledServers = enabledServers;
         
         return data;
     }
     
     
     //Version_2
      static Data departure_in_fogNodes (List<Integer> solution, int [][]C_tprev, int [][] Ct, double CDHt[], double[][]BWI)
     {
         Random rand = new Random();
         List<double[]> ServersCoordinates = new ArrayList<>();
         disabledServers = new ArrayList<>();
         int[] enabledServers = new int[edges];
       
         int nbLeftFogNodes = (int) (edges * setProportionOfFogNodesToDisappear);
         int cardinality = calculateCardinalityOfSolution(solution);
         if(nbLeftFogNodes > (edges - cardinality))
             nbLeftFogNodes = edges - cardinality;
         
         List<Integer> candidates = new ArrayList<>();
         for( int s =0; s < edges; s++)
         {
             if(!solution.contains(s))
                 candidates.add(s);
         }
         
         //System.out.println(" "+candidates.size()+" == "+nbLeftFogNodes);
         for( int s =0; s < nbLeftFogNodes; s++)
         {
             int edge = candidates.get(rand.nextInt(candidates.size()));
             disabledServers.add(edge);
             candidates.remove(candidates.indexOf(edge));   
         } 
         
         int count = 0;
          for( int s =0; s < edges; s++)
          {
             if( !disabledServers.contains(s))
             {
                  double x = (ServersCoordinatesI[s][0]<axesXmin? axesXmin: ServersCoordinatesI[s][0]);
                  double y = (ServersCoordinatesI[s][1]<axesYmin? axesYmin: ServersCoordinatesI[s][1]);
            
                    x = (x>axesXmax? axesXmax: x);
                    y = (y>axesYmax? axesYmax: y);
       
                  
                 double [] xy = {ServersCoordinatesI[s][0], ServersCoordinatesI[s][1]};
                 ServersCoordinates.add(xy);
                 enabledServers[s] = count;
                 count++;
             }
             else
                 enabledServers[s] = -1;
         }
         
         System.out.println("Servers to remove: "+disabledServers.size());
         for( int s =0; s < disabledServers.size(); s++)
         {
             System.out.print(" "+disabledServers.get(s));
         }
         System.out.println("");
         
         //remove groups already assigned to disbled servers
         LeftGroups = new ArrayList<>();
         for (int g=0;g<solution.size(); g++)
         {
             if (disabledServers.contains(solution.get(g)))
             {
                 LeftGroups.add(g);
             }
         }
         
         enabledServersCoordinates = new double[ServersCoordinates.size()][2];
         count = 0;
         
         for( int s =0; s < ServersCoordinates.size(); s++)
         {
                enabledServersCoordinates[s][0] = ServersCoordinates.get(s)[0];
                enabledServersCoordinates[s][1] = ServersCoordinates.get(s)[1];
         }  
         
         
         C_org = new int [ServersCoordinates.size()][dim];
         C_prev = new int [ServersCoordinates.size()] [dim];
         CDH_org = new double[ServersCoordinates.size()];
         BW_org = new double[ServersCoordinates.size()][ServersCoordinates.size()];
         count = 0;
         for(int s=0; s < edges; s++)
         {
             if(!disabledServers.contains(s))
             {
                 C_org [count][0] = Ct[s][0];
                 C_prev [count][0] = C_tprev[s][0];
                 CDH_org[count] = CDHt[s];
                 count++;
             }
         }
         
         count = 0;
//         System.out.println("BWI: "+Arrays.deepToString(BWI));
         for(int s=0; s < edges; s++)
         {
             if(!disabledServers.contains(s))
             {
                 int j = 0;
                 for(int e=0; e < edges; e++)
                 {
                     if(!disabledServers.contains(e))
                     {
                         BW_org[count][j] = BWI[s][e];
                         BW_org[j][count] = BWI[s][e];
                         j++;
                     }
                 }
                 count ++;
             }
         }
      
         
         
         edges = ServersCoordinates.size();
         
         Data data = new Data();
         data.C = C_org;
         data.C_prev = C_prev;
         data.CDH = CDH_org;
         data.BW = BW_org;
         data.edges = edges;
         data.enabledServersCoordinates = enabledServersCoordinates;
         data.enabledServers = enabledServers;
         
         return data;
     }
     
     static void define_new_base(double[] centroidsX, double[] centroidsY)
     {
         axesXmin = getMin(centroidsX);
         axesYmin = getMin(centroidsY);
         
         axesXmax = getMax(centroidsX);
         axesYmax = getMax(centroidsY);
         
         System.out.println(" New area: x_min = "+axesXmin+" y_min = "+axesYmin+" x_max = "+axesXmax+" y_max = "+axesYmax);
     }
     
     
     static int calculateCardinalityOfSolution(List<Integer>solution)
     {
         List<Integer> checked = new ArrayList<>();
         
         for(int i = 0; i < solution.size(); i++)
         {
             if( !checked.contains(solution.get(i)))
                 checked.add(solution.get(i));
         }
         return checked.size();
     }
     
    
     
     
     static double claculateNetworkDelay(double[][][] PlayersCoodOld, double[][] serversCoodI,int s, int p, int e)
     {
//         System.out.println("player: "+p+" of set :"+s+" x: "+PlayersCoodOld[s][p][0]+" y: "+PlayersCoodOld[s][p][1]);
        
         double distance =Math.round( Math.sqrt(Math.pow(PlayersCoodOld[s][p][0] - serversCoodI[e][0], 2.00)
                            + Math.pow(PlayersCoodOld[s][p][1] - serversCoodI[e][1], 2.00)));
                    for (Map.Entry<Double, Double> set : alphaRange.entrySet())
                        if(distance<=set.getKey())
                        {
                            alpha=set.getValue();break;
                        }
          return alpha*Math.sqrt(distance);
     }
     
     
     
     
     static synchronized double[][][] calculateNetworkDelay(double[][][] Dn, double[][][] PlayersLocation, double[][] EdgesLocation, int sets, int nbOfplayersPerSet, int edges, Zone[]zones,boolean bool) {
    
        Dn = new double[sets][nbOfplayersPerSet][edges ];//+1
        for (int s = 0; s < sets; s++) 
        {
            for (int p = 0; p < nbOfplayersPerSet; p++) 
            {        
                double x = PlayersLocation[s][p][0];
                double y = PlayersLocation[s][p][1];
                
                for (int e = 0; e < edges; e++) 
                {
                    double distance =Math.round( Math.sqrt(Math.pow(x - EdgesLocation[e][0], 2.00)
                            + Math.pow(y - EdgesLocation[e][1], 2.00)));
                    for (Map.Entry<Double, Double> set : alphaRange.entrySet())
                        if(distance<=set.getKey())
                        {
                            alpha=set.getValue();break;
                        }
                    Dn[s][p][e]=alpha*Math.sqrt(distance);
                }
            }
        }
        return Dn;
    } 
     
     
     
     static synchronized void display_sets_nodes_zone( double[][][] PlayersLocation, double[][] EdgesLocation, int sets, int nbOfplayersPerSet, int edges, Zone[]zones,boolean bool) {
    
       
         for(Zone z:zones_all)
         {
             System.out.println("------------------------------");
             for (int s = 0; s < z.sets.size(); s++)
             {
                 System.out.println("");
                int set = z.sets.get(s);
                for(int e = 0; e<z.servers.size();e++)
                {
                     int node = z.servers.get(e);

                    for (int p = 0; p < nbOfplayersPerSet; p++) 
                    {
                        double centroidX = PlayersLocation[set][0][0];
                        double centroidY = PlayersLocation[set][1][0];

                        double x = PlayersLocation[set][p][0];
                        double y = PlayersLocation[set][p][1];

                        if (p!= 0)
                         {
                             //surface
//                            double distance =Math.round( Math.sqrt(Math.pow(centroidX - PlayersLocation[set][p][0], 2.00)
//                                    + Math.pow(centroidY - PlayersLocation[set][p][1], 2.00)));
//                            if (distance > radius)
//                            {
//                                if (centroidX < x)
//                                    x = x - axesXmax;
//                                if (centroidX > x)
//                                    x = x + axesXmax;
//
//                                if (centroidY < y)
//                                    y = y - axesXmax;
//                                if (centroidY > y)
//                                    y = y + axesYmax;
//                         }
//                              if( p == 0)
//                            {
//                                x = centroidX; y = centroidY;
//                            }
                             distance =Math.round( Math.sqrt(Math.pow(x - EdgesLocation[node][0], 2.00)
                                    + Math.pow(y - EdgesLocation[node][1], 2.00)));
                            for (Map.Entry<Double, Double> tac : alphaRange.entrySet())
                                if(distance<=tac.getKey())
                                {
                                    alpha=tac.getValue();break;
                                }
                             System.out.println(" set "+set+"p "+p+" to node "+node+" = "+alpha*Math.sqrt(distance));
                  
                        }
                           
                    }
                }
             }
         }
       
    } 
     
     
     
     // Method for getting the minimum value
  public static double getMin(double[] inputArray)
  { 
    double minValue = inputArray[0]; 
    for(int i=1;i<inputArray.length;i++)
    { 
      if(inputArray[i] < minValue)
      { 
        minValue = inputArray[i]; 
      } 
    } 
    return minValue; 
  } 
     
     // Method for getting the maximum value
  public static double getMax(double[] inputArray)
  { 
    double maxValue = inputArray[0]; 
    for(int i=1;i<inputArray.length;i++)
    { 
      if(inputArray[i] > maxValue)
      { 
        maxValue = inputArray[i]; 
      } 
    } 
    return maxValue; 
  }  
    
    
    
    
     static double [][]getServersCoordinates(String fileName) throws FileNotFoundException
    {
        Scanner s = new Scanner(new FileReader(fileName));
        String got;
        Scanner in;
        ServersCoordinatesI = new double[edges][2];
        got=s.nextLine();
        got = got.replaceAll("[^\\d.]", " ");
        String tokens[] = got.split(" ");
        List<String> tokens_up =new ArrayList<>();
        for(int i =0; i< tokens.length;i++)
        {
            if(! tokens[i].equals("") ) 
                tokens_up.add(tokens[i]);
        }
        int count = 0;
        for (int edge = 0; edge < edges; edge++) 
        {
            for(int xy=0;xy<2;xy++)
               {   
                   ServersCoordinatesI[edge][xy]= Double.valueOf(tokens_up.get(count)); 
                   count ++;
               }     
        }
        
        
        s.close();
        return ServersCoordinatesI;
    }
     
    
     static double [][][]getPlayersCoordinates(String fileName) throws FileNotFoundException
    {
        Scanner s = new Scanner(new FileReader(fileName));
        PlayersCoordinatesI = new double[sets][nbOfplayersPerSet][2];
        String got=s.nextLine();
        got = got.replaceAll("[^\\d.]", " ");
        String tokens[] = got.split(" ");
        List<String> tokens_up =new ArrayList<>();
        for(int i =0; i< tokens.length;i++)
        {
            if(! tokens[i].equals("") ) 
                tokens_up.add(tokens[i]);
        }

        int count = 0;
        for (int set = 0; set < sets; set++) 
        {
            for (int p = 0; p < nbOfplayersPerSet; p++) 
            {
               for(int xy=0;xy<2;xy++)
                    {   
                        PlayersCoordinatesI[set][p][xy]= Double.valueOf(tokens_up.get(count)); 
                        count ++;
                    }
            } 
        }
        s.close();
        return process_data(PlayersCoordinatesI);
    }
    
     
     static double[][][] process_data(double[][][]PlayersCoordinatesI)
     {
//         System.out.println("Iamiral: "+Arrays.deepToString(PlayersCoordinatesI));
         for(int s=0;s<PlayersCoordinatesI.length;s++)
         {
             double centroidX= PlayersCoordinatesI[s][0][0];
             double centroidY= PlayersCoordinatesI[s][0][1];
//             System.out.println("centroid x: "+centroidX+" y: "+centroidY+"________________________________set "+s);
             for(int p=1;p<nbOfplayersPerSet;p++)
             {
//                 if (s == 10)
//                  {
//                        System.out.println("avant: x: "+PlayersCoordinatesI[s][p][0]+" y: "+PlayersCoordinatesI[s][p][1]);
//                  }
                double p1= PlayersCoordinatesI[s][p][0] - centroidX;
                double p2= PlayersCoordinatesI[s][p][1] - centroidY;
                double distance = Math.sqrt(Math.pow(p1, 2.00)+ Math.pow(p2, 2.00));
                if(distance>radius)
                {
                    
                    double uniform1=Math.random();
                    double r = radius * Math.sqrt(uniform1);
                    double uniform2=Math.random();
                    double theta = 2 * Math.PI*uniform2;

                    double x = (r * Math.cos(theta)+centroidX) ;
                    double y = (r * Math.sin(theta)+centroidY) ;
                    
//                    if(s==10)
//                    {
//                       System.out.println("distance "+distance);
//                        System.out.println("uniform: "+uniform1+" theta: "+theta+" radius");
//                    }

                    PlayersCoordinatesI[s][p][0] = x;
                    PlayersCoordinatesI[s][p][1] = y;
                }
//                 if (s == 10)
//                  {
//                        System.out.println("x: "+PlayersCoordinatesI[s][p][0]+" y: "+PlayersCoordinatesI[s][p][1]);
//                  }
             }
     
         }
         
//          System.out.println("Iamidal: "+Arrays.deepToString(PlayersCoordinatesI));
         return PlayersCoordinatesI;
     }
      
     
    static synchronized void initializeZoning(boolean bool)
    {
        System.out.println("-----------Surface Initial: "+rangeX+"*"+rangeY+"-----------------");
       
       
        
        axesXmax = rangeX;
        axesXmin = radius;
        axesYmin = radius;
        axesYmax = rangeX;
        
        double cellSize=radius;//windowD;
        double step=radius;
        double ymin;
//        System.out.println("cellSize: "+cellSize);
                
        int box=(int)Math.ceil((double)(rangeX)/step)+1;//cellsize
//        System.out.println("box: "+box+" rangeX: "+rangeX);
        if(box==0 || box==1)
        {
            cellSize=rangeX+radius; box=1;//consider if one server is situated at the borderline
        }

        zones_all=new Zone[(box)*(box)];
        zones_mig=new Zone[(box)*(box)];
//        System.out.println("zones size: "+zones.length);
//        System.out.println("nbr of zones: "+(box)+" * "+(box));

        int co=0;
        double x = 0;
        int minx = -radius;
//        System.out.println(axesXmin+" axesYmin "+axesYmin);
        System.out.println("radius: "+radius);
        for(int by=0;by<box;by++)
        {
            ymin=-radius;//axesYmin;
            double y=(by*step)+ymin;
            for(int bx=0;bx<box;bx++)
            { 
               if(bx == 0)
                   x = (bx*step)+minx;
               else
                   x = x + cellSize;//
//                System.out.println("x: "+x);
               Zone z1=new Zone(x,y,cellSize,step);
               Zone z2=new Zone(x,y,cellSize,step);
               zones_mig[co]=z1;zones_all[co]=z2;co++;
            }
        }

//        if(bool)
//        {
//            Animation plot=new Animation(new double[edges][2],axesXmin,axesXmax,axesYmin,axesYmax,new double[sets][nbOfplayersPerSet][2],0,'n',0,0,zones_all,org);
//               plot.setBackground(Color.white);
//                  frame.setContentPane(plot);
//                  frame.setVisible(true);
//        }
    }
     
     
    static synchronized int [][] checkElegibility(int [][]Dn_eligibility,double [][]Dn_closeness,int[][] Dn_constraint, int threshold, Zone[] zones) 
    {
        Dn_eligibility = new int[Dn_constraint.length][Dn_constraint[0].length];
        int co=0;
        List<Integer>setsWithProblem = new ArrayList<>();
        for(int s=0;s<Dn_constraint.length;s++)
        {
            int count=0;
            for(int e=0;e<Dn_constraint[s].length;e++)
            {
                if((int)(Dn_constraint[s][e]*100/nbOfplayersPerSet)<=threshold)
                {
                    
                    Dn_eligibility[s][e]=1;
                    Dn_closeness[s][e]=0;
                    count++;
                }
                else
                    Dn_eligibility[s][e]=0;
            }
            if(count==0) 
            {
                co++;
                setsWithProblem.add(s);
            }
        }
        
//        System.out.println("Initial: "+Arrays.deepToString(PlayersCoordinatesI));
//        System.out.println("Final  : "+Arrays.deepToString(PlayerCoordinatesAll));
        System.out.println(" "+setsWithProblem.size());
        for(int s=0;s<setsWithProblem.size();s++)
        {
            boolean bool = true;
               for(Zone z:zones)
            {
               if (z.sets.contains(setsWithProblem.get(s)))
               {
                   System.out.println("It is located in zone x: "+z.x+" y: "+z.y);
                   System.out.println("Number of existing servers in this zone is: "+z.servers);
                   for(int e=0; e<z.servers.size();e++)
                   {
                       System.out.println("Server: "+z.servers.get(e)+" x: "+enabledServersCoordinates[z.servers.get(e)][0]+" y: "+enabledServersCoordinates[z.servers.get(e)][1]);
                       System.out.println("closness to : "+z.servers.get(e)+" is: "+Dn_closeness[setsWithProblem.get(s)][z.servers.get(e)]);
                       
                       for(int p=0;p<nbOfplayersPerSet;p++)
                       {
                           System.out.println("player in initial data: "+PlayersCoordinatesI[setsWithProblem.get(s)][p][0]+" "+PlayersCoordinatesI[setsWithProblem.get(s)][p][1]);
                            System.out.println("Set with  non eligible servers:  "+setsWithProblem.get(s)+" x: "+PlayerCoordinatesAll[setsWithProblem.get(s)][p][0]+" "+PlayerCoordinatesAll[setsWithProblem.get(s)][p][1]);
                              double p1= PlayerCoordinatesAll[setsWithProblem.get(s)][p][0] - enabledServersCoordinates[z.servers.get(e)][0];
                              double p2 = PlayerCoordinatesAll[setsWithProblem.get(s)][p][1] - enabledServersCoordinates[z.servers.get(e)][1];
                              System.out.println("p1: "+p1+" pow: "+Math.pow(p1, 2.00));
                               System.out.println("p2: "+p2+" pow: "+Math.pow(p2, 2.00));
                            double distance = Math.sqrt(Math.pow(p1, 2.00)
                            + Math.pow(p2, 2.00));
                            for (Map.Entry<Double, Double> set : alphaRange.entrySet())
                            if(distance<=set.getKey())
                            {
                                alpha=set.getValue();break;
                            }
                            System.out.println("distance: "+distance+" alpha "+alpha);
                           System.out.println("latency = "+alpha*Math.sqrt(distance));
                       }
                   }
                   bool =false;
               } 
            }
                if(bool)
               {
                    for(int p=0;p<nbOfplayersPerSet;p++)
                       {
                           System.out.println("player in initial data: "+PlayersCoordinatesI[setsWithProblem.get(s)][p][0]+" "+PlayersCoordinatesI[setsWithProblem.get(s)][p][1]);
                           System.out.println("Set with  non eligible servers:  "+setsWithProblem.get(s)+" x: "+PlayerCoordinatesAll[setsWithProblem.get(s)][p][0]+" "+PlayerCoordinatesAll[setsWithProblem.get(s)][p][1]);
                            
                       }
               }
        }
//        System.out.println("");
        System.out.println(velocity+" ---non eligible: "+co);
//        if (co != 0)
//            d
       
//        System.out.println(" "+Arrays.deepToString(Dn_eligibility));
        return Dn_eligibility;
    }
    
    static synchronized Data calculateDeadConstraintPlayers( double[][][] Dn, double [][] Dn_closeness_all, int Dn_constraint_all[][], int edges) 
    {
//        System.out.println("Dn edges: "+Dn[0][0].length+" == edges: "+edges);
        
       Dn_closeness_all=new double[Dn.length][edges];
       Dn_constraint_all=new int[Dn.length][edges];
       for(int s=0;s<Dn_constraint_all.length;s++)
       {
           double pr=opitimisticProcessingD();
           for(int e=0;e<Dn_constraint_all[s].length;e++)
           {
               double closeness=0.0;
               int count=0;
               for(int p=0;p<Dn[s].length;p++)
               {
                   if(Dn[s][p][e]+pr>Dmax)
                   {
                       count++;
                       closeness+=((Dn[s][p][e]+pr)-Dmax);
                   }
               }
               Dn_closeness_all[s][e]=closeness;
               Dn_constraint_all[s][e]=count;
           }
       }
//        System.out.println(" closeness: "+Arrays.deepToString(Dn_closeness_all));
       Data data = new Data();
       data.Dn_closeness = Dn_closeness_all;
       data.Dn_constraints = Dn_constraint_all;
       
       return data;
    }
    
    
     static synchronized double[][][] generatePlayersCoordinates(double [][]centroid,double[][][] PlayersLocation, int sets, int nbOfplayersPerSet,int rayon) {
        PlayersLocation = new double[sets][nbOfplayersPerSet][2];
         
        for (int s=0;s<sets;s++) 
        {
            double uniform1=Math.random();
            double uniform2=Math.random();
            double xCenter = ((uniform1*rangeX) + axesXmin) % axesXmax;
            double yCenter = ((uniform2*rangeY) + axesYmin) % axesYmax;
            PlayersLocation[s][0][0] = xCenter;
            PlayersLocation[s][0][1] = yCenter;
           
            centroid[s][0]=xCenter;
            centroid[s][1]=yCenter;

            for (int p=1;p < nbOfplayersPerSet;p++) 
            {
              uniform1=Math.random();
              double r = rayon * Math.sqrt(uniform1);
              uniform2=Math.random();
              double theta = 2 * Math.PI*uniform2;
              
              double x = (r * Math.cos(theta)+xCenter) ;
              double y = (r * Math.sin(theta)+yCenter) ;
            
              PlayersLocation[s][p][0] = x;
              PlayersLocation[s][p][1] = y;
            }
        }
        

        return PlayersLocation;
    }
    
    
      //**Generate service requirements(small,Medium, Large)
    static synchronized void generateServiceRequirmentSML(double W[][][], int sets, int smallS, int mediumS, int largeS, int xtraLargeS) {
        for (int set = 0; set < sets; set++) {
            int service = 0;
            for (int s = 0; s < smallS; s++) {
                int pos = 0;
                for(int r = 0; r < dim; r++)
                    W[set][service][r] = serviceGameReq[pos][r];
                service++;
            }
            for (int e = 0; e < mediumS; e++) {
                int pos = 1;
                for(int r = 0; r < dim; r++)
                    W[set][service][r] = serviceGameReq[pos][r];
                service++;
            }
            for (int e = 0; e < largeS; e++) {
                int pos = 2;
                for(int r = 0; r < dim; r++)
                    W[set][service][r] = serviceGameReq[pos][r];
                service++;
            }
            for (int e = 0; e < xtraLargeS; e++) {
                int pos = 3;
                for(int r = 0; r < dim; r++)
                    W[set][service][r] = serviceGameReq[pos][r];
                service++;
            }

        }
    }
    
    
    static int calculate_Resource_amount(int C[][], List<Integer>servers)
    {
      
        int amount = 0;
        for(int s =0; s<servers.size(); s++)
        {
//            System.out.println("length: "+C.length+" server: "+servers.get(s));
            amount  += C[servers.get(s)][0];
        }
        return amount;
    }
      static synchronized Data repair (int addedC, int C[][], double CDH[],double[][]BW, Zone [] zones, int edges, double [][]enabledServersCoordinates, int C_type[][], double CDH_type[],double[][]BW_type,double[][][]W_type,double[][][]playerCoordinates_type)
    { 
        
         int [][]C_copy = new int [edges][dim];
         double []CDH_copy = new double [edges];
         double[][] BW_copy = new double[edges][edges];
         System.arraycopy(C, 0, C_copy, 0, C.length);
         System.arraycopy(CDH, 0, CDH_copy, 0, CDH.length);
         System.arraycopy(BW, 0, BW_copy, 0, BW.length);
         Random rand = new Random();
         
       int total = 0;  
//        System.out.println("Nbr of zones: "+zones.length);
       
       for(int i=0;i<zones.length;i++)
        {
            Zone zone=zones[i];
            List<Integer>servers=zone.servers;
            List<Integer>sets=zone.sets;
            total += sets.size();
            double rq, c;
            c=calculate_Resource_amount(C, servers);
//            System.out.println("c: "+c);
            rq=serviceGameReq[addedR][0];
            int demanded=(int)((rq*nbOfplayersPerSet)/tick);
            if(demanded<1000)
                demanded=1000;
            if(demanded>1000 & demanded<2000)
                demanded=2000;
            demanded=demanded*sets.size();

//            System.out.println("sigma*c*servers.size(): "+sigma*c*servers.size());
//            System.out.println("demanded "+demanded+ " sigma "+sigma);
           
//           System.out.println("z: "+i+" nb of sets: "+zone.sets.size()+" x: "+zone.x+" y: "+zone.y);
//            System.out.println(" nb of fog nodes: "+zone.servers.size());
//            for(int s=0;s<zone.sets.size();s++)
//            System.out.print(" "+zone.sets.get(s));
//        System.out.println("");
           while((demanded)>sigma*c || !confirm_eligibility(servers, sets, W_type,playerCoordinates_type,enabledServersCoordinates))
            {
//                System.out.println("new server");
                //add new server
                double x,y;
                 if(zone.x!=0)
                    x = zone.x+Math.random()*zone.cellSize;
                    else
                    x = zone.x+Math.random()*(zone.x+zone.cellSize);
                  if(zone.y!=radius)
                    y = zone.y+Math.random()*zone.cellSize;
                    else
                    y = Math.random()*(zone.y+zone.cellSize);
                  
//                 System.out.println("------------->New server: "+x+" "+y);
//                 System.out.println(" "+zone.servers.size()+" "+c );
                 zone.servers.add(edges);
                 edges++;
//                 System.out.println("added: "+edges);
                 C_type = new int [edges][dim];
                 CDH_type = new double [edges];
                 BW_type = new double [edges][edges];

                 System.arraycopy(C_copy, 0,C_type , 0, C_copy.length);
                 System.arraycopy(CDH_copy, 0, CDH_type, 0, CDH_copy.length);

                 C_type[C_type.length-1][0] = edgeCap[addedC][0];
                 CDH_type[CDH_type.length-1]= cost[addedC];
                 for(int e=0; e< edges-1; e++)
                 {
                     for(int j=0; j< edges-1; j++)
                    {
                        BW_type[e][j]=BW_copy[e][j];
                        BW_type[j][e]=BW_copy[e][j];
                    }
                 }
                 for(int e=0; e< edges-1; e++)
                 {
                    double distance =Math.round( Math.sqrt(Math.pow(x - enabledServersCoordinates[e][0], 2.00)
                            + Math.pow(y - enabledServersCoordinates[e][1], 2.00)));
                    for (Map.Entry<Double, Double> set : alphaRange.entrySet())
                    if(distance<=set.getKey())
                     {
                        alpha=set.getValue();break;
                     }
                    double latency=alpha*Math.sqrt(distance);
                    if (latency != 0.0)
                    {
                        BW_type[edges-1][e]=(double)1/latency;
                        BW_type[e][edges-1]=(double)1/latency;
                    }
                    else
                    {
                       BW_type[edges-1][e]=0;
                       BW_type[e][edges-1]=0; 
                    }
               
                 }
                 BW_type[BW_type.length-1][BW_type.length-1]=0;
                 C_copy= new int [edges][dim];
                 CDH_copy = new double [edges];
                 BW_copy = new double [edges][edges];
                 System.arraycopy(C_type, 0, C_copy, 0, C_type.length);
                 System.arraycopy(CDH_type, 0, CDH_copy, 0, CDH_type.length);
                 System.arraycopy(BW_type, 0, BW_copy, 0, CDH_type.length);
                 c=calculate_Resource_amount(C_copy, zone.servers);
//                 System.out.println("c: "+c);
                 enabledServersCoordinates = updateCoos(x, y, enabledServersCoordinates, edges);
            }
            
        } 
        System.out.println("----TOTAL: "+total);
       Data data = new Data();
       data.C = C_copy;
       data.CDH=CDH_copy;
       data.BW=BW_copy;
       data.edges=edges;
       data.zones = zones;
       data.enabledServersCoordinates=enabledServersCoordinates;
      
       return data;
       
    }
      
      static boolean confirm_eligibility(List<Integer>servers, List<Integer>sets,double [][][]W,double[][][]playerCoordinates_type,double[][]enabledServersCoordinates)
      {
//          System.out.println("nb of servers: "+servers.size());
          
          int []checked = new int[sets.size()];
          for(int s=0; s<sets.size();s++)
              checked[s]=-1;
          for(int s=0; s<sets.size();s++)
          {
              for(int e=0; e<servers.size();e++)
              {
                  double [] latencies = calculate_latency(sets.get(s), servers.get(e), W,playerCoordinates_type,enabledServersCoordinates);
                  int co=0;
                  for(int value= 0; value<latencies.length;value++)
                  {
                      if(Dmax - latencies[value]<0)
                          co++;
                  }
                 
                  int nb = (int)(gamma* nbOfplayersPerSet);
                  if(co <= nb)
                      checked[s] = 1;
              }
          }
         for(int s=0; s<sets.size();s++)
         {
             if(checked[s]== -1)
                 return false;
         }
         return true;
      }
      
       static synchronized double[][] updateCoos(double x,double y, double [][]enabledServersCoordinates, int edges )
    {
        double edgeLocation_copy[][]=new double[enabledServersCoordinates.length][2];
        for(int e=0;e<enabledServersCoordinates.length;e++)
        {
            edgeLocation_copy[e][0]=enabledServersCoordinates[e][0];
            edgeLocation_copy[e][1]=enabledServersCoordinates[e][1];
        }
        
        enabledServersCoordinates=new double[edges][2];
        for(int e=0;e<edgeLocation_copy.length;e++)
        { 
            enabledServersCoordinates[e][0]=edgeLocation_copy[e][0];
            enabledServersCoordinates[e][1]=edgeLocation_copy[e][1];
        }
       
        enabledServersCoordinates[edgeLocation_copy.length][0]=x;
        enabledServersCoordinates[edgeLocation_copy.length][1]=y;
        
        return enabledServersCoordinates;
    }
   
    static Data calculate_cummulative_cost (double[][][]W,int [][]C_org,double[]CDH, List<Integer> sol_old, int[] candidateForMigration)
    {
        int satisfactedGroups = 0;
        int use[]=new int[C_org.length]; 
        for(int  i=0; i< candidateForMigration.length; i++)
        {
           if(candidateForMigration[i] == -1)
           {
               int server = sol_old.get(i);
               double cpu =((W[i][0][0]*nbOfplayersPerSet)/tick);
               if(cpu <1000) cpu =1000;
               if(cpu>1000 && cpu <2000) cpu =2000;
               use[server] += cpu;
               satisfactedGroups+= 1;
           }
        }
        
        double clientPaidFee = fee*  satisfactedGroups;
        double cummulative_cost = 0.0;
        int C_prev [][]= new int[use.length][dim]; 
        for(int i =0; i< use.length; i++)
        {
            cummulative_cost+=(use[i]/1000)*CDH[i];
            C_prev [i][0]= C_org[i][0] - use[i]; 
        }
        
        Data data = new Data();
        data.C_prev = C_prev;
        data.cummulative_cost = cummulative_cost;
        data.cummulative_clientPaidFee = clientPaidFee;
        
        return data;
    }

       
       //----------------------------Input Data---------------------
    static void GetData(String fileName) throws FileNotFoundException, IOException 
    {

        Scanner s = new Scanner(new FileReader(fileName));
            String got=s.next();
            Scanner in = new Scanner(got).useDelimiter("[^0-9]+");
            sets = in.nextInt();
            
            got=s.next();
            in = new Scanner(got).useDelimiter("[^0-9]+");
            edges = in.nextInt();
            
            
            got=s.next();
            in = new Scanner(got).useDelimiter("[^0-9]+");
            nbOfplayersPerSet = in.nextInt();
            
            got=s.next();
            in = new Scanner(got).useDelimiter("[^0-9]+");
            Dmax = in.nextInt();
            
            got=s.next();
            got = got.replaceAll("[^\\d.]", "");
            tick = Double.valueOf(got);

            
            System.out.println("Sets:  "+sets+" edges: "+edges+" players: "+nbOfplayersPerSet+" Dmax: "+Dmax+" tick: "+tick);
        
            DnI=new double[sets][nbOfplayersPerSet][edges];
            for(int set=0;set<sets;set++)
            {
                for(int p=0;p<nbOfplayersPerSet;p++)
                {
                    for(int e=0;e<edges;e++)
                    {
                        got=s.next();
                        got = got.replaceAll("[^\\d.]", "");
                        DnI[set][p][e]= Double.valueOf(got);
                    }
                }
            }
//            System.out.println("Dn "+Arrays.deepToString(DnI));
            
            got=s.next();

            DnI_closeness=new double[sets ][edges];
            for(int set=0;set<sets;set++)
            {
                for(int e=0;e<edges;e++)
                {
                    got=s.next();
                    got = got.replaceAll("[^\\d.]", "");
                    DnI_closeness[set][e]= Double.valueOf(got);
                    
                }   
            }
            
            
            got=s.next();

            DnI_eligibility=new int[sets ][edges];
            for(int set=0;set<sets;set++)
            {
                for(int e=0;e<edges;e++)
                {
                    got=s.next();
                    in = new Scanner(got).useDelimiter("[^0-9]+");
                    DnI_eligibility[set][e]=in.nextInt();
                    
                }   
            }
            
//            System.out.println("Dn_el");
//            System.out.println(Arrays.deepToString(DnI_eligibility));
//            System.out.println("");

            got=s.next();

            WI=new double[sets][nbOfplayersPerSet][dim];
            for(int set=0;set<sets;set++)
            {
                for(int p=0;p<nbOfplayersPerSet;p++)
                {
                    for(int e=0;e<dim;e++)
                    {
                        got=s.next();
                        got = got.replaceAll("[^\\d.]", "");
                        WI[set][p][e]= Double.valueOf(got); 
                    }
                }
            }
//            System.out.println("W "+Arrays.deepToString(W));
            got=s.next();

            Ct=new int[edges ][dim];
            for(int e=0;e<edges;e++)
            {
                for(int r=0;r<dim;r++)
                {
                    got=s.next();
                    in = new Scanner(got).useDelimiter("[^0-9]+");
                    Ct[e][r]=in.nextInt();
                }   
            }
                
//            System.out.println("C "+Arrays.deepToString(Ct));
//            System.out.println("C");
            got=s.next();
            
            CDHt=new double[edges ];
            for(int e=0;e<edges;e++)
            {
                    got=s.next();
                    got = got.replaceAll("[^\\d.]", "");
                    CDHt[e]= Double.valueOf(got); 
            }
                for(int e=0;e<CDHt.length;e++)
                    System.out.print("__ "+CDHt[e]);
            
                
        got = s.next();
        BWI = new double[edges][edges];
        for (int e = 0; e < edges; e++) 
        {
            for (int ed = 0; ed < edges; ed++) 
            {
                try
                {
                got = s.next();
                got = got.replaceAll("[^\\d.]", "");
                BWI[e][ed]= Double.valueOf(got); 
                }
                catch(Exception ex)
                {
                   BWI[e][ed]=0;  
                }
            }
        }
//        System.out.println("");
//        System.out.println("BW "+Arrays.deepToString(BWI));
       
        got = s.next();
        VMSI = new int[sets];
        for (int set = 0; set < sets; set++)
        {
            got = s.next();
            in = new Scanner(got).useDelimiter("[^0-9]+");
            VMSI[set] = in.nextInt();
        }    
       
            s.close();
            System.out.println("------------------------------------------------");
    }
       
       static void retreiveSolution(String filename) throws FileNotFoundException 
       {
            solution= new ArrayList<>();
            Scanner s = new Scanner(new FileReader(filename));

            while (s.hasNextInt()) 
                solution.add( s.nextInt());
            
            solution_identique = new ArrayList<>(solution);
       }
       
         static List<Double> retreiveServersUsage(String filename) throws FileNotFoundException {
        List<Double> serversData = new ArrayList<>();

        Scanner s = new Scanner(new FileReader(filename));
        while (s.hasNext()) {
            serversData.add(Double.valueOf(s.next()));
        }

        return serversData;
    }

    static List<Integer> retreiveActiveServers(String filename) throws FileNotFoundException {
        List<Integer> activeServers = new ArrayList<>();
        Scanner s = new Scanner(new FileReader(filename));
        while (s.hasNextInt()) {
            activeServers.add(s.nextInt());
        }

        return activeServers;
    }

    static void retainRemainedOverallCap(int CPU, int servers, List<Double> serversData, List<Integer> activeNodes) {
        C_tprev = new int[servers][1];
        for (int i = 0; i < servers; i++) {
            C_tprev[i][0] = CPU;
        }
        System.out.println("!!!! "+serversData.size()+" activeNodes: "+activeNodes.size()+" edges: "+servers);
        for (int i = 0; i < activeNodes.size(); i++) 
        {
//            System.out.println("activeNodes.get(i): "+activeNodes.get(i));
            C_tprev[activeNodes.get(i)][0] = CPU - (int) (serversData.get(i) * CPU) / 100;
        }
    }
    
    static int retainSurface(int config, String filename) throws FileNotFoundException 
    {
        List<Integer> surfaces = new ArrayList<>();
        System.out.println("File name: "+filename);
        Scanner s = new Scanner(new FileReader(filename));
        while (s.hasNextInt()) {
            surfaces.add(s.nextInt());
        }
      
        return surfaces.get(config);
    }
    
    
    
    
    //-------------------------------------------------------Save Result--------------------------
    public void save_cood(int t, double [][][]PlayersLocation, double[][]EdgesLocation, String Path, String instance, String delimiter, String scenario,int id,int threshold,float variation) throws IOException
    {
        System.out.println("Start Saving data!!");
        PrintWriter sortieP = new PrintWriter(new FileWriter(Path+delimiter+"CoordinatesVIS"+delimiter+instance+delimiter+"players_t_"+t+"_"+scenario+"_"+id+"_"+velocity_char+"_"+threshold+"_"+variation+".txt"));
        PrintWriter sortieE = new PrintWriter(new FileWriter(Path+delimiter+"CoordinatesVIS"+delimiter+instance+delimiter+"edges_t_"+t+"_"+scenario+"_"+id+"_"+velocity_char+"_"+threshold+"_"+variation+".txt"));
        
        PrintWriter sortiePP = new PrintWriter(new FileWriter(Path+delimiter+"CoordinatesSet"+delimiter+instance+delimiter+"players_t_"+t+"_"+scenario+"_"+id+"_"+velocity_char+"_"+threshold+"_"+variation+".txt"));
        PrintWriter sortieEE = new PrintWriter(new FileWriter(Path+delimiter+"CoordinatesSet"+delimiter+instance+delimiter+"edges_t_"+t+"_"+scenario+"_"+id+"_"+velocity_char+"_"+threshold+"_"+variation+".txt"));
        
        sortiePP.print("xyp=[");
        for (int s = 0; s < PlayersLocation.length; s++) 
        {
            sortiePP.print("[");
            for (int p = 0; p < nbOfplayersPerSet; p++) 
            {
               sortiePP.print("[");
               sortieP.print(PlayersLocation[s][p][0]);
               sortiePP.print(PlayersLocation[s][p][0]+" ");
               sortieP.print(',');
               sortiePP.print(',');
               sortieP.print(PlayersLocation[s][p][1]);
               sortiePP.print(PlayersLocation[s][p][1]);
               sortieP.println();
               sortiePP.print("]");
            }
            sortiePP.print("],");
        }
        sortiePP.print("];");
        sortieP.flush();
        sortieP.close();
        sortiePP.flush();
        sortiePP.close();
        
        
        sortieEE.print("xyE=[");
        for(int e=0;e<EdgesLocation.length;e++)
        {
            sortieEE.print("[");
            sortieE.print(EdgesLocation[e][0]);
            sortieEE.print(EdgesLocation[e][0]+" ");
            sortieE.print(',');
            sortieE.print(EdgesLocation[e][1]);
            sortieEE.print(EdgesLocation[e][1]);
            sortieE.println();
            sortieEE.print("],");
        }
        sortieEE.print("];");
        sortieE.flush();
        sortieE.close();
        
        sortieEE.flush();
        sortieEE.close();
    }
    
    
    static void save_t(int t,int sets, int edges, int nbOfplayersPerSet, double [][][]Dn, double[][]Dn_closeness, int[][]Dn_eligibility,double[][][]W,int[][]C,double[]CDH,int[]VMS, double[][]BW,List<Integer>solution,String dynamicPath,String delimiter,String instance, String type, String scenario,int id,double cummulative_cost,double paidFee,int threshold,float variation) throws IOException
    {
        PrintWriter sortie = new PrintWriter(new FileWriter(dynamicPath+delimiter+type+delimiter+instance+delimiter+"t_"+t+"_"+scenario+"_"+id+"_"+velocity_char+"_"+threshold+"_"+variation+".txt"));
        PrintWriter sortieOPT = new PrintWriter(new FileWriter(dynamicPath+delimiter+type+delimiter+instance+delimiter+"OPT_t_"+t+"_"+scenario+"_"+id+"_"+velocity_char+"_"+threshold+"_"+variation+".txt"));
        PrintWriter sortieSol = new PrintWriter(new FileWriter(dynamicPath+delimiter+"Solution"+delimiter+instance+delimiter+"from_"+(t-1)+"_to_"+t+"_"+scenario+"_"+id+"_"+velocity_char+"_"+threshold+"_"+variation+".txt"));
        PrintWriter sortieFEE = new PrintWriter(new FileWriter(dynamicPath+delimiter+"PaidFee"+delimiter+instance+delimiter+"Fee_t_"+t+"_"+scenario+"_"+id+"_"+velocity_char+"_"+threshold+"_"+variation+".txt"));
        PrintWriter sortieCost = new PrintWriter(new FileWriter(dynamicPath+delimiter+"CummulativeCost"+delimiter+instance+delimiter+"cost_t_"+t+"_"+scenario+"_"+id+"_"+velocity_char+"_"+threshold+"_"+variation+".txt"));
        
        
        sortie.print("sets=" + sets + ";");
        sortie.println();
        
        sortieOPT.print("sets=" + sets + ";");
        sortieOPT.println();
        //**Edges**
        sortie.print("edges=" + (edges ) + ";");//+1
        sortie.println();//+Cloud
        
        sortieOPT.print("edges=" + (edges ) + ";");//+1
        sortieOPT.println();//+Cloud
        //**Players**
        sortie.print("players=" + nbOfplayersPerSet + ";");
        sortie.println();
        
        sortieOPT.print("players=" + nbOfplayersPerSet + ";");
        sortieOPT.println();
        //**Dmax**
        sortie.print("Dmax=" + Dmax + ";");
        sortie.println();
        
        sortieOPT.print("Dmax=" + Dmax + ";");
        sortieOPT.println();
        //**Tick**
        sortie.print("tick=" + String.valueOf(tick) + ";");
        sortie.println();
        
        sortieOPT.print("tick=" + String.valueOf(tick) + ";");
        sortieOPT.println();
        
        
        sortie.print("Dn=");
        sortieOPT.print("Dn=");
        sortie.print('[');
        sortieOPT.print('[');
        for (int s = 0; s < sets; s++) 
        {
            sortie.print('[');
            sortieOPT.print('[');
            for (int p = 0; p < nbOfplayersPerSet; p++) 
            {
                sortie.print('[');
                sortieOPT.print('[');
                for (int e = 0; e < edges; e++) 
                {
                    sortie.print(Dn[s][p][e] + " ");
                    sortieOPT.print(Dn[s][p][e] + " ");
                }
                sortie.print(']');
                sortieOPT.print(']');
                if (p + 1 < nbOfplayersPerSet) {
                    sortie.print(',');
                    sortieOPT.print(',');
                }
            }
            sortie.print(']');
            sortieOPT.print(']');
            if (s + 1 < sets) 
            {
                sortie.print(',');
                sortieOPT.print(',');
            }
        }
        sortie.print(']');
        sortie.print(';');
        sortie.println();
        
        sortieOPT.print(']');
        sortieOPT.print(';');
        sortieOPT.println();
        
        sortie.print("Dn_closeness=");
        sortie.print('[');
        for (int s = 0; s < sets; s++)
        {
            sortie.print('[');
            for (int e = 0; e < edges; e++) 
            {
                sortie.print(Dn_closeness[s][e] + " ");
            }
            sortie.print(']');
            if (s + 1 < sets)
            {
                sortie.print(',');
            }
        }
        sortie.print(']');
        sortie.print(';');
        sortie.println();
        
//        System.out.println("Dn_closeness= "+Arrays.deepToString(Dn_closeness));
        
        sortie.print("Dn_eligibility=");
        sortieOPT.print("Dn_eligibility=");
        sortie.print('[');
        sortieOPT.print('[');
        for (int s = 0; s < sets; s++)
        {
            sortie.print('[');
            sortieOPT.print('[');
            for (int e = 0; e < edges; e++)
            {
                sortie.print(Dn_eligibility[s][e] + " ");
                sortieOPT.print(Dn_eligibility[s][e] + " ");
            }
            sortie.print(']');
            sortieOPT.print(']');
            if (s + 1 < sets) 
            {
                sortie.print(',');
                sortieOPT.print(',');
            }
        }
        sortie.print(']');
        sortie.print(';');
        sortie.println();
        
        sortieOPT.print(']');
        sortieOPT.print(';');
        sortieOPT.println();
        
        

        //**Requirement weight**
        sortie.print("W=");
        sortieOPT.print("W=");
        sortie.print('[');
        sortieOPT.print('[');
        for (int s = 0; s < sets; s++) 
        {
            sortie.print('[');
            sortieOPT.print('[');
            for (int p = 0; p < nbOfplayersPerSet; p++) {
                sortie.print('[');
                sortieOPT.print('[');
                for (int r = 0; r < dim; r++) 
                {
                    sortie.print(W[s][p][r] + " ");
                    sortieOPT.print(W[s][p][r] + " ");
                }
                sortie.print(']');
                sortieOPT.print(']');
                if (p + 1 < nbOfplayersPerSet) 
                {
                    sortie.print(',');
                    sortieOPT.print(',');
                }
            }
            sortie.print(']');
            sortieOPT.print(']');
            if (s + 1 < sets) 
            {
                sortie.print(',');
                sortieOPT.print(',');
            }
        }
        sortie.print(']');
        sortie.print(';');
        sortie.println();
        
        sortieOPT.print(']');
        sortieOPT.print(';');
        sortieOPT.println();

        
        sortie.print("C=");
        sortieOPT.print("C=");
        sortie.print('[');
        sortieOPT.print('[');
        for (int e = 0; e < edges; e++)
        {
            sortie.print('[');
            sortieOPT.print('[');
            for (int r = 0; r < dim; r++)
            {
                sortie.print(C[e][r] + " ");
                sortieOPT.print(C[e][r] + " ");
            }
            sortie.print(']');
            sortieOPT.print(']');
            if (e + 1 < edges) {
                sortie.print(',');
                sortieOPT.print(',');
            }
        }

        sortie.print(']');
        sortie.print(';');
        sortie.println();

        sortieOPT.print(']');
        sortieOPT.print(';');
        sortieOPT.println();

        //**Cost**
        sortie.print("CDH=");
        sortieOPT.print("CDH=");
        sortie.print('[');
        sortieOPT.print('[');
        for (int e = 0; e < edges; e++)
        {
            sortie.print(CDH[e] + " ");
            sortieOPT.print(CDH[e] + " ");
            if (e + 1 < edges)
            {
                sortie.print(',');
                sortieOPT.print(',');
            }
        }
        sortie.print(']');
        sortie.print(';');
        sortie.println();
        
        sortieOPT.print(']');
        sortieOPT.print(';');
        sortieOPT.println();
        
        
        
           //**dynamic
          //if(scenario.equals("dynamic"))
          //{
                sortie.print("BW=");
                sortie.print('[');
                sortieOPT.print("BW=");
                sortieOPT.print('[');
                for (int e = 0; e < edges; e++) 
                {
                    sortie.print('[');
                    sortieOPT.print('[');
                    for (int ed = 0; ed < edges; ed++) 
                    {
                        sortie.print(BW[e][ed] + " ");
                        sortieOPT.print(BW[e][ed] + " ");
                    }
                    sortie.print(']');
                    sortieOPT.print(']');
                    if (e + 1 < edges) 
                    {
                        sortie.print(',');
                        sortieOPT.print(',');
                    }
                }

                sortie.print(']');
                sortie.print(';');
                sortie.println();

                sortieOPT.print(']');
                sortieOPT.print(';');
                sortieOPT.println();


                sortie.print("VMS=");
                sortie.print('[');

                sortieOPT.print("VMS=");
                sortieOPT.print('[');
                for (int s = 0; s < sets; s++) 
                {
                    sortie.print(VMS[s] + " ");
                    sortieOPT.print(VMS[s] + " ");
                    if (s + 1 < sets) 
                    {
                        sortie.print(',');
                        sortieOPT.print(',');
                    }
                }

                sortie.print(']');
                sortie.print(';');
                sortie.println();   

                sortieOPT.print(']');
                sortieOPT.print(';');
                sortieOPT.println();
          //}
          
          for(int i = 0; i<solution.size(); i++)
          {
              sortieSol.print(solution.get(i));
              sortieSol.println();
          }
          
          sortie.flush();
          sortieOPT.flush();
          sortieSol.flush();
          
          sortie.close();
          sortieOPT.close();
          sortieSol.close();
          
          sortieFEE.print(paidFee);
          sortieFEE.println();
          sortieFEE.flush();
          sortieFEE.close();
          
          sortieCost.print(cummulative_cost);
          sortieCost.println();
          sortieCost.flush();
          sortieCost.close();
          
    }
    
    //---------------------------------File Management--------------------------------------
//    static void initialize(String staticPath, String dynamicPath, String type, String fileName, String delimiter)
//    {
//        String toDirect = dynamicPath+delimiter+type+delimiter+fileName;
//        File file = new File(toDirect);
//        if (! file.exists())
//        {
//            try {
//                file.mkdir();
//                File source = new File(staticPath+delimiter+fileName);
//                File dest = new File (toDirect+delimiter+"t0.txt");
////                FileUtils.copyDirectory(source, dest);
//                FileUtils.copyFile(source, dest);
//                 } 
//            catch (IOException ex) 
//            {
//                Logger.getLogger(DynamicOperations.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
//    }
    
    static void save_Input (String dynamicPath, String type, String delimiter, int instance,int id, int sets, int sets_mig, int edges, int edges_mig, int candidateToMig, int allWithoutNew,int threshold,double variation) throws IOException
    {
        PrintWriter sortieSetsStaic = new PrintWriter(new FileWriter(dynamicPath+delimiter+type+delimiter+"Sets"+delimiter+instance+delimiter+"Static_"+id+"_"+threshold+"_"+velocity_char+" "+variation+".txt",true));
        PrintWriter sortieSetsDynamic = new PrintWriter(new FileWriter(dynamicPath+delimiter+type+delimiter+"Sets"+delimiter+instance+delimiter+"Dynamic_"+id+"_"+threshold+"_"+velocity_char+" "+variation+".txt",true));
        PrintWriter sortieFogNodesStatic = new PrintWriter(new FileWriter(dynamicPath+delimiter+type+delimiter+"FogNodes"+delimiter+instance+delimiter+"Static_"+id+"_"+threshold+"_"+velocity_char+" "+variation+".txt",true));
        PrintWriter sortieFogNodesDynamic = new PrintWriter(new FileWriter(dynamicPath+delimiter+type+delimiter+"FogNodes"+delimiter+instance+delimiter+"Dynamic_"+id+"_"+threshold+"_"+velocity_char+" "+variation+".txt",true));
        PrintWriter sortieMigCanS = new PrintWriter(new FileWriter(dynamicPath+delimiter+type+delimiter+"Migration"+delimiter+instance+delimiter+"CandidatesS_"+id+"_"+threshold+"_"+velocity_char+" "+variation+".txt",true));
        PrintWriter sortieMigCanD = new PrintWriter(new FileWriter(dynamicPath+delimiter+type+delimiter+"Migration"+delimiter+instance+delimiter+"CandidatesD_"+id+"_"+threshold+"_"+velocity_char+" "+variation+".txt",true));
        
        PrintWriter sortieSetsAdded = new PrintWriter(new FileWriter(dynamicPath+delimiter+type+delimiter+"Sets"+delimiter+instance+delimiter+"Added_"+id+"_"+threshold+"_"+velocity_char+" "+variation+".txt",true));
        PrintWriter sortieSetsLeft = new PrintWriter(new FileWriter(dynamicPath+delimiter+type+delimiter+"Sets"+delimiter+instance+delimiter+"Left_"+id+"_"+threshold+"_"+velocity_char+" "+variation+".txt",true));
        PrintWriter sortieNodesLeft= new PrintWriter(new FileWriter(dynamicPath+delimiter+type+delimiter+"FogNodes"+delimiter+instance+delimiter+"Left_"+id+"_"+threshold+"_"+velocity_char+" "+variation+".txt",true));
       
        
        
        sortieSetsStaic.print(sets);
        sortieSetsStaic.println();
                
        sortieSetsDynamic.print(sets_mig);
        sortieSetsDynamic.println();
                
        sortieFogNodesStatic.print(edges);
        sortieFogNodesStatic.println();
                
        sortieFogNodesDynamic.print(edges_mig);
        sortieFogNodesDynamic.println();
        
        sortieMigCanD.print(candidateToMig);
        sortieMigCanD.println();
        
        sortieMigCanS.print(allWithoutNew);
        sortieMigCanS.println();
        
        sortieSetsAdded.print(newSets);
        sortieSetsAdded.println();
        
        sortieSetsLeft.print(LeftGroups.size());
        sortieSetsLeft.println();
        
        sortieNodesLeft.print(disabledServers.size());
        sortieNodesLeft.println();
        
        sortieSetsStaic.flush();
        sortieSetsStaic.close();
                
        sortieSetsDynamic.flush();
        sortieSetsDynamic.close();
                
        sortieFogNodesStatic.flush();
        sortieFogNodesStatic.close();
                
        sortieFogNodesDynamic.flush();
        sortieFogNodesDynamic.close();
        
        sortieMigCanS.flush();
        sortieMigCanS.close();
        
        sortieMigCanD.flush();
        sortieMigCanD.close();
        
        sortieSetsAdded.flush();
        sortieSetsAdded.close();
        
        sortieSetsLeft.flush();
        sortieSetsLeft.close();
        
        sortieNodesLeft.flush();
        sortieNodesLeft.close();
    }
}
