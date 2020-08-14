package com.webank.wecross.stub.bcos.contract;

import static junit.framework.TestCase.assertEquals;

import java.io.IOException;
import java.math.BigInteger;
import org.fisco.bcos.web3j.abi.FunctionEncoder;
import org.fisco.bcos.web3j.abi.datatypes.Function;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.ExtendedRawTransaction;
import org.fisco.bcos.web3j.crypto.ExtendedTransactionDecoder;
import org.fisco.bcos.web3j.crypto.gm.GenCredential;
import org.junit.Test;

public class TransactionSignTest {

    @Test
    public void transactionSignTest() throws IOException {
        Credentials credentials = GenCredential.create();
        BigInteger blockNumber = BigInteger.valueOf(1111111);

        String funcName = "testFuncName";
        String[] params = new String[] {"aaa", "bbbb", "ccc"};
        Function function = FunctionUtility.newDefaultFunction(funcName, params);
        String abiData = FunctionEncoder.encode(function);

        String to = "0xb3c223fc0bf6646959f254ac4e4a7e355b50a355";
        String extraData = "extraData";

        BigInteger groupID = BigInteger.valueOf(111);
        BigInteger chainID = BigInteger.valueOf(222);

        String sign = SignTransaction.sign(credentials, to, groupID, chainID, blockNumber, abiData);
        ExtendedRawTransaction decodeExtendedRawTransaction =
                ExtendedTransactionDecoder.decode(sign);

        assertEquals(SignTransaction.gasPrice, decodeExtendedRawTransaction.getGasPrice());
        assertEquals(SignTransaction.gasLimit, decodeExtendedRawTransaction.getGasLimit());
        assertEquals(to, decodeExtendedRawTransaction.getTo());
        assertEquals(BigInteger.ZERO, decodeExtendedRawTransaction.getValue());
        assertEquals(abiData, "0x" + decodeExtendedRawTransaction.getData());
        assertEquals(groupID, decodeExtendedRawTransaction.getGroupId());
        assertEquals(chainID, decodeExtendedRawTransaction.getFiscoChainId());
    }
}
