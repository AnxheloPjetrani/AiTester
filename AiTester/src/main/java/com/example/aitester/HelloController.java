package com.example.aitester;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class HelloController {

    @FXML
    private TextField urlField;

    @FXML
    private Label resultLabel;

    @FXML
    protected void onTestUrlClick() {
        String url = urlField.getText().trim();
        if (url.isEmpty()) {
            resultLabel.setText("⚠️ Please enter a URL.");
            return;
        }

        boolean reachable = isUrlReachable(url);
        resultLabel.setText(reachable ? "✅ URL is reachable!" : "❌ URL is not reachable.");
    }

    private boolean isUrlReachable(String urlString) {
        try {
            if (!urlString.startsWith("http")) {
                urlString = "https://" + urlString;
            }

            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(3000); // milliseconds
            connection.connect();

            int code = connection.getResponseCode();
            return code >= 200 && code < 400;
        } catch (IOException e) {
            return false;
        }
    }
}
