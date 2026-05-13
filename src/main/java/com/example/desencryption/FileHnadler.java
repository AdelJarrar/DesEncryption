package com.example.desencryption;

import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Handles choosing, reading, and writing files for the JavaFX UI.
 *
 * <p>This class does not encrypt or decrypt data. It only lets the user choose
 * files with JavaFX dialogs and converts selected files to raw byte arrays that
 * can be passed to {@link DesDataService}.</p>
 */
public class FileHnadler {

    /**
     * Opens a chooser for text files.
     *
     * @param owner the window that owns the dialog
     * @return the selected file, or null if the user cancels
     */
    public File chooseTextFile(Window owner) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Text File");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Text Files", "*.txt")
        );
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        return fileChooser.showOpenDialog(owner);
    }

    /**
     * Opens a chooser for common image, audio, and video files.
     *
     * @param owner the window that owns the dialog
     * @return the selected file, or null if the user cancels
     */
    public File chooseMediaFile(Window owner) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Image, Audio, or Video File");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "Media Files",
                        "*.png", "*.jpg", "*.jpeg", "*.mp3", "*.wav", "*.mp4"
                )
        );
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        return fileChooser.showOpenDialog(owner);
    }

    /**
     * Opens a chooser for encrypted files.
     *
     * @param owner the window that owns the dialog
     * @return the selected file, or null if the user cancels
     */
    public File chooseEncryptedFile(Window owner) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Encrypted File");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("DES Encrypted Files", "*.des")
        );
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        return fileChooser.showOpenDialog(owner);
    }

    /**
     * Opens a save dialog.
     *
     * @param owner       the window that owns the dialog
     * @param defaultName suggested output filename
     * @return the selected output file, or null if the user cancels
     */
    public File chooseSaveFile(Window owner, String defaultName) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Result");
        fileChooser.setInitialFileName(defaultName);
        return fileChooser.showSaveDialog(owner);
    }

    /**
     * Reads a file as raw bytes.
     *
     * @param file the file to read
     * @return the file contents
     * @throws IOException              if reading fails
     * @throws IllegalArgumentException if file is null
     */
    public byte[] readFileBytes(File file) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("File must not be null.");
        }

        return Files.readAllBytes(file.toPath());
    }

    /**
     * Writes raw bytes to a file.
     *
     * @param file the file to write
     * @param data the bytes to write
     * @throws IOException              if writing fails
     * @throws IllegalArgumentException if file or data is null
     */
    public void writeFileBytes(File file, byte[] data) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("File must not be null.");
        }
        if (data == null) {
            throw new IllegalArgumentException("Data must not be null.");
        }

        Files.write(file.toPath(), data);
    }
}
