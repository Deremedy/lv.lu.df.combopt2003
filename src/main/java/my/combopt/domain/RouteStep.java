package my.combopt.domain;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.variable.AnchorShadowVariable;
import ai.timefold.solver.core.api.domain.variable.InverseRelationShadowVariable;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;
import ai.timefold.solver.core.api.domain.variable.PlanningVariableGraphType;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@PlanningEntity
@Setter @Getter @NoArgsConstructor
@JsonIdentityInfo(scope = RouteStep.class,
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
public class RouteStep {
    private static int nextId = 0;

    private int id;
    private Vertex startVertex;
    private Vertex endVertex;

    @InverseRelationShadowVariable(sourceVariableName = "nextStep")
    @JsonIdentityReference(alwaysAsId = true)
    private RouteStep prevStep;

    @PlanningVariable(valueRangeProviderRefs = "routeStepRange",
            graphType = PlanningVariableGraphType.CHAINED)
    @JsonIdentityReference(alwaysAsId = true)
    private RouteStep nextStep;

    @AnchorShadowVariable(sourceVariableName = "nextStep")
    @JsonIdentityReference(alwaysAsId = true)
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