package com.stockdual.domain.service;
 
import org.springframework.stereotype.Service;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.UUID;
 
@Service
public class ImageProcessingService {
 
    private static final String UPLOAD_DIR = "uploaded-images";
    private static final int TARGET_SIZE = 650;
 
    public ImageProcessingService() {
        File dir = new File(UPLOAD_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }
 
    /**
     * Processes a raw image: scales to fit inside a 650x650 square canvas,
     * cleans off-white backgrounds to pure white, and saves as WebP.
     * 
     * @param imageBytes original image bytes
     * @return the filename of the saved WebP image
     * @throws IOException if processing fails
     */
    public String processAndSaveImage(byte[] imageBytes) throws IOException {
        BufferedImage original = ImageIO.read(new ByteArrayInputStream(imageBytes));
        if (original == null) {
            throw new IOException("Could not read image bytes. Unsupported format or corrupted data.");
        }
 
        // 1. Create a squared 650x650 canvas with TYPE_INT_ARGB (supports transparency if needed)
        BufferedImage squared = new BufferedImage(TARGET_SIZE, TARGET_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = squared.createGraphics();
 
        // Fill canvas with solid white (standard e-commerce backdrop)
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, TARGET_SIZE, TARGET_SIZE);
 
        // Enable high quality rendering hints
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 
        // 2. Scale image keeping aspect ratio
        int originalWidth = original.getWidth();
        int originalHeight = original.getHeight();
        double ratio = Math.min((double) TARGET_SIZE / originalWidth, (double) TARGET_SIZE / originalHeight);
        int targetWidth = (int) (originalWidth * ratio);
        int targetHeight = (int) (originalHeight * ratio);
 
        int x = (TARGET_SIZE - targetWidth) / 2;
        int y = (TARGET_SIZE - targetHeight) / 2;
 
        g2d.drawImage(original, x, y, targetWidth, targetHeight, null);
        g2d.dispose();
 
        // 3. Clean background: convert light grays and off-whites to pure white (#FFFFFF)
        cleanBackground(squared);
 
        // 4. Save to filesytem as WebP
        String filename = UUID.randomUUID().toString() + ".webp";
        File outputFile = new File(UPLOAD_DIR, filename);
        
        boolean success = false;
        try {
            // Write utilizing the sejda-imageio-webp plugin
            success = ImageIO.write(squared, "webp", outputFile);
        } catch (Throwable t) {
            // Fallback if native libraries (e.g. webp-imageio.dll) fail to load
            success = false;
        }
        
        if (!success) {
            // Fallback to PNG if WebP writer is not loaded
            filename = filename.replace(".webp", ".png");
            outputFile = new File(UPLOAD_DIR, filename);
            ImageIO.write(squared, "png", outputFile);
        }
 
        return filename;
    }
 
    private void cleanBackground(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
 
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = image.getRGB(x, y);
                int r = (argb >> 16) & 0xff;
                int g = (argb >> 8) & 0xff;
                int b = argb & 0xff;
 
                // If a pixel is very bright and has low saturation (off-white/light gray)
                // we force it to pure white to remove camera background noise.
                if (r > 210 && g > 210 && b > 210) {
                    int diffRG = Math.abs(r - g);
                    int diffRB = Math.abs(r - b);
                    int diffGB = Math.abs(g - b);
                    
                    if (diffRG < 15 && diffRB < 15 && diffGB < 15) {
                        image.setRGB(x, y, 0xFFFFFFFF); // Force solid white
                    }
                }
            }
        }
    }
}
