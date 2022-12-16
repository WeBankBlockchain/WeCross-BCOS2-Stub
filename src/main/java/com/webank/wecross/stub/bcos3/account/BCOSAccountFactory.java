package com.webank.wecross.stub.bcos3.account;

import static com.webank.wecross.stub.bcos3.common.BCOSConstant.BCOS3_ECDSA_EVM_STUB_TYPE;
import static com.webank.wecross.stub.bcos3.common.BCOSConstant.BCOS3_ECDSA_WASM_STUB_TYPE;
import static com.webank.wecross.stub.bcos3.common.BCOSConstant.BCOS3_GM_EVM_STUB_TYPE;
import static com.webank.wecross.stub.bcos3.common.BCOSConstant.BCOS3_GM_WASM_STUB_TYPE;

import com.webank.wecross.stub.bcos3.config.BCOSAccountConfig;
import com.webank.wecross.stub.bcos3.config.BCOSAccountConfigParser;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.security.KeyPair;
import java.util.Map;
import org.fisco.bcos.sdk.v3.crypto.CryptoSuite;
import org.fisco.bcos.sdk.v3.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.v3.crypto.keystore.P12KeyStore;
import org.fisco.bcos.sdk.v3.crypto.keystore.PEMKeyStore;
import org.fisco.bcos.sdk.v3.model.CryptoType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

public class BCOSAccountFactory {

    private static final Logger logger = LoggerFactory.getLogger(BCOSAccountFactory.class);

    private CryptoSuite cryptoSuite;

    private BCOSAccountFactory(CryptoSuite cryptoSuite) {
        this.cryptoSuite = cryptoSuite;
    }

    public static BCOSAccountFactory getInstance(CryptoSuite cryptoSuite) {
        return new BCOSAccountFactory(cryptoSuite);
    }

    public BCOSAccount build(Map<String, Object> properties) {
        String username = (String) properties.get("username");
        Integer keyID = (Integer) properties.get("keyID");
        String type = (String) properties.get("type");
        Boolean isDefault = (Boolean) properties.get("isDefault");
        String pubKey = (String) properties.get("pubKey");
        String secKey = (String) properties.get("secKey");
        String address = (String) properties.get("ext0");

        if (cryptoSuite.getCryptoTypeConfig() == CryptoType.ECDSA_TYPE) {
            if (!type.equals(BCOS3_ECDSA_EVM_STUB_TYPE)
                    && !type.equals(BCOS3_ECDSA_WASM_STUB_TYPE)) {
                logger.error("Invalid stub type: " + type);
                return null;
            }
        }

        if (cryptoSuite.getCryptoTypeConfig() == CryptoType.SM_TYPE) {
            if (!type.equals(BCOS3_GM_EVM_STUB_TYPE) && !type.equals(BCOS3_GM_WASM_STUB_TYPE)) {
                logger.error("Invalid stub type: " + type);
                return null;
            }
        }

        if (username == null || username.length() == 0) {
            logger.error("username has not given");
            return null;
        }

        if (keyID == null) {
            logger.error("keyID has not given");
            return null;
        }

        if (isDefault == null) {
            logger.error("isDefault has not given");
            return null;
        }

        if (pubKey == null || pubKey.length() == 0) {
            logger.error("pubKey has not given");
            return null;
        }

        if (secKey == null || secKey.length() == 0) {
            logger.error("secKey has not given");
            return null;
        }

        if (address == null || address.length() == 0) {
            logger.error("address has not given in ext0");
            return null;
        }

        try {
            logger.info("New account: {} type:{}", username, type);
            CryptoKeyPair cryptoKeyPair = buildPemPrivateKey(secKey);
            BCOSAccount account = new BCOSAccount(username, type, cryptoKeyPair);

            if (!account.getCredentials().getAddress().equals(address)) {
                throw new Exception("Given address is not belongs to the secKey of " + username);
            }

            return account;

        } catch (Exception e) {
            logger.error("BCOSAccount exception: " + e.getMessage());
            return null;
        }
    }

    public BCOSAccount build(String name, String accountPath) throws IOException {
        String accountConfigFile = accountPath + File.separator + "account.toml";
        logger.debug("Loading account.toml: {}", accountConfigFile);

        BCOSAccountConfigParser parser = new BCOSAccountConfigParser(accountConfigFile);
        BCOSAccountConfig bcosAccountConfig = parser.loadConfig();

        String accountFile = accountPath + File.separator + bcosAccountConfig.getAccountFile();
        String passwd = bcosAccountConfig.getPasswd();

        CryptoKeyPair cryptoKeyPair = null;
        if (accountFile.endsWith("p12")) {
            logger.debug("Loading account p12: {}", accountFile);
            cryptoKeyPair = loadP12Account(accountFile, passwd);
        } else {
            logger.debug("Loading account pem: {}", accountFile);
            cryptoKeyPair = loadPemAccount(accountFile);
        }

        return new BCOSAccount(name, bcosAccountConfig.getType(), cryptoKeyPair);
    }

    // load pem account file
    public CryptoKeyPair loadPemAccount(String accountFile) throws IOException {
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource pemResources = resolver.getResource(accountFile);
        PEMKeyStore keyTool = new PEMKeyStore(pemResources.getInputStream());
        CryptoKeyPair cryptoKeyPair =
                cryptoSuite.getKeyPairFactory().createKeyPair(keyTool.getKeyPair());
        logger.info(" credentials address: {}", cryptoKeyPair.getAddress());
        return cryptoKeyPair;
    }

    public CryptoKeyPair buildPemPrivateKey(String keyContent) {
        PEMKeyStore pemKeyStore = new PEMKeyStore(new ByteArrayInputStream(keyContent.getBytes()));
        KeyPair keyPair = pemKeyStore.getKeyPair();
        CryptoKeyPair cryptoKeyPair = cryptoSuite.getKeyPairFactory().createKeyPair(keyPair);
        logger.info(" credentials address: {}", cryptoKeyPair.getAddress());
        return cryptoKeyPair;
    }

    // load p12 account file
    public CryptoKeyPair loadP12Account(String accountFile, String password) throws IOException {
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource p12Resources = resolver.getResource(accountFile);
        P12KeyStore keyTool = new P12KeyStore(p12Resources.getInputStream(), password);
        CryptoKeyPair cryptoKeyPair =
                cryptoSuite.getKeyPairFactory().createKeyPair(keyTool.getKeyPair());
        logger.info(" credentials address: {}", cryptoKeyPair.getAddress());
        return cryptoKeyPair;
    }
}
