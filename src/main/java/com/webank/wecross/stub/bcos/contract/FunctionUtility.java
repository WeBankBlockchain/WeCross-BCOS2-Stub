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
import org.fisco.bcos.web3j.utils.Numeric;

/**
 * Function object used across blockchain chain. Wecross requires that a cross-chain contract
 * interface must conform to the following format:
 *
 * <p>function funcName(string[] params) public returns(string[])
 */
@SuppressWarnings("rawtypes")
public class FunctionUtility {

    private FunctionUtility() {}

    public static final List<TypeReference<?>> abiTypeReferenceOutputs =
            Collections.singletonList(new TypeReference<DynamicArray<Utf8String>>() {});

    /**
     * Get the function object used to encode and decode the abi parameters
     *
     * @param funcName
     * @param params
     * @return Function
     */
    public static Function newFunction(String funcName, List<String> params) {
        return new Function(
                funcName,
                Arrays.asList(
                        (Objects.isNull(params) || params.isEmpty())
                                ? DynamicArray.empty("string[]")
                                : new DynamicArray<>(
                                        org.fisco.bcos.web3j.abi.Utils.typeMap(
                                                params, Utf8String.class))),
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
        if (Objects.isNull(input)
                || "".equals(input)
                || input.length() <= 8
                || (Numeric.containsHexPrefix(input) && input.length() <= 10)) {
            return null;
        }

        if (Numeric.containsHexPrefix(input)) {
            return decodeOutput(input.substring(10));
        }

        return decodeOutput(input.substring(8));
    }

    /**
     * decode TransactionReceipt output field
     *
     * @param receipt
     * @return
     */
    public static String[] decodeOutput(TransactionReceipt receipt) {
        if (Objects.isNull(receipt)
                || Objects.isNull(receipt.getOutput())
                || !receipt.isStatusOK()) {
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
        List<Type> outputTypes =
                FunctionReturnDecoder.decode(
                        output, Utils.convert(FunctionUtility.abiTypeReferenceOutputs));
        List<String> outputArgs = FunctionUtility.convertToStringList(outputTypes);
        return outputArgs.toArray(new String[0]);
    }
}
