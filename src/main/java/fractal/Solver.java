package fractal;

import base.NewtonSolver;
import com.sun.javaws.exceptions.InvalidArgumentException;
import org.apache.commons.math3.complex.Complex;

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

    private static final double step = 0.0001;

    private NewtonSolver newtonSolver = new NewtonSolver(f, df_dz);

    public Solver() {
        newtonSolver.setAccuracy(step / 2);
    }

    /**
     * This method finds root for function {@code f} for points in rectangle {@code [-a, a]x[-b, b]}.
     * Each parameter must be positive number.
     *
     * @param a parameter for axis Ox
     * @param b parameter for axis Oy
     */
    public void solve(double a, double b) throws InvalidArgumentException {
        if (Double.compare(a, 0.0) <= 0 || Double.compare(b, 0.0) <= 0) {
            throw new InvalidArgumentException(new String[]{"Non-positive arguments"});
        }

        for (double x = -a; Double.compare(x, a) <= 0; x += step) {
            for (double y = -b; Double.compare(y, b) <= 0; y +=step) {
                newtonSolver.apply(new Complex(x, y));
            }
        }
    }
}
