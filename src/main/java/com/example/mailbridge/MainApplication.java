package com.example.mailbridge;

import javafx.animation.ScaleTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import java.io.IOException;
import java.util.Objects;

public class MainApplication extends Application {
    private double xOffset = 0;
    private double yOffset = 0;
    private final DataSingleton data = DataSingleton.getInstance();

    @Override
    public void start(Stage stage) throws IOException {
        stage.initStyle(StageStyle.UNDECORATED);

        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("Main.fxml"));
        Parent root = fxmlLoader.load();

        StackPane titleBar = new StackPane();
        titleBar.setStyle("-fx-background-color: #000000;");
        titleBar.setPrefHeight(40);

        Label titleLabel = new Label("Mail Bridge");
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setStyle("-fx-font-size: 14px;");
        titleLabel.setPadding(new Insets(0, 10, 0, 10));

        Button closeButton = createTitleBarButton("/images/ico1.png", Color.RED);
        closeButton.setOnAction(event -> {
            Platform.exit();
            System.exit(0);
        });

        Button maximizeButton = createTitleBarButton("/images/ico2.png", Color.GREEN);
        // maximizeButton.setOnAction(event -> stage.setMaximized(!stage.isMaximized()));

        Button minimizeButton = createTitleBarButton("/images/ico3.png", Color.YELLOW);
        minimizeButton.setOnAction(event -> stage.setIconified(true));

        HBox buttonContainer = new HBox(minimizeButton, maximizeButton, closeButton);
        buttonContainer.setAlignment(Pos.CENTER_RIGHT);
        buttonContainer.setSpacing(5);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        titleBar.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        titleBar.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });

        titleBar.getChildren().addAll(titleLabel, spacer, buttonContainer);

        VBox mainContainer = new VBox();
        mainContainer.getChildren().addAll(titleBar, root);
        VBox.setVgrow(root, Priority.ALWAYS);

        Scene scene = new Scene(mainContainer);

        stage.setScene(scene);
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/MailBridgeIcon.png"))));
        stage.setResizable(false);
        stage.setTitle("File Sort");
        stage.show();
    }

    private Button createTitleBarButton(String imagePath, Color color) {
        Button button = new Button();
        ImageView imageView = new ImageView((new Image(Objects.requireNonNull(getClass().getResourceAsStream(imagePath)))));
        imageView.setFitWidth(16);
        imageView.setFitHeight(16);
        button.setGraphic(imageView);
        button.setStyle("-fx-background-color: transparent; -fx-text-fill: white;");
        button.setMinSize(40, 40);

        button.setOnMouseEntered(event -> {
            button.setStyle("-fx-background-color: " + toRGBCode(color) + "; -fx-text-fill: white;");
            scaleButtonAnimation(button, 1.2);
        });
        button.setOnMouseExited(event -> {
            button.setStyle("-fx-background-color: transparent; -fx-text-fill: white;");
            scaleButtonAnimation(button, 1.0);
        });

        return button;
    }

    private void scaleButtonAnimation(Button button, double scale) {
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(100), button);
        scaleTransition.setToX(scale);
        scaleTransition.setToY(scale);
        scaleTransition.play();
    }

    private String toRGBCode(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        data.stopTask();

        Platform.exit();
        System.exit(0);
    }

    public static void main(String[] args) {
        launch();
    }
}