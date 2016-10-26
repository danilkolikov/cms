package fractal;

import de.erichseifert.gral.data.DataTable;
import de.erichseifert.gral.plots.XYPlot;
import de.erichseifert.gral.plots.lines.DefaultLineRenderer2D;
import de.erichseifert.gral.plots.lines.LineRenderer;
import de.erichseifert.gral.plots.points.PointRenderer;
import de.erichseifert.gral.ui.InteractivePanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Main frame for fractal task
 *
 * @author Danil Kolikov
 */
public class MainFrame extends JFrame {
    public MainFrame() throws HeadlessException {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(600, 400));

        DataTable data = new DataTable(Double.class, Double.class);
        XYPlot plot = new XYPlot();
        LineRenderer lines = new DefaultLineRenderer2D();

        // count data to cos(x)
        for (double x = -5.0; x <= 5.0; x += 0.25) {
            double y = Math.cos(x);
            data.add(x, y);
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

    public static void main(String[] args) {
        MainFrame mainFrame = new MainFrame();
        mainFrame.setTitle("Fractals");
        mainFrame.setVisible(true);
    }
}
