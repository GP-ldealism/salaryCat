import java.io.File;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class AudioPlayer {
    private Clip clip;
    private boolean playing;

    public boolean load(File file) {
        try {
            AudioInputStream ais = AudioSystem.getAudioInputStream(file);
            clip = AudioSystem.getClip();
            clip.open(ais);
            return true;
        } catch (Exception e) {
            System.err.println("Audio load failed: " + e.getMessage());
            return false;
        }
    }

    public boolean canPlay() {
        return clip != null;
    }

    public void playLoop() {
        if (clip == null) return;
        clip.loop(Clip.LOOP_CONTINUOUSLY);
        playing = true;
    }

    public void stop() {
        if (clip == null) return;
        clip.stop();
        clip.flush();
        clip.setFramePosition(0);
        playing = false;
    }

    public void close() {
        stop();
        if (clip != null) clip.close();
    }
}
