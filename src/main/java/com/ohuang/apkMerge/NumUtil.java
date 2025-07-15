package com.ohuang.apkMerge;

public class NumUtil {

    public static int ox2Int(String s) {
        String s1 = s;
        if (s.startsWith("0x") || s.startsWith("0X")) {
            s1 = s.substring(2);
        }
        return oxString2Int(s1);
    }

    public static String int2ox(int num) {
        int temp = num;
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            int t = temp & 0xf;
            temp >>= 4;
            stringBuilder.append(int2oxChar(t));
        }
        String s1 = stringBuilder.toString();
        StringBuilder stringBuilder1 = new StringBuilder();
        stringBuilder1.append("0x");
        for (int length = s1.length() - 1; length >= 0; length--) {
            stringBuilder1.append(s1.charAt(length));
        }

        return stringBuilder1.toString();
    }


    private static int oxString2Int(String s) {
        String substring = s;
        if (s.length() > 8) {
            substring = s.substring(0, 8);
        }
        int num = 0;
        for (int i = 0; i < substring.length(); i++) {
            num <<= 4;
            num |= oxChar2Int(substring.charAt(i));
        }
        return num;
    }

    private static int oxChar2Int(char c) {
        int i = 0;
        switch (c) {
            case '0':
                i = 0x0;
                break;
            case '1':
                i = 0x1;
                break;
            case '2':
                i = 0x2;
                break;
            case '3':
                i = 0x3;
                break;
            case '4':
                i = 0x4;
                break;
            case '5':
                i = 0x5;
                break;
            case '6':
                i = 0x6;
                break;
            case '7':
                i = 0x7;
                break;
            case '8':
                i = 0x8;
                break;
            case '9':
                i = 0x9;
                break;
            case 'a':
                i = 0xa;
                break;
            case 'A':
                i = 0xa;
                break;
            case 'b':
                i = 0xb;
                break;
            case 'B':
                i = 0xb;
                break;
            case 'c':
                i = 0xc;
                break;
            case 'C':
                i = 0xc;
                break;
            case 'd':
                i = 0xd;
                break;
            case 'D':
                i = 0xd;
                break;
            case 'e':
                i = 0xe;
                break;
            case 'E':
                i = 0xe;
                break;
            case 'f':
                i = 0xf;
                break;
            case 'F':
                i = 0xf;
                break;
        }
        return i;
    }


    private static char int2oxChar(int i) {
        char c = '0';
        switch (i) {
            case 0x0:
                c = '0';
                break;
            case 0x1:
                c = '1';
                break;
            case 0x2:
                c = '2';
                break;
            case 0x3:
                c = '3';
                break;
            case 0x4:
                c = '4';
                break;
            case 0x5:
                c = '5';
                break;
            case 0x6:
                c = '6';
                break;
            case 0x7:
                c = '7';
                break;
            case 0x8:
                c = '8';
                break;
            case 0x9:
                c = '9';
                break;
            case 0xa:
                c = 'a';
                break;
            case 0xb:
                c = 'b';
                break;
            case 0xc:
                c = 'c';
                break;
            case 0xd:
                c = 'd';
                break;
            case 0xe:
                c = 'e';
                break;

            case 0xf:
                c = 'f';
                break;

        }
        return c;
    }
}
