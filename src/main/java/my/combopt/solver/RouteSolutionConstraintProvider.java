package my.combopt.solver;

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.score.stream.Joiners;
import my.combopt.domain.Edge;
import my.combopt.domain.RouteStep;

import java.util.Objects;

public class RouteSolutionConstraintProvider implements ConstraintProvider {
    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
            minimizeActiveStepCount(constraintFactory),
            visitedAllEdges(constraintFactory),
            stepsAreChainedV2(constraintFactory),
            minimizeTotalWeight(constraintFactory),
//            invalidEdgePenalty(constraintFactory),
            routeIsACycle(constraintFactory)
        };
    }

    public Constraint minimizeActiveStepCount(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEach(RouteStep.class)
                .filter(RouteStep::getIsActive)
                .penalize(HardSoftScore.ONE_SOFT, (routeStep) -> 1000)
                .asConstraint("minimizeStepsCount");
    }

    public Constraint visitedAllEdges(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEach(Edge.class)
                .ifNotExists(RouteStep.class,
                        Joiners.equal(Edge::getStart, RouteStep::getStartVertex),
                        Joiners.equal(Edge::getEnd, RouteStep::getEndVertex),
                        Joiners.filtering((edge, routeStep) -> routeStep.getIsActive()))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("visitedAllEdges");
    }

    public Constraint stepsAreChainedV2(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEach(RouteStep.class)
                .filter(RouteStep::getIsActive)  // Only consider active steps
                .join(RouteStep.class,
                        Joiners.filtering((step1, step2) -> step2.getIsActive()),  // Join with next active step
                        Joiners.filtering((step1, step2) -> {
                            RouteStep nextActiveStep = step1.getNextStep();
                            while (!nextActiveStep.getIsActive()) {
                                nextActiveStep = nextActiveStep.getNextStep();
                            }
                            return Objects.equals(nextActiveStep,step2);  // Find the next active step in the chain
                        }))
                .penalize(HardSoftScore.ONE_HARD, (step1, step2) ->
                        !Objects.equals(step1.getEndVertex(), step2.getStartVertex()) ? 1 : 0)
                .asConstraint("stepsAreChained");
    }

    public Constraint stepsAreChainedV1(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEach(RouteStep.class)
                .filter(RouteStep::getIsActive)  // Only consider active steps
                .join(RouteStep.class,
                        Joiners.filtering((step1, step2) -> step2.getIsActive()),  // Join with next active step
                        Joiners.filtering((step1, step2) -> {
                            RouteStep nextActiveStep = step1.getNextStep();
                            while (nextActiveStep != null && !nextActiveStep.getIsActive()) {
                                nextActiveStep = nextActiveStep.getNextStep();
                            }
                            return nextActiveStep == step2;  // Find the next active step in the chain
                        }))
                .penalize(HardSoftScore.ONE_HARD, (step1, step2) ->
                        !Objects.equals(step1.getEndVertex(), step2.getStartVertex()) ? 1 : 0)
                .asConstraint("stepsAreChained");
    }

    public Constraint minimizeTotalWeight(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEach(RouteStep.class)
                .filter(RouteStep::getIsActive)
                .join(Edge.class,
                        Joiners.equal(RouteStep::getStartVertex, Edge::getStart),
                        Joiners.equal(RouteStep::getEndVertex, Edge::getEnd))
                .penalize(HardSoftScore.ONE_SOFT, (routeStep, edge) ->
                        (int)edge.getWeight())
                .asConstraint("minimizeTotalWeight");
    }
    public Constraint invalidEdgePenalty(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEach(RouteStep.class)
                .filter(RouteStep::getIsActive)
                .filter(routeStep -> !routeStep.getStartVertex().getNeighbours().contains(routeStep.getEndVertex().getId()))
                .filter(routeStep -> !routeStep.isStart())
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("nonAdjacentPenalty");
    }

    private Constraint routeIsACycle(ConstraintFactory constraintFactory) {
        // Route starts with the same vertex as it ends
        return constraintFactory
                .forEach(RouteStep.class)
                .filter(RouteStep::getIsActive)
                .join(RouteStep.class,
                        Joiners.filtering((step1, step2) -> step2.getIsActive() && (step1.getPrevStep() == null || !step1.getPrevStep().getIsActive())),
                        Joiners.filtering((step1, step2) -> step2.getIsActive() && (step2.getNextStep() == null || !step2.getNextStep().getIsActive())))
                .penalize(HardSoftScore.ONE_HARD,
                        (firstStep, lastStep) -> firstStep.getStartVertex().equals(lastStep.getEndVertex()) ? 0 : 1)
                .asConstraint("startAndEndAtSameVertex");
    }
}
