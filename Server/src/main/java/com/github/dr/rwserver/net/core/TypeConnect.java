package com.github.dr.rwserver.net.core;

import com.github.dr.rwserver.io.Packet;
import org.jetbrains.annotations.NotNull;

/**
 * @author Dr
 */
public interface TypeConnect {
    /**
     * 协议处理
     * @param con
     * @param packet
     * @throws Exception
     */
    void typeConnect(@NotNull AbstractNetConnect con, @NotNull Packet packet) throws Exception;

    /**
     * 获取TypeConnect处理版本号
     * @return Version
     */
    @NotNull
    String getVersion();
}
