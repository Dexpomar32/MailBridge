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
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
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

    final Path desktopPath = Paths.get(System.getProperty("user.home"), "Desktop");

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
        FilePath = URL + "\\" + FolderName;

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

            BufferedWriter mboxWriter = new BufferedWriter(new FileWriter(FilePath.replace("/", ".") + ".mbox"));

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
        } catch (MessagingException | IOException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Error", "An error occurred while saving Mbox file.");
        }

        Platform.runLater(() -> {
            PercentsLabel.setText("");
            MessagesSavingProgress.setProgress(0.0);
            MessagesSavingProgress.setVisible(false);
        });
    }

    private void saveMIME() {
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
            else
                emailFolder = store.getFolder(FolderName);

            assert emailFolder != null;
            emailFolder.open(Folder.READ_ONLY);

            Message[] messages = emailFolder.getMessages();
            savedMessagesCount = messages.length;

            File zipFile = new File(FilePath.replace("/", ".") + ".zip");

            try (ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(zipFile))) {
                double progress = 0.0;
                Platform.runLater(() -> MessagesSavingProgress.setProgress(0.0));

                for (int i = 0; i < messages.length; i++) {
                    Message message = messages[i];

                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    message.writeTo(outputStream);

                    String entryName = "message_" + (i + 1) + ".mime";
                    ZipEntry zipEntry = new ZipEntry(entryName);
                    zipOutputStream.putNextEntry(zipEntry);

                    zipOutputStream.write(outputStream.toByteArray());
                    zipOutputStream.closeEntry();

                    progress += 1.0 / savedMessagesCount;
                    final double currentProgress = progress;

                    Platform.runLater(() -> MessagesSavingProgress.setProgress(currentProgress));

                    DecimalFormat decimalFormat = new DecimalFormat("0");
                    String progressText = decimalFormat.format(currentProgress * 100) + "%";

                    Platform.runLater(() -> PercentsLabel.setText(progressText));
                }
            }

            emailFolder.close(false);
            store.close();

            showAlert(AlertType.INFORMATION, "Success", "MIME files saved as a zip archive. Saved messages: " + savedMessagesCount);
        } catch (MessagingException | IOException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Error", "An error occurred while saving MIME files as a zip archive.");
        }

        Platform.runLater(() -> {
            PercentsLabel.setText("");
            MessagesSavingProgress.setProgress(0.0);
            MessagesSavingProgress.setVisible(false);
        });
    }

    private void saveEML() {
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
            else
                emailFolder = store.getFolder(FolderName);

            assert emailFolder != null;
            emailFolder.open(Folder.READ_ONLY);

            Message[] messages = emailFolder.getMessages();
            savedMessagesCount = messages.length;

            File zipFile = new File(FilePath.replace("/", ".") + ".zip");

            try (ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(zipFile))) {
                double progress = 0.0;
                Platform.runLater(() -> MessagesSavingProgress.setProgress(0.0));

                for (int i = 0; i < messages.length; i++) {
                    Message message = messages[i];

                    String entryName = "message_" + (i + 1) + ".eml";
                    ZipEntry zipEntry = new ZipEntry(entryName);
                    zipOutputStream.putNextEntry(zipEntry);

                    message.writeTo(zipOutputStream);
                    zipOutputStream.closeEntry();

                    progress += 1.0 / savedMessagesCount;
                    final double currentProgress = progress;

                    Platform.runLater(() -> MessagesSavingProgress.setProgress(currentProgress));

                    DecimalFormat decimalFormat = new DecimalFormat("0");
                    String progressText = decimalFormat.format(currentProgress * 100) + "%";

                    Platform.runLater(() -> PercentsLabel.setText(progressText));
                }
            }

            emailFolder.close(false);
            store.close();

            showAlert(AlertType.INFORMATION, "Success", "EML files saved as a zip archive. Saved messages: " + savedMessagesCount);
        } catch (MessagingException | IOException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Error", "An error occurred while saving EML files as a zip archive.");
        }

        Platform.runLater(() -> {
            PercentsLabel.setText("");
            MessagesSavingProgress.setProgress(0.0);
            MessagesSavingProgress.setVisible(false);
        });
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

        ExtensionComboBox.setItems(options);
        ExtensionComboBox.getSelectionModel().selectFirst();
    }

    private void saveMessages() {
        switch (ExtensionComboBox.getValue()) {
            case "Mbox" -> saveMbox();
            case "MIME" -> saveMIME();
            case "EML" -> saveEML();
            default -> {}
        }
    }

    private void setTag() {
        switch (data.getImapAddress()) {
            case "imap.aol.com", "imap-mail.outlook.com", "imap.mail.yahoo.com", "imap.yandex.ru", "imap.mail.me.com" -> tag = "";
            case "imap.gmail.com" -> tag = "[Gmail]/";
            default -> {}
        }
    }
}