package com.example.desencryption;

public final class BlockUtil {

    private static final int DES_BLOCK_BYTES = 8;
    private static final int DES_BLOCK_BITS = 64;

    /* Handles BlockUtil. */
    private BlockUtil() {
        // Stops callers from creating this utility class.
    }

    /* Handles bytesToBinaryBlock. */
    public static String bytesToBinaryBlock(byte[] block) {
        // Checks the condition before continuing.
        if (block == null) {
            // Throws an error for invalid input.
            throw new IllegalArgumentException("Block must not be null.");
        }
        // Checks the condition before continuing.
        if (block.length != DES_BLOCK_BYTES) {
            // Throws an error for invalid input.
            throw new IllegalArgumentException("Block must be exactly 8 bytes.");
        }

        // Creates a local value for this method.
        StringBuilder binary = new StringBuilder(DES_BLOCK_BITS);

        // Loops through the needed values.
        for (byte value : block) {
            // Java bytes are signed, so & 0xFF treats the value as an unsigned byte from 0 to 255.
            // Creates a local value for this method.
            String byteBinary = Integer.toBinaryString(value & 0xFF);
            // Appends text to the builder.
            binary.append(String.format("%8s", byteBinary).replace(' ', '0'));
        }

        // Returns the result to the caller.
        return binary.toString();
    }

    /* Handles binaryBlockToBytes. */
    public static byte[] binaryBlockToBytes(String binaryBlock) {
        // Checks the condition before continuing.
        if (binaryBlock == null) {
            // Throws an error for invalid input.
            throw new IllegalArgumentException("Binary block must not be null.");
        }
        // Checks the condition before continuing.
        if (binaryBlock.length() != DES_BLOCK_BITS) {
            // Throws an error for invalid input.
            throw new IllegalArgumentException("Binary block must be exactly 64 bits.");
        }
        // Checks the condition before continuing.
        if (!binaryBlock.matches("[01]+")) {
            // Throws an error for invalid input.
            throw new IllegalArgumentException("Binary block must contain only 0 and 1.");
        }

        // Creates a local value for this method.
        byte[] block = new byte[DES_BLOCK_BYTES];

        // Loops through the needed values.
        for (int i = 0; i < DES_BLOCK_BYTES; i++) {
            // Every 8 bits make one byte.
            // Creates a local value for this method.
            String byteBinary = binaryBlock.substring(i * 8, (i + 1) * 8);
            // Stores the value used by this method.
            block[i] = (byte) Integer.parseInt(byteBinary, 2);
        }

        // Returns the result to the caller.
        return block;
    }
}

