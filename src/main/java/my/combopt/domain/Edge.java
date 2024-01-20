package my.combopt.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class Edge {
    private long id;
    private Vertex start;
    private Vertex end;
    private double weight; // svars = ielas garums (metros)
    private boolean isOneWay; // vai iela ir vienvirziena

    public Edge(long id, Vertex start, Vertex end, double weight) {
        this.id = id;
        this.start = start;
        this.end = end;
        this.weight = weight;
    }
}