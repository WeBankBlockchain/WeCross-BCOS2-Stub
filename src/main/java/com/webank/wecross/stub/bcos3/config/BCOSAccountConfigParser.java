package com.webank.wecross.stub.bcos3.config;

import com.moandjiezana.toml.Toml;
import com.webank.wecross.stub.bcos3.common.BCOSConstant;
import com.webank.wecross.stub.bcos3.common.BCOSToml;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BCOSAccountConfigParser extends AbstractBCOSConfigParser {

    private static final Logger logger = LoggerFactory.getLogger(BCOSAccountConfigParser.class);

    public BCOSAccountConfigParser(String configPath) {
        super(configPath);
    }

    public BCOSAccountConfig loadConfig() throws IOException {
        BCOSToml bcosToml = new BCOSToml(getConfigPath());
        Toml toml = bcosToml.getToml();

        Map<String, Object> accountConfig = toml.toMap();

        Map<String, Object> accountValue = (Map<String, Object>) accountConfig.get("account");
        requireItemNotNull(accountValue, "account", getConfigPath());

        String accountFile = (String) accountValue.get("accountFile");
        requireFieldNotNull(accountFile, "account", "accountFile", getConfigPath());

        String passwd = (String) accountValue.get("password");
        if (accountFile.trim().endsWith("p12")) {
            requireFieldNotNull(accountFile, "account", "password", getConfigPath());
        }

        String type = (String) accountValue.get("type");
        requireFieldNotNull(accountFile, "account", "type", getConfigPath());
        if (!type.equals(BCOSConstant.BCOS3_ECDSA_EVM_STUB_TYPE)
                && !type.equals(BCOSConstant.BCOS3_ECDSA_WASM_STUB_TYPE)
                && !type.equals(BCOSConstant.BCOS3_GM_EVM_STUB_TYPE)
                && !type.equals(BCOSConstant.BCOS3_GM_WASM_STUB_TYPE)) {
            throw new InvalidParameterException(" unrecognized type: " + type);
        }

        BCOSAccountConfig bcosAccountConfig = new BCOSAccountConfig();
        bcosAccountConfig.setAccountFile(accountFile);
        bcosAccountConfig.setPasswd(passwd);
        bcosAccountConfig.setType(type);

        logger.info(" account: {}", bcosAccountConfig);

        return bcosAccountConfig;
    }
}
