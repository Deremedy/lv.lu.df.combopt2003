package my.combopt.domain;

import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.*;

@PlanningSolution
@Setter @Getter @NoArgsConstructor
public class RouteSolution {
    private String solutionId;

    @PlanningScore
    private HardSoftScore score;

    @ProblemFactCollectionProperty
    private List<Vertex> vertexList;

    @ProblemFactCollectionProperty
    private List<Edge> edgeList;

    private Vertex startVertex;

    private Map<Vertex, List<AdjacentVertexWithWeight>> adjacencyListWithWeights;

    @PlanningEntityCollectionProperty
    private List<RouteStep> routeSteps;

    public void print() {
        if (routeSteps == null || routeSteps.isEmpty()) {
            System.out.println("No route steps available.");
            return;
        }

        RouteStep currentStep = routeSteps.get(0); // Assuming the first step is the start of the route
        RouteStep startStep = routeSteps.get(0);
        boolean firstStep = true;

        while (currentStep != null) {
            if (currentStep.equals(startStep) && !firstStep) {
                break;
            }

            firstStep = false;
//            if (!currentStep.getIsActive()) {
//                currentStep = currentStep.getNextStep();
//                continue;
//            }
//            System.out.println(currentStep.getStartVertex().getId() + " -> " + currentStep.getEndVertex().getId() + " (" + currentStep.getId() + ")");
            System.out.println(currentStep.getStartVertex().getId() + " -> " + currentStep.getEndVertex().getId() + " (" + currentStep.getIsActive() + ")");

            currentStep = currentStep.getNextStep();
        }
    }

    @ValueRangeProvider(id = "statusRange")
    public List<Boolean> getStatusRange() {
        return List.of(true, false);
    }

    @ValueRangeProvider(id = "routeStepRange")
    public List<RouteStep> getRouteStepRange() {
        return routeSteps;
    }
}