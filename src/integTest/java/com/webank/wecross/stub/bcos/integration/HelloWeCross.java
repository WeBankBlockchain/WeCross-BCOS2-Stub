import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import org.fisco.bcos.channel.client.TransactionSucCallback;
import org.fisco.bcos.web3j.abi.FunctionReturnDecoder;
import org.fisco.bcos.web3j.abi.TypeReference;
import org.fisco.bcos.web3j.abi.datatypes.DynamicArray;
import org.fisco.bcos.web3j.abi.datatypes.Function;
import org.fisco.bcos.web3j.abi.datatypes.Type;
import org.fisco.bcos.web3j.abi.datatypes.Utf8String;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.EncryptType;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.core.RemoteCall;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.fisco.bcos.web3j.tuples.generated.Tuple1;
import org.fisco.bcos.web3j.tx.Contract;
import org.fisco.bcos.web3j.tx.TransactionManager;
import org.fisco.bcos.web3j.tx.gas.ContractGasProvider;
import org.fisco.bcos.web3j.tx.txdecode.TransactionDecoder;

/**
 * Auto generated code.
 *
 * <p><strong>Do not modify!</strong>
 *
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.fisco.bcos.web3j.codegen.SolidityFunctionWrapperGenerator in the <a
 * href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version none.
 */
@SuppressWarnings("unchecked")
public class HelloWeCross extends Contract {
    public static String BINARY =
            "608060405234801561001057600080fd5b50610580806100206000396000f30060806040526004361061004c576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff16806352e66db814610051578063b5cbae611461008e575b600080fd5b34801561005d57600080fd5b506100786004803603610073919081019061033b565b6100cb565b6040516100859190610420565b60405180910390f35b34801561009a57600080fd5b506100b560048036036100b0919081019061033b565b6100ec565b6040516100c29190610420565b60405180910390f35b606081600090805190602001906100e39291906100f6565b50819050919050565b6060819050919050565b828054828255906000526020600020908101928215610145579160200282015b82811115610144578251829080519060200190610134929190610156565b5091602001919060010190610116565b5b50905061015291906101d6565b5090565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f1061019757805160ff19168380011785556101c5565b828001600101855582156101c5579182015b828111156101c45782518255916020019190600101906101a9565b5b5090506101d29190610202565b5090565b6101ff91905b808211156101fb57600081816101f29190610227565b506001016101dc565b5090565b90565b61022491905b80821115610220576000816000905550600101610208565b5090565b90565b50805460018160011615610100020316600290046000825580601f1061024d575061026c565b601f01602090049060005260206000209081019061026b9190610202565b5b50565b600082601f830112151561028257600080fd5b81356102956102908261046f565b610442565b9150818183526020840193506020810190508360005b838110156102db57813586016102c188826102e5565b8452602084019350602083019250506001810190506102ab565b5050505092915050565b600082601f83011215156102f857600080fd5b813561030b61030682610497565b610442565b9150808252602083016020830185838301111561032757600080fd5b6103328382846104f3565b50505092915050565b60006020828403121561034d57600080fd5b600082013567ffffffffffffffff81111561036757600080fd5b6103738482850161026f565b91505092915050565b6000610387826104d0565b808452602084019350836020820285016103a0856104c3565b60005b848110156103d95783830388526103bb8383516103ea565b92506103c6826104e6565b91506020880197506001810190506103a3565b508196508694505050505092915050565b60006103f5826104db565b808452610409816020860160208601610502565b61041281610535565b602085010191505092915050565b6000602082019050818103600083015261043a818461037c565b905092915050565b6000604051905081810181811067ffffffffffffffff8211171561046557600080fd5b8060405250919050565b600067ffffffffffffffff82111561048657600080fd5b602082029050602081019050919050565b600067ffffffffffffffff8211156104ae57600080fd5b601f19601f8301169050602081019050919050565b6000602082019050919050565b600081519050919050565b600081519050919050565b6000602082019050919050565b82818337600083830152505050565b60005b83811015610520578082015181840152602081019050610505565b8381111561052f576000848401525b50505050565b6000601f19601f83011690509190505600a265627a7a72305820eed487e816dcce23436f904a995c92358a2a7f3d4e903e0d267d2138974827a16c6578706572696d656e74616cf50037";

    public static final String ABI =
            "[{\"constant\":false,\"inputs\":[{\"name\":\"_ss\",\"type\":\"string[]\"}],\"name\":\"set\",\"outputs\":[{\"name\":\"\",\"type\":\"string[]\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"ss\",\"type\":\"string[]\"}],\"name\":\"get\",\"outputs\":[{\"name\":\"\",\"type\":\"string[]\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"}]";

    public static final TransactionDecoder transactionDecoder = new TransactionDecoder(ABI, BINARY);

    public static String SM_BINARY =
            "608060405234801561001057600080fd5b50610580806100206000396000f30060806040526004361061004c576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff16806342dd463414610051578063656fa89a1461008e575b600080fd5b34801561005d57600080fd5b506100786004803603610073919081019061033b565b6100cb565b6040516100859190610420565b60405180910390f35b34801561009a57600080fd5b506100b560048036036100b0919081019061033b565b6100d5565b6040516100c29190610420565b60405180910390f35b6060819050919050565b606081600090805190602001906100ed9291906100f6565b50819050919050565b828054828255906000526020600020908101928215610145579160200282015b82811115610144578251829080519060200190610134929190610156565b5091602001919060010190610116565b5b50905061015291906101d6565b5090565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f1061019757805160ff19168380011785556101c5565b828001600101855582156101c5579182015b828111156101c45782518255916020019190600101906101a9565b5b5090506101d29190610202565b5090565b6101ff91905b808211156101fb57600081816101f29190610227565b506001016101dc565b5090565b90565b61022491905b80821115610220576000816000905550600101610208565b5090565b90565b50805460018160011615610100020316600290046000825580601f1061024d575061026c565b601f01602090049060005260206000209081019061026b9190610202565b5b50565b600082601f830112151561028257600080fd5b81356102956102908261046f565b610442565b9150818183526020840193506020810190508360005b838110156102db57813586016102c188826102e5565b8452602084019350602083019250506001810190506102ab565b5050505092915050565b600082601f83011215156102f857600080fd5b813561030b61030682610497565b610442565b9150808252602083016020830185838301111561032757600080fd5b6103328382846104f3565b50505092915050565b60006020828403121561034d57600080fd5b600082013567ffffffffffffffff81111561036757600080fd5b6103738482850161026f565b91505092915050565b6000610387826104d0565b808452602084019350836020820285016103a0856104c3565b60005b848110156103d95783830388526103bb8383516103ea565b92506103c6826104e6565b91506020880197506001810190506103a3565b508196508694505050505092915050565b60006103f5826104db565b808452610409816020860160208601610502565b61041281610535565b602085010191505092915050565b6000602082019050818103600083015261043a818461037c565b905092915050565b6000604051905081810181811067ffffffffffffffff8211171561046557600080fd5b8060405250919050565b600067ffffffffffffffff82111561048657600080fd5b602082029050602081019050919050565b600067ffffffffffffffff8211156104ae57600080fd5b601f19601f8301169050602081019050919050565b6000602082019050919050565b600081519050919050565b600081519050919050565b6000602082019050919050565b82818337600083830152505050565b60005b83811015610520578082015181840152602081019050610505565b8381111561052f576000848401525b50505050565b6000601f19601f83011690509190505600a265627a7a72305820b153bd6667df22d35899370ae1128b9588647ae29dbc4189137f11e3d3aba58e6c6578706572696d656e74616cf50037";

    public static final String FUNC_SET = "set";

    public static final String FUNC_GET = "get";

    @Deprecated
    protected HelloWeCross(
            String contractAddress,
            Web3j web3j,
            Credentials credentials,
            BigInteger gasPrice,
            BigInteger gasLimit) {
        super(getBinary(), contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected HelloWeCross(
            String contractAddress,
            Web3j web3j,
            Credentials credentials,
            ContractGasProvider contractGasProvider) {
        super(getBinary(), contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected HelloWeCross(
            String contractAddress,
            Web3j web3j,
            TransactionManager transactionManager,
            BigInteger gasPrice,
            BigInteger gasLimit) {
        super(getBinary(), contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected HelloWeCross(
            String contractAddress,
            Web3j web3j,
            TransactionManager transactionManager,
            ContractGasProvider contractGasProvider) {
        super(getBinary(), contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static String getBinary() {
        return (EncryptType.encryptType == EncryptType.ECDSA_TYPE ? BINARY : SM_BINARY);
    }

    public static TransactionDecoder getTransactionDecoder() {
        return transactionDecoder;
    }

    public RemoteCall<TransactionReceipt> set(List<String> _ss) {
        final Function function =
                new Function(
                        FUNC_SET,
                        Arrays.<Type>asList(
                                _ss.isEmpty()
                                        ? org.fisco.bcos.web3j.abi.datatypes.DynamicArray.empty(
                                                "string[]")
                                        : new org.fisco.bcos.web3j.abi.datatypes.DynamicArray<
                                                org.fisco.bcos.web3j.abi.datatypes.Utf8String>(
                                                org.fisco.bcos.web3j.abi.Utils.typeMap(
                                                        _ss,
                                                        org.fisco.bcos.web3j.abi.datatypes
                                                                .Utf8String.class))),
                        Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public void set(List<String> _ss, TransactionSucCallback callback) {
        final Function function =
                new Function(
                        FUNC_SET,
                        Arrays.<Type>asList(
                                _ss.isEmpty()
                                        ? org.fisco.bcos.web3j.abi.datatypes.DynamicArray.empty(
                                                "string[]")
                                        : new org.fisco.bcos.web3j.abi.datatypes.DynamicArray<
                                                org.fisco.bcos.web3j.abi.datatypes.Utf8String>(
                                                org.fisco.bcos.web3j.abi.Utils.typeMap(
                                                        _ss,
                                                        org.fisco.bcos.web3j.abi.datatypes
                                                                .Utf8String.class))),
                        Collections.<TypeReference<?>>emptyList());
        asyncExecuteTransaction(function, callback);
    }

    public String setSeq(List<String> _ss) {
        final Function function =
                new Function(
                        FUNC_SET,
                        Arrays.<Type>asList(
                                _ss.isEmpty()
                                        ? org.fisco.bcos.web3j.abi.datatypes.DynamicArray.empty(
                                                "string[]")
                                        : new org.fisco.bcos.web3j.abi.datatypes.DynamicArray<
                                                org.fisco.bcos.web3j.abi.datatypes.Utf8String>(
                                                org.fisco.bcos.web3j.abi.Utils.typeMap(
                                                        _ss,
                                                        org.fisco.bcos.web3j.abi.datatypes
                                                                .Utf8String.class))),
                        Collections.<TypeReference<?>>emptyList());
        return createTransactionSeq(function);
    }

    public Tuple1<List<String>> getSetInput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function =
                new Function(
                        FUNC_SET,
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(
                                new TypeReference<DynamicArray<Utf8String>>() {}));
        List<Type> results = FunctionReturnDecoder.decode(data, function.getOutputParameters());
        ;
        return new Tuple1<List<String>>(
                convertToNative((List<Utf8String>) results.get(0).getValue()));
    }

    public Tuple1<List<String>> getSetOutput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getOutput();
        final Function function =
                new Function(
                        FUNC_SET,
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(
                                new TypeReference<DynamicArray<Utf8String>>() {}));
        List<Type> results = FunctionReturnDecoder.decode(data, function.getOutputParameters());
        ;
        return new Tuple1<List<String>>(
                convertToNative((List<Utf8String>) results.get(0).getValue()));
    }

    public RemoteCall<List> get(List<String> ss) {
        final Function function =
                new Function(
                        FUNC_GET,
                        Arrays.<Type>asList(
                                ss.isEmpty()
                                        ? org.fisco.bcos.web3j.abi.datatypes.DynamicArray.empty(
                                                "string[]")
                                        : new org.fisco.bcos.web3j.abi.datatypes.DynamicArray<
                                                org.fisco.bcos.web3j.abi.datatypes.Utf8String>(
                                                org.fisco.bcos.web3j.abi.Utils.typeMap(
                                                        ss,
                                                        org.fisco.bcos.web3j.abi.datatypes
                                                                .Utf8String.class))),
                        Arrays.<TypeReference<?>>asList(
                                new TypeReference<DynamicArray<Utf8String>>() {}));
        return new RemoteCall<List>(
                new Callable<List>() {
                    @Override
                    @SuppressWarnings("unchecked")
                    public List call() throws Exception {
                        List<Type> result =
                                (List<Type>) executeCallSingleValueReturn(function, List.class);
                        return convertToNative(result);
                    }
                });
    }

    @Deprecated
    public static HelloWeCross load(
            String contractAddress,
            Web3j web3j,
            Credentials credentials,
            BigInteger gasPrice,
            BigInteger gasLimit) {
        return new HelloWeCross(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static HelloWeCross load(
            String contractAddress,
            Web3j web3j,
            TransactionManager transactionManager,
            BigInteger gasPrice,
            BigInteger gasLimit) {
        return new HelloWeCross(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static HelloWeCross load(
            String contractAddress,
            Web3j web3j,
            Credentials credentials,
            ContractGasProvider contractGasProvider) {
        return new HelloWeCross(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static HelloWeCross load(
            String contractAddress,
            Web3j web3j,
            TransactionManager transactionManager,
            ContractGasProvider contractGasProvider) {
        return new HelloWeCross(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<HelloWeCross> deploy(
            Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(
                HelloWeCross.class, web3j, credentials, contractGasProvider, getBinary(), "");
    }

    @Deprecated
    public static RemoteCall<HelloWeCross> deploy(
            Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(
                HelloWeCross.class, web3j, credentials, gasPrice, gasLimit, getBinary(), "");
    }

    public static RemoteCall<HelloWeCross> deploy(
            Web3j web3j,
            TransactionManager transactionManager,
            ContractGasProvider contractGasProvider) {
        return deployRemoteCall(
                HelloWeCross.class,
                web3j,
                transactionManager,
                contractGasProvider,
                getBinary(),
                "");
    }

    @Deprecated
    public static RemoteCall<HelloWeCross> deploy(
            Web3j web3j,
            TransactionManager transactionManager,
            BigInteger gasPrice,
            BigInteger gasLimit) {
        return deployRemoteCall(
                HelloWeCross.class, web3j, transactionManager, gasPrice, gasLimit, getBinary(), "");
    }
}
