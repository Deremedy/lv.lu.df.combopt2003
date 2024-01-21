package my.combopt;

import ai.timefold.solver.benchmark.api.PlannerBenchmark;
import ai.timefold.solver.benchmark.api.PlannerBenchmarkFactory;
import lv.lu.df.combopt.domain.RoutingSolution;
import lv.lu.df.combopt.domain.RoutingSolutionJsonIO;
import my.combopt.domain.RouteSolution;
import my.combopt.utils.ProblemGenerator;

import java.io.File;

public class BenchmarkerRunner {
    public static void main(String[] args) {
        PlannerBenchmarkFactory benchmarkFactoryFromXML = PlannerBenchmarkFactory
                .createFromXmlResource("MyBenchmarkConfig.xml");

        PlannerBenchmark benchmark = benchmarkFactoryFromXML.buildPlannerBenchmark();

//        RoutingSolutionJsonIO routingSolutionJsonIO = new RoutingSolutionJsonIO();
//        routingSolutionJsonIO.write(RoutingSolution.generateData(5),
//                new File("data/classExample5.json"));

//        PlannerBenchmark benchmark = benchmarkFactoryFromXML.buildPlannerBenchmark();

        benchmark.benchmarkAndShowReportInBrowser();

    }
}
