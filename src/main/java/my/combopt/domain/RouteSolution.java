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
            System.out.println(step.getCurrentVertex().getId() + " -> " + step.getNextVertex().getId() + " (" + step.getIsActive() + ")");
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

        v1.setNeighbours(List.of(v2, v3, v4));
        v2.setNeighbours(List.of(v1, v3, v4));
        v3.setNeighbours(List.of(v1, v2, v4));
        v4.setNeighbours(List.of(v1, v2, v3));

        // V4   E3  V3
        // E4 E5/E6 E2
        // V1   E1  V2
        Edge e1 = new Edge(1, v1, v2, 1.0);
        Edge e2 = new Edge(2, v2, v3, 1.0);
        Edge e3 = new Edge(3, v3, v4, 1.0);
        Edge e4 = new Edge(4, v4, v1, 1.0);
        Edge e5 = new Edge(5, v1, v3, 1.0);
        Edge e6 = new Edge(6, v4, v2, 1.0);
        Edge e7 = new Edge(7, v2, v1, 1.0);
        Edge e8 = new Edge(8, v3, v2, 1.0);
        Edge e9 = new Edge(9, v4, v3, 1.0);
        Edge e10 = new Edge(10, v1, v4, 1.0);
        Edge e11 = new Edge(11, v3, v1, 1.0);
        Edge e12 = new Edge(12, v2, v4, 1.0);

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
        problem.setEdgeList(List.of(e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11, e12));

        List<RouteStep> routeSteps = new ArrayList<>();
        Set<Vertex> visited = new HashSet<>();
        dfsTraversal(v1, visited, problem.getAdjacencyListWithWeights(), routeSteps);

//        List<RouteStep> routeSteps = RouteInitSolutionGenerator.generateInitialSolution(problem.getAdjacencyListWithWeights());

        routeSteps.get(0).setRouteStart(true);
        routeSteps.get(routeSteps.size() - 1).setRouteEnd(true);
        problem.setRouteSteps(routeSteps);

        return problem;
    }

    private static void dfsTraversal(Vertex current, Set<Vertex> visited, Map<Vertex, List<AdjacentVertexWithWeight>> adjacencyList, List<RouteStep> steps) {
        visited.add(current);

        // Separate neighbors into unvisited and visited
        List<Vertex> unvisitedNeighbors = new ArrayList<>();
        List<Vertex> visitedNeighbors = new ArrayList<>();
        for (AdjacentVertexWithWeight adjacent : adjacencyList.get(current)) {
            Vertex next = adjacent.getVertex();
            if (visited.contains(next)) {
                visitedNeighbors.add(next);
            } else {
                unvisitedNeighbors.add(next);
            }
        }

        // First, traverse to all unvisited neighbors
        for (Vertex next : unvisitedNeighbors) {
            steps.add(new RouteStep(current, next)); // Traverse to next vertex
            dfsTraversal(next, visited, adjacencyList, steps); // Continue DFS from there
            steps.add(new RouteStep(next, current)); // Traverse back
        }

        // Then, traverse to visited neighbors
        for (Vertex next : visitedNeighbors) {
            steps.add(new RouteStep(current, next)); // Traverse to next vertex
            // No need to call dfsTraversal since 'next' is already visited
            steps.add(new RouteStep(next, current)); // Traverse back
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

    @ValueRangeProvider(id = "statusRange")
    public List<Boolean> getStatusRange() {
        return List.of(true, false);
    }
}