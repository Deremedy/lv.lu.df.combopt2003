package my.combopt.solver;

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.score.stream.Joiners;
import my.combopt.domain.Edge;
import my.combopt.domain.RouteSolution;
import my.combopt.domain.RouteStep;

public class RouteSolutionConstraintProvider implements ConstraintProvider {
    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
            minimizeTotalWeight(constraintFactory),
//            nonAdjacentPenalty(constraintFactory),
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

//    private Constraint nonAdjacentPenalty(ConstraintFactory constraintFactory) {
//        return constraintFactory.forEach(RouteStep.class)
//                .filter(routeStep -> {
//                    // Access RouteSolution and check adjacency
//                    RouteSolution solution = (RouteSolution) scoreDirector.getWorkingSolution();
//                    return !solution.isAdjacent(routeStep.getCurrentVertex(), routeStep.getNextVertex());
//                })
//                .penalize(HardSoftScore.ONE_HARD, routeStep -> 1)
//                .asConstraint("Non-adjacent vertex penalty");
//    }
}
