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
            stepsAreChained(constraintFactory),
            minimizeTotalWeight(constraintFactory),
            invalidEdgePenalty(constraintFactory),
//            routeIsACycle(constraintFactory)
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
                .join(
                        RouteStep.class,
                        Joiners.equal(Edge::getStart, RouteStep::getStartVertex),
                        Joiners.equal(Edge::getEnd, RouteStep::getEndVertex)
                )
                .filter((edge, routeStep) -> !routeStep.getIsActive())
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("visitedAllEdges");
    }

    public Constraint stepsAreChained(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEach(RouteStep.class)
                .filter(RouteStep::getIsActive)  // Only consider active steps
                .join(RouteStep.class,
                        Joiners.equal(RouteStep::getEndVertex, RouteStep::getStartVertex),
                        Joiners.filtering((step1, step2) -> step2.getIsActive())) // Join with next active step
                .filter((step1, step2) -> !Objects.equals(step1.getNextStep(), step2)) // Check if steps are chained
                .penalize(HardSoftScore.ONE_HARD, (step1, step2) -> 1)
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
                .filter(routeStep -> !routeStep.getStartVertex().getNeighbours().contains(routeStep.getEndVertex()))
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
