package my.combopt.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter @Getter @NoArgsConstructor
public class AdjacentVertexWithWeight {
    private Vertex vertex;
    private double weight; // Edge weight

    public AdjacentVertexWithWeight(Vertex vertex, double weight) {
        this.vertex = vertex;
        this.weight = weight;
    }
}
