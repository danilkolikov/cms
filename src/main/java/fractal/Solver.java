package fractal;

import base.NewtonSolver;
import com.sun.javaws.exceptions.InvalidArgumentException;
import org.apache.commons.math3.complex.Complex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
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

    private static final double step = 0.0001;

    private ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private NewtonSolver newtonSolver = new NewtonSolver(f, df_dz);


    public Solver() {
        newtonSolver.setAccuracy(step / 2);
    }

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
        ArrayList<ColoredPoint> points = new ArrayList<>();
        for (double x = a.getReal(); x <= b.getReal(); x += step) {
            for (double y = a.getImaginary(); y <= b.getImaginary(); y +=step) {
                final Complex point = new Complex(x, y);
                executor.submit(() -> {
                    Complex temp = newtonSolver.apply(point);
                    synchronized (points) {
                        if (temp != null) {
                            points.add(new ColoredPoint(point, findClosestRoot(temp)));
                        } else {
                            points.add(new ColoredPoint(point, 4));
                        }
                    }
                });
            }
        }

        while (!executor.isTerminated()) {
            try {
                executor.awaitTermination(100, TimeUnit.NANOSECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return points;
    }

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
