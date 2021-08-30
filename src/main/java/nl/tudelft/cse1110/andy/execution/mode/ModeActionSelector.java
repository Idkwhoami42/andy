package nl.tudelft.cse1110.andy.execution.mode;

import nl.tudelft.cse1110.andy.execution.ExecutionStep;
import nl.tudelft.cse1110.andy.execution.step.*;

import java.util.Collections;
import java.util.List;

import static nl.tudelft.cse1110.andy.execution.mode.Action.*;
import static nl.tudelft.cse1110.andy.execution.mode.Mode.*;

public class ModeActionSelector {

    private Mode mode;
    private Action action;

    public ModeActionSelector(Mode mode, Action action) {
        this.mode = mode;
        this.action = action;
    }

    public Mode getMode() {
        return mode;
    }

    public Action getAction() {
        return action;
    }

    public List<ExecutionStep> getSteps() {
        if(action==META_TEST)
            return getMetaTestMode();

        switch (mode) {
            case PRACTICE -> {
                return getPracticeMode();
            }
            case EXAM -> {
                return getExamMode();
            }
            case GRADING -> {
                return getGradingMode();
            }
        }

        return Collections.emptyList();
    }

    public boolean shouldShowFullHints() {
        return (mode == GRADING) || (mode == PRACTICE && action == FULL_WITH_HINTS);
    }

    public boolean shouldShowPartialHints() {
        return mode == PRACTICE && action == FULL_WITHOUT_HINTS;
    }

    public boolean shouldGenerateAnalytics() {
        return !(mode == EXAM || action == META_TEST || mode == GRADING);
    }

    public boolean shouldCalculateAndShowGrades() {
        boolean notExam = !mode.equals(Mode.EXAM);
        boolean fullRun = action != COVERAGE && action != TESTS;

        return notExam && fullRun;
    }

    private List<ExecutionStep> getPracticeMode() {
        if (action == FULL_WITH_HINTS || action == FULL_WITHOUT_HINTS) {
            return fullMode();
        } else if (action == COVERAGE) {
            return withCoverage();
        } else {
            return justTests();
        }
    }

    private List<ExecutionStep> getExamMode() {
        if (action == FULL_WITH_HINTS || action == FULL_WITHOUT_HINTS || action == COVERAGE) {
            return withCoverage();
        } else {
            return justTests();
        }
    }

    private List<ExecutionStep> getGradingMode() {
        return fullMode();
    }

    private List<ExecutionStep> getMetaTestMode() {
        return justTests();
    }

    static List<ExecutionStep> justTests() {
        return List.of(new RunJUnitTestsStep());
    }

    static List<ExecutionStep> withCoverage() {
        return List.of(
                new RunJUnitTestsStep(),
                new RunJacocoCoverageStep(),
                new RunPitestStep()
        );
    }

    static List<ExecutionStep> fullMode() {
        return List.of(
                new RunJUnitTestsStep(),
                new RunJacocoCoverageStep(),
                new RunPitestStep(),
                new RunCodeChecksStep(),
                new RunMetaTestsStep()
        );
    }


}
