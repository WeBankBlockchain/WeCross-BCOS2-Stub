package com.webank.wecross.stub.bcos3.contract;

import org.fisco.bcos.sdk.v3.client.protocol.response.Call;
import org.fisco.bcos.sdk.v3.codec.datatypes.generated.tuples.generated.Tuple2;
import org.junit.Assert;
import org.junit.Test;

public class RevertMessageTest {
    @Test
    public void callRevertMessageTest() {
        Call.CallOutput callOutput = new Call.CallOutput();
        callOutput.setBlockNumber(123);
        callOutput.setStatus(22);
        callOutput.setOutput(
                "0x08c379a00000000000000000000000000000000000000000000000000000000000000020000000000000000000000000000000000000000000000000000000000000008408c379a00000000000000000000000000000000000000000000000000000000000000020000000000000000000000000000000000000000000000000000000000000003846726f7a656e20636f6e74726163743a32326435633762306433323734363366363363643466343932323030653832663961623034383962000000000000000000000000000000000000000000000000000000000000000000000000");

        Tuple2<Boolean, String> booleanStringTuple2 =
                RevertMessage.tryParserRevertMessage(
                        callOutput.getStatus(), callOutput.getOutput());
        Assert.assertTrue(booleanStringTuple2.getValue1());
        Assert.assertEquals(
                booleanStringTuple2.getValue2(),
                "Frozen contract:22d5c7b0d327463f63cd4f492200e82f9ab0489b");
    }
}
