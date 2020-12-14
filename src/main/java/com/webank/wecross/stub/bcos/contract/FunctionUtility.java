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
import org.fisco.bcos.web3j.tuples.generated.*;
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

    public static final String ProxySendTXMethodId =
            FunctionEncoder.buildMethodId("sendTransaction(string,string,bytes)");

    public static final String ProxySendTransactionTXMethodId =
            FunctionEncoder.buildMethodId(
                    "sendTransaction(string,string,uint256,string,string,bytes)");

    public static final String ProxyCallWithTransactionIdMethodId =
            FunctionEncoder.buildMethodId("constantCall(string,string,string,bytes)");

    public static final String ProxyCallMethodId =
            FunctionEncoder.buildMethodId("constantCall(string,bytes)");

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
     * WeCrossProxy constantCall function <br>
     * </>function sendTransaction(string memory _name, bytes memory _argsWithMethodId) public
     * returns(bytes memory)
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
     * @param uid
     * @param tid
     * @param seq
     * @param path
     * @param methodSignature
     * @param abi
     * @return
     */
    public static Function newSendTransactionProxyFunction(
            String uid, String tid, long seq, String path, String methodSignature, String abi) {
        Function function =
                new Function(
                        "sendTransaction",
                        Arrays.<Type>asList(
                                new org.fisco.bcos.web3j.abi.datatypes.Utf8String(uid),
                                new org.fisco.bcos.web3j.abi.datatypes.Utf8String(tid),
                                new Uint256(seq),
                                new org.fisco.bcos.web3j.abi.datatypes.Utf8String(path),
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
     * @param uid
     * @param name
     * @param methodSignature
     * @param abi
     * @return
     */
    public static Function newSendTransactionProxyFunction(
            String uid, String name, String methodSignature, String abi) {
        String methodId = FunctionEncoder.buildMethodId(methodSignature);
        Function function =
                new Function(
                        "sendTransaction",
                        Arrays.<Type>asList(
                                new org.fisco.bcos.web3j.abi.datatypes.Utf8String(uid),
                                new org.fisco.bcos.web3j.abi.datatypes.Utf8String(name),
                                new org.fisco.bcos.web3j.abi.datatypes.DynamicBytes(
                                        Numeric.hexStringToByteArray(methodId + abi))),
                        Collections.<TypeReference<?>>emptyList());
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

        return new Tuple4<String, String, String, byte[]>(
                (String) results.get(0).getValue(),
                (String) results.get(1).getValue(),
                (String) results.get(2).getValue(),
                (byte[]) results.get(3).getValue());
    }

    /**
     * decode WeCrossProxy constantCall input
     *
     * @param input
     * @return
     */
    public static Tuple2<String, byte[]> getConstantCallFunctionInput(String input) {
        String data = input.substring(Numeric.containsHexPrefix(input) ? 10 : 8);
        final Function function =
                new Function(
                        "constantCall",
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(
                                new TypeReference<Utf8String>() {},
                                new TypeReference<DynamicBytes>() {}));
        List<Type> results = FunctionReturnDecoder.decode(data, function.getOutputParameters());

        return new Tuple2<String, byte[]>(
                (String) results.get(0).getValue(), (byte[]) results.get(1).getValue());
    }

    /**
     * decode WeCrossProxy sendTransaction input
     *
     * @param input
     * @return
     */
    public static Tuple6<String, String, BigInteger, String, String, byte[]>
            getSendTransactionProxyFunctionInput(String input) {
        String data = input.substring(Numeric.containsHexPrefix(input) ? 10 : 8);

        final Function function =
                new Function(
                        "sendTransaction",
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(
                                new TypeReference<Utf8String>() {},
                                new TypeReference<Utf8String>() {},
                                new TypeReference<Uint256>() {},
                                new TypeReference<Utf8String>() {},
                                new TypeReference<Utf8String>() {},
                                new TypeReference<DynamicBytes>() {}));
        List<Type> results = FunctionReturnDecoder.decode(data, function.getOutputParameters());

        return new Tuple6<>(
                (String) results.get(0).getValue(),
                (String) results.get(1).getValue(),
                (BigInteger) results.get(2).getValue(),
                (String) results.get(3).getValue(),
                (String) results.get(4).getValue(),
                (byte[]) results.get(5).getValue());
    }

    /**
     * decode WeCrossProxy sendTransaction input
     *
     * @param input
     * @return
     */
    public static Tuple3<String, String, byte[]> getSendTransactionProxyWithoutTxIdFunctionInput(
            String input) {
        String data = input.substring(Numeric.containsHexPrefix(input) ? 10 : 8);

        final Function function =
                new Function(
                        "sendTransaction",
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(
                                new TypeReference<Utf8String>() {},
                                new TypeReference<Utf8String>() {},
                                new TypeReference<DynamicBytes>() {}));
        List<Type> results = FunctionReturnDecoder.decode(data, function.getOutputParameters());

        return new Tuple3<>(
                (String) results.get(0).getValue(),
                (String) results.get(1).getValue(),
                (byte[]) results.get(2).getValue());
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
    public static String[] decodeDefaultInput(TransactionReceipt receipt) {
        if (Objects.isNull(receipt) || Objects.isNull(receipt.getInput())) {
            return null;
        }

        return decodeDefaultInput(receipt.getInput());
    }

    /**
     * @param input
     * @return
     */
    public static String[] decodeDefaultInput(String input) {
        if (Objects.isNull(input) || input.length() < MethodIDWithHexPrefixLength) {
            return null;
        }

        // function funcName() public returns(string[])
        if (input.length() == MethodIDWithHexPrefixLength) {
            return null;
        }

        return decodeDefaultOutput(input.substring(MethodIDWithHexPrefixLength));
    }

    /**
     * decode TransactionReceipt output field
     *
     * @param receipt
     * @return
     */
    public static String[] decodeDefaultOutput(TransactionReceipt receipt) {
        if (Objects.isNull(receipt) || !receipt.isStatusOK()) {
            return null;
        }

        return decodeDefaultOutput(receipt.getOutput());
    }

    /**
     * decode abi encode data
     *
     * @param output
     * @return
     */
    public static String[] decodeDefaultOutput(String output) {
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
