package com.webank.wecross.stub.bcos.contract;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.fisco.bcos.web3j.abi.FunctionEncoder;
import org.fisco.bcos.web3j.abi.FunctionReturnDecoder;
import org.fisco.bcos.web3j.abi.TypeReference;
import org.fisco.bcos.web3j.abi.Utils;
import org.fisco.bcos.web3j.abi.datatypes.DynamicArray;
import org.fisco.bcos.web3j.abi.datatypes.DynamicBytes;
import org.fisco.bcos.web3j.abi.datatypes.Function;
import org.fisco.bcos.web3j.abi.datatypes.Type;
import org.fisco.bcos.web3j.abi.datatypes.Utf8String;
import org.fisco.bcos.web3j.abi.datatypes.generated.Uint256;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.fisco.bcos.web3j.tuples.generated.Tuple4;
import org.fisco.bcos.web3j.tuples.generated.Tuple5;
import org.fisco.bcos.web3j.utils.Numeric;

/**
 * Function object used across blockchain chain. Wecross requires that a cross-chain contract
 * interface must conform to the following format:
 *
 * <p>function funcName(string[] params) public returns(string[])
 *
 * <p>or
 *
 * <p>function funcName() public returns(string[])
 */
@SuppressWarnings("rawtypes")
public class FunctionUtility {

    public static final int MethodIDLength = 8;
    public static final int MethodIDWithHexPrefixLength = MethodIDLength + 2;

    public static final List<TypeReference<?>> abiTypeReferenceOutputs =
            Collections.singletonList(new TypeReference<DynamicArray<Utf8String>>() {});

    /**
     * Get the function object used to encode and decode the abi parameters
     *
     * @param funcName
     * @param params
     * @return Function
     */
    public static Function newDefaultFunction(String funcName, String[] params) {

        if (Objects.isNull(params)) {
            // public func() returns(string[])
            return new Function(funcName, Arrays.<Type>asList(), abiTypeReferenceOutputs);
        }

        // public func(string[]) returns(string[])
        return new Function(
                funcName,
                Arrays.asList(
                        (0 == params.length)
                                ? DynamicArray.empty("string[]")
                                : new DynamicArray<>(
                                        org.fisco.bcos.web3j.abi.Utils.typeMap(
                                                Arrays.asList(params), Utf8String.class))),
                abiTypeReferenceOutputs);
    }

    /**
     * WeCrossProxy constantCall function function sendTransaction(string memory _name, bytes memory
     * _argsWithMethodId) public returns(bytes memory)
     *
     * @param id
     * @param path
     * @param methodSignature
     * @param abi
     * @return
     */
    public static Function newConstantCallProxyFunction(
            String id, String path, String methodSignature, String abi) {
        Function function =
                new Function(
                        "constantCall",
                        Arrays.<Type>asList(
                                new org.fisco.bcos.web3j.abi.datatypes.Utf8String(id),
                                new org.fisco.bcos.web3j.abi.datatypes.Utf8String(path),
                                new org.fisco.bcos.web3j.abi.datatypes.Utf8String(methodSignature),
                                new org.fisco.bcos.web3j.abi.datatypes.DynamicBytes(
                                        Numeric.hexStringToByteArray(abi))),
                        Collections.<TypeReference<?>>emptyList());
        return function;
    }

    /**
     * WeCrossProxy constantCall function function sendTransaction(string memory _name, bytes memory
     * _argsWithMethodId) public returns(bytes memory)
     *
     * @param name
     * @param methodSignature
     * @param abi
     * @return
     */
    public static Function newConstantCallProxyFunction(
            String name, String methodSignature, String abi) {
        String methodId = FunctionEncoder.buildMethodId(methodSignature);
        Function function =
                new Function(
                        "constantCall",
                        Arrays.<Type>asList(
                                new org.fisco.bcos.web3j.abi.datatypes.Utf8String(name),
                                new org.fisco.bcos.web3j.abi.datatypes.DynamicBytes(
                                        Numeric.hexStringToByteArray(methodId + abi))),
                        Collections.<TypeReference<?>>emptyList());
        return function;
    }

    /**
     * WeCrossProxy sendTransaction function function sendTransaction(string memory _transactionID,
     * uint256 _seq, string memory _path, string memory _func, bytes memory _args) public
     * returns(bytes memory)
     *
     * @param id
     * @param seq
     * @param path
     * @param methodSignature
     * @param abi
     * @return
     */
    public static Function newSendTransactionProxyFunction(
            String id, int seq, String path, String methodSignature, String abi) {
        Function function =
                new Function(
                        "sendTransaction",
                        Arrays.<Type>asList(
                                new org.fisco.bcos.web3j.abi.datatypes.Utf8String(id),
                                new Uint256(seq),
                                new org.fisco.bcos.web3j.abi.datatypes.Utf8String(path.toString()),
                                new org.fisco.bcos.web3j.abi.datatypes.Utf8String(methodSignature),
                                new org.fisco.bcos.web3j.abi.datatypes.DynamicBytes(
                                        Numeric.hexStringToByteArray(abi))),
                        Collections.<TypeReference<?>>emptyList());
        return function;
    }

    /**
     * WeCrossProxy sendTransaction function function sendTransaction(string memory _name, bytes
     * memory _argsWithMethodId) public returns(bytes memory)
     *
     * @param name
     * @param methodSignature
     * @param abi
     * @return
     */
    public static Function newSendTransactionProxyFunction(
            String name, String methodSignature, String abi) {
        String methodId = FunctionEncoder.buildMethodId(methodSignature);
        Function function =
                new Function(
                        "sendTransaction",
                        Arrays.<Type>asList(
                                new org.fisco.bcos.web3j.abi.datatypes.Utf8String(name),
                                new org.fisco.bcos.web3j.abi.datatypes.DynamicBytes(
                                        Numeric.hexStringToByteArray(methodId + abi))),
                        Collections.<TypeReference<?>>emptyList());
        return function;
    }

    /**
     * @param name
     * @param version
     * @param address
     * @param abi
     * @return
     */
    public static Function newCNSInsertFunction(
            String methodName, String name, String version, String address, String abi) {
        Function function =
                new Function(
                        methodName,
                        Arrays.<Type>asList(
                                new Utf8String(name),
                                new Utf8String(version),
                                new Utf8String(address),
                                new Utf8String(abi)),
                        Collections.<TypeReference<?>>emptyList());
        return function;
    }

    /**
     * @param method
     * @param name
     * @param version
     * @return
     */
    public static Function newCNSSelectByNameAndVersionFunction(
            String method, String name, String version) {
        Function function =
                new Function(
                        method,
                        Arrays.<Type>asList(new Utf8String(name), new Utf8String(version)),
                        Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        return function;
    }

    /**
     * @param method
     * @param name
     * @return
     */
    public static Function newCNSSelectByNameFunction(String method, String name) {
        Function function =
                new Function(
                        method,
                        Arrays.<Type>asList(new Utf8String(name)),
                        Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        return function;
    }

    /**
     * decode WeCrossProxy constantCall input
     *
     * @param input
     * @return
     */
    public static Tuple4<String, String, String, byte[]> getConstantCallProxyFunctionInput(
            String input) {
        String data = input.substring(Numeric.containsHexPrefix(input) ? 10 : 8);
        final Function function =
                new Function(
                        "constantCall",
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(
                                new TypeReference<Utf8String>() {},
                                new TypeReference<Utf8String>() {},
                                new TypeReference<Utf8String>() {},
                                new TypeReference<DynamicBytes>() {}));
        List<Type> results = FunctionReturnDecoder.decode(data, function.getOutputParameters());
        ;
        return new Tuple4<String, String, String, byte[]>(
                (String) results.get(0).getValue(),
                (String) results.get(1).getValue(),
                (String) results.get(2).getValue(),
                (byte[]) results.get(3).getValue());
    }

    /**
     * decode WeCrossProxy sendTransaction input
     *
     * @param input
     * @return
     */
    public static Tuple5<String, BigInteger, String, String, byte[]>
            getSendTransactionProxyFunctionInput(String input) {
        String data = input.substring(Numeric.containsHexPrefix(input) ? 10 : 8);

        final Function function =
                new Function(
                        "sendTransaction",
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(
                                new TypeReference<Utf8String>() {},
                                new TypeReference<Uint256>() {},
                                new TypeReference<Utf8String>() {},
                                new TypeReference<Utf8String>() {},
                                new TypeReference<DynamicBytes>() {}));
        List<Type> results = FunctionReturnDecoder.decode(data, function.getOutputParameters());
        ;
        return new Tuple5<String, BigInteger, String, String, byte[]>(
                (String) results.get(0).getValue(),
                (BigInteger) results.get(1).getValue(),
                (String) results.get(2).getValue(),
                (String) results.get(3).getValue(),
                (byte[]) results.get(4).getValue());
    }

    public static List<String> convertToStringList(List<Type> typeList) {
        List<String> stringList = new ArrayList<>();
        if (!typeList.isEmpty()) {
            @SuppressWarnings("unchecked")
            List<Utf8String> utf8StringList = ((DynamicArray) typeList.get(0)).getValue();
            for (Utf8String utf8String : utf8StringList) {
                stringList.add(utf8String.getValue());
            }
        }
        return stringList;
    }

    /**
     * decode TransactionReceipt input field
     *
     * @param receipt
     * @return
     */
    public static String[] decodeInput(TransactionReceipt receipt) {
        if (Objects.isNull(receipt) || Objects.isNull(receipt.getInput())) {
            return null;
        }

        return decodeInput(receipt.getInput());
    }

    /**
     * @param input
     * @return
     */
    public static String[] decodeInput(String input) {
        if (Objects.isNull(input) || input.length() < MethodIDWithHexPrefixLength) {
            return null;
        }

        // function funcName() public returns(string[])
        if (input.length() == MethodIDWithHexPrefixLength) {
            return null;
        }

        return decodeOutput(input.substring(MethodIDWithHexPrefixLength));
    }

    /**
     * decode TransactionReceipt output field
     *
     * @param receipt
     * @return
     */
    public static String[] decodeOutput(TransactionReceipt receipt) {
        if (Objects.isNull(receipt) || !receipt.isStatusOK()) {
            return null;
        }

        return decodeOutput(receipt.getOutput());
    }

    /**
     * decode abi encode data
     *
     * @param output
     * @return
     */
    public static String[] decodeOutput(String output) {
        if (Objects.isNull(output) || output.length() < MethodIDWithHexPrefixLength) {
            return null;
        }

        List<Type> outputTypes =
                FunctionReturnDecoder.decode(
                        output, Utils.convert(FunctionUtility.abiTypeReferenceOutputs));
        List<String> outputArgs = FunctionUtility.convertToStringList(outputTypes);
        return outputArgs.toArray(new String[0]);
    }

    public static String decodeOutputAsString(String output) {
        if (Objects.isNull(output) || output.length() < MethodIDWithHexPrefixLength) {
            return null;
        }

        List<Type> outputTypes =
                FunctionReturnDecoder.decode(
                        output,
                        Utils.convert(
                                Collections.singletonList(new TypeReference<Utf8String>() {})));
        if (Objects.isNull(outputTypes) || outputTypes.isEmpty()) {
            return null;
        }

        return (String) outputTypes.get(0).getValue();
    }
}
