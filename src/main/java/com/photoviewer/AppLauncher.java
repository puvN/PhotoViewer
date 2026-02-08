package com.photoviewer;

/**
 * Launcher class to bypass JavaFX main class checks.
 * This class does NOT extend javafx.application.Application.
 */
public class AppLauncher {
    public static void main(String[] args) {
        PhotoViewerApp.main(args);
    }
}
