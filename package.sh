#!/bin/bash

# PhotoViewer Native Packaging Script
# This script creates native installers for Windows, macOS, and Linux

# Configuration
APP_NAME="PhotoViewer"
APP_VERSION="1.0.0"
MAIN_CLASS="com.photoviewer.AppLauncher"
MAIN_JAR="target/photoviewer-1.0.0.jar"

echo "Building PhotoViewer native installer..."

# Step 1: Build the application with Maven
echo "Step 1: Building application with Maven..."
mvn clean package
if [ $? -ne 0 ]; then
    echo "Maven build failed!"
    exit 1
fi

# Step 2: Detect OS and create appropriate installer
OS_TYPE=$(uname -s)

case "$OS_TYPE" in
    MINGW*|MSYS*|CYGWIN*|Windows*)
        echo "Step 2: Creating Windows installer..."
        
        # Add local WiX tools to PATH if they exist
        if [ -d "wix-tools" ]; then
            echo "Using local WiX tools from wix-tools directory"
            export PATH="$PATH:$(pwd)/wix-tools"
        fi

        jpackage \
            --input target \
            --name "$APP_NAME" \
            --main-jar photoviewer-1.0.0.jar \
            --main-class "$MAIN_CLASS" \
            --type exe \
            --app-version "$APP_VERSION" \
            --vendor "PhotoViewer" \
            --description "Cross-platform image viewer and editor with AI integration" \
            --file-associations windows-file-associations.properties \
            --win-menu \
            --win-shortcut \
            --win-dir-chooser
        echo "Windows installer created successfully!"
        ;;
    
    Darwin*)
        echo "Step 2: Creating macOS installer..."
        jpackage \
            --input target \
            --name "$APP_NAME" \
            --main-jar photoviewer-1.0.0.jar \
            --main-class "$MAIN_CLASS" \
            --type dmg \
            --app-version "$APP_VERSION" \
            --vendor "PhotoViewer" \
            --description "Cross-platform image viewer and editor with AI integration" \
            --file-associations macos-file-associations.properties \
            --mac-package-name "$APP_NAME"
        echo "macOS installer created successfully!"
        ;;
    
    Linux*)
        echo "Step 2: Creating Linux installers..."
        # Create DEB package
        jpackage \
            --input target \
            --name "$APP_NAME" \
            --main-jar photoviewer-1.0.0.jar \
            --main-class "$MAIN_CLASS" \
            --type deb \
            --app-version "$APP_VERSION" \
            --vendor "PhotoViewer" \
            --description "Cross-platform image viewer and editor with AI integration" \
            --file-associations linux-file-associations.properties \
            --linux-shortcut
        
        # Create RPM package
        jpackage \
            --input target \
            --name "$APP_NAME" \
            --main-jar photoviewer-1.0.0.jar \
            --main-class "$MAIN_CLASS" \
            --type rpm \
            --app-version "$APP_VERSION" \
            --vendor "PhotoViewer" \
            --description "Cross-platform image viewer and editor with AI integration" \
            --file-associations linux-file-associations.properties \
            --linux-shortcut
        echo "Linux installers created successfully!"
        ;;
    
    *)
        echo "Unknown operating system: $OS_TYPE"
        exit 1
        ;;
esac

echo "Packaging complete!"
