package my.combopt.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter @Setter @NoArgsConstructor
public class Vertex {
    private long id;
    private List<Vertex> neighbours;
    private Double lat;
    private Double lon;

    public Vertex(long id, Double lat, Double lon) {
        this.id = id;
        this.lat = lat;
        this.lon = lon;
    }
}
