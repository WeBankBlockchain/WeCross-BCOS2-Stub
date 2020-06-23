package com.webank.wecross.stub.bcos.custom;

import com.webank.wecross.stub.*;
import com.webank.wecross.stub.bcos.AsyncCnsService;
import com.webank.wecross.stub.bcos.common.*;
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

        AsyncCnsService asyncCnsService = new AsyncCnsService();
        asyncCnsService.insert(
                name,
                address,
                version,
                abi,
                account,
                blockHeaderManager,
                connection,
                exception -> {
                    if (Objects.nonNull(exception)) {
                        logger.warn("registering abi failed", exception);
                        callback.onResponse(exception, null);
                        return;
                    }

                    // store abi
                    abiMap.put(path.toString(), abi);

                    callback.onResponse(null, "success");
                });
    }
}
