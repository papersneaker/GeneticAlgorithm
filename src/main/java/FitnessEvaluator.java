/**
 * FitnessEvaluator - safely evaluates a Chromosome's expressed math string
 * and scores it against a target value.
 *
 * Two responsibilities:
 *   1. PARSE  → evaluate a raw expression string like "6*7" or "(3+4)*6"
 *              using a hand-written recursive descent parser (pure Java, no libs)
 *   2. SCORE  → return a fitness score: how close the result is to the target
 *              (lower score = fitter; 0.0 = exact match = done!)
 *
 * Recursive Descent Parser — how it works:
 *   Mathematical precedence is encoded in the grammar rules:
 *
 *     expression  →  term  ( ('+' | '-')  term  )*
 *     term        →  power ( ('*' | '/' | '%')  power )*
 *     power       →  factor ( '^' power )*          (right-associative)
 *     factor      →  number  |  '(' expression ')'
 *
 *   Higher in the call stack = lower precedence.
 *   Lower in the call stack = higher precedence (evaluated first).
 *   Parentheses force a recursive re-entry at the top level.
 *
 * Invalid expressions (e.g. "*+/(", "6//7", unbalanced parens) return
 * a fitness score of Double.MAX_VALUE — maximally unfit, never selected.
 */
public class FitnessEvaluator {

    private final double target;

    /**
     * @param target  the number we are trying to evolve an expression for
     */
    public FitnessEvaluator(double target) {
        this.target = target;
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Score a Chromosome against the target. Lower is better; 0.0 is a perfect match.
     *
     * @param chromosome  the chromosome to evaluate
     * @return            absolute difference from target, or Double.MAX_VALUE if invalid
     */
    public double score(Chromosome chromosome) {
        String expression = chromosome.express();
        try {
            double result = evaluate(expression);
            if (Double.isNaN(result) || Double.isInfinite(result)) {
                return Double.MAX_VALUE; // e.g. division by zero
            }
            return Math.abs(result - target);
        } catch (Exception e) {
            return Double.MAX_VALUE; // invalid expression
        }
    }

    /**
     * Check if a Chromosome is an exact match for the target.
     * Uses a small epsilon for floating-point safety.
     */
    public boolean isExactMatch(Chromosome chromosome) {
        return score(chromosome) < 1e-9;
    }

    /**
     * Evaluate a raw math expression string and return its numeric result.
     * Exposed as package-level for testing purposes.
     *
     * @param expression  e.g. "6*7", "(3+4)*6", "42"
     * @return            the computed double result
     * @throws Exception  if the expression is malformed
     */
    public double evaluate(String expression) throws Exception {
        // Strip all whitespace for clean parsing
        String cleaned = expression.replaceAll("\\s+", "");
        if (cleaned.isEmpty()) {
            throw new Exception("Empty expression.");
        }
        Parser parser = new Parser(cleaned);
        double result = parser.parseExpression();
        if (parser.hasMore()) {
            // Leftover characters means the expression was malformed
            throw new Exception("Unexpected characters at position " + parser.getPosition());
        }
        return result;
    }

    // -------------------------------------------------------------------------
    // Recursive Descent Parser (inner class)
    // -------------------------------------------------------------------------

    /**
     * Parser - walks through the expression string character by character,
     * building up a numeric result using recursive grammar rules.
     *
     * Grammar:
     *   expression  →  term  ( ('+' | '-')  term  )*
     *   term        →  power ( ('*' | '/')  power )*
     *   power       →  factor ( '^'  power )*          (right-associative)
     *   factor      →  number  |  '(' expression ')'
     */
    private static class Parser {

        private final String input;
        private int position;

        Parser(String input) {
            this.input = input;
            this.position = 0;
        }

        boolean hasMore() {
            return position < input.length();
        }

        int getPosition() {
            return position;
        }

        char peek() {
            return input.charAt(position);
        }

        char consume() {
            return input.charAt(position++);
        }

        // --- Grammar Rule: expression → term ( ('+' | '-') term )* ---

        double parseExpression() throws Exception {
            double result = parseTerm();

            while (hasMore() && (peek() == '+' || peek() == '-')) {
                char op = consume();
                double right = parseTerm();
                if (op == '+') result += right;
                else           result -= right;
            }

            return result;
        }

        // --- Grammar Rule: term → power ( ('*' | '/' | '%') power )* ---

        double parseTerm() throws Exception {
            double result = parsePower();

            while (hasMore() && (peek() == '*' || peek() == '/' || peek() == '%')) {
                char op = consume();
                double right = parsePower();
                if (op == '*') {
                    result *= right;
                } else if (op == '/') {
                    if (right == 0) throw new Exception("Division by zero.");
                    result /= right;
                } else {
                    if (right == 0) throw new Exception("Modulo by zero.");
                    result %= right;
                }
            }

            return result;
        }

        // --- Grammar Rule: power → factor ( '^' power )* ---  (right-associative)

        double parsePower() throws Exception {
            double base = parseFactor();
            if (hasMore() && peek() == '^') {
                consume(); // eat '^'
                double exponent = parsePower(); // right-recursive for right-associativity
                return Math.pow(base, exponent);
            }
            return base;
        }

        // --- Grammar Rule: factor → number | '(' expression ')' ---

        double parseFactor() throws Exception {
            if (!hasMore()) {
                throw new Exception("Unexpected end of expression at position " + position);
            }

            // Parenthesized sub-expression — recurse back to the top
            if (peek() == '(') {
                consume(); // eat '('
                double result = parseExpression();
                if (!hasMore() || peek() != ')') {
                    throw new Exception("Expected ')' at position " + position);
                }
                consume(); // eat ')'
                return result;
            }

            // Number — consume all consecutive digit characters
            if (Character.isDigit(peek())) {
                return parseNumber();
            }

            throw new Exception("Unexpected character '" + peek() + "' at position " + position);
        }

        // --- Number parser — handles multi-digit numbers like "42" or "100" ---

        double parseNumber() throws Exception {
            int start = position;
            while (hasMore() && Character.isDigit(peek())) {
                consume();
            }
            if (position == start) {
                throw new Exception("Expected number at position " + position);
            }
            return Double.parseDouble(input.substring(start, position));
        }
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public double getTarget() {
        return target;
    }
}
