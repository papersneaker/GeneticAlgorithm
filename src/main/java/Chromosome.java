import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Chromosome - an ordered sequence of Genomes representing a candidate
 * mathematical expression (e.g. "6*7" or "(3+4)*6").
 *
 * Responsibilities:
 *   - express()   → render the genome sequence as a String expression
 *   - mutate()    → randomly flip genes based on mutation rate
 *   - crossover() → splice two chromosomes to produce two offspring
 *   - copy()      → deep copy (used during reproduction)
 *
 * All chromosomes in a Population share the same length — they are the
 * same "species". Length is set at construction and owned by the Population.
 */
public class Chromosome {

    private List<Genome> genomes;
    private double mutationRate; // probability (0.0 - 1.0) each gene mutates
    private static final Random random = new Random();

    // --- Constructors ---

    /**
     * Create a Chromosome of a given length with random Genomes.
     * This is how the initial population is seeded.
     *
     * @param length       number of genes (shared across the population)
     * @param mutationRate probability that any individual gene mutates (0.0 - 1.0)
     */
    public Chromosome(int length, double mutationRate) {
        if (length < 1) {
            throw new IllegalArgumentException("Chromosome length must be at least 1.");
        }
        if (mutationRate < 0.0 || mutationRate > 1.0) {
            throw new IllegalArgumentException("Mutation rate must be between 0.0 and 1.0.");
        }
        this.mutationRate = mutationRate;
        this.genomes = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            genomes.add(new Genome()); // randomly initialized
        }
    }

    /**
     * Private constructor for building a Chromosome from an existing genome list.
     * Used internally by crossover() and copy().
     */
    private Chromosome(List<Genome> genomes, double mutationRate) {
        this.genomes = genomes;
        this.mutationRate = mutationRate;
    }

    // --- Core Methods ---

    /**
     * Express this chromosome as a raw String by concatenating each Genome's
     * expressed character. Example result: "6*7" or "(3+4)*6"
     *
     * Note: the raw expression may not be valid math — that's the FitnessEvaluator's
     * job to handle gracefully.
     */
    public String express() {
        StringBuilder sb = new StringBuilder();
        for (Genome g : genomes) {
            sb.append(g.express());
        }
        return sb.toString();
    }

    /**
     * Mutate this chromosome in place. Each gene independently has a
     * mutationRate chance of being randomized.
     *
     * Called on offspring after crossover during each new generation.
     */
    public void mutate() {
        for (Genome g : genomes) {
            if (random.nextDouble() < mutationRate) {
                g.mutate();
            }
        }
    }

    /**
     * Perform single-point crossover with another Chromosome, producing
     * two offspring. A random crossover point is chosen; genes before the
     * point come from one parent, genes after from the other.
     *
     * Both parents must be the same length (enforced by Population).
     *
     * Example (length=6, crossoverPoint=3):
     *   Parent A: [1, 2, 3, | 4, 5, 6]
     *   Parent B: [7, 8, 9, | 0, 1, 2]
     *   Child  1: [1, 2, 3,   0, 1, 2]
     *   Child  2: [7, 8, 9,   4, 5, 6]
     *
     * @param other   the second parent
     * @return        array of two offspring Chromosomes
     */
    public Chromosome[] crossover(Chromosome other) {
        int length = this.genomes.size();
        if (other.genomes.size() != length) {
            throw new IllegalArgumentException("Cannot crossover chromosomes of different lengths.");
        }

        // Pick a crossover point (at least 1 gene from each parent)
        int crossoverPoint = random.nextInt(length - 1) + 1;

        List<Genome> childGenomes1 = new ArrayList<>(length);
        List<Genome> childGenomes2 = new ArrayList<>(length);

        for (int i = 0; i < length; i++) {
            if (i < crossoverPoint) {
                childGenomes1.add(this.genomes.get(i).copy());
                childGenomes2.add(other.genomes.get(i).copy());
            } else {
                childGenomes1.add(other.genomes.get(i).copy());
                childGenomes2.add(this.genomes.get(i).copy());
            }
        }

        return new Chromosome[]{
            new Chromosome(childGenomes1, this.mutationRate),
            new Chromosome(childGenomes2, this.mutationRate)
        };
    }

    /**
     * Deep copy this Chromosome. Used when carrying survivors into the
     * next generation without modifying the originals.
     */
    public Chromosome copy() {
        List<Genome> copiedGenomes = new ArrayList<>(genomes.size());
        for (Genome g : genomes) {
            copiedGenomes.add(g.copy());
        }
        return new Chromosome(copiedGenomes, this.mutationRate);
    }

    // --- Getters ---

    public int getLength() {
        return genomes.size();
    }

    public double getMutationRate() {
        return mutationRate;
    }

    public List<Genome> getGenomes() {
        return genomes;
    }

    // --- Debug ---

    @Override
    public String toString() {
        return "Chromosome[length=" + genomes.size()
             + ", expression=\"" + express() + "\""
             + ", mutationRate=" + mutationRate + "]";
    }
}
