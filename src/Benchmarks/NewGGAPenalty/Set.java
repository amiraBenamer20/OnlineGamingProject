/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Benchmarks.NewGGAPenalty;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ASUS
 */
public class Set implements Serializable{
    public int hostBin=-1;//new Server(-1, 0);//group
    public int setId;
    List<Player>players;
    public int nbPlayers;
    public double migrationTime;
    public int sizeItem;

    public Set() {
    }

    public Set(int setId) {
        this.setId = setId;
    }
    
    
    
    public void assignPlayer(int [][][]W)
    {
        players=new ArrayList<>();
        for(int p=0;p<W[setId].length;p++)
        {
            Player pl=new Player(W[setId][p][0]);//, W[setId][p][1]); memory
            players.add(pl);
        }
    }
    public void setSizeItem(int [][][]W)
    {
        int cpu=0;
        for(int p=0;p<W[setId].length;p++)
        {
            cpu+=W[setId][p][0];
        }
        this.sizeItem=cpu;
    }

    public void setSizeItem(int cpu)
    {
        this.sizeItem=cpu;
    }
    public int getSizeItem() {
        return sizeItem;
    }
    
}
