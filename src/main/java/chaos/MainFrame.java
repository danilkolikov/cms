package chaos;

import javax.swing.*;
import java.awt.*;

/**
 * Main frame for chaos task
 *
 * @author Danil Kolikov
 */
public class MainFrame extends JFrame {
    public MainFrame() throws HeadlessException {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(300, 300));
    }

    public static void main(String[] args) {
        MainFrame mainFrame = new MainFrame();
        mainFrame.setTitle("Chaos");
        mainFrame.setVisible(true);
    }
}
