package fractal;

import base.NewtonSolver;
import com.sun.javaws.exceptions.InvalidArgumentException;
import org.jblas.ComplexDouble;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;

/**
 * Solver for fractal task
 *
 * @author Danil Kolikov
 */
public class Solver {

    /**
     * Function, which roots we want to find.
     */
    private static Function<ComplexDouble, ComplexDouble> f = z -> z.mul(z).muli(z).subi(ComplexDouble.UNIT);
    /**
     * Derivative of {@code f}.
     */
    private static Function<ComplexDouble, ComplexDouble> df_dz = z -> z.mul(z).muli(3);

    private static final ComplexDouble[] roots = {
            new ComplexDouble(1, 0),
            new ComplexDouble(Math.cos(2 * Math.PI / 3), Math.sin(2 * Math.PI / 3)),
            new ComplexDouble(Math.cos(4 * Math.PI / 3), Math.sin(4 * Math.PI / 3))};

    private static final int pointsPerAxis = 200;

    private ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private NewtonSolver newtonSolver = new NewtonSolver(f, df_dz);

    private int findClosestRoot(ComplexDouble point) {
        double min = roots[0].sub(point).abs();
        int pos = 0;
        for (int i = 1; i < 3; i++) {
            double dist = roots[i].sub(point).abs();
            if (dist < min) {
                min = dist;
                pos = i;
            }
        }
        return pos;
    }

    /**
     * This method finds root for function {@code f} for points in rectangle {@code [a.real, b.real]x[a.imaginary, b.imaginary]}.
     * Parameter {@code a} must be less or equal to parameter {@code b}.
     *
     * @param a the most left and down point of the rectangle
     * @param b the most right and up point of the rectangle
     * @return list of colored points
     */
    public List<ColoredPoint> solve(ComplexDouble a, ComplexDouble b) throws InvalidArgumentException {
        if (a.real() > b.real() || (a.real() == b.real() && a.imag() > b.imag())) {
            throw new InvalidArgumentException(new String[]{"Input points are not in lexicographical order"});
        }

        double stepX = Math.abs(a.real() - b.real()) / pointsPerAxis;
        double stepY = Math.abs(a.imag() - b.imag()) / pointsPerAxis;
        newtonSolver.setAccuracy(Math.min(stepX, stepY) / 2);
        ArrayList<ColoredPoint> points = new ArrayList<>();
        ArrayList<Future> futures = new ArrayList<>();
        for (double x = a.real(); x <= b.real(); x += stepX) {
            double finalX = x;
            futures.add(executor.submit(() -> {
                ArrayList<ColoredPoint> result = new ArrayList<>();
                for (double y = a.imag(); y <= b.imag(); y += stepY) {
                    final ComplexDouble point = new ComplexDouble(finalX, y);
                    ComplexDouble temp = newtonSolver.apply(point);
                    if (temp != null) {
                        result.add(new ColoredPoint(point, findClosestRoot(temp)));
                    } else {
                        result.add(new ColoredPoint(point, 3));
                    }

                }
                synchronized (points) {
                    points.addAll(result);
                }
            }));
        }

        for (Future future : futures) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        return points;
    }

    /**
     * Returns path, which is created in {@link NewtonSolver::getPath}.
     *
     * @param p start point
     * @return list of points in the path
     */
    @Nonnull
    public List<ComplexDouble> solvePath(ComplexDouble p) {
        return newtonSolver.getPath(p);
    }

    class ColoredPoint {
        private final ComplexDouble point;
        private final int color;

        ColoredPoint(ComplexDouble point, int color) {
            this.point = point;
            this.color = color;
        }

        public ComplexDouble getPoint() {
            return point;
        }

        public int getColor() {
            return color;
        }
    }
}
