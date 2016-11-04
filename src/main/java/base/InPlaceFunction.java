package base;

import org.jblas.ComplexDouble;

/**
 * Complex function that can be calculated in place
 */
@FunctionalInterface
public interface InPlaceFunction {
    void apply(ComplexDouble z, ComplexDouble result);
}
