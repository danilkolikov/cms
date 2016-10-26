package fractal;

import base.NewtonSolver;
import com.sun.javaws.exceptions.InvalidArgumentException;
import org.apache.commons.math3.complex.Complex;

import java.util.ArrayList;
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
     * This method finds root for function {@code f} for points in rectangle {@code [-a, a]x[-b, b]}.
     * Each parameter must be positive number.
     * @param a parameter for axis Ox
     * @param b parameter for axis Oy
     * @return list of colored points
     */
    public ArrayList<ColoredPoint> solve(double a, double b) throws InvalidArgumentException {
        if (Double.compare(a, 0.0) <= 0 || Double.compare(b, 0.0) <= 0) {
            throw new InvalidArgumentException(new String[]{"Non-positive arguments"});
        }

        ArrayList<ColoredPoint> points = new ArrayList<>();
        for (double x = -a; Double.compare(x, a) <= 0; x += step) {
            for (double y = -b; Double.compare(y, b) <= 0; y +=step) {
                final Complex point = new Complex(x, y);
                executor.submit(() -> {
                    Complex temp = newtonSolver.apply(point);
                    synchronized (points) {
                        points.add(new ColoredPoint(point, findClosestRoot(temp)));
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
