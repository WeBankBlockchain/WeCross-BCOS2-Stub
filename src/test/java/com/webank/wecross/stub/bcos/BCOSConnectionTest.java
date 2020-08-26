package com.webank.wecross.stub.bcos;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.webank.wecross.stub.*;
import com.webank.wecross.stub.bcos.common.BCOSConstant;
import com.webank.wecross.stub.bcos.common.BCOSRequestType;
import com.webank.wecross.stub.bcos.common.BCOSStatusCode;
import com.webank.wecross.stub.bcos.config.BCOSStubConfig;
import com.webank.wecross.stub.bcos.config.BCOSStubConfigParser;
import com.webank.wecross.stub.bcos.contract.FunctionUtility;
import com.webank.wecross.stub.bcos.contract.SignTransaction;
import com.webank.wecross.stub.bcos.protocol.request.TransactionParams;
import com.webank.wecross.stub.bcos.web3j.Web3jWrapper;
import com.webank.wecross.stub.bcos.web3j.Web3jWrapperCallNotSucStatus;
import com.webank.wecross.stub.bcos.web3j.Web3jWrapperImplMock;
import com.webank.wecross.stub.bcos.web3j.Web3jWrapperWithExceptionMock;
import com.webank.wecross.stub.bcos.web3j.Web3jWrapperWithNullMock;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import org.fisco.bcos.web3j.abi.FunctionEncoder;
import org.fisco.bcos.web3j.abi.datatypes.Function;
import org.fisco.bcos.web3j.crypto.gm.GenCredential;
import org.fisco.bcos.web3j.protocol.ObjectMapperFactory;
import org.fisco.bcos.web3j.protocol.channel.StatusCode;
import org.fisco.bcos.web3j.protocol.core.methods.response.BcosBlock;
import org.fisco.bcos.web3j.protocol.core.methods.response.Call;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.junit.Test;

public class BCOSConnectionTest {
    @Test
    public void toBlockHeaderTest() throws IOException {
        String blockJson =
                "{\"number\":331,\"hash\":\"0x6db416c8ac6b1fe7ed08771de419b71c084ee5969029346806324601f2e3f0d0\",\"parentHash\":\"0xed0ef6826277efbc9601dedc1b6ea20067eed219e415e1038f111155b8fc1e24\",\"nonce\":0,\"sha3Uncles\":null,\"logsBloom\":\"0x00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000\",\"transactionsRoot\":\"0x07009a9d655cee91e95dcd1c53d5917a58f80e6e6ac689bae24bd911d75c471c\",\"stateRoot\":\"0xce8a92c9311e9e0b77842c86adf8fcf91cbab8fb5daefc85b21f501ca8b1f682\",\"receiptsRoot\":\"0x2a4433b7611c4b1fae16b873ced1dec9a65b82416e448f58fded002c05a10082\",\"author\":null,\"sealer\":\"0x1\",\"mixHash\":null,\"extraData\":[],\"gasLimit\":0,\"gasUsed\":0,\"timestamp\":1584081463141,\"transactions\":[{}],\"uncles\":null,\"sealerList\":[\"7f6b1fc98c6bc8dbde4afe62bf1322a4f10ff29528f1e6bb0e57590aa81c31bfe57510787c5adf3fb90fb4239d5483c0d805874451aeb7e76c6c15e1b2123165\",\"9b04ba34f30452a43e7868e1b918c380f1d3d3bdc98d752d1dc30155e6a3dd9da6e530a4351eb4eab42f8703a3922233b830f2678c14179e3ac0f9e5bef8c954\",\"f4c43730a29511e66e9eddbee7024a65d8a8b3b886e6f652785faefb979676f04bd9671529aef9147c86edf58df0482b4e5b293006a179b14039484c6d20a18e\"],\"numberRaw\":\"0x14b\",\"nonceRaw\":null,\"gasLimitRaw\":\"0x0\",\"gasUsedRaw\":\"0x0\",\"timestampRaw\":\"0x170d29ce765\"}";
        ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

        BcosBlock.Block block = objectMapper.readValue(blockJson, BcosBlock.Block.class);

        BCOSConnection connection = new BCOSConnection(null);
        BlockHeader blockHeader = connection.convertToBlockHeader(block);

        assertEquals(blockHeader.getHash(), block.getHash());
        assertEquals(blockHeader.getPrevHash(), block.getParentHash());
        assertEquals(blockHeader.getReceiptRoot(), block.getReceiptsRoot());
        assertEquals(blockHeader.getStateRoot(), block.getStateRoot());
        assertEquals(blockHeader.getNumber(), block.getNumber().intValue());
        assertEquals(blockHeader.getTransactionRoot(), block.getTransactionsRoot());
    }

    @Test
    public void resourceInfoListTest() throws IOException {
        BCOSStubConfigParser bcosStubConfigParser =
                new BCOSStubConfigParser("./", "stub-sample-ut.toml");
        BCOSStubConfig bcosStubConfig = bcosStubConfigParser.loadConfig();

        List<ResourceInfo> resourceInfoList = bcosStubConfig.convertToResourceInfos();

        assertEquals(resourceInfoList.size(), bcosStubConfig.getResources().size());
        assertFalse(resourceInfoList.isEmpty());
        for (int i = 0; i < resourceInfoList.size(); i++) {
            ResourceInfo resourceInfo = resourceInfoList.get(i);
            assertEquals(resourceInfo.getStubType(), bcosStubConfig.getType());
            assertEquals(resourceInfo.getProperties().get(BCOSConstant.BCOS_CHAIN_ID), 123);
            assertEquals(resourceInfo.getProperties().get(BCOSConstant.BCOS_GROUP_ID), 111);
        }
    }

    @Test
    public void handleUnknownTypeTest() throws IOException {
        Web3jWrapper web3jWrapper = new Web3jWrapperImplMock();
        BCOSConnection connection = new BCOSConnection(web3jWrapper);
        Request request = new Request();
        request.setType(2000);
        Response response = connection.send(request);
        assertEquals(response.getErrorCode(), BCOSStatusCode.UnrecognizedRequestType);
    }

    @Test
    public void handleGetBlockNumberTest() {
        Web3jWrapper web3jWrapper = new Web3jWrapperImplMock();
        BCOSConnection connection = new BCOSConnection(web3jWrapper);
        Request request = new Request();
        request.setType(BCOSRequestType.GET_BLOCK_NUMBER);
        connection.asyncSend(
                request,
                response -> {
                    BigInteger blockNumber = new BigInteger(response.getData());
                    assertEquals(response.getErrorCode(), BCOSStatusCode.Success);
                    assertEquals(blockNumber.longValue(), 11111);
                });
    }

    @Test
    public void handleFailedGetBlockNumberTest() {
        Web3jWrapper web3jWrapper = new Web3jWrapperWithExceptionMock();
        BCOSConnection connection = new BCOSConnection(web3jWrapper);
        Request request = new Request();
        request.setType(BCOSRequestType.GET_BLOCK_NUMBER);
        connection.asyncSend(
                request,
                response ->
                        assertEquals(
                                response.getErrorCode(),
                                BCOSStatusCode.HandleGetBlockNumberFailed));
    }

    @Test
    public void handleGetBlockTest() throws IOException {
        BCOSDriver driver = new BCOSDriver();

        Web3jWrapper web3jWrapper = new Web3jWrapperImplMock();
        BCOSConnection connection = new BCOSConnection(web3jWrapper);
        Request request = new Request();
        request.setType(BCOSRequestType.GET_BLOCK_HEADER);

        request.setData(BigInteger.valueOf(11111).toByteArray());

        connection.asyncSend(
                request,
                response -> {
                    assertTrue(Objects.nonNull(connection.getWeb3jWrapper()));
                    assertEquals(response.getErrorCode(), 0);

                    BlockHeader blockHeader = driver.decodeBlockHeader(response.getData());
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
                });
    }

    @Test
    public void handleFailedGetBlockTest() throws IOException {
        Web3jWrapper web3jWrapper = new Web3jWrapperWithExceptionMock();
        BCOSConnection connection = new BCOSConnection(web3jWrapper);
        Request request = new Request();
        request.setType(BCOSRequestType.GET_BLOCK_HEADER);

        request.setData(BigInteger.valueOf(11111).toByteArray());
        connection.asyncSend(
                request,
                response -> {
                    assertTrue(Objects.nonNull(connection.getWeb3jWrapper()));
                    assertEquals(
                            response.getErrorCode(), BCOSStatusCode.HandleGetBlockHeaderFailed);
                });
    }

    @Test
    public void handleCallTest() throws IOException {

        Web3jWrapper web3jWrapper = new Web3jWrapperImplMock();
        BCOSConnection connection = new BCOSConnection(web3jWrapper);

        String address = "0x6db416c8ac6b1fe7ed08771de419b71c084ee5969029346806324601f2e3f0d0";
        String funName = "funcName";
        String[] params = new String[] {"abc", "def", "hig"};
        Function function = FunctionUtility.newDefaultFunction(funName, params);

        String abi = FunctionEncoder.encode(function);

        TransactionRequest transactionRequest = new TransactionRequest(funName, params);
        TransactionParams transactionParams =
                new TransactionParams(transactionRequest, abi, TransactionParams.TP_YPE.CALL);
        transactionParams.setFrom(address);
        transactionParams.setTo(address);

        Request request = new Request();
        request.setType(BCOSRequestType.CALL);
        request.setData(ObjectMapperFactory.getObjectMapper().writeValueAsBytes(transactionParams));
        Response response = connection.send(request);

        assertEquals(response.getErrorCode(), BCOSStatusCode.Success);

        Call.CallOutput callOutput =
                ObjectMapperFactory.getObjectMapper()
                        .readValue(response.getData(), Call.CallOutput.class);
        assertEquals(callOutput.getStatus(), StatusCode.Success);

        String data = callOutput.getOutput();

        String[] strings = FunctionUtility.decodeDefaultOutput(data);
        assertEquals(strings.length, params.length);

        for (int i = 0; i < params.length; i++) {
            assertEquals(strings[i], params[i]);
        }
    }

    @Test
    public void handleFailedCallTest() throws IOException {

        Web3jWrapper web3jWrapper = new Web3jWrapperWithExceptionMock();
        BCOSConnection connection = new BCOSConnection(web3jWrapper);

        String address = "0x6db416c8ac6b1fe7ed08771de419b71c084ee5969029346806324601f2e3f0d0";
        String funName = "funcName";
        String[] params = new String[] {"abc", "def", "hig"};
        Function function = FunctionUtility.newDefaultFunction(funName, params);

        String abi = FunctionEncoder.encode(function);

        TransactionRequest transactionRequest = new TransactionRequest(funName, params);
        TransactionParams transactionParams =
                new TransactionParams(transactionRequest, abi, TransactionParams.TP_YPE.CALL);
        transactionParams.setTo(address);
        transactionParams.setFrom(address);

        Request request = new Request();
        request.setType(BCOSRequestType.CALL);
        request.setData(ObjectMapperFactory.getObjectMapper().writeValueAsBytes(transactionParams));
        Response response = connection.send(request);

        assertEquals(response.getErrorCode(), BCOSStatusCode.HandleCallRequestFailed);
    }

    @Test
    public void handleFailedCallTest0() throws IOException {

        Web3jWrapper web3jWrapper = new Web3jWrapperCallNotSucStatus();
        BCOSConnection connection = new BCOSConnection(web3jWrapper);

        String address = "0x6db416c8ac6b1fe7ed08771de419b71c084ee5969029346806324601f2e3f0d0";
        String funName = "funcName";
        String[] params = new String[] {"abc", "def", "hig"};
        Function function = FunctionUtility.newDefaultFunction(funName, params);

        String abi = FunctionEncoder.encode(function);

        TransactionRequest transactionRequest = new TransactionRequest(funName, params);
        TransactionParams transactionParams =
                new TransactionParams(transactionRequest, abi, TransactionParams.TP_YPE.CALL);
        transactionParams.setTo(address);
        transactionParams.setFrom(address);

        Request request = new Request();
        request.setType(BCOSRequestType.CALL);
        request.setData(ObjectMapperFactory.getObjectMapper().writeValueAsBytes(transactionParams));
        Response response = connection.send(request);

        assertEquals(response.getErrorCode(), BCOSStatusCode.Success);
    }

    @Test
    public void handleSendTransactionTest() throws IOException {

        Web3jWrapper web3jWrapper = new Web3jWrapperImplMock();
        BCOSConnection connection = new BCOSConnection(web3jWrapper);
        Request request = new Request();
        request.setType(BCOSRequestType.SEND_TRANSACTION);

        String address = "0x6db416c8ac6b1fe7ed08771de419b71c084ee5969029346806324601f2e3f0d0";
        String funName = "funcName";
        String[] params = new String[] {"abc", "def", "hig"};

        Function function = FunctionUtility.newDefaultFunction(funName, params);

        String abi = FunctionEncoder.encode(function);

        String sign =
                SignTransaction.sign(
                        GenCredential.create(),
                        address,
                        BigInteger.valueOf(1),
                        BigInteger.valueOf(1),
                        BigInteger.valueOf(1),
                        abi);

        TransactionRequest transactionRequest = new TransactionRequest();
        transactionRequest.setMethod(funName);
        transactionRequest.setArgs(params);
        TransactionParams transaction1 =
                new TransactionParams(transactionRequest, sign, TransactionParams.TP_YPE.SEND_TX);

        request.setData(ObjectMapperFactory.getObjectMapper().writeValueAsBytes(transaction1));

        Response response = connection.send(request);

        assertEquals(response.getErrorCode(), BCOSStatusCode.Success);
        TransactionReceipt transactionReceipt =
                ObjectMapperFactory.getObjectMapper()
                        .readValue(response.getData(), TransactionReceipt.class);

        assertEquals(transactionReceipt.getBlockNumber().longValue(), 9);
        assertEquals(
                transactionReceipt.getTransactionHash(),
                "0x8b3946912d1133f9fb0722a7b607db2456d468386c2e86b035e81ef91d94eb90");
        assertFalse(transactionReceipt.getTxProof().isEmpty());
        assertFalse(transactionReceipt.getReceiptProof().isEmpty());
    }

    @Test
    public void handleFailedSendTransactionTest() throws IOException {

        Web3jWrapper web3jWrapper = new Web3jWrapperWithExceptionMock();
        BCOSConnection connection = new BCOSConnection(web3jWrapper);
        Request request = new Request();
        request.setType(BCOSRequestType.SEND_TRANSACTION);

        String address = "0x6db416c8ac6b1fe7ed08771de419b71c084ee5969029346806324601f2e3f0d0";
        String funName = "funcName";
        String[] params = new String[] {"abc", "def", "hig"};

        Function function = FunctionUtility.newDefaultFunction(funName, params);

        String abi = FunctionEncoder.encode(function);

        String sign =
                SignTransaction.sign(
                        GenCredential.create(),
                        address,
                        BigInteger.valueOf(1),
                        BigInteger.valueOf(1),
                        BigInteger.valueOf(1),
                        abi);

        TransactionRequest transactionRequest = new TransactionRequest();
        transactionRequest.setMethod(funName);
        transactionRequest.setArgs(params);
        TransactionParams transaction1 =
                new TransactionParams(transactionRequest, sign, TransactionParams.TP_YPE.SEND_TX);

        request.setData(ObjectMapperFactory.getObjectMapper().writeValueAsBytes(transaction1));

        Response response = connection.send(request);

        assertEquals(response.getErrorCode(), BCOSStatusCode.HandleSendTransactionFailed);
    }

    @Test
    public void handleFailedSendTransactionTest0() throws IOException {

        Web3jWrapper web3jWrapper = new Web3jWrapperWithNullMock();
        BCOSConnection connection = new BCOSConnection(web3jWrapper);
        Request request = new Request();
        request.setType(BCOSRequestType.SEND_TRANSACTION);

        String address = "0x6db416c8ac6b1fe7ed08771de419b71c084ee5969029346806324601f2e3f0d0";
        String funName = "funcName";
        String[] params = new String[] {"abc", "def", "hig"};

        Function function = FunctionUtility.newDefaultFunction(funName, params);

        String abi = FunctionEncoder.encode(function);

        String sign =
                SignTransaction.sign(
                        GenCredential.create(),
                        address,
                        BigInteger.valueOf(1),
                        BigInteger.valueOf(1),
                        BigInteger.valueOf(1),
                        abi);

        TransactionRequest transactionRequest = new TransactionRequest();
        transactionRequest.setMethod(funName);
        transactionRequest.setArgs(params);
        TransactionParams transaction1 =
                new TransactionParams(transactionRequest, sign, TransactionParams.TP_YPE.SEND_TX);

        request.setData(ObjectMapperFactory.getObjectMapper().writeValueAsBytes(transaction1));

        Response response = connection.send(request);

        assertEquals(response.getErrorCode(), BCOSStatusCode.TransactionReceiptNotExist);
    }

    @Test
    public void handleFailedGetTransactionProofTest() throws IOException {
        String hash = "0x633a3386a189455354c058af6606d705697f3b216ad555958dc680f68cc4e99d";
        Request request = new Request();
        request.setType(BCOSRequestType.GET_TRANSACTION_PROOF);
        request.setData(hash.getBytes(StandardCharsets.UTF_8));

        Web3jWrapper web3jWrapper = new Web3jWrapperWithExceptionMock();
        BCOSConnection connection = new BCOSConnection(web3jWrapper);
        Response response = connection.send(request);

        assertEquals(response.getErrorCode(), BCOSStatusCode.UnrecognizedRequestType);
    }
}
