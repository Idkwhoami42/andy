package nl.tudelft.cse1110.andy.config;

import nl.tudelft.cse1110.andy.config.metatest.evaluators.InsertAtEvaluator;
import nl.tudelft.cse1110.andy.config.metatest.evaluators.LineReplacementEvaluator;
import nl.tudelft.cse1110.andy.config.metatest.evaluators.MetaEvaluator;
import nl.tudelft.cse1110.andy.config.metatest.evaluators.StringReplacementEvaluator;

public class MetaTest {

    private final int weight;
    private String name;
    private MetaEvaluator evaluator;

    private MetaTest(int weight, String name, MetaEvaluator evaluator) {
        this.weight = weight;
        this.name = name;
        this.evaluator = evaluator;
    }

    public String getName() {
        return this.name;
    }

    public int getWeight() {
        return weight;
    }

    public static MetaTest withStringReplacement(int weight, String name, String old, String replacement) {
        MetaEvaluator evaluator = new StringReplacementEvaluator(old, replacement);
        return new MetaTest(weight, name, evaluator);
    }

    public static MetaTest withStringReplacement(String name, String old, String replacement) {
        return withStringReplacement(1, name, old, replacement);
    }

    public static MetaTest withLineReplacement(int weight, String name, int start, int end, String replacement) {
        MetaEvaluator evaluator = new LineReplacementEvaluator(start, end, replacement);
        return new MetaTest(weight, name, evaluator);
    }

    public static MetaTest withLineReplacement(String name, int start, int end, String replacement) {
        return withLineReplacement(1, name, start, end, replacement);
    }

    public static MetaTest insertAt(int weight, String name, int lineToInsertStartingIn1, String contentToAdd) {
        MetaEvaluator evaluator = new InsertAtEvaluator(lineToInsertStartingIn1, contentToAdd);
        return new MetaTest(weight, name, evaluator);
    }

    public static MetaTest insertAt(String name, int lineToInsertStartingIn1, String contentToAdd) {
        return insertAt(1, name, lineToInsertStartingIn1, contentToAdd);
    }

    public String evaluate(String oldLibraryCode) {
        return this.evaluator.evaluate(oldLibraryCode);
    }

    public String getNameAndWeight() {
        return String.format("%s (weight: %d)", name, weight);
    }

}
