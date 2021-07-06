package com.github.dr.rwserver.net.web.realization.constant;

import io.netty.util.AttributeKey;

public class Config {
    private static byte[] POINTER;//分隔符
    private static int MessageMax;//信息包最大长度
    private static String rootUrl;//根路径
    private static String webSocketUrl;//websocketURL
    private static int fileMaxLength = 6553666;//文件最大长度
    public static final AttributeKey<Long> CHANNEL_ID = AttributeKey.valueOf("channelId");
    public static int getFileMaxLength() {
        return fileMaxLength;
    }

    public static void setFileMaxLength(int fileMaxLength) {
        Config.fileMaxLength = fileMaxLength;
    }

    public static String getWebSocketUrl() {
        return webSocketUrl;
    }

    public static void setWebSocketUrl(String webSocketUrl) {
        Config.webSocketUrl = webSocketUrl;
    }

    public static final AttributeKey<Long> HTTP_ID = AttributeKey.valueOf("httpId");

    public static String getRootUrl() {
        return rootUrl;
    }

    public static void setRootUrl(String rootUrl) {
        Config.rootUrl = rootUrl;
    }

    public static byte[] getPointer() {
        return POINTER;
    }

    public static void setPointer(String pointer) {
        Config.POINTER = pointer.getBytes();
    }

    public static int getMessageMax() {
        return MessageMax;
    }

    public static void setMessageMax(int messageMax) {
        MessageMax = messageMax;
    }
}
