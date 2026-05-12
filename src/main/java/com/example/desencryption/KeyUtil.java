package com.example.desencryption;

import java.math.BigInteger;

/**
 * Utility class responsible for generating DES round keys.
 *
 * DES uses a 64-bit key as input, but only 56 bits are actually used.
 * The remaining 8 bits are parity bits and are removed using PC-1.
 *
 * This class follows the DES key schedule process:
 *
 * 1. Convert the original hexadecimal key into a 64-bit binary string.
 * 2. Apply PC-1 to reduce the key from 64 bits to 56 bits.
 * 3. Split the 56-bit key into two 28-bit halves.
 * 4. Perform left circular shifts on both halves for each round.
 * 5. Apply PC-2 to generate sixteen 48-bit round keys.
 */
public class KeyUtil {

    /**
     * PC-1 table.
     *
     * This table removes the parity bits from the original 64-bit key
     * and permutes the remaining bits to produce a 56-bit key.
     */
    private final int[] pc1 = {
            57, 49, 41, 33, 25, 17, 9, 1,
            58, 50, 42, 34, 26, 18, 10, 2,
            59, 51, 43, 35, 27, 19, 11, 3,
            60, 52, 44, 36, 63, 55, 47, 39,
            31, 23, 15, 7, 62, 54, 46, 38,
            30, 22, 14, 6, 61, 53, 45, 37,
            29, 21, 13, 5, 28, 20, 12, 4
    };

    /**
     * PC-2 table.
     *
     * This table compresses and permutes each shifted 56-bit key
     * into a 48-bit round key.
     */
    private final int[] pc2 = {
            14, 17, 11, 24, 1, 5, 3, 28,
            15, 6, 21, 10, 23, 19, 12, 4,
            26, 8, 16, 7, 27, 20, 13, 2,
            41, 52, 31, 37, 47, 55, 30, 40,
            51, 45, 33, 48, 44, 49, 39, 56,
            34, 53, 46, 42, 50, 36, 29, 32
    };

    /**
     * Number of left shifts applied to the key halves in each DES round.
     *
     * Rounds 1, 2, 9, and 16 use one left shift.
     * All other rounds use two left shifts.
     */
    private final int[] roundShifts = {
            1, 1, 2, 2, 2, 2, 2, 2,
            1, 2, 2, 2, 2, 2, 2, 1
    };

    /**
     * Stores the generated round keys.
     *
     * During keySchedule2(), this array temporarily stores the 56-bit shifted keys.
     * After secondKeySchedule(), it stores the final 48-bit DES round keys.
     */
    public String[] roundKeys = new String[16];

    /**
     * First step of the DES key schedule.
     *
     * Converts the original 16-character hexadecimal key into a 64-bit binary string,
     * then applies the PC-1 permutation table to produce a 56-bit key.
     *
     * @param key the original DES key in hexadecimal format, exactly 16 hex characters
     * @return the 56-bit key after applying PC-1
     * @throws IllegalArgumentException if the key is null, not 16 characters long,
     *                                  or contains non-hexadecimal characters
     */
    public String KeySchedule1(String key) {
        if (key == null || key.length() != 16 || !key.matches("[0-9a-fA-F]+")) {
            throw new IllegalArgumentException("DES key must be exactly 16 hexadecimal characters.");
        }

        /*
         * Convert the hexadecimal key into binary.
         *
         * BigInteger removes leading zeroes, so we pad the result back to 64 bits.
         */
        String binary = new BigInteger(key, 16).toString(2);
        binary = String.format("%64s", binary).replace(' ', '0');

        StringBuilder pc1Key = new StringBuilder();

        /*
         * Apply PC-1.
         *
         * DES tables are 1-based, while Java strings are 0-based.
         * That is why we use pos - 1.
         */
        for (int i = 0; i < pc1.length; i++) {
            int pos = pc1[i];
            pc1Key.append(binary.charAt(pos - 1));
        }

        return pc1Key.toString();
    }

    /**
     * Second step of the DES key schedule.
     *
     * Splits the 56-bit PC-1 key into two 28-bit halves, then performs
     * the required left circular shifts for each of the 16 rounds.
     *
     * The shifted 56-bit keys are temporarily stored in roundKeys.
     *
     * @param pc1Key the 56-bit key produced after applying PC-1
     * @throws IllegalArgumentException if pc1Key is null or not exactly 56 bits
     */
    public void keySchedule2(String pc1Key) {
        if (pc1Key == null || pc1Key.length() != 56) {
            throw new IllegalArgumentException("PC-1 key must be exactly 56 bits.");
        }

        /*
         * Split the 56-bit key into two halves:
         *
         * left  = C0
         * right = D0
         */
        String left = pc1Key.substring(0, 28);
        String right = pc1Key.substring(28, 56);

        /*
         * Generate C1,D1 through C16,D16 by shifting both halves.
         */
        for (int i = 0; i < 16; i++) {
            int shift = roundShifts[i];

            if (shift == 1) {
                /*
                 * Left circular shift by 1 bit.
                 *
                 * Example:
                 * 10110 -> 01101
                 */
                left = left.substring(1) + left.charAt(0);
                right = right.substring(1) + right.charAt(0);
            } else {
                /*
                 * Left circular shift by 2 bits.
                 *
                 * Example:
                 * 10110 -> 11010
                 */
                left = left.substring(2) + left.charAt(0) + left.charAt(1);
                right = right.substring(2) + right.charAt(0) + right.charAt(1);
            }

            /*
             * Combine Ci and Di into one 56-bit key.
             *
             * At this stage, this is not yet the final round key.
             * PC-2 still needs to be applied.
             */
            String concatedKey = left + right;

            /*
             * Temporarily store the shifted 56-bit key for this round.
             */
            roundKeys[i] = concatedKey;
        }
    }

    /**
     * Final step of the DES key schedule.
     *
     * Applies PC-2 to each shifted 56-bit key to produce the final
     * sixteen 48-bit DES round keys.
     *
     * @param keys the sixteen shifted 56-bit keys generated by keySchedule2()
     * @throws IllegalArgumentException if keys is null, does not contain 16 keys,
     *                                  or any key is not exactly 56 bits
     */
    public void secondKeySchedule(String[] keys) {
        if (keys == null || keys.length != 16) {
            throw new IllegalArgumentException("You must pass exactly 16 shifted keys.");
        }

        for (int i = 0; i < 16; i++) {
            if (keys[i] == null || keys[i].length() != 56) {
                throw new IllegalArgumentException(
                        "Shifted key at round " + (i + 1) + " must be exactly 56 bits."
                );
            }

            StringBuilder newKey = new StringBuilder();

            /*
             * Apply PC-2.
             *
             * This compresses the 56-bit shifted key into a 48-bit round key.
             */
            for (int j = 0; j < pc2.length; j++) {
                int pos = pc2[j];
                newKey.append(keys[i].charAt(pos - 1));
            }

            /*
             * Replace the temporary 56-bit shifted key with
             * the final 48-bit round key.
             */
            roundKeys[i] = newKey.toString();
        }
    }

    /**
     * Generates all sixteen DES round keys from the original hexadecimal key.
     *
     * This method runs the full key schedule process:
     *
     * 1. Apply PC-1.
     * 2. Split and shift the key halves.
     * 3. Apply PC-2.
     *
     * @param key the original DES key in hexadecimal format
     * @return an array containing sixteen 48-bit round keys
     */
    public String[] generateRoundKeys(String key) {
        String pc1Key = KeySchedule1(key);
        keySchedule2(pc1Key);
        secondKeySchedule(roundKeys);

        return roundKeys;
    }

    /**
     * Prints all generated round keys.
     *
     * This is mainly useful for debugging and checking that each key
     * is exactly 48 bits long.
     */
    public void printRoundKeys() {
        for (int i = 0; i < roundKeys.length; i++) {
            System.out.println(
                    "K" + (i + 1) + ": " + roundKeys[i]
                            + " | length = " + roundKeys[i].length()
            );
        }
    }
}
