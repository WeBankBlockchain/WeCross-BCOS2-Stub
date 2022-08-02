package com.webank.wecross.stub.bcos.common;

import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.rlp.RlpEncoder;
import org.fisco.bcos.sdk.rlp.RlpList;
import org.fisco.bcos.sdk.rlp.RlpString;
import org.fisco.bcos.sdk.rlp.RlpType;
import org.fisco.bcos.sdk.utils.Numeric;

import java.util.ArrayList;
import java.util.List;

/**
 * @projectName: bcos2-stub
 * @package: com.webank.wecross.stub.bcos.common
 * @className: ReceiptEncoder
 * @author: lbhan2
 * @description: ReceiptEncoder
 * @date: 2022/8/2 16:43
 * @Copyright: 2021 www.iflytek.com Inc. All rights reserved.
 * 注意：本内容仅限于科大讯飞股份有限公司内部传阅，禁止外泄以及用于其他的商业目的
 */
public class ReceiptEncoder {
    public static String encode(TransactionReceipt transactionReceipt,EnumNodeVersion.Version version) {
        List<RlpType> values = asRlpValues(transactionReceipt,version);
        RlpList rlpList = new RlpList(values);
        byte[] rlpBytes = RlpEncoder.encode(rlpList);
        return Numeric.toHexString(rlpBytes);
    }

    private static List<RlpType> asRlpValues(TransactionReceipt transactionReceipt ,EnumNodeVersion.Version version) {
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
