package com.example.desencryption;

/**
 * Converts between 8-byte blocks and the 64-bit binary strings used by DES.
 */
public final class BlockUtil {

    private static final int DES_BLOCK_BYTES = 8;
    private static final int DES_BLOCK_BITS = 64;

    private BlockUtil() {
    }

    /**
     * Converts exactly 8 bytes into a 64-bit binary string.
     *
     * @param block the 8-byte block to convert
     * @return the 64-bit binary representation
     * @throws IllegalArgumentException if block is null or not exactly 8 bytes
     */
    public static String bytesToBinaryBlock(byte[] block) {
        if (block == null) {
            throw new IllegalArgumentException("Block must not be null.");
        }
        if (block.length != DES_BLOCK_BYTES) {
            throw new IllegalArgumentException("Block must be exactly 8 bytes.");
        }

        StringBuilder binary = new StringBuilder(DES_BLOCK_BITS);

        for (byte value : block) {
            String byteBinary = Integer.toBinaryString(value & 0xFF);
            binary.append(String.format("%8s", byteBinary).replace(' ', '0'));
        }

        return binary.toString();
    }

    /**
     * Converts a 64-bit binary string into exactly 8 bytes.
     *
     * @param binaryBlock the 64-bit binary block to convert
     * @return the 8-byte representation
     * @throws IllegalArgumentException if binaryBlock is null, not exactly
     *                                  64 bits, or contains non-binary chars
     */
    public static byte[] binaryBlockToBytes(String binaryBlock) {
        if (binaryBlock == null) {
            throw new IllegalArgumentException("Binary block must not be null.");
        }
        if (binaryBlock.length() != DES_BLOCK_BITS) {
            throw new IllegalArgumentException("Binary block must be exactly 64 bits.");
        }
        if (!binaryBlock.matches("[01]+")) {
            throw new IllegalArgumentException("Binary block must contain only 0 and 1.");
        }

        byte[] block = new byte[DES_BLOCK_BYTES];

        for (int i = 0; i < DES_BLOCK_BYTES; i++) {
            String byteBinary = binaryBlock.substring(i * 8, (i + 1) * 8);
            block[i] = (byte) Integer.parseInt(byteBinary, 2);
        }

        return block;
    }
}
