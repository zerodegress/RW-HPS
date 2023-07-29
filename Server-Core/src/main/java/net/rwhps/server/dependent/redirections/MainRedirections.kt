package net.rwhps.server.dependent.redirections

import net.rwhps.asm.agent.AsmData
import net.rwhps.asm.api.Redirection
import net.rwhps.asm.redirections.AsmRedirections
import net.rwhps.asm.redirections.DefaultRedirections

/**
 * 用来实现各个 ASM Redirections 服务
 * 进行统一调用
 *
 * @author RW-HPS/Dr
 */
interface MainRedirections {
    // 强制实现
    fun register()

    fun redirectClass(classPath: String) {
        AsmData.addClassIgnore(classPath)
    }

    fun redirect(desc: String, redirection: Redirection = DefaultRedirections.NULL) {
        AsmRedirections.redirect(desc, redirection)
    }

    fun redirect(packetName: String, p: Array<String>, redirection: Redirection = DefaultRedirections.NULL) {
        AsmData.addPartialMethod(packetName, p, redirection)
    }
}