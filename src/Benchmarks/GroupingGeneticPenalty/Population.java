/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Benchmarks.GroupingGeneticPenalty;

import java.util.Random;
/**
 *
 * @author ASUS
 */
public class Population {
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

     public void sortPop(Population pop)
    {
        boolean toSort=true;
        while(toSort)
        {
            toSort=false;
            for(int i=0;i<pop.size()-1;i++)
            {
                if((pop.getIndividual(i).fitness>pop.getIndividual(i+1).fitness )
//                        && pop.getIndividual(i+1).checkCapacity())
//                        ||(pop.getIndividual(i).fitness<pop.getIndividual(i+1).fitness 
//                        && pop.getIndividual(i+1).checkCapacity() &&!pop.getIndividual(i).checkCapacity() )
                    && pop.getIndividual(i).migrationConstraint()
                        )   
                {
                    Chromosome temp=pop.getIndividual(i);
                    pop.setIndividual(i, pop.getIndividual(i+1));
                    pop.setIndividual(i+1, temp);
                    toSort=true;
                }
            }
        }
    }
     
  
     
     
     
    public void shuffle() 
    {
        Random rnd = new Random();
        for (int i = population.length - 1; i > 0; i--) 
        {
            int index = rnd.nextInt(i + 1);
            Chromosome a = population[index];
            population[index] = population[i];
            population[i] = a;
        }
    }
    
      int partition(Population pop,int begin,int end) 
    {
    int pivot = end;

 
    int counter = begin;
    for (int i = begin; i < end; i++) {
        if (pop.getIndividual(i).fitness < pop.getIndividual(pivot).fitness ) {
            Chromosome temp = pop.getIndividual(counter);
            pop.setIndividual(counter, pop.getIndividual(i));
            pop.setIndividual(i, temp);
            counter++;
        }
    }
    Chromosome temp = pop.getIndividual(pivot);
    pop.setIndividual(pivot, pop.getIndividual(counter));
    pop.setIndividual(counter, temp);
  
    return counter;
    }

    public  void quickSort(Population pop,int begin,int end) 
    {
        if (end <= begin) return;
        int pivot = partition(pop, begin, end);
        quickSort(pop, begin, pivot-1);
        quickSort(pop, pivot+1, end);
    }
    
    public void bubbleSort(Population pop)
    {
        boolean bool=true;
        while(bool)
        {
            bool=false;
            for(int i=0;i<pop.size()-1;i++)
            {
                System.out.println("i "+pop.getIndividual(i).fitness);
                System.out.println("i+1 "+pop.getIndividual(i+1).fitness);
                if(pop.getIndividual(i).fitness>pop.getIndividual(i+1).fitness)
                {
                    Chromosome temp = pop.getIndividual(i);
                    pop.setIndividual(i, pop.getIndividual(i+1));
                    pop.setIndividual(i+1, temp);
                    bool=true;
                }
            }
        }
    }
    
    public void display()
    {
        for(int i=0;i<population.length;i++)
        {
            Chromosome crh=population[i];
            crh.display();
        }
    }
    


}
