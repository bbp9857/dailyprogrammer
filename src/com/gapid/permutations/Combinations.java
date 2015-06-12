package com.gapid.permutations;

import java.util.stream.IntStream;

/**
 * Created by bbp on 6/11/2015.
 */
public class Combinations {
    public static void main(String[] args) {
        int digits []  = {0, 1, 2, 3};

        int count = 0;
        for (int i = 0; i < 200; i++ ) {
            count ++;
            if (!nextCombo(digits, 6)) break;
        }

        System.out.println(count);
    }

    public static boolean nextCombo(int[] digits, int maxDomain) {
        int spot = 0;
        for (  spot = 0; spot < digits.length - 1; spot++ ) {
           if (digits[spot] + 1 != digits[spot + 1]) break;
        }
        System.out.print(spot);
        for (int i = 0 ; i < digits.length; i++ ) System.out.print(" " + digits[i]);
        System.out.println();

        digits[spot] ++;

        for (int i = 0; i < spot; i++) {digits[i] = i;}
        return !(digits[digits.length - 1] == maxDomain) ;
    }
}
