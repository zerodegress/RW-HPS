package com.github.dr.rwserver.core;

import com.github.dr.rwserver.core.ex.Threads;
import com.github.dr.rwserver.data.global.Data;

/**
 * @author Dr
 */
public class Core {
    public static void exit() {
        Data.core.save();
        Threads.close();
        System.exit(0);
    }

    public static void mandatoryExit() {
        System.exit(0);
    }
}
