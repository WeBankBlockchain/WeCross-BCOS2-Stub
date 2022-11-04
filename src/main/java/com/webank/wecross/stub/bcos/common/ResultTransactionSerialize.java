package com.webank.wecross.stub.bcos.common;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import org.fisco.bcos.sdk.v3.client.protocol.response.BcosBlock;

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
            gen.writeObject(transactionObject.get());
        }
    }
}
