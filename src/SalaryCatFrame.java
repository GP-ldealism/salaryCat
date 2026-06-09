import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import javax.swing.*;

public class SalaryCatFrame extends JFrame {
    private static final Color BG_COLOR = new Color(0x1a, 0x1a, 0x2e);
    private static final Color TEXT_COLOR = new Color(0xff, 0x88, 0xaa);

    private final AudioPlayer audioPlayer = new AudioPlayer();
    private final List<BufferedImage> rawFrames = new ArrayList<>();
    private final List<Integer> delays = new ArrayList<>();
    private final List<BufferedImage> scaledFrames = new ArrayList<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "frame-timer");
        t.setDaemon(true);
        return t;
    });

    private GifPanel gifPanel;
    private int displayW = -1;
    private int displayH = -1;
    private volatile int currentFrameIndex;
    private volatile boolean running;

    public SalaryCatFrame() {
        initFrame();
        loadGif();
        initContent();
        loadAudio();
        start();
    }

    private void initFrame() {
        setTitle("Salary Cat");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setBackground(BG_COLOR);
    }

    private void loadGif() {
        File gifFile = findAsset("cat.GIF");
        if (!gifFile.exists()) {
            System.err.println("cat.GIF not found at: " + gifFile.getAbsolutePath());
            return;
        }

        try (ImageInputStream iis = ImageIO.createImageInputStream(gifFile)) {
            ImageReader reader = ImageIO.getImageReadersByFormatName("gif").next();
            reader.setInput(iis, false);

            int count = reader.getNumImages(true);
            for (int i = 0; i < count; i++) {
                rawFrames.add(reader.read(i));
                int delay = 100;
                try {
                    IIOMetadata meta = reader.getImageMetadata(i);
                    if ("javax_imageio_gif_image_1.0".equals(meta.getNativeMetadataFormatName())) {
                        IIOMetadataNode root = (IIOMetadataNode) meta.getAsTree(meta.getNativeMetadataFormatName());
                        IIOMetadataNode gce = getChildNode(root, "GraphicControlExtension");
                        if (gce != null) {
                            delay = Integer.parseInt(gce.getAttribute("delayTime")) * 10;
                        }
                    }
                } catch (Exception ignored) {}
                delays.add(Math.max(delay, 20));
            }
            reader.dispose();
            System.out.println("GIF loaded: " + count + " frames");
        } catch (Exception e) {
            System.err.println("GIF load failed: " + e.getMessage());
        }
    }

    private IIOMetadataNode getChildNode(IIOMetadataNode node, String name) {
        for (int i = 0; i < node.getChildNodes().getLength(); i++) {
            if (node.getChildNodes().item(i).getNodeName().equals(name)) {
                return (IIOMetadataNode) node.getChildNodes().item(i);
            }
        }
        return null;
    }

    private void initContent() {
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(BG_COLOR);

        gifPanel = new GifPanel();
        contentPanel.add(gifPanel, BorderLayout.CENTER);

        JLabel footerLabel = new JLabel("我真的特别爱你", SwingConstants.CENTER);
        footerLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 28));
        footerLabel.setForeground(TEXT_COLOR);
        footerLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 40, 0));
        contentPanel.add(footerLabel, BorderLayout.SOUTH);

        setContentPane(contentPanel);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                recacheScaledFrames();
            }
        });
    }

    private void recacheScaledFrames() {
        if (rawFrames.isEmpty()) return;
        int w = gifPanel.getWidth();
        int h = gifPanel.getHeight();
        if (w <= 0 || h <= 0) return;

        BufferedImage sample = rawFrames.get(0);
        int imgW = sample.getWidth();
        int imgH = sample.getHeight();

        float scale = Math.min((float) w / imgW, (float) h / imgH);
        scale = Math.min(scale, 1.8f);

        int newW = Math.round(imgW * scale);
        int newH = Math.round(imgH * scale);
        if (newW == displayW && newH == displayH) return;

        displayW = newW;
        displayH = newH;
        scaledFrames.clear();

        if (newW == imgW && newH == imgH) {
            scaledFrames.addAll(rawFrames);
        } else {
            for (BufferedImage frame : rawFrames) {
                BufferedImage scaled = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = scaled.createGraphics();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                g2.drawImage(frame, 0, 0, newW, newH, null);
                g2.dispose();
                scaledFrames.add(scaled);
            }
        }
    }

    private void loadAudio() {
        File wavFile = findAsset("music.wav");
        if (wavFile.exists() && audioPlayer.load(wavFile)) {
            System.out.println("Audio loaded: " + wavFile.getName());
        }
    }

    private File findAsset(String name) {
        File local = new File(name);
        if (local.exists()) return local;
        try {
            var location = getClass().getProtectionDomain().getCodeSource().getLocation();
            if (location != null) {
                File parent = new File(location.toURI()).getParentFile();
                if (parent != null) {
                    File alongside = new File(parent, name);
                    if (alongside.exists()) return alongside;
                }
            }
        } catch (Exception ignored) {}
        return local;
    }

    private void start() {
        if (audioPlayer.canPlay()) {
            audioPlayer.playLoop();
        }

        if (rawFrames.isEmpty()) return;

        running = true;
        currentFrameIndex = 0;
        scheduleNextFrame();
    }

    private void scheduleNextFrame() {
        if (!running || rawFrames.isEmpty()) return;
        int delay = delays.get(currentFrameIndex);
        scheduler.schedule(() -> {
            if (!running) return;
            currentFrameIndex = (currentFrameIndex + 1) % rawFrames.size();
            SwingUtilities.invokeLater(() -> gifPanel.repaint());
            scheduleNextFrame();
        }, delay, TimeUnit.MILLISECONDS);
    }

    @Override
    public void dispose() {
        running = false;
        scheduler.shutdownNow();
        audioPlayer.close();
        super.dispose();
    }

    private class GifPanel extends JPanel {
        GifPanel() {
            setBackground(BG_COLOR);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            int idx = currentFrameIndex;
            List<BufferedImage> source = scaledFrames.isEmpty() ? rawFrames : scaledFrames;

            if (source.isEmpty()) {
                g.setColor(TEXT_COLOR);
                g.setFont(new Font("Dialog", Font.PLAIN, 120));
                FontMetrics fm = g.getFontMetrics();
                String emoji = "🐱";
                g.drawString(emoji,
                        (getWidth() - fm.stringWidth(emoji)) / 2,
                        (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
                return;
            }

            BufferedImage frame = source.get(idx % source.size());
            int dw = displayW > 0 ? displayW : frame.getWidth();
            int dh = displayH > 0 ? displayH : frame.getHeight();
            int x = (getWidth() - dw) / 2;
            int y = (getHeight() - dh) / 2;

            g.drawImage(frame, x, y, dw, dh, null);
        }
    }
}
