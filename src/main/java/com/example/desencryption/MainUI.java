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
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Main JavaFX screen for the DES application.
 *
 * This class is responsible for user interaction only: selecting files,
 * reading key/text input, calling the DES services, and showing status
 * messages. The actual encryption logic stays in {link DesDataService}.
 */
public class MainUI  extends BorderPane {

    private static final Path GENERATED_KEY_FILE = Path.of("generated_des_key.txt");

    private final FileHnadler fileHnadler = new FileHnadler();
    private final DesDataService desDataService = new DesDataService();

    private TextField textField;
    private TextField keyField;
    private TextArea resultArea;

    /*
     * These fields remember the file the user loaded most recently.
     * Encrypt and Decrypt can then use the already-read bytes instead of asking
     * the user to choose the same file again.
     */
    private File selectedFile;
    private byte[] selectedFileBytes;

    /*
     * These fields remember the latest encrypted or decrypted result.
     * The Save Result button uses them if the user wants to save the output
     * again after canceling or choosing another location.
     */
    private byte[] lastOutputBytes;
    private String lastOutputSuggestedName;

    /**
     * Builds the whole UI screen in Java code.
     *
     * The constructor creates labels, text fields, buttons, and the result
     * area, then connects each button to the method that performs its action.
     */
    public MainUI() {
        Label titleLabel = new Label("Des Encryption");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label inputLabel = new Label("Enter text or choose a file:");
        textField = new TextField();
        textField.setPromptText("Type text here, or load a file to show its path...");

        Label keyLabel = new Label("Enter Key:");
        keyField = new TextField();
        keyField.setPromptText("16 hex characters, for example 133457799BBCDFF1");


        Button loadFileButton = new Button("Load File");
        Button encryptButton = new Button("Encrypt");
        Button decryptButton = new Button("Decrypt");
        Button decryptTextButton = new Button("Decrypt Text");
        Button loadKeyButton = new Button("Load Key");
        Button showKeyExpansionButton = new Button("Show Key Expansion");
        Button generateKeyButton = new Button("Generate Key");
        Button saveResultButton = new Button("Save Result");


        Label resultLabel = new Label("Result:");
        resultArea = new TextArea();
        resultArea.setEditable(false);
        resultArea.setWrapText(true);
        resultArea.setPrefHeight(500);

        loadFileButton.setOnAction(event -> loadAnyFile());
        loadKeyButton.setOnAction(event -> loadKeyFile());
        encryptButton.setOnAction(event -> encryptCurrentInput());
        decryptButton.setOnAction(event -> decryptLoadedFile());
        decryptTextButton.setOnAction(event -> decryptTextFromInputField());
        showKeyExpansionButton.setOnAction(event -> showKeyExpansion());
        generateKeyButton.setOnAction(event -> generateRandomKey());
        saveResultButton.setOnAction(event -> saveLastOutput());

        // File actions are grouped together because they work with loaded files.
        HBox fileActionButtons = new HBox(10);
        fileActionButtons.getChildren().addAll(
                loadFileButton, encryptButton, decryptButton, saveResultButton
        );

        // Text/key actions are grouped together because they work with typed text or the key field.
        HBox keyAndTextButtons = new HBox(10);
        keyAndTextButtons.getChildren().addAll(
                decryptTextButton, loadKeyButton, showKeyExpansionButton, generateKeyButton
        );

        VBox vBox = new VBox(10);
        vBox.setPadding(new Insets(20));
        vBox.getChildren().addAll(
                titleLabel,
                inputLabel,
                textField,
                keyLabel,
                keyField,
                fileActionButtons,
                keyAndTextButtons,
                resultLabel,
                resultArea
        );
        setCenter(vBox);
    }

    /**
     * Lets the user choose any file, stores its bytes, and prepares it for
     * either encryption or decryption.
     */
    private void loadAnyFile() {
        File file = fileHnadler.chooseAnyFile(getSceneWindow());
        if (file == null) {
            return;
        }

        try {
            selectedFile = file;
            selectedFileBytes = fileHnadler.readFileBytes(file);
            textField.setText(file.getAbsolutePath());

            if (isTextFile(file)) {
                resultArea.setText(
                        "Loaded text file: " + file.getAbsolutePath()
                                + System.lineSeparator()
                                + "Size: " + selectedFileBytes.length + " bytes"
                                + System.lineSeparator()
                                + "Click Encrypt to encrypt it, or Decrypt if this file is encrypted data."
                                + System.lineSeparator()
                                + System.lineSeparator()
                                + new String(selectedFileBytes, StandardCharsets.UTF_8)
                );
            } else {
                resultArea.setText(
                        "Loaded file: " + file.getAbsolutePath()
                                + System.lineSeparator()
                                + "Size: " + selectedFileBytes.length + " bytes"
                                + System.lineSeparator()
                                + "Click Encrypt to encrypt it, or Decrypt if this file is encrypted data."
                                + System.lineSeparator()
                                + "Binary file contents are not displayed here."
                );
            }
        } catch (IOException | IllegalArgumentException ex) {
            showError("Could not load file", ex);
        }
    }

    /**
     * Loads a DES key from a small text file.
     *
     * The file should contain one key such as 133457799BBCDFF1. Whitespace
     * before or after the key is ignored.
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
            String keyHex = getValidatedKey();
            byte[] inputBytes = getBytesToEncrypt();

            byte[] encryptedBytes = desDataService.encryptBytes(inputBytes, keyHex);
            lastOutputBytes = encryptedBytes;
            lastOutputSuggestedName = getEncryptedDefaultName();

            if (!shouldEncryptSelectedFile()) {
                String hexCipherText = BitUtil.bytesToHex(encryptedBytes);
                resultArea.setText(
                        "Text encryption complete."
                                + System.lineSeparator()
                                + "Copy this hexadecimal ciphertext into the input field, then click Decrypt Text:"
                                + System.lineSeparator()
                                + System.lineSeparator()
                                + hexCipherText
                );
                return;
            }

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
     * Decrypts short text that was encrypted and shown as hexadecimal.
     *
     * The input field should contain the hexadecimal ciphertext produced by
     * encrypting typed text with the Encrypt button.
     */
    private void decryptTextFromInputField() {
        try {
            String keyHex = getValidatedKey();
            String hexCipherText = textField.getText().trim().replaceAll("\\s+", "");
            if (hexCipherText.isEmpty()) {
                throw new IllegalArgumentException("Enter hexadecimal ciphertext in the input field first.");
            }

            byte[] encryptedBytes = BitUtil.hexToBytes(hexCipherText);
            byte[] decryptedBytes = desDataService.decryptBytes(encryptedBytes, keyHex);
            String plainText = new String(decryptedBytes, StandardCharsets.UTF_8);

            lastOutputBytes = decryptedBytes;
            lastOutputSuggestedName = "decrypted_text.txt";

            resultArea.setText(
                    "Text decryption complete."
                            + System.lineSeparator()
                            + System.lineSeparator()
                            + plainText
            );
        } catch (IllegalArgumentException ex) {
            showError("Text decryption failed. Make sure the input is hexadecimal ciphertext and the key is correct", ex);
        }
    }

    /**
     * Decrypts the currently loaded file and asks where to save the result.
     */
    private void decryptLoadedFile() {
        if (!shouldEncryptSelectedFile()) {
            resultArea.setText("Load an encrypted file first, then click Decrypt.");
            return;
        }

        try {
            String keyHex = getValidatedKey();
            byte[] decryptedBytes = desDataService.decryptBytes(selectedFileBytes, keyHex);

            lastOutputBytes = decryptedBytes;
            lastOutputSuggestedName = getRestoredDefaultName(selectedFile);

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
     * Shows the 16 DES round keys generated from the current key.
     *
     * Each key is displayed in binary and hexadecimal, matching the key
     * expansion requirement from the project description.
     */
    private void showKeyExpansion() {
        try {
            String keyHex = getValidatedKey();
            KeyUtil keyUtil = new KeyUtil();
            String[] roundKeys = keyUtil.generateRoundKeys(keyHex);

            StringBuilder output = new StringBuilder();
            output.append("DES key expansion for key: ").append(keyHex).append(System.lineSeparator());
            output.append(System.lineSeparator());

            for (int i = 0; i < roundKeys.length; i++) {
                output.append("K").append(i + 1).append(System.lineSeparator());
                output.append("Binary: ").append(roundKeys[i]).append(System.lineSeparator());
                output.append("Hex: ").append(BitUtil.binaryToHex(roundKeys[i])).append(System.lineSeparator());
                if (i < roundKeys.length - 1) {
                    output.append(System.lineSeparator());
                }
            }

            resultArea.setText(output.toString());
        } catch (IllegalArgumentException ex) {
            showError("Could not show key expansion", ex);
        }
    }

    /**
     * Generates a valid random DES key, puts it in the key field, and saves it
     * to generated_des_key.txt in the project folder.
     */
    private void generateRandomKey() {
        String key = KeyUtil.generateRandomDesKey();
        keyField.setText(key);

        try {
            Files.writeString(GENERATED_KEY_FILE, key + System.lineSeparator(), StandardCharsets.UTF_8);
            resultArea.setText(
                    "Generated random DES key:"
                            + System.lineSeparator()
                            + System.lineSeparator()
                            + key
                            + System.lineSeparator()
                            + System.lineSeparator()
                            + "Saved to: " + GENERATED_KEY_FILE.toAbsolutePath()
            );
        } catch (IOException ex) {
            resultArea.setText(
                    "Generated random DES key:"
                            + System.lineSeparator()
                            + System.lineSeparator()
                            + key
                            + System.lineSeparator()
                            + System.lineSeparator()
                            + "Warning: the key was generated, but it could not be saved to "
                            + GENERATED_KEY_FILE.toAbsolutePath()
                            + System.lineSeparator()
                            + ex.getMessage()
            );
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
     * If a file was loaded, encryption uses the file bytes. Otherwise, it
     * uses the text typed directly in the input field.
     */
    private byte[] getBytesToEncrypt() {
        if (shouldEncryptSelectedFile()) {
            return selectedFileBytes;
        }

        String text = textField.getText();
        if (text == null || text.isEmpty()) {
            throw new IllegalArgumentException("Enter text or load a supported file first.");
        }

        return text.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * A selected file is used only while its path is still shown in the input field.
     * If the user replaces the path with typed text, Encrypt uses that text instead.
     */
    private boolean shouldEncryptSelectedFile() {
        return selectedFile != null
                && selectedFileBytes != null
                && textField.getText().equals(selectedFile.getAbsolutePath());
    }

    private String getEncryptedDefaultName() {
        if (shouldEncryptSelectedFile()) {
            return selectedFile.getName() + ".des";
        }

        return "encrypted_text.des";
    }

    /**
     * Suggests a filename for decrypted file output.
     *
     * If the encrypted file is named {@code photo.jpg.des}, removing
     * {code .des} suggests {code photo.jpg}, which restores the original
     * extension for normal project use.
     */
    private String getRestoredDefaultName(File encryptedFile) {
        String name = encryptedFile.getName();
        if (name.toLowerCase().endsWith(".des")) {
            name = name.substring(0, name.length() - 4);
            if (name.isBlank()) {
                name = "decrypted_output";
            }
            return name;
        }

        return "restored_" + name;
    }

    /**
     * Checks whether a loaded file can be safely previewed as text.
     */
    private boolean isTextFile(File file) {
        return file.getName().toLowerCase().endsWith(".txt");
    }

    /**
     * Reads and validates the key from the key field.
     *
     * DES keys are entered as 16 hexadecimal characters, which represent
     * the original 64-bit DES key before PC-1 removes parity bits.
     */
    private String getValidatedKey() {
        String keyHex = keyField.getText().trim();
        if (keyHex.isEmpty()) {
            throw new IllegalArgumentException("Enter a DES key first. The key must be 16 hexadecimal characters.");
        }
        if (keyHex.length() != 16 || !keyHex.matches("[0-9a-fA-F]+")) {
            throw new IllegalArgumentException("DES key must be exactly 16 hexadecimal characters, for example 133457799BBCDFF1.");
        }

        return keyHex.toUpperCase();
    }

    /**
     * Returns the JavaFX window that owns file chooser dialogs.
     */
    private javafx.stage.Window getSceneWindow() {
        if (getScene() == null) {
            return null;
        }

        return getScene().getWindow();
    }

    /**
     * Shows an error message in the result area instead of printing to console.
     */
    private void showError(String prefix, Exception ex) {
        resultArea.setText(prefix + ": " + ex.getMessage());
    }
}
