package chaos;

import de.erichseifert.gral.data.DataTable;
import de.erichseifert.gral.navigation.NavigationEvent;
import de.erichseifert.gral.navigation.NavigationListener;
import de.erichseifert.gral.plots.XYPlot;
import de.erichseifert.gral.plots.axes.Axis;
import de.erichseifert.gral.plots.lines.LineRenderer;
import de.erichseifert.gral.plots.points.PointRenderer;
import de.erichseifert.gral.ui.InteractivePanel;
import de.erichseifert.gral.util.PointND;
import org.apache.commons.math3.util.Pair;

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
    private static final double EPS = 1e-8;
    private static final int MAX_ITERATIONS = 10_000;
    private static final int POINTS_COUNT = 200;

    private final Solver.AsyncSolver solver;

    public MainFrame() throws HeadlessException {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(600, 400));

        solver = new Solver.AsyncSolver();

        DataTable data = new DataTable(Double.class, Double.class);
        XYPlot plot = new XYPlot();

        fillDataTable(data, -2, 4);
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
                List<Double> result = Solver.findConvergeSeries(X, EPS, MAX_ITERATIONS);
                ConvergeSeriesFrame seriesFrame = new ConvergeSeriesFrame(result);
                seriesFrame.setTitle("Series");
                seriesFrame.setVisible(true);
            }
        });

        plot.getNavigator().addNavigationListener(new NavigationListener() {
            @Override
            public void centerChanged(NavigationEvent<PointND<? extends Number>> navigationEvent) {
                Axis axisX = plot.getAxis(XYPlot.AXIS_X);
                double left = axisX.getMin().doubleValue();
                double right = axisX.getMax().doubleValue();
                redrawPoints(left, right);

                System.out.println("Moved center: " + navigationEvent.getValueNew());
            }

            @Override
            public void zoomChanged(NavigationEvent<Double> navigationEvent) {
                Axis axisX = plot.getAxis(XYPlot.AXIS_X);
                double left = axisX.getMin().doubleValue();
                double right = axisX.getMax().doubleValue();
                if (navigationEvent.getValueOld() > navigationEvent.getValueNew()) {
                    double scale = navigationEvent.getValueNew() / navigationEvent.getValueOld();
                    double middle = (left + right) / 2;
                    double length = (right - left) / scale;
                    left = middle - length / 2;
                    right = middle + length / 2;
                }
                redrawPoints(left, right);

                System.out.println("Changed zoom: " + navigationEvent.getValueNew());
            }

            private void redrawPoints(double left, double right) {
                System.out.println("View: " + left + " " + right);

                data.clear();
                fillDataTable(data, left, right);
            }
        });

        getContentPane().add(interactivePanel);
    }

    private void fillDataTable(DataTable data, double minX, double maxX) {
        data.clear();
        minX = Math.max(-2, minX);
        maxX = Math.min(4, maxX);
        List<Pair<Double, List<Double>>> points = solver.solve(minX, maxX, POINTS_COUNT, EPS, MAX_ITERATIONS);
        for (Pair<Double, List<Double>> point : points) {
            double r = point.getKey();
            for (Double value : point.getValue()) {
                data.add(r, value);
            }
        }
    }

    public static void main(String[] args) {
        MainFrame mainFrame = new MainFrame();
        mainFrame.setTitle("Chaos");
        mainFrame.setVisible(true);
    }
}
