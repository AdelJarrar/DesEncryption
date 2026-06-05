package com.example.desencryption;

import java.math.BigInteger;
import java.security.SecureRandom;

public class KeyUtil {

    private static final SecureRandom RANDOM = new SecureRandom();

    public String[] roundKeys = new String[16];

    public String[] reversedRoundKeys = new String[16];

    /* Handles KeySchedule1. */
    public String KeySchedule1(String key) {
        // Checks the condition before continuing.
        if (key == null || key.length() != 16 || !key.matches("[0-9a-fA-F]+")) {
            // Throws an error for invalid input.
            throw new IllegalArgumentException("DES key must be exactly 16 hexadecimal characters.");
        }

        /*
         * Convert the hexadecimal key into binary.
         *
         * BigInteger removes leading zeroes, so we pad the result back to 64 bits.
         */
        // Creates a local value for this method.
        String binary = new BigInteger(key, 16).toString(2);
        // Stores the value used by this method.
        binary = String.format("%64s", binary).replace(' ', '0');

        // Creates a local value for this method.
        StringBuilder pc1Key = new StringBuilder();

        /*
         * Apply PC-1.
         *
         * DES tables are 1-based, while Java strings are 0-based.
         * That is why we use pos - 1.
         */
        // Loops through the needed values.
        for (int i = 0; i < PermutationTables.PC1.length; i++) {
            // Creates a local value for this method.
            int pos = PermutationTables.PC1[i];
            // Appends text to the builder.
            pc1Key.append(binary.charAt(pos - 1));
        }

        // Returns the result to the caller.
        return pc1Key.toString();
    }

    /* Handles keySchedule2. */
    public void keySchedule2(String pc1Key) {
        // Checks the condition before continuing.
        if (pc1Key == null || pc1Key.length() != 56) {
            // Throws an error for invalid input.
            throw new IllegalArgumentException("PC-1 key must be exactly 56 bits.");
        }

        /*
         * Split the 56-bit key into two halves:
         *
         * left  = C0
         * right = D0
         */
        // Creates a local value for this method.
        String left = pc1Key.substring(0, 28);
        // Creates a local value for this method.
        String right = pc1Key.substring(28, 56);

        /*
         * Generate C1,D1 through C16,D16 by shifting both halves.
         */
        // Loops through the needed values.
        for (int i = 0; i < 16; i++) {
            // Creates a local value for this method.
            int shift = PermutationTables.ROUND_SHIFTS[i];

            // Checks the condition before continuing.
            if (shift == 1) {
                /*
                 * Left circular shift by 1 bit.
                 *
                 * Example:
                 * 10110 -> 01101
                 */
                // Stores the value used by this method.
                left = left.substring(1) + left.charAt(0);
                // Stores the value used by this method.
                right = right.substring(1) + right.charAt(0);
            // Runs this line of the method.
            } else {
                /*
                 * Left circular shift by 2 bits.
                 *
                 * Example:
                 * 10110 -> 11010
                 */
                // Stores the value used by this method.
                left = left.substring(2) + left.charAt(0) + left.charAt(1);
                // Stores the value used by this method.
                right = right.substring(2) + right.charAt(0) + right.charAt(1);
            }

            /*
             * Combine Ci and Di into one 56-bit key.
             *
             * At this stage, this is not yet the final round key.
             * PC-2 still needs to be applied.
             */
            // Creates a local value for this method.
            String concatedKey = left + right;

            /*
             * Temporarily store the shifted 56-bit key for this round.
             */
            // Stores the value used by this method.
            roundKeys[i] = concatedKey;
        }
    }

    /* Handles secondKeySchedule. */
    public void secondKeySchedule(String[] keys) {
        // Checks the condition before continuing.
        if (keys == null || keys.length != 16) {
            // Throws an error for invalid input.
            throw new IllegalArgumentException("You must pass exactly 16 shifted keys.");
        }

        // Loops through the needed values.
        for (int i = 0; i < 16; i++) {
            // Checks the condition before continuing.
            if (keys[i] == null || keys[i].length() != 56) {
                // Throws an error for invalid input.
                throw new IllegalArgumentException(
                        // Runs this line of the method.
                        "Shifted key at round " + (i + 1) + " must be exactly 56 bits."
                );
            }

            // Creates a local value for this method.
            StringBuilder newKey = new StringBuilder();

            /*
             * Apply PC-2.
             *
             * This compresses the 56-bit shifted key into a 48-bit round key.
             */
            // Loops through the needed values.
            for (int j = 0; j < PermutationTables.PC2.length; j++) {
                // Creates a local value for this method.
                int pos = PermutationTables.PC2[j];
                // Appends text to the builder.
                newKey.append(keys[i].charAt(pos - 1));
            }

            /*
             * Replace the temporary 56-bit shifted key with
             * the final 48-bit round key, and store the same key in reverse
             * order for decryption.
             */
            // Creates a local value for this method.
            String finalRoundKey = newKey.toString();
            // Stores the value used by this method.
            roundKeys[i] = finalRoundKey;
            // Stores the value used by this method.
            reversedRoundKeys[15 - i] = finalRoundKey;
        }
    }

    /* Handles generateRoundKeys. */
    public String[] generateRoundKeys(String key) {
        // Creates a local value for this method.
        String pc1Key = KeySchedule1(key);
        // Runs this line of the method.
        keySchedule2(pc1Key);
        // Runs this line of the method.
        secondKeySchedule(roundKeys);

        // Returns the result to the caller.
        return roundKeys;
    }

    /* Handles generateReversedRoundKeys. */
    public String[] generateReversedRoundKeys(String key) {
        // Creates a local value for this method.
        String pc1Key = KeySchedule1(key);
        // Runs this line of the method.
        keySchedule2(pc1Key);
        // Runs this line of the method.
        secondKeySchedule(roundKeys);

        // Returns the result to the caller.
        return reversedRoundKeys;
    }

    /* Handles generateRandomDesKey. */
    public static String generateRandomDesKey() {
        // Creates a local value for this method.
        byte[] keyBytes = new byte[8];
        // Runs this line of the method.
        RANDOM.nextBytes(keyBytes);
        // Returns the result to the caller.
        return BitUtil.bytesToHex(keyBytes);
    }

}

