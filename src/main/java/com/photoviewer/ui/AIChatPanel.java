package com.photoviewer.ui;

import com.photoviewer.ai.AIClient;
import com.photoviewer.ai.OpenAIProvider;
import com.photoviewer.ai.AnthropicProvider;
import com.photoviewer.ai.GeminiProvider;
import com.photoviewer.image.ImageManager;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * AI Chat panel for interacting with AI models to manipulate images.
 */
public class AIChatPanel extends VBox {
    private final ImageCanvas canvas;
    private final ImageManager imageManager;

    private TextArea chatHistory;
    private TextField inputField;
    private ComboBox<String> providerSelector;
    private Button sendButton;

    private AIClient currentAIClient;
    private String openAIKey = "";
    private String anthropicKey = "";
    private String geminiKey = "";

    public AIChatPanel(ImageCanvas canvas, ImageManager imageManager) {
        this.canvas = canvas;
        this.imageManager = imageManager;

        setupUI();
    }

    private void setupUI() {
        setSpacing(10);
        setPadding(new Insets(10));
        setStyle("-fx-background-color: #2c2c2c; -fx-min-width: 300px; -fx-max-width: 400px;");

        // Title
        Label title = new Label("AI Assistant");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");

        // Provider selector
        HBox providerBox = new HBox(5);
        providerBox.setAlignment(Pos.CENTER_LEFT);
        Label providerLabel = new Label("Provider:");
        providerLabel.setStyle("-fx-text-fill: white; -fx-font-size: 11px;");

        providerSelector = new ComboBox<>();
        providerSelector.getItems().addAll("OpenAI", "Anthropic", "Google Gemini");
        providerSelector.setValue("OpenAI");
        providerSelector.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(providerSelector, Priority.ALWAYS);

        providerBox.getChildren().addAll(providerLabel, providerSelector);

        // Chat history
        chatHistory = new TextArea();
        chatHistory.setEditable(false);
        chatHistory.setWrapText(true);
        chatHistory.setStyle("-fx-control-inner-background: #1e1e1e; -fx-text-fill: white;");
        chatHistory.setPrefHeight(400);
        VBox.setVgrow(chatHistory, Priority.ALWAYS);

        // Input area
        inputField = new TextField();
        inputField.setPromptText("Ask AI to edit the image...");
        inputField.setStyle("-fx-background-color: #3c3c3c; -fx-text-fill: white;");
        inputField.setOnAction(e -> sendMessage());

        sendButton = new Button("Send");
        sendButton.setOnAction(e -> sendMessage());
        sendButton.setStyle("-fx-background-color: #0066cc; -fx-text-fill: white;");

        HBox inputBox = new HBox(5);
        inputBox.setAlignment(Pos.CENTER);
        HBox.setHgrow(inputField, Priority.ALWAYS);
        inputBox.getChildren().addAll(inputField, sendButton);

        // Info label
        Label infoLabel = new Label("Configure API keys in AI menu");
        infoLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 10px;");
        infoLabel.setWrapText(true);

        getChildren().addAll(title, providerBox, chatHistory, inputBox, infoLabel);

        // Add welcome message
        addMessage("AI", "Hello! I can help you edit images. Try commands like:\n" +
                "- 'Resize to 800x600'\n" +
                "- 'Describe this image'\n" +
                "- 'Crop to center'\n" +
                "- 'What's in this image?'");
    }

    private void sendMessage() {
        String message = inputField.getText().trim();
        if (message.isEmpty())
            return;

        addMessage("You", message);
        inputField.clear();

        // Check if API key is configured
        String provider = providerSelector.getValue();
        if (!isAPIKeyConfigured(provider)) {
            addMessage("System", "Please configure your " + provider + " API key first (AI > Configure API Keys)");
            return;
        }

        // Process message in background
        sendButton.setDisable(true);
        new Thread(() -> {
            try {
                String response = processAIMessage(message);
                Platform.runLater(() -> {
                    addMessage("AI", response);
                    sendButton.setDisable(false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    addMessage("Error", "Failed to get AI response: " + e.getMessage());
                    sendButton.setDisable(false);
                });
            }
        }).start();
    }

    private String processAIMessage(String message) {
        // Initialize AI client if needed
        if (currentAIClient == null) {
            initializeAIClient();
        }

        if (currentAIClient == null) {
            return "AI client not initialized. Please check your API key.";
        }

        // Send message to AI
        try {
            return currentAIClient.sendMessage(message, imageManager.getCurrentImage());
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private void initializeAIClient() {
        String provider = providerSelector.getValue();

        switch (provider) {
            case "OpenAI":
                if (!openAIKey.isEmpty()) {
                    currentAIClient = new OpenAIProvider(openAIKey);
                }
                break;
            case "Anthropic":
                if (!anthropicKey.isEmpty()) {
                    currentAIClient = new AnthropicProvider(anthropicKey);
                }
                break;
            case "Google Gemini":
                if (!geminiKey.isEmpty()) {
                    currentAIClient = new GeminiProvider(geminiKey);
                }
                break;
        }
    }

    private boolean isAPIKeyConfigured(String provider) {
        switch (provider) {
            case "OpenAI":
                return !openAIKey.isEmpty();
            case "Anthropic":
                return !anthropicKey.isEmpty();
            case "Google Gemini":
                return !geminiKey.isEmpty();
            default:
                return false;
        }
    }

    private void addMessage(String sender, String message) {
        String timestamp = java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
        chatHistory.appendText(String.format("[%s] %s: %s\n\n", timestamp, sender, message));
    }

    public void showAPIConfiguration() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Configure API Keys");
        dialog.setHeaderText("Enter your API keys for AI providers:");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        PasswordField openAIField = new PasswordField();
        openAIField.setPromptText("sk-...");
        openAIField.setText(openAIKey);

        PasswordField anthropicField = new PasswordField();
        anthropicField.setPromptText("sk-ant-...");
        anthropicField.setText(anthropicKey);

        PasswordField geminiField = new PasswordField();
        geminiField.setPromptText("AIza...");
        geminiField.setText(geminiKey);

        grid.add(new Label("OpenAI API Key:"), 0, 0);
        grid.add(openAIField, 1, 0);
        grid.add(new Label("Anthropic API Key:"), 0, 1);
        grid.add(anthropicField, 1, 1);
        grid.add(new Label("Google Gemini API Key:"), 0, 2);
        grid.add(geminiField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                openAIKey = openAIField.getText();
                anthropicKey = anthropicField.getText();
                geminiKey = geminiField.getText();
                currentAIClient = null; // Reset to reinitialize with new keys
                addMessage("System", "API keys updated successfully");
            }
            return null;
        });

        dialog.showAndWait();
    }
}
