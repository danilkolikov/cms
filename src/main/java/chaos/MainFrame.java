package chaos;

import de.erichseifert.gral.data.DataTable;
import de.erichseifert.gral.plots.XYPlot;
import de.erichseifert.gral.plots.axes.Axis;
import de.erichseifert.gral.plots.lines.LineRenderer;
import de.erichseifert.gral.plots.points.PointRenderer;
import de.erichseifert.gral.ui.InteractivePanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Main frame for chaos task
 *
 * @author Danil Kolikov
 */
public class MainFrame extends JFrame {
    public MainFrame() throws HeadlessException {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(600, 400));

        DataTable data = new DataTable(Double.class, Double.class);
        XYPlot plot = new XYPlot();

        final double eps = 0.001;
        int maxIterations = 1_000;
        for (double r = 0.0; r < 5; r += 0.01) {
            List<Double> roots = Solver.findRoots(r, eps, maxIterations);
            for (Double root : roots) {
                data.add(r, root);
            }
        }
        plot.add(data);

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
        interactivePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                Axis axisX = plot.getAxis(XYPlot.AXIS_X);
                Number numberX = plot.getAxisRenderer(XYPlot.AXIS_X).viewToWorld(axisX, e.getX(), true);
                double X = numberX.doubleValue();
                System.out.println(X);
                List<Double> result = Solver.findConvergeSeries(X, eps, maxIterations);
                // TODO: show points from result list
            }
        });
        getContentPane().add(interactivePanel);
    }

    public static void main(String[] args) {
        MainFrame mainFrame = new MainFrame();
        mainFrame.setTitle("Chaos");
        mainFrame.setVisible(true);
    }
}
