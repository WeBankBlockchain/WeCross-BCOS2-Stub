import static junit.framework.TestCase.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.webank.wecross.stub.BlockHeader;
import com.webank.wecross.stub.bcos.BCOSConnection;
import java.io.IOException;
import org.fisco.bcos.web3j.protocol.ObjectMapperFactory;
import org.fisco.bcos.web3j.protocol.core.methods.response.BcosBlock;
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
        BlockHeader blockHeader = connection.toBlockHeader(block);
        assertEquals(blockHeader.getHash(), block.getHash());
        assertEquals(blockHeader.getPrevHash(), block.getParentHash());
        assertEquals(blockHeader.getReceiptRoot(), block.getReceiptsRoot());
        assertEquals(blockHeader.getStateRoot(), block.getStateRoot());
        assertEquals(blockHeader.getNumber(), block.getNumber().intValue());
        assertEquals(blockHeader.getTransactionRoot(), block.getTransactionsRoot());
    }
}
