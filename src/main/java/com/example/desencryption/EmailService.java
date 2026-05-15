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

/**
 * Sends encrypted DES output by email using Gmail SMTP.
 *
 * SMS sends the DES key. Email sends the ciphertext or encrypted file.
 */
public class EmailService {

    private static final String EMAIL_PATTERN = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$";

    /**
     * Sends hexadecimal ciphertext as normal email text.
     *
     * param toEmail       recipient email address
     * param subject       email subject
     * param hexCipherText encrypted text shown as hexadecimal
     */
    public void sendCipherTextEmail(String toEmail, String subject, String hexCipherText) {
        String recipient = validateEmail(toEmail);
        String safeSubject = validateSubject(subject);
        String ciphertext = validateCipherText(hexCipherText);
        validateEmailConfig();

        try {
            Message message = createBaseMessage(recipient, safeSubject);
            message.setText("Here is the encrypted DES ciphertext:\n\n" + ciphertext);
            Transport.send(message);
        } catch (MessagingException ex) {
            throw new IllegalArgumentException("Could not send email: " + ex.getMessage(), ex);
        }
    }

    /**
     * Sends encrypted file bytes as a .des attachment.
     *
     * param toEmail        recipient email address
     * param subject        email subject
     * param encryptedBytes encrypted file bytes
     * param fileName       attachment filename
     */
    public void sendEncryptedFileEmail(String toEmail, String subject, byte[] encryptedBytes, String fileName) {
        String recipient = validateEmail(toEmail);
        String safeSubject = validateSubject(subject);
        byte[] safeBytes = validateEncryptedBytes(encryptedBytes);
        String safeFileName = validateFileName(fileName);
        validateEmailConfig();

        try {
            Message message = createBaseMessage(recipient, safeSubject);

            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText("The encrypted DES file is attached.");

            MimeBodyPart attachmentPart = new MimeBodyPart();
            ByteArrayDataSource dataSource = new ByteArrayDataSource(safeBytes, "application/octet-stream");
            attachmentPart.setDataHandler(new DataHandler(dataSource));
            attachmentPart.setFileName(safeFileName);

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(textPart);
            multipart.addBodyPart(attachmentPart);

            message.setContent(multipart);
            Transport.send(message);
        } catch (MessagingException ex) {
            throw new IllegalArgumentException("Could not send email: " + ex.getMessage(), ex);
        }
    }

    /**
     * Validates a recipient or sender email address.
     */
    public String validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Enter the recipient email address first.");
        }

        String cleanedEmail = email.trim();
        if (!cleanedEmail.matches(EMAIL_PATTERN)) {
            throw new IllegalArgumentException("Email address format is invalid.");
        }

        return cleanedEmail;
    }

    /**
     * Validates email subject text.
     */
    public String validateSubject(String subject) {
        if (subject == null || subject.trim().isEmpty()) {
            return "DES encrypted output";
        }

        return subject.trim();
    }

    /**
     * Validates encrypted text before sending it in the email body.
     */
    public String validateCipherText(String hexCipherText) {
        if (hexCipherText == null || hexCipherText.trim().isEmpty()) {
            throw new IllegalArgumentException("Encrypt text first, then send the ciphertext email.");
        }

        String cleanedCipherText = hexCipherText.trim();
        if (!cleanedCipherText.matches("[0-9a-fA-F]+")) {
            throw new IllegalArgumentException("Ciphertext must be hexadecimal.");
        }

        return cleanedCipherText.toUpperCase();
    }

    /**
     * Validates encrypted file bytes before attaching them to an email.
     */
    public byte[] validateEncryptedBytes(byte[] encryptedBytes) {
        if (encryptedBytes == null || encryptedBytes.length == 0) {
            throw new IllegalArgumentException("Encrypt a file first, then send the encrypted file email.");
        }

        return encryptedBytes;
    }

    /**
     * Validates the attachment filename.
     */
    public String validateFileName(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IllegalArgumentException("Encrypted attachment filename is missing.");
        }

        String cleanedFileName = fileName.trim();
        if (!cleanedFileName.toLowerCase().endsWith(".des")) {
            cleanedFileName = cleanedFileName + ".des";
        }

        return cleanedFileName;
    }

    /**
     * Makes sure Gmail credentials were filled in.
     */
    public void validateEmailConfig() {
        validateConfigValue(EmailConfig.FROM_EMAIL, "PUT_GMAIL_HERE", "Gmail sender email");
        validateConfigValue(EmailConfig.APP_PASSWORD, "PUT_GMAIL_APP_PASSWORD_HERE", "Gmail app password");
        validateEmail(EmailConfig.FROM_EMAIL);
    }

    private Message createBaseMessage(String recipient, String subject) throws MessagingException {
        Session session = createSession();
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(EmailConfig.FROM_EMAIL));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
        message.setSubject(subject);
        return message;
    }

    private Session createSession() {
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");

        return Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EmailConfig.FROM_EMAIL, EmailConfig.APP_PASSWORD);
            }
        });
    }

    private void validateConfigValue(String value, String placeholder, String label) {
        if (value == null || value.trim().isEmpty() || value.equals(placeholder)) {
            throw new IllegalArgumentException(label + " is not set in EmailConfig.");
        }
    }
}
