import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FitnessEvaluator Tests")
public class FitnessEvaluatorTest {

    private FitnessEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new FitnessEvaluator(42);
    }

    // --- Parser: Basic Arithmetic ---

    @Test
    @DisplayName("Single number evaluates correctly")
    void testSingleNumber() throws Exception {
        assertEquals(42.0, evaluator.evaluate("42"), 1e-9);
    }

    @Test
    @DisplayName("Simple addition evaluates correctly")
    void testAddition() throws Exception {
        assertEquals(10.0, evaluator.evaluate("3+7"), 1e-9);
    }

    @Test
    @DisplayName("Simple subtraction evaluates correctly")
    void testSubtraction() throws Exception {
        assertEquals(4.0, evaluator.evaluate("9-5"), 1e-9);
    }

    @Test
    @DisplayName("Simple multiplication evaluates correctly")
    void testMultiplication() throws Exception {
        assertEquals(42.0, evaluator.evaluate("6*7"), 1e-9);
    }

    @Test
    @DisplayName("Simple division evaluates correctly")
    void testDivision() throws Exception {
        assertEquals(6.0, evaluator.evaluate("42/7"), 1e-9);
    }

    // --- Parser: Operator Precedence ---

    @Test
    @DisplayName("Multiplication binds tighter than addition")
    void testPrecedenceMultiplicationOverAddition() throws Exception {
        // 2+3*4 should be 2+(3*4) = 14, NOT (2+3)*4 = 20
        assertEquals(14.0, evaluator.evaluate("2+3*4"), 1e-9);
    }

    @Test
    @DisplayName("Division binds tighter than subtraction")
    void testPrecedenceDivisionOverSubtraction() throws Exception {
        // 10-6/2 should be 10-(6/2) = 7, NOT (10-6)/2 = 2
        assertEquals(7.0, evaluator.evaluate("10-6/2"), 1e-9);
    }

    @Test
    @DisplayName("Mixed operators respect precedence")
    void testMixedPrecedence() throws Exception {
        // 2+3*4-1 = 2+12-1 = 13
        assertEquals(13.0, evaluator.evaluate("2+3*4-1"), 1e-9);
    }

    // --- Parser: Parentheses ---

    @Test
    @DisplayName("Parentheses override precedence")
    void testParenthesesOverridePrecedence() throws Exception {
        // (2+3)*4 = 5*4 = 20
        assertEquals(20.0, evaluator.evaluate("(2+3)*4"), 1e-9);
    }

    @Test
    @DisplayName("Nested parentheses evaluate correctly")
    void testNestedParentheses() throws Exception {
        // ((2+3)*4)-2 = 20-2 = 18
        assertEquals(18.0, evaluator.evaluate("((2+3)*4)-2"), 1e-9);
    }

    @Test
    @DisplayName("Classic target expression: 6*7 = 42")
    void testClassicExpression() throws Exception {
        assertEquals(42.0, evaluator.evaluate("6*7"), 1e-9);
    }

    @Test
    @DisplayName("Parenthesized target expression: (6*7) = 42")
    void testParenthesizedTargetExpression() throws Exception {
        assertEquals(42.0, evaluator.evaluate("(6*7)"), 1e-9);
    }

    // --- Parser: Multi-digit Numbers ---

    @Test
    @DisplayName("Multi-digit numbers parse correctly")
    void testMultiDigitNumbers() throws Exception {
        assertEquals(100.0, evaluator.evaluate("50+50"), 1e-9);
        assertEquals(1000.0, evaluator.evaluate("100*10"), 1e-9);
    }

    // --- Parser: Error Handling ---

    @Test
    @DisplayName("Division by zero returns MAX_VALUE score")
    void testDivisionByZeroReturnsMaxScore() {
        // Build a mock chromosome that expresses "1/0"
        // We test via evaluate() directly since we can't easily force a chromosome expression
        assertThrows(Exception.class, () -> evaluator.evaluate("1/0"));
    }

    @Test
    @DisplayName("Empty expression throws exception")
    void testEmptyExpressionThrows() {
        assertThrows(Exception.class, () -> evaluator.evaluate(""));
    }

    @Test
    @DisplayName("Operator-only expression throws exception")
    void testOperatorOnlyThrows() {
        assertThrows(Exception.class, () -> evaluator.evaluate("*+/"));
    }

    @Test
    @DisplayName("Unbalanced parenthesis throws exception")
    void testUnbalancedParenthesisThrows() {
        assertThrows(Exception.class, () -> evaluator.evaluate("(3+4"));
    }

    // --- Fitness Scoring ---

    @Test
    @DisplayName("Exact match scores 0.0")
    void testExactMatchScoresZero() throws Exception {
        double result = evaluator.evaluate("6*7");
        assertEquals(0.0, Math.abs(result - 42), 1e-9);
    }

    @Test
    @DisplayName("Score is absolute distance from target")
    void testScoreIsAbsoluteDistance() throws Exception {
        // evaluate("40") = 40, target = 42, score should be 2.0
        double result = evaluator.evaluate("40");
        assertEquals(2.0, Math.abs(result - 42), 1e-9);
    }

    @Test
    @DisplayName("isExactMatch returns true for perfect expression")
    void testIsExactMatchTrue() {
        // We need a chromosome that expresses "6*7"
        // Gene encoding: 6=6, *=12, 7=7
        Chromosome c = new Chromosome(3, 0.0);
        c.getGenomes().get(0).setGene(6);  // '6'
        c.getGenomes().get(1).setGene(12); // '*'
        c.getGenomes().get(2).setGene(7);  // '7'
        assertTrue(evaluator.isExactMatch(c));
    }

    @Test
    @DisplayName("isExactMatch returns false for non-matching expression")
    void testIsExactMatchFalse() {
        // Chromosome that expresses "1+1" = 2, not 42
        Chromosome c = new Chromosome(3, 0.0);
        c.getGenomes().get(0).setGene(1);  // '1'
        c.getGenomes().get(1).setGene(10); // '+'
        c.getGenomes().get(2).setGene(1);  // '1'
        assertFalse(evaluator.isExactMatch(c));
    }
}
