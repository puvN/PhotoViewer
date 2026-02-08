package com.photoviewer.ui;

import com.photoviewer.image.ImageManager;
import com.photoviewer.image.ImageOperations;
import com.photoviewer.tools.*;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.awt.image.BufferedImage;

/**
 * Canvas for displaying and interacting with images.
 * Handles zoom, pan, and tool interactions.
 */
public class ImageCanvas extends Pane {
    private final Canvas canvas;
    private final ImageManager imageManager;

    private double zoomLevel = 1.0;
    private double offsetX = 0;
    private double offsetY = 0;

    private Tool currentTool;
    private BufferedImage drawingLayer;

    // Mouse tracking
    private double lastMouseX;
    private double lastMouseY;
    private boolean isPanning = false;

    public ImageCanvas(ImageManager imageManager) {
        this.imageManager = imageManager;
        this.canvas = new Canvas();

        getChildren().add(canvas);

        // Bind canvas size to pane size
        canvas.widthProperty().bind(widthProperty());
        canvas.heightProperty().bind(heightProperty());

        // Redraw when size changes
        widthProperty().addListener((obs, oldVal, newVal) -> displayImage());
        heightProperty().addListener((obs, oldVal, newVal) -> displayImage());

        setupMouseHandlers();

        // Set default background
        setStyle("-fx-background-color: #2b2b2b;");
    }

    private void setupMouseHandlers() {
        canvas.setOnMousePressed(this::handleMousePressed);
        canvas.setOnMouseDragged(this::handleMouseDragged);
        canvas.setOnMouseReleased(this::handleMouseReleased);
        canvas.setOnScroll(this::handleScroll);
    }

    private void handleMousePressed(MouseEvent e) {
        lastMouseX = e.getX();
        lastMouseY = e.getY();

        if (e.isMiddleButtonDown() || (e.isPrimaryButtonDown() && e.isControlDown())) {
            isPanning = true;
        } else if (currentTool != null) {
            double[] imageCoords = screenToImageCoords(e.getX(), e.getY());
            currentTool.onMousePressed(imageCoords[0], imageCoords[1]);
            displayImage();
        }
    }

    private void handleMouseDragged(MouseEvent e) {
        if (isPanning) {
            double dx = e.getX() - lastMouseX;
            double dy = e.getY() - lastMouseY;
            offsetX += dx;
            offsetY += dy;
            lastMouseX = e.getX();
            lastMouseY = e.getY();
            displayImage();
        } else if (currentTool != null) {
            double[] imageCoords = screenToImageCoords(e.getX(), e.getY());
            currentTool.onMouseDragged(imageCoords[0], imageCoords[1]);
            displayImage();
        }
    }

    private void handleMouseReleased(MouseEvent e) {
        if (isPanning) {
            isPanning = false;
        } else if (currentTool != null) {
            double[] imageCoords = screenToImageCoords(e.getX(), e.getY());
            currentTool.onMouseReleased(imageCoords[0], imageCoords[1]);
            displayImage();
        }
    }

    private void handleScroll(ScrollEvent e) {
        if (e.getDeltaY() > 0) {
            zoomIn();
        } else {
            zoomOut();
        }
    }

    /**
     * Convert screen coordinates to image coordinates.
     */
    private double[] screenToImageCoords(double screenX, double screenY) {
        BufferedImage img = imageManager.getCurrentImage();
        if (img == null)
            return new double[] { 0, 0 };

        double zoomedWidth = img.getWidth() * zoomLevel;
        double zoomedHeight = img.getHeight() * zoomLevel;
        double centX = (canvas.getWidth() - zoomedWidth) / 2;
        double centY = (canvas.getHeight() - zoomedHeight) / 2;

        double imageX = (screenX - centX - offsetX) / zoomLevel;
        double imageY = (screenY - centY - offsetY) / zoomLevel;

        return new double[] { imageX, imageY };
    }

    /**
     * Display the current image on the canvas.
     */
    public void displayImage() {
        Image image = imageManager.getCurrentImageFX();

        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Clear canvas
        gc.setFill(Color.rgb(43, 43, 43));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        if (image == null)
            return;

        // Calculate total zoomed image dimensions
        double zoomedWidth = image.getWidth() * zoomLevel;
        double zoomedHeight = image.getHeight() * zoomLevel;

        // Center the image within the view
        double centX = (canvas.getWidth() - zoomedWidth) / 2;
        double centY = (canvas.getHeight() - zoomedHeight) / 2;

        // Draw image with zoom
        gc.save();
        gc.translate(centX + offsetX, centY + offsetY);
        gc.scale(zoomLevel, zoomLevel);
        gc.drawImage(image, 0, 0);
        gc.restore();

        // Draw tool overlay if active
        if (currentTool != null) {
            gc.save();
            gc.translate(centX + offsetX, centY + offsetY);
            gc.scale(zoomLevel, zoomLevel);
            currentTool.draw(gc);
            gc.restore();
        }
    }

    public void zoomIn() {
        zoomLevel = Math.min(zoomLevel * 1.2, 5.0);
        displayImage();
    }

    public void zoomOut() {
        zoomLevel = Math.max(zoomLevel / 1.2, 0.1);
        displayImage();
    }

    public void fitToWindow() {
        BufferedImage img = imageManager.getCurrentImage();
        if (img == null)
            return;

        double scaleX = canvas.getWidth() / img.getWidth();
        double scaleY = canvas.getHeight() / img.getHeight();
        zoomLevel = Math.min(scaleX, scaleY) * 0.9; // 90% to add some padding

        // Reset offsets
        offsetX = 0;
        offsetY = 0;

        displayImage();
    }

    public void setTool(Tool tool) {
        // Reset cursor when changing tools
        canvas.setCursor(javafx.scene.Cursor.DEFAULT);
        this.currentTool = tool;
    }

    public Tool getCurrentTool() {
        return currentTool;
    }

    public void setCanvasCursor(javafx.scene.Cursor cursor) {
        canvas.setCursor(cursor);
    }

    public void applyCurrentDrawing() {
        if (drawingLayer != null) {
            imageManager.applyDrawing(drawingLayer);
            clearDrawingLayer();
            displayImage();
        }
    }

    public double getZoomLevel() {
        return zoomLevel;
    }

    public BufferedImage getDrawingLayer() {
        if (drawingLayer == null && imageManager.getCurrentImage() != null) {
            BufferedImage img = imageManager.getCurrentImage();
            drawingLayer = ImageOperations.createTransparentImage(img.getWidth(), img.getHeight());
        }
        return drawingLayer;
    }

    public void clearDrawingLayer() {
        drawingLayer = null;
    }

    public ImageManager getImageManager() {
        return imageManager;
    }
}
