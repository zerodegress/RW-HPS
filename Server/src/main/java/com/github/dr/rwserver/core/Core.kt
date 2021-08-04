package com.github.dr.rwserver.core

import com.github.dr.rwserver.core.thread.Threads.close
import com.github.dr.rwserver.data.global.Data

/**
 * @author Dr
 */
object Core {
    @JvmStatic
    fun exit() {
        Data.core.save()
        close()
        System.exit(0)
    }

    @JvmStatic
    fun mandatoryExit() {
        System.exit(0)
    }
}