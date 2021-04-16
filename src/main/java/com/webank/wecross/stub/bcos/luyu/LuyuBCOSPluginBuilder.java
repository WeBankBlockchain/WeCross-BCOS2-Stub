package com.webank.wecross.stub.bcos.luyu;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.stub.ObjectMapperFactory;
import com.webank.wecross.stub.bcos.BCOSStubFactory;
import com.webank.wecross.stub.bcos.account.BCOSAccountFactory;
import java.util.Map;
import org.luyu.protocol.link.Connection;
import org.luyu.protocol.link.Driver;
import org.luyu.protocol.link.LuyuPlugin;
import org.luyu.protocol.link.PluginBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@LuyuPlugin("BCOS2.0")
public class LuyuBCOSPluginBuilder implements PluginBuilder {
    private static Logger logger = LoggerFactory.getLogger(LuyuBCOSPluginBuilder.class);
    private BCOSStubFactory stubFactory = new BCOSStubFactory();
    private LuyuMemoryBlockManagerFactory memoryBlockManagerFactory =
            new LuyuMemoryBlockManagerFactory();
    private ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
    private BCOSAccountFactory accountFactory = new BCOSAccountFactory();

    @Override
    public Connection newConnection(Map<String, Object> properties) {
        com.webank.wecross.stub.Connection wecrossConnection =
                stubFactory.newConnection(properties);
        if (wecrossConnection == null) {
            try {
                logger.error(
                        "newConnection error, properties: {}",
                        objectMapper.writeValueAsString(properties.toString()));
            } catch (Exception e) {
                logger.error("newConnection error, properties: {}", properties.toString());
            }
            return null;
        }

        LuyuConnectionAdapter luyuConnectionAdapter = new LuyuConnectionAdapter(wecrossConnection);
        return luyuConnectionAdapter;
    }

    @Override
    public Driver newDriver(Connection connection, Map<String, Object> properties) {
        String chainPath = (String) properties.get("chainPath");
        com.webank.wecross.stub.Driver wecrossDriver = stubFactory.newDriver();
        LuyuWeCrossConnection luyuWeCrossConnection = new LuyuWeCrossConnection(connection);
        LuyuMemoryBlockManager blockManager =
                memoryBlockManagerFactory.build(chainPath, wecrossDriver, luyuWeCrossConnection);
        LuyuDriverAdapter luyuDriverAdapter =
                new LuyuDriverAdapter(
                        "BCOS2.0",
                        chainPath,
                        wecrossDriver,
                        luyuWeCrossConnection,
                        blockManager,
                        accountFactory);

        return luyuDriverAdapter;
    }
}
