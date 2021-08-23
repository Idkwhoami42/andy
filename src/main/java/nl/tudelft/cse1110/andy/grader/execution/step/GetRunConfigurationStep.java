package nl.tudelft.cse1110.andy.grader.execution.step;

import nl.tudelft.cse1110.andy.grader.config.DefaultRunConfiguration;
import nl.tudelft.cse1110.andy.grader.config.RunConfiguration;
import nl.tudelft.cse1110.andy.grader.execution.Context;
import nl.tudelft.cse1110.andy.grader.execution.ExecutionFlow;
import nl.tudelft.cse1110.andy.grader.execution.ExecutionStep;
import nl.tudelft.cse1110.andy.grader.execution.step.helper.Action;
import nl.tudelft.cse1110.andy.grader.execution.step.helper.Mode;
import nl.tudelft.cse1110.andy.grader.execution.step.helper.ModeSelector;
import nl.tudelft.cse1110.andy.grader.grade.GradeWeight;
import nl.tudelft.cse1110.andy.grader.result.ResultBuilder;

import java.util.List;
import java.util.NoSuchElementException;

import static nl.tudelft.cse1110.andy.grader.util.ClassUtils.allClassesButTestingAndConfigOnes;
import static nl.tudelft.cse1110.andy.grader.util.ClassUtils.getConfigurationClass;
import static nl.tudelft.cse1110.andy.grader.config.RunConfiguration.*;

public class GetRunConfigurationStep implements ExecutionStep {

    @Override
    public void execute(Context ctx, ResultBuilder result) {
        try {
            ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();

            Class<?> runConfigurationClass = Class.forName(getConfigurationClass(ctx.getNewClassNames()), false, currentClassLoader);
            RunConfiguration runConfiguration = (RunConfiguration) runConfigurationClass.getDeclaredConstructor().newInstance();

            ctx.setRunConfiguration(runConfiguration);

            this.buildGradeValues(runConfiguration, result);
            this.setTotalNumberOfMutations(runConfiguration, result);
            this.addExecutionSteps(ctx);
        } catch (NoSuchElementException ex) {
            // There's no configuration set. We put a default one!
            RunConfiguration runConfiguration = new DefaultRunConfiguration(allClassesButTestingAndConfigOnes(ctx.getNewClassNames()));
            ctx.setRunConfiguration(runConfiguration);

            this.buildGradeValues(runConfiguration, result);
            this.setTotalNumberOfMutations(runConfiguration, result);
            this.addExecutionSteps(ctx);
        } catch (Exception ex) {
            result.genericFailure(this, ex);
        }
    }

    private void buildGradeValues(RunConfiguration runCfg, ResultBuilder result) {
        boolean failureGivesZero = runCfg.failureGivesZero();
        float coverage = runCfg.weights().get("coverage");
        float mutation = runCfg.weights().get("mutation");
        float meta = runCfg.weights().get("meta");
        float codechecks = runCfg.weights().get("codechecks");

        result.setGradeWeights(new GradeWeight(failureGivesZero, coverage, mutation, meta, codechecks));
    }

    /* Set the total number of mutations which will replace the total number generated by Pitest
     * If it's -1, we use the number generated by Pitest (the check happens in ResultBuilder)
     */
    private void setTotalNumberOfMutations(RunConfiguration runCfg, ResultBuilder result) {
        int numberOfMutations = runCfg.numberOfMutationsToConsider();
        result.setNumberOfMutationsToConsider(numberOfMutations);
    }

    private void addExecutionSteps(Context ctx) {
        ExecutionFlow flow = ctx.getFlow();

        ModeSelector modeSelector = createModeSelector(ctx);

        // In case flow is null, it means flow already contains other steps than the basics and hence we return early.
        // This happens for example in tests or when running the meta tests.
        if (flow == null) {
            return;
        }

        flow.addSteps(modeSelector.getCorrectSteps());
    }

    private ModeSelector createModeSelector(Context ctx) {
        RunConfiguration runConfiguration = ctx.getRunConfiguration();
        Mode mode = runConfiguration.mode();
        Action action = ctx.getAction();

        ModeSelector modeSelector = new ModeSelector(mode, action);
        ctx.setModeSelector(modeSelector);

        return modeSelector;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof GetRunConfigurationStep;
    }
}
