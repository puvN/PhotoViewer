package com.photoviewer.ui;

import com.photoviewer.image.ImageManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.io.File;

/**
 * UI component that displays thumbnail previews of the previous, current, and
 * next images.
 */
public class ThumbnailBar extends HBox {
    private final ImageManager imageManager;
    private final ImageView prevView;
    private final ImageView currView;
    private final ImageView nextView;

    public ThumbnailBar(ImageManager imageManager) {
        this.imageManager = imageManager;

        setSpacing(20);
        setPadding(new Insets(10));
        setAlignment(Pos.CENTER);
        setStyle("-fx-background-color: #2b2b2b; -fx-min-height: 120px;");

        prevView = createThumbnailView();
        currView = createThumbnailView();
        currView.setStyle("-fx-border-color: #00ccff; -fx-border-width: 2px;");
        nextView = createThumbnailView();

        getChildren().addAll(
                createLabeledView("Previous", prevView),
                createLabeledView("Current", currView),
                createLabeledView("Next", nextView));

        updateThumbnails();
    }

    private ImageView createThumbnailView() {
        ImageView iv = new ImageView();
        iv.setFitWidth(100);
        iv.setFitHeight(80);
        iv.setPreserveRatio(true);
        iv.setSmooth(true);
        return iv;
    }

    private VBox createLabeledView(String text, ImageView iv) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 10px;");
        VBox box = new VBox(5, label, iv);
        box.setAlignment(Pos.CENTER);
        return box;
    }

    public void updateThumbnails() {
        File prev = imageManager.getPreviousFile();
        File curr = imageManager.getCurrentFile();
        File next = imageManager.getNextFile();

        loadThumbnail(prevView, prev);
        loadThumbnail(currView, curr);
        loadThumbnail(nextView, next);
    }

    private void loadThumbnail(ImageView iv, File file) {
        if (file != null) {
            // Background loading to prevent UI freeze
            Image thumb = new Image(file.toURI().toString(), 100, 80, true, true, true);
            iv.setImage(thumb);
            iv.setVisible(true);
        } else {
            iv.setImage(null);
            iv.setVisible(false);
        }
    }
}
