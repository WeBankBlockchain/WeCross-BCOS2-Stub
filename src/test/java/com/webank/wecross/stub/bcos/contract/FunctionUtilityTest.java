package com.webank.wecross.stub.bcos.contract;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.fisco.bcos.web3j.abi.FunctionEncoder;
import org.fisco.bcos.web3j.abi.FunctionReturnDecoder;
import org.fisco.bcos.web3j.abi.datatypes.Function;
import org.fisco.bcos.web3j.abi.datatypes.Type;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.junit.Test;

public class FunctionUtilityTest {
    @Test
    public void newFunctionTest() throws IOException {
        String funcName = "funcName";
        List<String> params = Arrays.asList("aa", "bb", "cc");
        Function function = FunctionUtility.newFunction(funcName, params);
        String abi = FunctionEncoder.encode(function);
        assertFalse(abi.isEmpty());
        assertTrue(funcName.equals(function.getName()));
        assertTrue(function.getInputParameters().size() == 1);
        assertTrue(function.getOutputParameters().size() == 1);
    }

    @Test
    public void newFunctionWithEmptyParamsTest() throws IOException {
        String funcName = "funcName";
        List<String> params = Arrays.asList();
        Function function = FunctionUtility.newFunction(funcName, params);
        String abi = FunctionEncoder.encode(function);
        assertFalse(abi.length() == 10);
        assertTrue(funcName.equals(function.getName()));
        assertTrue(function.getInputParameters().size() == 1);
        assertTrue(function.getOutputParameters().size() == 1);
    }

    @Test
    public void convertToStringListTest() throws IOException {
        String funcName = "funcName";
        List<String> params = Arrays.asList("aaa", "bbb", "ccc");
        Function function = FunctionUtility.newFunction(funcName, params);
        String abi = FunctionEncoder.encode(function);

        List<Type> typeList =
                FunctionReturnDecoder.decode(abi.substring(10), function.getOutputParameters());
        List<String> resultList = FunctionUtility.convertToStringList(typeList);
        assertTrue(resultList.size() == params.size());
        for (int i = 0; i < params.size(); i++) {
            assertEquals(params.get(i), resultList.get(i));
        }
    }

    @Test
    public void convertToStringListTest1() throws IOException {
        String funcName = "funcName";
        List<String> params = Arrays.asList();
        Function function = FunctionUtility.newFunction(funcName, params);
        String abi = FunctionEncoder.encode(function);
        assertTrue(funcName.equals(function.getName()));

        List<Type> typeList =
                FunctionReturnDecoder.decode(abi.substring(10), function.getOutputParameters());
        List<String> resultList = FunctionUtility.convertToStringList(typeList);
        assertTrue(resultList.isEmpty());
    }

    @Test
    public void decodeOutputTest() throws IOException {
        String[] output = FunctionUtility.decodeOutput("0x");
        assertTrue(output.length == 0);

        String[] output0 = FunctionUtility.decodeOutput("");
        assertTrue(output0.length == 0);

        String funcName = "funcName";
        List<String> params0 = Arrays.asList();
        Function function0 = FunctionUtility.newFunction("funcName", Arrays.asList());
        String abi0 = FunctionEncoder.encode(function0);

        String[] output1 = FunctionUtility.decodeOutput(abi0.substring(10));
        assertTrue(output1.length == params0.size());
        for (int i = 0; i < output1.length; ++i) {
            assertEquals(output1[i], params0.get(i));
        }

        List<String> params1 = Arrays.asList("aa", "bb", "cc");
        Function function1 = FunctionUtility.newFunction(funcName, params1);
        String abi1 = FunctionEncoder.encode(function1);

        String[] output2 = FunctionUtility.decodeOutput(abi1.substring(10));
        assertTrue(output2.length == params1.size());
        for (int i = 0; i < output2.length; ++i) {
            assertEquals(output2[i], params1.get(i));
        }
    }

    @Test
    public void decodeInputTest() throws IOException {
        String[] input = FunctionUtility.decodeInput("0x");
        assertTrue(input.length == 0);

        String[] input0 = FunctionUtility.decodeInput("");
        assertTrue(input0.length == 0);

        String funcName = "funcName";
        List<String> params0 = Arrays.asList();
        Function function0 = FunctionUtility.newFunction(funcName, params0);
        String abi0 = FunctionEncoder.encode(function0);

        String[] input1 = FunctionUtility.decodeInput(abi0);
        assertTrue(input1.length == params0.size());
        for (int i = 0; i < input1.length; ++i) {
            assertEquals(input1[i], params0.get(i));
        }

        List<String> params1 = Arrays.asList("aa", "bb", "cc");
        Function function1 = FunctionUtility.newFunction(funcName, params1);
        String abi1 = FunctionEncoder.encode(function1);

        String[] input2 = FunctionUtility.decodeInput(abi1);
        assertTrue(input2.length == params1.size());
        for (int i = 0; i < input2.length; ++i) {
            assertEquals(input2[i], params1.get(i));
        }
    }

    @Test
    public void decodeTransactionReceiptInputTest() throws IOException {
        TransactionReceipt receipt = new TransactionReceipt();
        receipt.setStatus("0x1");

        String funcName = "funcName";
        List<String> params = Arrays.asList();
        Function function = FunctionUtility.newFunction(funcName, params);
        String abi = FunctionEncoder.encode(function);
        receipt.setInput(abi);
        receipt.setOutput("0x" + abi.substring(10));
        String[] inputs = FunctionUtility.decodeInput(receipt);
        assertTrue(inputs.length == params.size());
        String[] outputs = FunctionUtility.decodeOutput(receipt);
        assertTrue(outputs.length == 0);
    }

    @Test
    public void decodeTransactionReceiptInputTest1() throws IOException {
        TransactionReceipt receipt = new TransactionReceipt();
        receipt.setStatus("0x0");

        String funcName = "funcName";
        List<String> params = Arrays.asList("aa", "bb", "cc");

        Function function = FunctionUtility.newFunction(funcName, params);
        String abi = FunctionEncoder.encode(function);
        receipt.setInput(abi);
        receipt.setOutput("0x" + abi.substring(10));
        String[] inputs = FunctionUtility.decodeInput(receipt);
        assertTrue(inputs.length == params.size());
        String[] outputs = FunctionUtility.decodeOutput(receipt);
        assertTrue(outputs.length == params.size());
    }

    @Test
    public void decodeTransactionReceiptInputTest2() throws IOException {
        TransactionReceipt receipt = new TransactionReceipt();
        receipt.setStatus("0x16");

        String funcName = "funcName";
        List<String> params = Arrays.asList("aa", "bb", "cc");

        Function function = FunctionUtility.newFunction(funcName, params);
        String abi = FunctionEncoder.encode(function);
        receipt.setInput(abi);
        receipt.setOutput("0x" + abi.substring(10));
        String[] inputs = FunctionUtility.decodeInput(receipt);
        assertTrue(inputs.length == params.size());
        String[] outputs = FunctionUtility.decodeOutput(receipt);
        assertTrue(outputs.length == 0);
    }
}
