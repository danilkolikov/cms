package base;

import de.erichseifert.gral.data.DataTable;
import de.erichseifert.gral.plots.XYPlot;
import org.apache.commons.math3.util.Pair;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Utils for working with plot
 */
public class PlotUtils {
    @Nonnull
    private static final Field pointsField;

    static {
        try {
            pointsField = DataTable.class.getDeclaredField("a");
            pointsField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            // Exit
            throw new RuntimeException(e);
        }
    }

    /**
     * Replace all points on plot
     *
     * @param points    Points to place
     * @param dataTable Old dataset
     * @param plot      Plot
     */
    public static void replaceData(
            @Nonnull List<Pair<Double, Double>> points,
            @Nonnull DataTable dataTable,
            @Nonnull XYPlot plot
    ) {

        List<Comparable<?>[]> newList = new ArrayList<>(points.size());
        for (Pair<Double, Double> point : points) {
            Comparable<?>[] array = new Comparable[2];
            array[0] = point.getFirst();
            array[1] = point.getSecond();
            newList.add(array);
        }
        try {
            //noinspection unchecked It has this type
            List<Comparable<?>[]> stored = (List<Comparable<?>[]>) pointsField.get(dataTable);
            stored.clear();
            stored.addAll(newList);
            plot.dataUpdated(dataTable);

        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
