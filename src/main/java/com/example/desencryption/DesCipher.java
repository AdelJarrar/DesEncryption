package com.example.desencryption;

public class DesCipher {

    //Initial permutation rearranges the text only
    public String initialPermutation(String block){
        return BitUtil.applyPermutation(block,PermutationTables.INITIAL_PERMUTATION);
    }
    //Final permutation is the opposite of Initial to retrieve the original text arrange
    public String finalPermutation(String block){
        return BitUtil.applyPermutation(block,PermutationTables.FINAL_PERMUTATION);
    }

    public String expand(String rightBlock) {
        return BitUtil.applyPermutation(
                rightBlock,
                PermutationTables.EXPANSION_PERMUTATION
        );
    }

    public String substitute(String input48) {
        if (input48 == null || input48.length() != 48 || !input48.matches("[01]+")) {
            throw new IllegalArgumentException("S-box input must be exactly 48 bits.");
        }

        StringBuilder output32 = new StringBuilder(32);

        for (int i = 0; i < 8; i++) {
            int start = i * 6;

            int b1 = input48.charAt(start) - '0';
            int b2 = input48.charAt(start + 1) - '0';
            int b3 = input48.charAt(start + 2) - '0';
            int b4 = input48.charAt(start + 3) - '0';
            int b5 = input48.charAt(start + 4) - '0';
            int b6 = input48.charAt(start + 5) - '0';

            int row = 2 * b1 + b6;
            int col = 8 * b2 + 4 * b3 + 2 * b4 + b5;

            int value = PermutationTables.S_BOXES[i][row][col];

            output32.append((value / 8) % 2);
            output32.append((value / 4) % 2);
            output32.append((value / 2) % 2);
            output32.append(value % 2);
        }

        return output32.toString();
    }

    public String feistelFunction(String rightBlock, String roundKey) {
        String expanded = expand(rightBlock);
        String xored = BitUtil.xor(expanded, roundKey);
        String substituted = substitute(xored);
        return BitUtil.applyPermutation(
                substituted,
                PermutationTables.P_PERMUTATION
        );
    }

    public String mixer(String leftBlock, String rightBlock, String roundKey) {
        String t1 = rightBlock;
        String t2 = feistelFunction(t1, roundKey);
        String t3 = BitUtil.xor(leftBlock, t2);

        return t3;
    }

    public String[] swapper(String leftBlock, String rightBlock) {
        String temp = leftBlock;
        leftBlock = rightBlock;
        rightBlock = temp;

        return new String[]{leftBlock, rightBlock};
    }

    public String cipher(String plainBlock64, String[] roundKeys) {
        String inBlock = initialPermutation(plainBlock64);

        String leftBlock = BitUtil.leftHalf(inBlock);
        String rightBlock = BitUtil.rightHalf(inBlock);

        for (int round = 0; round < 16; round++) {
            leftBlock = mixer(leftBlock, rightBlock, roundKeys[round]);

            if (round != 15) {
                String[] swapped = swapper(leftBlock, rightBlock);
                leftBlock = swapped[0];
                rightBlock = swapped[1];
            }
        }

        String outBlock = leftBlock + rightBlock;

        return finalPermutation(outBlock);
    }

    /*public String decrypt(String cipherBlock64, String[] roundKeys) {
        return cipher(cipherBlock64, roundKeys);
    }*/
}
