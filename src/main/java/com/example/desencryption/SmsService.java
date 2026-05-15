package com.example.desencryption;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

/**
 * Sends DES keys by SMS using Twilio.
 *
 * <p>This class keeps SMS code out of the UI. The UI gives it a recipient
 * phone number and a DES key, then this class validates the inputs and calls
 * Twilio.</p>
 */
public class SmsService {

    private static final String PHONE_PATTERN = "\\+[1-9][0-9]{7,14}";
    private static final String KEY_PATTERN = "[0-9a-fA-F]{16}";

    /**
     * Sends the current DES key to a recipient phone number.
     *
     * param recipientPhoneNumber the phone number that should receive the SMS
     * param keyHex               the DES key as 16 hexadecimal characters
     * return the Twilio message SID when Twilio accepts the message
     * throws IllegalArgumentException if the phone number, key, or Twilio
     *                                  config values are invalid
     */
    public String sendKeySms(String recipientPhoneNumber, String keyHex) {
        String recipient = validateRecipientPhoneNumber(recipientPhoneNumber);
        String key = validateKey(keyHex);
        validateTwilioConfig();

        Twilio.init(TwilioConfig.ACCOUNT_SID, TwilioConfig.AUTH_TOKEN);

        Message message = Message.creator(
                new PhoneNumber(recipient),
                new PhoneNumber(TwilioConfig.FROM_PHONE),
                "DES key: " + key
        ).create();

        return message.getSid();
    }

    /**
     * Validates and cleans the recipient phone number.
     *
     * Twilio expects phone numbers in E.164 format, which starts with a plus
     * sign and includes the country code.
     */
    public String validateRecipientPhoneNumber(String recipientPhoneNumber) {
        if (recipientPhoneNumber == null || recipientPhoneNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Enter the recipient phone number first.");
        }

        String cleanedPhoneNumber = recipientPhoneNumber.trim();
        if (!cleanedPhoneNumber.matches(PHONE_PATTERN)) {
            throw new IllegalArgumentException("Phone number must use international format, for example +970599123456.");
        }

        return cleanedPhoneNumber;
    }

    /**
     * Validates and normalizes the DES key before it is sent by SMS.
     */
    public String validateKey(String keyHex) {
        if (keyHex == null || keyHex.trim().isEmpty()) {
            throw new IllegalArgumentException("Enter or generate a DES key first.");
        }

        String cleanedKey = keyHex.trim();
        if (!cleanedKey.matches(KEY_PATTERN)) {
            throw new IllegalArgumentException("DES key must be exactly 16 hexadecimal characters.");
        }

        return cleanedKey.toUpperCase();
    }

    /**
     * Makes sure the code-based Twilio credentials were filled in.
     */
    public void validateTwilioConfig() {
        validateConfigValue(TwilioConfig.ACCOUNT_SID, "PUT_ACCOUNT_SID_HERE", "Twilio Account SID");
        validateConfigValue(TwilioConfig.AUTH_TOKEN, "PUT_AUTH_TOKEN_HERE", "Twilio Auth Token");
        validateConfigValue(TwilioConfig.FROM_PHONE, "PUT_TWILIO_PHONE_HERE", "Twilio sender phone number");
        validateRecipientPhoneNumber(TwilioConfig.FROM_PHONE);
    }

    private void validateConfigValue(String value, String placeholder, String label) {
        if (value == null || value.trim().isEmpty() || value.equals(placeholder)) {
            throw new IllegalArgumentException(label + " is not set in TwilioConfig.");
        }
    }
}
