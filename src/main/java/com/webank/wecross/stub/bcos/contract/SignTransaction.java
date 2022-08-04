package com.webank.wecross.stub.bcos.contract;

import java.math.BigInteger;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.service.GroupManagerService;
import org.fisco.bcos.sdk.transaction.model.po.RawTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SignTransaction {

    private static final Logger logger = LoggerFactory.getLogger(SignTransaction.class);

    public static final BigInteger gasPrice = BigInteger.valueOf(300000000000L);
    public static final BigInteger gasLimit = BigInteger.valueOf(300000000000L);

    public static RawTransaction buildTransaction(
            String contractAddress,
            BigInteger groupId,
            BigInteger chainId,
            BigInteger blockNumber,
            String abi) {
        Random r = ThreadLocalRandom.current();
        BigInteger randomid = new BigInteger(250, r);
        BigInteger blockLimit = blockNumber.add(GroupManagerService.BLOCK_LIMIT);

        RawTransaction rawTransaction =
                RawTransaction.createTransaction(
                        randomid,
                        SignTransaction.gasPrice,
                        SignTransaction.gasLimit,
                        blockLimit,
                        contractAddress,
                        BigInteger.ZERO,
                        abi,
                        chainId,
                        groupId,
                        "");

        return rawTransaction;
    }
}
