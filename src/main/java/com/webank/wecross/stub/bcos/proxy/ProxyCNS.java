package com.webank.wecross.stub.bcos.proxy;

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

public class ProxyCNS {

    private static final Logger logger = LoggerFactory.getLogger(ProxyCNS.class);

    /** query cns to get address,abi of proxy contract */
    public static CnsInfo queryProxyCnsInfo(Web3jWrapper web3jWrapper) {
        /** function selectByName(string memory cnsName) public returns(string memory) */
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
                    logger.warn(" cns info empty.");
                    return null;
                } else {
                    int size = infoList.size();
                    CnsInfo proxyCnsInfo = infoList.get(size - 1);
                    logger.info(
                            " WeCrossProxy cns info, name: {}, version: {}, address: {}, abi: {}",
                            proxyCnsInfo.getName(),
                            proxyCnsInfo.getVersion(),
                            proxyCnsInfo.getAddress(),
                            proxyCnsInfo.getAbi());
                    return proxyCnsInfo;
                }
            } else {
                logger.error(
                        " unable query proxy cns info, status: {}, message: {}",
                        callOutput.getStatus(),
                        StatusCode.getStatusMessage(callOutput.getStatus()));
                return null;
            }
        } catch (Exception e) {
            logger.error(" query proxy cns info e: ", e);
            return null;
        }
    }
}
