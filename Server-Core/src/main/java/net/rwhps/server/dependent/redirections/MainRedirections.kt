package net.rwhps.server.dependent.redirections

import net.rwhps.asm.agent.AsmCore
import net.rwhps.asm.api.Redirection
import net.rwhps.asm.redirections.AsmRedirections
import net.rwhps.asm.redirections.DefaultRedirections

interface MainRedirections {
    fun register()

    fun redirect(desc: String) {
        AsmRedirections.redirect(desc, DefaultRedirections.NULL)
    }

    fun redirect(desc: String, redirection: Redirection) {
        AsmRedirections.redirect(desc, redirection)
    }

    fun redirect(desc: String, p: Array<String>, redirection: Redirection? = null) {
        AsmCore.addPartialMethod(desc, p, redirection)
    }
}