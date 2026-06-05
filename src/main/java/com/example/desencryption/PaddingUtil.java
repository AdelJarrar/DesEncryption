package com.example.desencryption;

public final class PaddingUtil {

    private static final int BLOCK_SIZE = 8;

    /* Handles PaddingUtil. */
    private PaddingUtil() {
        // Stops callers from creating this utility class.
    }

    /* Handles addPkcs5Padding. */
    public static byte[] addPkcs5Padding(byte[] data) {
        // Checks the condition before continuing.
        if (data == null) {
            // Throws an error for invalid input.
            throw new IllegalArgumentException("Data must not be null.");
        }

        // Padding length is also the value stored inside each padding byte.
        // Creates a local value for this method.
        int paddingLength = BLOCK_SIZE - (data.length % BLOCK_SIZE);
        // Creates a local value for this method.
        byte[] padded = new byte[data.length + paddingLength];

        // Copy the real data first.
        // Loops through the needed values.
        for (int i = 0; i < data.length; i++) {
            // Stores the value used by this method.
            padded[i] = data[i];
        }

        // Fill the remaining bytes with the padding value.
        // Loops through the needed values.
        for (int i = data.length; i < padded.length; i++) {
            // Stores the value used by this method.
            padded[i] = (byte) paddingLength;
        }

        // Returns the result to the caller.
        return padded;
    }

    /* Handles removePkcs5Padding. */
    public static byte[] removePkcs5Padding(byte[] paddedData) {
        // Checks the condition before continuing.
        if (paddedData == null) {
            // Throws an error for invalid input.
            throw new IllegalArgumentException("Padded data must not be null.");
        }
        // Checks the condition before continuing.
        if (paddedData.length == 0 || paddedData.length % BLOCK_SIZE != 0) {
            // Throws an error for invalid input.
            throw new IllegalArgumentException("Padded data length must be a non-empty multiple of 8.");
        }

        // The final byte tells us how many padding bytes were added.
        // Creates a local value for this method.
        int paddingLength = paddedData[paddedData.length - 1] & 0xFF;

        // Checks the condition before continuing.
        if (paddingLength < 1 || paddingLength > BLOCK_SIZE) {
            // Throws an error for invalid input.
            throw new IllegalArgumentException("Invalid PKCS5 padding length.");
        }

        // Creates a local value for this method.
        int originalLength = paddedData.length - paddingLength;

        // Each padding byte must equal the padding length.
        // Loops through the needed values.
        for (int i = originalLength; i < paddedData.length; i++) {
            // Checks the condition before continuing.
            if ((paddedData[i] & 0xFF) != paddingLength) {
                // Throws an error for invalid input.
                throw new IllegalArgumentException("Invalid PKCS5 padding bytes.");
            }
        }

        // Creates a local value for this method.
        byte[] original = new byte[originalLength];

        // Copy only the real original data and leave the padding behind.
        // Loops through the needed values.
        for (int i = 0; i < originalLength; i++) {
            // Stores the value used by this method.
            original[i] = paddedData[i];
        }

        // Returns the result to the caller.
        return original;
    }
}

