package fractal;

import base.PlotUtils;
import com.sun.javaws.exceptions.InvalidArgumentException;
import de.erichseifert.gral.data.DataTable;
import de.erichseifert.gral.navigation.NavigationEvent;
import de.erichseifert.gral.navigation.NavigationListener;
import de.erichseifert.gral.plots.XYPlot;
import de.erichseifert.gral.plots.axes.Axis;
import de.erichseifert.gral.plots.lines.DefaultLineRenderer2D;
import de.erichseifert.gral.plots.lines.LineRenderer;
import de.erichseifert.gral.plots.points.PointRenderer;
import de.erichseifert.gral.ui.InteractivePanel;

import de.erichseifert.gral.util.PointND;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.util.Pair;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Main frame for fractal task
 *
 * @author Danil Kolikov
 */
public class MainFrame extends JFrame {
    private static final Shape circle = new Ellipse2D.Double(-2.0, -2.0, 4.0, 4.0);

    private List<DataTable> pointsData = new ArrayList<>(4);
    private DataTable pathData = new DataTable(Double.class, Double.class);
    private XYPlot plot = new XYPlot();
    private InteractivePanel interactivePanel;
    private LineRenderer lineRenderer = new DefaultLineRenderer2D();

    private Solver solver = new Solver();

    private void drawCircle() {
        DataTable circleData = new DataTable(Double.class, Double.class);
        for (int i = 0; i <= 720; i++) {
            double x = Math.cos(i * Math.PI / 360);
            double y = Math.sin(i * Math.PI / 360);
            circleData.add(x, y);
        }
        plot.add(circleData);
        plot.setLineRenderers(circleData, lineRenderer);
        for (PointRenderer pR : plot.getPointRenderers(circleData)) {
            Shape circle = new Ellipse2D.Double(-1.0, -1.0, 2.0, 2.0);
            pR.setShape(circle);
        }
    }

    private void drawPoints(Complex leftBottomPoint, Complex rightTopPoint) {
        SwingWorker<List<Solver.ColoredPoint>, Void> worker = new SwingWorker<List<Solver.ColoredPoint>, Void>() {
            @Override
            protected List<Solver.ColoredPoint> doInBackground() throws Exception {
                return solver.solve(leftBottomPoint, rightTopPoint);
            }

            @Override
            protected void done() {
                try {
                    List<Solver.ColoredPoint> points = get();
                    List<Pair<Double, Double>>[] shown = new List[4];
                    for (int i = 0; i < 4; i++) {
                        shown[i] = new ArrayList<>();
                    }
                    for (Solver.ColoredPoint coloredPoint : points) {
                        Complex point = coloredPoint.getPoint();
                        int color = coloredPoint.getColor();
                        shown[color].add(new Pair<>(point.getReal(), point.getImaginary()));
                    }
                    for (int i = 0; i < 4; i++) {
                        PlotUtils.replaceData(shown[i], pointsData.get(i), plot);
                    }
                    colorPoints();
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

    private void colorPoints() {
        for (int color = 0; color < 4; color++) {
            for (PointRenderer pR : plot.getPointRenderers(pointsData.get(color))) {
                pR.setShape(circle);
                switch (color) {
                    case 0:
                        pR.setColor(Color.RED);
                        break;
                    case 1:
                        pR.setColor(Color.GREEN);
                        break;
                    case 2:
                        pR.setColor(Color.BLUE);
                        break;
                    case 3:
                        //TODO: Black is never printed! May be we should increase number of iterations?
                        pR.setColor(Color.BLACK);
                        break;
                }
            }
        }
    }

    private void drawPath(Complex startPoint) {
        plot.remove(pathData);
        pathData.clear();
        for (Complex point : solver.solvePath(startPoint)) {
            pathData.add(point.getReal(), point.getImaginary());
        }
        plot.add(pathData);
        plot.setLineRenderers(pathData, lineRenderer);
    }

    private MainFrame() throws HeadlessException {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(700, 700));
        for (int i = 0; i < 4; i++) {
            pointsData.add(new DataTable(Double.class, Double.class));
            plot.add(pointsData.get(i));
        }

        plot.getNavigator().setZoomMax(Double.POSITIVE_INFINITY);
        plot.getNavigator().setZoomMin(Double.NEGATIVE_INFINITY);
        plot.getAxis(XYPlot.AXIS_X).setAutoscaled(false);
        plot.getAxis(XYPlot.AXIS_Y).setAutoscaled(false);
        plot.getNavigator().addNavigationListener(new NavigationListener() {
            @Override
            public void centerChanged(NavigationEvent<PointND<? extends Number>> navigationEvent) {
                System.out.println("Center changed");
                Axis axisX = plot.getAxis(XYPlot.AXIS_X);
                Axis axisY = plot.getAxis(XYPlot.AXIS_Y);
                Complex leftBottomPoint = new Complex(axisX.getMin().doubleValue(), axisY.getMin().doubleValue());
                Complex rightTopPoint = new Complex(axisX.getMax().doubleValue(), axisY.getMax().doubleValue());
                drawPoints(leftBottomPoint, rightTopPoint);
            }

            @Override
            public void zoomChanged(NavigationEvent<Double> navigationEvent) {
                Axis axisX = plot.getAxis(XYPlot.AXIS_X);
                Axis axisY = plot.getAxis(XYPlot.AXIS_Y);
                double multiplier = navigationEvent.getValueOld() / navigationEvent.getValueNew();
                double partX = (axisX.getMax().doubleValue() - axisX.getMin().doubleValue()) / 2;
                double partY = (axisY.getMax().doubleValue() - axisY.getMin().doubleValue()) / 2;
                Complex leftBottomPoint = new Complex(axisX.getMin().doubleValue() + partX - partX * multiplier, axisY.getMin().doubleValue() + partY - partY * multiplier);
                Complex rightTopPoint = new Complex(axisX.getMin().doubleValue() + partX + partX * multiplier, axisY.getMin().doubleValue() + partY + partY * multiplier);
                drawPoints(leftBottomPoint, rightTopPoint);
                System.out.println("Changed zoom: " + navigationEvent.getValueNew());
            }
        });

        interactivePanel = new InteractivePanel(plot);
        interactivePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                Axis axisX = plot.getAxis(XYPlot.AXIS_X);
                Axis axisY = plot.getAxis(XYPlot.AXIS_Y);
                Number numberX = plot.getAxisRenderer(XYPlot.AXIS_X).viewToWorld(axisX, e.getX(), true);
                Number numberY = plot.getAxisRenderer(XYPlot.AXIS_Y).viewToWorld(axisY, e.getY(), true);
                double X = numberX.doubleValue();
                double Y = -numberY.doubleValue();
                drawPath(new Complex(X, Y));
                getContentPane().repaint();
            }
        });

        getContentPane().add(interactivePanel);
    }

    public static void main(String[] args) {
        MainFrame mainFrame = new MainFrame();
        mainFrame.setTitle("Fractals");

        double squareSide = 5.0f;
        mainFrame.drawPoints(new Complex(-squareSide, -squareSide), new Complex(squareSide, squareSide));
        mainFrame.drawCircle();

        mainFrame.setVisible(true);
        // We should set range manually because auto scaling turned off
        mainFrame.plot.getAxis(XYPlot.AXIS_X).setRange(-squareSide, squareSide);
        mainFrame.plot.getAxis(XYPlot.AXIS_Y).setRange(-squareSide, squareSide);
    }
}