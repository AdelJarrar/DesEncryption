package com.example.desencryption;

import java.math.BigInteger;

public final class BitUtil {

    /* Handles BitUtil. */
    private BitUtil() {
        // Stops callers from creating this utility class.
    }

    /* Handles applyPermutation. */
    public static String applyPermutation(String input, int[] table) {
        // Checks the condition before continuing.
        if (input == null) {
            // Throws an error for invalid input.
            throw new IllegalArgumentException("Input must not be null.");
        }
        // Checks the condition before continuing.
        if (table == null) {
            // Throws an error for invalid input.
            throw new IllegalArgumentException("Permutation table must not be null.");
        }

        // Creates a local value for this method.
        StringBuilder result = new StringBuilder(table.length);

        // Loops through the needed values.
        for (int position : table) {
            // Checks the condition before continuing.
            if (position < 1 || position > input.length()) {
                // Throws an error for invalid input.
                throw new IllegalArgumentException(
                        // Runs this line of the method.
                        "Permutation position must be between 1 and input length."
                );
            }

            // Appends text to the builder.
            result.append(input.charAt(position - 1));
        }

        // Returns the result to the caller.
        return result.toString();
    }

    /* Handles xor. */
    public static String xor(String left, String right) {
        // Runs this line of the method.
        validateBinary(left, "Left value");
        // Runs this line of the method.
        validateBinary(right, "Right value");

        // Checks the condition before continuing.
        if (left.length() != right.length()) {
            // Throws an error for invalid input.
            throw new IllegalArgumentException("Binary values must have the same length.");
        }

        // Creates a local value for this method.
        StringBuilder result = new StringBuilder(left.length());

        // Loops through the needed values.
        for (int i = 0; i < left.length(); i++) {
            // Stores the value used by this method.
            result.append(left.charAt(i) == right.charAt(i) ? '0' : '1');
        }

        // Returns the result to the caller.
        return result.toString();
    }

    /* Handles hexToBinary. */
    public static String hexToBinary(String hex, int expectedBits) {
        // Checks the condition before continuing.
        if (hex == null) {
            // Throws an error for invalid input.
            throw new IllegalArgumentException("Hex value must not be null.");
        }
        // Checks the condition before continuing.
        if (expectedBits < 0) {
            // Throws an error for invalid input.
            throw new IllegalArgumentException("Expected bit length must not be negative.");
        }
        // Checks the condition before continuing.
        if (!hex.matches("[0-9a-fA-F]+")) {
            // Throws an error for invalid input.
            throw new IllegalArgumentException("Hex value must contain only hexadecimal characters.");
        }

        // Creates a local value for this method.
        String binary = new BigInteger(hex, 16).toString(2);

        // Checks the condition before continuing.
        if (binary.length() > expectedBits) {
            // Throws an error for invalid input.
            throw new IllegalArgumentException("Hex value does not fit in the expected bit length.");
        }

        // Returns the result to the caller.
        return String.format("%" + expectedBits + "s", binary).replace(' ', '0');
    }

    /* Handles binaryToHex. */
    public static String binaryToHex(String binary) {
        // Runs this line of the method.
        validateBinary(binary, "Binary value");

        // Creates a local value for this method.
        int hexLength = (int) Math.ceil(binary.length() / 4.0);
        // Creates a local value for this method.
        String hex = new BigInteger(binary, 2).toString(16).toUpperCase();

        // Returns the result to the caller.
        return String.format("%" + hexLength + "s", hex).replace(' ', '0');
    }

    /* Handles bytesToHex. */
    public static String bytesToHex(byte[] bytes) {
        // Checks the condition before continuing.
        if (bytes == null) {
            // Throws an error for invalid input.
            throw new IllegalArgumentException("Bytes must not be null.");
        }

        // Creates a local value for this method.
        StringBuilder hex = new StringBuilder(bytes.length * 2);
        // Loops through the needed values.
        for (byte value : bytes) {
            // Appends text to the builder.
            hex.append(String.format("%02X", value & 0xFF));
        }

        // Returns the result to the caller.
        return hex.toString();
    }

    /* Handles hexToBytes. */
    public static byte[] hexToBytes(String hex) {
        // Checks the condition before continuing.
        if (hex == null) {
            // Throws an error for invalid input.
            throw new IllegalArgumentException("Hex value must not be null.");
        }
        // Checks the condition before continuing.
        if (hex.length() % 2 != 0) {
            // Throws an error for invalid input.
            throw new IllegalArgumentException("Hex value must have an even number of characters.");
        }
        // Checks the condition before continuing.
        if (!hex.matches("[0-9a-fA-F]*")) {
            // Throws an error for invalid input.
            throw new IllegalArgumentException("Hex value must contain only hexadecimal characters.");
        }

        // Creates a local value for this method.
        byte[] bytes = new byte[hex.length() / 2];
        // Loops through the needed values.
        for (int i = 0; i < bytes.length; i++) {
            // Creates a local value for this method.
            int start = i * 2;
            // Creates a local value for this method.
            String hexByte = hex.substring(start, start + 2);
            // Stores the value used by this method.
            bytes[i] = (byte) Integer.parseInt(hexByte, 16);
        }

        // Returns the result to the caller.
        return bytes;
    }

    /* Handles leftHalf. */
    public static String leftHalf(String input) {
        // Runs this line of the method.
        validateEvenLength(input);
        // Returns the result to the caller.
        return input.substring(0, input.length() / 2);
    }

    /* Handles rightHalf. */
    public static String rightHalf(String input) {
        // Runs this line of the method.
        validateEvenLength(input);
        // Returns the result to the caller.
        return input.substring(input.length() / 2);
    }

    /* Handles validateBinary. */
    private static void validateBinary(String value, String label) {
        // Checks the condition before continuing.
        if (value == null) {
            // Throws an error for invalid input.
            throw new IllegalArgumentException(label + " must not be null.");
        }
        // Checks the condition before continuing.
        if (!value.matches("[01]+")) {
            // Throws an error for invalid input.
            throw new IllegalArgumentException(label + " must contain only 0 and 1.");
        }
    }

    /* Handles validateEvenLength. */
    private static void validateEvenLength(String input) {
        // Checks the condition before continuing.
        if (input == null) {
            // Throws an error for invalid input.
            throw new IllegalArgumentException("Input must not be null.");
        }
        // Checks the condition before continuing.
        if (input.length() % 2 != 0) {
            // Throws an error for invalid input.
            throw new IllegalArgumentException("Input length must be even.");
        }
    }
}

