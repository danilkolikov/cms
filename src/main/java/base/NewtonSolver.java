package base;

import com.sun.istack.internal.Nullable;
import org.jblas.ComplexDouble;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Novik Dmitry ITMO University
 */
public class NewtonSolver {

    private final InPlaceFunction f;
    private final InPlaceFunction df_dz;

    private double accuracy = 1e-4;
    private static final int MAX_ITERATIONS = 1000;

    public NewtonSolver(InPlaceFunction f, InPlaceFunction df_dz) {
        this.f = f;
        this.df_dz = df_dz;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    /**
     * Finds root for function {@code f}, using Newton method.
     *
     * @param point start point
     * @param next  Some temporary point
     * @param temp  Some temporary point
     * @return root point
     */
    @Nullable
    public ComplexDouble apply(ComplexDouble point, ComplexDouble next, ComplexDouble temp) {
        int iteration = 0;
        while (true) {
            if (iteration == MAX_ITERATIONS) {
                return null;
            }
            // next = f(point)
            f.apply(point, next);
            // temp = f'(point)
            df_dz.apply(point, temp);
            // next = -(f(point) / f'(point)) + previous
            next = next.divi(temp).negi().addi(point);
            // temp = (next - point)
            temp = temp.copy(next).subi(point);
            if (temp.abs() < accuracy) {
                return next;
            } else {
                point = point.copy(next);
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
        ComplexDouble previous = new ComplexDouble(complex.real(), complex.imag());
        ComplexDouble next = new ComplexDouble(0);
        ComplexDouble temp = new ComplexDouble(0);
        int iteration = 0;
        ArrayList<ComplexDouble> points = new ArrayList<>();
        while (true) {
            points.add(new ComplexDouble(previous.real(), previous.imag()));
            if (iteration == MAX_ITERATIONS) {
                return points;
            }
            // next = -(f(previous) / f'(previous)) + previous
            f.apply(previous, next);
            df_dz.apply(previous, temp);
            next = next.divi(temp).negi().addi(previous);
            temp = temp.copy(next).subi(previous);

            if (temp.abs() < accuracy) {
                points.add(new ComplexDouble(next.real(), next.imag()));
                return points;
            } else {
                previous = previous.copy(next);
            }
            ++iteration;
        }
    }
}
