package com.webank.wecross.stub.bcos.verify;

import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.stub.bcos.common.BCOSBlockHeader;
import com.webank.wecross.stub.bcos.uaproof.Signer;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.fisco.bcos.web3j.crypto.EncryptType;
import org.fisco.bcos.web3j.protocol.ObjectMapperFactory;
import org.fisco.bcos.web3j.protocol.core.methods.response.BcosBlockHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlockHeaderValidation {
    private static final Logger logger = LoggerFactory.getLogger(BlockHeaderValidation.class);

    public static void verifyBlockHeader(String sealerMapString, BCOSBlockHeader bcosBlockHeader)
            throws IOException, WeCrossException {
        //        BCOSStubConfig.Sealers sealers =
        Map<String, String> sealerMap =
                ObjectMapperFactory.getObjectMapper().readValue(sealerMapString, Map.class);

        List<BcosBlockHeader.Signature> signatureList = bcosBlockHeader.getSignatureList();

        String blockHash = bcosBlockHeader.getHash();
        Signer signer = Signer.newSigner(EncryptType.encryptType);
        boolean verifyFlag = true;
        for (BcosBlockHeader.Signature signature : signatureList) {
            String pubKey = sealerMap.get(signature.getIndex());
            if (pubKey == null) {
                logger.error(
                        "Can not find pubKey in sealerMap, signatureList is {}", signatureList);
                throw new WeCrossException(
                        WeCrossException.ErrorCode.INTERNAL_ERROR, "verifyBlockHeader fail!");
            }
            verifyFlag =
                    signer.verify(
                            signature.getSignature().getBytes(), blockHash.getBytes(), pubKey);
            if (!verifyFlag) break;
        }
        if (!verifyFlag) {
            logger.error("VerifyBlockHeader fail!, signatureList is {}", signatureList);
            throw new WeCrossException(
                    WeCrossException.ErrorCode.INTERNAL_ERROR, "verifyBlockHeader fail!");
        }
    }
}
