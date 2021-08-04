package com.github.dr.rwserver.util.log.exp

import com.github.dr.rwserver.util.log.ErrorCode
import java.lang.Exception

/**
 * @author Dr
 */
class RwGamaException {
    open class KickException(type: String) : Exception(ErrorCode.valueOf(type).error)

    class KickStartException(type: String) : KickException(type)

    class KickPullException(type: String) : KickException(type)

    class PasswdException(type: String) : KickException(type)
}