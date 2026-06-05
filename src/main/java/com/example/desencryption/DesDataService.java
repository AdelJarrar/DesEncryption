package com.example.desencryption;

public class DesDataService {

    private static final int BLOCK_SIZE = 8;

    private final DesCipher desCipher = new DesCipher();

    /* Handles encryptBytes. */
    public byte[] encryptBytes(byte[] input, String keyHex) {
        // Checks the condition before continuing.
        if (input == null) {
            // Throws an error for invalid input.
            throw new IllegalArgumentException("Input must not be null.");
        }

        // Pad once before splitting so every DES block has exactly 8 bytes.
        // Creates a local value for this method.
        byte[] paddedInput = PaddingUtil.addPkcs5Padding(input);

        // Generate the 16 encryption round keys once and reuse them for every block.
        // Creates a local value for this method.
        KeyUtil keyUtil = new KeyUtil();
        // Creates a local value for this method.
        String[] roundKeys = keyUtil.generateRoundKeys(keyHex);

        // Creates a local value for this method.
        byte[] encryptedOutput = new byte[paddedInput.length];

        // Walk through the padded input 8 bytes at a time.
        // Loops through the needed values.
        for (int blockStart = 0; blockStart < paddedInput.length; blockStart += BLOCK_SIZE) {
            // Creates a local value for this method.
            byte[] currentBlock = new byte[BLOCK_SIZE];

            // Loops through the needed values.
            for (int i = 0; i < BLOCK_SIZE; i++) {
                // Stores the value used by this method.
                currentBlock[i] = paddedInput[blockStart + i];
            }

            // Convert the byte block to the binary-string form used by DesCipher.
            // Creates a local value for this method.
            String binaryBlock = BlockUtil.bytesToBinaryBlock(currentBlock);

            // Encrypt one DES block.
            // Creates a local value for this method.
            String encryptedBinaryBlock = desCipher.cipher(binaryBlock, roundKeys);

            // Convert encrypted bits back to bytes for storage/output.
            // Creates a local value for this method.
            byte[] encryptedBlock = BlockUtil.binaryBlockToBytes(encryptedBinaryBlock);

            // Loops through the needed values.
            for (int i = 0; i < BLOCK_SIZE; i++) {
                // Stores the value used by this method.
                encryptedOutput[blockStart + i] = encryptedBlock[i];
            }
        }

        // Returns the result to the caller.
        return encryptedOutput;
    }

    /* Handles decryptBytes. */
    public byte[] decryptBytes(byte[] encryptedInput, String keyHex) {
        // Checks the condition before continuing.
        if (encryptedInput == null) {
            // Throws an error for invalid input.
            throw new IllegalArgumentException("Encrypted input must not be null.");
        }
        // Checks the condition before continuing.
        if (encryptedInput.length == 0 || encryptedInput.length % BLOCK_SIZE != 0) {
            // Throws an error for invalid input.
            throw new IllegalArgumentException("Encrypted input length must be a non-empty multiple of 8.");
        }

        // Generate reversed keys once because DES decryption uses K16 down to K1.
        // Creates a local value for this method.
        KeyUtil keyUtil = new KeyUtil();
        // Creates a local value for this method.
        String[] reversedRoundKeys = keyUtil.generateReversedRoundKeys(keyHex);

        // Creates a local value for this method.
        byte[] decryptedWithPadding = new byte[encryptedInput.length];

        // Decrypt every 8-byte block independently, then remove padding at the end.
        // Loops through the needed values.
        for (int blockStart = 0; blockStart < encryptedInput.length; blockStart += BLOCK_SIZE) {
            // Creates a local value for this method.
            byte[] currentBlock = new byte[BLOCK_SIZE];

            // Loops through the needed values.
            for (int i = 0; i < BLOCK_SIZE; i++) {
                // Stores the value used by this method.
                currentBlock[i] = encryptedInput[blockStart + i];
            }

            // Convert encrypted bytes into the binary-string form used by DesCipher.
            // Creates a local value for this method.
            String binaryBlock = BlockUtil.bytesToBinaryBlock(currentBlock);

            // Decrypt one DES block using reversed round keys.
            // Creates a local value for this method.
            String decryptedBinaryBlock = desCipher.decrypt(binaryBlock, reversedRoundKeys);

            // Convert decrypted bits back to bytes.
            // Creates a local value for this method.
            byte[] decryptedBlock = BlockUtil.binaryBlockToBytes(decryptedBinaryBlock);

            // Loops through the needed values.
            for (int i = 0; i < BLOCK_SIZE; i++) {
                // Stores the value used by this method.
                decryptedWithPadding[blockStart + i] = decryptedBlock[i];
            }
        }

        // Padding exists only at the end of the full decrypted data.
        // Returns the result to the caller.
        return PaddingUtil.removePkcs5Padding(decryptedWithPadding);
    }
}

