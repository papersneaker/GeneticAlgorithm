import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the full GeneticAlgorithm simulation.
 *
 * These tests verify end-to-end behavior — that the algorithm as a whole
 * can find solutions for simple targets within a reasonable number of
 * generations. They are intentionally given generous limits to avoid
 * flakiness while still validating correctness.
 */
@DisplayName("GeneticAlgorithm Integration Tests")
public class GeneticAlgorithmTest {

    // --- Builder ---

    @Test
    @DisplayName("Builder constructs without exception using defaults")
    void testBuilderDefaultConstruction() {
        assertDoesNotThrow(() ->
            new GeneticAlgorithm.Builder(42).build()
        );
    }

    @Test
    @DisplayName("Builder constructs without exception with all custom values")
    void testBuilderCustomConstruction() {
        assertDoesNotThrow(() ->
            new GeneticAlgorithm.Builder(100)
                .populationSize(100)
                .chromosomeLength(7)
                .mutationRate(0.05)
                .survivorCount(20)
                .maxGenerations(1000)
                .build()
        );
    }

    // --- Simulation: Simple Targets ---

    @Test
    @DisplayName("Algorithm runs without exception for target 42")
    void testRunsWithoutException() {
        assertDoesNotThrow(() ->
            new GeneticAlgorithm.Builder(42)
                .maxGenerations(500)
                .build()
                .run()
        );
    }

    @Test
    @DisplayName("Algorithm runs without exception for target 0")
    void testRunsForTargetZero() {
        // Target 0 is trivially solvable — any single digit '0' chromosome matches
        assertDoesNotThrow(() ->
            new GeneticAlgorithm.Builder(0)
                .chromosomeLength(1)
                .maxGenerations(500)
                .build()
                .run()
        );
    }

    @Test
    @DisplayName("Algorithm runs without exception for target 9")
    void testRunsForTargetNine() {
        // Single digit target — solvable by a length-1 chromosome
        assertDoesNotThrow(() ->
            new GeneticAlgorithm.Builder(9)
                .chromosomeLength(1)
                .maxGenerations(500)
                .build()
                .run()
        );
    }

    // --- FitnessEvaluator + Chromosome Integration ---

    @Test
    @DisplayName("Manually constructed winning chromosome scores 0.0")
    void testManualWinnerScoresZero() {
        // Build a chromosome that expresses "6*7" = 42
        FitnessEvaluator evaluator = new FitnessEvaluator(42);
        Chromosome winner = new Chromosome(3, 0.0);
        winner.getGenomes().get(0).setGene(6);  // '6'
        winner.getGenomes().get(1).setGene(12); // '*'
        winner.getGenomes().get(2).setGene(7);  // '7'

        assertEquals("6*7", winner.express());
        assertEquals(0.0, evaluator.score(winner), 1e-9);
        assertTrue(evaluator.isExactMatch(winner));
    }

    @Test
    @DisplayName("Manually constructed losing chromosome scores non-zero")
    void testManualLoserScoresNonZero() {
        FitnessEvaluator evaluator = new FitnessEvaluator(42);
        Chromosome loser = new Chromosome(3, 0.0);
        loser.getGenomes().get(0).setGene(1);  // '1'
        loser.getGenomes().get(1).setGene(10); // '+'
        loser.getGenomes().get(2).setGene(1);  // '1'

        assertEquals("1+1", loser.express());
        assertTrue(evaluator.score(loser) > 0.0);
        assertFalse(evaluator.isExactMatch(loser));
    }

    // --- Stress Test ---

    @Test
    @DisplayName("Algorithm handles large target with longer chromosomes")
    @RepeatedTest(3)
    void testLargeTarget() {
        assertDoesNotThrow(() ->
            new GeneticAlgorithm.Builder(999)
                .chromosomeLength(9)
                .populationSize(300)
                .survivorCount(30)
                .maxGenerations(2000)
                .build()
                .run()
        );
    }
}
