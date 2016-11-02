package chaos;

import de.erichseifert.gral.data.DataTable;
import de.erichseifert.gral.plots.XYPlot;
import de.erichseifert.gral.plots.lines.DefaultLineRenderer2D;
import de.erichseifert.gral.plots.lines.LineRenderer;
import de.erichseifert.gral.plots.points.PointRenderer;
import de.erichseifert.gral.ui.InteractivePanel;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ConvergeSeriesFrame extends JFrame {
    public ConvergeSeriesFrame(@Nonnull List<Double> series) throws HeadlessException {
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(600, 400));

        DataTable data = new DataTable(Double.class, Double.class);

        LineRenderer lines = new DefaultLineRenderer2D();
        XYPlot plot = new XYPlot();

        for (int i = 0; i < series.size(); i++) {
            data.add((double) i, series.get(i));
        }
        plot.add(data);

        // set lines
        plot.setLineRenderers(data, lines);

        // set colors
        Color color = new Color(0.0f, 0.0f, 1.0f);
        Color red = new Color(1.0f, 0.0f, 0.0f);
        for (PointRenderer pR : plot.getPointRenderers(data)) {
            pR.setColor(red);
        }
        for (LineRenderer lR : plot.getLineRenderers(data)) {
            lR.setColor(color);
        }

        InteractivePanel interactivePanel = new InteractivePanel(plot);
        getContentPane().add(interactivePanel);
    }
}
