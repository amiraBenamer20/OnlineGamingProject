/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package onlinegamingproject;
import java.io.File;

/**
 *
 * @author BENAMER
 */

public class CreateFilesStructure
{

   public static void main(String[] args) 
   {
       String path = "Data\\";
       String folders[] = {"CoordinatesSet","CoordinatesVIS","DataSet","Exec","NumberOfUS","Analytics","Solution","CDF","BestIter","ActiveServers",
       "Obj","Output","Percent","Profiling","Quality","ServerData","Synthese","ViolatedCap","ViolatedDead","Input","Input_Output","PaidFee","AllocationCost","CummulativeCost"
       ,"ViolatedMig"};
      int configs[] =  {20,40,60,80,100,250,500,750,1000,1250,1500,1750,2000,3000 };//20,40,60,80,100,250,500,750,1000,1250,1500,1750,2000,3000,
          for(int f= 0; f<folders.length;f++)
          {
              String dir = path+folders[f];
              File directory = new File(dir);
              System.out.println(directory.mkdir());
              for(int i= 0; i<configs.length;i++)
              {
                dir = path+folders[f]+"\\"+configs[i];
                directory = new File(dir);
                System.out.println(directory.mkdir());
                for(int t= 0; t<144;t++)
                {
                    String sub_dir = path+folders[f]+"\\"+configs[i]+"\\"+t;
                    directory = new File(sub_dir);
                    System.out.println(directory.mkdir());
                }
              }
          }
          
          for(int f= 0; f<folders.length;f++)
          {
              if(folders[f].equals("Input_Output"))
              {
                for (int in=0; in < 3; in++)
                {
                 String dir = path+folders_sub[in];
                 File directory = new File(dir);
                 System.out.println(directory.mkdir());
                 for(int i= 0; i<configs.length;i++)
                 {
                   dir = path+folders[f]+"\\"+configs[i];
                  directory = new File(dir);
                  System.out.println(directory.mkdir());
                  for(int t= 0; t<144;t++)
                  {
                      String sub_dir = path+folders[f]+"\\"+configs[i]+"\\"+t;
                       directory = new File(sub_dir);
                      System.out.println(directory.mkdir());
                  }
                 } 
                }
              }
          }
       
   }

}
