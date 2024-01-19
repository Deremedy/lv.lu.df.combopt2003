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

    private Map<Vertex, List<AdjacentVertexWithWeight>> adjacencyListWithWeights;

    @PlanningEntityCollectionProperty
    private List<RouteStep> routeSteps;

    public void print() {
        this.getRouteSteps().forEach(step -> {
            System.out.println(step.getCurrentVertex().getId() + " -> " + step.getNextVertex().getId());
        });
    }

    public static RouteSolution generateData() {
        RouteSolution problem = new RouteSolution();
        problem.setSolutionId("P1");

        // V4 ? V3
        // ?     ?
        // V1 ? V2
        Vertex v1 = new Vertex(1, 0.0, 0.0);
        Vertex v2 = new Vertex(2, 1.0, 0.0);
        Vertex v3 = new Vertex(3, 1.0, 1.0);
        Vertex v4 = new Vertex(4, 0.0, 1.0);

        // V4   E3  V3
        // E4 E5/E6 E2
        // V1   E1  V2
        Edge e1 = new Edge(1, v1, v2, 1.0, false);
        Edge e2 = new Edge(2, v2, v3, 1.0, false);
        Edge e3 = new Edge(3, v3, v4, 1.0, false);
        Edge e4 = new Edge(4, v4, v1, 1.0, false);
        Edge e5 = new Edge(5, v1, v3, 1.0, false);
        Edge e6 = new Edge(6, v4, v2, 1.0, false);

        problem.setAdjacencyListWithWeights(Map.of(
                v1, List.of(
                        new AdjacentVertexWithWeight(v2, 1.0),
                        new AdjacentVertexWithWeight(v3, 1.0),
                        new AdjacentVertexWithWeight(v4, 1.0)
                ),
                v2, List.of(
                        new AdjacentVertexWithWeight(v1, 1.0),
                        new AdjacentVertexWithWeight(v3, 1.0),
                        new AdjacentVertexWithWeight(v4, 1.0)
                ),
                v3, List.of(
                        new AdjacentVertexWithWeight(v1, 1.0),
                        new AdjacentVertexWithWeight(v2, 1.0),
                        new AdjacentVertexWithWeight(v4, 1.0)
                ),
                v4, List.of(
                        new AdjacentVertexWithWeight(v1, 1.0),
                        new AdjacentVertexWithWeight(v2, 1.0),
                        new AdjacentVertexWithWeight(v3, 1.0)
                )
        ));
        problem.setVertexList(List.of(v1, v2, v3, v4));
        problem.setEdgeList(List.of(e1, e2, e3, e4, e5, e6));

        // DFS to create initial RouteSteps for directed edges
//        List<RouteStep> routeSteps = performDirectedDFS(v1, problem.getAdjacencyListWithWeights());
//        problem.setRouteSteps(routeSteps);

        List<RouteStep> routeSteps = new ArrayList<>();
        Set<Vertex> visited = new HashSet<>();
        dfsTraversal(v1, visited, problem.getAdjacencyListWithWeights(), routeSteps);

        problem.setRouteSteps(routeSteps);

        return problem;
    }

    private static void dfsTraversal(Vertex current, Set<Vertex> visited, Map<Vertex, List<AdjacentVertexWithWeight>> adjacencyList, List<RouteStep> steps) {
        visited.add(current);
        for (AdjacentVertexWithWeight adjacent : adjacencyList.get(current)) {
            Vertex next = adjacent.getVertex();

            // Traverse to next vertex
            steps.add(new RouteStep(current, next));

            // If the next vertex is unvisited, continue DFS from there
            if (!visited.contains(next)) {
                dfsTraversal(next, visited, adjacencyList, steps);
            }

            // Traverse back
            steps.add(new RouteStep(next, current));
        }
    }

    public boolean hasEdge(Vertex from, Vertex to) {
        return adjacencyListWithWeights.get(from).stream()
                .anyMatch(adjacentVertexWithWeight -> adjacentVertexWithWeight.getVertex().equals(to));
    }

    public double getWeight(Vertex v1, Vertex v2) {
//        System.out.println(v1.getId() + " -> " + v2.getId());
        return adjacencyListWithWeights.get(v1).stream()
                .filter(adjacentVertexWithWeight -> adjacentVertexWithWeight.getVertex().equals(v2))
                .findFirst()
                .orElseThrow()
                .getWeight();
    }

    public boolean isAdjacent(Vertex from, Vertex to) {
        return adjacencyListWithWeights.get(from).stream()
                .anyMatch(adjacentVertexWithWeight -> adjacentVertexWithWeight.getVertex().equals(to));
    }

    @ValueRangeProvider(id = "vertexRange")
    public List<Vertex> getVertexRange() {
        return vertexList;
    }
}