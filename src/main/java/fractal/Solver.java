package fractal;

import base.NewtonSolver;
import com.sun.javaws.exceptions.InvalidArgumentException;
import org.apache.commons.math3.complex.Complex;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.*;
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
    private static Function<Complex, Complex> f = (z) -> z.pow(3).subtract(Complex.ONE);
    /**
     * Derivative of {@code f}.
     */
    private static Function<Complex, Complex> df_dz = (z) -> z.pow(2).multiply(3);

    private static final Complex[] roots = {
            new Complex(1, 0),
            new Complex(Math.cos(2 * Math.PI / 3), Math.sin(2 * Math.PI / 3)),
            new Complex(Math.cos(4 * Math.PI / 3), Math.sin(4 * Math.PI / 3))};

    private static final int pointsPerAxis = 100;

    private ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private NewtonSolver newtonSolver = new NewtonSolver(f, df_dz);

    private int findClosestRoot(Complex point) {
        TreeMap<Double, Integer> map = new TreeMap<>();
        for (int i = 0; i < roots.length; ++i) {
            map.put(roots[i].subtract(point).abs(), i);
        }
        return map.firstEntry().getValue();
    }

    /**
     * This method finds root for function {@code f} for points in rectangle {@code [a.real, b.real]x[a.imaginary, b.imaginary]}.
     * Parameter {@code a} must be less or equal to parameter {@code b}.
     *
     * @param a the most left and down point of the rectangle
     * @param b the most right and up point of the rectangle
     * @return list of colored points
     */
    public List<ColoredPoint> solve(Complex a, Complex b) throws InvalidArgumentException {
        if (a.getReal() > b.getReal() || (a.getReal() == b.getReal() && a.getImaginary() > b.getImaginary())) {
            throw new InvalidArgumentException(new String[]{"Input points are not in lexicographical order"});
        }

        double stepX = Math.abs(a.getReal() - b.getReal()) / pointsPerAxis;
        double stepY = Math.abs(a.getImaginary() - b.getImaginary()) / pointsPerAxis;
        newtonSolver.setAccuracy(Math.min(stepX, stepY) / 2);
        ArrayList<ColoredPoint> points = new ArrayList<>();
        ArrayList<Future> futures = new ArrayList<>();
        for (double x = a.getReal(); x <= b.getReal(); x += stepX) {
            double finalX = x;
            futures.add(executor.submit(() -> {
                ArrayList<ColoredPoint> result = new ArrayList<ColoredPoint>();
                for (double y = a.getImaginary(); y <= b.getImaginary(); y += stepY) {
                    final Complex point = new Complex(finalX, y);
                    Complex temp = newtonSolver.apply(point);
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
    public List<Complex> solvePath(Complex p) {
        return newtonSolver.getPath(p);
    }

    class ColoredPoint {
        private final Complex point;
        private final int color;

        ColoredPoint(Complex point, int color) {
            this.point = point;
            this.color = color;
        }

        public Complex getPoint() {
            return point;
        }

        public int getColor() {
            return color;
        }
    }
}
