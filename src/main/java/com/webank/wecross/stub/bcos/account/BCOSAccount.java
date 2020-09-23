package com.webank.wecross.stub.bcos.account;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.stub.Account;
import com.webank.wecross.stub.UniversalAccount;
import com.webank.wecross.stub.bcos.uaproof.PublicSign;
import com.webank.wecross.stub.bcos.uaproof.Signer;
import com.webank.wecross.stub.bcos.uaproof.UAProof;
import java.util.AbstractMap;
import java.util.Map;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.EncryptType;
import org.fisco.bcos.web3j.protocol.ObjectMapperFactory;
import org.fisco.bcos.web3j.utils.Numeric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BCOSAccount implements Account {

    private Logger logger = LoggerFactory.getLogger(BCOSAccount.class);

    private final String name;
    private final String type;
    private final String publicKey;
    private final Credentials credentials;

    private int keyID;

    private boolean isDefault;

    public BCOSAccount(String name, String type, Credentials credentials) {
        this.name = name;
        this.type = type;
        this.credentials = credentials;
        this.publicKey =
                Numeric.toHexStringNoPrefixZeroPadded(
                        credentials.getEcKeyPair().getPublicKey(), 128);
        logger.info(" name: {}, type: {}, publicKey: {}", name, type, publicKey);
    }

    public Credentials getCredentials() {
        return credentials;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getIdentity() {
        return credentials.getAddress();
    }

    @Override
    public int getKeyID() {
        return keyID;
    }

    @Override
    public boolean isDefault() {
        return isDefault;
    }

    public void setKeyID(int keyID) {
        this.keyID = keyID;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    public String getPub() {
        return publicKey;
    }

    @Override
    public String generateUAProof(UniversalAccount ua) {

        String caPub = Numeric.cleanHexPrefix(getPub());
        String uaPub = Numeric.cleanHexPrefix(ua.getPub());

        long currentTimeMillis = System.currentTimeMillis();
        PublicSign uaPublicSign = new PublicSign(uaPub, currentTimeMillis);
        PublicSign caPublicSign = new PublicSign(caPub, currentTimeMillis);

        ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();

        /*
        BCOS privateKey sign UA   data
        UA   privateKey sign BCOS data
         */
        try {

            Signer signer = Signer.newSigner(EncryptType.encryptType);

            byte[] caSign =
                    signer.sign(
                            getCredentials().getEcKeyPair(),
                            objectMapper.writeValueAsBytes(uaPublicSign));

            byte[] uaSign = ua.sign(objectMapper.writeValueAsBytes(caPublicSign));

            UAProof proof = new UAProof();
            proof.setTimestamp(currentTimeMillis);
            proof.setCaPub(caPub);
            proof.setUaPub(uaPub);
            proof.setCaSig(Hex.encodeHexString(caSign));
            proof.setUaSig(Hex.encodeHexString(uaSign));

            logger.info("proof: {}", proof);

            return objectMapper.writeValueAsString(proof);
        } catch (JsonProcessingException e) {
            throw new UnsupportedOperationException(e.getCause());
        }
    }

    @Override
    public Map.Entry<String, String> recoverProof(String proof, UniversalAccount ua) {
        try {
            ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
            UAProof uaProof = objectMapper.readValue(proof, UAProof.class);
            /*
            Verify UA signature
             */
            PublicSign uaPublicSign = new PublicSign(uaProof.getUaPub(), uaProof.getTimestamp());
            PublicSign caPublicSign = new PublicSign(uaProof.getCaPub(), uaProof.getTimestamp());

            if (!ua.verify(
                    Hex.decodeHex(uaProof.getUaSig()),
                    objectMapper.writeValueAsBytes(caPublicSign))) {
                logger.error(
                        " Failed to verify UA account signature, ua: {}, proof: {}", ua, uaProof);
                return null;
            }

            /*
            Verify Chain signature
             */
            Signer signer = Signer.newSigner(EncryptType.encryptType);
            if (!signer.verify(
                    Hex.decodeHex(uaProof.getCaSig()),
                    objectMapper.writeValueAsBytes(uaPublicSign),
                    uaProof.getCaPub())) {
                logger.error(
                        " Failed to verify chain account signature, ua: {}, proof: {}",
                        ua,
                        uaProof);
                return null;
            }

            return new AbstractMap.SimpleEntry<>(uaProof.getCaPub(), uaProof.getUaPub());

        } catch (DecoderException | JsonProcessingException e) {
            logger.error("e:", e);
            return null;
        }
    }
}
