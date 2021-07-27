package com.github.dr.rwserver.util.log.exp

import com.github.dr.rwserver.util.log.ErrorCode
import java.lang.Exception

class NetException(type: String) : Exception(
    ErrorCode.valueOf(type).error
)