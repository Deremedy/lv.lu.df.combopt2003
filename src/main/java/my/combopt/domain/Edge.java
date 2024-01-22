package my.combopt.domain;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
@JsonIdentityInfo(scope = Edge.class,
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
public class Edge {
    private long id;
    private Vertex start;
    private Vertex end;
    private double weight; // svars = ielas garums (metros)

    public Edge(long id, Vertex start, Vertex end, double weight) {
        this.id = id;
        this.start = start;
        this.end = end;
        this.weight = weight;
    }
}