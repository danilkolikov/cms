package base;

import com.sun.istack.internal.Nullable;
import org.jblas.ComplexDouble;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * @author Novik Dmitry ITMO University
 */
public class NewtonSolver implements Function<ComplexDouble, ComplexDouble> {

    private final Function<ComplexDouble, ComplexDouble> f;
    private final Function<ComplexDouble, ComplexDouble> df_dz;

    private double accuracy = 1e-4;
    private static final int MAX_ITERATIONS = 1000;

    public NewtonSolver(Function<ComplexDouble, ComplexDouble> f, Function<ComplexDouble, ComplexDouble> df_dz) {
        this.f = f;
        this.df_dz = df_dz;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    /**
     * Finds root for function {@code f}, using Newton method.
     *
     * @param complex start point
     * @return root point
     */
    @Override
    @Nullable
    public ComplexDouble apply(ComplexDouble complex) {
        ComplexDouble previous = complex;
        int iteration = 0;
        while (true) {
            if (iteration == MAX_ITERATIONS) {
                return null;
            }
            // next = -(f(previous) / f'(previous)) + previous
            ComplexDouble next = f.apply(previous).divi(df_dz.apply(previous)).negi().addi(previous);
            if (next.sub(previous).abs() < accuracy) {
                return next;
            } else {
                previous = next;
            }
            ++iteration;
        }
    }

    /**
     * Returns points from Newton method's iterations.
     *
     * @param complex start point
     * @return list of points
     */
    @Nonnull
    public List<ComplexDouble> getPath(ComplexDouble complex) {
        ComplexDouble previous = complex;
        int iteration = 0;
        ArrayList<ComplexDouble> points = new ArrayList<>();
        while (true) {
            points.add(previous);
            if (iteration == MAX_ITERATIONS) {
                return points;
            }
            // next = -(f(previous) / f'(previous)) + previous
            ComplexDouble next = f.apply(previous).divi(df_dz.apply(previous)).negi().addi(previous);
            if (next.sub(previous).abs() < accuracy) {
                points.add(next);
                return points;
            } else {
                previous = next;
            }
            ++iteration;
        }
    }
}
