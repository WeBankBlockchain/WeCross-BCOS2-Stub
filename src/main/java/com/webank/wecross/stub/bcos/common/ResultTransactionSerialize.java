package com.webank.wecross.stub.bcos.common;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import org.fisco.bcos.sdk.client.protocol.model.JsonTransactionResponse;
import org.fisco.bcos.sdk.client.protocol.response.BcosBlock;

public class ResultTransactionSerialize extends JsonSerializer<BcosBlock.TransactionResult> {

    @Override
    public void serialize(
            BcosBlock.TransactionResult value, JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
        if (value instanceof BcosBlock.TransactionHash) {
            BcosBlock.TransactionHash transactionHash = (BcosBlock.TransactionHash) value;
            gen.writeString(transactionHash.get());
        } else if (value instanceof BcosBlock.TransactionObject) {
            BcosBlock.TransactionObject transactionObject = (BcosBlock.TransactionObject) value;
            JsonTransactionResponse jsonTransactionResponse = new JsonTransactionResponse();
            jsonTransactionResponse.setHash(transactionObject.getHash());
            jsonTransactionResponse.setBlockHash(transactionObject.getBlockHash());
            jsonTransactionResponse.setBlockNumber(transactionObject.getBlockNumber().toString());
            jsonTransactionResponse.setFrom(transactionObject.getFrom());
            jsonTransactionResponse.setGas(transactionObject.getGas());
            jsonTransactionResponse.setInput(transactionObject.getInput());
            jsonTransactionResponse.setNonce(transactionObject.getNonce());
            jsonTransactionResponse.setTo(transactionObject.getTo());
            jsonTransactionResponse.setTransactionIndex(transactionObject.getTransactionIndex());
            jsonTransactionResponse.setValue(transactionObject.getValue());
            jsonTransactionResponse.setGasPrice(transactionObject.getGasPrice());
            jsonTransactionResponse.setBlockLimit(transactionObject.getBlockLimit());
            jsonTransactionResponse.setChainId(transactionObject.getChainId());
            jsonTransactionResponse.setGroupId(transactionObject.getGroupId());
            jsonTransactionResponse.setExtraData(transactionObject.getExtraData());
            gen.writeObject(jsonTransactionResponse);
        }
    }
}
