import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * Population - a generation of same-length Chromosomes representing
 * candidate solutions to the target expression problem.
 *
 * Manages the full lifecycle of a generation:
 *   1. Seed    → create an initial population of random Chromosomes
 *   2. Evaluate → score each Chromosome against the target via FitnessEvaluator
 *   3. Select  → identify the fittest survivors (Top N by score)
 *   4. Reproduce → crossover survivors to fill the next generation
 *   5. Mutate  → apply random mutations to offspring
 *
 * All Chromosomes in a Population share the same length — they are the
 * same "species". This keeps crossover clean and meaningful.
 */
public class Population {

    private List<Chromosome> chromosomes;
    private final int populationSize;   // total number of chromosomes per generation
    private final int chromosomeLength; // shared length across all chromosomes
    private final double mutationRate;  // passed down to each Chromosome
    private final int survivorCount;    // how many top survivors breed each generation
    private final FitnessEvaluator evaluator;
    private static final Random random = new Random();

    // --- Constructor ---

    /**
     * Create a new Population and seed it with random Chromosomes.
     *
     * @param populationSize    total chromosomes per generation (e.g. 100)
     * @param chromosomeLength  number of genes per chromosome (e.g. 5 for "6*7+2")
     * @param mutationRate      probability any gene mutates (e.g. 0.05 = 5%)
     * @param survivorCount     how many fittest chromosomes breed (e.g. top 20)
     * @param evaluator         the FitnessEvaluator tied to our target number
     */
    public Population(int populationSize,
                      int chromosomeLength,
                      double mutationRate,
                      int survivorCount,
                      FitnessEvaluator evaluator) {

        if (survivorCount < 2) {
            throw new IllegalArgumentException("survivorCount must be at least 2 to allow crossover.");
        }
        if (survivorCount > populationSize) {
            throw new IllegalArgumentException("survivorCount cannot exceed populationSize.");
        }

        this.populationSize    = populationSize;
        this.chromosomeLength  = chromosomeLength;
        this.mutationRate      = mutationRate;
        this.survivorCount     = survivorCount;
        this.evaluator         = evaluator;
        this.chromosomes       = new ArrayList<>(populationSize);

        seed();
    }

    // --- Lifecycle Methods ---

    /**
     * Seed the population with fresh random Chromosomes.
     * Called once at construction. Could also be called to restart evolution.
     */
    public void seed() {
        chromosomes.clear();
        for (int i = 0; i < populationSize; i++) {
            chromosomes.add(new Chromosome(chromosomeLength, mutationRate));
        }
    }

    /**
     * Evolve the population by one generation:
     *   1. Sort chromosomes by fitness (ascending — lower score = fitter)
     *   2. Select top N survivors
     *   3. Crossover survivors to produce offspring
     *   4. Mutate offspring
     *   5. Replace old population with survivors + offspring
     *
     * @return the fittest Chromosome this generation (lowest score)
     */
    public Chromosome evolve() {
        // Step 1: Sort by fitness score (ascending — 0.0 is perfect)
        chromosomes.sort(Comparator.comparingDouble(evaluator::score));

        // Step 2: Select top N survivors
        List<Chromosome> survivors = new ArrayList<>(survivorCount);
        for (int i = 0; i < survivorCount; i++) {
            survivors.add(chromosomes.get(i).copy()); // deep copy — protect originals
        }

        // Step 3 & 4: Build next generation via crossover + mutation
        List<Chromosome> nextGeneration = new ArrayList<>(populationSize);

        // Carry survivors forward untouched (elitism — best always survive)
        for (Chromosome survivor : survivors) {
            nextGeneration.add(survivor);
        }

        // Fill the rest of the population with offspring
        while (nextGeneration.size() < populationSize) {
            // Pick two distinct parents randomly from survivors
            Chromosome parentA = survivors.get(random.nextInt(survivorCount));
            Chromosome parentB = survivors.get(random.nextInt(survivorCount));

            // Avoid self-crossover if possible
            int attempts = 0;
            while (parentA == parentB && survivorCount > 1 && attempts < 10) {
                parentB = survivors.get(random.nextInt(survivorCount));
                attempts++;
            }

            // Crossover produces two children
            Chromosome[] offspring = parentA.crossover(parentB);

            // Mutate each child
            offspring[0].mutate();
            offspring[1].mutate();

            // Add children (don't overfill)
            nextGeneration.add(offspring[0]);
            if (nextGeneration.size() < populationSize) {
                nextGeneration.add(offspring[1]);
            }
        }

        // Step 5: Replace old generation
        this.chromosomes = nextGeneration;

        // Return the fittest of this generation (already sorted — it's index 0 of survivors)
        return survivors.get(0);
    }

    /**
     * Scan the current population for an exact match.
     * Called each generation before evolve() to check if we're done.
     *
     * @return the matching Chromosome, or null if none found
     */
    public Chromosome findExactMatch() {
        for (Chromosome c : chromosomes) {
            if (evaluator.isExactMatch(c)) {
                return c;
            }
        }
        return null;
    }

    /**
     * Return the current fittest Chromosome without evolving.
     * Useful for reporting progress.
     */
    public Chromosome getFittest() {
        return chromosomes.stream()
            .min(Comparator.comparingDouble(evaluator::score))
            .orElseThrow(() -> new IllegalStateException("Population is empty."));
    }

    // --- Getters ---

    public int getPopulationSize()   { return populationSize; }
    public int getChromosomeLength() { return chromosomeLength; }
    public double getMutationRate()  { return mutationRate; }
    public int getSurvivorCount()    { return survivorCount; }
    public List<Chromosome> getChromosomes() { return chromosomes; }

    // --- Debug ---

    @Override
    public String toString() {
        Chromosome fittest = getFittest();
        return "Population[size=" + populationSize
             + ", length=" + chromosomeLength
             + ", mutationRate=" + mutationRate
             + ", fittest=\"" + fittest.express()
             + "\" (score=" + evaluator.score(fittest) + ")]";
    }
}
