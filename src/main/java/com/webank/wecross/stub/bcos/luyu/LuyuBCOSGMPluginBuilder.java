package com.webank.wecross.stub.bcos.luyu;

import java.util.Map;
import org.luyu.protocol.link.Connection;
import org.luyu.protocol.link.Driver;
import org.luyu.protocol.link.LuyuPlugin;
import org.luyu.protocol.link.PluginBuilder;

@LuyuPlugin("GM_BCOS2.0")
public class LuyuBCOSGMPluginBuilder implements PluginBuilder {
    @Override
    public Connection newConnection(Map<String, Object> properties) {
        return null;
    }

    @Override
    public Driver newDriver(Connection connection, Map<String, Object> properties) {
        return null;
    }
}
