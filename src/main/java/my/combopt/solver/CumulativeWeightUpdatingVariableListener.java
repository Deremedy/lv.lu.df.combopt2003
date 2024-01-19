package my.combopt.solver;

import ai.timefold.solver.core.api.domain.variable.VariableListener;
import ai.timefold.solver.core.api.score.director.ScoreDirector;
import my.combopt.domain.RouteSolution;
import my.combopt.domain.RouteStep;
import my.combopt.domain.Vertex;

public class CumulativeWeightUpdatingVariableListener implements VariableListener<RouteSolution, RouteStep> {
    public void beforeVariableChanged(ScoreDirector<RouteSolution> scoreDirector, RouteStep routeStep) {}

    @Override
    public void afterVariableChanged(ScoreDirector<RouteSolution> scoreDirector, RouteStep routeStep) {
        recalculateCumulativeWeightFromStep(scoreDirector, routeStep);
    }

    private void recalculateCumulativeWeightFromStep(ScoreDirector<RouteSolution> scoreDirector, RouteStep startingStep) {
        RouteStep step = startingStep;
        Double cumulativeWeight = (step.getPreviousStep() != null) ? step.getPreviousStep().getCumulativeWeight() : 0L;

        while (step != null) {
            scoreDirector.beforeVariableChanged(step, "cumulativeWeight");
            if (step.getNextVertex() != null && !step.getCurrentVertex().equals(step.getNextVertex())) {
                cumulativeWeight += getEdgeWeight(scoreDirector.getWorkingSolution(), step.getCurrentVertex(), step.getNextVertex());
            }
            step.setCumulativeWeight(cumulativeWeight);
            scoreDirector.afterVariableChanged(step, "cumulativeWeight");

            step = step.getNextStep(); // getNextStep() should return the next step in the route
        }
    }

    private boolean hasEdge(RouteSolution routeSolution, Vertex from, Vertex to) {
        return routeSolution.hasEdge(from, to);
    }

    private double getEdgeWeight(RouteSolution routeSolution, Vertex from, Vertex to) {
        return routeSolution.getWeight(from, to);
    }

    public void beforeEntityAdded(ScoreDirector<RouteSolution> scoreDirector, RouteStep routeStep) {}

    public void afterEntityAdded(ScoreDirector<RouteSolution> scoreDirector, RouteStep routeStep) {}

    public void beforeEntityRemoved(ScoreDirector<RouteSolution> scoreDirector, RouteStep routeStep) {}

    public void afterEntityRemoved(ScoreDirector<RouteSolution> scoreDirector, RouteStep routeStep) {}
}
