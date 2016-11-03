package chaos;

import base.PlotUtils;
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

import javax.annotation.Nonnull;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Main frame for chaos task
 *
 * @author Danil Kolikov
 */
public class MainFrame extends JFrame {
    private static final double EPS = 1e-10;
    private static final int MAX_ITERATIONS = 10_000;
    private static final int POINTS_COUNT = 2000;

    private final Solver.AsyncSolver solver;
    private final XYPlot plot;
    private final InteractivePanel interactivePanel;
    private volatile long currentRedraw;    // For showing only last set of points

    public MainFrame() throws HeadlessException {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(600, 400));

        solver = new Solver.AsyncSolver();

        DataTable data = new DataTable(Double.class, Double.class);
        plot = new XYPlot();

        fillDataTable(data, -2, 4, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, ++currentRedraw);
        plot.add(data);

        // No need to scroll closer then EPS
        plot.getNavigator().setZoomMax(1 / EPS);

        // set colors
        Color color = new Color(0.0f, 0.0f, 1.0f);
        Color red = new Color(1.0f, 0.0f, 0.0f);
        for (PointRenderer pR : plot.getPointRenderers(data)) {
            pR.setColor(red);
        }
        for (LineRenderer lR : plot.getLineRenderers(data)) {
            lR.setColor(color);
        }

        plot.getAxis(XYPlot.AXIS_X).setAutoscaled(false);
        plot.getAxis(XYPlot.AXIS_Y).setAutoscaled(false);

        interactivePanel = new InteractivePanel(plot);
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
                Axis axisY = plot.getAxis(XYPlot.AXIS_Y);
                double left = axisX.getMin().doubleValue();
                double right = axisX.getMax().doubleValue();
                double top = axisY.getMax().doubleValue();
                double bottom = axisY.getMin().doubleValue();
                redrawPoints(left, right, bottom, top);

                System.out.println("Moved center: " + navigationEvent.getValueNew());
            }

            @Override
            public void zoomChanged(NavigationEvent<Double> navigationEvent) {
                Axis axisX = plot.getAxis(XYPlot.AXIS_X);
                Axis axisY = plot.getAxis(XYPlot.AXIS_Y);
                double left = axisX.getMin().doubleValue();
                double right = axisX.getMax().doubleValue();
                double bottom = axisY.getMin().doubleValue();
                double top = axisY.getMax().doubleValue();

                // Scale event comes before real scale of axis
                if (navigationEvent.getValueOld() > navigationEvent.getValueNew()) {
                    double scale = navigationEvent.getValueNew() / navigationEvent.getValueOld();
                    Pair<Double, Double> scaledX = scale(left, right, scale);
                    Pair<Double, Double> scaledY = scale(bottom, top, scale);
                    left = scaledX.getFirst();
                    right = scaledX.getSecond();
                    bottom = scaledY.getFirst();
                    top = scaledY.getSecond();
                }
                redrawPoints(left, right, bottom, top);

                System.out.println("Changed zoom: " + navigationEvent.getValueNew());
            }

            private void redrawPoints(double left, double right, double bottom, double top) {
                fillDataTable(data, left, right, bottom, top, ++currentRedraw);
                System.out.println("View: " + left + " " + right + " " + currentRedraw);
            }

            @Nonnull
            private Pair<Double, Double> scale(double left, double right, double scale) {
                double middle = (left + right) / 2;
                double length = (right - left) / scale;
                left = middle - length / 2;
                right = middle + length / 2;
                return new Pair<>(left, right);

            }
        });

        getContentPane().add(interactivePanel);
    }

    private void fillDataTable(DataTable data, double minX, double maxX, double minY, double maxY, long redraw) {
        final double finalMinX = Math.max(-2, minX);
        final double finalMaxX = Math.min(4, maxX);

        SwingWorker<List<Pair<Double, List<Double>>>, Void> worker = new SwingWorker<List<Pair<Double, List<Double>>>, Void>() {
            @Override
            protected List<Pair<Double, List<Double>>> doInBackground() throws Exception {
                return solver.solve(finalMinX, finalMaxX, POINTS_COUNT, EPS, MAX_ITERATIONS);
            }


            @Override
            protected void done() {
                if (currentRedraw != redraw) {
                    return;
                }
                try {
                    List<Pair<Double, List<Double>>> points = get();
                    List<Pair<Double, Double>> shown = new ArrayList<>();
                    data.clear();
                    for (Pair<Double, List<Double>> point : points) {
                        double r = point.getKey();
                        for (Double value : point.getValue()) {
                            if (minY < value && value < maxY) {
                                shown.add(new Pair<>(r, value));
                            }
                        }
                    }

                    // Use it for quickly place points on plot
                    PlotUtils.replaceData(shown, data, plot);
                    if (interactivePanel != null) {
                        interactivePanel.repaint();
                    }
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    public static void main(String[] args) {
        MainFrame mainFrame = new MainFrame();
        mainFrame.setTitle("Chaos");
        mainFrame.setVisible(true);
    }
}
