package com.github.dr.rwserver.util.log.exp

import com.github.dr.rwserver.util.log.ErrorCode
import java.lang.RuntimeException

class ParseException(type: String) : RuntimeException(ErrorCode.valueOf(type).error) {
    /**
     * Constructs a new json exception with the specified detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to [.initCause].
     *
     * @param json     the json text which cause JSONParseException
     * @param position the position of illegal escape char at json text;
     * @param message  the detail message. The detail message is saved for
     * later retrieval by the [.getMessage] method.
     */
    class ParseJsonException(val json: String, val position: Int, message: String?) : RuntimeException(message) {

        /**
         * Get message about error when parsing illegal json
         *
         * @return error message
         */
        override val message: String
            get() {
                val maxTipLength = 10
                var end = position + 1
                var start = end - maxTipLength
                if (start < 0) start = 0
                if (end > json.length) end = json.length
                return String.format("%s  (%d):%s", json.substring(start, end), position, super.message)
            }

        companion object {
            private const val serialVersionUID = 3674125742687171239L
        }
    }
}