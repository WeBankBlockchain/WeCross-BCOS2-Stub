import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.webank.wecross.stub.bcos.contract.StubFunction;
import com.webank.wecross.stub.bcos.web3j.Web3jWrapper;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import org.fisco.bcos.web3j.abi.datatypes.Function;
import org.fisco.bcos.web3j.protocol.ObjectMapperFactory;
import org.fisco.bcos.web3j.protocol.core.methods.response.BcosBlock;
import org.fisco.bcos.web3j.protocol.core.methods.response.Call;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;

public class Web3jWrapperMock implements Web3jWrapper {

    @Override
    public BcosBlock.Block getBlockByNumber(long blockNumber) throws IOException {
        String blockJson =
                "{\"number\":331,\"hash\":\"0x6db416c8ac6b1fe7ed08771de419b71c084ee5969029346806324601f2e3f0d0\",\"parentHash\":\"0xed0ef6826277efbc9601dedc1b6ea20067eed219e415e1038f111155b8fc1e24\",\"nonce\":0,\"sha3Uncles\":null,\"logsBloom\":\"0x00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000\",\"transactionsRoot\":\"0x07009a9d655cee91e95dcd1c53d5917a58f80e6e6ac689bae24bd911d75c471c\",\"stateRoot\":\"0xce8a92c9311e9e0b77842c86adf8fcf91cbab8fb5daefc85b21f501ca8b1f682\",\"receiptsRoot\":\"0x2a4433b7611c4b1fae16b873ced1dec9a65b82416e448f58fded002c05a10082\",\"author\":null,\"sealer\":\"0x1\",\"mixHash\":null,\"extraData\":[],\"gasLimit\":0,\"gasUsed\":0,\"timestamp\":1584081463141,\"transactions\":[{}],\"uncles\":null,\"sealerList\":[\"7f6b1fc98c6bc8dbde4afe62bf1322a4f10ff29528f1e6bb0e57590aa81c31bfe57510787c5adf3fb90fb4239d5483c0d805874451aeb7e76c6c15e1b2123165\",\"9b04ba34f30452a43e7868e1b918c380f1d3d3bdc98d752d1dc30155e6a3dd9da6e530a4351eb4eab42f8703a3922233b830f2678c14179e3ac0f9e5bef8c954\",\"f4c43730a29511e66e9eddbee7024a65d8a8b3b886e6f652785faefb979676f04bd9671529aef9147c86edf58df0482b4e5b293006a179b14039484c6d20a18e\"],\"numberRaw\":\"0x14b\",\"nonceRaw\":null,\"gasLimitRaw\":\"0x0\",\"gasUsedRaw\":\"0x0\",\"timestampRaw\":\"0x170d29ce765\"}";
        ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        BcosBlock.Block block = objectMapper.readValue(blockJson, BcosBlock.Block.class);
        return block;
    }

    @Override
    public BigInteger getBlockNumber() throws IOException {
        return BigInteger.valueOf(11111);
    }

    @Override
    public TransactionReceipt sendTransaction(String signedTransactionData) throws IOException {
        String str =
                "{\"transactionHash\":\"0xcd0ec220b00a97115e367749be2dedec848236781f6a242a3ffa1d956dbf8ec5\",\"transactionIndex\":0,\"blockHash\":\"0x5f5c1d6ecf6a17c33ad58351cb49f981d96baf369cf177c3fc79f104e28b9fcb\",\"blockNumber\":334,\"gasUsed\":21008,\"contractAddress\":\"0x0000000000000000000000000000000000000000\",\"root\":\"0xf5a5fd42d16a20302798ef6ed309979b43003d2320d9f0e8ea9831a92759fb4b\",\"status\":\"0x1a\",\"message\":null,\"from\":\"0x4559386e129e5a96b47881a7123f8d3f9a187a9d\",\"to\":\"0x8827cca7f0f38b861b62dae6d711efe92a1e3602\",\"input\":\"0x0000\",\"output\":\"0x\",\"logs\":[],\"logsBloom\":\"0x00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000\"}";
        ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        TransactionReceipt receipt = objectMapper.readValue(str, TransactionReceipt.class);
        return receipt;
    }

    @Override
    public Call.CallOutput call(String contractAddress, String data) throws IOException {
        Function function =
                StubFunction.newFunction("funcName", Arrays.asList("aa", "bb", "cc", "dd"));
        Call.CallOutput callOutput = new Call.CallOutput();
        callOutput.setCurrentBlockNumber("0x1111");
        callOutput.setStatus("0x0");
        callOutput.setOutput(data.substring(10));
        return callOutput;
    }
}
