package my.combopt.rest;

import ai.timefold.solver.core.api.score.analysis.ScoreAnalysis;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.score.constraint.Indictment;
import ai.timefold.solver.core.api.solver.SolutionManager;
import ai.timefold.solver.core.api.solver.SolverManager;
import jakarta.annotation.PostConstruct;
import my.combopt.solver.SimpleIndictmentObject;
import my.combopt.domain.RouteSolution;
import my.combopt.utils.ProblemGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/routes")
public class RoutingController {
    @Autowired
    private SolverManager<RouteSolution, String> solverManager;
    @Autowired
    private SolutionManager<RouteSolution, HardSoftScore> solutionManager;

    private Map<String, RouteSolution> solutionMap = new HashMap<>();

//    private Router ghRouter = Router.getDefaultRouterInstance();

    @PostMapping("/solve")
    public void solve(@RequestBody RouteSolution problem) {
//        ghRouter.setDistanceTimeMap(problem.getLocationList());
        solverManager.solveAndListen(problem.getSolutionId(), id -> problem,
                solution -> solutionMap.put(solution.getSolutionId(), solution));
    }

    @GetMapping("/solution")
    public RouteSolution solution(@RequestParam String id) {
        return solutionMap.get(id);
    }

    @GetMapping("/list")
    public List<RouteSolution> list() {
        return solutionMap.values().stream().toList();
    }

    @GetMapping("/score")
    public ScoreAnalysis<HardSoftScore> score(@RequestParam String id) {
        return solutionManager.analyze(solutionMap.get(id));
    }
//
    @GetMapping("/indictments")
    public List<SimpleIndictmentObject> indictments(@RequestParam String id) {
        return solutionManager.explain(solutionMap.getOrDefault(id, null)).getIndictmentMap().entrySet().stream()
                .map(entry -> {
                    Indictment<HardSoftScore> indictment = entry.getValue();
                    return
                            new SimpleIndictmentObject(entry.getKey(), // indicted Object
                                    indictment.getScore(),
                                    indictment.getConstraintMatchCount(),
                                    indictment.getConstraintMatchSet());
                }).collect(Collectors.toList());
    }
//
    @PostConstruct
    public void init() {
        ProblemGenerator problemGenerator = new ProblemGenerator();
        RouteSolution problem1 = problemGenerator.getProblem1();
        RouteSolution problem2 = problemGenerator.getProblem2();
        RouteSolution problem3 = problemGenerator.getProblem3();
        RouteSolution problem4 = problemGenerator.getProblem4();
        RouteSolution problem5 = problemGenerator.getProblem5();
//        ghRouter.setDistanceTimeMap(problem50.getLocationList());
        //solutionIOJSON.write(problem50, new File("data/exampleRiga50.json"));
        solverManager.solveAndListen(problem1.getSolutionId(), id -> problem1, solution -> {
            solutionMap.put(solution.getSolutionId(), solution);});
        solverManager.solveAndListen(problem2.getSolutionId(), id -> problem2, solution -> {
            solutionMap.put(solution.getSolutionId(), solution);});
        solverManager.solveAndListen(problem3.getSolutionId(), id -> problem3, solution -> {
            solutionMap.put(solution.getSolutionId(), solution);});
        solverManager.solveAndListen(problem4.getSolutionId(), id -> problem4, solution -> {
            solutionMap.put(solution.getSolutionId(), solution);});
        solverManager.solveAndListen(problem5.getSolutionId(), id -> problem5, solution -> {
            solutionMap.put(solution.getSolutionId(), solution);});
    }
}
