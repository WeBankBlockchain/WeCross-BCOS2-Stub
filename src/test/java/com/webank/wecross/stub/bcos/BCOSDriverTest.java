package com.webank.wecross.stub.bcos;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

import com.webank.wecross.stub.Account;
import com.webank.wecross.stub.BlockHeader;
import com.webank.wecross.stub.BlockHeaderManager;
import com.webank.wecross.stub.Connection;
import com.webank.wecross.stub.Driver;
import com.webank.wecross.stub.Request;
import com.webank.wecross.stub.ResourceInfo;
import com.webank.wecross.stub.TransactionContext;
import com.webank.wecross.stub.TransactionRequest;
import com.webank.wecross.stub.TransactionResponse;
import com.webank.wecross.stub.VerifiedTransaction;
import com.webank.wecross.stub.bcos.account.BCOSAccountFactory;
import com.webank.wecross.stub.bcos.common.BCOSRequestType;
import com.webank.wecross.stub.bcos.web3j.Web3jWrapperFaildMock;
import com.webank.wecross.stub.bcos.web3j.Web3jWrapperImplMock;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.junit.Before;
import org.junit.Test;

public class BCOSDriverTest {

    private Driver driver = null;
    private Account account = null;
    private Connection connection = null;
    private Connection failedConnection = null;
    private ResourceInfo resourceInfo = null;
    private BlockHeaderManager blockHeaderManager = null;

    public TransactionContext<TransactionRequest> createTransactionRequestContext(
            String method, List<String> args) {
        TransactionRequest transactionRequest =
                new TransactionRequest(method, args.toArray(new String[0]));
        TransactionContext<TransactionRequest> requestTransactionContext =
                new TransactionContext<TransactionRequest>(
                        transactionRequest, account, resourceInfo, blockHeaderManager);
        requestTransactionContext.setAccount(account);
        requestTransactionContext.setBlockHeaderManager(blockHeaderManager);
        requestTransactionContext.setData(transactionRequest);
        requestTransactionContext.setResourceInfo(resourceInfo);
        return requestTransactionContext;
    }

    @Before
    public void initializer() throws Exception {

        BCOSStubFactory bcosStubFactory = new BCOSStubFactory();
        driver = bcosStubFactory.newDriver();
        account = BCOSAccountFactory.build("bcos", "classpath:/accounts/bcos");
        connection = BCOSConnectionFactory.build("stub-sample-ut.toml", new Web3jWrapperImplMock());
        failedConnection =
                BCOSConnectionFactory.build("stub-sample-ut.toml", new Web3jWrapperFaildMock());
        blockHeaderManager = new BlockHeaderManagerImplMock(new Web3jWrapperImplMock());
        resourceInfo = ((BCOSConnection) connection).getResourceInfoList().get(0);
    }

    @Test
    public void decodeTransactionRequestTest() throws IOException {
        assertTrue(Objects.isNull(driver.decodeTransactionRequest(null)));
    }

    @Test
    public void isTransactionTest() throws IOException {
        Request request = new Request();
        request.setData(new byte[0]);

        request.setType(BCOSRequestType.CALL);
        assertTrue(driver.isTransaction(request));
        request.setType(BCOSRequestType.SEND_TRANSACTION);
        assertTrue(driver.isTransaction(request));

        request.setType(BCOSRequestType.GET_BLOCK_NUMBER);
        assertFalse(driver.isTransaction(request));
        request.setType(BCOSRequestType.GET_BLOCK_HEADER);
        assertFalse(driver.isTransaction(request));

        request.setType(BCOSRequestType.GET_TRANSACTION_RECEIPT);
        assertFalse(driver.isTransaction(request));

        request.setType(11111);
        assertFalse(driver.isTransaction(request));
    }

    @Test
    public void decodeBlockHeaderTest() throws IOException {
        assertTrue(Objects.isNull(driver.decodeBlockHeader(new byte[0])));
    }

    @Test
    public void getBlockNumberTest() {
        Request request = new Request();
        request.setType(BCOSRequestType.GET_BLOCK_NUMBER);

        long blockNumber = driver.getBlockNumber(connection);

        assertEquals(blockNumber, 11111);
    }

    @Test
    public void getBlockNumberFailedTest() {
        Request request = new Request();
        request.setType(BCOSRequestType.GET_BLOCK_NUMBER);

        long blockNumber = driver.getBlockNumber(failedConnection);

        assertTrue(blockNumber < 0);
    }

    @Test
    public void getBlockHeaderTest() throws IOException {

        Request request = new Request();
        request.setType(BCOSRequestType.GET_BLOCK_HEADER);
        request.setData(BigInteger.valueOf(11111).toByteArray());

        byte[] blockHeader1 = driver.getBlockHeader(1111, connection);
        assertTrue(Objects.nonNull(blockHeader1));

        BlockHeader blockHeader = driver.decodeBlockHeader(blockHeader1);
        assertEquals(
                blockHeader.getHash(),
                "0x6db416c8ac6b1fe7ed08771de419b71c084ee5969029346806324601f2e3f0d0");
        assertEquals(
                blockHeader.getPrevHash(),
                "0xed0ef6826277efbc9601dedc1b6ea20067eed219e415e1038f111155b8fc1e24");
        assertEquals(
                blockHeader.getReceiptRoot(),
                "0x2a4433b7611c4b1fae16b873ced1dec9a65b82416e448f58fded002c05a10082");
        assertEquals(
                blockHeader.getStateRoot(),
                "0xce8a92c9311e9e0b77842c86adf8fcf91cbab8fb5daefc85b21f501ca8b1f682");
        assertEquals(blockHeader.getNumber(), 331);
        assertEquals(
                blockHeader.getTransactionRoot(),
                "0x07009a9d655cee91e95dcd1c53d5917a58f80e6e6ac689bae24bd911d75c471c");
    }

    @Test
    public void getBlockHeaderFailedTest() {

        Request request = new Request();
        request.setType(BCOSRequestType.GET_BLOCK_HEADER);
        request.setData(BigInteger.valueOf(11111).toByteArray());

        byte[] blockHeader1 = driver.getBlockHeader(1111, failedConnection);
        assertTrue(Objects.isNull(blockHeader1));
    }

    @Test
    public void callTest() throws IOException {

        Request request = new Request();
        request.setType(BCOSRequestType.CALL);

        String address = "0x6db416c8ac6b1fe7ed08771de419b71c084ee5969029346806324601f2e3f0d0";
        String funName = "funcName";
        List<String> params = Arrays.asList("abc", "def", "hig", "xxxxx");

        TransactionContext<TransactionRequest> requestTransactionContext =
                createTransactionRequestContext(funName, params);
        TransactionResponse transactionResponse =
                driver.call(requestTransactionContext, connection);

        assertTrue(transactionResponse.getErrorCode() == 0);
        assertTrue(transactionResponse.getResult().length == params.size());

        for (int i = 0; i < params.size(); ++i) {
            assertEquals(params.get(i), transactionResponse.getResult()[i]);
        }
    }

    @Test
    public void callFailedTest() throws IOException {

        Request request = new Request();
        request.setType(BCOSRequestType.CALL);

        String address = "0x6db416c8ac6b1fe7ed08771de419b71c084ee5969029346806324601f2e3f0d0";
        String funName = "funcName";
        List<String> params = Arrays.asList("abc", "def", "hig", "xxxxx");

        TransactionContext<TransactionRequest> requestTransactionContext =
                createTransactionRequestContext(funName, params);
        TransactionResponse transactionResponse =
                driver.call(requestTransactionContext, failedConnection);

        assertTrue(transactionResponse.getErrorCode() < 0);
    }

    @Test
    public void sendTransactionTest() throws IOException {

        Request request = new Request();
        request.setType(BCOSRequestType.SEND_TRANSACTION);

        String address = "0x6db416c8ac6b1fe7ed08771de419b71c084ee5969029346806324601f2e3f0d0";
        String funName = "funcName";
        List<String> params = Arrays.asList("abc", "def", "hig", "xxxxx");

        TransactionContext<TransactionRequest> requestTransactionContext =
                createTransactionRequestContext(funName, params);
        TransactionResponse transactionResponse =
                driver.sendTransaction(requestTransactionContext, connection);

        assertTrue(transactionResponse.getErrorCode() == 0);
        assertTrue(
                transactionResponse
                        .getHash()
                        .equals(
                                "0xcd0ec220b00a97115e367749be2dedec848236781f6a242a3ffa1d956dbf8ec5"));
        // assertTrue(transactionResponse.getResult().length == params.size());

        //        for (int i = 0; i < params.size(); ++i) {
        //            assertEquals(params.get(i), transactionResponse.getResult()[i]);
        //        }
    }

    @Test
    public void sendTransactionFailedTest() throws IOException {

        Request request = new Request();
        request.setType(BCOSRequestType.SEND_TRANSACTION);

        String address = "0x6db416c8ac6b1fe7ed08771de419b71c084ee5969029346806324601f2e3f0d0";
        String funName = "funcName";
        List<String> params = Arrays.asList("abc", "def", "hig", "xxxxx");

        TransactionContext<TransactionRequest> requestTransactionContext =
                createTransactionRequestContext(funName, params);
        TransactionResponse transactionResponse =
                driver.sendTransaction(requestTransactionContext, failedConnection);

        assertTrue(transactionResponse.getErrorCode() < 0);
    }

    @Test
    public void getTransactionReceiptTest() throws IOException {

        Request request = new Request();
        request.setType(BCOSRequestType.GET_TRANSACTION_RECEIPT);

        VerifiedTransaction verifiedTransaction =
                driver.getVerifiedTransaction(
                        "0xcd0ec220b00a97115e367749be2dedec848236781f6a242a3ffa1d956dbf8ec5",
                        1,
                        blockHeaderManager,
                        connection);
    }

    @Test
    public void getTransactionReceiptFailedTest() throws IOException {

        Request request = new Request();
        request.setType(BCOSRequestType.GET_TRANSACTION_RECEIPT);

        VerifiedTransaction verifiedTransaction =
                driver.getVerifiedTransaction(
                        "0xcd0ec220b00a97115e367749be2dedec848236781f6a242a3ffa1d956dbf8ec5",
                        1,
                        blockHeaderManager,
                        failedConnection);
    }
}
