package com.webank.wecross.stub.bcos.web3j;

import com.webank.wecross.stub.bcos.common.StatusCode;
import org.fisco.bcos.sdk.client.protocol.response.Call;

public class Web3jWrapperCallNotSucStatus extends Web3jWrapperImplMock {
    @Override
    public Call.CallOutput call(String accountAddress, String contractAddress, String data)
            {
        Call.CallOutput callOutput = new Call.CallOutput();
        callOutput.setCurrentBlockNumber("0x1111");
        callOutput.setStatus(StatusCode.RevertInstruction);
        callOutput.setOutput(data.substring(10));
        return callOutput;
    }
}
