package com.webank.wecross.stub.bcos;

import com.webank.wecross.stub.bcos.common.BCOSConstant;
import com.webank.wecross.stub.bcos.config.BCOSStubConfig;
import com.webank.wecross.stub.bcos.config.BCOSStubConfigParser;
import com.webank.wecross.stub.bcos.proxy.ProxyCNS;
import com.webank.wecross.stub.bcos.web3j.Web3jUtility;
import com.webank.wecross.stub.bcos.web3j.Web3jWrapper;
import com.webank.wecross.stub.bcos.web3j.Web3jWrapperImpl;
import java.util.Objects;
import org.fisco.bcos.fisco.EnumNodeVersion;
import org.fisco.bcos.web3j.precompile.cns.CnsInfo;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.core.methods.response.NodeVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BCOSConnectionFactory {
    private static final Logger logger = LoggerFactory.getLogger(BCOSConnectionFactory.class);

    public static BCOSConnection build(
            String stubConfigPath, String configName, Web3jWrapper web3jWrapper) throws Exception {
        /** load stub.toml config */
        logger.info(" stubConfigPath: {} ", stubConfigPath);
        BCOSStubConfigParser bcosStubConfigParser =
                new BCOSStubConfigParser(stubConfigPath, configName);
        BCOSStubConfig bcosStubConfig = bcosStubConfigParser.loadConfig();

        /** web3jWrapper is null ,create default one */
        if (Objects.isNull(web3jWrapper)) {
            Web3j web3j = Web3jUtility.initWeb3j(bcosStubConfig.getChannelService());
            web3jWrapper = new Web3jWrapperImpl(web3j);
            logger.info(" web3j: {} ", web3j);
        }

        checkBCOSVersion(web3jWrapper);

        BCOSConnection bcosConnection = new BCOSConnection(web3jWrapper);
        bcosConnection.setResourceInfoList(bcosStubConfig.convertToResourceInfos());

        bcosConnection.addProperty(
                BCOSConstant.BCOS_GROUP_ID, String.valueOf(bcosStubConfig.getChain().getGroupID()));
        bcosConnection.addProperty(
                BCOSConstant.BCOS_CHAIN_ID, String.valueOf(bcosStubConfig.getChain().getChainID()));
        bcosConnection.addProperty(
                BCOSConstant.BCOS_STUB_TYPE, String.valueOf(bcosStubConfig.getType()));

        CnsInfo cnsInfo = ProxyCNS.queryProxyCnsInfo(web3jWrapper);
        if (Objects.nonNull(cnsInfo)) {
            bcosConnection.addProperty(BCOSConstant.BCOS_PROXY_NAME, cnsInfo.getAddress());
            bcosConnection.addProperty(BCOSConstant.BCOS_PROXY_ABI, cnsInfo.getAbi());
        }
        return bcosConnection;
    }

    public static void checkBCOSVersion(Web3jWrapper web3jWrapper) throws Exception {
        NodeVersion.Version respondNodeVersion =
                web3jWrapper.getWeb3j().getNodeVersion().send().getNodeVersion();

        String supportedVersionStr = respondNodeVersion.getSupportedVersion();
        String nodeVersionStr = respondNodeVersion.getVersion();
        EnumNodeVersion.Version supportedVersion =
                EnumNodeVersion.getClassVersion(supportedVersionStr);

        /*2.4.0 gm or 2.4.0*/
        String[] strings = nodeVersionStr.split(" ");
        EnumNodeVersion.Version nodeVersion = EnumNodeVersion.getClassVersion(strings[0]);

        // must not below than 2.4.0
        if (!(supportedVersion.getMajor() == 2 && supportedVersion.getMinor() >= 4)) {
            throw new Exception(
                    "FISCO BCOS supported version is not supported, version must not below than 2.4.0, but current is "
                            + supportedVersionStr);
        }

        // must not below than 2.4.0
        if (!(nodeVersion.getMajor() == 2 && nodeVersion.getMinor() >= 4)) {
            throw new Exception(
                    "FISCO BCOS version is not supported, version must not below than 2.4.0, but current is "
                            + nodeVersionStr);
        }
    }
}
