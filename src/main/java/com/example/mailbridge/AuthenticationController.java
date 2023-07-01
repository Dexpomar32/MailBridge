package com.example.mailbridge;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

public class AuthenticationController {

    @FXML
    private Button LoginButton;

    @FXML
    private TextField UsernameTextField, HostTextField;

    @FXML
    private PasswordField PasswordField;

    @FXML
    private HBox HostHBox;

    @FXML
    private ImageView MailImageView;

    @FXML
    private CheckBox SSLCheckBox;

    private final DataSingleton data = DataSingleton.getInstance();

    @FXML
    private void initialize() {
        setImage();
        HostContainerVisibility();
        openSaveMbox(LoginButton);
    }

    private void openSaveMbox(Button button) {
        LoginButton.setOnAction(actionEvent -> {
            if (UsernameTextField.getText().isEmpty() || PasswordField.getText().isEmpty() || (data.getHostVisibility() && HostTextField.getText().isEmpty())) {
                showAlert();
            } else {
                try {
                    data.setUsername(UsernameTextField.getText());
                    data.setPassword(PasswordField.getText());
                    data.setSSL(SSLCheckBox.selectedProperty().get());

                    if (data.getHostVisibility())
                        data.setImapAddress(HostTextField.getText());

                    FXMLLoader loader = new FXMLLoader(getClass().getResource("SaveMessages.fxml"));
                    Parent root = loader.load();
                    Scene scene = button.getScene();
                    Pane rootContainer = (Pane) scene.getRoot();

                    rootContainer.getChildren().removeIf(node -> node != rootContainer.getChildren().get(0));
                    rootContainer.getChildren().add(root);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void showAlert() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText("Please fill all fields with data.");
        alert.showAndWait();
    }

    private void HostContainerVisibility() {
        boolean hostVisibility = data.getHostVisibility();
        HostHBox.setVisible(hostVisibility);
    }

    private void setImage() {
        Image image = new Image(data.getImageURL());
        MailImageView.setImage(image);
    }
}
