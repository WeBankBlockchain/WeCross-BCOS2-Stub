package com.webank.wecross.stub.bcos.account;

import static com.webank.wecross.stub.bcos.common.BCOSConstant.BCOS_ACCOUNT;
import static com.webank.wecross.stub.bcos.common.BCOSConstant.BCOS_SM_ACCOUNT;

import com.webank.wecross.stub.Account;
import com.webank.wecross.stub.AccountFactory;
import com.webank.wecross.stub.bcos.config.BCOSAccountConfig;
import com.webank.wecross.stub.bcos.config.BCOSAccountConfigParser;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.Map;
import org.fisco.bcos.channel.client.P12Manager;
import org.fisco.bcos.channel.client.PEMManager;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.ECKeyPair;
import org.fisco.bcos.web3j.crypto.EncryptType;
import org.fisco.bcos.web3j.crypto.gm.GenCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BCOSAccountFactory implements AccountFactory {

    private static final Logger logger = LoggerFactory.getLogger(BCOSAccountFactory.class);

    public Account build(Map<String, Object> properties) {
        String username = (String) properties.get("username");
        Integer keyID = (Integer) properties.get("keyID");
        String type = (String) properties.get("type");
        Boolean isDefault = (Boolean) properties.get("isDefault");
        String pubKey = (String) properties.get("pubKey");
        String secKey = (String) properties.get("secKey");
        String address = (String) properties.get("ext0");

        if (EncryptType.encryptType == EncryptType.ECDSA_TYPE) {
            if (!type.equals(BCOS_ACCOUNT)) {
                logger.error("Invalid stub type: " + type);
                return null;
            }
        }

        if (EncryptType.encryptType == EncryptType.SM2_TYPE) {
            if (!type.equals(BCOS_SM_ACCOUNT)) {
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

        if (secKey == null || secKey.length() == 0) {
            logger.error("secKey has not given");
            return null;
        }

        try {
            logger.info("New account: {} type:{}", username, type);
            Credentials credentials = buildPemPrivateKey(secKey);
            BCOSAccount account = new BCOSAccount(username, type, credentials);

            /*
            if (!account.getCredentials().getAddress().equals(address)) {
                throw new Exception("Given address is not belongs to the secKey of " + username);
            }
             */

            return account;

        } catch (Exception e) {
            logger.error("BCOSAccount exception: " + e.getMessage());
            return null;
        }
    }

    public static BCOSAccount build(String name, String accountPath)
            throws IOException, CertificateException, UnrecoverableKeyException,
                    NoSuchAlgorithmException, KeyStoreException, NoSuchProviderException,
                    InvalidKeySpecException {
        String accountConfigFile = accountPath + File.separator + "account.toml";
        logger.debug("Loading account.toml: {}", accountConfigFile);

        BCOSAccountConfigParser parser = new BCOSAccountConfigParser(accountConfigFile);
        BCOSAccountConfig bcosAccountConfig = parser.loadConfig();

        String accountFile = accountPath + File.separator + bcosAccountConfig.getAccountFile();
        String passwd = bcosAccountConfig.getPasswd();

        Credentials credentials = null;
        if (accountFile.endsWith("p12")) {
            logger.debug("Loading account p12: {}", accountFile);
            credentials = loadP12Account(accountFile, passwd);
        } else {
            logger.debug("Loading account pem: {}", accountFile);
            credentials = loadPemAccount(accountFile);
        }

        return new BCOSAccount(name, bcosAccountConfig.getType(), credentials);
    }

    // load pem account file
    public static Credentials loadPemAccount(String accountFile)
            throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException,
                    NoSuchProviderException, InvalidKeySpecException, UnrecoverableKeyException {
        PEMManager pem = new PEMManager();
        pem.setPemFile(accountFile);
        pem.load();
        ECKeyPair keyPair = pem.getECKeyPair();
        Credentials credentials = GenCredential.create(keyPair.getPrivateKey().toString(16));

        logger.info(" credentials address: {}", credentials.getAddress());
        return credentials;
    }

    public static Credentials buildPemPrivateKey(String keyContent) throws Exception {
        PEMManager pem = new PEMManager();
        pem.load(new ByteArrayInputStream(keyContent.getBytes()));
        ECKeyPair keyPair = pem.getECKeyPair();
        Credentials credentials = GenCredential.create(keyPair.getPrivateKey().toString(16));

        logger.info(" credentials address: {}", credentials.getAddress());
        return credentials;
    }

    // load p12 account file
    public static Credentials loadP12Account(String accountFile, String password)
            throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException,
                    NoSuchProviderException, InvalidKeySpecException, UnrecoverableKeyException {
        P12Manager p12Manager = new P12Manager();
        p12Manager.setP12File(accountFile);
        p12Manager.setPassword(password);
        p12Manager.load();
        ECKeyPair keyPair = p12Manager.getECKeyPair();
        Credentials credentials = GenCredential.create(keyPair.getPrivateKey().toString(16));

        logger.info(" credentials address: {}", credentials.getAddress());
        return credentials;
    }
}
