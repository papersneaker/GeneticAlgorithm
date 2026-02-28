import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Genome Tests")
public class GenomeTest {

    // --- Construction ---

    @Test
    @DisplayName("Valid gene values construct without exception")
    void testValidConstruction() {
        assertDoesNotThrow(() -> new Genome(0));
        assertDoesNotThrow(() -> new Genome(15));
        assertDoesNotThrow(() -> new Genome(7));
    }

    @Test
    @DisplayName("Gene value below minimum throws exception")
    void testGeneBelowMinThrows() {
        assertThrows(IllegalArgumentException.class, () -> new Genome(-1));
    }

    @Test
    @DisplayName("Gene value above maximum throws exception")
    void testGeneAboveMaxThrows() {
        assertThrows(IllegalArgumentException.class, () -> new Genome(16));
    }

    @Test
    @DisplayName("Random constructor stays within valid range")
    @RepeatedTest(50)
    void testRandomConstructorInRange() {
        Genome g = new Genome();
        assertTrue(g.getGene() >= Genome.GENE_MIN);
        assertTrue(g.getGene() <= Genome.GENE_MAX);
    }

    // --- Expression ---

    @Test
    @DisplayName("Digits 0-9 express correctly")
    void testDigitExpression() {
        for (int i = 0; i <= 9; i++) {
            Genome g = new Genome(i);
            assertEquals((char)('0' + i), g.express(),
                "Gene " + i + " should express as digit '" + (char)('0' + i) + "'");
        }
    }

    @Test
    @DisplayName("Operators express correctly")
    void testOperatorExpression() {
        assertEquals('+', new Genome(10).express());
        assertEquals('-', new Genome(11).express());
        assertEquals('*', new Genome(12).express());
        assertEquals('/', new Genome(13).express());
    }

    @Test
    @DisplayName("Parentheses express correctly")
    void testParenthesisExpression() {
        assertEquals('(', new Genome(14).express());
        assertEquals(')', new Genome(15).express());
    }

    // --- Mutation ---

    @Test
    @DisplayName("Mutated gene stays within valid range")
    @RepeatedTest(50)
    void testMutateStaysInRange() {
        Genome g = new Genome(0);
        g.mutate();
        assertTrue(g.getGene() >= Genome.GENE_MIN);
        assertTrue(g.getGene() <= Genome.GENE_MAX);
    }

    // --- Copy ---

    @Test
    @DisplayName("Copy produces equal gene value")
    void testCopyEqualValue() {
        Genome original = new Genome(7);
        Genome copy = original.copy();
        assertEquals(original.getGene(), copy.getGene());
    }

    @Test
    @DisplayName("Copy is independent - mutating copy does not affect original")
    void testCopyIsIndependent() {
        Genome original = new Genome(0); // gene = 0, expresses '0'
        Genome copy = original.copy();
        copy.setGene(15);               // change copy to ')'
        assertEquals(0, original.getGene(), "Original should be unchanged after mutating copy");
    }
}
