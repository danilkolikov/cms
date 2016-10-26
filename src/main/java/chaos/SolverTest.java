package chaos;

import java.util.List;

import static chaos.Solver.findRoots;
import static org.junit.Assert.assertFalse;

/**
 * Test for Solver
 */
public class SolverTest {
    @org.junit.Test
    public void testFindRoots() throws Exception {
        double eps = 0.01;
        int maxIterations = 1_000;
        for (double r = 0; r < 5; r += 0.01) {
            List<Double> roots = findRoots(r, eps, maxIterations);
            System.out.println(r + " " + roots.size() + " " + roots);
            assertFalse(roots.isEmpty());
        }
    }
}