package com.webank.wecross.stub.bcos.common;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.fisco.bcos.sdk.v3.client.protocol.response.BcosBlock;

public class ObjectMapperFactory {
    private static final ObjectMapper DEFAULT_OBJECT_MAPPER = getObjectMapper();

    static {
        configureObjectMapper(DEFAULT_OBJECT_MAPPER);
    }

    public static ObjectMapper getObjectMapper() {
        return configureObjectMapper(new ObjectMapper());
    }

    public static ObjectReader getObjectReader() {
        return DEFAULT_OBJECT_MAPPER.reader();
    }

    private static ObjectMapper configureObjectMapper(ObjectMapper objectMapper) {

        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(
                BcosBlock.TransactionResult.class, new ResultTransactionSerialize());
        objectMapper.registerModule(simpleModule);

        return objectMapper;
    }
}
