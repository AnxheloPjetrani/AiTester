package com.example.aitester;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

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

                // Let user pick save location for main DOM
                saveDomWithFileChooser(domHtml);

                // Ask to snapshot all <a> links
                snapshotAllLinksFromPage(url);
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

    // 🆕 NEW METHOD: Save DOMs of all <a href> links
    private void snapshotAllLinksFromPage(String baseUrl) {
        try {
            Document doc = Jsoup.connect(baseUrl).get();
            doc.setBaseUri(baseUrl);

            // Select all elements that *may* contain navigable links
            Elements elements = doc.select("[href], [src], form[action]");
            Set<String> uniqueUrls = new HashSet<>();

            for (Element el : elements) {
                String link = el.hasAttr("href") ? el.absUrl("href") :
                        el.hasAttr("src") ? el.absUrl("src") :
                                el.hasAttr("action") ? el.absUrl("action") : "";

                if (!link.isEmpty()
                        && link.startsWith("http")
                        && !link.matches(".*\\.(css|js|jpg|png|gif|svg|woff|ico)(\\?.*)?$")) {
                    uniqueUrls.add(link);
                }
            }

            if (uniqueUrls.isEmpty()) {
                resultLabel.setText("ℹ️ No valid links found on the page.");
                return;
            }

            DirectoryChooser dirChooser = new DirectoryChooser();
            dirChooser.setTitle("Choose folder to save linked page DOMs");
            File folder = dirChooser.showDialog(urlField.getScene().getWindow());

            if (folder == null) {
                resultLabel.setText("ℹ️ Link snapshot cancelled.");
                return;
            }

            int count = 1;
            for (String link : uniqueUrls) {
                if (!isUrlReachable(link)) continue;

                String dom = fetchDomHtml(link);
                if (dom == null) continue;

                // Sanitize filename
                String safeName = link.replaceAll("[^a-zA-Z0-9]", "_");
                if (safeName.length() > 50) {
                    safeName = safeName.substring(0, 50);
                }

                File outFile = new File(folder, String.format("page_%02d_%s.html", count++, safeName));
                saveDomToFile(dom, outFile.getAbsolutePath());
            }

            resultLabel.setText("✅ Snapshots saved: " + (count - 1));
        } catch (IOException e) {
            resultLabel.setText("❌ Failed to fetch links or DOMs.");
            e.printStackTrace();
        }
    }

}
