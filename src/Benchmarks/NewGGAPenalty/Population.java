/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Benchmarks.NewGGAPenalty;

/**
 *
 * @author ASUS
 */
public class Population 
{
    private Chromosome population[];
    private double populationFitness = -1;
    public int C[][];
    public double W[][][];
    
    public Population(int populationSize, int binsLength,int itemsLength,int C[][],double [][][]W) 
    {
        this.population = new Chromosome[populationSize];
        for (int individualCount = 0; individualCount <populationSize; individualCount++) 
        {
            Chromosome individual = new Chromosome(binsLength,itemsLength,C);
            this.population[individualCount] = individual;
        }
        this.C=C;
        this.W=W;
    }
    
    public Chromosome[] getIndividuals() 
    {
        return this.population;
    }
    
    public void setPopulationFitness(double fitness) 
    {
        this.populationFitness = fitness;
    }
    public double getPopulationFitness() 
    {
        return this.populationFitness;
    }
    public int size() 
    {
        return this.population.length;
    }
    public Chromosome setIndividual(int offset, Chromosome individual) 
    {
        return population[offset] = individual;
    }
    public Chromosome getIndividual(int offset) 
    {
        return population[offset];
    }
    
    public void display(String path)
    {
        for(int i=0;i<population.length;i++)
        {
            population[i].display(0,"",0,0,path);
            System.out.println("");
        }

            
    }
}
