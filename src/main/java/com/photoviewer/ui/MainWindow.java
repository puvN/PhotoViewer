package com.photoviewer.ui;

import com.photoviewer.image.ImageManager;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

/**
 * Main application window with menu bar, tool panel, image canvas, and AI chat
 * panel.
 */
public class MainWindow {
    private final Stage stage;
    private final BorderPane root;
    private final ImageCanvas imageCanvas;
    private final ToolPanel toolPanel;
    private final AIChatPanel aiChatPanel;
    private final ThumbnailBar thumbnailBar;
    private final ImageManager imageManager;

    public MainWindow(Stage stage) {
        this.stage = stage;
        this.imageManager = new ImageManager();
        this.root = new BorderPane();

        // Initialize components
        this.imageCanvas = new ImageCanvas(imageManager);
        this.toolPanel = new ToolPanel(imageCanvas, imageManager, this);
        this.aiChatPanel = new AIChatPanel(imageCanvas, imageManager);
        this.thumbnailBar = new ThumbnailBar(imageManager);

        setupUI();
        setupMenuBar();
    }

    private void setupUI() {
        root.setCenter(imageCanvas);
        root.setLeft(toolPanel);

        // Create hint bar
        Label hintLabel = new Label("R: Rotate | ↑/↓: Zoom | ←/→: Navigation | Ctrl+Z/Y: Undo/Redo");
        hintLabel.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 11px;");
        HBox hintBar = new HBox(hintLabel);
        hintBar.setAlignment(Pos.CENTER);
        hintBar.setPadding(new Insets(2, 10, 2, 10));
        hintBar.setStyle("-fx-background-color: #333333;");

        VBox bottomContainer = new VBox(thumbnailBar, hintBar);
        root.setBottom(bottomContainer);
        // AI panel is hidden by default - will be shown via toggle button

        // Create scene
        Scene scene = new Scene(root, 1200, 800);

        // Load CSS if available
        try {
            String css = getClass().getResource("/styles.css").toExternalForm();
            scene.getStylesheets().add(css);
        } catch (Exception e) {
            System.out.println("CSS file not found, using default styles");
        }

        // Add keyboard shortcuts
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.isControlDown()) {
                switch (event.getCode()) {
                    case Z:
                        // Undo
                        imageManager.undo();
                        imageCanvas.displayImage();
                        event.consume();
                        break;
                    case Y:
                        // Redo
                        imageManager.redo();
                        imageCanvas.displayImage();
                        event.consume();
                        break;
                    case O:
                        // Open
                        openImage();
                        event.consume();
                        break;
                    case S:
                        // Save
                        saveImage();
                        event.consume();
                        break;
                    case C:
                        // Copy to clipboard
                        imageCanvas.copyToClipboard();
                        event.consume();
                        break;
                }
            } else {
                // Single key shortcuts
                switch (event.getCode()) {
                    case RIGHT:
                        if (imageManager.loadNextImage()) {
                            imageCanvas.displayImage();
                            imageCanvas.fitToWindow();
                            updateTitle();
                            thumbnailBar.updateThumbnails();
                        }
                        event.consume();
                        break;
                    case LEFT:
                        if (imageManager.loadPreviousImage()) {
                            imageCanvas.displayImage();
                            imageCanvas.fitToWindow();
                            updateTitle();
                            thumbnailBar.updateThumbnails();
                        }
                        event.consume();
                        break;
                    case R:
                        imageManager.rotateImage90Right();
                        imageCanvas.displayImage();
                        imageCanvas.fitToWindow();
                        event.consume();
                        break;
                    case UP:
                        imageCanvas.zoomIn();
                        event.consume();
                        break;
                    case DOWN:
                        imageCanvas.zoomOut();
                        event.consume();
                        break;
                }
            }
        });

        // Set application icon
        try {
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/icon.png")));
        } catch (Exception e) {
            System.out.println("Favicon not found, using default");
        }

        stage.setScene(scene);
        stage.setTitle("PhotoViewer");
        stage.setMinWidth(800);
        stage.setMinHeight(600);
    }

    private void setupMenuBar() {
        MenuBar menuBar = new MenuBar();

        // File Menu
        Menu fileMenu = new Menu("File");
        MenuItem openItem = new MenuItem("Open Image...");
        openItem.setOnAction(e -> openImage());

        MenuItem saveItem = new MenuItem("Save");
        saveItem.setOnAction(e -> imageManager.saveImage());

        MenuItem saveAsItem = new MenuItem("Save As...");
        saveAsItem.setOnAction(e -> imageManager.saveImageAs());

        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setOnAction(e -> stage.close());

        fileMenu.getItems().addAll(openItem, saveItem, saveAsItem, new SeparatorMenuItem(), exitItem);

        // Edit Menu
        Menu editMenu = new Menu("Edit");
        MenuItem undoItem = new MenuItem("Undo");
        undoItem.setOnAction(e -> imageManager.undo());

        MenuItem redoItem = new MenuItem("Redo");
        redoItem.setOnAction(e -> imageManager.redo());

        MenuItem resizeItem = new MenuItem("Resize Image...");
        resizeItem.setOnAction(e -> showResizeDialog());

        editMenu.getItems().addAll(undoItem, redoItem, new SeparatorMenuItem(), resizeItem);

        // View Menu
        Menu viewMenu = new Menu("View");
        MenuItem zoomInItem = new MenuItem("Zoom In");
        zoomInItem.setOnAction(e -> imageCanvas.zoomIn());

        MenuItem zoomOutItem = new MenuItem("Zoom Out");
        zoomOutItem.setOnAction(e -> imageCanvas.zoomOut());

        MenuItem fitToWindowItem = new MenuItem("Fit to Window");
        fitToWindowItem.setOnAction(e -> imageCanvas.fitToWindow());

        CheckMenuItem toggleAIPanelItem = new CheckMenuItem("Show AI Chat");
        toggleAIPanelItem.setSelected(false); // Hidden by default
        toggleAIPanelItem.setOnAction(e -> {
            if (toggleAIPanelItem.isSelected()) {
                root.setRight(aiChatPanel);
            } else {
                root.setRight(null);
            }
        });

        viewMenu.getItems().addAll(zoomInItem, zoomOutItem, fitToWindowItem,
                new SeparatorMenuItem(), toggleAIPanelItem);

        // AI Menu
        Menu aiMenu = new Menu("AI");
        MenuItem configureAPIItem = new MenuItem("Configure API Keys...");
        configureAPIItem.setOnAction(e -> aiChatPanel.showAPIConfiguration());

        aiMenu.getItems().add(configureAPIItem);

        menuBar.getMenus().addAll(fileMenu, editMenu, viewMenu, aiMenu);
        root.setTop(menuBar);
    }

    private void openImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp", "*.webp"),
                new FileChooser.ExtensionFilter("PNG", "*.png"),
                new FileChooser.ExtensionFilter("JPEG", "*.jpg", "*.jpeg"),
                new FileChooser.ExtensionFilter("GIF", "*.gif"),
                new FileChooser.ExtensionFilter("BMP", "*.bmp"),
                new FileChooser.ExtensionFilter("WebP", "*.webp"));

        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            if (imageManager.loadImage(file, true)) {
                imageCanvas.displayImage();
                imageCanvas.fitToWindow();
                updateTitle();
                thumbnailBar.updateThumbnails();
            }
        }
    }

    private void updateTitle() {
        File file = imageManager.getCurrentFile();
        if (file != null) {
            stage.setTitle("PhotoViewer - " + file.getName());
        } else {
            stage.setTitle("PhotoViewer");
        }
    }

    private void showResizeDialog() {
        if (imageManager.getCurrentImage() == null) {
            showAlert("No Image", "Please open an image first.");
            return;
        }

        Dialog<int[]> dialog = new Dialog<>();
        dialog.setTitle("Resize Image");
        dialog.setHeaderText("Enter new dimensions:");

        ButtonType resizeButtonType = new ButtonType("Resize", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(resizeButtonType, ButtonType.CANCEL);

        TextField widthField = new TextField(String.valueOf(imageManager.getCurrentImage().getWidth()));
        TextField heightField = new TextField(String.valueOf(imageManager.getCurrentImage().getHeight()));

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Width:"), 0, 0);
        grid.add(widthField, 1, 0);
        grid.add(new Label("Height:"), 0, 1);
        grid.add(heightField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == resizeButtonType) {
                try {
                    int width = Integer.parseInt(widthField.getText());
                    int height = Integer.parseInt(heightField.getText());
                    return new int[] { width, height };
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(dimensions -> {
            if (dimensions != null && dimensions.length == 2) {
                imageManager.resizeImage(dimensions[0], dimensions[1]);
                imageCanvas.displayImage();
            }
        });
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void saveImage() {
        imageManager.saveImage();
    }

    public void show() {
        stage.show();
    }

    /**
     * Toggle the AI chat panel visibility.
     */
    public void toggleAIPanel() {
        if (root.getRight() == null) {
            root.setRight(aiChatPanel);
        } else {
            root.setRight(null);
        }
    }
}
