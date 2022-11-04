package com.webank.wecross.stub.bcos.contract;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import org.fisco.bcos.sdk.v3.codec.abi.FunctionEncoder;
import org.fisco.bcos.sdk.v3.codec.abi.FunctionReturnDecoder;
import org.fisco.bcos.sdk.v3.codec.datatypes.Function;
import org.fisco.bcos.sdk.v3.codec.datatypes.Type;
import org.fisco.bcos.sdk.v3.crypto.CryptoSuite;
import org.fisco.bcos.sdk.v3.model.CryptoType;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fisco.bcos.sdk.v3.utils.Hex;
import org.junit.Test;

public class FunctionUtilityTest {

    private static final String[] params = new String[] {"aa", "bb", "cc"};
    private static final String[] emptyParams = new String[0];
    private static final String[] nonParams = null;
    FunctionEncoder functionEncoder = new FunctionEncoder(new CryptoSuite(CryptoType.ECDSA_TYPE));
    FunctionReturnDecoder functionReturnDecoder = new FunctionReturnDecoder();
    private static String funcName = "funcName";
    private static String funcSignature = "funcName(string[])";
    private static String funcNoneParamsSignature = "funcName()";
    private static String funcMethodId;
    private static String funcEmptyParamsMethodId;
    private static String funcNoneParamsMethodId;
    private static Function function = FunctionUtility.newDefaultFunction(funcName, params);
    private static Function emptyParamsFunction =
            FunctionUtility.newDefaultFunction(funcName, emptyParams);
    private static Function noneParamsFunction =
            FunctionUtility.newDefaultFunction(funcName, nonParams);

    @Test
    public void newFunctionTest() throws IOException {
        funcMethodId = Hex.toHexString(functionEncoder.buildMethodId(funcSignature));
        funcEmptyParamsMethodId = Hex.toHexString(functionEncoder.buildMethodId(funcSignature));
        funcNoneParamsMethodId =
                Hex.toHexString(functionEncoder.buildMethodId(funcNoneParamsSignature));
        String abi = Hex.toHexString(functionEncoder.encode(function));
        assertTrue(abi.startsWith(funcMethodId));
        assertEquals(funcName, function.getName());
        assertTrue(abi.startsWith(funcMethodId));
        assertEquals(1, function.getInputParameters().size());
        assertEquals(1, function.getOutputParameters().size());
    }

    @Test
    public void newFunctionWithEmptyParamsTest() throws IOException {
        String abi = Hex.toHexString(functionEncoder.encode(emptyParamsFunction));
        assertEquals(funcName, emptyParamsFunction.getName());
        assertTrue(abi.startsWith(funcEmptyParamsMethodId));
        assertEquals(1, emptyParamsFunction.getInputParameters().size());
        assertEquals(1, emptyParamsFunction.getOutputParameters().size());
    }

    @Test
    public void newFunctionWithNonParamsTest() throws IOException {
        String abi = Hex.toHexString(functionEncoder.encode(noneParamsFunction));
        assertEquals(FunctionUtility.MethodIDWithHexPrefixLength, abi.length());
        assertEquals(funcName, noneParamsFunction.getName());
        assertTrue(abi.startsWith(funcNoneParamsMethodId));
        assertEquals(0, noneParamsFunction.getInputParameters().size());
        assertEquals(1, noneParamsFunction.getOutputParameters().size());
    }

    @Test
    public void convertToStringListTest() throws Exception {
        String abi = Hex.toHexString(functionEncoder.encode(function));
        assertTrue(abi.startsWith(funcMethodId));

        List<Type> typeList =
                functionReturnDecoder.decode(
                        abi.substring(FunctionUtility.MethodIDWithHexPrefixLength),
                        function.getOutputParameters());
        List<String> resultList = FunctionUtility.convertToStringList(typeList);
        assertEquals(resultList.size(), params.length);
        for (int i = 0; i < params.length; i++) {
            assertEquals(params[i], resultList.get(i));
        }
    }

    @Test
    public void emptyParamsConvertToStringListTest() throws IOException {
        Function function = FunctionUtility.newDefaultFunction(funcName, emptyParams);
        String abi = Hex.toHexString(functionEncoder.encode(function));
        assertTrue(abi.startsWith(funcEmptyParamsMethodId));

        assertEquals(funcName, function.getName());

        List<Type> typeList =
                functionReturnDecoder.decode(
                        abi.substring(FunctionUtility.MethodIDWithHexPrefixLength),
                        function.getOutputParameters());
        List<String> resultList = FunctionUtility.convertToStringList(typeList);
        assertTrue(resultList.isEmpty());
    }

    @Test
    public void noneParamsConvertToStringListTest() throws IOException {
        Function function = FunctionUtility.newDefaultFunction(funcName, nonParams);
        String abi = Hex.toHexString(functionEncoder.encode(function));
        assertTrue(abi.startsWith(funcNoneParamsMethodId));
        assertEquals(funcName, function.getName());

        List<Type> typeList =
                functionReturnDecoder.decode(
                        abi.substring(FunctionUtility.MethodIDWithHexPrefixLength),
                        function.getOutputParameters());
        List<String> resultList = FunctionUtility.convertToStringList(typeList);
        assertTrue(resultList.isEmpty());
    }

    @Test
    public void decodeOutputTest() throws IOException {
        assertTrue(Objects.isNull(FunctionUtility.decodeDefaultOutput("0x")));
        assertTrue(Objects.isNull(FunctionUtility.decodeDefaultOutput("")));

        String abi1 = Hex.toHexString(functionEncoder.encode(emptyParamsFunction));

        String[] output1 =
                FunctionUtility.decodeDefaultOutput(
                        "0x" + abi1.substring(FunctionUtility.MethodIDWithHexPrefixLength));
        assertEquals(0, output1.length);

        String abi2 = Hex.toHexString(functionEncoder.encode(function));
        String[] output2 =
                FunctionUtility.decodeDefaultOutput(
                        abi2.substring(FunctionUtility.MethodIDWithHexPrefixLength));
        assertEquals(output2.length, params.length);
        for (int i = 0; i < output2.length; ++i) {
            assertEquals(output2[i], params[i]);
        }

        String abi3 = Hex.toHexString(functionEncoder.encode(noneParamsFunction));
        String[] output3 =
                FunctionUtility.decodeDefaultOutput(
                        abi3.substring(FunctionUtility.MethodIDWithHexPrefixLength));
        assertTrue(Objects.isNull(output3));
    }

    @Test
    public void decodeInputTest() throws IOException {
        assertTrue(Objects.isNull(FunctionUtility.decodeDefaultInput("0x")));

        assertTrue(Objects.isNull(FunctionUtility.decodeDefaultInput("")));

        String abi1 = Hex.toHexString(functionEncoder.encode(emptyParamsFunction));
        String[] input1 = FunctionUtility.decodeDefaultInput(abi1);
        assertEquals(0, input1.length);

        String abi2 = Hex.toHexString(functionEncoder.encode(function));
        String[] input2 = FunctionUtility.decodeDefaultInput(abi2);
        assertEquals(input2.length, params.length);

        for (int i = 0; i < input2.length; ++i) {
            assertEquals(input2[i], params[i]);
        }

        String abi3 = Hex.toHexString(functionEncoder.encode(noneParamsFunction));
        String[] input3 = FunctionUtility.decodeDefaultInput(abi3);
        assertEquals(0, input3.length);
    }

    @Test
    public void decodeTransactionReceiptInputTest() throws IOException {
        TransactionReceipt receipt = new TransactionReceipt();
        receipt.setStatus(1);
        String abi = Hex.toHexStringWithPrefix(functionEncoder.encode(function));
        receipt.setInput(abi);
        receipt.setOutput("0x" + abi.substring(10));
        String[] inputs = FunctionUtility.decodeDefaultInput(receipt);
        assertEquals(inputs.length, params.length);
        String[] outputs = FunctionUtility.decodeDefaultOutput(receipt);
        assertTrue(Objects.isNull(outputs));
    }

    @Test
    public void decodeTransactionReceiptInputTest0() throws IOException {
        TransactionReceipt receipt = new TransactionReceipt();
        receipt.setStatus(0);
        String abi = Hex.toHexStringWithPrefix(functionEncoder.encode(emptyParamsFunction));
        receipt.setInput(abi);
        receipt.setOutput("0x" + abi.substring(10));
        String[] inputs = FunctionUtility.decodeDefaultInput(receipt);
        assertEquals(inputs.length, emptyParams.length);
        String[] outputs = FunctionUtility.decodeDefaultOutput(receipt);
        assertEquals(0, outputs.length);
    }

    @Test
    public void decodeTransactionReceiptInputTest1() throws IOException {
        TransactionReceipt receipt = new TransactionReceipt();
        receipt.setStatus(0);
        String abi = Hex.toHexStringWithPrefix(functionEncoder.encode(noneParamsFunction));
        receipt.setInput(abi);
        receipt.setOutput("0x" + abi.substring(10));
        String[] inputs = FunctionUtility.decodeDefaultInput(receipt);
        assertTrue(Objects.isNull(inputs));
    }

    @Test
    public void decodeTransactionReceiptInputTest2() throws IOException {
        TransactionReceipt receipt = new TransactionReceipt();
        receipt.setStatus(16);

        String funcName = "funcName";
        String[] params = new String[] {"aa", "bb", "cc"};

        Function function = FunctionUtility.newDefaultFunction(funcName, params);
        String abi = Hex.toHexStringWithPrefix(functionEncoder.encode(function));
        receipt.setInput(abi);
        receipt.setOutput("0x" + abi.substring(10));
        String[] inputs = FunctionUtility.decodeDefaultInput(receipt);
        assertEquals(inputs.length, params.length);
        String[] outputs = FunctionUtility.decodeDefaultOutput(receipt);
        assertTrue(Objects.isNull(outputs));
    }
}
