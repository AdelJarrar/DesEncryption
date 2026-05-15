package com.example.desencryption;

/**
 * Stores the Twilio values used for sending SMS messages.
 *
 * <p>For easy project testing, the credentials are kept in one place. Replace
 * the placeholder values with your Twilio Account SID, Auth Token, and Twilio
 * phone number before using the Send Key SMS button.</p>
 */
public final class TwilioConfig {

    /**
     * Twilio Account SID from the Twilio Console.
     */
    public static final String ACCOUNT_SID = "AC6b907ccce9db080acf3602ac68c2c0fb";

    /**
     * Twilio Auth Token from the Twilio Console.
     *
     * <p>Do not share this value publicly because it can be used to access your
     * Twilio account.</p>
     */
    public static final String AUTH_TOKEN = "3a8a97aa634b0d10026037bcb1cd7565";

    /**
     * Twilio phone number used as the SMS sender.
     *
     * <p>The number should be written in international format, for example
     * {@code +12025550123}.</p>
     */
    public static final String FROM_PHONE = "+15722204455";

    private TwilioConfig() {
    }
}
