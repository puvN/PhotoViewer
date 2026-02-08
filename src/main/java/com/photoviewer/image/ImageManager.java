package com.photoviewer.image;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Stack;

/**
 * Manages the current image state, file operations, and undo/redo
 * functionality.
 */
public class ImageManager {
    private BufferedImage currentImage;
    private File currentFile;
    private Stack<BufferedImage> undoStack;
    private Stack<BufferedImage> redoStack;

    public ImageManager() {
        this.undoStack = new Stack<>();
        this.redoStack = new Stack<>();
    }

    /**
     * Load an image from a file.
     */
    public boolean loadImage(File file) {
        try {
            BufferedImage image = ImageIO.read(file);
            if (image != null) {
                saveToUndoStack();
                this.currentImage = image;
                this.currentFile = file;
                this.redoStack.clear();
                return true;
            }
        } catch (IOException e) {
            System.err.println("Error loading image: " + e.getMessage());
        }
        return false;
    }

    /**
     * Save the current image to its original file.
     */
    public boolean saveImage() {
        if (currentFile != null && currentImage != null) {
            return saveImageToFile(currentFile);
        }
        return saveImageAs();
    }

    /**
     * Save the current image to a new file.
     */
    public boolean saveImageAs() {
        if (currentImage == null) {
            return false;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PNG", "*.png"),
                new FileChooser.ExtensionFilter("JPEG", "*.jpg"),
                new FileChooser.ExtensionFilter("BMP", "*.bmp"));

        File file = fileChooser.showSaveDialog(new Stage());
        if (file != null) {
            if (saveImageToFile(file)) {
                this.currentFile = file;
                return true;
            }
        }
        return false;
    }

    private boolean saveImageToFile(File file) {
        try {
            String extension = getFileExtension(file);
            ImageIO.write(currentImage, extension, file);
            return true;
        } catch (IOException e) {
            System.err.println("Error saving image: " + e.getMessage());
            return false;
        }
    }

    private String getFileExtension(File file) {
        String name = file.getName();
        int lastDot = name.lastIndexOf('.');
        if (lastDot > 0) {
            return name.substring(lastDot + 1).toLowerCase();
        }
        return "png"; // default
    }

    /**
     * Resize the current image.
     */
    public void resizeImage(int newWidth, int newHeight) {
        if (currentImage != null) {
            saveToUndoStack();
            currentImage = ImageOperations.resize(currentImage, newWidth, newHeight);
            redoStack.clear();
        }
    }

    /**
     * Crop the image to the specified rectangle.
     */
    public void cropImage(int x, int y, int width, int height) {
        if (currentImage != null) {
            saveToUndoStack();
            currentImage = ImageOperations.crop(currentImage, x, y, width, height);
            redoStack.clear();
        }
    }

    /**
     * Apply a drawing operation to the image.
     */
    public void applyDrawing(BufferedImage drawingLayer) {
        if (currentImage != null) {
            saveToUndoStack();
            currentImage = ImageOperations.mergeImages(currentImage, drawingLayer);
            redoStack.clear();
        }
    }

    /**
     * Paste an image region at the specified location.
     */
    public void pasteRegion(BufferedImage region, int x, int y) {
        if (currentImage != null) {
            saveToUndoStack();
            currentImage = ImageOperations.pasteRegion(currentImage, region, x, y);
            redoStack.clear();
        }
    }

    /**
     * Undo the last operation.
     */
    public void undo() {
        if (!undoStack.isEmpty()) {
            redoStack.push(copyImage(currentImage));
            currentImage = undoStack.pop();
        }
    }

    /**
     * Redo the last undone operation.
     */
    public void redo() {
        if (!redoStack.isEmpty()) {
            undoStack.push(copyImage(currentImage));
            currentImage = redoStack.pop();
        }
    }

    private void saveToUndoStack() {
        if (currentImage != null) {
            undoStack.push(copyImage(currentImage));
            // Limit undo stack size to prevent memory issues
            if (undoStack.size() > 20) {
                undoStack.remove(0);
            }
        }
    }

    private BufferedImage copyImage(BufferedImage source) {
        if (source == null)
            return null;
        BufferedImage copy = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());
        copy.getGraphics().drawImage(source, 0, 0, null);
        return copy;
    }

    /**
     * Get the current image as a JavaFX Image.
     */
    public Image getCurrentImageFX() {
        if (currentImage != null) {
            return SwingFXUtils.toFXImage(currentImage, null);
        }
        return null;
    }

    /**
     * Get the current BufferedImage.
     */
    public BufferedImage getCurrentImage() {
        return currentImage;
    }

    /**
     * Set the current image (used for operations that modify the image directly).
     */
    public void setCurrentImage(BufferedImage image) {
        saveToUndoStack();
        this.currentImage = image;
        redoStack.clear();
    }

    public File getCurrentFile() {
        return currentFile;
    }

    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public boolean canRedo() {
        return !redoStack.isEmpty();
    }
}
