package com.webank.wecross.stub.bcos;

import com.webank.wecross.stub.bcos.config.BCOSStubConfig;
import com.webank.wecross.stub.bcos.config.BCOSStubConfigParser;
import com.webank.wecross.stub.bcos.web3j.Web3jUtility;
import com.webank.wecross.stub.bcos.web3j.Web3jWrapper;
import com.webank.wecross.stub.bcos.web3j.Web3jWrapperImpl;
import java.util.Objects;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BCOSConnectionFactory {
    private static final Logger logger = LoggerFactory.getLogger(BCOSConnectionFactory.class);

    private BCOSConnectionFactory() {}

    public static BCOSConnection build(String stubConfigPath, Web3jWrapper web3jWrapper)
            throws Exception {
        /** load stub.toml config */
        logger.info(" stubConfigPath: {} ", stubConfigPath);
        BCOSStubConfigParser bcosStubConfigParser = new BCOSStubConfigParser(stubConfigPath);
        BCOSStubConfig bcosStubConfig = bcosStubConfigParser.loadConfig();

        /** web3jWrapper is null ,create default one */
        if (Objects.isNull(web3jWrapper)) {
            Web3j web3j = Web3jUtility.initWeb3j(bcosStubConfig.getChannelService());
            web3jWrapper = new Web3jWrapperImpl(web3j);
            logger.info(" web3j: {} ", web3j);
        }

        BCOSConnection bcosConnection = new BCOSConnection(web3jWrapper);
        bcosConnection.setResourceInfoList(bcosStubConfig.convertToResourceInfos());
        return bcosConnection;
    }
}
