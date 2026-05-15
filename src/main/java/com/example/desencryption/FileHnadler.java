package com.example.desencryption;

import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Handles choosing, reading, and writing files for the JavaFX UI.
 *
 * This class does not encrypt or decrypt data. It only lets the user choose
 * files with JavaFX dialogs and converts selected files to raw byte arrays that
 * can be passed to {link DesDataService}. Text files, images, audio, and video
 * are all read the same way: as bytes.
 */
public class FileHnadler {

    /**
     * Opens a chooser for any file the application can encrypt or decrypt.
     *
     * param owner the window that owns the dialog
     * return the selected file, or null if the user cancels
     */
    public File chooseAnyFile(Window owner) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose File");

        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "Supported Files",
                        "*.txt", "*.docx", "*.des", "*.png", "*.jpg", "*.jpeg", "*.mp3", "*.wav", "*.mp4"
                )
        );
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        return fileChooser.showOpenDialog(owner);
    }

    /**
     * Opens a chooser for text files.
     *
     * param owner the window that owns the dialog
     * return the selected file, or null if the user cancels
     */
    public File chooseTextFile(Window owner) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Text File");

        // Filters make the dialog easier to use, but the file is still read as bytes.
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Text Files", "*.txt")
        );
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        return fileChooser.showOpenDialog(owner);
    }

    /**
     * Opens a save dialog.
     *
     * param owner       the window that owns the dialog
     * param defaultName suggested output filename
     * return the selected output file, or null if the user cancels
     */
    public File chooseSaveFile(Window owner, String defaultName) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Result");

        if (defaultName != null && !defaultName.isBlank()) {
            fileChooser.setInitialFileName(defaultName);
        }

        return fileChooser.showSaveDialog(owner);
    }

    /**
     * Reads a file as raw bytes.
     *
     * param file the file to read
     * return the file contents
     * throws IOException              if reading fails
     * throws IllegalArgumentException if file is null
     */
    public byte[] readFileBytes(File file) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("File must not be null.");
        }
        if (!isSupportedFile(file)) {
            throw new IllegalArgumentException("Unsupported file type. Supported files are .txt, .docx, .des, .png, .jpg, .jpeg, .mp3, .wav, and .mp4.");
        }

        // Raw bytes work for every file type: text, images, audio, and video.
        return Files.readAllBytes(file.toPath());
    }

    /**
     * Writes raw bytes to a file.
     *
     * param file the file to write
     * param data the bytes to write
     * throws IOException              if writing fails
     * throws IllegalArgumentException if file or data is null
     */
    public void writeFileBytes(File file, byte[] data) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("File must not be null.");
        }
        if (data == null) {
            throw new IllegalArgumentException("Data must not be null.");
        }

        // Write the exact bytes produced by encryption/decryption.
        Files.write(file.toPath(), data);
    }

    /**
     * Checks whether this project officially supports the selected file type.
     *
     * param file the file to check
     * return true when the file extension is supported
     */
    public boolean isSupportedFile(File file) {
        if (file == null) {
            return false;
        }

        String name = file.getName().toLowerCase();
        return name.endsWith(".txt")
                || name.endsWith(".docx")
                || name.endsWith(".des")
                || name.endsWith(".png")
                || name.endsWith(".jpg")
                || name.endsWith(".jpeg")
                || name.endsWith(".mp3")
                || name.endsWith(".wav")
                || name.endsWith(".mp4");
    }
}
