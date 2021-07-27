package com.github.dr.rwserver.util.log.exp

import com.github.dr.rwserver.util.log.ErrorCode
import java.lang.Exception

class FileException(type: String) : Exception(
    ErrorCode.valueOf(type).error
)