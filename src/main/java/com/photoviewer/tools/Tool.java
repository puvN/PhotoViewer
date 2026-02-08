package com.photoviewer.tools;

import javafx.scene.canvas.GraphicsContext;

/**
 * Base interface for all tools in the PhotoViewer.
 */
public interface Tool {
    void onMousePressed(double x, double y);

    void onMouseDragged(double x, double y);

    void onMouseReleased(double x, double y);

    void draw(GraphicsContext gc);

    void reset();
}
