package com.photoviewer.tools;

import com.photoviewer.image.ImageManager;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Selection tool for creating rectangular selections.
 */
public class SelectionTool implements Tool {
    private final ImageManager imageManager;

    private double startX, startY;
    private double endX, endY;
    private boolean isSelecting = false;
    private boolean hasSelection = false;

    public SelectionTool(ImageManager imageManager) {
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

            // Draw selection rectangle
            gc.setStroke(Color.DODGERBLUE);
            gc.setLineWidth(2.0);
            gc.setLineDashes(5, 5);
            gc.strokeRect(x, y, width, height);
            gc.setLineDashes(null);

            // Draw semi-transparent fill
            gc.setFill(Color.rgb(30, 144, 255, 0.2));
            gc.fillRect(x, y, width, height);
        }
    }

    @Override
    public void reset() {
        isSelecting = false;
        hasSelection = false;
    }

    public boolean hasSelection() {
        return hasSelection;
    }

    public int getSelectionX() {
        return (int) Math.min(startX, endX);
    }

    public int getSelectionY() {
        return (int) Math.min(startY, endY);
    }

    public int getSelectionWidth() {
        return (int) Math.abs(endX - startX);
    }

    public int getSelectionHeight() {
        return (int) Math.abs(endY - startY);
    }

    /**
     * Crop the image to the current selection.
     */
    public void cropToSelection() {
        if (!hasSelection())
            return;

        int width = getSelectionWidth();
        int height = getSelectionHeight();

        // Validate dimensions
        if (width <= 0 || height <= 0) {
            System.err.println("Invalid selection dimensions: width=" + width + ", height=" + height);
            return;
        }

        imageManager.cropImage(getSelectionX(), getSelectionY(), width, height);
        reset();
    }
}
