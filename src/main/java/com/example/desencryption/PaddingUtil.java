package com.example.desencryption;

/**
 * Adds and removes PKCS5 padding for DES 8-byte blocks.
 *
 * DES cannot encrypt a short final block. Padding fills the final block so
 * every block has exactly 8 bytes before encryption.
 */
public final class PaddingUtil {

    private static final int BLOCK_SIZE = 8;

    private PaddingUtil() {
    }

    /**
     * Adds PKCS5 padding to data so its length becomes a multiple of 8 bytes.
     *
     * If the data already fits perfectly into 8-byte blocks, PKCS5 still
     * adds one full padding block. That makes it clear during decryption that
     * the final bytes are padding, not real data.
     *
     * param data the original data
     * return a new array containing the original data plus padding
     * throws IllegalArgumentException if data is null
     */
    public static byte[] addPkcs5Padding(byte[] data) {
        if (data == null) {
            throw new IllegalArgumentException("Data must not be null.");
        }

        // Padding length is also the value stored inside each padding byte.
        int paddingLength = BLOCK_SIZE - (data.length % BLOCK_SIZE);
        byte[] padded = new byte[data.length + paddingLength];

        // Copy the real data first.
        for (int i = 0; i < data.length; i++) {
            padded[i] = data[i];
        }

        // Fill the remaining bytes with the padding value.
        for (int i = data.length; i < padded.length; i++) {
            padded[i] = (byte) paddingLength;
        }

        return padded;
    }

    /**
     * Removes PKCS5 padding after validating the final padding bytes.
     *
     * Validation matters because a wrong key can decrypt into random bytes.
     * Invalid padding is one way to detect that the decrypted result is not
     * valid project data.
     *
     * param paddedData data whose length is a non-empty multiple of 8 bytes
     * return a new array containing the original unpadded data
     * throws IllegalArgumentException if paddedData is null, has an invalid
     *                                  length, or contains invalid padding
     */
    public static byte[] removePkcs5Padding(byte[] paddedData) {
        if (paddedData == null) {
            throw new IllegalArgumentException("Padded data must not be null.");
        }
        if (paddedData.length == 0 || paddedData.length % BLOCK_SIZE != 0) {
            throw new IllegalArgumentException("Padded data length must be a non-empty multiple of 8.");
        }

        // The final byte tells us how many padding bytes were added.
        int paddingLength = paddedData[paddedData.length - 1] & 0xFF;

        if (paddingLength < 1 || paddingLength > BLOCK_SIZE) {
            throw new IllegalArgumentException("Invalid PKCS5 padding length.");
        }

        int originalLength = paddedData.length - paddingLength;

        // Each padding byte must equal the padding length.
        for (int i = originalLength; i < paddedData.length; i++) {
            if ((paddedData[i] & 0xFF) != paddingLength) {
                throw new IllegalArgumentException("Invalid PKCS5 padding bytes.");
            }
        }

        byte[] original = new byte[originalLength];

        // Copy only the real original data and leave the padding behind.
        for (int i = 0; i < originalLength; i++) {
            original[i] = paddedData[i];
        }

        return original;
    }
}
