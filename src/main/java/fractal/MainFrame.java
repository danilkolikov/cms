package fractal;

import com.sun.javaws.exceptions.InvalidArgumentException;
import de.erichseifert.gral.data.DataTable;
import de.erichseifert.gral.plots.XYPlot;
import de.erichseifert.gral.plots.lines.DefaultLineRenderer2D;
import de.erichseifert.gral.plots.lines.LineRenderer;
import de.erichseifert.gral.plots.points.PointData;
import de.erichseifert.gral.plots.points.PointRenderer;
import de.erichseifert.gral.ui.InteractivePanel;

import org.apache.commons.math3.complex.Complex;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.*;
import java.util.List;

/**
 * Main frame for fractal task
 *
 * @author Danil Kolikov
 */
public class MainFrame extends JFrame {
    private static final Shape circle = new Ellipse2D.Double(-4.0, -4.0, 8.0, 8.0);

    List<DataTable> pointsData = new ArrayList<>(4);
    private XYPlot plot = new XYPlot();
    private InteractivePanel interactivePanel;

    LineRenderer lineRenderer = new DefaultLineRenderer2D();

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
            Shape circle = new Ellipse2D.Double(-4.0, -4.0, 8.0, 8.0);
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

    public MainFrame() throws HeadlessException {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(600, 600));

        drawCircle();
        for (int i = 0; i < 4; i++) {
            pointsData.add(new DataTable(Double.class, Double.class));
            plot.add(pointsData.get(i));
        }

        interactivePanel = new InteractivePanel(plot);
        getContentPane().add(interactivePanel);
    }

    public static void main(String[] args) {
        MainFrame mainFrame = new MainFrame();
        mainFrame.setTitle("Fractals");
        Solver solver = new Solver();
        try {
            List<Solver.ColoredPoint> answer = solver.solve(new Complex(-2.0f, -2.0f), new Complex(2.0f, 2.0f));
            for (Solver.ColoredPoint point : answer) {
                mainFrame.drawPoint(point);
            }
        } catch (InvalidArgumentException e) {
            System.out.println("Очень жаль " + e.getRealMessage());
        }

        mainFrame.setVisible(true);
    }
}
