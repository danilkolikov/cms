package chaos;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Math.abs;

/**
 * Solver for chaos task
 *
 * @author Danil Kolikov
 */
public class Solver {
    /**
     * Required function
     *
     * @param r Parameter
     * @param x Argument
     * @return Result
     */
    private static double f(double r, double x) {
        return r * x * (1 - x);
    }

    /**
     * Find one root of x = r * x * (1 - x)
     *
     * @param r             Parameter
     * @param eps           Epsilon
     * @param maxIterations Maximal number of iterations
     * @return Found root, if method converges, or find one of roots, if method doesn't converge
     */
    public static double findRoot(double r, double eps, int maxIterations) {
        // phi' = r * (1 - 2 * x)
        // For converge, we need that |phi'(x)| <= q < 1
        // Assume that we need q = 1/2
        // Then we should take such x that (2 * r - 1) / (4 * r) < x < (2 * r + 1) / (4 * r)
        // Let's take the middle
        double current = 0.5;
        double previous;
        int iterations = 0;
        do {
            previous = current;
            current = f(r, previous);
            if (abs(current - previous) < eps) {
                break;
            }
        } while (++iterations < maxIterations);
        return current;
    }

    /**
     * Find all roots of x = r * x * (1 - x) to which method of simple iteration converges
     *
     * @param r   Parameter
     * @param eps Epsilon
     * @return List of roots
     */
    @Nonnull
    public static List<Double> findRoots(double r, double eps, int maxIterations) {
        return findAllRoots(r, eps, maxIterations).stream()
                .filter(ConvergedRoot::isConverged)
                .map(ConvergedRoot::getValue)
                .collect(Collectors.toList());
    }

    /**
     * Find series of points, starting from 0.5 to all roots
     *
     * @param r Parameter
     * @param eps Epsilon
     * @return Series of points
     */
    @Nonnull
    public static List<Double> findConvergeSeries(double r, double eps, int maxIterations) {
        return findAllRoots(r, eps, maxIterations).stream()
                .map(ConvergedRoot::getValue)
                .collect(Collectors.toList());
    }

    @Nonnull
    private static List<ConvergedRoot> findAllRoots(double r, double eps, int maxIterations) {
        double current = 0.5;
        int iterations = 0;

        List<ConvergedRoot> roots = new ArrayList<>();
        ConvergedRoot closest = new ConvergedRoot(current, false);
        roots.add(closest);

        do {
            current = f(r, closest.getValue());

            // Find closest root
            for (ConvergedRoot root : roots) {
                if (Double.compare(abs(root.value - current), abs(closest.value - current)) < 0) {
                    closest = root;
                }
            }

            if (abs(closest.value - current) < eps) {
                // Root converged
                closest.converged = true;
            } else {
                // Root doesn't converge
                closest = new ConvergedRoot(current, false);
                roots.add(closest);
            }

            // If we found a finite number of roots, then we found a cycle.
            // If last root in cycle converge, that all roots were converged
            if (roots.get(roots.size() - 1).isConverged()) {
                break;
            }
        } while (++iterations < maxIterations);

        return roots;
    }

    /**
     * Root of equation that can be converged or not
     */
    private static class ConvergedRoot {
        double value;

        boolean converged;

        public ConvergedRoot(double value, boolean converged) {
            this.value = value;
            this.converged = converged;
        }

        public double getValue() {
            return value;
        }

        public boolean isConverged() {
            return converged;
        }
    }
}
