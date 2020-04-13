package com.webank.wecross.stub.bcos.web3j;

import java.io.IOException;
import org.fisco.bcos.web3j.protocol.channel.StatusCode;
import org.fisco.bcos.web3j.protocol.core.methods.response.Call;

public class Web3jWrapperCallNotSucStatus extends Web3jWrapperImplMock {
    @Override
    public Call.CallOutput call(String accountAddress, String contractAddress, String data)
            throws IOException {
        Call.CallOutput callOutput = new Call.CallOutput();
        callOutput.setCurrentBlockNumber("0x1111");
        callOutput.setStatus(StatusCode.RevertInstruction);
        callOutput.setOutput(data.substring(10));
        return callOutput;
    }
}
