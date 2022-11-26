/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GeneticAlgorithmPenalty;

/**
 *
 * @author Amira BENAMER
 */
public class Population
{
    private Chromosome population[];
    private double populationFitness = -1;
    public int C[][];
    public double W[][][];
    
    public Population(int populationSize, int itemSetLength,int C[][],double [][][]W) 
    {
        this.population = new Chromosome[populationSize];
        for (int individualCount = 0; individualCount <populationSize; individualCount++) 
        {
            Chromosome individual = new Chromosome(itemSetLength);
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
    
    public void display(int length)
    {
        for(int i=0;i<length;i++)
        {
            System.out.println("i: "+i);
            population[i].display();
        }

            
    }
}
