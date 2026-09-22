package org.ash.gui;

import com.egantt.awt.image.encoder.BasicJPEGEncoder;
import org.junit.jupiter.api.Test;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import javax.imageio.ImageIO;
import static org.junit.jupiter.api.Assertions.*;

class ImageEncoderTest {
    @Test void jpegCanBeDecodedByStandardJava() throws Exception {
        byte[] bytes = BasicJPEGEncoder.getInstance().encode(new BufferedImage(12, 8, BufferedImage.TYPE_INT_RGB));
        assertTrue(bytes.length > 0);
        BufferedImage decoded = ImageIO.read(new ByteArrayInputStream(bytes));
        assertEquals(12, decoded.getWidth());
        assertEquals(8, decoded.getHeight());
    }
}
