package com.webank.wecross.stub.bcos.client;

import org.fisco.bcos.sdk.v3.client.protocol.response.Call;
import org.fisco.bcos.sdk.v3.utils.Hex;

public class ClientWrapperCallNotSucStatus extends ClientWrapperImplMock {
    @Override
    public Call.CallOutput call(String accountAddress, String contractAddress, byte[] data) {
        Call.CallOutput callOutput = new Call.CallOutput();
        callOutput.setBlockNumber(1111);
        callOutput.setStatus(16);
        callOutput.setOutput(Hex.toHexString(data).substring(8));
        return callOutput;
    }
}
