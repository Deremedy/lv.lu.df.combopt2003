package my.combopt.domain;

import ai.timefold.solver.jackson.impl.domain.solution.JacksonSolutionFileIO;
import lv.lu.df.combopt.domain.RoutingSolution;

public class RouteSolutionJsonIO extends JacksonSolutionFileIO<RouteSolution> {
    public RouteSolutionJsonIO() {
        super(RouteSolution.class);
    }
}
