package com.example.mailbridge;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.DirectoryChooser;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.Objects;
import java.util.Properties;
import javax.mail.*;
import javafx.scene.control.Alert.AlertType;

public class SaveMessagesController {
    @FXML
    public ToggleGroup FolderType;

    @FXML
    private Button ChangeURLButton;

    @FXML
    private TextField FolderTextField, URLTextField;

    @FXML
    private ImageView MailImageView;

    @FXML
    private ComboBox<String> ExtensionComboBox;

    @FXML
    private ProgressBar MessagesSavingProgress;

    @FXML
    private Label PercentsLabel;

    @FXML
    private RadioButton DefaultRadioButton, CustomRadioButton;

    private final DataSingleton data = DataSingleton.getInstance();

    Path desktopPath = Paths.get(System.getProperty("user.home"), "Desktop");

    private String tag = "", FolderName, URL, host, username, password, FilePath;

    private int port, savedMessagesCount;

    private boolean Type;

    @FXML
    private void initialize() {
        URLTextField.setText(desktopPath.toString());
        MessagesSavingProgress.setVisible(false);

        addOptions();
        setMailImageView();
        changeURL();
    }

    @FXML
    public void SaveButton() {
        interfaceOperation();
        getMailData();

        Task<Void> newTask = new Task<>() {
            @Override
            protected Void call() {
                saveMessages();
                return null;
            }
        };

        data.startTask(newTask);
    }

    private void changeURL() {
        DirectoryChooser directoryChooser = new DirectoryChooser();

        ChangeURLButton.setOnAction(actionEvent -> {
            directoryChooser.setTitle("Choice Folder");
            directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));
            File selectedDirectory = directoryChooser.showDialog(ChangeURLButton.getScene().getWindow());
            if (selectedDirectory != null) {
                URLTextField.setText(selectedDirectory.getAbsolutePath());
            }
        });
    }

    private void interfaceOperation() {
        FolderName = FolderTextField.getText();
        URL = URLTextField.getText();

        if (DefaultRadioButton.isSelected())
            Type = false;
        else if (CustomRadioButton.isSelected())
            Type = true;
    }

    private void getMailData() {
        setTag();

        host = data.getImapAddress();
        username = data.getUsername();
        password = data.getPassword();

        if (!Objects.equals(tag, ""))
            FilePath = URL + "\\" + FolderName.substring(FolderName.lastIndexOf("/") + 1);
        else FilePath = URL + "\\" + FolderName;

        if (data.getSSL())
            port = 993;
        else
            port = 143;
    }

    private void saveMbox() {
        try {
            MessagesSavingProgress.setVisible(true);

            Properties properties = new Properties();
            properties.setProperty("mail.store.protocol", "imaps");
            properties.setProperty("mail.imap.port", String.valueOf(port));

            Session session = Session.getInstance(properties);
            Store store = session.getStore("imaps");
            store.connect(host, username, password);

            Folder emailFolder;

            if (!Type)
                emailFolder = store.getFolder(tag + FolderName);
            else emailFolder = store.getFolder(FolderName);

            assert emailFolder != null;
            emailFolder.open(Folder.READ_ONLY);

            Message[] messages = emailFolder.getMessages();
            savedMessagesCount = messages.length;

            BufferedWriter mboxWriter = new BufferedWriter(new FileWriter(FilePath + ".mbox"));

            double progress = 0.0;
            Platform.runLater(() -> MessagesSavingProgress.setProgress(0.0));

            for (Message message : messages) {

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                message.writeTo(outputStream);

                mboxWriter.write("From - " + message.getFrom()[0].toString() + "\n");
                mboxWriter.write("To - " + message.getAllRecipients()[0].toString() + "\n");
                mboxWriter.write("Subject - " + message.getSubject() + "\n");
                mboxWriter.write(outputStream.toString());
                mboxWriter.write("\n\n");

                progress += 1.0 / savedMessagesCount;
                final double currentProgress = progress;

                Platform.runLater(() -> MessagesSavingProgress.setProgress(currentProgress));

                DecimalFormat decimalFormat = new DecimalFormat("0");
                String progressText = decimalFormat.format(currentProgress * 100) + "%";

                Platform.runLater(() -> PercentsLabel.setText(progressText));
            }

            mboxWriter.close();
            emailFolder.close(false);
            store.close();

            showAlert(AlertType.INFORMATION, "Success", " Mbox file saved successfully. Saved messages: " + savedMessagesCount);
            MessagesSavingProgress.setProgress(0.0);
        } catch (MessagingException | IOException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Error", "An error occurred while saving Mbox file.");
            MessagesSavingProgress.setProgress(0.0);
        }

        MessagesSavingProgress.setVisible(false);
    }

    private void saveMIME() {
        String host = data.getImapAddress();
        String username = data.getUsername();
        String password = data.getPassword();
        String mboxFilePath = URLTextField.getText() + "\\" + FolderTextField.getText() + ".mime";
        int port;

        if (data.getSSL())
            port = 993;
        else port = 143;

        try {
            Properties properties = new Properties();
            properties.setProperty("mail.store.protocol", "imaps");
            properties.setProperty("mail.imap.port", String.valueOf(port));

            Session session = Session.getInstance(properties);
            Store store = session.getStore("imaps");
            store.connect(host, username, password);

            Folder emailFolder = store.getFolder(FolderTextField.getText());
            emailFolder.open(Folder.READ_WRITE);

            Message[] messages = emailFolder.getMessages();
            savedMessagesCount = 0;

            BufferedWriter mimeWriter = new BufferedWriter(new FileWriter(mboxFilePath));

            for (Message message : messages) {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                message.writeTo(outputStream);

                mimeWriter.write(outputStream.toString());
                mimeWriter.write("\n\n");

                savedMessagesCount++;
            }

            mimeWriter.close();

            emailFolder.close(false);
            store.close();

            showAlert(AlertType.INFORMATION, "Success", "MIME file saved successfully. Saved messages: " + savedMessagesCount);
        } catch (MessagingException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Error", "An error occurred while accessing the email folder or messages.");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Error", "An error occurred while saving MIME file.");
        }
    }

    private void saveEML() {

    }

    private void saveMSG() {

    }

    private void saveEMLX() {

    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(alertType);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private void setMailImageView() {
        Image image = new Image(data.getImageURL());
        MailImageView.setImage(image);
    }

    private void addOptions() {
        ObservableList<String> options = FXCollections.observableArrayList();

        options.add("Mbox");
        options.add("MIME");
        options.add("EML");
        options.add("MSG");
        options.add("EMLX");

        ExtensionComboBox.setItems(options);
        ExtensionComboBox.getSelectionModel().selectFirst();
    }

    private void saveMessages() {
        switch (ExtensionComboBox.getValue()) {
            case "Mbox" -> saveMbox();
            case "MIME" -> saveMIME();
            case "EML" -> saveEML();
            case "MSG" -> saveMSG();
            case "EMLX" -> saveEMLX();
            default -> {
            }
        }
    }

    private void setTag() {
    Platform.runLater(() -> {
        switch (data.getImapAddress()) {
            case "imap.aol.com" -> tag = "[Aol]/";
            case "imap.gmail.com" -> tag = "[Gmail]/";
            case "imap-mail.outlook.com" -> tag = "[Outlook]/";
            case "imap.mail.yahoo.com" -> tag = "[Yahoo]/";
            case "imap.yandex.ru" -> tag = "";
            case "imap.mail.me.com" -> tag = "[Me]/";
            default -> tag = FolderName.substring(0, FolderName.lastIndexOf("/")) + "/";

        }
    });

        System.out.println(tag);
    }
}