package com.github.dr.rwserver.util.log.exp

import com.github.dr.rwserver.util.log.ErrorCode
import java.lang.RuntimeException

/**
 * @author Dr
 */
class VariableException(type: String) : RuntimeException(ErrorCode.valueOf(type).error)

{
    class ArrayRuntimeException(type: String) : RuntimeException(ErrorCode.valueOf(type).error)

    class ObjectMapRuntimeException(info: String) : RuntimeException(info)

    class MapRuntimeException(type: String) : RuntimeException(ErrorCode.valueOf(type).error)
}