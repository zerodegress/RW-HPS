/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.mods;

/**
 * Mods加载辅助
 * @author RW-HPS/Dr
 */
class ModsLoadUtil {
    protected static void checkName(String paramString) {
        String str = paramString;
        if (str.length() == 0) {
            throw new RuntimeException("name cannot be empty");
        }
        if (str.contains(" ") || str.contains("}") || str.contains("$") || str.contains(".") || str
                .contains("{") || str.contains("-") || str.contains("+") || str.contains(":") || str.contains("(")) {
            throw new RuntimeException("invalid character in name");
        }
        if (Character.isDigit(str.charAt(0))) {
            throw new RuntimeException("name cannot start with a digit");
        }
    }
}
