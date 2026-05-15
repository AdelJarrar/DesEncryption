package com.example.desencryption;

/**
 * Encrypts and decrypts raw byte arrays using DES.
 *
 * This class is the multi-block layer above {link DesCipher}. It does not
 * read files, open dialogs, or interact with the UI. It accepts raw bytes,
 * applies PKCS5 padding before encryption, processes the data in 8-byte DES
 * blocks, and removes PKCS5 padding after decryption.
 */
public class DesDataService {

    private static final int BLOCK_SIZE = 8;

    private final DesCipher desCipher = new DesCipher();

    /**
     * Encrypts any byte array by padding it and processing each 8-byte block.
     *
     * param input  the raw bytes to encrypt
     * param keyHex the DES key as exactly 16 hexadecimal characters
     * return encrypted bytes whose length is a multiple of 8
     * throws IllegalArgumentException if input is null or keyHex is invalid
     */
    public byte[] encryptBytes(byte[] input, String keyHex) {
        if (input == null) {
            throw new IllegalArgumentException("Input must not be null.");
        }

        // Pad once before splitting so every DES block has exactly 8 bytes.
        byte[] paddedInput = PaddingUtil.addPkcs5Padding(input);

        // Generate the 16 encryption round keys once and reuse them for every block.
        KeyUtil keyUtil = new KeyUtil();
        String[] roundKeys = keyUtil.generateRoundKeys(keyHex);

        byte[] encryptedOutput = new byte[paddedInput.length];

        // Walk through the padded input 8 bytes at a time.
        for (int blockStart = 0; blockStart < paddedInput.length; blockStart += BLOCK_SIZE) {
            byte[] currentBlock = new byte[BLOCK_SIZE];

            for (int i = 0; i < BLOCK_SIZE; i++) {
                currentBlock[i] = paddedInput[blockStart + i];
            }

            // Convert the byte block to the binary-string form used by DesCipher.
            String binaryBlock = BlockUtil.bytesToBinaryBlock(currentBlock);

            // Encrypt one DES block.
            String encryptedBinaryBlock = desCipher.cipher(binaryBlock, roundKeys);

            // Convert encrypted bits back to bytes for storage/output.
            byte[] encryptedBlock = BlockUtil.binaryBlockToBytes(encryptedBinaryBlock);

            for (int i = 0; i < BLOCK_SIZE; i++) {
                encryptedOutput[blockStart + i] = encryptedBlock[i];
            }
        }

        return encryptedOutput;
    }

    /**
     * Decrypts bytes produced by encryptBytes().
     *
     * param encryptedInput encrypted bytes; length must be a non-empty multiple of 8
     * param keyHex         the DES key as exactly 16 hexadecimal characters
     * return the original unpadded bytes
     * throws IllegalArgumentException if encryptedInput is null, empty, not a
     *                                  multiple of 8, has invalid padding, or
     *                                  keyHex is invalid
     */
    public byte[] decryptBytes(byte[] encryptedInput, String keyHex) {
        if (encryptedInput == null) {
            throw new IllegalArgumentException("Encrypted input must not be null.");
        }
        if (encryptedInput.length == 0 || encryptedInput.length % BLOCK_SIZE != 0) {
            throw new IllegalArgumentException("Encrypted input length must be a non-empty multiple of 8.");
        }

        // Generate reversed keys once because DES decryption uses K16 down to K1.
        KeyUtil keyUtil = new KeyUtil();
        String[] reversedRoundKeys = keyUtil.generateReversedRoundKeys(keyHex);

        byte[] decryptedWithPadding = new byte[encryptedInput.length];

        // Decrypt every 8-byte block independently, then remove padding at the end.
        for (int blockStart = 0; blockStart < encryptedInput.length; blockStart += BLOCK_SIZE) {
            byte[] currentBlock = new byte[BLOCK_SIZE];

            for (int i = 0; i < BLOCK_SIZE; i++) {
                currentBlock[i] = encryptedInput[blockStart + i];
            }

            // Convert encrypted bytes into the binary-string form used by DesCipher.
            String binaryBlock = BlockUtil.bytesToBinaryBlock(currentBlock);

            // Decrypt one DES block using reversed round keys.
            String decryptedBinaryBlock = desCipher.decrypt(binaryBlock, reversedRoundKeys);

            // Convert decrypted bits back to bytes.
            byte[] decryptedBlock = BlockUtil.binaryBlockToBytes(decryptedBinaryBlock);

            for (int i = 0; i < BLOCK_SIZE; i++) {
                decryptedWithPadding[blockStart + i] = decryptedBlock[i];
            }
        }

        // Padding exists only at the end of the full decrypted data.
        return PaddingUtil.removePkcs5Padding(decryptedWithPadding);
    }
}
