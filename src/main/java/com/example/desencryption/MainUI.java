package com.example.desencryption;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Main JavaFX screen for the DES application.
 *
 * <p>This class is responsible for user interaction only: selecting files,
 * reading key/text input, calling the DES services, and showing status
 * messages. The actual encryption logic stays in {@link DesDataService}.</p>
 */
public class MainUI  extends BorderPane {

    private final FileHnadler fileHnadler = new FileHnadler();
    private final DesDataService desDataService = new DesDataService();

    private TextField textField;
    private TextField keyField;
    private TextArea resultArea;

    private File selectedFile;
    private byte[] selectedFileBytes;
    private byte[] lastOutputBytes;
    private String lastOutputSuggestedName;

    public MainUI() {
        Label titleLabel = new Label("Des Encryption");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label inputLabel = new Label("Enter text or choose a file:");
        textField = new TextField();
        textField.setPromptText("Type text here, or load a file to show its path...");

        Label keyLabel = new Label("Enter Key:");
        keyField = new TextField();
        keyField.setPromptText("16 hex characters, for example 133457799BBCDFF1");


        Button loadMediaButton = new Button("Load Image, Audio, Video");
        Button encryptButton = new Button("Encrypt");
        Button decryptButton = new Button("Decrypt");
        Button loadTextButton = new Button("Load Text");
        Button loadKeyButton = new Button("Load Key");
        Button saveResultButton = new Button("Save Result");


        Label resultLabel = new Label("Result:");
        resultArea = new TextArea();
        resultArea.setEditable(false);
        resultArea.setWrapText(true);
        resultArea.setPrefHeight(500);

        loadTextButton.setOnAction(event -> loadTextFile());
        loadMediaButton.setOnAction(event -> loadMediaFile());
        loadKeyButton.setOnAction(event -> loadKeyFile());
        encryptButton.setOnAction(event -> encryptCurrentInput());
        decryptButton.setOnAction(event -> decryptSelectedFile());
        saveResultButton.setOnAction(event -> saveLastOutput());

        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(
                encryptButton, decryptButton, loadMediaButton,
                loadTextButton, loadKeyButton, saveResultButton
        );

        VBox vBox = new VBox(10);
        vBox.setPadding(new Insets(20));
        vBox.getChildren().addAll(
                titleLabel,
                inputLabel,
                textField,
                keyLabel,
                keyField,
                buttonBox,
                resultLabel,
                resultArea
        );
        setCenter(vBox);
    }

    /**
     * Lets the user choose a text file, stores its bytes, and previews the text.
     */
    private void loadTextFile() {
        File file = fileHnadler.chooseTextFile(getSceneWindow());
        if (file == null) {
            return;
        }

        try {
            selectedFile = file;
            selectedFileBytes = fileHnadler.readFileBytes(file);
            textField.setText(file.getAbsolutePath());
            resultArea.setText(
                    "Loaded text file: " + file.getAbsolutePath()
                            + System.lineSeparator()
                            + "Encrypt will use this file's bytes."
                            + System.lineSeparator()
                            + System.lineSeparator()
                            + new String(selectedFileBytes, StandardCharsets.UTF_8)
            );
        } catch (IOException | IllegalArgumentException ex) {
            showError("Could not load text file", ex);
        }
    }

    /**
     * Lets the user choose a media file and stores the raw bytes.
     *
     * <p>Images, audio, and video are binary data, so the UI shows only the
     * file path and size instead of printing the raw bytes.</p>
     */
    private void loadMediaFile() {
        File file = fileHnadler.chooseMediaFile(getSceneWindow());
        if (file == null) {
            return;
        }

        try {
            selectedFile = file;
            selectedFileBytes = fileHnadler.readFileBytes(file);
            textField.setText(file.getAbsolutePath());
            resultArea.setText(
                    "Loaded file: " + file.getAbsolutePath()
                            + System.lineSeparator()
                            + "Size: " + selectedFileBytes.length + " bytes"
                            + System.lineSeparator()
                            + "Encrypt will use this file's bytes."
                            + System.lineSeparator()
                            + "Binary file contents are not displayed here."
            );
        } catch (IOException | IllegalArgumentException ex) {
            showError("Could not load media file", ex);
        }
    }

    /**
     * Loads a DES key from a small text file.
     *
     * <p>The file should contain one key such as 133457799BBCDFF1. Whitespace
     * before or after the key is ignored.</p>
     */
    private void loadKeyFile() {
        File file = fileHnadler.chooseTextFile(getSceneWindow());
        if (file == null) {
            return;
        }

        try {
            byte[] keyBytes = fileHnadler.readFileBytes(file);
            String key = new String(keyBytes, StandardCharsets.UTF_8).trim();
            keyField.setText(key);
            resultArea.setText("Loaded key from: " + file.getAbsolutePath());
        } catch (IOException | IllegalArgumentException ex) {
            showError("Could not load key file", ex);
        }
    }

    /**
     * Encrypts the currently selected file bytes, or typed text if no file is selected.
     */
    private void encryptCurrentInput() {
        try {
            String keyHex = keyField.getText().trim();
            byte[] inputBytes = getBytesToEncrypt();

            byte[] encryptedBytes = desDataService.encryptBytes(inputBytes, keyHex);
            lastOutputBytes = encryptedBytes;
            lastOutputSuggestedName = getEncryptedDefaultName();

            File outputFile = fileHnadler.chooseSaveFile(getSceneWindow(), lastOutputSuggestedName);
            if (outputFile != null) {
                fileHnadler.writeFileBytes(outputFile, encryptedBytes);
                resultArea.setText(
                        "Encryption complete."
                                + System.lineSeparator()
                                + "Saved to: " + outputFile.getAbsolutePath()
                                + System.lineSeparator()
                                + "Encrypted size: " + encryptedBytes.length + " bytes"
                );
            } else {
                resultArea.setText(
                        "Encryption complete, but save was canceled."
                                + System.lineSeparator()
                                + "Use Save Result to choose an output file."
                                + System.lineSeparator()
                                + "Encrypted size: " + encryptedBytes.length + " bytes"
                );
            }
        } catch (IOException | IllegalArgumentException ex) {
            showError("Encryption failed", ex);
        }
    }

    /**
     * Chooses an encrypted file, decrypts it, and asks where to save the result.
     */
    private void decryptSelectedFile() {
        File encryptedFile = fileHnadler.chooseEncryptedFile(getSceneWindow());
        if (encryptedFile == null) {
            return;
        }

        try {
            String keyHex = keyField.getText().trim();
            byte[] encryptedBytes = fileHnadler.readFileBytes(encryptedFile);
            byte[] decryptedBytes = desDataService.decryptBytes(encryptedBytes, keyHex);

            lastOutputBytes = decryptedBytes;
            lastOutputSuggestedName = getRestoredDefaultName(encryptedFile);

            File outputFile = fileHnadler.chooseSaveFile(getSceneWindow(), lastOutputSuggestedName);
            if (outputFile != null) {
                fileHnadler.writeFileBytes(outputFile, decryptedBytes);
                resultArea.setText(
                        "Decryption complete."
                                + System.lineSeparator()
                                + "Saved to: " + outputFile.getAbsolutePath()
                                + System.lineSeparator()
                                + "Decrypted size: " + decryptedBytes.length + " bytes"
                );
            } else {
                resultArea.setText(
                        "Decryption complete, but save was canceled."
                                + System.lineSeparator()
                                + "Use Save Result to choose an output file."
                                + System.lineSeparator()
                                + "Decrypted size: " + decryptedBytes.length + " bytes"
                );
            }
        } catch (IOException | IllegalArgumentException ex) {
            showError("Decryption failed", ex);
        }
    }

    /**
     * Saves the most recent encrypted or decrypted bytes again.
     */
    private void saveLastOutput() {
        if (lastOutputBytes == null) {
            resultArea.setText("There is no encrypted or decrypted result to save yet.");
            return;
        }

        try {
            File outputFile = fileHnadler.chooseSaveFile(getSceneWindow(), lastOutputSuggestedName);
            if (outputFile == null) {
                return;
            }

            fileHnadler.writeFileBytes(outputFile, lastOutputBytes);
            resultArea.setText("Saved result to: " + outputFile.getAbsolutePath());
        } catch (IOException | IllegalArgumentException ex) {
            showError("Could not save result", ex);
        }
    }

    /**
     * Chooses what Encrypt should process.
     *
     * <p>If a file was loaded, encryption uses the file bytes. Otherwise, it
     * uses the text typed directly in the input field.</p>
     */
    private byte[] getBytesToEncrypt() {
        if (selectedFileBytes != null) {
            return selectedFileBytes;
        }

        return textField.getText().getBytes(StandardCharsets.UTF_8);
    }

    private String getEncryptedDefaultName() {
        if (selectedFile != null) {
            return selectedFile.getName() + ".des";
        }

        return "encrypted_text.des";
    }

    private String getRestoredDefaultName(File encryptedFile) {
        String name = encryptedFile.getName();
        if (name.toLowerCase().endsWith(".des")) {
            name = name.substring(0, name.length() - 4);
        }

        return "restored_" + name;
    }

    private javafx.stage.Window getSceneWindow() {
        if (getScene() == null) {
            return null;
        }

        return getScene().getWindow();
    }

    private void showError(String prefix, Exception ex) {
        resultArea.setText(prefix + ": " + ex.getMessage());
    }
}
