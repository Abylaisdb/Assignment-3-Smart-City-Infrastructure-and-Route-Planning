package app;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import javax.imageio.ImageIO;

public final class PlotWriter {
    private PlotWriter() {}

    private static final Color[] COLORS = {
            new Color(0x1f77b4), new Color(0xd62728), new Color(0x2ca02c), new Color(0xff7f0e), new Color(0x9467bd), new Color(0x8c564b)};

    public static void logLogChart(String title, String xLabel, String yLabel, double[] xs,
                                   Map<String, double[]> series, File out) throws IOException {
        System.setProperty("java.awt.headless", "true");
        int W = 960, H = 620, L = 95, R = 40, T = 60, B = 85;
        BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, W, H);

        double xmin = Double.MAX_VALUE, xmax = 0, ymin = Double.MAX_VALUE, ymax = 0;
        for (double x : xs) { xmin = Math.min(xmin, x); xmax = Math.max(xmax, x); }
        for (double[] ys : series.values())
            for (double y : ys) { y = Math.max(y, 1e-9); ymin = Math.min(ymin, y); ymax = Math.max(ymax, y); }
        double xlo = Math.floor(Math.log10(xmin)), xhi = Math.ceil(Math.log10(xmax));
        double ylo = Math.floor(Math.log10(ymin)), yhi = Math.ceil(Math.log10(ymax));
        if (xhi <= xlo) xhi = xlo + 1;
        if (yhi <= ylo) yhi = ylo + 1;

        g.setFont(new Font("SansSerif", Font.PLAIN, 13));
        FontMetrics fm = g.getFontMetrics();
        for (int k = (int) xlo; k <= (int) xhi; k++) {
            double px = map(Math.pow(10, k), xlo, xhi, L, W - R);
            g.setColor(new Color(0xdddddd));
            g.drawLine((int) px, T, (int) px, H - B);
            g.setColor(Color.DARK_GRAY);
            String s = "10^" + k;
            g.drawString(s, (int) px - fm.stringWidth(s) / 2, H - B + 20);
        }
        for (int k = (int) ylo; k <= (int) yhi; k++) {
            double py = map(Math.pow(10, k), ylo, yhi, H - B, T);
            g.setColor(new Color(0xdddddd));
            g.drawLine(L, (int) py, W - R, (int) py);
            g.setColor(Color.DARK_GRAY);
            String s = "10^" + k;
            g.drawString(s, L - 10 - fm.stringWidth(s), (int) py + 5);
        }
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(1.5f));
        g.drawLine(L, T, L, H - B);
        g.drawLine(L, H - B, W - R, H - B);

        int idx = 0;
        for (Map.Entry<String, double[]> en : series.entrySet()) {
            Color c = COLORS[idx % COLORS.length];
            double[] ys = en.getValue();
            g.setColor(c);
            g.setStroke(new BasicStroke(2.2f));
            for (int i = 0; i < xs.length; i++) {
                double px = map(xs[i], xlo, xhi, L, W - R), py = map(Math.max(ys[i], 1e-9), ylo, yhi, H - B, T);
                if (i > 0) {
                    double qx = map(xs[i - 1], xlo, xhi, L, W - R), qy = map(Math.max(ys[i - 1], 1e-9), ylo, yhi, H - B, T);
                    g.drawLine((int) qx, (int) qy, (int) px, (int) py);
                }
            }
            for (int i = 0; i < xs.length; i++) {
                double px = map(xs[i], xlo, xhi, L, W - R), py = map(Math.max(ys[i], 1e-9), ylo, yhi, H - B, T);
                g.fillOval((int) px - 4, (int) py - 4, 8, 8);
            }
            int ly = T + 18 + idx * 20;
            g.fillRect(L + 15, ly - 9, 22, 4);
            g.setColor(Color.BLACK);
            g.drawString(en.getKey(), L + 45, ly);
            idx++;
        }

        g.setColor(Color.BLACK);
        g.setFont(new Font("SansSerif", Font.BOLD, 17));
        g.drawString(title, (W - g.getFontMetrics().stringWidth(title)) / 2, 32);
        g.setFont(new Font("SansSerif", Font.PLAIN, 14));
        g.drawString(xLabel, (W - g.getFontMetrics().stringWidth(xLabel)) / 2, H - 20);
        g.rotate(-Math.PI / 2);
        g.drawString(yLabel, -(H + g.getFontMetrics().stringWidth(yLabel)) / 2, 25);
        g.dispose();

        out.getParentFile().mkdirs();
        ImageIO.write(img, "png", out);
    }

    private static double map(double v, double lo, double hi, double a, double b) {
        return a + (Math.log10(v) - lo) / (hi - lo) * (b - a);
    }
}
