package com.example.desencryption;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class MainUI  extends BorderPane {
    public MainUI() {
        Label titleLabel = new Label("Des Encryption");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label inputLabel = new Label("Enter text:");
        TextField textField = new TextField();
        textField.setPromptText("Enter text here...");

        Label keyLabel = new Label("Enter Key:");
        TextField keyField = new TextField();
        keyField.setPromptText("Enter key here...");


        Button runTest = new Button("Read Image, Audio, Video");
        Button encryptButton = new Button("Encrypt");
        Button decryptButton = new Button("Decrypt");
        Button loadTextButton = new Button("Load Text");
        Button loadKeyButton = new Button("Load Key");
        Button saveResultButton = new Button("Save Result");


        Label resultLabel = new Label("Result:");
        TextArea resultArea = new TextArea();
        resultArea.setEditable(false);
        resultArea.setWrapText(true);
        resultArea.setPrefHeight(500);

        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(
                encryptButton, decryptButton, runTest,
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
}
