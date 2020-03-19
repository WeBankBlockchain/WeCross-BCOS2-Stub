package com.webank.wecross.stub.bcos.contract;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.fisco.bcos.web3j.abi.TypeReference;
import org.fisco.bcos.web3j.abi.datatypes.DynamicArray;
import org.fisco.bcos.web3j.abi.datatypes.Function;
import org.fisco.bcos.web3j.abi.datatypes.Type;
import org.fisco.bcos.web3j.abi.datatypes.Utf8String;

/**
 * Function object used across blockchain chain. Wecross requires that a cross-chain contract
 * interface must conform to the following format: function funcName(string[] params) public
 * returns(string[])
 */
public class StubFunction {

    public static Function newFunction(String funcName, List<String> params) {
        Function function =
                new Function(
                        funcName,
                        Arrays.asList(
                                params.isEmpty()
                                        ? org.fisco.bcos.web3j.abi.datatypes.DynamicArray.empty(
                                                "string[]")
                                        : new org.fisco.bcos.web3j.abi.datatypes.DynamicArray<
                                                org.fisco.bcos.web3j.abi.datatypes.Utf8String>(
                                                org.fisco.bcos.web3j.abi.Utils.typeMap(
                                                        params,
                                                        org.fisco.bcos.web3j.abi.datatypes
                                                                .Utf8String.class))),
                        Arrays.asList(new TypeReference<DynamicArray<Utf8String>>() {}));
        return function;
    }

    public static List<String> convertToStringList(List<Type> typeList) {
        List<String> stringList = new ArrayList<>();
        if (!typeList.isEmpty()) {
            List<Utf8String> utf8StringList = ((DynamicArray) typeList.get(0)).getValue();
            for (int i = 0; i < utf8StringList.size(); ++i) {
                stringList.add(utf8StringList.get(i).getValue());
            }
        }
        return stringList;
    }
}
