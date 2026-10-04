package kz.aitu.daa;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;

final class Plotter {
    private static final Color[] COLORS = {
            new Color(30, 100, 175), new Color(200, 75, 35),
            new Color(20, 135, 100), new Color(140, 65, 165)
    };
    private static final String[] TITLES = {
            "W1 Random access", "W2 Search", "W3 Insert and remove", "W4 Priority processing"
    };
    private static final String[] AXES = {
            "Median time (ms)", "Steps (count)", "Moves (count)", "Comparisons (count)"
    };

    private Plotter() { }

    static void write(Path directory, Benchmark.Result[] results) throws IOException {
        Files.createDirectories(directory);
        for (int workload = 1; workload <= 4; workload++) {
            BufferedImage image = new BufferedImage(1_400, 760, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, image.getWidth(), image.getHeight());
            g.setColor(Color.BLACK);
            g.setFont(new Font("SansSerif", Font.BOLD, 26));
            g.drawString(TITLES[workload - 1], 35, 35);
            String[] series = workload == 4 ? new String[]{"MinHeap/-"}
                    : workload == 3 ? new String[]{"DynamicArray/head", "MyLinkedList/head",
                    "DynamicArray/middle", "MyLinkedList/middle"}
                    : new String[]{"DynamicArray/-", "MyLinkedList/-"};
            legend(g, series);
            for (int metric = 0; metric < 4; metric++) {
                panel(g, results, "W" + workload, series, metric,
                        95 + (metric % 2) * 690, 145 + (metric / 2) * 300);
            }
            g.setColor(Color.DARK_GRAY);
            g.setFont(new Font("SansSerif", Font.PLAIN, 17));
            g.drawString("x: log10(n); time: log10(ms); counts: log10(1 + count). 5 warm-ups, median of 5 runs.",
                    35, 745);
            g.dispose();
            ImageIO.write(image, "png", directory.resolve("W" + workload + ".png").toFile());
        }
    }

    private static void legend(Graphics2D g, String[] series) {
        g.setFont(new Font("SansSerif", Font.PLAIN, 20));
        for (int i = 0; i < series.length; i++) {
            int x = 35 + i * 340;
            g.setColor(COLORS[i]);
            g.setStroke(stroke(i));
            g.drawLine(x, 75, x + 28, 75);
            g.fillOval(x + 10, 71, 8, 8);
            g.setColor(Color.BLACK);
            g.drawString(series[i].replace("/-", "").replace("/", " "), x + 37, 82);
        }
    }

    private static void panel(Graphics2D g, Benchmark.Result[] results, String workload,
                              String[] series, int metric, int x, int y) {
        int width = 555;
        int height = 195;
        double max = 0;
        double min = Double.POSITIVE_INFINITY;
        for (Benchmark.Result result : results) {
            if (result.workload().equals(workload)) {
                max = Math.max(max, value(result, metric));
                min = Math.min(min, value(result, metric));
            }
        }
        double lower = metric == 0 ? Math.floor(Math.log10(min)) : 0;
        double upper = metric == 0 ? Math.ceil(Math.log10(max)) : Math.ceil(Math.log10(1 + max));
        upper = Math.max(lower + 1, upper);
        g.setFont(new Font("SansSerif", Font.BOLD, 21));
        g.setColor(Color.BLACK);
        g.drawString(AXES[metric], x, y - 18);
        g.setFont(new Font("SansSerif", Font.PLAIN, 18));
        g.setStroke(new BasicStroke(1));
        for (int i = (int) lower; i <= (int) upper; i++) {
            double tick = metric == 0 ? i : i == 0 ? 0 : Math.log10(1 + Math.pow(10, i));
            int py = y + height - (int) (height * (tick - lower) / (upper - lower));
            g.setColor(new Color(225, 230, 235));
            g.drawLine(x, py, x + width, py);
            g.setColor(Color.DARK_GRAY);
            String label = metric != 0 && i == 0 ? "0" : "1e" + i;
            g.drawString(label, x - 80, py + 6);
        }
        for (int i = 0; i < Benchmark.SIZES.length; i++) {
            int px = x + width * i / 3;
            g.setColor(new Color(225, 230, 235));
            g.drawLine(px, y, px, y + height);
            g.setColor(Color.DARK_GRAY);
            g.drawString(new String[]{"100", "1,000", "10,000", "100,000"}[i], px - 27, y + height + 27);
        }
        g.drawString("n (elements)", x + width / 2 - 45, y + height + 54);
        for (int s = 0; s < series.length; s++) {
            g.setColor(COLORS[s]);
            g.setStroke(stroke(s));
            int previousX = -1;
            int previousY = -1;
            for (Benchmark.Result result : results) {
                if (!result.workload().equals(workload)
                        || !(result.structure() + "/" + result.variant()).equals(series[s])) {
                    continue;
                }
                int px = x + (int) ((Math.log10(result.n()) - 2) * width / 3);
                double scaled = metric == 0 ? Math.log10(value(result, metric))
                        : Math.log10(1 + value(result, metric));
                int py = y + height - (int) ((scaled - lower) * height / (upper - lower));
                if (previousX >= 0) {
                    g.drawLine(previousX, previousY, px, py);
                }
                g.fillOval(px - 4, py - 4, 8, 8);
                previousX = px;
                previousY = py;
            }
        }
    }

    private static BasicStroke stroke(int series) {
        return new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                10, new float[]{10 + series * 4, 3 + series * 3}, 0);
    }

    private static double value(Benchmark.Result result, int metric) {
        return switch (metric) {
            case 0 -> result.timeMs();
            case 1 -> result.steps();
            case 2 -> result.moves();
            case 3 -> result.comparisons();
            default -> throw new IllegalArgumentException("Unknown metric");
        };
    }
}
