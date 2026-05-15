package com.example.desencryption;

import java.math.BigInteger;

/**
 * Generic helper methods for binary-string operations used by DES.
 *
 * This class does not contain DES tables or cipher-round logic. It only
 * handles reusable bit manipulation such as permutation, XOR, conversion, and
 * splitting.
 */
public final class BitUtil {

    private BitUtil() {
    }

    /**
     * Applies a 1-based permutation table to the input string.
     *
     *  input the source string to permute
     *  table the 1-based positions to read from input
     * return the permuted string
     * throws IllegalArgumentException if input or table is null, or a table
     *                                  position is outside the input bounds
     */
    public static String applyPermutation(String input, int[] table) {
        if (input == null) {
            throw new IllegalArgumentException("Input must not be null.");
        }
        if (table == null) {
            throw new IllegalArgumentException("Permutation table must not be null.");
        }

        StringBuilder result = new StringBuilder(table.length);

        for (int position : table) {
            if (position < 1 || position > input.length()) {
                throw new IllegalArgumentException(
                        "Permutation position must be between 1 and input length."
                );
            }

            result.append(input.charAt(position - 1));
        }

        return result.toString();
    }

    /**
     * Computes bitwise XOR for two equal-length binary strings.
     *
     * param left  the first binary string
     * param right the second binary string
     * return the XOR result as a binary string
     * throws IllegalArgumentException if either value is null, contains a
     *                                  non-binary character, or lengths differ
     */
    public static String xor(String left, String right) {
        validateBinary(left, "Left value");
        validateBinary(right, "Right value");

        if (left.length() != right.length()) {
            throw new IllegalArgumentException("Binary values must have the same length.");
        }

        StringBuilder result = new StringBuilder(left.length());

        for (int i = 0; i < left.length(); i++) {
            result.append(left.charAt(i) == right.charAt(i) ? '0' : '1');
        }

        return result.toString();
    }

    /**
     * Converts a hexadecimal string into a fixed-length binary string.
     *
     * param hex          the hexadecimal value to convert
     * param expectedBits the exact number of bits to return
     * return the binary value padded with leading zeroes
     * throws IllegalArgumentException if the value is null, not hexadecimal,
     *                                  expectedBits is negative, or the value
     *                                  does not fit in expectedBits
     */
    public static String hexToBinary(String hex, int expectedBits) {
        if (hex == null) {
            throw new IllegalArgumentException("Hex value must not be null.");
        }
        if (expectedBits < 0) {
            throw new IllegalArgumentException("Expected bit length must not be negative.");
        }
        if (!hex.matches("[0-9a-fA-F]+")) {
            throw new IllegalArgumentException("Hex value must contain only hexadecimal characters.");
        }

        String binary = new BigInteger(hex, 16).toString(2);

        if (binary.length() > expectedBits) {
            throw new IllegalArgumentException("Hex value does not fit in the expected bit length.");
        }

        return String.format("%" + expectedBits + "s", binary).replace(' ', '0');
    }

    /**
     * Converts a binary string into uppercase hexadecimal.
     *
     * The output is padded to preserve complete 4-bit groups. For example,
     * {code 00000001} becomes {@code 01}.
     *
     * param binary the binary value to convert
     * return the uppercase hexadecimal value
     * throws IllegalArgumentException if binary is null or contains a
     *                                  non-binary character
     */
    public static String binaryToHex(String binary) {
        validateBinary(binary, "Binary value");

        int hexLength = (int) Math.ceil(binary.length() / 4.0);
        String hex = new BigInteger(binary, 2).toString(16).toUpperCase();

        return String.format("%" + hexLength + "s", hex).replace(' ', '0');
    }

    /**
     * Converts raw bytes into uppercase hexadecimal text.
     *
     * This is useful for showing encrypted text in the UI, because encrypted
     * bytes may contain characters that cannot be displayed safely.
     *
     * param bytes the bytes to convert
     * return the bytes as uppercase hexadecimal text
     * throws IllegalArgumentException if bytes is null
     */
    public static String bytesToHex(byte[] bytes) {
        if (bytes == null) {
            throw new IllegalArgumentException("Bytes must not be null.");
        }

        StringBuilder hex = new StringBuilder(bytes.length * 2);
        for (byte value : bytes) {
            hex.append(String.format("%02X", value & 0xFF));
        }

        return hex.toString();
    }

    /**
     * Converts hexadecimal text back into raw bytes.
     *
     * param hex the hexadecimal text to convert
     * return the decoded bytes
     * throws IllegalArgumentException if hex is null, has odd length, or
     *                                  contains non-hexadecimal characters
     */
    public static byte[] hexToBytes(String hex) {
        if (hex == null) {
            throw new IllegalArgumentException("Hex value must not be null.");
        }
        if (hex.length() % 2 != 0) {
            throw new IllegalArgumentException("Hex value must have an even number of characters.");
        }
        if (!hex.matches("[0-9a-fA-F]*")) {
            throw new IllegalArgumentException("Hex value must contain only hexadecimal characters.");
        }

        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            int start = i * 2;
            String hexByte = hex.substring(start, start + 2);
            bytes[i] = (byte) Integer.parseInt(hexByte, 16);
        }

        return bytes;
    }

    /**
     * Returns the first half of an even-length string.
     *
     * param input the value to split
     * return the left half
     * throws IllegalArgumentException if input is null or has odd length
     */
    public static String leftHalf(String input) {
        validateEvenLength(input);
        return input.substring(0, input.length() / 2);
    }

    /**
     * Returns the second half of an even-length string.
     *
     * param input the value to split
     * return the right half
     * throws IllegalArgumentException if input is null or has odd length
     */
    public static String rightHalf(String input) {
        validateEvenLength(input);
        return input.substring(input.length() / 2);
    }

    private static void validateBinary(String value, String label) {
        if (value == null) {
            throw new IllegalArgumentException(label + " must not be null.");
        }
        if (!value.matches("[01]+")) {
            throw new IllegalArgumentException(label + " must contain only 0 and 1.");
        }
    }

    private static void validateEvenLength(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Input must not be null.");
        }
        if (input.length() % 2 != 0) {
            throw new IllegalArgumentException("Input length must be even.");
        }
    }
}
