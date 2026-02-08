package com.photoviewer.tools;

import com.photoviewer.image.ImageManager;
import com.photoviewer.image.ImageOperations;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.embed.swing.SwingFXUtils;

import java.awt.image.BufferedImage;

/**
 * Scissors tool for selecting image regions and copying them to the system
 * clipboard.
 */
public class ScissorsTool implements Tool {
    private final ImageManager imageManager;

    private double startX, startY;
    private double endX, endY;
    private boolean isSelecting = false;
    private boolean hasSelection = false;

    public ScissorsTool(ImageManager imageManager) {
        this.imageManager = imageManager;
    }

    @Override
    public void onMousePressed(double x, double y) {
        startX = x;
        startY = y;
        endX = x;
        endY = y;
        isSelecting = true;
        hasSelection = false;
    }

    @Override
    public void onMouseDragged(double x, double y) {
        if (isSelecting) {
            endX = x;
            endY = y;
        }
    }

    @Override
    public void onMouseReleased(double x, double y) {
        if (isSelecting) {
            endX = x;
            endY = y;
            isSelecting = false;
            hasSelection = true;
        }
    }

    @Override
    public void draw(GraphicsContext gc) {
        if (isSelecting || hasSelection) {
            double x = Math.min(startX, endX);
            double y = Math.min(startY, endY);
            double width = Math.abs(endX - startX);
            double height = Math.abs(endY - startY);

            gc.setStroke(Color.ORANGE);
            gc.setLineWidth(2.0);
            gc.setLineDashes(5, 5);
            gc.strokeRect(x, y, width, height);
            gc.setLineDashes(null);
        }
    }

    @Override
    public void reset() {
        isSelecting = false;
        hasSelection = false;
    }

    /**
     * Copy the selected region to the system clipboard.
     */
    public void copyToSystemClipboard() {
        if (hasSelection && imageManager.getCurrentImage() != null) {
            int x = (int) Math.min(startX, endX);
            int y = (int) Math.min(startY, endY);
            int width = (int) Math.abs(endX - startX);
            int height = (int) Math.abs(endY - startY);

            if (width > 0 && height > 0) {
                BufferedImage selection = ImageOperations.copyRegion(imageManager.getCurrentImage(), x, y, width,
                        height);
                if (selection != null) {
                    Clipboard clipboard = Clipboard.getSystemClipboard();
                    ClipboardContent content = new ClipboardContent();

                    // Convert BufferedImage to JavaFX Image
                    javafx.scene.image.Image fxImage = SwingFXUtils.toFXImage(selection, null);
                    content.putImage(fxImage);

                    clipboard.setContent(content);
                }
            }
        }
    }
}
