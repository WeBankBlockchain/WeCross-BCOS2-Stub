package org.trustnet.protocol.link.bcos;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.stub.ObjectMapperFactory;
import com.webank.wecross.stub.Path;
import com.webank.wecross.stub.bcos.BCOSStubFactory;
import com.webank.wecross.stub.bcos.account.BCOSAccountFactory;
import com.webank.wecross.stub.bcos.common.BCOSConstant;
import com.webank.wecross.stub.bcos.tn.TnConnectionAdapter;
import com.webank.wecross.stub.bcos.tn.TnDriverAdapter;
import com.webank.wecross.stub.bcos.tn.TnMemoryBlockManager;
import com.webank.wecross.stub.bcos.tn.TnMemoryBlockManagerFactory;
import com.webank.wecross.stub.bcos.tn.TnWeCrossConnection;
import java.util.ArrayList;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trustnet.protocol.link.Connection;
import org.trustnet.protocol.link.Driver;
import org.trustnet.protocol.link.PluginBuilder;
import org.trustnet.protocol.link.TnPlugin;
import org.trustnet.protocol.network.Resource;

@TnPlugin("BCOS2.0")
public class TnBCOSPluginBuilder extends PluginBuilder {
    private static Logger logger = LoggerFactory.getLogger(TnBCOSPluginBuilder.class);
    private BCOSStubFactory stubFactory = new BCOSStubFactory();
    private TnMemoryBlockManagerFactory memoryBlockManagerFactory =
            new TnMemoryBlockManagerFactory();
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

            TnConnectionAdapter tnConnectionAdapter =
                    new TnConnectionAdapter(wecrossConnection, chainPath);
            // parse resources

            ArrayList<Map<String, Object>> resources =
                    (ArrayList<Map<String, Object>>) properties.get("tn-resources");
            if (resources != null) {
                for (Map<String, Object> resourceMap : resources) {
                    Path path = Path.decode(chainPath);
                    String name = (String) resourceMap.get("name");
                    if (name == null) {
                        throw new Exception("\"name\" item not found, please check config ");
                    }

                    path.setResource(name);

                    Resource resource = new Resource();
                    resource.setType(BCOSConstant.BCOS_STUB_TYPE);
                    resource.setPath(path.toString());

                    ArrayList<String> methods = (ArrayList<String>) resourceMap.get("methods");
                    if (methods != null) {
                        resource.setMethods(methods.toArray(new String[] {}));
                    }

                    tnConnectionAdapter.addResource(resource);
                }
            }
            return tnConnectionAdapter;
        } catch (Exception e) {
            logger.error("newConnection error, properties: {}, {}", properties.toString(), e);
        }
        return null;
    }

    @Override
    public Driver newDriver(Connection connection, Map<String, Object> properties) {
        String chainPath = (String) properties.get("chainPath");
        com.webank.wecross.stub.Driver wecrossDriver = stubFactory.newDriver();
        TnWeCrossConnection tnWeCrossConnection = new TnWeCrossConnection(connection);

        String verifierString = getVerifierString(properties);
        tnWeCrossConnection.setVerifierString(verifierString);

        TnMemoryBlockManager blockManager =
                memoryBlockManagerFactory.build(chainPath, wecrossDriver, tnWeCrossConnection);
        TnDriverAdapter tnDriverAdapter =
                new TnDriverAdapter(
                        BCOSConstant.BCOS_STUB_TYPE,
                        chainPath,
                        wecrossDriver,
                        tnWeCrossConnection,
                        blockManager,
                        accountFactory);
        blockManager.start();
        return tnDriverAdapter;
    }

    private String getVerifierString(Map<String, Object> properties) throws RuntimeException {

        String chainPath = (String) properties.get("chainPath");
        String chainDir = (String) properties.get("chainDir");
        String verifierKey = BCOSConstant.BCOS_SEALER_LIST.toLowerCase();
        try {
            if (properties.containsKey(verifierKey)) {

                Map<String, Object> verifierMap = (Map<String, Object>) properties.get(verifierKey);
                // add chainDir in verifierMap
                verifierMap.put("chainDir", chainDir);
                verifierMap.put("chainType", BCOSConstant.BCOS_STUB_TYPE);

                ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
                String verifierString = objectMapper.writeValueAsString(verifierMap);
                logger.info("Chain: " + chainPath + " configure verifier as: " + verifierString);

                return verifierString;
            } else {
                return null;
            }

        } catch (Exception e) {
            throw new RuntimeException("Parse [" + verifierKey + "] in driver.toml failed. " + e);
        }
    }
}
