package com.webank.wecross.stub.bcos3.config;

import java.security.InvalidParameterException;
import java.util.Objects;

public abstract class AbstractBCOSConfigParser {

    public AbstractBCOSConfigParser(String configPath) {
        this.configPath = configPath;
    }

    private final String configPath;

    public String getConfigPath() {
        return configPath;
    }

    public void requireItemNotNull(Object object, String item, String configFile) {
        if (Objects.isNull(object)) {
            throw new InvalidParameterException(
                    item + " item not found, please check config file: " + configFile);
        }
    }

    public void requireFieldNotNull(Object object, String item, String field, String configFile) {
        if (Objects.isNull(object)) {
            throw new InvalidParameterException(
                    "["
                            + item
                            + "]:"
                            + field
                            + " field not found, please check config file: "
                            + configFile);
        }
    }
}
