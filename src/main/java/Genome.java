import java.util.Random;

/**
 * Genome - the atomic unit of the genetic algorithm.
 *
 * Each Genome encodes a single character in a mathematical expression:
 *   - A digit (0-9)
 *   - An operator (+, -, *, /)
 *   - A parenthesis ( or )
 *
 * Internally, a Genome stores an integer "gene" value that maps to one of
 * these characters via a fixed encoding table. This mirrors how real DNA
 * encodes information as a sequence of base values rather than the final
 * expressed trait directly.
 */
public class Genome {

    // --- Encoding Table ---
    // 0-9   → digits '0' through '9'
    // 10    → '+'
    // 11    → '-'
    // 12    → '*'
    // 13    → '/'
    // 14    → '('
    // 15    → ')'
    public static final int GENE_MIN = 0;
    public static final int GENE_MAX = 15; // inclusive

    private int gene; // raw gene value
    private static final Random random = new Random();

    // --- Constructors ---

    /**
     * Create a Genome with a specific gene value.
     * Useful for crossover and controlled construction.
     */
    public Genome(int gene) {
        if (gene < GENE_MIN || gene > GENE_MAX) {
            throw new IllegalArgumentException(
                "Gene value must be between " + GENE_MIN + " and " + GENE_MAX + ", got: " + gene
            );
        }
        this.gene = gene;
    }

    /**
     * Create a Genome with a random gene value.
     * This is how the initial population is seeded.
     */
    public Genome() {
        this.gene = random.nextInt(GENE_MAX - GENE_MIN + 1) + GENE_MIN;
    }

    // --- Core Methods ---

    /**
     * Express this genome as its character representation.
     * This is what gets assembled into a full math expression by a Chromosome.
     */
    public char express() {
        if (gene <= 9) {
            return (char) ('0' + gene); // digit
        }
        switch (gene) {
            case 10: return '+';
            case 11: return '-';
            case 12: return '*';
            case 13: return '/';
            case 14: return '(';
            case 15: return ')';
            default: throw new IllegalStateException("Unknown gene value: " + gene);
        }
    }

    /**
     * Mutate this genome by randomizing its gene value.
     * Called by Chromosome during the mutation phase.
     */
    public void mutate() {
        this.gene = random.nextInt(GENE_MAX - GENE_MIN + 1) + GENE_MIN;
    }

    /**
     * Returns a copy of this Genome (used during crossover).
     */
    public Genome copy() {
        return new Genome(this.gene);
    }

    // --- Getters / Setters ---

    public int getGene() {
        return gene;
    }

    public void setGene(int gene) {
        if (gene < GENE_MIN || gene > GENE_MAX) {
            throw new IllegalArgumentException("Gene value out of range: " + gene);
        }
        this.gene = gene;
    }

    // --- Debug ---

    @Override
    public String toString() {
        return "Genome[gene=" + gene + ", char='" + express() + "']";
    }
}
