package com.photoviewer.image;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

/**
 * Manages the current image state, file operations, and undo/redo
 * functionality.
 */
public class ImageManager {
    private BufferedImage currentImage;
    private Image currentImageFX;
    private File currentFile;
    private Stack<BufferedImage> undoStack;
    private Stack<BufferedImage> redoStack;
    private List<File> directoryFiles = new ArrayList<>();
    private int currentIndex = -1;

    public ImageManager() {
        this.undoStack = new Stack<>();
        this.redoStack = new Stack<>();
    }

    /**
     * Load an image from a file.
     */
    public boolean loadImage(File file) {
        return loadImage(file, false);
    }

    /**
     * Load an image from a file, optionally clearing the undo/redo stacks.
     */
    public boolean loadImage(File file, boolean clearHistory) {
        try {
            BufferedImage image = ImageIO.read(file);
            if (image != null) {
                if (clearHistory) {
                    undoStack.clear();
                    redoStack.clear();
                } else {
                    saveToUndoStack();
                }
                this.currentImage = image;
                this.currentImageFX = null; // Clear cache
                this.currentFile = file;
                this.redoStack.clear();
                updateDirectoryFiles(file);
                if (clearHistory) {
                    System.gc(); // Clean up previous images
                }
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
            currentImageFX = null; // Clear cache
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
            currentImageFX = null; // Clear cache
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
            currentImageFX = null; // Clear cache
            redoStack.clear();
        }
    }

    /**
     * Rotate the current image 90 degrees to the right.
     */
    public void rotateImage90Right() {
        if (currentImage != null) {
            saveToUndoStack();
            currentImage = ImageOperations.rotate90Right(currentImage);
            currentImageFX = null; // Clear cache
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
            currentImageFX = null; // Clear cache
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
            currentImageFX = null; // Clear cache
        }
    }

    /**
     * Redo the last undone operation.
     */
    public void redo() {
        if (!redoStack.isEmpty()) {
            undoStack.push(copyImage(currentImage));
            currentImage = redoStack.pop();
            currentImageFX = null; // Clear cache
        }
    }

    private void saveToUndoStack() {
        if (currentImage != null) {
            undoStack.push(copyImage(currentImage));
            // Limit undo stack size to prevent memory issues
            if (undoStack.size() > 3) {
                undoStack.remove(0);
                System.gc(); // Hint cleanup when removing history
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
            if (currentImageFX == null) {
                currentImageFX = SwingFXUtils.toFXImage(currentImage, null);
            }
            return currentImageFX;
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
        this.currentImageFX = null; // Clear cache
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

    private void updateDirectoryFiles(File file) {
        File parent = file.getParentFile();
        if (parent != null && parent.isDirectory()) {
            File[] files = parent.listFiles((dir, name) -> {
                String lower = name.toLowerCase();
                return lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg")
                        || lower.endsWith(".gif") || lower.endsWith(".bmp") || lower.endsWith(".webp");
            });
            if (files != null) {
                directoryFiles = new ArrayList<>(Arrays.asList(files));
                Collections.sort(directoryFiles);
                currentIndex = directoryFiles.indexOf(file);
            }
        }
    }

    public boolean loadNextImage() {
        if (directoryFiles.size() > 1 && currentIndex < directoryFiles.size() - 1) {
            return loadImage(directoryFiles.get(currentIndex + 1), true);
        }
        return false;
    }

    public boolean loadPreviousImage() {
        if (directoryFiles.size() > 1 && currentIndex > 0) {
            return loadImage(directoryFiles.get(currentIndex - 1), true);
        }
        return false;
    }

    public File getNextFile() {
        if (directoryFiles.size() > 1 && currentIndex < directoryFiles.size() - 1) {
            return directoryFiles.get(currentIndex + 1);
        }
        return null;
    }

    public File getPreviousFile() {
        if (directoryFiles.size() > 1 && currentIndex > 0) {
            return directoryFiles.get(currentIndex - 1);
        }
        return null;
    }

    public List<File> getDirectoryFiles() {
        return directoryFiles;
    }
}
