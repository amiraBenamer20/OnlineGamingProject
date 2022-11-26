/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Benchmarks.GroupingGeneticPenalty;




/**
 *
 * @author Amira BENAMER
 */
public class Player {
   int cpu;
   //int ram;
   double violation;
   double latency;
    public Player(int cpu)//, int ram) //memory
    {
        this.cpu = cpu;
        //this.ram = ram;
    }

    public double getLatency() {
        return latency;
    }

    public int getCpu() {
        return cpu;
    }

    public double getViolation() {
        return violation;
    }

    public void setCpu(int cpu) {
        this.cpu = cpu;
    }

    public void setLatency(double latency) {
        this.latency = latency;
    }

    public void setViolation(double violation) {
        this.violation = violation;
    }
    
    
   
    
}
