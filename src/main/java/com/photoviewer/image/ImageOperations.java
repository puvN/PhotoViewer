package com.photoviewer.image;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Utility class for image manipulation operations.
 */
public class ImageOperations {

    /**
     * Resize an image with high quality.
     */
    public static BufferedImage resize(BufferedImage original, int newWidth, int newHeight) {
        BufferedImage resized = new BufferedImage(newWidth, newHeight, original.getType());
        Graphics2D g = resized.createGraphics();

        // High quality rendering
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.drawImage(original, 0, 0, newWidth, newHeight, null);
        g.dispose();

        return resized;
    }

    /**
     * Crop an image to the specified rectangle.
     */
    public static BufferedImage crop(BufferedImage original, int x, int y, int width, int height) {
        // Ensure bounds are within image
        x = Math.max(0, Math.min(x, original.getWidth() - 1));
        y = Math.max(0, Math.min(y, original.getHeight() - 1));
        width = Math.min(width, original.getWidth() - x);
        height = Math.min(height, original.getHeight() - y);

        return original.getSubimage(x, y, width, height);
    }

    /**
     * Copy a region from an image.
     */
    public static BufferedImage copyRegion(BufferedImage source, int x, int y, int width, int height) {
        BufferedImage region = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = region.createGraphics();
        g.drawImage(source, 0, 0, width, height, x, y, x + width, y + height, null);
        g.dispose();
        return region;
    }

    /**
     * Paste a region onto an image at the specified location.
     */
    public static BufferedImage pasteRegion(BufferedImage base, BufferedImage region, int x, int y) {
        BufferedImage result = copyImage(base);
        Graphics2D g = result.createGraphics();
        g.drawImage(region, x, y, null);
        g.dispose();
        return result;
    }

    /**
     * Merge two images (overlay drawing layer on base image).
     */
    public static BufferedImage mergeImages(BufferedImage base, BufferedImage overlay) {
        BufferedImage merged = new BufferedImage(base.getWidth(), base.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = merged.createGraphics();
        g.drawImage(base, 0, 0, null);
        g.drawImage(overlay, 0, 0, null);
        g.dispose();
        return merged;
    }

    /**
     * Create a copy of an image.
     */
    public static BufferedImage copyImage(BufferedImage source) {
        BufferedImage copy = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());
        Graphics2D g = copy.createGraphics();
        g.drawImage(source, 0, 0, null);
        g.dispose();
        return copy;
    }

    /**
     * Rotate an image 90 degrees to the right.
     */
    public static BufferedImage rotate90Right(BufferedImage original) {
        int width = original.getWidth();
        int height = original.getHeight();

        // Swap width and height for the new image
        BufferedImage rotated = new BufferedImage(height, width, original.getType());
        Graphics2D g = rotated.createGraphics();

        // Setup rotation
        g.translate(height, 0);
        g.rotate(Math.toRadians(90));

        g.drawImage(original, 0, 0, null);
        g.dispose();

        return rotated;
    }

    /**
     * Create a blank transparent image.
     */
    public static BufferedImage createTransparentImage(int width, int height) {
        return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    }
}
