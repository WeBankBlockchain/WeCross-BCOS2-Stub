package com.webank.wecross.stub.bcos.contract;

import com.webank.wecross.stub.bcos.common.ExtendedTransactionDecoder;
import org.fisco.bcos.sdk.abi.FunctionEncoder;
import org.fisco.bcos.sdk.abi.datatypes.Function;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.model.CryptoType;
import org.fisco.bcos.sdk.transaction.codec.encode.TransactionEncoderService;
import org.fisco.bcos.sdk.transaction.model.po.RawTransaction;
import org.junit.Test;

import java.io.IOException;
import java.math.BigInteger;

import static junit.framework.TestCase.assertEquals;

public class TransactionSignTest {

    @Test
    public void transactionSignTest() throws IOException {
        CryptoSuite cryptoSuite = new CryptoSuite(CryptoType.ECDSA_TYPE);
        BigInteger blockNumber = BigInteger.valueOf(1111111);

        String funcName = "testFuncName";
        String[] params = new String[]{"aaa", "bbbb", "ccc"};
        FunctionEncoder functionEncoder = new FunctionEncoder(cryptoSuite);
        Function function = FunctionUtility.newDefaultFunction(funcName, params);
        String abiData = functionEncoder.encode(function);

        String to = "0xb3c223fc0bf6646959f254ac4e4a7e355b50a355";
        String extraData = "extraData";

        BigInteger groupID = BigInteger.valueOf(111);
        BigInteger chainID = BigInteger.valueOf(222);

        RawTransaction rawTransaction = SignTransaction.buildTransaction(to, groupID, chainID, blockNumber, abiData);
        CryptoKeyPair credentials = cryptoSuite.getCryptoKeyPair();
        TransactionEncoderService transactionEncoderService = new TransactionEncoderService(cryptoSuite);
        String signTx = transactionEncoderService.encodeAndSign(rawTransaction, credentials);

        RawTransaction decodeExtendedRawTransaction =
                ExtendedTransactionDecoder.decode(signTx);

        assertEquals(SignTransaction.gasPrice, decodeExtendedRawTransaction.getGasPrice());
        assertEquals(SignTransaction.gasLimit, decodeExtendedRawTransaction.getGasLimit());
        assertEquals(to, decodeExtendedRawTransaction.getTo());
        assertEquals(BigInteger.ZERO, decodeExtendedRawTransaction.getValue());
        assertEquals(abiData, "0x" + decodeExtendedRawTransaction.getData());
        assertEquals(groupID, decodeExtendedRawTransaction.getGroupId());
        assertEquals(chainID, decodeExtendedRawTransaction.getFiscoChainId());
    }
}
