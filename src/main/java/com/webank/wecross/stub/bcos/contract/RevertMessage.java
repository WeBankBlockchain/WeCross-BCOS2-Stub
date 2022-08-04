package com.webank.wecross.stub.bcos.contract;

import java.io.IOException;

import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple2;
import org.fisco.bcos.web3j.abi.TypeDecoder;
import org.fisco.bcos.web3j.abi.datatypes.Utf8String;
import org.fisco.bcos.web3j.precompile.common.PrecompiledCommon;
import org.fisco.bcos.web3j.precompile.common.PrecompiledResponse;
import org.fisco.bcos.web3j.protocol.ObjectMapperFactory;
import org.fisco.bcos.web3j.tuples.generated.Tuple2;
import org.fisco.bcos.web3j.tx.RevertResolver;
import org.fisco.bcos.web3j.utils.Numeric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RevertMessage {

    private static final Logger logger = LoggerFactory.getLogger(RevertMessage.class);

    /**
     * @param message
     * @return
     */
    public static String getErrorMessage(String message) {
        try {
            int errorIndex = message.indexOf("error:");
            if (errorIndex > 0) {
                int errorCode = Integer.parseInt(message.substring(errorIndex + 6).trim());
                errorCode = (errorCode > 0 ? -errorCode : errorCode);
                String errorMessage = PrecompiledCommon.transferToJson(errorCode);
                PrecompiledResponse precompiledResponse =
                        ObjectMapperFactory.getObjectMapper()
                                .readValue(errorMessage, PrecompiledResponse.class);
                if (logger.isDebugEnabled()) {
                    logger.debug(" errorCode: {}, errorMessage: {}", errorCode, errorMessage);
                }
                return precompiledResponse.getMsg();
            }
        } catch (IOException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("e: ", e);
            }
        }

        return "";
    }

    /**
     * try to resolve revert message, supports recursive operations
     *
     * @param status
     * @param data
     * @return
     */
    public static Tuple2<Boolean, String> tryParserRevertMessage(String status, String data) {
        try {
            int revertLoop = 0;

            while (RevertResolver.hasRevertMessage(
                    status, Numeric.cleanHexPrefix(data).substring((128 + 8) * revertLoop))) {
                revertLoop += 1;
            }

            if (revertLoop > 0) {
                Utf8String utf8String =
                        TypeDecoder.decode(
                                Numeric.cleanHexPrefix(data).substring((128 + 8) * revertLoop - 64),
                                0,
                                Utf8String.class);
                String revertMessage = utf8String.toString().trim();
                String errorMessage = getErrorMessage(revertMessage);
                if (!errorMessage.isEmpty()) {
                    revertMessage = revertMessage + " ,message: " + errorMessage;
                }

                if (logger.isDebugEnabled()) {
                    logger.debug(" revertMessage: {}", revertMessage);
                }

                return new Tuple2<>(true, revertMessage);
            }

            return new Tuple2<>(false, null);
        } catch (Exception e) {
            return new Tuple2<>(false, null);
        }
    }
}
