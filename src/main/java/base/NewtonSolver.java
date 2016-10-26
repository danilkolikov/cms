package base;

import org.apache.commons.math3.complex.Complex;

import java.util.function.Function;

/**
 * @author Novik Dmitry ITMO University
 */
public class NewtonSolver implements Function<Complex, Complex> {

    private final Function<Complex, Complex> f;
    private final Function<Complex, Complex> df_dz;

    private double accuracy = 1e-4;

    public NewtonSolver(Function<Complex, Complex> _f, Function<Complex, Complex> _df_dz) {
        f = _f;
        df_dz = _df_dz;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    @Override
    public Complex apply(Complex complex) {
        Complex previous = complex;
        while (true) {
            Complex next = previous.subtract(f.apply(previous).divide(df_dz.apply(previous)));
            if (next.subtract(previous).abs() < accuracy) {
                return next;
            } else {
                previous = next;
            }
        }
    }
}
