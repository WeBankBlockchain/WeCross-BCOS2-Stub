package com.webank.wecross.stub.bcos.preparation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.stub.bcos.common.BCOSConstant;
import com.webank.wecross.stub.bcos.contract.FunctionUtility;
import com.webank.wecross.stub.bcos.web3j.Web3jWrapper;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.fisco.bcos.web3j.abi.FunctionEncoder;
import org.fisco.bcos.web3j.abi.TypeReference;
import org.fisco.bcos.web3j.abi.datatypes.Function;
import org.fisco.bcos.web3j.abi.datatypes.Type;
import org.fisco.bcos.web3j.abi.datatypes.Utf8String;
import org.fisco.bcos.web3j.precompile.cns.CnsInfo;
import org.fisco.bcos.web3j.protocol.ObjectMapperFactory;
import org.fisco.bcos.web3j.protocol.channel.StatusCode;
import org.fisco.bcos.web3j.protocol.core.methods.response.Call;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CnsService {

    private static final Logger logger = LoggerFactory.getLogger(CnsService.class);

    public static CnsInfo queryProxyCnsInfo(Web3jWrapper web3jWrapper) {
        return queryCnsInfo(web3jWrapper, BCOSConstant.BCOS_PROXY_NAME);
    }

    public static CnsInfo queryHubCnsInfo(Web3jWrapper web3jWrapper) {
        return queryCnsInfo(web3jWrapper, BCOSConstant.BCOS_HUB_NAME);
    }

    /** query cns to get address,abi of hub contract */
    public static CnsInfo queryCnsInfo(Web3jWrapper web3jWrapper, String name) {
        if (name.equals(BCOSConstant.BCOS_CNS_NAME)) {
            CnsInfo cnsPrecompiledInfo = new CnsInfo();
            cnsPrecompiledInfo.setName(BCOSConstant.BCOS_CNS_NAME);
            cnsPrecompiledInfo.setAbi(BCOSConstant.CNS_ABI);
            cnsPrecompiledInfo.setAddress(BCOSConstant.CNS_PRECOMPILED_ADDRESS);
            cnsPrecompiledInfo.setVersion("1.0");
            return cnsPrecompiledInfo;
        }

        /** function selectByName(string memory cnsName) public returns(string memory) */
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
