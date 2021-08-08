package com.github.dr.rwserver.util;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class Strings {
    public static final Charset utf8 = StandardCharsets.UTF_8;

    /** Replaces all instances of {@code find} with {@code replace}. */
    public static StringBuilder replace(StringBuilder builder, String find, String replace){
        int findLength = find.length(), replaceLength = replace.length();
        int index = 0;
        while(true){
            index = builder.indexOf(find, index);
            if (index == -1) break;
            builder.replace(index, index + findLength, replace);
            index += replaceLength;
        }
        return builder;
    }

    /** Replaces all instances of {@code find} with {@code replace}. */
    public static StringBuilder replace(StringBuilder builder, char find, String replace) {
        int replaceLength = replace.length();
        int index = 0;
        while(true){
            while(true){
                if (index == builder.length()) return builder;
                if (builder.charAt(index) == find) break;
                index++;
            }
            builder.replace(index, index + 1, replace);
            index += replaceLength;
        }
    }
}
