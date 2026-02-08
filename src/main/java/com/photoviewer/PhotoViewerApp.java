package com.photoviewer;

import com.photoviewer.ui.MainWindow;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Main entry point for the PhotoViewer application.
 * Cross-platform image viewer and editor with AI integration.
 */
public class PhotoViewerApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        MainWindow mainWindow = new MainWindow(primaryStage);
        mainWindow.show();
    }

    @Override
    public void stop() {
        // Cleanup resources when application closes
        System.out.println("PhotoViewer shutting down...");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
