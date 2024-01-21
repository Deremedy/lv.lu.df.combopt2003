package my.combopt.domain;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.variable.AnchorShadowVariable;
import ai.timefold.solver.core.api.domain.variable.InverseRelationShadowVariable;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;
import ai.timefold.solver.core.api.domain.variable.PlanningVariableGraphType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@PlanningEntity
@Setter @Getter @NoArgsConstructor
public class RouteStep {
    private static int nextId = 0;

    private int id;
    private Vertex startVertex;
    private Vertex endVertex;

    @InverseRelationShadowVariable(sourceVariableName = "nextStep")
    private RouteStep prevStep;

    @PlanningVariable(valueRangeProviderRefs = "routeStepRange",
            graphType = PlanningVariableGraphType.CHAINED)
    private RouteStep nextStep;

    @AnchorShadowVariable(sourceVariableName = "nextStep")
    private RouteStep anchor;

    @PlanningVariable(valueRangeProviderRefs = "statusRange")
    private Boolean isActive;

    private boolean isStart;

    public RouteStep(Vertex current, Vertex next) {
        this.id = nextId++;
        this.startVertex = current;
        this.endVertex = next;
        this.isActive = true;
    }
}