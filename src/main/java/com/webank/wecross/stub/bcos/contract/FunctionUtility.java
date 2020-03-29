package com.webank.wecross.stub.bcos.contract;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.fisco.bcos.web3j.abi.TypeReference;
import org.fisco.bcos.web3j.abi.datatypes.DynamicArray;
import org.fisco.bcos.web3j.abi.datatypes.Function;
import org.fisco.bcos.web3j.abi.datatypes.Type;
import org.fisco.bcos.web3j.abi.datatypes.Utf8String;

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

    public static Function newFunction(String funcName, List<String> params) {
        return new Function(
                funcName,
                Arrays.asList(
                        params.isEmpty()
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
}
