package com.example.aitester;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class HelloController {

    @FXML
    private TextField urlField;

    @FXML
    private Label resultLabel;

    @FXML
    private TextArea domTextArea;

    @FXML
    protected void onTestUrlClick() {
        String url = urlField.getText().trim();
        domTextArea.clear();
        resultLabel.setText("");

        if (url.isEmpty()) {
            resultLabel.setText("⚠️ Please enter a URL.");
            return;
        }

        if (!url.startsWith("http")) {
            url = "https://" + url;
        }

        if (isUrlReachable(url)) {
            resultLabel.setText("✅ URL is reachable. Fetching DOM...");
            String domHtml = fetchDomHtml(url);
            if (domHtml != null) {
                domTextArea.setText(domHtml);

                // Let user pick save location
                saveDomWithFileChooser(domHtml);
            } else {
                resultLabel.setText("❌ Failed to fetch DOM.");
            }
        } else {
            resultLabel.setText("❌ URL is not reachable.");
        }
    }

    private boolean isUrlReachable(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(3000);
            connection.connect();

            int code = connection.getResponseCode();
            return code >= 200 && code < 400;
        } catch (IOException e) {
            return false;
        }
    }

    private String fetchDomHtml(String urlString) {
        try {
            Document doc = Jsoup.connect(urlString).get();
            return doc.outerHtml();
        } catch (IOException e) {
            return null;
        }
    }

    private void saveDomWithFileChooser(String domContent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save DOM Snapshot");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("HTML Files", "*.html")
        );

        // Get current window
        Window window = urlField.getScene().getWindow();
        File file = fileChooser.showSaveDialog(window);

        if (file != null) {
            boolean success = saveDomToFile(domContent, file.getAbsolutePath());
            if (success) {
                resultLabel.setText("✅ DOM saved to: " + file.getAbsolutePath());
            } else {
                resultLabel.setText("❌ Failed to save DOM.");
            }
        } else {
            resultLabel.setText("ℹ️ Save cancelled.");
        }
    }

    private boolean saveDomToFile(String domContent, String filePath) {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(domContent);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
