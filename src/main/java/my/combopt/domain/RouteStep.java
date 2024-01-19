package my.combopt.domain;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;
import ai.timefold.solver.core.api.domain.variable.ShadowVariable;
import my.combopt.solver.CumulativeWeightUpdatingVariableListener;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@PlanningEntity
@Setter @Getter @NoArgsConstructor
public class RouteStep {
    private Vertex currentVertex;

    private RouteStep previousStep;
    private RouteStep nextStep;

    @PlanningVariable(valueRangeProviderRefs = "vertexRange")
    private Vertex nextVertex;

    @ShadowVariable(variableListenerClass = CumulativeWeightUpdatingVariableListener.class,
            sourceVariableName = "nextVertex")
    private Double cumulativeWeight;

    public RouteStep(Vertex current, Vertex next) {
        this.currentVertex = current;
        this.nextVertex = next;
    }
}