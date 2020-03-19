package com.webank.wecross.stub.bcos;

import com.webank.wecross.stub.bcos.config.BCOSStubConfig;
import com.webank.wecross.stub.bcos.config.BCOSStubConfigParser;
import com.webank.wecross.stub.bcos.web3j.Web3jWrapper;
import com.webank.wecross.stub.bcos.web3j.Web3jWrapperFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BCOSConnectionFactory {
    private static Logger logger = LoggerFactory.getLogger(BCOSConnectionFactory.class);

    public static BCOSConnection build(String stubConfigPath) throws Exception {
        /** load stub.toml config */
        logger.info(" stubConfigPath: ", stubConfigPath);
        BCOSStubConfigParser loader = new BCOSStubConfigParser(stubConfigPath);
        BCOSStubConfig bcosStubConfig = loader.loadConfig();

        Web3jWrapper web3jWrapper = Web3jWrapperFactory.build(bcosStubConfig.getChannelService());
        BCOSConnection bcosConnection = new BCOSConnection(web3jWrapper);

        bcosConnection.setResourceInfoList(
                bcosConnection.getResourceInfoList(bcosStubConfig.getResources()));
        return bcosConnection;
    }
}
