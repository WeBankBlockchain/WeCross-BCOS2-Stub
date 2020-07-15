package com.webank.wecross.stub.bcos;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.stub.bcos.common.BCOSConstant;
import com.webank.wecross.stub.bcos.config.BCOSStubConfig;
import com.webank.wecross.stub.bcos.config.BCOSStubConfigParser;
import com.webank.wecross.stub.bcos.contract.FunctionUtility;
import com.webank.wecross.stub.bcos.web3j.Web3jUtility;
import com.webank.wecross.stub.bcos.web3j.Web3jWrapper;
import com.webank.wecross.stub.bcos.web3j.Web3jWrapperImpl;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.fisco.bcos.fisco.EnumNodeVersion;
import org.fisco.bcos.web3j.abi.FunctionEncoder;
import org.fisco.bcos.web3j.abi.TypeReference;
import org.fisco.bcos.web3j.abi.datatypes.Function;
import org.fisco.bcos.web3j.abi.datatypes.Type;
import org.fisco.bcos.web3j.abi.datatypes.Utf8String;
import org.fisco.bcos.web3j.precompile.cns.CnsInfo;
import org.fisco.bcos.web3j.protocol.ObjectMapperFactory;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.channel.StatusCode;
import org.fisco.bcos.web3j.protocol.core.methods.response.Call;
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

        CnsInfo cnsInfo = getWeCrossProxyCnsInfo(web3jWrapper);
        if (Objects.nonNull(cnsInfo)) {
            bcosConnection.addProperty(BCOSConstant.BCOS_PROXY_NAME, cnsInfo.getAddress());
            bcosConnection.addProperty(BCOSConstant.BCOS_PROXY_ABI, cnsInfo.getAbi());
        }
        return bcosConnection;
    }

    /** query cns to get address,abi of proxy contract */
    public static CnsInfo getWeCrossProxyCnsInfo(Web3jWrapper web3jWrapper) {
        Function function =
                new Function(
                        BCOSConstant.CNS_METHOD_SELECTBYNAME,
                        Arrays.<Type>asList(new Utf8String(BCOSConstant.BCOS_PROXY_NAME)),
                        Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        try {
            Call.CallOutput callOutput =
                    web3jWrapper.call(
                            BCOSConstant.DEFAULT_ADDRESS,
                            BCOSConstant.CNS_PRECOMPILED_ADDRESS,
                            FunctionEncoder.encode(function));

            if (logger.isTraceEnabled()) {
                logger.trace(
                        "call result, status: {}, blockNumber: {}",
                        callOutput.getStatus(),
                        callOutput.getCurrentBlockNumber());
            }

            if (StatusCode.Success.equals(callOutput.getStatus())) {
                String cnsInfo = FunctionUtility.decodeOutputAsString(callOutput.getOutput());
                if (Objects.isNull(cnsInfo)) {
                    return null;
                }

                ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
                List<CnsInfo> infoList =
                        objectMapper.readValue(
                                cnsInfo,
                                objectMapper
                                        .getTypeFactory()
                                        .constructCollectionType(List.class, CnsInfo.class));

                if (Objects.isNull(infoList) || infoList.isEmpty()) {
                    return null;
                } else {
                    int size = infoList.size();
                    CnsInfo proxyCnsInfo = infoList.get(size - 1);
                    logger.info(
                            " WeCrossProxy CNS, name: {}, version: {}, address: {}, abi: {}",
                            proxyCnsInfo.getName(),
                            proxyCnsInfo.getVersion(),
                            proxyCnsInfo.getAddress(),
                            proxyCnsInfo.getAbi());
                    return proxyCnsInfo;
                }
            } else {
                logger.warn(
                        "getting cns of proxy contract failed, status: {}, message: {}",
                        callOutput.getStatus(),
                        StatusCode.getStatusMessage(callOutput.getStatus()));
                return null;
            }
        } catch (Exception e) {
            logger.warn("getting cns of proxy contract failed, e: ", e);
            return null;
        }
    }

    public static void checkBCOSVersion(Web3jWrapper web3jWrapper) throws Exception {
        NodeVersion.Version respondNodeVersion =
                web3jWrapper.getWeb3j().getNodeVersion().send().getNodeVersion();
        String supportedVersionStr = respondNodeVersion.getSupportedVersion();
        String nodeVersionStr = respondNodeVersion.getVersion();
        EnumNodeVersion.Version supportedVersion =
                EnumNodeVersion.getClassVersion(supportedVersionStr);
        EnumNodeVersion.Version nodeVersion = EnumNodeVersion.getClassVersion(nodeVersionStr);

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
