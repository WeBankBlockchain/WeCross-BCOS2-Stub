package com.webank.wecross.stub.bcos.custom;

import com.webank.wecross.stub.Account;
import com.webank.wecross.stub.BlockHeaderManager;
import com.webank.wecross.stub.Connection;
import com.webank.wecross.stub.Driver;
import com.webank.wecross.stub.Path;
import com.webank.wecross.stub.TransactionException;
import com.webank.wecross.stub.bcos.AsyncCnsService;
import com.webank.wecross.stub.bcos.BCOSDriver;
import com.webank.wecross.stub.bcos.common.BCOSStatusCode;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegisterCnsHandler implements CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(RegisterCnsHandler.class);

    /** @param args version || address || abi */
    @Override
    public void handle(
            Path path,
            Object[] args,
            Account account,
            BlockHeaderManager blockHeaderManager,
            Connection connection,
            Map<String, String> abiMap,
            Driver.CustomCommandCallback callback) {
        if (Objects.isNull(args) || args.length < 3) {
            callback.onResponse(
                    new TransactionException(
                            BCOSStatusCode.RegisterContractFailed, "incomplete args"),
                    null);
            return;
        }

        String name = path.toString().split("\\.")[2];
        String version = (String) args[0];
        String address = (String) args[1];
        String abi = (String) args[2];

        Driver driver = new BCOSDriver();

        AsyncCnsService asyncCnsService = new AsyncCnsService();
        asyncCnsService.registerCNSByProxy(
                name,
                address,
                version,
                abi,
                account,
                blockHeaderManager,
                connection,
                driver,
                e -> {
                    if (Objects.nonNull(e)) {
                        logger.warn("registering abi failed", e);
                        callback.onResponse(e, null);
                        return;
                    }

                    // store abi
                    abiMap.put(path.toString(), abi);

                    callback.onResponse(null, "success");
                });
    }
}
