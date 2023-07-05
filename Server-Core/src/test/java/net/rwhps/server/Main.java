/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Structure;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.win32.StdCallLibrary;

import java.util.Arrays;
import java.util.List;

/**
 * @author RW-HPS/Dr
 * @date 2023/6/24 12:15
 */
public class Main {
    public interface User32 extends StdCallLibrary {
        User32 INSTANCE = Native.load("user32", User32.class);

        int MessageBoxA(int hWnd, String lpText, String lpCaption, int uType);
    }

    public static void main(String[] args) {
        User32 user32 = User32.INSTANCE;
        user32.MessageBoxA(0, "Hello, World!", "Notification Example", 0x00000040); // MB_ICONINFORMATION
    }
}