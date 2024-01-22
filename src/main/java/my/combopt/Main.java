package my.combopt;


import ai.timefold.solver.core.api.score.ScoreExplanation;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.solver.SolutionManager;
import ai.timefold.solver.core.api.solver.Solver;
import ai.timefold.solver.core.api.solver.SolverFactory;
import ai.timefold.solver.core.config.solver.EnvironmentMode;
import ai.timefold.solver.core.config.solver.SolverConfig;
import ai.timefold.solver.core.config.solver.termination.TerminationConfig;
import my.combopt.domain.RouteSolution;
import my.combopt.domain.RouteStep;
import my.combopt.solver.RouteSolutionConstraintProvider;
import my.combopt.utils.ProblemGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    public static void main(String[] args) {
        ProblemGenerator problemGenerator = new ProblemGenerator();
        RouteSolution problem = problemGenerator.generateSimpleProblem();
//        RouteSolution problem = problemGenerator.generateProblem();
        problem.print();

        SolverFactory<RouteSolution> solverFactoryFromXML = SolverFactory
                .createFromXmlResource("MySolverConfig.xml");

        SolverFactory<RouteSolution> solverFactory = SolverFactory.create(
                new SolverConfig()
                        .withSolutionClass(RouteSolution.class)
                        .withEntityClasses(RouteStep.class)
                        .withConstraintProviderClass(RouteSolutionConstraintProvider.class)
                        .withTerminationConfig(new TerminationConfig()
                                .withSecondsSpentLimit(30L))
                        .withEnvironmentMode(EnvironmentMode.FULL_ASSERT)
        );

        Solver<RouteSolution> solver = solverFactoryFromXML.buildSolver();
        RouteSolution solution = solver.solve(problem);

        SolutionManager<RouteSolution, HardSoftScore> solutionManager = SolutionManager.create(solverFactory);
        ScoreExplanation<RouteSolution, HardSoftScore> scoreExplanation = solutionManager.explain(solution);
        LOGGER.info(scoreExplanation.getSummary());

        solution.print();
    }
}