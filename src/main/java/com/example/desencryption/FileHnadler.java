package com.example.desencryption;

import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class FileHnadler {

    /* Handles chooseAnyFile. */
    public File chooseAnyFile(Window owner) {
        // Creates a local value for this method.
        FileChooser fileChooser = new FileChooser();
        // Sets a value on the object.
        fileChooser.setTitle("Choose File");

        // Adds the value to the collection.
        fileChooser.getExtensionFilters().add(
                // Runs this line of the method.
                new FileChooser.ExtensionFilter(
                        // Runs this line of the method.
                        "Supported Files",
                        // Runs this line of the method.
                        "*.txt", "*.docx", "*.des", "*.png", "*.jpg", "*.jpeg", "*.mp3", "*.wav"
                // Runs this line of the method.
                )
        );
        // Adds the value to the collection.
        fileChooser.getExtensionFilters().add(
                // Runs this line of the method.
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        // Returns the result to the caller.
        return fileChooser.showOpenDialog(owner);
    }

    /* Handles chooseTextFile. */
    public File chooseTextFile(Window owner) {
        // Creates a local value for this method.
        FileChooser fileChooser = new FileChooser();
        // Sets a value on the object.
        fileChooser.setTitle("Choose Text File");

        // Filters make the dialog easier to use, but the file is still read as bytes.
        // Adds the value to the collection.
        fileChooser.getExtensionFilters().add(
                // Runs this line of the method.
                new FileChooser.ExtensionFilter("Text Files", "*.txt")
        );
        // Adds the value to the collection.
        fileChooser.getExtensionFilters().add(
                // Runs this line of the method.
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        // Returns the result to the caller.
        return fileChooser.showOpenDialog(owner);
    }

    /* Handles chooseSaveFile. */
    public File chooseSaveFile(Window owner, String defaultName) {
        // Creates a local value for this method.
        FileChooser fileChooser = new FileChooser();
        // Sets a value on the object.
        fileChooser.setTitle("Save Result");

        // Checks the condition before continuing.
        if (defaultName != null && !defaultName.isBlank()) {
            // Sets a value on the object.
            fileChooser.setInitialFileName(defaultName);
        }

        // Returns the result to the caller.
        return fileChooser.showSaveDialog(owner);
    }

    /* Handles readFileBytes. */
    public byte[] readFileBytes(File file) throws IOException {
        // Checks the condition before continuing.
        if (file == null) {
            // Throws an error for invalid input.
            throw new IllegalArgumentException("File must not be null.");
        }
        // Checks the condition before continuing.
        if (!isSupportedFile(file)) {
            // Throws an error for invalid input.
            throw new IllegalArgumentException("Unsupported file type. Supported files are .txt, .docx, .des, .png, .jpg, .jpeg, .mp3, .wav, and .mp4.");
        }

        // Raw bytes work for every file type: text, images, audio, and video.
        // Returns the result to the caller.
        return Files.readAllBytes(file.toPath());
    }

    /* Handles writeFileBytes. */
    public void writeFileBytes(File file, byte[] data) throws IOException {
        // Checks the condition before continuing.
        if (file == null) {
            // Throws an error for invalid input.
            throw new IllegalArgumentException("File must not be null.");
        }
        // Checks the condition before continuing.
        if (data == null) {
            // Throws an error for invalid input.
            throw new IllegalArgumentException("Data must not be null.");
        }

        // Write the exact bytes produced by encryption/decryption.
        // Runs this line of the method.
        Files.write(file.toPath(), data);
    }

    /* Handles isSupportedFile. */
    public boolean isSupportedFile(File file) {
        // Checks the condition before continuing.
        if (file == null) {
            // Returns the result to the caller.
            return false;
        }

        // Creates a local value for this method.
        String name = file.getName().toLowerCase();
        // Returns the result to the caller.
        return name.endsWith(".txt")
                // Runs this line of the method.
                || name.endsWith(".docx")
                // Runs this line of the method.
                || name.endsWith(".des")
                // Runs this line of the method.
                || name.endsWith(".png")
                // Runs this line of the method.
                || name.endsWith(".jpg")
                // Runs this line of the method.
                || name.endsWith(".jpeg")
                // Runs this line of the method.
                || name.endsWith(".mp3")
                // Runs this line of the method.
                || name.endsWith(".wav");
    }
}

