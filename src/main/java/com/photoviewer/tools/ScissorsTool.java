package com.photoviewer.tools;

import com.photoviewer.image.ImageManager;
import com.photoviewer.image.ImageOperations;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.awt.image.BufferedImage;

/**
 * Scissors tool for selecting, moving, and pasting image regions.
 */
public class ScissorsTool implements Tool {
    private final ImageManager imageManager;

    private double startX, startY;
    private double endX, endY;
    private boolean isSelecting = false;
    private boolean hasSelection = false;

    // Floating region
    private BufferedImage floatingRegion;
    private double floatingX, floatingY;
    private boolean isDraggingRegion = false;

    public ScissorsTool(ImageManager imageManager) {
        this.imageManager = imageManager;
    }

    @Override
    public void onMousePressed(double x, double y) {
        if (floatingRegion != null && isPointInFloatingRegion(x, y)) {
            // Start dragging the floating region
            isDraggingRegion = true;
            startX = x;
            startY = y;
        } else {
            // Start new selection
            startX = x;
            startY = y;
            endX = x;
            endY = y;
            isSelecting = true;
            hasSelection = false;
        }
    }

    @Override
    public void onMouseDragged(double x, double y) {
        if (isDraggingRegion) {
            double dx = x - startX;
            double dy = y - startY;
            floatingX += dx;
            floatingY += dy;
            startX = x;
            startY = y;
        } else if (isSelecting) {
            endX = x;
            endY = y;
        }
    }

    @Override
    public void onMouseReleased(double x, double y) {
        if (isDraggingRegion) {
            isDraggingRegion = false;
        } else if (isSelecting) {
            endX = x;
            endY = y;
            isSelecting = false;
            hasSelection = true;
            extractSelection();
        }
    }

    @Override
    public void draw(GraphicsContext gc) {
        // Draw selection rectangle
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

        // Draw floating region
        if (floatingRegion != null) {
            javafx.scene.image.Image fxImage = javafx.embed.swing.SwingFXUtils.toFXImage(floatingRegion, null);
            gc.drawImage(fxImage, floatingX, floatingY);

            // Draw border around floating region
            gc.setStroke(Color.LIME);
            gc.setLineWidth(2.0);
            gc.strokeRect(floatingX, floatingY, floatingRegion.getWidth(), floatingRegion.getHeight());
        }
    }

    @Override
    public void reset() {
        isSelecting = false;
        hasSelection = false;
        floatingRegion = null;
        isDraggingRegion = false;
    }

    private void extractSelection() {
        if (hasSelection && imageManager.getCurrentImage() != null) {
            int x = (int) Math.min(startX, endX);
            int y = (int) Math.min(startY, endY);
            int width = (int) Math.abs(endX - startX);
            int height = (int) Math.abs(endY - startY);

            floatingRegion = ImageOperations.copyRegion(imageManager.getCurrentImage(), x, y, width, height);
            floatingX = x;
            floatingY = y;
        }
    }

    private boolean isPointInFloatingRegion(double x, double y) {
        if (floatingRegion == null)
            return false;
        return x >= floatingX && x <= floatingX + floatingRegion.getWidth() &&
                y >= floatingY && y <= floatingY + floatingRegion.getHeight();
    }

    /**
     * Paste the floating region to the image at its current position.
     */
    public void pasteFloatingRegion() {
        if (floatingRegion != null) {
            imageManager.pasteRegion(floatingRegion, (int) floatingX, (int) floatingY);
            floatingRegion = null;
        }
    }

    /**
     * Copy the floating region (duplicate it).
     */
    public void copyFloatingRegion() {
        // Keep the floating region for multiple pastes
        if (floatingRegion != null) {
            imageManager.pasteRegion(floatingRegion, (int) floatingX, (int) floatingY);
        }
    }
}
