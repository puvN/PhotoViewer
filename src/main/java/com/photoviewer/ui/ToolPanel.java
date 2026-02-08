package com.photoviewer.ui;

import com.photoviewer.image.ImageManager;
import com.photoviewer.tools.PencilTool;
import com.photoviewer.tools.ScissorsTool;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/**
 * Tool panel with buttons for all image manipulation tools.
 */
public class ToolPanel extends VBox {
    private final ImageCanvas canvas;
    private final ImageManager imageManager;
    private final MainWindow mainWindow;

    private ScissorsTool scissorsTool;
    private PencilTool pencilTool;

    private ToggleGroup toolGroup;

    // Pencil tool UI controls (shown/hidden dynamically)
    private ColorPicker colorPicker;
    private Label brushLabel;
    private Slider brushSlider;

    public ToolPanel(ImageCanvas canvas, ImageManager imageManager, MainWindow mainWindow) {
        this.canvas = canvas;
        this.imageManager = imageManager;
        this.mainWindow = mainWindow;

        initializeTools();
        setupUI();
    }

    private void initializeTools() {
        scissorsTool = new ScissorsTool(imageManager);
        pencilTool = new PencilTool(canvas);
    }

    private void setupUI() {
        setSpacing(10);
        setPadding(new Insets(10));
        setAlignment(Pos.TOP_CENTER);
        setStyle("-fx-background-color: #3c3c3c; -fx-min-width: 80px;");

        toolGroup = new ToggleGroup();

        // Title
        Label title = new Label("Tools");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");

        // Zoom controls
        Button zoomInBtn = createButton("Zoom +");
        zoomInBtn.setOnAction(e -> canvas.zoomIn());

        Button zoomOutBtn = createButton("Zoom -");
        zoomOutBtn.setOnAction(e -> canvas.zoomOut());

        Button fitBtn = createButton("Fit");
        fitBtn.setOnAction(e -> canvas.fitToWindow());

        Separator sep1 = new Separator();

        ToggleButton scissorsBtn = createToolButton("Scissors");
        scissorsBtn.setToggleGroup(toolGroup);
        scissorsBtn.setOnAction(e -> {
            if (scissorsBtn.isSelected()) {
                // Apply any pending drawing first
                if (canvas.getDrawingLayer() != null) {
                    imageManager.applyDrawing(canvas.getDrawingLayer());
                    canvas.clearDrawingLayer();
                    canvas.displayImage();
                }
                canvas.setTool(scissorsTool);
                canvas.setCanvasCursor(javafx.scene.Cursor.CROSSHAIR);
            } else {
                canvas.setTool(null);
                canvas.setCanvasCursor(javafx.scene.Cursor.DEFAULT);
            }
        });

        Separator sep2 = new Separator();

        ToggleButton pencilBtn = createToolButton("Pencil");
        pencilBtn.setToggleGroup(toolGroup);
        pencilBtn.setOnAction(e -> {
            if (pencilBtn.isSelected()) {
                canvas.setTool(pencilTool);
                canvas.setCanvasCursor(javafx.scene.Cursor.CROSSHAIR);
                // Show color picker and brush controls
                if (!getChildren().contains(colorPicker)) {
                    int pencilIndex = getChildren().indexOf(pencilBtn);
                    getChildren().add(pencilIndex + 1, colorPicker);
                    getChildren().add(pencilIndex + 2, brushLabel);
                    getChildren().add(pencilIndex + 3, brushSlider);
                }
            } else {
                // Apply any pending drawing before deselecting
                if (canvas.getDrawingLayer() != null) {
                    imageManager.applyDrawing(canvas.getDrawingLayer());
                    canvas.clearDrawingLayer();
                    canvas.displayImage();
                }
                canvas.setTool(null);
                canvas.setCanvasCursor(javafx.scene.Cursor.DEFAULT);
                // Hide color picker and brush controls
                getChildren().removeAll(colorPicker, brushLabel, brushSlider);
            }
        });

        // Color picker (will be added dynamically when pencil is selected)
        colorPicker = new ColorPicker(Color.RED);
        colorPicker.setMaxWidth(Double.MAX_VALUE);
        colorPicker.setOnAction(e -> pencilTool.setColor(colorPicker.getValue()));

        // Brush size (will be added dynamically when pencil is selected)
        brushLabel = new Label("Size:");
        brushLabel.setStyle("-fx-text-fill: white; -fx-font-size: 10px;");

        brushSlider = new Slider(1, 20, 3);
        brushSlider.setShowTickMarks(true);
        brushSlider.setShowTickLabels(false);
        brushSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            pencilTool.setBrushSize(newVal.doubleValue());
        });

        Separator sep3 = new Separator();

        // AI Chat toggle button
        Button aiChatBtn = createButton("AI Chat");
        aiChatBtn.setStyle("-fx-background-color: #0066cc; -fx-text-fill: white; -fx-font-size: 11px;");
        aiChatBtn.setOnAction(e -> mainWindow.toggleAIPanel());

        // Add all components
        getChildren().addAll(
                title,
                zoomInBtn, zoomOutBtn, fitBtn,
                sep1,
                scissorsBtn,
                sep2,
                pencilBtn,
                sep3,
                aiChatBtn);
    }

    private Button createButton(String text) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle("-fx-background-color: #555; -fx-text-fill: white; -fx-font-size: 11px;");
        return btn;
    }

    private ToggleButton createToolButton(String text) {
        ToggleButton btn = new ToggleButton(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle("-fx-background-color: #555; -fx-text-fill: white; -fx-font-size: 11px;");
        return btn;
    }
}
