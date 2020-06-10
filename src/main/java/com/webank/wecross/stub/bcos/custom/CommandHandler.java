package com.webank.wecross.stub.bcos.custom;

import com.webank.wecross.stub.*;
import java.util.Map;

public interface CommandHandler {
    /**
     * handle custom command
     *
     * @param path rule id
     * @param args command args
     * @param account if needs to sign
     * @param blockHeaderManager if needs to verify transaction
     * @param connection chain connection
     * @param abiMap store abi
     * @param callback
     */
    void handle(
            Path path,
            Object[] args,
            Account account,
            BlockHeaderManager blockHeaderManager,
            Connection connection,
            Map<String, String> abiMap,
            Driver.CustomCommandCallback callback);
}
