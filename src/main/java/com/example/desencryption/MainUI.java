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

public class MainUI  extends BorderPane {

    private static final Path GENERATED_KEY_FILE = Path.of("generated_des_key.txt");

    private final FileHnadler fileHnadler = new FileHnadler();
    private final DesDataService desDataService = new DesDataService();
    private final SmsService smsService = new SmsService();
    private final EmailService emailService = new EmailService();

    private TextField textField;
    private TextField keyField;
    private TextField phoneField;
    private TextField emailField;
    private TextArea resultArea;

    private File selectedFile;
    private byte[] selectedFileBytes;

    private byte[] lastOutputBytes;
    private String lastOutputSuggestedName;

    private byte[] lastEncryptedBytes;
    private String lastEncryptedTextHex;
    private String lastEncryptedSuggestedName;
    private boolean lastEncryptedOutputIsText;

    public MainUI() {
        Label titleLabel = new Label("Des Encryption");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label inputLabel = new Label("Enter text or choose a file:");
        textField = new TextField();
        textField.setPromptText("Type text here, or load a file to show its path...");

        Label keyLabel = new Label("Enter Key:");
        keyField = new TextField();
        keyField.setPromptText("16 hex characters, for example 133457799BBCDFF1");

        Label phoneLabel = new Label("Recipient Phone Number:");
        phoneField = new TextField();
        phoneField.setPromptText("International format, for example +970599123456");

        Label emailLabel = new Label("Recipient Email:");
        emailField = new TextField();
        emailField.setPromptText("example@email.com");

        Button loadFileButton = new Button("Load File");
        Button encryptButton = new Button("Encrypt");
        Button decryptButton = new Button("Decrypt");
        Button decryptTextButton = new Button("Decrypt Text");
        Button loadKeyButton = new Button("Load Key");
        Button showKeyExpansionButton = new Button("Show Key Expansion");
        Button generateKeyButton = new Button("Generate Key");
        Button sendKeySmsButton = new Button("Send Key SMS");
        Button sendCiphertextEmailButton = new Button("Send Ciphertext Email");
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
        sendKeySmsButton.setOnAction(event -> sendKeySms());
        sendCiphertextEmailButton.setOnAction(event -> sendCiphertextEmail());
        saveResultButton.setOnAction(event -> saveLastOutput());

        HBox fileActionButtons = new HBox(10);
        fileActionButtons.getChildren().addAll(
                loadFileButton, encryptButton, decryptButton, saveResultButton
        );

        HBox keyAndTextButtons = new HBox(10);
        keyAndTextButtons.getChildren().addAll(
                decryptTextButton, loadKeyButton, showKeyExpansionButton, generateKeyButton, sendKeySmsButton
        );

        HBox emailButtons = new HBox(10);
        emailButtons.getChildren().add(sendCiphertextEmailButton);

        VBox vBox = new VBox(10);
        vBox.setPadding(new Insets(20));
        vBox.getChildren().addAll(
                titleLabel,
                inputLabel,
                textField,
                keyLabel,
                keyField,
                phoneLabel,
                phoneField,
                emailLabel,
                emailField,
                fileActionButtons,
                keyAndTextButtons,
                emailButtons,
                resultLabel,
                resultArea
        );
        setCenter(vBox);
    }

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

    private void encryptCurrentInput() {
        try {
            String keyHex = getValidatedKey();
            byte[] inputBytes = getBytesToEncrypt();

            byte[] encryptedBytes = desDataService.encryptBytes(inputBytes, keyHex);
            lastOutputBytes = encryptedBytes;
            lastOutputSuggestedName = getEncryptedDefaultName();

            if (!shouldEncryptSelectedFile()) {
                String hexCipherText = BitUtil.bytesToHex(encryptedBytes);
                rememberEncryptedText(hexCipherText, encryptedBytes);
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
                rememberEncryptedFile(encryptedBytes, outputFile.getName());
                resultArea.setText(
                        "Encryption complete."
                                + System.lineSeparator()
                                + "Saved to: " + outputFile.getAbsolutePath()
                                + System.lineSeparator()
                                + "Encrypted size: " + encryptedBytes.length + " bytes"
                );
            } else {
                rememberEncryptedFile(encryptedBytes, lastOutputSuggestedName);
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

    /* Handles sendKeySms. */
    private void sendKeySms() {
        // Starts code that may fail.
        try {
            // Creates a local value for this method.
            String keyHex = getValidatedKey();
            // Creates a local value for this method.
            String recipientPhoneNumber = phoneField.getText().trim();

            // Creates a local value for this method.
            String messageSid = smsService.sendKeySms(recipientPhoneNumber, keyHex);

            // Sets a value on the object.
            resultArea.setText(
                    // Runs this line of the method.
                    "DES key sent by SMS."
                            // Runs this line of the method.
                            + System.lineSeparator()
                            // Runs this line of the method.
                            + "Recipient: " + recipientPhoneNumber
                            // Runs this line of the method.
                            + System.lineSeparator()
                            // Runs this line of the method.
                            + "Message SID: " + messageSid
            );
        // Runs this line of the method.
        } catch (IllegalArgumentException ex) {
            // Runs this line of the method.
            showError("SMS failed", ex);
        // Runs this line of the method.
        } catch (RuntimeException ex) {
            // Runs this line of the method.
            showError("Twilio rejected the SMS request", ex);
        }
    }

    /* Handles sendCiphertextEmail. */
    private void sendCiphertextEmail() {
        // Starts code that may fail.
        try {
            // Creates a local value for this method.
            String recipientEmail = emailField.getText().trim();

            // Checks the condition before continuing.
            if (lastEncryptedBytes == null) {
                // Throws an error for invalid input.
                throw new IllegalArgumentException("Encrypt text or a file first, then send the email.");
            }

            // Checks the condition before continuing.
            if (lastEncryptedOutputIsText) {
                // Runs this line of the method.
                emailService.sendCipherTextEmail(
                        // Runs this line of the method.
                        recipientEmail,
                        // Runs this line of the method.
                        "DES encrypted text",
                        // Runs this line of the method.
                        lastEncryptedTextHex
                );
                // Sets a value on the object.
                resultArea.setText("Encrypted text ciphertext sent to: " + recipientEmail);
            // Runs this line of the method.
            } else {
                // Runs this line of the method.
                emailService.sendEncryptedFileEmail(
                        // Runs this line of the method.
                        recipientEmail,
                        // Runs this line of the method.
                        "DES encrypted file",
                        // Runs this line of the method.
                        lastEncryptedBytes,
                        // Runs this line of the method.
                        lastEncryptedSuggestedName
                );
                // Sets a value on the object.
                resultArea.setText("Encrypted file sent to: " + recipientEmail);
            }
        // Runs this line of the method.
        } catch (IllegalArgumentException ex) {
            // Runs this line of the method.
            showError("Email failed", ex);
        }
    }

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

    private void rememberEncryptedText(String hexCipherText, byte[] encryptedBytes) {
        lastEncryptedTextHex = hexCipherText;
        lastEncryptedBytes = encryptedBytes;
        lastEncryptedSuggestedName = "encrypted_text.txt";
        lastEncryptedOutputIsText = true;
    }

    private void rememberEncryptedFile(byte[] encryptedBytes, String fileName) {
        lastEncryptedTextHex = null;
        lastEncryptedBytes = encryptedBytes;
        lastEncryptedSuggestedName = fileName;
        lastEncryptedOutputIsText = false;
    }

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

    private boolean isTextFile(File file) {
        return file.getName().toLowerCase().endsWith(".txt");
    }

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
