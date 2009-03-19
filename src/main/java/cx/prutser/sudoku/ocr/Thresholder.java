package cx.prutser.sudoku.ocr;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This commandline tool is used to apply the adaptive thresholding algortihm
 * to a photo.
 *
 * @author  Erik van Zijst
 */
public class Thresholder {

    private int size = 12;
    private int constant = 7;
    private String filename = null; // null for stdin

    private class GUI extends JFrame {
        private final BufferedImage image;

        public GUI(BufferedImage _image) throws HeadlessException {
            GridBagLayout layout = new GridBagLayout();
            setLayout(layout);
            image = _image;
            JLabel label = new JLabel(new ImageIcon(image));
            add(label);
            pack();
            setTitle((filename == null ? "stdin" : filename) +
                    String.format(" - %dx%d, s=%s, c=%d", image.getWidth(), image.getHeight(),
                            size, constant));

            addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    System.exit(0);
                }
            });
            setVisible(true);
        }
    }

    private Thresholder(String... args) {
        parseArgs(args);
    }

    private void run() {

        InputStream in = null;
        try {
            in = filename == null ? System.in : new FileInputStream(filename);
            final BufferedImage source = ImageIO.read(in);

            new GUI(OCRUtils.threshold(source, BufferedImage.TYPE_INT_RGB, size, constant));

        } catch (IOException ioe) {
            System.err.println("Error reading " + filename + ": " + ioe.getMessage());
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch(IOException e) {}
        }
    }

    public static void main(String... args) {
        new Thresholder(args).run();
    }

    private void parseArgs(String... args) {

        final String usage = "Usage: java " + getClass().getName() + " [OPTIONS]\n" +
                "\n" +
                "Applies adaptive thresholding to an image and displays the result.\n" +
                "\n" +
                "OPTIONS\n" +
                "   -s, --size  the size of the neigbourhood used in finding the threshold\n" +
                "               (default 12)\n" +
                "   -c, --const the constant value subtracted from the mean (default 7)\n" +
                "   -f, --file  the image file to read (stdin when omitted)\n" +
                "   -h, --help  print this help message and exit.";

        boolean exit = false;
        try {
            for (int i = 0; !exit && i < args.length; i++) {
                if("-f".equals(args[i]) || "--file".equals(args[i])) {
                    filename = args[++i];
                } else if("-h".equals(args[i]) || "--help".equals(args[i])) {
                    exit = true;
                } else if("-s".equals(args[i]) || "--size".equals(args[i])) {
                    size = Integer.parseInt(args[++i]);
                } else if("-c".equals(args[i]) || "--const".equals(args[i])) {
                    constant = Integer.parseInt(args[++i]);
                }
            }
        } catch(ArrayIndexOutOfBoundsException e) {
            exit = true;
        } catch (NumberFormatException nfe) {
            exit = true;
        }

        if (exit) {
            System.err.println(usage);
            System.exit(1);
        }
    }
}
