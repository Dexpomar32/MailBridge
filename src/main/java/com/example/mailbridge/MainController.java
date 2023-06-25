package com.example.mailbridge;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;

public class MainController {
    @FXML
    private Button AolButton, GmailButton, OutlookButton, YahooButton, YandexButton, iCloudButton, OtherMailButton;

    @FXML
    private Image AolImage, GmailImage, OutlookImage, YahooImage, YandexImage, iCloudImage;

    @FXML
    private Label MailNameLabel;

    private String imapAddress, imageURL;

    private boolean HostVisibility;

    private final DataSingleton data = DataSingleton.getInstance();

    @FXML
    private void initialize() {
        showMailName();
        onClick();
    }

    private void showMailName() {
        GmailButton.setOnMouseEntered(event -> MailNameLabel.setText("Gmail"));

        GmailButton.setOnMouseExited(event -> MailNameLabel.setText(""));

        AolButton.setOnMouseEntered(event -> MailNameLabel.setText("AOL"));

        AolButton.setOnMouseExited(event -> MailNameLabel.setText(""));

        iCloudButton.setOnMouseEntered(event -> MailNameLabel.setText("iCloud"));

        iCloudButton.setOnMouseExited(event -> MailNameLabel.setText(""));

        OutlookButton.setOnMouseEntered(event -> MailNameLabel.setText("Outlook"));

        OutlookButton.setOnMouseExited(event -> MailNameLabel.setText(""));

        YahooButton.setOnMouseEntered(event -> MailNameLabel.setText("Yahoo"));

        YahooButton.setOnMouseExited(event -> MailNameLabel.setText(""));

        YandexButton.setOnMouseEntered(event -> MailNameLabel.setText("Yandex"));

        YandexButton.setOnMouseExited(event -> MailNameLabel.setText(""));
    }

    private void showAuthentication() {
        try {
            data.setHostVisibility(HostVisibility);
            data.setImapAddress(imapAddress);
            data.setImageURL(imageURL);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("Authentication.fxml"));
            Parent root = loader.load();

            Scene scene = GmailButton.getScene();
            Pane rootContainer = (Pane) scene.getRoot();

            rootContainer.getChildren().removeIf(node -> node != rootContainer.getChildren().get(0));
            rootContainer.getChildren().add(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onClick() {
        AolButton.setOnAction(actionEvent -> {
            imapAddress = "imap.aol.com";
            imageURL = AolImage.getUrl();

            showAuthentication();
        });

        GmailButton.setOnAction(actionEvent -> {
            imapAddress = "imap.gmail.com";
            imageURL = GmailImage.getUrl();

            showAuthentication();
        });

        OutlookButton.setOnAction(actionEvent -> {
            imapAddress = "imap-mail.outlook.com";
            imageURL = OutlookImage.getUrl();

            showAuthentication();
        });

        YahooButton.setOnAction(actionEvent -> {
            imapAddress = "imap.mail.yahoo.com";
            imageURL = YahooImage.getUrl();

            showAuthentication();
        });

        YandexButton.setOnAction(actionEvent ->  {
            imapAddress = "imap.yandex.ru";
            imageURL = YandexImage.getUrl();

            showAuthentication();
        });

        iCloudButton.setOnAction(actionEvent -> {
            imapAddress = "imap.mail.me.com";
            imageURL = iCloudImage.getUrl();

            showAuthentication();
        });

        OtherMailButton.setOnAction(actionEvent -> {
            HostVisibility = true;
            imageURL = "C:\\Users\\Vlad\\IdeaProjects\\Mail Bridge\\src\\main\\resources\\images\\mail.png";

            showAuthentication();
        });
    }
}