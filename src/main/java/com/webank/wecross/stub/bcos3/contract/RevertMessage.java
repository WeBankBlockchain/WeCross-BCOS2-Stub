package com.webank.wecross.stub.bcos3.contract;

import org.fisco.bcos.sdk.v3.codec.abi.TypeDecoder;
import org.fisco.bcos.sdk.v3.codec.datatypes.TypeReference;
import org.fisco.bcos.sdk.v3.codec.datatypes.Utf8String;
import org.fisco.bcos.sdk.v3.codec.datatypes.generated.tuples.generated.Tuple2;
import org.fisco.bcos.sdk.v3.model.PrecompiledRetCode;
import org.fisco.bcos.sdk.v3.model.RetCode;
import org.fisco.bcos.sdk.v3.transaction.codec.decode.RevertMessageParser;
import org.fisco.bcos.sdk.v3.utils.Hex;
import org.fisco.bcos.sdk.v3.utils.Numeric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RevertMessage {

    private static final Logger logger = LoggerFactory.getLogger(RevertMessage.class);

    /**
     * @param message
     * @return
     */
    public static String getErrorMessage(String message) {
        int errorIndex = message.indexOf("error:");
        if (errorIndex > 0) {
            int errorCode = Integer.parseInt(message.substring(errorIndex + 6).trim());
            errorCode = (errorCode > 0 ? -errorCode : errorCode);
            RetCode retCode = PrecompiledRetCode.getPrecompiledResponse(errorCode, "unknown error");

            if (logger.isDebugEnabled()) {
                logger.debug(" errorCode: {}, errorMessage: {}", errorCode, retCode);
            }
            return retCode.getMessage();
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
    public static Tuple2<Boolean, String> tryParserRevertMessage(Integer status, String data) {
        try {
            int revertLoop = 0;

            while (RevertMessageParser.hasRevertMessage(
                    status, Numeric.cleanHexPrefix(data).substring((128 + 8) * revertLoop))) {
                revertLoop += 1;
            }
            if (revertLoop > 0) {
                Utf8String utf8String =
                        TypeDecoder.decode(
                                Hex.decode(
                                        Numeric.cleanHexPrefix(data)
                                                .substring((128 + 8) * revertLoop - 64)),
                                0,
                                new TypeReference<Utf8String>() {});
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
