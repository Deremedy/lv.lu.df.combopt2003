package my.combopt.solver;

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.score.stream.Joiners;
import my.combopt.domain.Edge;
import my.combopt.domain.RouteStep;

public class RouteSolutionConstraintProvider implements ConstraintProvider {
    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
            minimizeTotalWeight(constraintFactory),
            invalidEdgePenalty(constraintFactory),
            startAndEndAtSameVertex(constraintFactory)
        };
    }

    public Constraint minimizeTotalWeight(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEach(RouteStep.class)
                .filter(routeStep -> routeStep.getNextVertex() != null)
                .join(Edge.class, Joiners.equal(RouteStep::getNextVertex, Edge::getStart))
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

    private Constraint startAndEndAtSameVertex(ConstraintFactory constraintFactory) {
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
