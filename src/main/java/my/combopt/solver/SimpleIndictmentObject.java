package my.combopt.solver;

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.score.constraint.ConstraintMatch;
import lombok.Getter;
import lombok.Setter;
import my.combopt.domain.Edge;
import my.combopt.domain.RouteStep;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Setter @Getter
public class SimpleIndictmentObject {
    private String indictedObjectID;
    private HardSoftScore score;
    private int matchCount;
    private List<SimpleConstraintMatch> constraintMatches = new ArrayList<>();

    public SimpleIndictmentObject(Object indictedObject, HardSoftScore score, int matchCount, Set<ConstraintMatch<HardSoftScore>> constraintMatches) {
        if (indictedObject instanceof RouteStep) {
            this.indictedObjectID = "RouteStep"+((RouteStep) indictedObject).getId();
        } else if (indictedObject instanceof Edge) {
            this.indictedObjectID = "Edge"+((Edge) indictedObject).getId();
        } else {
            this.indictedObjectID = "unknown";
        }
//        this.indictedObjectID = indictedObject instanceof RouteStep ? String.valueOf("RouteStep"+((RouteStep) indictedObject).getId()) : "unknown";
        this.score = score;
        this.matchCount = matchCount;
        this.constraintMatches = constraintMatches.stream().map(constraintMatch -> {
            return new SimpleConstraintMatch(constraintMatch);
        }).collect(Collectors.toList());
    }
}
