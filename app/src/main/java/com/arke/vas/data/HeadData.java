package com.arke.vas.data;

/**
 * Message header message
 * <p>
 * 消息头信息
 */
public class HeadData {
    /**
     * version number
     * <p>
     * 版本号
     */
    private String version = "V1.2.0";

    @Override
    public String toString() {
        return "{\"version\": \"" + this.version + "\"}";
    }
}
