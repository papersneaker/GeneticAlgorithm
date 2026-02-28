/**
 * GeneticAlgorithm - the simulation engine and entry point for the project.
 *
 * Ties together all components:
 *   Genome → Chromosome → Population → FitnessEvaluator
 *
 * Given a target number, evolves a population of random mathematical
 * expressions across generations until one evaluates exactly to the target,
 * or a maximum generation limit is reached.
 *
 * Usage:
 *   GeneticAlgorithm ga = new GeneticAlgorithm.Builder(42).build();
 *   ga.run();
 *
 * Or with custom configuration:
 *   GeneticAlgorithm ga = new GeneticAlgorithm.Builder(42)
 *       .populationSize(200)
 *       .chromosomeLength(7)
 *       .mutationRate(0.05)
 *       .survivorCount(30)
 *       .maxGenerations(10000)
 *       .build();
 *   ga.run();
 */
public class GeneticAlgorithm {

    // --- Configuration ---
    private final double target;
    private final int    populationSize;
    private final int    chromosomeLength;
    private final double mutationRate;
    private final int    survivorCount;
    private final int    maxGenerations;

    // --- Internals ---
    private Population       population;
    private FitnessEvaluator evaluator;

    // --- Constructor (private — use Builder) ---

    private GeneticAlgorithm(Builder builder) {
        this.target           = builder.target;
        this.populationSize   = builder.populationSize;
        this.chromosomeLength = builder.chromosomeLength;
        this.mutationRate     = builder.mutationRate;
        this.survivorCount    = builder.survivorCount;
        this.maxGenerations   = builder.maxGenerations;
    }

    // -------------------------------------------------------------------------
    // Run
    // -------------------------------------------------------------------------

    /**
     * Run the genetic algorithm simulation.
     *
     * Each generation:
     *   1. Check for an exact match in the current population
     *   2. If found — report and stop
     *   3. If not   — evolve the population and report progress
     *   4. If max generations hit — report the best found and stop
     */
    public void run() {
        evaluator  = new FitnessEvaluator(target);
        population = new Population(
            populationSize,
            chromosomeLength,
            mutationRate,
            survivorCount,
            evaluator
        );

        printHeader();

        int generation = 0;

        while (generation < maxGenerations) {
            generation++;

            // Check for exact match BEFORE evolving
            Chromosome exactMatch = population.findExactMatch();
            if (exactMatch != null) {
                printSuccess(exactMatch, generation);
                return;
            }

            // Evolve and get the fittest of this generation
            Chromosome fittest = population.evolve();

            // Report progress every 100 generations
            if (generation % 100 == 0) {
                printProgress(fittest, generation);
            }
        }

        // Max generations reached — report best found
        printFailure(population.getFittest(), maxGenerations);
    }

    // -------------------------------------------------------------------------
    // Output / Reporting
    // -------------------------------------------------------------------------

    private void printHeader() {
        System.out.println("+----------------------------------------------+");
        System.out.println("|         Genetic Algorithm  -  Java           |");
        System.out.println("+----------------------------------------------+");
        System.out.println();
        System.out.println("  Target         : " + (int) target);
        System.out.println("  Population     : " + populationSize);
        System.out.println("  Chromosome Len : " + chromosomeLength);
        System.out.println("  Mutation Rate  : " + (mutationRate * 100) + "%");
        System.out.println("  Survivors/Gen  : " + survivorCount);
        System.out.println("  Max Generations: " + maxGenerations);
        System.out.println();
        System.out.println("  Evolving...");
        System.out.println("  ---------------------------------------------");
    }

    private void printProgress(Chromosome fittest, int generation) {
        double score = evaluator.score(fittest);
        System.out.printf("  Gen %6d  |  Best: %-20s |  Score: %.4f%n",
            generation, fittest.express(), score);
    }

    private void printSuccess(Chromosome winner, int generation) {
        System.out.println();
        System.out.println("  ---------------------------------------------");
        System.out.println("  [SUCCESS] SOLUTION FOUND!");
        System.out.println();
        System.out.printf ("  Expression  :  %s%n", winner.express());
        System.out.printf ("  Evaluates to:  %.0f%n", target);
        System.out.printf ("  Generations :  %d%n", generation);
        System.out.println("  ---------------------------------------------");
    }

    private void printFailure(Chromosome best, int maxGenerations) {
        double score = evaluator.score(best);
        System.out.println();
        System.out.println("  ---------------------------------------------");
        System.out.println("  [STOPPED] Max generations reached. No exact match found.");
        System.out.println();
        System.out.printf ("  Best expression : %s%n", best.express());
        System.out.printf ("  Score (delta)   : %.4f%n", score);
        System.out.printf ("  Generations run : %d%n", maxGenerations);
        System.out.println();
        System.out.println("  Tip: Try increasing chromosomeLength, populationSize,");
        System.out.println("       or maxGenerations via the Builder.");
        System.out.println("  ---------------------------------------------");
    }

    // -------------------------------------------------------------------------
    // Builder — clean, readable configuration
    // -------------------------------------------------------------------------

    /**
     * Builder pattern for configuring and constructing a GeneticAlgorithm.
     *
     * Sensible defaults are provided so the simplest usage is just:
     *   new GeneticAlgorithm.Builder(42).build().run();
     */
    public static class Builder {

        // Required
        private final double target;

        // Optional — with sensible defaults
        private int    populationSize   = 200;
        private int    chromosomeLength = 5;
        private double mutationRate     = 0.05;
        private int    survivorCount    = 20;
        private int    maxGenerations   = 50000;

        public Builder(double target) {
            this.target = target;
        }

        public Builder populationSize(int val)   { this.populationSize   = val; return this; }
        public Builder chromosomeLength(int val) { this.chromosomeLength = val; return this; }
        public Builder mutationRate(double val)  { this.mutationRate     = val; return this; }
        public Builder survivorCount(int val)    { this.survivorCount    = val; return this; }
        public Builder maxGenerations(int val)   { this.maxGenerations   = val; return this; }

        public GeneticAlgorithm build() {
            return new GeneticAlgorithm(this);
        }
    }

    // -------------------------------------------------------------------------
    // Main — run it!
    // -------------------------------------------------------------------------

    public static void main(String[] args) {
        // Simple run — target 42, all defaults
        new GeneticAlgorithm.Builder(42)
            .build()
            .run();

        System.out.println();

        // Custom run — bigger target, longer chromosomes
        new GeneticAlgorithm.Builder(100)
            .chromosomeLength(7)
            .populationSize(300)
            .mutationRate(0.04)
            .survivorCount(30)
            .build()
            .run();
    }
}