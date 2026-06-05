package com.example.desencryption;

public class DesCipher {

    /* Handles initialPermutation. */
    public String initialPermutation(String block){
        // Returns the result to the caller.
        return BitUtil.applyPermutation(block,PermutationTables.INITIAL_PERMUTATION);
    }

    /* Handles finalPermutation. */
    public String finalPermutation(String block){
        // Returns the result to the caller.
        return BitUtil.applyPermutation(block,PermutationTables.FINAL_PERMUTATION);
    }

    /* Handles expand. */
    public String expand(String rightBlock) {
        // Returns the result to the caller.
        return BitUtil.applyPermutation(
                // Runs this line of the method.
                rightBlock,
                // Runs this line of the method.
                PermutationTables.EXPANSION_PERMUTATION
        );
    }

    /* Handles substitute. */
    public String substitute(String input48) {
        // Checks the condition before continuing.
        if (input48 == null || input48.length() != 48 || !input48.matches("[01]+")) {
            // Throws an error for invalid input.
            throw new IllegalArgumentException("S-box input must be exactly 48 bits.");
        }

        // Creates a local value for this method.
        StringBuilder output32 = new StringBuilder(32);

        // Loops through the needed values.
        for (int i = 0; i < 8; i++) {
            // Creates a local value for this method.
            int start = i * 6;

            // Take one group of 6 bits for the current S-box.
            // Creates a local value for this method.
            int b1 = input48.charAt(start) - '0';
            // Creates a local value for this method.
            int b2 = input48.charAt(start + 1) - '0';
            // Creates a local value for this method.
            int b3 = input48.charAt(start + 2) - '0';
            // Creates a local value for this method.
            int b4 = input48.charAt(start + 3) - '0';
            // Creates a local value for this method.
            int b5 = input48.charAt(start + 4) - '0';
            // Creates a local value for this method.
            int b6 = input48.charAt(start + 5) - '0';

            // The first and last bits choose the S-box row.
            // Creates a local value for this method.
            int row = 2 * b1 + b6;

            // The four middle bits choose the S-box column.
            // Creates a local value for this method.
            int col = 8 * b2 + 4 * b3 + 2 * b4 + b5;

            // Creates a local value for this method.
            int value = PermutationTables.S_BOXES[i][row][col];

            // Convert the S-box number, 0 through 15, back into 4 bits.
            // Appends text to the builder.
            output32.append((value / 8) % 2);
            // Appends text to the builder.
            output32.append((value / 4) % 2);
            // Appends text to the builder.
            output32.append((value / 2) % 2);
            // Appends text to the builder.
            output32.append(value % 2);
        }

        // Returns the result to the caller.
        return output32.toString();
    }

    /* Handles feistelFunction. */
    public String feistelFunction(String rightBlock, String roundKey) {
        // Creates a local value for this method.
        String expanded = expand(rightBlock);
        // Creates a local value for this method.
        String xored = BitUtil.xor(expanded, roundKey);
        // Creates a local value for this method.
        String substituted = substitute(xored);
        // Returns the result to the caller.
        return BitUtil.applyPermutation(
                // Runs this line of the method.
                substituted,
                // Runs this line of the method.
                PermutationTables.P_PERMUTATION
        );
    }

    /* Handles mixer. */
    public String mixer(String leftBlock, String rightBlock, String roundKey) {
        // Creates a local value for this method.
        String t1 = rightBlock;
        // Creates a local value for this method.
        String t2 = feistelFunction(t1, roundKey);
        // Creates a local value for this method.
        String t3 = BitUtil.xor(leftBlock, t2);

        // Returns the result to the caller.
        return t3;
    }

    /* Handles swapper. */
    public String[] swapper(String leftBlock, String rightBlock) {
        // Creates a local value for this method.
        String temp = leftBlock;
        // Stores the value used by this method.
        leftBlock = rightBlock;
        // Stores the value used by this method.
        rightBlock = temp;

        // Returns the result to the caller.
        return new String[]{leftBlock, rightBlock};
    }

    /* Handles cipher. */
    public String cipher(String plainBlock64, String[] roundKeys) {
        // Creates a local value for this method.
        String inBlock = initialPermutation(plainBlock64);

        // Creates a local value for this method.
        String leftBlock = BitUtil.leftHalf(inBlock);
        // Creates a local value for this method.
        String rightBlock = BitUtil.rightHalf(inBlock);

        // Loops through the needed values.
        for (int round = 0; round < 16; round++) {
            // First mix the current left side with f(right side, round key).
            // Stores the value used by this method.
            leftBlock = mixer(leftBlock, rightBlock, roundKeys[round]);

            // DES swaps after rounds 1 through 15, but not after round 16.
            // Checks the condition before continuing.
            if (round != 15) {
                // Creates a local value for this method.
                String[] swapped = swapper(leftBlock, rightBlock);
                // Stores the value used by this method.
                leftBlock = swapped[0];
                // Stores the value used by this method.
                rightBlock = swapped[1];
            }
        }

        // Creates a local value for this method.
        String outBlock = leftBlock + rightBlock;

        // Returns the result to the caller.
        return finalPermutation(outBlock);
    }

    /* Handles decrypt. */
    public String decrypt(String cipherBlock64, String[] reversedRoundKeys) {
        // Returns the result to the caller.
        return cipher(cipherBlock64, reversedRoundKeys);
    }

    /* Handles encryptBlock. */
    public String encryptBlock(String plainHex, String keyHex) {
        // Creates a local value for this method.
        String plainBlock64 = BitUtil.hexToBinary(plainHex, 64);

        // Creates a local value for this method.
        KeyUtil keyUtil = new KeyUtil();
        // Creates a local value for this method.
        String[] roundKeys = keyUtil.generateRoundKeys(keyHex);

        // Creates a local value for this method.
        String cipherBlock64 = cipher(plainBlock64, roundKeys);

        // Returns the result to the caller.
        return BitUtil.binaryToHex(cipherBlock64);
    }

    /* Handles decryptBlock. */
    public String decryptBlock(String cipherHex, String keyHex) {
        // Creates a local value for this method.
        String cipherBlock64 = BitUtil.hexToBinary(cipherHex, 64);

        // Creates a local value for this method.
        KeyUtil keyUtil = new KeyUtil();
        // Creates a local value for this method.
        String[] reversedRoundKeys = keyUtil.generateReversedRoundKeys(keyHex);

        // Creates a local value for this method.
        String plainBlock64 = decrypt(cipherBlock64, reversedRoundKeys);

        // Returns the result to the caller.
        return BitUtil.binaryToHex(plainBlock64);
    }
}

