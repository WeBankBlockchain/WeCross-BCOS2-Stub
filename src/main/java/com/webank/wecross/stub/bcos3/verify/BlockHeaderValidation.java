package com.webank.wecross.stub.bcos3.verify;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.stub.bcos3.common.BCOSBlockHeader;
import com.webank.wecross.stub.bcos3.common.BCOSConstant;
import com.webank.wecross.stub.bcos3.common.ObjectMapperFactory;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.fisco.bcos.sdk.v3.client.protocol.response.BcosBlockHeader;
import org.fisco.bcos.sdk.v3.crypto.CryptoSuite;
import org.fisco.bcos.sdk.v3.utils.Numeric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlockHeaderValidation {
    private static final Logger logger = LoggerFactory.getLogger(BlockHeaderValidation.class);

    public static void verifyBlockHeader(
            BCOSBlockHeader bcosBlockHeader,
            String blockVerifierString,
            String stubType,
            CryptoSuite cryptoSuite)
            throws WeCrossException {
        String chainType = getChainTypeInBCOSVerifier(blockVerifierString);
        if (!stubType.equals(chainType)) {
            throw new WeCrossException(
                    WeCrossException.ErrorCode.UNEXPECTED_CONFIG,
                    "blockVerifier config error: wrong chainType: "
                            + chainType
                            + " actual stubType: "
                            + stubType);
        }

        List<String> sealerList = getPubKeyInBCOSVerifier(blockVerifierString);

        List<BcosBlockHeader.Signature> signatureList = bcosBlockHeader.getSignatureList();

        if (signatureList == null) {
            logger.error("signatureList is null");
            throw new WeCrossException(
                    WeCrossException.ErrorCode.INTERNAL_ERROR,
                    "verifyBlockHeader fail, signatureList is null.");
        }

        if (bcosBlockHeader.getNumber() != 0 && !isSignUnique(signatureList)) {
            logger.error(
                    "Some signature in SignList is not unique, signatureList is {}",
                    bcosBlockHeader.signatureListToString());
            throw new WeCrossException(
                    WeCrossException.ErrorCode.INTERNAL_ERROR,
                    "verifyBlockHeader fail, caused by sign is not unique.");
        }
        String blockHash = bcosBlockHeader.getHash();
        boolean verifyFlag = false;
        boolean finalizeFlag = true;
        try {
            for (BcosBlockHeader.Signature signature : signatureList) {
                for (String sealer : sealerList) {
                    byte[] signData = Numeric.hexStringToByteArray(signature.getSignature());
                    byte[] hashData = Numeric.hexStringToByteArray(blockHash);
                    verifyFlag = cryptoSuite.verify(sealer, hashData, signData);
                    if (verifyFlag) break;
                }
                finalizeFlag = finalizeFlag && verifyFlag;
            }
        } catch (Exception e) {
            throw new WeCrossException(
                    WeCrossException.ErrorCode.INTERNAL_ERROR,
                    "verifyBlockHeader fail, caused by " + e.getMessage());
        }
        if (logger.isDebugEnabled()) {
            logger.debug(
                    "Verify BCOS BlockHeader enabled, the final verification result is {}",
                    finalizeFlag);
        }
        if (!finalizeFlag) {
            logger.error(
                    "VerifyBlockHeader fail!, signatureList is {}",
                    bcosBlockHeader.signatureListToString());
            throw new WeCrossException(
                    WeCrossException.ErrorCode.INTERNAL_ERROR,
                    "verifyBlockHeader fail, caused by verify fail.");
        }
    }

    private static boolean isSignUnique(List<BcosBlockHeader.Signature> signatureList) {
        Set<String> testSet = new HashSet<>();
        boolean testFlag = false;
        for (BcosBlockHeader.Signature signature : signatureList) {
            testFlag = testSet.add(signature.getSignature());
            if (!testFlag) break;
        }

        return testFlag;
    }

    private static String getChainTypeInBCOSVerifier(String blockVerifierString)
            throws WeCrossException {
        ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
        try {
            Objects.requireNonNull(
                    blockVerifierString,
                    "'blockVerifierString' in getPubKeyInBCOSVerifier is null.");
            Map<String, Object> bcosVerifierMapper =
                    objectMapper.readValue(
                            blockVerifierString, new TypeReference<Map<String, Object>>() {});
            return (String) bcosVerifierMapper.get("chainType");
        } catch (JsonProcessingException e) {
            throw new WeCrossException(
                    WeCrossException.ErrorCode.UNEXPECTED_CONFIG,
                    "Parse Json to BCOSVerifier Error, " + e.getMessage(),
                    e.getCause());
        } catch (Exception e) {
            throw new WeCrossException(
                    WeCrossException.ErrorCode.UNEXPECTED_CONFIG,
                    "Read BCOSVerifier Json Error, " + e.getMessage(),
                    e.getCause());
        }
    }

    private static List<String> getPubKeyInBCOSVerifier(String blockVerifierString)
            throws WeCrossException {
        ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
        try {
            Objects.requireNonNull(
                    blockVerifierString,
                    "'blockVerifierString' in getPubKeyInBCOSVerifier is null.");
            Map<String, Object> bcosVerifierMapper =
                    objectMapper.readValue(
                            blockVerifierString, new TypeReference<Map<String, Object>>() {});
            List<String> pubKey = (List<String>) bcosVerifierMapper.get("pubKey");
            if (pubKey == null) {
                throw new WeCrossException(
                        WeCrossException.ErrorCode.UNEXPECTED_CONFIG,
                        "pubKey is null in BCOS Verifier.");
            }
            for (String key : pubKey) {
                if (key.length() != BCOSConstant.BCOS_NODE_ID_LENGTH) {
                    throw new WeCrossException(
                            WeCrossException.ErrorCode.UNEXPECTED_CONFIG,
                            "pubKey length is not in conformity with the BCOS right way, pubKey: "
                                    + key
                                    + " length is "
                                    + key.length());
                }
            }
            return pubKey;
        } catch (JsonProcessingException e) {
            throw new WeCrossException(
                    WeCrossException.ErrorCode.UNEXPECTED_CONFIG,
                    "Parse Json to BCOSVerifier Error, " + e.getMessage(),
                    e.getCause());
        } catch (Exception e) {
            throw new WeCrossException(
                    WeCrossException.ErrorCode.UNEXPECTED_CONFIG,
                    "Read BCOSVerifier Json Error, " + e.getMessage(),
                    e.getCause());
        }
    }
}
