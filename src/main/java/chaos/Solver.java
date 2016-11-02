package chaos;

import org.apache.commons.math3.util.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
     * Find all roots of x = r * x * (1 - x) to which method of simple iteration converges
     *
     * @param r   Parameter
     * @param eps Epsilon
     * @return List of roots
     */
    @Nonnull
    public static List<Double> findRoots(double r, double eps, int maxIterations) {
        double current = 0.5;

        int currentSkip = 1;
        do {
            double previous = current;

            int length = 1;
            for (; length < currentSkip; length++) {
                current = f(r, current);
                if (equals(previous, current, eps)) {
                    break;
                }
            }
            if (equals(previous, current, eps)) {
                // cycle possibly found
                List<Double> result = checkCycle(current, r, length, eps);
                if (result != null) {
                    return result;
                }
            }
            currentSkip *= 2;
        } while (currentSkip <= maxIterations);

        List<Double> extra = new ArrayList<>(10);
        for (int i = 0; i < 10; i++) {
            extra.add(f(r, current));
        }
        return extra;
    }

    /**
     * Check that there is a cycle of converged points starting in specified position
     *
     * @param start  Start position of cycle
     * @param r      Parameter
     * @param length Length of a cycle
     * @param eps    Epsilon
     * @return Cycle, if there is some, or null
     */
    @Nullable
    private static List<Double> checkCycle(double start, double r, int length, double eps) {
        List<Double> result = new ArrayList<>(length);
        // get cycle
        for (int i = 0; i < length; i++) {
            result.add(start);
            start = f(r, start);
        }
        // Check that cycle converged. For do it check that every element X equals to f^length (X)
        boolean cycleConverged = equals(start, result.get(0), eps);

        for (int i = 1; i < result.size(); i++) {
            cycleConverged &= equals(result.get(i), f(r, result.get(i - 1)), eps);
        }
        if (cycleConverged) {
            return result;
        } else {
            return null;
        }
    }

    private static boolean equals(double a, double b, double eps) {
        return abs(a - b) < eps;
    }

    /**
     * Find series of points, starting from 0.5 to all roots
     *
     * @param r   Parameter
     * @param eps Epsilon
     * @return Series of points
     */
    @Nonnull
    public static List<Double> findConvergeSeries(double r, double eps, int maxIterations) {
        double current = 0.5;

        List<Double> result = new ArrayList<>();
        result.add(current);
        int currentSkip = 1;
        do {
            double previous = current;

            int length = 1;
            for (; length < currentSkip; length++) {
                current = f(r, current);
                if (equals(previous, current, eps)) {
                    break;
                }
                result.add(current);
            }
            if (equals(previous, current, eps)) {
                // cycle possibly found
                List<Double> cycle = checkCycle(current, r, length, eps);
                if (cycle != null) {
                    result.addAll(cycle);
                    return result;
                }
            }
            currentSkip *= 2;
        } while (currentSkip <= maxIterations);
        return result;
    }

    public static class AsyncSolver {
        private final ExecutorService executorService;
        private final int threadsCount;

        public AsyncSolver() {
            threadsCount = Runtime.getRuntime().availableProcessors();
            executorService = Executors.newFixedThreadPool(threadsCount);
        }

        public List<Pair<Double, List<Double>>> solve(double left, double right, double pointsCount,
                                                      double eps, int maxIterations) {
            List<Pair<Double, List<Double>>> result = new ArrayList<>();
            List<Future<List<Pair<Double, List<Double>>>>> futures = new ArrayList<>();

            long before = System.currentTimeMillis();
            double totalStep = (right - left) / threadsCount;
            for (int i = 0; i < threadsCount; i++) {
                final double currentLeft = left + totalStep * i;
                final double currentRight = left + totalStep * (i + 1);

                futures.add(executorService.submit(() -> {
                    List<Pair<Double, List<Double>>> points = new ArrayList<>();
                    double step = (currentRight - currentLeft) / (pointsCount / threadsCount);
                    for (double r = currentLeft; r < currentRight; r += step) {
                        points.add(new Pair<>(r, findRoots(r, eps, maxIterations)));
                    }
                    return points;
                }));
            }
            for (Future<List<Pair<Double, List<Double>>>> future : futures) {
                try {
                    result.addAll(future.get());
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
            long after = System.currentTimeMillis();
            System.out.println(after - before);
            return result;
        }
    }
}
