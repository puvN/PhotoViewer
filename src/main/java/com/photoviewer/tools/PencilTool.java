package com.photoviewer.tools;

import com.photoviewer.ui.ImageCanvas;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Pencil tool for freehand drawing on the image.
 */
public class PencilTool implements Tool {
    private final ImageCanvas canvas;

    private Color color = Color.RED;
    private double brushSize = 3.0;

    private double lastX = -1;
    private double lastY = -1;
    private boolean isDrawing = false;

    public PencilTool(ImageCanvas canvas) {
        this.canvas = canvas;
    }

    @Override
    public void onMousePressed(double x, double y) {
        lastX = x;
        lastY = y;
        isDrawing = true;

        // Draw a dot at the start point
        drawOnLayer(x, y, x, y);
    }

    @Override
    public void onMouseDragged(double x, double y) {
        if (isDrawing) {
            drawOnLayer(lastX, lastY, x, y);
            lastX = x;
            lastY = y;
        }
    }

    @Override
    public void onMouseReleased(double x, double y) {
        if (isDrawing) {
            drawOnLayer(lastX, lastY, x, y);
            isDrawing = false;
            lastX = -1;
            lastY = -1;

            // Automatically save the drawing stroke to undo stack
            canvas.applyCurrentDrawing();
        }
    }

    @Override
    public void draw(GraphicsContext gc) {
        // Draw the current drawing layer on top of the image
        BufferedImage layer = canvas.getDrawingLayer();
        if (layer != null) {
            javafx.scene.image.Image fxImage = SwingFXUtils.toFXImage(layer, null);
            gc.drawImage(fxImage, 0, 0);
        }
    }

    @Override
    public void reset() {
        isDrawing = false;
        lastX = -1;
        lastY = -1;
    }

    private void drawOnLayer(double x1, double y1, double x2, double y2) {
        BufferedImage layer = canvas.getDrawingLayer();
        if (layer == null)
            return;

        Graphics2D g = layer.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Convert JavaFX color to AWT color
        java.awt.Color awtColor = new java.awt.Color(
                (float) color.getRed(),
                (float) color.getGreen(),
                (float) color.getBlue(),
                (float) color.getOpacity());

        g.setColor(awtColor);
        g.setStroke(new BasicStroke((float) brushSize, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
        g.dispose();
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    public void setBrushSize(double size) {
        this.brushSize = Math.max(1.0, Math.min(size, 50.0));
    }

    public double getBrushSize() {
        return brushSize;
    }
}
