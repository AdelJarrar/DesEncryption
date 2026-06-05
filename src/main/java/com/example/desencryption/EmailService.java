package com.example.desencryption;

import jakarta.activation.DataHandler;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;

import java.util.Properties;

public class EmailService {

    private static final String EMAIL_PATTERN = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$";

    /* Handles sendCipherTextEmail. */
    public void sendCipherTextEmail(String toEmail, String subject, String hexCipherText) {
        // Creates a local value for this method.
        String recipient = validateEmail(toEmail);
        // Creates a local value for this method.
        String safeSubject = validateSubject(subject);
        // Creates a local value for this method.
        String ciphertext = validateCipherText(hexCipherText);
        // Runs this line of the method.
        validateEmailConfig();

        // Starts code that may fail.
        try {
            // Creates a local value for this method.
            Message message = createBaseMessage(recipient, safeSubject);
            // Sets a value on the object.
            message.setText("Here is the encrypted DES ciphertext:\n\n" + ciphertext);
            // Runs this line of the method.
            Transport.send(message);
        // Runs this line of the method.
        } catch (MessagingException ex) {
            // Throws an error for invalid input.
            throw new IllegalArgumentException("Could not send email: " + ex.getMessage(), ex);
        }
    }

    /* Handles sendEncryptedFileEmail. */
    public void sendEncryptedFileEmail(String toEmail, String subject, byte[] encryptedBytes, String fileName) {
        // Creates a local value for this method.
        String recipient = validateEmail(toEmail);
        // Creates a local value for this method.
        String safeSubject = validateSubject(subject);
        // Creates a local value for this method.
        byte[] safeBytes = validateEncryptedBytes(encryptedBytes);
        // Creates a local value for this method.
        String safeFileName = validateFileName(fileName);
        // Runs this line of the method.
        validateEmailConfig();

        // Starts code that may fail.
        try {
            // Creates a local value for this method.
            Message message = createBaseMessage(recipient, safeSubject);

            // Creates a local value for this method.
            MimeBodyPart textPart = new MimeBodyPart();
            // Sets a value on the object.
            textPart.setText("The encrypted DES file is attached.");

            // Creates a local value for this method.
            MimeBodyPart attachmentPart = new MimeBodyPart();
            // Creates a local value for this method.
            ByteArrayDataSource dataSource = new ByteArrayDataSource(safeBytes, "application/octet-stream");
            // Sets a value on the object.
            attachmentPart.setDataHandler(new DataHandler(dataSource));
            // Sets a value on the object.
            attachmentPart.setFileName(safeFileName);

            // Creates a local value for this method.
            Multipart multipart = new MimeMultipart();
            // Adds the value to the collection.
            multipart.addBodyPart(textPart);
            // Adds the value to the collection.
            multipart.addBodyPart(attachmentPart);

            // Sets a value on the object.
            message.setContent(multipart);
            // Runs this line of the method.
            Transport.send(message);
        // Runs this line of the method.
        } catch (MessagingException ex) {
            // Throws an error for invalid input.
            throw new IllegalArgumentException("Could not send email: " + ex.getMessage(), ex);
        }
    }

    /* Handles validateEmail. */
    public String validateEmail(String email) {
        // Checks the condition before continuing.
        if (email == null || email.trim().isEmpty()) {
            // Throws an error for invalid input.
            throw new IllegalArgumentException("Enter the recipient email address first.");
        }

        // Creates a local value for this method.
        String cleanedEmail = email.trim();
        // Checks the condition before continuing.
        if (!cleanedEmail.matches(EMAIL_PATTERN)) {
            // Throws an error for invalid input.
            throw new IllegalArgumentException("Email address format is invalid.");
        }

        // Returns the result to the caller.
        return cleanedEmail;
    }

    /* Handles validateSubject. */
    public String validateSubject(String subject) {
        // Checks the condition before continuing.
        if (subject == null || subject.trim().isEmpty()) {
            // Returns the result to the caller.
            return "DES encrypted output";
        }

        // Returns the result to the caller.
        return subject.trim();
    }

    /* Handles validateCipherText. */
    public String validateCipherText(String hexCipherText) {
        // Checks the condition before continuing.
        if (hexCipherText == null || hexCipherText.trim().isEmpty()) {
            // Throws an error for invalid input.
            throw new IllegalArgumentException("Encrypt text first, then send the ciphertext email.");
        }

        // Creates a local value for this method.
        String cleanedCipherText = hexCipherText.trim();
        // Checks the condition before continuing.
        if (!cleanedCipherText.matches("[0-9a-fA-F]+")) {
            // Throws an error for invalid input.
            throw new IllegalArgumentException("Ciphertext must be hexadecimal.");
        }

        // Returns the result to the caller.
        return cleanedCipherText.toUpperCase();
    }

    /* Handles validateEncryptedBytes. */
    public byte[] validateEncryptedBytes(byte[] encryptedBytes) {
        // Checks the condition before continuing.
        if (encryptedBytes == null || encryptedBytes.length == 0) {
            // Throws an error for invalid input.
            throw new IllegalArgumentException("Encrypt a file first, then send the encrypted file email.");
        }

        // Returns the result to the caller.
        return encryptedBytes;
    }

    /* Handles validateFileName. */
    public String validateFileName(String fileName) {
        // Checks the condition before continuing.
        if (fileName == null || fileName.trim().isEmpty()) {
            // Throws an error for invalid input.
            throw new IllegalArgumentException("Encrypted attachment filename is missing.");
        }

        // Creates a local value for this method.
        String cleanedFileName = fileName.trim();
        // Checks the condition before continuing.
        if (!cleanedFileName.toLowerCase().endsWith(".des")) {
            // Stores the value used by this method.
            cleanedFileName = cleanedFileName + ".des";
        }

        // Returns the result to the caller.
        return cleanedFileName;
    }

    /* Handles validateEmailConfig. */
    public void validateEmailConfig() {
        // Runs this line of the method.
        validateConfigValue(EmailConfig.FROM_EMAIL, "PUT_GMAIL_HERE", "Gmail sender email");
        // Runs this line of the method.
        validateConfigValue(EmailConfig.APP_PASSWORD, "PUT_GMAIL_APP_PASSWORD_HERE", "Gmail app password");
        // Runs this line of the method.
        validateEmail(EmailConfig.FROM_EMAIL);
    }

    /* Handles createBaseMessage. */
    private Message createBaseMessage(String recipient, String subject) throws MessagingException {
        // Creates a local value for this method.
        Session session = createSession();
        // Creates a local value for this method.
        Message message = new MimeMessage(session);
        // Sets a value on the object.
        message.setFrom(new InternetAddress(EmailConfig.FROM_EMAIL));
        // Sets a value on the object.
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
        // Sets a value on the object.
        message.setSubject(subject);
        // Returns the result to the caller.
        return message;
    }

    /* Handles createSession. */
    private Session createSession() {
        // Creates a local value for this method.
        Properties properties = new Properties();
        // Stores an entry in the properties.
        properties.put("mail.smtp.auth", "true");
        // Stores an entry in the properties.
        properties.put("mail.smtp.starttls.enable", "true");
        // Stores an entry in the properties.
        properties.put("mail.smtp.host", "smtp.gmail.com");
        // Stores an entry in the properties.
        properties.put("mail.smtp.port", "587");

        // Returns the result to the caller.
        return Session.getInstance(properties, new Authenticator() {
            @Override
            /* Handles getPasswordAuthentication. */
            protected PasswordAuthentication getPasswordAuthentication() {
                // Returns the result to the caller.
                return new PasswordAuthentication(EmailConfig.FROM_EMAIL, EmailConfig.APP_PASSWORD);
            }
        });
    }

    /* Handles validateConfigValue. */
    private void validateConfigValue(String value, String placeholder, String label) {
        // Checks the condition before continuing.
        if (value == null || value.trim().isEmpty() || value.equals(placeholder)) {
            // Throws an error for invalid input.
            throw new IllegalArgumentException(label + " is not set in EmailConfig.");
        }
    }
}

