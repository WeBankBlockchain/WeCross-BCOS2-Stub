package com.webank.wecross.stub.bcos.account;

import com.webank.wecross.stub.bcos.config.BCOSAccountConfig;
import com.webank.wecross.stub.bcos.config.BCOSAccountConfigParser;
import java.io.File;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import org.fisco.bcos.channel.client.P12Manager;
import org.fisco.bcos.channel.client.PEMManager;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.ECKeyPair;
import org.fisco.bcos.web3j.crypto.gm.GenCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BCOSAccountFactory {

    private static final Logger logger = LoggerFactory.getLogger(BCOSAccountFactory.class);

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

        BCOSAccount bcosAccount = new BCOSAccount(name, bcosAccountConfig.getType(), credentials);
        return bcosAccount;
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
