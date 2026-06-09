import javax.swing.*;

public class SalaryCatApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}

            SalaryCatFrame frame = new SalaryCatFrame();
            frame.setVisible(true);
        });
    }
}
