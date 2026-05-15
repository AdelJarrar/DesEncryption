package com.example.desencryption;

/**
 * Implements the DES algorithm for one 64-bit block at a time.
 *
 * This class follows the same DES flow shown in the project pseudocode:
 * initial permutation, split into left/right halves, 16 Feistel rounds, then
 * final permutation. Larger text and files are handled by {link DesDataService},
 * which calls this class once for each 8-byte block.
 */
public class DesCipher {

    /**
     * Applies the DES initial permutation table to a 64-bit block.
     *
     * This step only rearranges the bits. It does not encrypt by itself.
     *
     * param block the 64-bit binary block before the DES rounds
     * return the same bits rearranged by the initial permutation table
     */
    public String initialPermutation(String block){
        return BitUtil.applyPermutation(block,PermutationTables.INITIAL_PERMUTATION);
    }

    /**
     * Applies the DES final permutation table to a 64-bit block.
     *
     * This is the last rearrangement after all 16 DES rounds finish.
     *
     * param block the 64-bit binary block after the DES rounds
     * return the final 64-bit encrypted or decrypted block
     */
    public String finalPermutation(String block){
        return BitUtil.applyPermutation(block,PermutationTables.FINAL_PERMUTATION);
    }

    /**
     * Expands the right half from 32 bits to 48 bits.
     *
     * DES does this so the right half can be XORed with a 48-bit round key.
     *
     * param rightBlock the 32-bit right half of the current DES block
     * return a 48-bit expanded version of rightBlock
     */
    public String expand(String rightBlock) {
        return BitUtil.applyPermutation(
                rightBlock,
                PermutationTables.EXPANSION_PERMUTATION
        );
    }

    /**
     * Runs the DES S-box substitution step.
     *
     * The input is split into eight groups of 6 bits. Each group is sent to
     * one S-box, and each S-box returns 4 bits. That changes 48 bits into
     * 32 bits.
     *
     * param input48 the 48-bit value after expansion and XOR with the round key
     * return the 32-bit value produced by the eight S-boxes
     */
    public String substitute(String input48) {
        if (input48 == null || input48.length() != 48 || !input48.matches("[01]+")) {
            throw new IllegalArgumentException("S-box input must be exactly 48 bits.");
        }

        StringBuilder output32 = new StringBuilder(32);

        for (int i = 0; i < 8; i++) {
            int start = i * 6;

            // Take one group of 6 bits for the current S-box.
            int b1 = input48.charAt(start) - '0';
            int b2 = input48.charAt(start + 1) - '0';
            int b3 = input48.charAt(start + 2) - '0';
            int b4 = input48.charAt(start + 3) - '0';
            int b5 = input48.charAt(start + 4) - '0';
            int b6 = input48.charAt(start + 5) - '0';

            // The first and last bits choose the S-box row.
            int row = 2 * b1 + b6;

            // The four middle bits choose the S-box column.
            int col = 8 * b2 + 4 * b3 + 2 * b4 + b5;

            int value = PermutationTables.S_BOXES[i][row][col];

            // Convert the S-box number, 0 through 15, back into 4 bits.
            output32.append((value / 8) % 2);
            output32.append((value / 4) % 2);
            output32.append((value / 2) % 2);
            output32.append(value % 2);
        }

        return output32.toString();
    }

    /**
     * Runs the DES f function for one round.
     *
     * The f function expands the right half, XORs it with the round key,
     * applies the S-boxes, then applies the P permutation.
     *
     * param rightBlock the 32-bit right half for this round
     * param roundKey   the 48-bit key for this round
     * return the 32-bit output of the f function
     */
    public String feistelFunction(String rightBlock, String roundKey) {
        String expanded = expand(rightBlock);
        String xored = BitUtil.xor(expanded, roundKey);
        String substituted = substitute(xored);
        return BitUtil.applyPermutation(
                substituted,
                PermutationTables.P_PERMUTATION
        );
    }

    /**
     * Performs the DES mixer step for one round.
     *
     * It applies the f function to the right half, then XORs that result
     * with the left half. The returned value becomes the new left side before
     * the optional swap in the round loop.
     *
     * param leftBlock  the current 32-bit left half
     * param rightBlock the current 32-bit right half
     * param roundKey   the 48-bit key for this round
     * return the mixed 32-bit result
     */
    public String mixer(String leftBlock, String rightBlock, String roundKey) {
        String t1 = rightBlock;
        String t2 = feistelFunction(t1, roundKey);
        String t3 = BitUtil.xor(leftBlock, t2);

        return t3;
    }

    /**
     * Swaps the left and right halves.
     *
     * DES swaps after every round except the final round.
     *
     * param leftBlock  the current left half
     * param rightBlock the current right half
     * return an array where index 0 is the new left half and index 1 is the new right half
     */
    public String[] swapper(String leftBlock, String rightBlock) {
        String temp = leftBlock;
        leftBlock = rightBlock;
        rightBlock = temp;

        return new String[]{leftBlock, rightBlock};
    }

    /**
     * Encrypts or decrypts one 64-bit binary block using the provided round keys.
     *
     * The same method works for encryption and decryption. Encryption passes
     * keys K1 to K16. Decryption passes the same keys in reverse order.
     *
     * param plainBlock64 the 64-bit block to process
     * param roundKeys    the 16 round keys in the order they should be used
     * return the processed 64-bit block
     */
    public String cipher(String plainBlock64, String[] roundKeys) {
        String inBlock = initialPermutation(plainBlock64);

        String leftBlock = BitUtil.leftHalf(inBlock);
        String rightBlock = BitUtil.rightHalf(inBlock);

        for (int round = 0; round < 16; round++) {
            // First mix the current left side with f(right side, round key).
            leftBlock = mixer(leftBlock, rightBlock, roundKeys[round]);

            // DES swaps after rounds 1 through 15, but not after round 16.
            if (round != 15) {
                String[] swapped = swapper(leftBlock, rightBlock);
                leftBlock = swapped[0];
                rightBlock = swapped[1];
            }
        }

        String outBlock = leftBlock + rightBlock;

        return finalPermutation(outBlock);
    }

    /**
     * Decrypts one 64-bit binary block using round keys in reverse order.
     *
     * param cipherBlock64     the encrypted 64-bit block
     * param reversedRoundKeys the round keys ordered from K16 down to K1
     * return the decrypted 64-bit block
     */
    public String decrypt(String cipherBlock64, String[] reversedRoundKeys) {
        return cipher(cipherBlock64, reversedRoundKeys);
    }

    /**
     * Encrypts one block written as 16 hexadecimal characters.
     *
     * This helper is useful for DES test vectors and small examples.
     *
     * param plainHex the plaintext block as 16 hex characters
     * param keyHex   the DES key as 16 hex characters
     * return the encrypted block as uppercase hexadecimal
     */
    public String encryptBlock(String plainHex, String keyHex) {
        String plainBlock64 = BitUtil.hexToBinary(plainHex, 64);

        KeyUtil keyUtil = new KeyUtil();
        String[] roundKeys = keyUtil.generateRoundKeys(keyHex);

        String cipherBlock64 = cipher(plainBlock64, roundKeys);

        return BitUtil.binaryToHex(cipherBlock64);
    }

    /**
     * Decrypts one block written as 16 hexadecimal characters.
     *
     * param cipherHex the encrypted block as 16 hex characters
     * param keyHex    the DES key as 16 hex characters
     * return the decrypted block as uppercase hexadecimal
     */
    public String decryptBlock(String cipherHex, String keyHex) {
        String cipherBlock64 = BitUtil.hexToBinary(cipherHex, 64);

        KeyUtil keyUtil = new KeyUtil();
        String[] reversedRoundKeys = keyUtil.generateReversedRoundKeys(keyHex);

        String plainBlock64 = decrypt(cipherBlock64, reversedRoundKeys);

        return BitUtil.binaryToHex(plainBlock64);
    }
}
