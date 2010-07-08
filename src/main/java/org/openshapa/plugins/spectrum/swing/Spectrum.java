package org.openshapa.plugins.spectrum.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;

import java.util.Random;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;


/**
 * Component for painting a spectrum.
 *
 * @author Douglas Teoh
 *
 */
public final class Spectrum extends JComponent {

    /** Pixel padding between the frame and the histogram axes. */
    private static final int AXIS_PADDING = 75;

    /** Pixel gap between each rectangle in the histogram. */
    private static final int RECT_GAP = 5;

    /** Minimum dB value. */
    private static final int MIN_DB = -70;

    /** Color to represent the minumum dB value. */
    private static final Color MIN_COLOR = Color.GREEN;

    /** Color to represent the maximum dB value. */
    private static final Color MAX_COLOR = Color.RED;

    private static final int XAXIS_STR_PADDING = 10;
    private static final int YAXIS_STR_PADDING = 25;

    /** Magnitude values for each frequency value. */
    private double[] magVals;

    private double[] freqVals;

    /** String values for the frequency axis. */
    private String[] xAxisVals;

    public void setMagnitudelVals(final double[] dbVals) {
        this.magVals = dbVals;
    }

    public void setFreqVals(final double[] freqVals) {
        this.freqVals = freqVals;
        xAxisVals = createXAxisVals(freqVals);
    }

    private String[] createXAxisVals(final double[] freqVals) {
        String[] vals = new String[freqVals.length];

        for (int i = 0; i < freqVals.length; i++) {
            vals[i] = createAxisVal(freqVals[i]);
        }

        return vals;
    }

    private String createAxisVal(final double freq) {
        double frequency = freq;

        if (frequency < 1000) {
            return Integer.toString((int) frequency);
        }

        frequency = frequency / 1000D;

        StringBuffer sb = new StringBuffer();
        sb.append(frequency);

        int decimalIdx = sb.indexOf(".");

        if (decimalIdx != -1) {
            sb.delete(decimalIdx + 2, sb.length());
        }

        sb.append('k');

        return sb.toString();
    }

    public static void main(final String[] args) {

        Runnable edtTask = new Runnable() {
                @Override public void run() {
                    JFrame frame = new JFrame();
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    frame.setLayout(new MigLayout("ins 0", "[grow]", "[grow]"));
                    frame.setMinimumSize(new Dimension(800, 480));

                    Spectrum s = new Spectrum();

                    Random rng = new Random(System.currentTimeMillis());
                    double[] dBVals = new double[30];

                    for (int i = 0; i < dBVals.length; i++) {
                        dBVals[i] = rng.nextDouble() * -70;
                    }

                    s.setMagnitudelVals(dBVals);

                    double[] freqVals = new double[30];

                    for (int i = 0; i < dBVals.length; i++) {
                        freqVals[i] = rng.nextDouble() * 20000;
                    }

                    s.setFreqVals(freqVals);

                    frame.add(s, "grow");
                    frame.pack();

                    frame.setVisible(true);
                }
            };

        SwingUtilities.invokeLater(edtTask);

    }

    @Override protected void paintComponent(final Graphics g) {

        if (magVals == null) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        Dimension d = getSize();

        // Black background.
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, d.width, d.height);

        // y coordinate of the x axis.
        final int xAxisY = d.height - AXIS_PADDING;

        // Number of pixels per dB.
        final double pixelsDb = (d.height - (AXIS_PADDING * 2))
            / (double) Math.abs(MIN_DB);

        // Width of a rectangle.
        final double rectWidth =
            (double) (d.width - (AXIS_PADDING * 2) - RECT_GAP)
            / (double) magVals.length;

        g2.setColor(Color.BLACK);

        // Paint gridlines.
        int dbVal = Math.abs(MIN_DB);
        g2.setColor(new Color(255, 255, 255, 128));

        while (dbVal >= 0) {
            int y = (int) (pixelsDb * dbVal) + AXIS_PADDING;

            if ((dbVal % 10) == 0) {
                g2.drawLine(AXIS_PADDING, y, d.width - AXIS_PADDING, y);
            }

            dbVal -= 10;
        }

        FontMetrics fm = g2.getFontMetrics(g2.getFont());

        // Paint the histogram as a gradient.
        GradientPaint gp = new GradientPaint(AXIS_PADDING, AXIS_PADDING,
                MAX_COLOR, AXIS_PADDING, xAxisY, MIN_COLOR);
        Paint oldPaint = g2.getPaint();

        g2.setPaint(gp);

        // Paint the rectangles.
        for (int i = 0; i < magVals.length; i++) {
            int x = (int) (i * rectWidth) + AXIS_PADDING + RECT_GAP;
            int y = (int) Math.abs(Math.max(magVals[i], MIN_DB) * pixelsDb)
                + AXIS_PADDING;
            int width = (int) Math.ceil(rectWidth - RECT_GAP);
            int height = (int) (Math.abs(MIN_DB - magVals[i]) * pixelsDb);

            if (magVals[i] > MIN_DB) {
                g2.fillRect(x, y, width, height);
            }

            // Paint x-axis labels.
            if (xAxisVals != null) {
                g2.setPaint(oldPaint);
                g2.setColor(Color.WHITE);

                Rectangle2D strBounds = fm.getStringBounds(xAxisVals[i], g2);

                g2.drawString(xAxisVals[i],
                    (int) (x + ((width - strBounds.getWidth()) / 2)),
                    d.height - AXIS_PADDING + YAXIS_STR_PADDING);
                g2.setPaint(gp);
            }

        }

        // Restore the old painting strategy.
        g2.setPaint(oldPaint);

        // Paint the y axis values.
        dbVal = Math.abs(MIN_DB);
        g2.setColor(Color.WHITE);

        while (dbVal >= 0) {
            int y = (int) (pixelsDb * dbVal) + AXIS_PADDING;

            if ((dbVal % 10) == 0) {
                String val = Integer.toString(-1 * dbVal);

                // For calculating right alignment for left axis.
                Rectangle2D strBounds = fm.getStringBounds(val, g2);

                g2.drawString(val,
                    (int) (AXIS_PADDING - XAXIS_STR_PADDING
                        - strBounds.getWidth()), y);
                g2.drawString(val, d.width - AXIS_PADDING + XAXIS_STR_PADDING,
                    y);
            }

            dbVal -= 10; // 2
        }

        // Paint the spectogram x and y axis.
        g2.setColor(Color.WHITE);

        // Left y axis.
        g2.drawLine(AXIS_PADDING, AXIS_PADDING, AXIS_PADDING, xAxisY);

        // Right y axis.
        g2.drawLine(d.width - AXIS_PADDING, AXIS_PADDING,
            d.width - AXIS_PADDING, xAxisY);

        // Bottom x axis.
        g2.drawLine(AXIS_PADDING, xAxisY, d.width - AXIS_PADDING, xAxisY);

        // Paint axis labels.
        final String db = "dB";
        final String hz = "Frequency (Hz)";

        Rectangle2D dbBounds = fm.getStringBounds(db, g2);
        Rectangle2D hzBounds = fm.getStringBounds(hz, g2);

        g2.drawString(db, (int) ((AXIS_PADDING - dbBounds.getWidth()) / 2),
            d.height / 2);

        g2.drawString(db,
            (int) ((AXIS_PADDING - dbBounds.getWidth()) / 2) + d.width
            - AXIS_PADDING, d.height / 2);

        g2.drawString(hz, (int) ((d.width - hzBounds.getWidth()) / 2D),
            (int) (xAxisY + YAXIS_STR_PADDING + hzBounds.getHeight() + 5));

        g2.dispose();

    }

}
