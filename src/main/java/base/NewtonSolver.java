package base;

import com.sun.istack.internal.Nullable;
import org.apache.commons.math3.complex.Complex;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * @author Novik Dmitry ITMO University
 */
public class NewtonSolver implements Function<Complex, Complex> {

    private final Function<Complex, Complex> f;
    private final Function<Complex, Complex> df_dz;

    private double accuracy = 1e-4;
    private static final int MAX_ITERATIONS = 1000;

    public NewtonSolver(Function<Complex, Complex> f, Function<Complex, Complex> df_dz) {
        this.f = f;
        this.df_dz = df_dz;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    @Override
    @Nullable
    public Complex apply(Complex complex) {
        Complex previous = complex;
        int iteration = 0;
        while (true) {
            if (iteration == MAX_ITERATIONS) {
                return null;
            }
            Complex next = previous.subtract(f.apply(previous).divide(df_dz.apply(previous)));
            if (next.subtract(previous).abs() < accuracy) {
                return next;
            } else {
                previous = next;
            }
            ++iteration;
        }
    }

    public List<Complex> getPath(Complex complex) {
        Complex previous = complex;
        int iteration = 0;
        ArrayList<Complex> points = new ArrayList<>();
        while (true) {
            points.add(previous);
            if (iteration == MAX_ITERATIONS) {
                return points;
            }
            Complex next = previous.subtract(f.apply(previous).divide(df_dz.apply(previous)));
            if (next.subtract(previous).abs() < accuracy) {
                points.add(next);
                return points;
            } else {
                previous = next;
            }
            ++iteration;
        }
    }
}
