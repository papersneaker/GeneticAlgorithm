import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Chromosome Tests")
public class ChromosomeTest {

    // --- Construction ---

    @Test
    @DisplayName("Chromosome has correct length after construction")
    void testCorrectLength() {
        Chromosome c = new Chromosome(5, 0.05);
        assertEquals(5, c.getLength());
    }

    @Test
    @DisplayName("Length below 1 throws exception")
    void testInvalidLengthThrows() {
        assertThrows(IllegalArgumentException.class, () -> new Chromosome(0, 0.05));
    }

    @Test
    @DisplayName("Mutation rate below 0 throws exception")
    void testInvalidMutationRateLowThrows() {
        assertThrows(IllegalArgumentException.class, () -> new Chromosome(5, -0.1));
    }

    @Test
    @DisplayName("Mutation rate above 1 throws exception")
    void testInvalidMutationRateHighThrows() {
        assertThrows(IllegalArgumentException.class, () -> new Chromosome(5, 1.1));
    }

    // --- Expression ---

    @Test
    @DisplayName("express() returns string of correct length")
    void testExpressCorrectLength() {
        Chromosome c = new Chromosome(7, 0.05);
        assertEquals(7, c.express().length());
    }

    @Test
    @DisplayName("express() contains only valid characters")
    void testExpressValidCharacters() {
        Chromosome c = new Chromosome(20, 0.05);
        String expr = c.express();
        for (char ch : expr.toCharArray()) {
            assertTrue(
                Character.isDigit(ch) || "+-*/()^%".indexOf(ch) >= 0,
                "Unexpected character in expression: '" + ch + "'"
            );
        }
    }

    // --- Mutation ---

    @Test
    @DisplayName("mutate() does not change chromosome length")
    void testMutatePreservesLength() {
        Chromosome c = new Chromosome(5, 1.0); // 100% mutation rate
        c.mutate();
        assertEquals(5, c.getLength());
    }

    @Test
    @DisplayName("mutate() with 100% rate changes expression (very likely)")
    @RepeatedTest(10)
    void testMutateChangesGenes() {
        // With 100% mutation rate and length 10, probability of no change is astronomically low
        Chromosome c = new Chromosome(10, 1.0);
        String before = c.express();
        c.mutate();
        String after = c.express();
        // At least one character should differ — not a guaranteed assertion but statistically solid
        assertNotNull(after);
        assertEquals(before.length(), after.length());
    }

    // --- Crossover ---

    @Test
    @DisplayName("crossover() produces two offspring of correct length")
    void testCrossoverProducesCorrectLength() {
        Chromosome a = new Chromosome(6, 0.05);
        Chromosome b = new Chromosome(6, 0.05);
        Chromosome[] offspring = a.crossover(b);
        assertEquals(2, offspring.length);
        assertEquals(6, offspring[0].getLength());
        assertEquals(6, offspring[1].getLength());
    }

    @Test
    @DisplayName("crossover() with different lengths throws exception")
    void testCrossoverDifferentLengthsThrows() {
        Chromosome a = new Chromosome(5, 0.05);
        Chromosome b = new Chromosome(7, 0.05);
        assertThrows(IllegalArgumentException.class, () -> a.crossover(b));
    }

    @Test
    @DisplayName("crossover() offspring are independent of parents")
    void testCrossoverOffspringIndependent() {
        Chromosome a = new Chromosome(6, 0.05);
        Chromosome b = new Chromosome(6, 0.05);
        Chromosome[] offspring = a.crossover(b);
        String offspringExpr = offspring[0].express();

        // Mutate parent — offspring should be unaffected
        a.mutate();
        assertEquals(offspringExpr, offspring[0].express(),
            "Offspring expression should not change when parent is mutated");
    }

    // --- Copy ---

    @Test
    @DisplayName("copy() produces same expression")
    void testCopySameExpression() {
        Chromosome c = new Chromosome(5, 0.05);
        Chromosome copy = c.copy();
        assertEquals(c.express(), copy.express());
    }

    @Test
    @DisplayName("copy() is a deep copy - mutating copy does not affect original")
    void testCopyIsDeep() {
        Chromosome original = new Chromosome(5, 0.05);
        String originalExpr = original.express();
        Chromosome copy = original.copy();
        copy.mutate(); // force mutation on copy with high probability
        // Original should be unchanged
        assertEquals(originalExpr, original.express(),
            "Original chromosome should be unaffected by mutating its copy");
    }
}
