package com.stockdual.domain.service;
 
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;
 
class ImageProcessingServiceTest {
 
    private ImageProcessingService imageProcessingService;
    private String savedFilename;
 
    @BeforeEach
    void setUp() {
        imageProcessingService = new ImageProcessingService();
    }
 
    @AfterEach
    void tearDown() {
        if (savedFilename != null) {
            File file = new File("uploaded-images", savedFilename);
            if (file.exists()) {
                file.delete();
            }
        }
    }
 
    @Test
    void testProcessAndSaveImage() throws IOException {
        // Create a 100x100 dummy image (colored center, bright border)
        BufferedImage original = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = original.createGraphics();
        g2d.setColor(new Color(220, 220, 220)); // Off-white border
        g2d.fillRect(0, 0, 100, 100);
        g2d.setColor(Color.RED); // Red center
        g2d.fillRect(25, 25, 50, 50);
        g2d.dispose();
 
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(original, "png", baos);
        byte[] originalBytes = baos.toByteArray();
 
        // Process the image
        savedFilename = imageProcessingService.processAndSaveImage(originalBytes);
 
        // Assertions
        assertNotNull(savedFilename);
        assertTrue(savedFilename.endsWith(".webp") || savedFilename.endsWith(".png"));
 
        File savedFile = new File("uploaded-images", savedFilename);
        assertTrue(savedFile.exists(), "The processed file should exist on disk");
 
        // Read saved file back and check its size
        BufferedImage savedImage = ImageIO.read(savedFile);
        assertNotNull(savedImage);
        assertEquals(650, savedImage.getWidth(), "Width should be exactly 650px");
        assertEquals(650, savedImage.getHeight(), "Height should be exactly 650px");
 
        // The background of saved image at (0, 0) should be solid white (#FFFFFF)
        // because the original (220,220,220) was off-white and should have been cleaned to pure white
        int rgb = savedImage.getRGB(0, 0);
        int r = (rgb >> 16) & 0xff;
        int g = (rgb >> 8) & 0xff;
        int b = rgb & 0xff;
        assertEquals(255, r, "Background red component should be 255");
        assertEquals(255, g, "Background green component should be 255");
        assertEquals(255, b, "Background blue component should be 255");
    }
 
    @Test
    void testProcessCorruptedBytesThrowsException() {
        byte[] corrupted = new byte[]{1, 2, 3, 4, 5};
        assertThrows(IOException.class, () -> {
            imageProcessingService.processAndSaveImage(corrupted);
        });
    }
}
