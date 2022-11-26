package cn.rwhps.server.dependent.redirections

import cn.rwhps.asm.api.Redirection
import cn.rwhps.asm.redirections.AsmRedirections
import cn.rwhps.asm.redirections.DefaultRedirections

interface MainRedirections {
    fun redirect(desc: String) {
        AsmRedirections.redirect(desc, DefaultRedirections.NULL)
    }

    fun redirect(desc: String, redirection: Redirection) {
        AsmRedirections.redirect(desc, redirection)
    }
}