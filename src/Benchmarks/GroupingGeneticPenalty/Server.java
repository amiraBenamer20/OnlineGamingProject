/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Benchmarks.GroupingGeneticPenalty;


import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ASUS
 */
public class Server implements Cloneable
{
    public int capacity;
    public int filled;
    public List<Set>sets;
    public int serverID=-1;
    
    public double delV;
    public double delC;
    public int vCPU;

    public Server(int serverID,int capacity)
    {
        sets=new ArrayList<>();
        this.serverID=serverID;
        this.capacity=capacity;
    }
    
      @Override
    public Object clone() throws CloneNotSupportedException
    {
        return (Server)super.clone();
    }
    
//    public boolean addElement(Set elem) {
//		if (filled + elem.getSizeItem() <= capacity) {
//			filled += elem.getSizeItem();
//			sets.add(elem);
//			return true;
//		}
//
//		return false;
//	}
    
     public void addElement(Set elem) 
    {

        filled += elem.getSizeItem();
	sets.add(elem);
        vCPU+=Math.round((double)elem.getSizeItem()/1000);
    }
    
    public void removeElement(Set elem) 
    {
        filled -= elem.getSizeItem();
	sets.remove(elem);
        vCPU-=Math.round((double)elem.getSizeItem()/1000);
    }
    
    public int getCapacity() {
        return capacity;
    }

    public int getFilled() {
        return filled;
    }

    public List<Set> getSets() {
        return sets;
    }

    public int getServerID() {
        return serverID;
    }

   

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public void setFilled(int filled) {
        this.filled = filled;
    }

    public void setSets(List<Set> sets) {
        this.sets = sets;
    }

    public void setServerID(int serverID) {
        this.serverID = serverID;
    }

    
    
    
}
