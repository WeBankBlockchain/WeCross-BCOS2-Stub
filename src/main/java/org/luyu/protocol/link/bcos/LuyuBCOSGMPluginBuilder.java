package org.luyu.protocol.link.bcos;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.stub.ObjectMapperFactory;
import com.webank.wecross.stub.Path;
import com.webank.wecross.stub.bcos.BCOSGMStubFactory;
import com.webank.wecross.stub.bcos.account.BCOSAccountFactory;
import com.webank.wecross.stub.bcos.common.BCOSConstant;
import java.util.ArrayList;
import java.util.Map;
import org.luyu.protocol.link.Connection;
import org.luyu.protocol.link.Driver;
import org.luyu.protocol.link.LuyuPlugin;
import org.luyu.protocol.link.PluginBuilder;
import org.luyu.protocol.network.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@LuyuPlugin("GM_BCOS2.0")
public class LuyuBCOSGMPluginBuilder implements PluginBuilder {
    private static Logger logger = LoggerFactory.getLogger(LuyuBCOSGMPluginBuilder.class);
    private BCOSGMStubFactory stubFactory = new BCOSGMStubFactory();
    private LuyuMemoryBlockManagerFactory memoryBlockManagerFactory =
            new LuyuMemoryBlockManagerFactory();
    private ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
    private BCOSAccountFactory accountFactory = new BCOSAccountFactory();

    @Override
    public Connection newConnection(Map<String, Object> properties) {
        try {
            String chainPath = (String) properties.get("chainPath");
            com.webank.wecross.stub.Connection wecrossConnection =
                    stubFactory.newConnection(properties);
            if (wecrossConnection == null) {

                logger.error(
                        "newConnection error, properties: {}",
                        objectMapper.writeValueAsString(properties.toString()));

                return null;
            }

            LuyuConnectionAdapter luyuConnectionAdapter =
                    new LuyuConnectionAdapter(wecrossConnection, chainPath);
            // parse resources

            ArrayList<Map<String, Object>> resources =
                    (ArrayList<Map<String, Object>>) properties.get("luyu-resources");
            if (resources != null) {
                for (Map<String, Object> resourceMap : resources) {
                    Path path = Path.decode(chainPath);
                    String name = (String) resourceMap.get("name");
                    if (name == null) {
                        throw new Exception("\"name\" item not found, please check config ");
                    }

                    path.setResource(name);

                    Resource resource = new Resource();
                    resource.setType(BCOSConstant.GM_BCOS_STUB_TYPE);
                    resource.setPath(path.toString());

                    ArrayList<String> methods = (ArrayList<String>) resourceMap.get("methods");
                    if (methods != null) {
                        resource.setMethods(methods.toArray(new String[] {}));
                    }

                    luyuConnectionAdapter.addResource(resource);
                }
            }
            return luyuConnectionAdapter;
        } catch (Exception e) {
            logger.error("newConnection error, properties: {}, {}", properties.toString(), e);
        }
        return null;
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
                        BCOSConstant.GM_BCOS_STUB_TYPE,
                        chainPath,
                        wecrossDriver,
                        luyuWeCrossConnection,
                        blockManager,
                        accountFactory);
        blockManager.start();
        return luyuDriverAdapter;
    }
}
