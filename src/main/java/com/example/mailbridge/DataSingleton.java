package com.example.mailbridge;

import javafx.concurrent.Task;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DataSingleton {
    private static final DataSingleton instance = new DataSingleton();
    private Task<?> task;
    private final ExecutorService executorService = Executors.newFixedThreadPool(1);
    private boolean HostVisibility, SSL;
    private String imapAddress, imageURL, Username, Password;

    private DataSingleton() {}

    public static DataSingleton getInstance() {
        return instance;
    }

    public boolean getHostVisibility() {
        return HostVisibility;
    }

    public void setHostVisibility(boolean hostVisibility) {
        HostVisibility = hostVisibility;
    }

    public String getImapAddress() {
        return imapAddress;
    }

    public void setImapAddress(String imapAddress) {
        this.imapAddress = imapAddress;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getUsername() {
        return Username;
    }

    public void setUsername(String username) {
        Username = username;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }

    public boolean getSSL() {
        return SSL;
    }

    public void setSSL(boolean SSL) {
        this.SSL = SSL;
    }

    public synchronized void startTask(Task<?> task) {
        if (this.task == null || this.task.isDone()) {
            this.task = task;
            executorService.submit(task);
        }
    }

    public synchronized void stopTask() {
        if (task != null) {
            task.cancel(true);
            executorService.shutdown();
            task = null;
        }
    }
}
