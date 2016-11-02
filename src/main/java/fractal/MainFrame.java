package fractal;

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

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.util.*;
import java.util.List;

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

    private void drawPoint(Solver.ColoredPoint coloredPoint) {
        Complex point = coloredPoint.getPoint();
        int color = coloredPoint.getColor();
        pointsData.get(color).add(point.getReal(), point.getImaginary());
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

    private void drawPath(Complex startPoint) {
        // clean previous path
        plot.remove(pathData);
        pathData.clear();

        // count new data
        for (Complex point : solver.solvePath(startPoint)) {
            pathData.add(point.getReal(), point.getImaginary());
        }

        // add new path to plot
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

        plot.getNavigator().addNavigationListener(new NavigationListener() {
            @Override
            public void centerChanged(NavigationEvent<PointND<? extends Number>> navigationEvent) {

            }

            @Override
            public void zoomChanged(NavigationEvent<Double> navigationEvent) {
                for (DataTable dataTable : pointsData) {
                    plot.remove(dataTable);
                    dataTable.clear();
                }

                plot.remove(pathData);
                Axis axisX = plot.getAxis(XYPlot.AXIS_X);
                Axis axisY = plot.getAxis(XYPlot.AXIS_Y);
                Complex leftBottomPoint = new Complex(axisX.getMin().doubleValue(), axisY.getMin().doubleValue());
                Complex rightTopPoint = new Complex(axisX.getMax().doubleValue(), axisY.getMax().doubleValue());
                System.out.print("Points: " + leftBottomPoint.getReal() + " " + leftBottomPoint.getImaginary() + "; " + rightTopPoint.getReal() + " " + rightTopPoint.getImaginary());
                for (DataTable dataTable : pointsData) {
                    plot.add(dataTable);
                }
                // TODO: calculate new coordinates
                try {
                    List<Solver.ColoredPoint> newData = solver.solve(leftBottomPoint, rightTopPoint);
                    for (Solver.ColoredPoint point : newData) {
                        drawPoint(point);
                    }
                    // TODO: points don't have color
                } catch (InvalidArgumentException e) {
                    e.printStackTrace();
                }

                System.out.println("Changed zoom: " + navigationEvent.getValueNew());
            }
        });

        InteractivePanel interactivePanel = new InteractivePanel(plot);
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
        try {
            List<Solver.ColoredPoint> answer = mainFrame.solver.solve(new Complex(-squareSide, -squareSide), new Complex(squareSide, squareSide));
            for (Solver.ColoredPoint point : answer) {
                mainFrame.drawPoint(point);
            }
        } catch (InvalidArgumentException e) {
            System.out.println("Очень жаль " + e.getRealMessage());
        }
        mainFrame.drawCircle();

        mainFrame.setVisible(true);
    }
}