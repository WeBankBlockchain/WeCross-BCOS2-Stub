package com.webank.wecross.stub.bcos;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.webank.wecross.stub.BlockHeader;
import com.webank.wecross.stub.Connection;
import com.webank.wecross.stub.Request;
import com.webank.wecross.stub.ResourceInfo;
import com.webank.wecross.stub.Response;
import com.webank.wecross.stub.bcos.common.BCOSConstant;
import com.webank.wecross.stub.bcos.common.BCOSRequestType;
import com.webank.wecross.stub.bcos.config.BCOSStubConfig;
import com.webank.wecross.stub.bcos.web3j.Web3jWrapper;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.fisco.bcos.web3j.protocol.ObjectMapperFactory;
import org.fisco.bcos.web3j.protocol.core.methods.response.BcosBlock;
import org.fisco.bcos.web3j.protocol.core.methods.response.Call;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BCOSConnection implements Connection {

    private static final Logger logger = LoggerFactory.getLogger(BCOSConnection.class);

    private ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();

    private List<ResourceInfo> resourceInfoList;

    private final Web3jWrapper web3jWrapper;

    public BCOSConnection(Web3jWrapper web3jWrapper) {
        this.web3jWrapper = web3jWrapper;
        this.objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }

    public List<ResourceInfo> getResourceInfoList() {
        return resourceInfoList;
    }

    public void setResourceInfoList(List<ResourceInfo> resourceInfoList) {
        this.resourceInfoList = resourceInfoList;
    }

    public Web3jWrapper getWeb3jWrapper() {
        return web3jWrapper;
    }

    @Override
    public List<ResourceInfo> getResources() {
        return resourceInfoList;
    }

    public List<ResourceInfo> getResourceInfoList(List<BCOSStubConfig.Resource> resourceList) {
        List<ResourceInfo> resourceInfos = new ArrayList<>();
        org.fisco.bcos.web3j.crypto.SHA3Digest sha3Digest =
                new org.fisco.bcos.web3j.crypto.SHA3Digest();
        for (int i = 0; i < resourceList.size(); ++i) {
            ResourceInfo resourceInfo = new ResourceInfo();
            BCOSStubConfig.Resource resource = resourceList.get(i);

            resourceInfo.setName(resource.getName());
            resourceInfo.setStubType(BCOSConstant.BCOS_ACCOUNT);
            resourceInfo.setChecksum(sha3Digest.hash(resource.getValue()));

            resourceInfo.getProperties().put(resource.getName(), resource.getValue());
            resourceInfo
                    .getProperties()
                    .put(
                            BCOSConstant.BCOS_RESOURCEINFO_GROUP_ID,
                            resourceList.get(i).getChain().getGroupID());
            resourceInfo
                    .getProperties()
                    .put(
                            BCOSConstant.BCOS_RESOURCEINFO_CHAIN_ID,
                            resourceList.get(i).getChain().getChainID());

            resourceInfos.add(resourceInfo);
        }

        logger.info(" resource list: {}", resourceInfos);

        return resourceInfos;
    }

    @Override
    public Response send(Request request) {
        switch (request.getType()) {
            case BCOSRequestType.CALL:
                return handleCallRequest(request);
            case BCOSRequestType.SEND_TRANSACTION:
                return handleTransactionRequest(request);
            case BCOSRequestType.GET_BLOCK_HEADER:
                return handleGetBlockHeaderRequest(request);
            case BCOSRequestType.GET_BLOCK_NUMBER:
                return handleGetBlockNumberRequest(request);
            case BCOSRequestType.GET_TRANSACTION_RECEIPT:
                return handleGetTransactionReceipt(request);
            default:
                logger.warn(" unrecognized request type, type: {}", request.getType());
                Response response = new Response();
                response.setErrorCode(-1);
                response.setErrorMessage(" unrecognized request type, type: " + request.getType());
                return response;
        }
    }

    public Response handleCallRequest(Request request) {
        Response response = new Response();
        try {
            String params = new String(request.getData(), StandardCharsets.UTF_8);
            String[] split = params.split(",");

            Call.CallOutput callOutput = web3jWrapper.call(split[0], split[1]);

            logger.debug(
                    " contractAddress: {}, abi: {}, status: {}, blk: {}, output: {}",
                    split[0],
                    split[1],
                    callOutput.getStatus(),
                    callOutput.getCurrentBlockNumber(),
                    callOutput.getOutput());

            response.setErrorCode(0);
            response.setErrorMessage("success");
            response.setData(objectMapper.writeValueAsBytes(callOutput));
        } catch (Exception e) {
            logger.warn(" Exception, e: {}", e);
            response.setErrorCode(-1);
            response.setErrorMessage(" errorMessage: " + e.getMessage());
        }
        return response;
    }

    public Response handleTransactionRequest(Request request) {
        Response response = new Response();
        try {
            String signTx = new String(request.getData(), StandardCharsets.UTF_8);

            logger.trace(" signTx: {}", signTx);

            TransactionReceipt receipt = web3jWrapper.sendTransaction(signTx);

            response.setErrorCode(0);
            response.setErrorMessage("success");

            response.setData(objectMapper.writeValueAsBytes(receipt));
            logger.debug(" sendTransaction, receipt: {}", receipt);

        } catch (Exception e) {
            logger.warn(" Exception, request type: {}, e: {}", request.getType(), e);
            response.setErrorCode(-1);
            response.setErrorMessage(" errorMessage: " + e.getMessage());
        }
        return response;
    }

    public Response handleGetBlockNumberRequest(Request request) {
        Response response = new Response();
        try {
            BigInteger blockNumber = web3jWrapper.getBlockNumber();

            response.setErrorCode(0);
            response.setErrorMessage("success");
            response.setData(blockNumber.toByteArray());
            logger.debug(" blockNumber: {}", blockNumber);

        } catch (Exception e) {
            logger.warn(" Exception, e: {}", e);
            response.setErrorCode(-1);
            response.setErrorMessage(" errorMessage: " + e.getMessage());
        }
        return response;
    }

    public Response handleGetTransactionReceipt(Request request) {
        Response response = new Response();
        try {
            String txHash = new String(request.getData(), StandardCharsets.UTF_8);

            TransactionReceipt receipt = web3jWrapper.getTransactionReceipt(txHash);

            if (Objects.isNull(receipt)
                    || Objects.isNull(receipt.getTransactionHash())
                    || "".equals(receipt.getTransactionHash())) {
                throw new RuntimeException(" Not found transaction receipt, tx hash: " + txHash);
            }

            response.setErrorCode(0);
            response.setErrorMessage("success");

            response.setData(objectMapper.writeValueAsBytes(receipt));
            logger.debug(" getTransactionReceipt, tx hash: {}, receipt: {}", txHash, receipt);

        } catch (Exception e) {
            logger.warn(" Exception, e: {}", e);
            response.setErrorCode(-1);
            response.setErrorMessage(" errorMessage: " + e.getMessage());
        }

        return response;
    }

    /**
     * convert Block to BlockHeader
     *
     * @param block
     * @return
     */
    public BlockHeader toBlockHeader(BcosBlock.Block block) {
        BlockHeader blockHeader = new BlockHeader();
        blockHeader.setHash(block.getHash());
        blockHeader.setPrevHash(block.getParentHash());
        blockHeader.setNumber(block.getNumber().longValue());
        blockHeader.setReceiptRoot(block.getReceiptsRoot());
        blockHeader.setStateRoot(block.getStateRoot());
        blockHeader.setTransactionRoot(block.getTransactionsRoot());
        return blockHeader;
    }

    public Response handleGetBlockHeaderRequest(Request request) {
        Response response = new Response();
        try {
            BigInteger blockNumber = new BigInteger(request.getData());
            BcosBlock.Block block = web3jWrapper.getBlockByNumber(blockNumber.longValue());

            response.setErrorCode(0);
            response.setErrorMessage("success");
            response.setData(objectMapper.writeValueAsBytes(toBlockHeader(block)));
            logger.debug(" getBlockByNumber, blk: {}, block: {}", blockNumber, block);
        } catch (Exception e) {
            logger.warn(" Exception, e: {}", e);
            response.setErrorCode(-1);
            response.setErrorMessage(" errorMessage: " + e.getMessage());
        }
        return response;
    }
}
