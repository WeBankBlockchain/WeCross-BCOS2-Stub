package com.webank.wecross.stub.bcos.performance.parallel.proxy;

import com.webank.wecross.stub.bcos.BCOSConnection;
import com.webank.wecross.stub.bcos.BCOSStubFactory;
import com.webank.wecross.stub.bcos.account.BCOSAccount;
import com.webank.wecross.stub.bcos.performance.PerformanceSuite;
import com.webank.wecross.stub.bcos.performance.PerformanceSuiteCallback;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import org.fisco.bcos.channel.client.TransactionSucCallback;
import org.fisco.bcos.web3j.abi.FunctionEncoder;
import org.fisco.bcos.web3j.abi.TypeReference;
import org.fisco.bcos.web3j.abi.datatypes.Function;
import org.fisco.bcos.web3j.abi.datatypes.Type;
import org.fisco.bcos.web3j.abi.datatypes.generated.Uint256;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.fisco.bcos.web3j.tx.gas.StaticGasProvider;
import org.fisco.bcos.web3j.utils.Numeric;

public class BCOSProxyParallelSuite implements PerformanceSuite {
    private Web3j web3j;
    private Credentials credentials;
    private WeCrossProxy weCrossProxy;
    private String contractAddress;
    private DagUserMgr dagUserMgr;
    private String path;

    public BCOSProxyParallelSuite(
            String chain, String account, String path, String proxyAddress, String userFile)
            throws Exception {

        BCOSStubFactory bcosStubFactory = new BCOSStubFactory();

        BCOSConnection connection =
                (BCOSConnection) bcosStubFactory.newConnection("classpath:/" + chain);

        this.web3j = connection.getWeb3jWrapper().getWeb3j();

        BCOSAccount bcosAccount =
                (BCOSAccount)
                        bcosStubFactory.newAccount(
                                account, "classpath:/accounts" + File.separator + account);

        this.dagUserMgr = new DagUserMgr(userFile);
        this.dagUserMgr.loadDagTransferUser();

        this.path = path;
        this.credentials = bcosAccount.getCredentials();
        this.contractAddress = proxyAddress;
        this.weCrossProxy =
                WeCrossProxy.load(
                        this.contractAddress,
                        this.web3j,
                        this.credentials,
                        new StaticGasProvider(
                                new BigInteger("3000000000"), new BigInteger("3000000000")));
    }

    @Override
    public String getName() {
        return "Parallel Proxy Call Suite";
    }

    @Override
    public void call(PerformanceSuiteCallback callback, int index) {
        String from = dagUserMgr.getFrom(index).getUser();
        String to = dagUserMgr.getTo(index).getUser();
        final BigInteger amount = BigInteger.ONE;

        /**
         * function userTransfer(string memory user_a, string memory user_b, uint256 amount) public
         * returns(uint256)
         */
        final Function function =
                new Function(
                        "userTransfer",
                        Arrays.<Type>asList(
                                new org.fisco.bcos.web3j.abi.datatypes.Utf8String(from),
                                new org.fisco.bcos.web3j.abi.datatypes.Utf8String(to),
                                new Uint256(amount)),
                        Collections.<TypeReference<?>>emptyList());

        /** A string beginning with 0x is marked as the contract address */
        boolean sendTxByPath = !path.startsWith("0x");

        String parallelSendTransactionSeq = null;
        if (sendTxByPath) {
            parallelSendTransactionSeq =
                    weCrossProxy.parallelSendTransactionSeq(
                            from,
                            to,
                            path,
                            "userTransfer(string,string,uint256)",
                            Numeric.hexStringToByteArray(FunctionEncoder.encode(function)));
        } else {
            parallelSendTransactionSeq =
                    weCrossProxy.parallelSendTransactionByAddressSeq(
                            from,
                            to,
                            path,
                            "userTransfer(string,string,uint256)",
                            Numeric.hexStringToByteArray(FunctionEncoder.encode(function)));
        }

        try {
            web3j.sendRawTransaction(
                    parallelSendTransactionSeq,
                    new TransactionSucCallback() {
                        @Override
                        public void onResponse(TransactionReceipt response) {
                            if (response.isStatusOK()) {
                                callback.onSuccess("Success");

                            } else {
                                callback.onFailed("Failed! status: " + response.getStatus());
                            }
                        }
                    });
            /*
            web3j.sendRawTransactionAndGetProof(
                    parallelSendTransactionSeq,
                    new TransactionSucCallback() {
                        @Override
                        public void onResponse(TransactionReceipt response) {
                            if (response.isStatusOK()) {
                                callback.onSuccess("Success");

                            } else {
                                callback.onFailed("Failed! status: " + response.getStatus());
                            }
                        }
                    });
            */
        } catch (IOException e) {
            // e.printStackTrace();
            callback.onFailed("Call failed: " + e);
        }
    }
}
