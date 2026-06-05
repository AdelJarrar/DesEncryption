package com.example.desencryption;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

public class SmsService {

    private static final String PHONE_PATTERN = "\\+[1-9][0-9]{7,14}";
    private static final String KEY_PATTERN = "[0-9a-fA-F]{16}";

    /* Handles sendKeySms. */
    public String sendKeySms(String recipientPhoneNumber, String keyHex) {
        // Creates a local value for this method.
        String recipient = validateRecipientPhoneNumber(recipientPhoneNumber);
        // Creates a local value for this method.
        String key = validateKey(keyHex);
        // Runs this line of the method.
        validateTwilioConfig();

        // Runs this line of the method.
        Twilio.init(TwilioConfig.ACCOUNT_SID, TwilioConfig.AUTH_TOKEN);

        // Creates a local value for this method.
        Message message = Message.creator(
                // Runs this line of the method.
                new PhoneNumber(recipient),
                // Runs this line of the method.
                new PhoneNumber(TwilioConfig.FROM_PHONE),
                // Runs this line of the method.
                "DES key: " + key
        // Runs this line of the method.
        ).create();

        // Returns the result to the caller.
        return message.getSid();
    }

    /* Handles validateRecipientPhoneNumber. */
    public String validateRecipientPhoneNumber(String recipientPhoneNumber) {
        // Checks the condition before continuing.
        if (recipientPhoneNumber == null || recipientPhoneNumber.trim().isEmpty()) {
            // Throws an error for invalid input.
            throw new IllegalArgumentException("Enter the recipient phone number first.");
        }

        // Creates a local value for this method.
        String cleanedPhoneNumber = recipientPhoneNumber.trim();
        // Checks the condition before continuing.
        if (!cleanedPhoneNumber.matches(PHONE_PATTERN)) {
            // Throws an error for invalid input.
            throw new IllegalArgumentException("Phone number must use international format, for example +970599123456.");
        }

        // Returns the result to the caller.
        return cleanedPhoneNumber;
    }

    /* Handles validateKey. */
    public String validateKey(String keyHex) {
        // Checks the condition before continuing.
        if (keyHex == null || keyHex.trim().isEmpty()) {
            // Throws an error for invalid input.
            throw new IllegalArgumentException("Enter or generate a DES key first.");
        }

        // Creates a local value for this method.
        String cleanedKey = keyHex.trim();
        // Checks the condition before continuing.
        if (!cleanedKey.matches(KEY_PATTERN)) {
            // Throws an error for invalid input.
            throw new IllegalArgumentException("DES key must be exactly 16 hexadecimal characters.");
        }

        // Returns the result to the caller.
        return cleanedKey.toUpperCase();
    }

    /* Handles validateTwilioConfig. */
    public void validateTwilioConfig() {
        // Runs this line of the method.
        validateConfigValue(TwilioConfig.ACCOUNT_SID, "PUT_ACCOUNT_SID_HERE", "Twilio Account SID");
        // Runs this line of the method.
        validateConfigValue(TwilioConfig.AUTH_TOKEN, "PUT_AUTH_TOKEN_HERE", "Twilio Auth Token");
        // Runs this line of the method.
        validateConfigValue(TwilioConfig.FROM_PHONE, "PUT_TWILIO_PHONE_HERE", "Twilio sender phone number");
        // Runs this line of the method.
        validateRecipientPhoneNumber(TwilioConfig.FROM_PHONE);
    }

    /* Handles validateConfigValue. */
    private void validateConfigValue(String value, String placeholder, String label) {
        // Checks the condition before continuing.
        if (value == null || value.trim().isEmpty() || value.equals(placeholder)) {
            // Throws an error for invalid input.
            throw new IllegalArgumentException(label + " is not set in TwilioConfig.");
        }
    }
}

