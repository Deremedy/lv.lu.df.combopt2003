package my.combopt.utils;

import my.combopt.domain.RouteSolutionJsonIO;

import java.io.File;

public class ProblemSerializer {
    public static void main(String[] args) {
        ProblemGenerator problemGenerator = new ProblemGenerator();
        RouteSolutionJsonIO routingSolutionJsonIO = new RouteSolutionJsonIO();

        routingSolutionJsonIO.write(problemGenerator.getProblem1(), new File("data/tf_problem118_500.json"));
    }
}
