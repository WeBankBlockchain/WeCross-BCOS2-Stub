package com.webank.wecross.stub.bcos.contract;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.fisco.bcos.web3j.abi.FunctionReturnDecoder;
import org.fisco.bcos.web3j.abi.TypeReference;
import org.fisco.bcos.web3j.abi.Utils;
import org.fisco.bcos.web3j.abi.datatypes.DynamicArray;
import org.fisco.bcos.web3j.abi.datatypes.Function;
import org.fisco.bcos.web3j.abi.datatypes.Type;
import org.fisco.bcos.web3j.abi.datatypes.Utf8String;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;

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
    public static Function newFunction(String funcName, String[] params) {

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
