package com.webank.wecross.stub.bcos.preparation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.stub.bcos.common.BCOSConstant;
import com.webank.wecross.stub.bcos.common.StatusCode;
import com.webank.wecross.stub.bcos.contract.FunctionUtility;
import com.webank.wecross.stub.bcos.web3j.Web3jWrapper;
import org.fisco.bcos.sdk.abi.FunctionEncoder;
import org.fisco.bcos.sdk.abi.TypeReference;
import org.fisco.bcos.sdk.abi.datatypes.Function;
import org.fisco.bcos.sdk.abi.datatypes.Type;
import org.fisco.bcos.sdk.abi.datatypes.Utf8String;
import org.fisco.bcos.sdk.client.protocol.response.Call;
import org.fisco.bcos.sdk.contract.precompiled.cns.CnsInfo;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.utils.ObjectMapperFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class CnsService {

    private static final Logger logger = LoggerFactory.getLogger(CnsService.class);

    public static final int MAX_VERSION_LENGTH = 40;

    public static CnsInfo queryProxyCnsInfo(Web3jWrapper web3jWrapper, CryptoSuite cryptoSuite) {
        return queryCnsInfo(web3jWrapper, BCOSConstant.BCOS_PROXY_NAME, cryptoSuite);
    }

    public static CnsInfo queryHubCnsInfo(Web3jWrapper web3jWrapper, CryptoSuite cryptoSuite) {
        return queryCnsInfo(web3jWrapper, BCOSConstant.BCOS_HUB_NAME, cryptoSuite);
    }

    /** query cns to get address,abi of hub contract */
    private static CnsInfo queryCnsInfo(Web3jWrapper web3jWrapper, String name, CryptoSuite cryptoSuite) {
        /** function selectByName(string memory cnsName) public returns(string memory) */
        FunctionEncoder functionEncoder = new FunctionEncoder(cryptoSuite);

        Function function =
                new Function(
                        BCOSConstant.CNS_METHOD_SELECTBYNAME,
                        Arrays.<Type>asList(new Utf8String(name)),
                        Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        try {
            Call.CallOutput callOutput =
                    web3jWrapper.call(
                            BCOSConstant.DEFAULT_ADDRESS,
                            BCOSConstant.CNS_PRECOMPILED_ADDRESS,
                            functionEncoder.encode(function));

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
                    logger.warn("Cns info empty.");
                    return null;
                } else {
                    int size = infoList.size();
                    CnsInfo hubCnsInfo = infoList.get(size - 1);
                    logger.info(
                            "{} cns info, name: {}, version: {}, address: {}, abi: {}",
                            name,
                            hubCnsInfo.getName(),
                            hubCnsInfo.getVersion(),
                            hubCnsInfo.getAddress(),
                            hubCnsInfo.getAbi());
                    return hubCnsInfo;
                }
            } else {
                logger.error(
                        "Unable query {} cns info, status: {}, message: {}",
                        name,
                        callOutput.getStatus(),
                        StatusCode.getStatusMessage(callOutput.getStatus()));
                return null;
            }
        } catch (Exception e) {
            logger.error("Query {} cns info e: ", name, e);
            return null;
        }
    }
}
