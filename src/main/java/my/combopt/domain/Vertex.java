package my.combopt.domain;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter @Setter @NoArgsConstructor
@JsonIdentityInfo(scope = Vertex.class,
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
public class Vertex {
    private long id;
    private List<Long> neighbours;
    private Double lat;
    private Double lon;

    public Vertex(long id, Double lat, Double lon) {
        this.id = id;
        this.lat = lat;
        this.lon = lon;
    }
}
