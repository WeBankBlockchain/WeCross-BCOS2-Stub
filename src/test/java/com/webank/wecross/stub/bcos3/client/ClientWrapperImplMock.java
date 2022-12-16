package com.webank.wecross.stub.bcos3.client;

import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.webank.wecross.stub.bcos3.common.ObjectMapperFactory;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Collections;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.client.protocol.model.JsonTransactionResponse;
import org.fisco.bcos.sdk.v3.client.protocol.response.BcosBlock;
import org.fisco.bcos.sdk.v3.client.protocol.response.BcosBlockHeader;
import org.fisco.bcos.sdk.v3.client.protocol.response.BcosGroupInfo;
import org.fisco.bcos.sdk.v3.client.protocol.response.BcosGroupNodeInfo;
import org.fisco.bcos.sdk.v3.client.protocol.response.Call;
import org.fisco.bcos.sdk.v3.crypto.CryptoSuite;
import org.fisco.bcos.sdk.v3.model.CryptoType;
import org.fisco.bcos.sdk.v3.model.EnumNodeVersion;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fisco.bcos.sdk.v3.model.callback.TransactionCallback;
import org.fisco.bcos.sdk.v3.utils.Hex;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

public class ClientWrapperImplMock extends AbstractClientWrapper {

    private static final Client mockClient = Mockito.mock(Client.class);
    private static final CryptoSuite cryptoSuite = new CryptoSuite(CryptoType.ECDSA_TYPE);

    public ClientWrapperImplMock() {
        super(mockClient);
    }

    static {
        when(mockClient.getChainId()).thenReturn("chain0");
        when(mockClient.getGroup()).thenReturn("group0");
        when(mockClient.getCryptoSuite()).thenReturn(cryptoSuite);
        when(mockClient.isWASM()).thenReturn(false);
        when(mockClient.getBlockLimit()).thenReturn(BigInteger.valueOf(500));
        when(mockClient.getGroupInfo())
                .then(
                        (Answer<BcosGroupInfo>)
                                invocation -> {
                                    BcosGroupInfo bcosGroupInfo = new BcosGroupInfo();
                                    BcosGroupInfo.GroupInfo groupInfo =
                                            new BcosGroupInfo.GroupInfo();
                                    BcosGroupNodeInfo.GroupNodeInfo groupNodeInfo =
                                            new BcosGroupNodeInfo.GroupNodeInfo();
                                    BcosGroupNodeInfo.Protocol protocol =
                                            new BcosGroupNodeInfo.Protocol();
                                    protocol.setCompatibilityVersion(
                                            EnumNodeVersion.BCOS_3_1_0.getVersion());
                                    groupNodeInfo.setProtocol(protocol);
                                    groupInfo.setNodeList(Collections.singletonList(groupNodeInfo));
                                    bcosGroupInfo.setResult(groupInfo);
                                    return bcosGroupInfo;
                                });
    }

    @Override
    public BcosBlock.Block getBlockByNumber(long blockNumber) throws IOException {
        String blockJson =
                "{\"consensusWeights\":[1,1],\"extraData\":\"0x\",\"gasUsed\":\"1114278\",\"hash\":\"0xc403e7f3255c7822e86075c1b97c4de359a511030794af8f8c74692e1b494e03\",\"number\":9,\"parentInfo\":[{\"blockHash\":\"0x84a1387f18dc03ee329050715819566d8962225b8cee25ce5db5f2d863f3ec3a\",\"blockNumber\":8}],\"receiptsRoot\":\"0x121775bcc0ef53db7fd984d012d7a855990bd873f493564e5de0b3b43745e297\",\"sealer\":0,\"sealerList\":[\"0x97af395f31cd52868162c790c2248e23f65c85a64cd0581d323515f6afffc0138279292a55f7bd706f8f1602f142b12a3407a45334eb0cf7daeb064dcec69369\",\"0xffa9aa23918afcfa5c20a07177e83731c46f153b3ce33b98cb3c4b61c767d06296ef9c1b7f7c6737c3077a6ec61c1a86d665475629cecd1c209b3f9a3b8688dc\"],\"signatureList\":[{\"sealerIndex\":0,\"signature\":\"0x01d116a8e7faaf8822911ebe97261b80653bff3082077a04efc1f758a0b290eb1abc413f8c429bdc280a1ece318607cf614eaa7c2eb6751845a2bec1a798495900\"},{\"sealerIndex\":1,\"signature\":\"0x38ad518cfab1c1b830f4f26b99c75c87860320b8ef02672720cf9c4529ca379c75d642636490c7dcd9abc7d6a1f5280ce4d04aae6c4669f66b03bbe9803f2d5a01\"}],\"stateRoot\":\"0x71eb54a4996de36cb36ba52136f9a2e87f6c40627b3ab9c87ca9fb641073f013\",\"timestamp\":1667550861150,\"transactions\":[\"0x4803a0eee41c3d6f585913071cdaca955f70b023120648143373102c83e39bf5\",\"0xa89c07b85efeaeb602ad688895dc5f7fed29b26c2c5ecae80099a4f9c1c8f4c1\",\"0xddc1b82bb2a76311984f5ad31a5df3395ae0eb2d1985e2dbfdede77d13ffe21f\",\"0x724792b07d72938afaa7d1af271bc249b0a6ce529ed8796c5f7d8a74e5fc5110\",\"0x41e115d3b06ecdf99f3406102f6a9aac93efdbd0a9d4f6fae134465845190ba0\",\"0xcdc7bc7df2c8206f8ab3a95a1a26cababff07f479e786432dab29e35d027703d\",\"0xe4c739590938bf4169a8139019403c2ccfcef3996cfbf71638e16d38a2988ff4\",\"0xbc34f391fb114d3c2e58c79d2d53ee94049e1b8826f808cd1a3df15f73603931\",\"0xf4d9a1b1d825afc64cf51e2e67ab00181d3a26532eb3996eeb9f0f6ce794fc31\",\"0x37482c537f48186adf5b91e3bc8f7461cf93dd7ef8050d4af13822e327d3b966\",\"0xe0a7cb5b1203086e78d2e3e0669397ec3efb35d74b7184f8bbd39e3944cbb502\",\"0x3c90977952af6b890febe7a726688d33c9f2179f65d90a0cd345dbb049f3dee1\",\"0xc66777b2d66b96ea625deb32d543bbbd66327b9b1ffd149c8203ad99d3fde2bf\",\"0x10e77e7b03501167c7577e7d0d31aa4fe7074b4578263e6b9208deb23d2d960d\",\"0xf3421699230b54b0b1b863b924d8918f033782721269e0f087769e0f4690a3d3\",\"0x0c36ab2720599546cbab1a856f30ac3f083af6c145c31d64ff2a30ad6a0a2aac\",\"0xf6f14d0cd1d33b85426375cbda176d13cf48dcded95cc0c0aafe34f57d38b5b4\",\"0xf116212394b43e8bb2023e362a9607c5262c199b8d9128cb04031a460503c431\",\"0xa5014d2fe03ad786ec32bd76739d0d53f50fb89344b52fadb1455a3bb46a80e3\",\"0x54533f6dc61ea9b7197cec3d25c3a2268acab81379d0e7f9ba656d8036210e96\",\"0xadff4d2a66d3ffc1cc2f9b3e8e55c13d2acc6ae478bf64978425ef08663f43d0\",\"0x9d30feb6b23b8e35df695fd45ddbaccf9e2adc5f41e680f067f8f7fa60ad999d\",\"0x44d2388bf03738c3ed0fdbab06cc12fe1735b197c7f7cba20729e72d22038392\",\"0x0e1ad907103a6710e15be99a18a74e8a81c2481fb2e01f3dfc08cd7c00156c72\",\"0xf7269392ef937ba492316a309a7a908a89dac9f64abedb270e5d441778c8e11b\",\"0xa6c5bc68e885a05e18ec4df4b7bea4ae0aa34a1aca8a74280b3a38ac7eddd1e9\",\"0x8cfae1560598c657d276f7a798dea685ef6b29e504b6bdddb36a404ecd90723b\",\"0x793fe55d71ab9a098390804491d66e4fc3772b43d57b83ea632417451a76d5c7\",\"0x6d1e3d3c4e6c1186e2231285883b21f39de0ca1419ffe250e299ac2f9cf31c82\",\"0x4bea8a08c57970258b73ef49ca7b124ac8299a1a92efb69bd4078a0ca2ce3f11\",\"0x12ad556d00af6fb3799689e34ca6adcad7f70a281c7a8fd2cfd3465366117461\",\"0x814b439a52921d7fe5c57be945491cc31393c4cd4b43b688d116192bdbbee596\",\"0xd25b9fff6c204075f517acd7fee165cb04782352501349939ed8b9aa88a365ae\",\"0x4a7a6cfb853e0c030c412fb8d9cbc457a1568a7fda3a8fb92030de1806e18ac0\",\"0xa53ed2ea96cbb1327c2b2cb40c9feb9d8727d36e80ed9a236f594692e087ff96\",\"0xe673b20420c22e26e9997b5fa73ca2e2a321d9e7e04280ba16c39256ed9c6a8c\",\"0xa6b41955b9b1ee1340244d2dba5e648c7ce16a525cdeb43e7f8c9df82ecd304b\",\"0xdad5f66e4799b16a594d23adeb02b4059a94b5449ce319facc4044d38cdff354\",\"0x718d01dbefb0d4a944e86be2f09de1fee4718544b0564561adbe5a998a556e72\",\"0x74d480c91d838a62cd77109ae6aa5dc74a661541030b8ed2d9479050eaa6a2ef\",\"0xeb23bc649a138b6d1d8a79eb11074275631382fec7151e4b0926bc3515bb98e3\",\"0x2093c00678e4fd1c2ae653677c20e0a7338717f6130cf357336fcb920cd3db6f\",\"0x970a8e6ef5a3ba1ece44b87747730e1269b1b1d7a3b574914c9015841f2916fe\",\"0x58d1dce7c4ad2fad74748adcc8d25e57fe0ff0dae62fb6c5dd699f891fb556ca\",\"0x536f673c131e19f92735e0f77cb669efdfb04f15e9ee5095eec1ea290ae97548\",\"0x272765b69b9d400c3f3d90906f0ebcdc2016e67113e3bf670ee9e98d38a39de3\",\"0x96a9240afb7803cb91f6280f2ab9bdc4bbc7dee89901ff099a0a27049ad4de16\",\"0x91c80eaac79f294180a12e6def545837cb01df6ce2344a603b1eaf33c5bd6d74\",\"0xb6ed9766531166d245c6e155cf5def013bc12cf5639bed291ca2cea681a508cb\",\"0x339967cc6b48f27c983b704ca3217385f1759b539fc04244ed639be9d71d428c\",\"0xf48a6d79eeefa43e6e8d8c319f53f689431725d432a058f8040fee9c9dfcdf8d\"],\"txsRoot\":\"0xaddb42e1db5ef2625c610506b46c745d2418263463b5beb537cf5c10e8d387dd\",\"version\":50331649}";
        ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        return objectMapper.readValue(blockJson, BcosBlock.Block.class);
    }

    @Override
    public BcosBlockHeader.BlockHeader getBlockHeaderByNumber(long blockNumber) throws IOException {
        String headerJson =
                "{\"consensusWeights\":[1,1],\"extraData\":\"0x\",\"gasUsed\":\"1114278\",\"hash\":\"0xc403e7f3255c7822e86075c1b97c4de359a511030794af8f8c74692e1b494e03\",\"number\":9,\"parentInfo\":[{\"blockHash\":\"0x84a1387f18dc03ee329050715819566d8962225b8cee25ce5db5f2d863f3ec3a\",\"blockNumber\":8}],\"receiptsRoot\":\"0x121775bcc0ef53db7fd984d012d7a855990bd873f493564e5de0b3b43745e297\",\"sealer\":0,\"sealerList\":[\"0x97af395f31cd52868162c790c2248e23f65c85a64cd0581d323515f6afffc0138279292a55f7bd706f8f1602f142b12a3407a45334eb0cf7daeb064dcec69369\",\"0xffa9aa23918afcfa5c20a07177e83731c46f153b3ce33b98cb3c4b61c767d06296ef9c1b7f7c6737c3077a6ec61c1a86d665475629cecd1c209b3f9a3b8688dc\"],\"signatureList\":[{\"sealerIndex\":0,\"signature\":\"0x01d116a8e7faaf8822911ebe97261b80653bff3082077a04efc1f758a0b290eb1abc413f8c429bdc280a1ece318607cf614eaa7c2eb6751845a2bec1a798495900\"},{\"sealerIndex\":1,\"signature\":\"0x38ad518cfab1c1b830f4f26b99c75c87860320b8ef02672720cf9c4529ca379c75d642636490c7dcd9abc7d6a1f5280ce4d04aae6c4669f66b03bbe9803f2d5a01\"}],\"stateRoot\":\"0x71eb54a4996de36cb36ba52136f9a2e87f6c40627b3ab9c87ca9fb641073f013\",\"timestamp\":1667550861150,\"txsRoot\":\"0xaddb42e1db5ef2625c610506b46c745d2418263463b5beb537cf5c10e8d387dd\",\"version\":50331649}";
        ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        return objectMapper.readValue(headerJson, BcosBlockHeader.BlockHeader.class);
    }

    @Override
    public BigInteger getBlockNumber() throws IOException {
        return BigInteger.valueOf(11111);
    }

    @Override
    public void sendTransaction(String signedTransactionData, TransactionCallback callback)
            throws IOException {
        String transactionReceiptJson =
                "{\"blockNumber\":9,\"checksumContractAddress\":\"\",\"contractAddress\":\"\",\"from\":\"0x1937ded126d516a8611683abfe590860e539d1c9\",\"gasUsed\":\"34954\",\"hash\":\"0x2937b8ce820e148bf8cc26698b2d38ecabc8fe745d3d0bcd509228143c0365c7\",\"input\":\"0x6a5bae4e000000000000000000000000000000000000000000000000000000000000006000000000000000000000000000000000000000000000000000000000000000a000000000000000000000000000000000000000000000000000000000000000e0000000000000000000000000000000000000000000000000000000000000000f745f746573743939343131373433340000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000c6b65793939343131373433340000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000200000000000000000000000000000000000000000000000000000000000000400000000000000000000000000000000000000000000000000000000000000080000000000000000000000000000000000000000000000000000000000000000d6e616d6539393431313734333400000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000c6167653939343131373433340000000000000000000000000000000000000000\",\"logEntries\":[{\"address\":\"4721d1a77e0e76851d460073e64ea06d9c104194\",\"data\":\"0x0000000000000000000000000000000000000000000000000000000000000000\",\"topics\":[\"0xb5636cd912a73dcdb5b570dbe331dfa3e6435c93e029e642def2c8e40dacf210\"]}],\"message\":\"\",\"output\":\"0x0000000000000000000000000000000000000000000000000000000000000000\",\"receiptProof\":[{\"left\":[],\"right\":[\"66ad4f39b7ee01bc0116ce9e073add491a86d4bf115f0f6b8ac448e2ebc7aff2\",\"2937b8ce820e148bf8cc26698b2d38ecabc8fe745d3d0bcd509228143c0365c7\",\"2937b8ce820e148bf8cc26698b2d38ecabc8fe745d3d0bcd509228143c0365c7\",\"2937b8ce820e148bf8cc26698b2d38ecabc8fe745d3d0bcd509228143c0365c7\",\"2937b8ce820e148bf8cc26698b2d38ecabc8fe745d3d0bcd509228143c0365c7\",\"66ad4f39b7ee01bc0116ce9e073add491a86d4bf115f0f6b8ac448e2ebc7aff2\",\"66ad4f39b7ee01bc0116ce9e073add491a86d4bf115f0f6b8ac448e2ebc7aff2\",\"2937b8ce820e148bf8cc26698b2d38ecabc8fe745d3d0bcd509228143c0365c7\",\"2937b8ce820e148bf8cc26698b2d38ecabc8fe745d3d0bcd509228143c0365c7\",\"66ad4f39b7ee01bc0116ce9e073add491a86d4bf115f0f6b8ac448e2ebc7aff2\",\"2937b8ce820e148bf8cc26698b2d38ecabc8fe745d3d0bcd509228143c0365c7\",\"66ad4f39b7ee01bc0116ce9e073add491a86d4bf115f0f6b8ac448e2ebc7aff2\",\"66ad4f39b7ee01bc0116ce9e073add491a86d4bf115f0f6b8ac448e2ebc7aff2\",\"2937b8ce820e148bf8cc26698b2d38ecabc8fe745d3d0bcd509228143c0365c7\",\"66ad4f39b7ee01bc0116ce9e073add491a86d4bf115f0f6b8ac448e2ebc7aff2\"]},{\"left\":[],\"right\":[\"4983676cd0d3cc4c1339d051d7f2a972f4baeaca9f371c3e01660babc082c16d\",\"8e805e8916c3a61efc62acf90f292b052d3f9ca8a6a3fd0abc5892cb27a4cbb0\",\"6f77a89d32e21a2886da4b86734b456fe06974f0c72924155ab6aee29e1148a7\"]},{\"left\":[],\"right\":[]}],\"status\":0,\"to\":\"0x4721d1a77e0e76851d460073e64ea06d9c104194\",\"transactionHash\":\"0xf3421699230b54b0b1b863b924d8918f033782721269e0f087769e0f4690a3d3\",\"transactionProof\":[{\"left\":[\"4803a0eee41c3d6f585913071cdaca955f70b023120648143373102c83e39bf5\",\"a89c07b85efeaeb602ad688895dc5f7fed29b26c2c5ecae80099a4f9c1c8f4c1\",\"ddc1b82bb2a76311984f5ad31a5df3395ae0eb2d1985e2dbfdede77d13ffe21f\",\"724792b07d72938afaa7d1af271bc249b0a6ce529ed8796c5f7d8a74e5fc5110\",\"41e115d3b06ecdf99f3406102f6a9aac93efdbd0a9d4f6fae134465845190ba0\",\"cdc7bc7df2c8206f8ab3a95a1a26cababff07f479e786432dab29e35d027703d\",\"e4c739590938bf4169a8139019403c2ccfcef3996cfbf71638e16d38a2988ff4\",\"bc34f391fb114d3c2e58c79d2d53ee94049e1b8826f808cd1a3df15f73603931\",\"f4d9a1b1d825afc64cf51e2e67ab00181d3a26532eb3996eeb9f0f6ce794fc31\",\"37482c537f48186adf5b91e3bc8f7461cf93dd7ef8050d4af13822e327d3b966\",\"e0a7cb5b1203086e78d2e3e0669397ec3efb35d74b7184f8bbd39e3944cbb502\",\"3c90977952af6b890febe7a726688d33c9f2179f65d90a0cd345dbb049f3dee1\",\"c66777b2d66b96ea625deb32d543bbbd66327b9b1ffd149c8203ad99d3fde2bf\",\"10e77e7b03501167c7577e7d0d31aa4fe7074b4578263e6b9208deb23d2d960d\"],\"right\":[\"0c36ab2720599546cbab1a856f30ac3f083af6c145c31d64ff2a30ad6a0a2aac\"]},{\"left\":[],\"right\":[\"9261a95de605eb8ac450b82f4b0d55bdebd3c51584e021975c802122b1d79041\",\"cb981ea1077a408feab9e866e7a27eac0f10d4e0e5d0f92a3558efe14da9d1b9\",\"c2fd3a094ef3754fac54937e980003af0825f79abb363c46a8ab740f80ce1a30\"]},{\"left\":[],\"right\":[]}],\"version\":0}";
        ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        TransactionReceipt transactionReceipt =
                objectMapper.readValue(transactionReceiptJson, TransactionReceipt.class);
        callback.onResponse(transactionReceipt);
    }

    @Override
    public TransactionReceipt getTransactionReceiptByHashWithProof(String transactionHash)
            throws IOException {
        String transactionReceiptAndProofJson0 =
                "{\"blockNumber\":9,\"checksumContractAddress\":\"\",\"contractAddress\":\"\",\"from\":\"0x1937ded126d516a8611683abfe590860e539d1c9\",\"gasUsed\":\"34954\",\"hash\":\"0x2937b8ce820e148bf8cc26698b2d38ecabc8fe745d3d0bcd509228143c0365c7\",\"input\":\"0x6a5bae4e000000000000000000000000000000000000000000000000000000000000006000000000000000000000000000000000000000000000000000000000000000a000000000000000000000000000000000000000000000000000000000000000e0000000000000000000000000000000000000000000000000000000000000000f745f746573743939343131373433340000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000c6b65793939343131373433340000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000200000000000000000000000000000000000000000000000000000000000000400000000000000000000000000000000000000000000000000000000000000080000000000000000000000000000000000000000000000000000000000000000d6e616d6539393431313734333400000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000c6167653939343131373433340000000000000000000000000000000000000000\",\"logEntries\":[{\"address\":\"4721d1a77e0e76851d460073e64ea06d9c104194\",\"data\":\"0x0000000000000000000000000000000000000000000000000000000000000000\",\"topics\":[\"0xb5636cd912a73dcdb5b570dbe331dfa3e6435c93e029e642def2c8e40dacf210\"]}],\"message\":\"\",\"output\":\"0x0000000000000000000000000000000000000000000000000000000000000000\",\"receiptProof\":[{\"left\":[],\"right\":[\"66ad4f39b7ee01bc0116ce9e073add491a86d4bf115f0f6b8ac448e2ebc7aff2\",\"2937b8ce820e148bf8cc26698b2d38ecabc8fe745d3d0bcd509228143c0365c7\",\"2937b8ce820e148bf8cc26698b2d38ecabc8fe745d3d0bcd509228143c0365c7\",\"2937b8ce820e148bf8cc26698b2d38ecabc8fe745d3d0bcd509228143c0365c7\",\"2937b8ce820e148bf8cc26698b2d38ecabc8fe745d3d0bcd509228143c0365c7\",\"66ad4f39b7ee01bc0116ce9e073add491a86d4bf115f0f6b8ac448e2ebc7aff2\",\"66ad4f39b7ee01bc0116ce9e073add491a86d4bf115f0f6b8ac448e2ebc7aff2\",\"2937b8ce820e148bf8cc26698b2d38ecabc8fe745d3d0bcd509228143c0365c7\",\"2937b8ce820e148bf8cc26698b2d38ecabc8fe745d3d0bcd509228143c0365c7\",\"66ad4f39b7ee01bc0116ce9e073add491a86d4bf115f0f6b8ac448e2ebc7aff2\",\"2937b8ce820e148bf8cc26698b2d38ecabc8fe745d3d0bcd509228143c0365c7\",\"66ad4f39b7ee01bc0116ce9e073add491a86d4bf115f0f6b8ac448e2ebc7aff2\",\"66ad4f39b7ee01bc0116ce9e073add491a86d4bf115f0f6b8ac448e2ebc7aff2\",\"2937b8ce820e148bf8cc26698b2d38ecabc8fe745d3d0bcd509228143c0365c7\",\"66ad4f39b7ee01bc0116ce9e073add491a86d4bf115f0f6b8ac448e2ebc7aff2\"]},{\"left\":[],\"right\":[\"4983676cd0d3cc4c1339d051d7f2a972f4baeaca9f371c3e01660babc082c16d\",\"8e805e8916c3a61efc62acf90f292b052d3f9ca8a6a3fd0abc5892cb27a4cbb0\",\"6f77a89d32e21a2886da4b86734b456fe06974f0c72924155ab6aee29e1148a7\"]},{\"left\":[],\"right\":[]}],\"status\":0,\"to\":\"0x4721d1a77e0e76851d460073e64ea06d9c104194\",\"transactionHash\":\"0xf3421699230b54b0b1b863b924d8918f033782721269e0f087769e0f4690a3d3\",\"transactionProof\":[{\"left\":[\"4803a0eee41c3d6f585913071cdaca955f70b023120648143373102c83e39bf5\",\"a89c07b85efeaeb602ad688895dc5f7fed29b26c2c5ecae80099a4f9c1c8f4c1\",\"ddc1b82bb2a76311984f5ad31a5df3395ae0eb2d1985e2dbfdede77d13ffe21f\",\"724792b07d72938afaa7d1af271bc249b0a6ce529ed8796c5f7d8a74e5fc5110\",\"41e115d3b06ecdf99f3406102f6a9aac93efdbd0a9d4f6fae134465845190ba0\",\"cdc7bc7df2c8206f8ab3a95a1a26cababff07f479e786432dab29e35d027703d\",\"e4c739590938bf4169a8139019403c2ccfcef3996cfbf71638e16d38a2988ff4\",\"bc34f391fb114d3c2e58c79d2d53ee94049e1b8826f808cd1a3df15f73603931\",\"f4d9a1b1d825afc64cf51e2e67ab00181d3a26532eb3996eeb9f0f6ce794fc31\",\"37482c537f48186adf5b91e3bc8f7461cf93dd7ef8050d4af13822e327d3b966\",\"e0a7cb5b1203086e78d2e3e0669397ec3efb35d74b7184f8bbd39e3944cbb502\",\"3c90977952af6b890febe7a726688d33c9f2179f65d90a0cd345dbb049f3dee1\",\"c66777b2d66b96ea625deb32d543bbbd66327b9b1ffd149c8203ad99d3fde2bf\",\"10e77e7b03501167c7577e7d0d31aa4fe7074b4578263e6b9208deb23d2d960d\"],\"right\":[\"0c36ab2720599546cbab1a856f30ac3f083af6c145c31d64ff2a30ad6a0a2aac\"]},{\"left\":[],\"right\":[\"9261a95de605eb8ac450b82f4b0d55bdebd3c51584e021975c802122b1d79041\",\"cb981ea1077a408feab9e866e7a27eac0f10d4e0e5d0f92a3558efe14da9d1b9\",\"c2fd3a094ef3754fac54937e980003af0825f79abb363c46a8ab740f80ce1a30\"]},{\"left\":[],\"right\":[]}],\"version\":0}";
        TransactionReceipt receiptAndProof0 =
                ObjectMapperFactory.getObjectMapper()
                        .readValue(transactionReceiptAndProofJson0, TransactionReceipt.class);
        return receiptAndProof0;
    }

    @Override
    public JsonTransactionResponse getTransactionByHashWithProof(String transactionHash)
            throws IOException {
        String transactionAndProofJson0 =
                "{\"abi\":\"\",\"blockLimit\":508,\"chainID\":\"chain0\",\"from\":\"0x1937ded126d516a8611683abfe590860e539d1c9\",\"groupID\":\"group0\",\"hash\":\"0xf3421699230b54b0b1b863b924d8918f033782721269e0f087769e0f4690a3d3\",\"importTime\":1667550860785,\"input\":\"0x6a5bae4e000000000000000000000000000000000000000000000000000000000000006000000000000000000000000000000000000000000000000000000000000000a000000000000000000000000000000000000000000000000000000000000000e0000000000000000000000000000000000000000000000000000000000000000f745f746573743939343131373433340000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000c6b65793939343131373433340000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000200000000000000000000000000000000000000000000000000000000000000400000000000000000000000000000000000000000000000000000000000000080000000000000000000000000000000000000000000000000000000000000000d6e616d6539393431313734333400000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000c6167653939343131373433340000000000000000000000000000000000000000\",\"nonce\":\"72563413500349189967074512898819750022089027217545557199062596823714270938848\",\"signature\":\"0x14243c47970b701f9d37b47a5a913938930cecdbfe24e38ba9e24f67275a13e0240c89d25e14b3aef1b90c172b0a2fb5288385f1a3ec0107c538721d34c6978d00\",\"to\":\"0x4721d1a77e0e76851d460073e64ea06d9c104194\",\"transactionProof\":[{\"left\":[\"4803a0eee41c3d6f585913071cdaca955f70b023120648143373102c83e39bf5\",\"a89c07b85efeaeb602ad688895dc5f7fed29b26c2c5ecae80099a4f9c1c8f4c1\",\"ddc1b82bb2a76311984f5ad31a5df3395ae0eb2d1985e2dbfdede77d13ffe21f\",\"724792b07d72938afaa7d1af271bc249b0a6ce529ed8796c5f7d8a74e5fc5110\",\"41e115d3b06ecdf99f3406102f6a9aac93efdbd0a9d4f6fae134465845190ba0\",\"cdc7bc7df2c8206f8ab3a95a1a26cababff07f479e786432dab29e35d027703d\",\"e4c739590938bf4169a8139019403c2ccfcef3996cfbf71638e16d38a2988ff4\",\"bc34f391fb114d3c2e58c79d2d53ee94049e1b8826f808cd1a3df15f73603931\",\"f4d9a1b1d825afc64cf51e2e67ab00181d3a26532eb3996eeb9f0f6ce794fc31\",\"37482c537f48186adf5b91e3bc8f7461cf93dd7ef8050d4af13822e327d3b966\",\"e0a7cb5b1203086e78d2e3e0669397ec3efb35d74b7184f8bbd39e3944cbb502\",\"3c90977952af6b890febe7a726688d33c9f2179f65d90a0cd345dbb049f3dee1\",\"c66777b2d66b96ea625deb32d543bbbd66327b9b1ffd149c8203ad99d3fde2bf\",\"10e77e7b03501167c7577e7d0d31aa4fe7074b4578263e6b9208deb23d2d960d\"],\"right\":[\"0c36ab2720599546cbab1a856f30ac3f083af6c145c31d64ff2a30ad6a0a2aac\"]},{\"left\":[],\"right\":[\"9261a95de605eb8ac450b82f4b0d55bdebd3c51584e021975c802122b1d79041\",\"cb981ea1077a408feab9e866e7a27eac0f10d4e0e5d0f92a3558efe14da9d1b9\",\"c2fd3a094ef3754fac54937e980003af0825f79abb363c46a8ab740f80ce1a30\"]},{\"left\":[],\"right\":[]}],\"version\":0}";
        JsonTransactionResponse transAndProof0 =
                ObjectMapperFactory.getObjectMapper()
                        .readValue(transactionAndProofJson0, JsonTransactionResponse.class);
        return transAndProof0;
    }

    @Override
    public Call.CallOutput call(String accountAddress, String contractAddress, byte[] data)
            throws IOException {
        Call.CallOutput callOutput = new Call.CallOutput();
        callOutput.setBlockNumber(4369L);
        callOutput.setStatus(0);
        callOutput.setOutput(Hex.toHexString(data).substring(8));
        return callOutput;
    }
}
