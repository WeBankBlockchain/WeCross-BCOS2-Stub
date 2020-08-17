package com.webank.wecross.stub.bcos.performance.hellowecross;

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
    public static final String[] BINARY_ARRAY = {
        "608060405234801561001057600080fd5b5061088d806100206000396000f300608060405260043610610057576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff168063108c2da11461005c57806352e66db8146100875780636d4ce63c146100c4575b600080fd5b34801561006857600080fd5b506100716100ef565b60405161007e919061072d565b60405180910390f35b34801561009357600080fd5b506100ae60048036036100a99190810190610648565b6101ec565b6040516100bb919061072d565b60405180910390f35b3480156100d057600080fd5b506100d96102ee565b6040516100e6919061072d565b60405180910390f35b6060806000805480602002602001604051908101604052809291908181526020016000905b828210156101d0578382906000526020600020018054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156101bc5780601f10610191576101008083540402835291602001916101bc565b820191906000526020600020905b81548152906001019060200180831161019f57829003601f168201915b505050505081526020019060010190610114565b505050509050600080816101e491906103d7565b508091505090565b60608160009080519060200190610204929190610403565b506000805480602002602001604051908101604052809291908181526020016000905b828210156102e3578382906000526020600020018054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156102cf5780601f106102a4576101008083540402835291602001916102cf565b820191906000526020600020905b8154815290600101906020018083116102b257829003601f168201915b505050505081526020019060010190610227565b505050509050919050565b60606000805480602002602001604051908101604052809291908181526020016000905b828210156103ce578382906000526020600020018054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156103ba5780601f1061038f576101008083540402835291602001916103ba565b820191906000526020600020905b81548152906001019060200180831161039d57829003601f168201915b505050505081526020019060010190610312565b50505050905090565b8154818355818111156103fe578183600052602060002091820191016103fd9190610463565b5b505050565b828054828255906000526020600020908101928215610452579160200282015b8281111561045157825182908051906020019061044192919061048f565b5091602001919060010190610423565b5b50905061045f9190610463565b5090565b61048c91905b80821115610488576000818161047f919061050f565b50600101610469565b5090565b90565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f106104d057805160ff19168380011785556104fe565b828001600101855582156104fe579182015b828111156104fd5782518255916020019190600101906104e2565b5b50905061050b9190610557565b5090565b50805460018160011615610100020316600290046000825580601f106105355750610554565b601f0160209004906000526020600020908101906105539190610557565b5b50565b61057991905b8082111561057557600081600090555060010161055d565b5090565b90565b600082601f830112151561058f57600080fd5b81356105a261059d8261077c565b61074f565b9150818183526020840193506020810190508360005b838110156105e857813586016105ce88826105f2565b8452602084019350602083019250506001810190506105b8565b5050505092915050565b600082601f830112151561060557600080fd5b8135610618610613826107a4565b61074f565b9150808252602083016020830185838301111561063457600080fd5b61063f838284610800565b50505092915050565b60006020828403121561065a57600080fd5b600082013567ffffffffffffffff81111561067457600080fd5b6106808482850161057c565b91505092915050565b6000610694826107dd565b808452602084019350836020820285016106ad856107d0565b60005b848110156106e65783830388526106c88383516106f7565b92506106d3826107f3565b91506020880197506001810190506106b0565b508196508694505050505092915050565b6000610702826107e8565b80845261071681602086016020860161080f565b61071f81610842565b602085010191505092915050565b600060208201905081810360008301526107478184610689565b905092915050565b6000604051905081810181811067ffffffffffffffff8211171561077257600080fd5b8060405250919050565b600067ffffffffffffffff82111561079357600080fd5b602082029050602081019050919050565b600067ffffffffffffffff8211156107bb57600080fd5b601f19601f8301169050602081019050919050565b6000602082019050919050565b600081519050919050565b600081519050919050565b6000602082019050919050565b82818337600083830152505050565b60005b8381101561082d578082015181840152602081019050610812565b8381111561083c576000848401525b50505050565b6000601f19601f83011690509190505600a265627a7a72305820b875a605260ef55b52f0920a5b0d70ec4e917b562227b08ff809fd2987bd76696c6578706572696d656e74616cf50037"
    };

    public static final String BINARY = String.join("", BINARY_ARRAY);

    public static final String[] ABI_ARRAY = {
        "[{\"constant\":false,\"inputs\":[],\"name\":\"getAndClear\",\"outputs\":[{\"name\":\"\",\"type\":\"string[]\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_ss\",\"type\":\"string[]\"}],\"name\":\"set\",\"outputs\":[{\"name\":\"\",\"type\":\"string[]\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"get\",\"outputs\":[{\"name\":\"\",\"type\":\"string[]\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"}]"
    };

    public static final String ABI = String.join("", ABI_ARRAY);

    public static final TransactionDecoder transactionDecoder = new TransactionDecoder(ABI, BINARY);

    public static final String[] SM_BINARY_ARRAY = {
        "608060405234801561001057600080fd5b5061088d806100206000396000f300608060405260043610610057576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff16806305f505be1461005c578063299f7f9d14610087578063656fa89a146100b2575b600080fd5b34801561006857600080fd5b506100716100ef565b60405161007e919061072d565b60405180910390f35b34801561009357600080fd5b5061009c6101ec565b6040516100a9919061072d565b60405180910390f35b3480156100be57600080fd5b506100d960048036036100d49190810190610648565b6102d5565b6040516100e6919061072d565b60405180910390f35b6060806000805480602002602001604051908101604052809291908181526020016000905b828210156101d0578382906000526020600020018054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156101bc5780601f10610191576101008083540402835291602001916101bc565b820191906000526020600020905b81548152906001019060200180831161019f57829003601f168201915b505050505081526020019060010190610114565b505050509050600080816101e491906103d7565b508091505090565b60606000805480602002602001604051908101604052809291908181526020016000905b828210156102cc578382906000526020600020018054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156102b85780601f1061028d576101008083540402835291602001916102b8565b820191906000526020600020905b81548152906001019060200180831161029b57829003601f168201915b505050505081526020019060010190610210565b50505050905090565b606081600090805190602001906102ed929190610403565b506000805480602002602001604051908101604052809291908181526020016000905b828210156103cc578382906000526020600020018054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156103b85780601f1061038d576101008083540402835291602001916103b8565b820191906000526020600020905b81548152906001019060200180831161039b57829003601f168201915b505050505081526020019060010190610310565b505050509050919050565b8154818355818111156103fe578183600052602060002091820191016103fd9190610463565b5b505050565b828054828255906000526020600020908101928215610452579160200282015b8281111561045157825182908051906020019061044192919061048f565b5091602001919060010190610423565b5b50905061045f9190610463565b5090565b61048c91905b80821115610488576000818161047f919061050f565b50600101610469565b5090565b90565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f106104d057805160ff19168380011785556104fe565b828001600101855582156104fe579182015b828111156104fd5782518255916020019190600101906104e2565b5b50905061050b9190610557565b5090565b50805460018160011615610100020316600290046000825580601f106105355750610554565b601f0160209004906000526020600020908101906105539190610557565b5b50565b61057991905b8082111561057557600081600090555060010161055d565b5090565b90565b600082601f830112151561058f57600080fd5b81356105a261059d8261077c565b61074f565b9150818183526020840193506020810190508360005b838110156105e857813586016105ce88826105f2565b8452602084019350602083019250506001810190506105b8565b5050505092915050565b600082601f830112151561060557600080fd5b8135610618610613826107a4565b61074f565b9150808252602083016020830185838301111561063457600080fd5b61063f838284610800565b50505092915050565b60006020828403121561065a57600080fd5b600082013567ffffffffffffffff81111561067457600080fd5b6106808482850161057c565b91505092915050565b6000610694826107dd565b808452602084019350836020820285016106ad856107d0565b60005b848110156106e65783830388526106c88383516106f7565b92506106d3826107f3565b91506020880197506001810190506106b0565b508196508694505050505092915050565b6000610702826107e8565b80845261071681602086016020860161080f565b61071f81610842565b602085010191505092915050565b600060208201905081810360008301526107478184610689565b905092915050565b6000604051905081810181811067ffffffffffffffff8211171561077257600080fd5b8060405250919050565b600067ffffffffffffffff82111561079357600080fd5b602082029050602081019050919050565b600067ffffffffffffffff8211156107bb57600080fd5b601f19601f8301169050602081019050919050565b6000602082019050919050565b600081519050919050565b600081519050919050565b6000602082019050919050565b82818337600083830152505050565b60005b8381101561082d578082015181840152602081019050610812565b8381111561083c576000848401525b50505050565b6000601f19601f83011690509190505600a265627a7a7230582086c36eabdba040db28b78a7c7ce8157509d74b9c3e013a5add1c4a56692bcef96c6578706572696d656e74616cf50037"
    };

    public static final String SM_BINARY = String.join("", SM_BINARY_ARRAY);

    public static final String FUNC_GETANDCLEAR = "getAndClear";

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

    public RemoteCall<TransactionReceipt> getAndClear() {
        final Function function =
                new Function(
                        FUNC_GETANDCLEAR,
                        Arrays.<Type>asList(),
                        Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public void getAndClear(TransactionSucCallback callback) {
        final Function function =
                new Function(
                        FUNC_GETANDCLEAR,
                        Arrays.<Type>asList(),
                        Collections.<TypeReference<?>>emptyList());
        asyncExecuteTransaction(function, callback);
    }

    public String getAndClearSeq() {
        final Function function =
                new Function(
                        FUNC_GETANDCLEAR,
                        Arrays.<Type>asList(),
                        Collections.<TypeReference<?>>emptyList());
        return createTransactionSeq(function);
    }

    public Tuple1<List<String>> getGetAndClearOutput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getOutput();
        final Function function =
                new Function(
                        FUNC_GETANDCLEAR,
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(
                                new TypeReference<DynamicArray<Utf8String>>() {}));
        List<Type> results = FunctionReturnDecoder.decode(data, function.getOutputParameters());
        ;
        return new Tuple1<List<String>>(
                convertToNative((List<Utf8String>) results.get(0).getValue()));
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

    public RemoteCall<List> get() {
        final Function function =
                new Function(
                        FUNC_GET,
                        Arrays.<Type>asList(),
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
