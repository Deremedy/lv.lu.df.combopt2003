package my.combopt.utils;

import my.combopt.domain.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import org.json.JSONObject;
import org.json.JSONArray;

public class ProblemGenerator {
    public RouteSolution generateSimpleProblem() {
        RouteSolution problem = new RouteSolution();
        problem.setSolutionId("P0");

        // V4 ? V3
        // ?     ?
        // V1 ? V2
        Vertex v1 = new Vertex(1, 0.0, 0.0);
        Vertex v2 = new Vertex(2, 1.0, 0.0);
        Vertex v3 = new Vertex(3, 1.0, 1.0);
        Vertex v4 = new Vertex(4, 0.0, 1.0);

        v1.setNeighbours(List.of(2L, 3L, 4L));
        v2.setNeighbours(List.of(1L, 3L, 4L));
        v3.setNeighbours(List.of(1L, 2L, 4L));
        v4.setNeighbours(List.of(1L, 2L, 3L));

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
                1L, List.of(
                        new AdjacentVertexWithWeight(v2, 1.0),
                        new AdjacentVertexWithWeight(v3, 1.0),
                        new AdjacentVertexWithWeight(v4, 1.0)
                ),
                2L, List.of(
                        new AdjacentVertexWithWeight(v1, 1.0),
                        new AdjacentVertexWithWeight(v3, 1.0),
                        new AdjacentVertexWithWeight(v4, 1.0)
                ),
                3L, List.of(
                        new AdjacentVertexWithWeight(v1, 1.0),
                        new AdjacentVertexWithWeight(v2, 1.0),
                        new AdjacentVertexWithWeight(v4, 1.0)
                ),
                4L, List.of(
                        new AdjacentVertexWithWeight(v1, 1.0),
                        new AdjacentVertexWithWeight(v2, 1.0),
                        new AdjacentVertexWithWeight(v3, 1.0)
                )
        ));
        problem.setVertexList(List.of(v1, v2, v3, v4));
        problem.setEdgeList(List.of(e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11, e12));

        Vertex startVertex = v1;
        List<RouteStep> routeSteps = new ArrayList<>();
        routeSteps.add(new RouteStep(startVertex, startVertex));
        Set<Vertex> visited = new HashSet<>();
        dfsTraversal(startVertex, visited, problem.getAdjacencyListWithWeights(), routeSteps);

        // For each route step, set next step
        for (int i = 0; i < routeSteps.size(); i++) {
            RouteStep currentStep = routeSteps.get(i);
            RouteStep nextStep = routeSteps.get((i + 1) % routeSteps.size());
            currentStep.setNextStep(nextStep);
            nextStep.setPrevStep(currentStep);
        }
        routeSteps.get(routeSteps.size() - 1).setNextStep(routeSteps.get(0)); // Set last step to point to first step
        routeSteps.get(0).setStart(true); // Set first step as anchor

        for (int i = 1; i < routeSteps.size(); i++) {
            RouteStep currentStep = routeSteps.get(i);
            currentStep.setAnchor(routeSteps.get(0)); // All subsequent steps in the same chain have the same anchor
        }

        problem.setRouteSteps(routeSteps);

        return problem;
    }

    private static void dfsTraversal(Vertex current, Set<Vertex> visited, Map<Long, List<AdjacentVertexWithWeight>> adjacencyList, List<RouteStep> steps) {
        visited.add(current);

        // Separate neighbors into unvisited and visited
        List<Vertex> unvisitedNeighbors = new ArrayList<>();
        List<Vertex> visitedNeighbors = new ArrayList<>();
        for (AdjacentVertexWithWeight adjacent : adjacencyList.getOrDefault(current.getId(), Collections.emptyList())) {
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

    public RouteSolution getProblem1() {
        RouteSolution problem = generateProblemFromJson("data/problems/problem14_200.json");
        problem.setSolutionId("P1");

        problem.setStartVertex(problem.getVertexList().get(1));

        List<RouteStep> routeSteps = generateRouteSteps(problem);
        problem.setRouteSteps(routeSteps);

        return problem;
    }

    public RouteSolution getProblem2() {
        RouteSolution problem = generateProblemFromJson("data/problems/problem52_300.json");
        problem.setSolutionId("P2");

        problem.setStartVertex(problem.getVertexList().get(0));

        List<RouteStep> routeSteps = generateRouteSteps(problem);
        problem.setRouteSteps(routeSteps);

        return problem;
    }

    public RouteSolution getProblem3() {
        RouteSolution problem = generateProblemFromJson("data/problems/problem118_500.json");
        problem.setSolutionId("P3");

        problem.setStartVertex(problem.getVertexList().get(0));

        List<RouteStep> routeSteps = generateRouteSteps(problem);
        problem.setRouteSteps(routeSteps);

        return problem;
    }


    public RouteSolution getProblem4() {
        RouteSolution problem = generateProblemFromJson("data/problems/problem173_600.json");
        problem.setSolutionId("P4");

        problem.setStartVertex(problem.getVertexList().get(0));

        List<RouteStep> routeSteps = generateRouteSteps(problem);
        problem.setRouteSteps(routeSteps);

        return problem;
    }

    public RouteSolution getProblem5() {
        RouteSolution problem = generateProblemFromJson("data/problems/problem293_800.json");
        problem.setSolutionId("P5");

        problem.setStartVertex(problem.getVertexList().get(0));

        List<RouteStep> routeSteps = generateRouteSteps(problem);
        problem.setRouteSteps(routeSteps);

        return problem;
    }

    private List<RouteStep> generateRouteSteps(RouteSolution problem) {
        Vertex startVertex = problem.getStartVertex();
        List<RouteStep> routeSteps = new ArrayList<>();
        routeSteps.add(new RouteStep(startVertex, startVertex));
        Set<Vertex> visited = new HashSet<>();
        dfsTraversal(startVertex, visited, problem.getAdjacencyListWithWeights(), routeSteps);

        // For each route step, set next step
        for (int i = 0; i < routeSteps.size(); i++) {
            RouteStep currentStep = routeSteps.get(i);
            RouteStep nextStep = routeSteps.get((i + 1) % routeSteps.size());
            currentStep.setNextStep(nextStep);
            nextStep.setPrevStep(currentStep);
        }
        routeSteps.get(routeSteps.size() - 1).setNextStep(routeSteps.get(0)); // Set last step to point to first step
        routeSteps.get(0).setStart(true); // Set first step as anchor

        for (int i = 1; i < routeSteps.size(); i++) {
            RouteStep currentStep = routeSteps.get(i);
            currentStep.setAnchor(routeSteps.get(0)); // All subsequent steps in the same chain have the same anchor
        }

        return routeSteps;
    }

    public RouteSolution generateProblemFromJson(String filePath) {
        try {
            String content = new String(Files.readAllBytes(Paths.get(filePath)));
            JSONObject json = new JSONObject(content);

            // Assuming your JSON structure contains vertices and their coordinates
            JSONObject verticesJson = json.getJSONObject("vertices");
            Map<Long, Vertex> vertices = new HashMap<>();

            // Parsing vertices
            for (String key : verticesJson.keySet()) {
                JSONArray vertexInfo = verticesJson.getJSONArray(key);
                double lat = vertexInfo.getJSONObject(0).getDouble("lat");
                double lon = vertexInfo.getJSONObject(0).getDouble("lon");
                Vertex vertex = new Vertex(Long.parseLong(key), lat, lon);
//                vertices.put(Integer.parseInt(key), vertex);
                vertices.put(Long.parseLong(key), vertex);
            }

            JSONObject adjacencyList = json.getJSONObject("adj_list");
            Map<Long, List<AdjacentVertexWithWeight>> adjacencyListWithWeights = new HashMap<>();

            // Parsing adjacency list
            for (String key : adjacencyList.keySet()) {
                JSONArray adjacentVertices = adjacencyList.getJSONArray(key);
                List<AdjacentVertexWithWeight> adjacentVerticesWithWeights = new ArrayList<>();
                for (int i = 0; i < adjacentVertices.length(); i++) {
                    JSONObject adjacentVertex = adjacentVertices.getJSONObject(i);
                    long id = adjacentVertex.getLong("vertex");
                    double weight = adjacentVertex.getDouble("weight");
                    adjacentVerticesWithWeights.add(new AdjacentVertexWithWeight(vertices.get(id), weight));
                }
                adjacencyListWithWeights.put(Long.parseLong(key), adjacentVerticesWithWeights);
            }

            // Parsing edges
            List<Edge> edges = new ArrayList<>();
            adjacencyListWithWeights.forEach((vertexId, adjacentVertices) -> {
                for (AdjacentVertexWithWeight adjacentVertex : adjacentVertices) {
                    edges.add(new Edge(edges.size() + 1, vertices.get(vertexId), adjacentVertex.getVertex(), adjacentVertex.getWeight()));
                }
            });

            vertices.forEach((id, vertex) -> {
                List<AdjacentVertexWithWeight> adjacentVertices = adjacencyListWithWeights.getOrDefault(id, Collections.emptyList());
                vertex.setNeighbours(adjacentVertices.stream().map(AdjacentVertexWithWeight::getVertex).map(Vertex::getId).toList());
            });

            // Create and return RouteSolution object
            RouteSolution problem = new RouteSolution();
            problem.setAdjacencyListWithWeights(adjacencyListWithWeights);
            problem.setVertexList(new ArrayList<>(vertices.values()));
            problem.setEdgeList(new ArrayList<>(edges));

            return problem;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
