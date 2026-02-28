import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Population Tests")
public class PopulationTest {

    private FitnessEvaluator evaluator;
    private Population population;

    @BeforeEach
    void setUp() {
        evaluator  = new FitnessEvaluator(42);
        population = new Population(50, 5, 0.05, 10, evaluator);
    }

    // --- Construction ---

    @Test
    @DisplayName("Population has correct size after seeding")
    void testCorrectPopulationSize() {
        assertEquals(50, population.getChromosomes().size());
    }

    @Test
    @DisplayName("All chromosomes share the same length")
    void testAllChromosomesSameLength() {
        for (Chromosome c : population.getChromosomes()) {
            assertEquals(5, c.getLength(),
                "All chromosomes should have length 5");
        }
    }

    @Test
    @DisplayName("survivorCount less than 2 throws exception")
    void testSurvivorCountTooLowThrows() {
        assertThrows(IllegalArgumentException.class,
            () -> new Population(50, 5, 0.05, 1, evaluator));
    }

    @Test
    @DisplayName("survivorCount greater than populationSize throws exception")
    void testSurvivorCountExceedsPopulationThrows() {
        assertThrows(IllegalArgumentException.class,
            () -> new Population(10, 5, 0.05, 11, evaluator));
    }

    // --- Evolution ---

    @Test
    @DisplayName("evolve() returns a non-null fittest chromosome")
    void testEvolveReturnsChromosome() {
        Chromosome fittest = population.evolve();
        assertNotNull(fittest);
    }

    @Test
    @DisplayName("Population size stays constant across generations")
    void testPopulationSizeStableAfterEvolve() {
        population.evolve();
        assertEquals(50, population.getChromosomes().size());
    }

    @Test
    @DisplayName("All chromosomes still same length after evolution")
    void testChromosomeLengthStableAfterEvolve() {
        population.evolve();
        for (Chromosome c : population.getChromosomes()) {
            assertEquals(5, c.getLength());
        }
    }

    @Test
    @DisplayName("Fitness improves or stays same over multiple generations")
    @RepeatedTest(5)
    void testFitnessImprovesOverGenerations() {
        double initialScore = evaluator.score(population.getFittest());
        for (int i = 0; i < 100; i++) {
            population.evolve();
        }
        double finalScore = evaluator.score(population.getFittest());
        // Score should generally improve (get closer to 0) — elitism guarantees non-regression
        assertTrue(finalScore <= initialScore,
            "Fitness should not regress over 100 generations due to elitism. " +
            "Initial: " + initialScore + ", Final: " + finalScore);
    }

    // --- Exact Match Detection ---

    @Test
    @DisplayName("findExactMatch returns null when no match exists in fresh population")
    void testFindExactMatchNullOnFreshPopulation() {
        // Force a population with chromosomes that can't match (length 1, only operators)
        // We test the null case by checking a fresh small population
        // (statistically very unlikely to accidentally contain exact match)
        Population smallPop = new Population(5, 1, 0.0, 2, evaluator);
        // A single-gene chromosome can only be a digit 0-9, none of which equal 42
        // So findExactMatch should return null
        assertNull(smallPop.findExactMatch());
    }

    @Test
    @DisplayName("getFittest returns chromosome with lowest score")
    void testGetFittestLowestScore() {
        Chromosome fittest = population.getFittest();
        double fittestScore = evaluator.score(fittest);
        for (Chromosome c : population.getChromosomes()) {
            assertTrue(evaluator.score(c) >= fittestScore,
                "getFittest() should return the chromosome with the minimum score");
        }
    }

    // --- Seed ---

    @Test
    @DisplayName("seed() resets the population to correct size")
    void testSeedResetsPopulation() {
        population.evolve();
        population.seed();
        assertEquals(50, population.getChromosomes().size());
    }
}
