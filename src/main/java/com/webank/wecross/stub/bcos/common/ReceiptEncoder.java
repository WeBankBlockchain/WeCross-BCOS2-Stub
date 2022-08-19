package com.webank.wecross.stub.bcos.common;

import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.rlp.RlpEncoder;
import org.fisco.bcos.sdk.rlp.RlpList;
import org.fisco.bcos.sdk.rlp.RlpString;
import org.fisco.bcos.sdk.rlp.RlpType;
import org.fisco.bcos.sdk.utils.Numeric;

import java.util.ArrayList;
import java.util.List;


public class ReceiptEncoder {
    public static String encode(TransactionReceipt transactionReceipt, EnumNodeVersion.Version version) {
        List<RlpType> values = asRlpValues(transactionReceipt, version);
        RlpList rlpList = new RlpList(values);
        byte[] rlpBytes = RlpEncoder.encode(rlpList);
        return Numeric.toHexString(rlpBytes);
    }

    private static List<RlpType> asRlpValues(TransactionReceipt transactionReceipt, EnumNodeVersion.Version version) {
        List<RlpType> result = new ArrayList<>();
        // bytes
        result.add(RlpString.create(Numeric.hexStringToByteArray(transactionReceipt.getRoot())));

        // BigInteger
        result.add(RlpString.create(Numeric.toBigInt(transactionReceipt.getGasUsed())));

        result.add(
                RlpString.create(
                        Numeric.hexStringToByteArray(transactionReceipt.getContractAddress())));

        result.add(
                RlpString.create(Numeric.hexStringToByteArray(transactionReceipt.getLogsBloom())));

        result.add(RlpString.create(Numeric.toBigInt(transactionReceipt.getStatus())));

        result.add(RlpString.create(Numeric.hexStringToByteArray(transactionReceipt.getOutput())));
        // gas used
        if (version != null && version.getMinor() >= 9) {
            result.add(RlpString.create(Numeric.toBigInt(transactionReceipt.getRemainGas())));
        }
        // List
        List<TransactionReceipt.Logs> logs = transactionReceipt.getLogs();
        List<RlpType> logList = new ArrayList<>();
        for (TransactionReceipt.Logs log : logs) {
            List<RlpType> logUnit = new ArrayList<>();
            logUnit.add(RlpString.create(Numeric.hexStringToByteArray(log.getAddress())));

            List<String> topics = log.getTopics();
            List<RlpType> topicList = new ArrayList<>();
            for (String topic : topics) {
                topicList.add(RlpString.create(Numeric.hexStringToByteArray(topic)));
            }
            RlpList topicRlpList = new RlpList(topicList);
            logUnit.add(topicRlpList);
            logUnit.add(RlpString.create(Numeric.hexStringToByteArray(log.getData())));
            logList.add(new RlpList(logUnit));
        }
        RlpList logRlpList = new RlpList(logList);
        result.add(logRlpList);
        return result;
    }
}
