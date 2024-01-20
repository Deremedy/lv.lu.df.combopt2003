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
            routeStartsAndEndsAtSameVertex(constraintFactory)
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
                        Joiners.equal(Edge::getStart, RouteStep::getCurrentVertex),
                        Joiners.equal(Edge::getEnd, RouteStep::getNextVertex)
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
                        Joiners.equal(RouteStep::getNextVertex, RouteStep::getCurrentVertex),
                        Joiners.filtering((step1, step2) -> step2.getIsActive())) // Join with next active step
                .filter((step1, step2) -> !Objects.equals(step1.getNextStep(), step2)) // Check if steps are chained
                .penalize(HardSoftScore.ONE_HARD, (step1, step2) -> 1)
                .asConstraint("stepsAreChained");
    }

    public Constraint minimizeTotalWeight(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEach(RouteStep.class)
                .filter(routeStep -> routeStep.getNextVertex() != null)
                .join(Edge.class,
                        Joiners.equal(RouteStep::getCurrentVertex, Edge::getStart),
                        Joiners.equal(RouteStep::getNextVertex, Edge::getEnd))
                .penalize(HardSoftScore.ONE_SOFT, (routeStep, edge) ->
                        (int)edge.getWeight())
                .asConstraint("minimizeTotalWeight");
    }
    public Constraint invalidEdgePenalty(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEach(RouteStep.class)
                .filter(routeStep -> routeStep.getNextVertex() != null)
                .filter(routeStep -> !routeStep.getCurrentVertex().getNeighbours().contains(routeStep.getNextVertex()))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("nonAdjacentPenalty");
    }

    private Constraint routeStartsAndEndsAtSameVertex(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEach(RouteStep.class)
                .filter(RouteStep::isRouteStart)
                .join(RouteStep.class,
                        Joiners.filtering((firstStep, lastStep) -> lastStep.isRouteEnd()))
                .penalize(HardSoftScore.ONE_HARD,
                        (firstStep, lastStep) -> firstStep.getCurrentVertex().equals(lastStep.getNextVertex()) ? 0 : 1)
                .asConstraint("startAndEndAtSameVertex");
    }
}
