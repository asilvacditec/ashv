package org.ash.sqlserver;

import org.ash.activity.ActivityRepository.Point;
import org.junit.jupiter.api.Test;
import javax.swing.SwingUtilities;
import java.awt.image.BufferedImage;
import java.time.Instant;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class SqlServerChartTest {
    @Test void paintsEmptyZeroAndSingleTimestampWithoutDivisionErrors() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            var chart = new SqlServerFrame.ActivityChart();
            chart.setSize(900, 400);
            var image = new BufferedImage(900, 400, BufferedImage.TYPE_INT_RGB);
            var graphics = image.createGraphics();
            try {
                chart.paint(graphics);
                chart.setPoints(List.of(new Point(Instant.EPOCH, 0)));
                chart.paint(graphics);
                chart.setPoints(List.of(new Point(Instant.EPOCH, 4), new Point(Instant.EPOCH.plusSeconds(120), 2)));
                chart.paint(graphics);
                assertNotEquals(image.getRGB(65, 40), image.getRGB(300, 200));
            } finally { graphics.dispose(); }
        });
    }
}
