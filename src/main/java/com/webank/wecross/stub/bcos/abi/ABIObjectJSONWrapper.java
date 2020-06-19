package com.webank.wecross.stub.bcos.abi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.stub.bcos.abi.ABIObject.ListType;
import com.webank.wecross.stub.bcos.abi.ABIObject.ObjectType;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.fisco.bcos.web3j.abi.datatypes.Address;
import org.fisco.bcos.web3j.abi.datatypes.Bytes;
import org.fisco.bcos.web3j.abi.datatypes.Utf8String;
import org.fisco.bcos.web3j.abi.datatypes.generated.Uint256;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ABIObjectJSONWrapper {
    private Logger logger = LoggerFactory.getLogger(ABIObjectJSONWrapper.class);
    private ObjectMapper objectMapper = new ObjectMapper();
    private Pattern suffixPattern = Pattern.compile("\\[(\\d*)\\]");

    public static void main(String[] args) {
        Pattern pattern = Pattern.compile("\\[(\\d*)\\]");

        String str = "test";

        Matcher matcher = pattern.matcher(str);

        System.out.println("start...");
        while (matcher.find()) {
            System.out.println("count: " + matcher.group(0));
        }
    }

    public ABIObject buildCodec(JsonNode jsonNode) {
        try {
            ABIObject abiObject = new ABIObject(ObjectType.STRUCT);
            Iterator<JsonNode> iterator = jsonNode.iterator();

            while (iterator.hasNext()) {
                JsonNode argNode = iterator.next();

                abiObject.getStructFields().add(buildArgCodec(argNode));
            }

            return abiObject;
        } catch (Exception e) {
            logger.error("Error", e);
        }

        return null;
    }

    public ABIObject buildArgCodec(JsonNode argNode) {
        try {
            ABIObject codecObject = null;

            String type = argNode.get("type").asText();
            String name = argNode.get("name").asText();

            if (type.startsWith("int")) {
                codecObject = new ABIObject(new Uint256(0));
            } else if (type.startsWith("string")) {
                codecObject = new ABIObject(new Utf8String(""));
            } else if (type.startsWith("bytes")) {
                codecObject = new ABIObject(new Bytes(0, "".getBytes()));
            } else if (type.startsWith("address")) {
                codecObject = new ABIObject(new Address(""));
            } else if (type.startsWith("tuple")) {
                codecObject = new ABIObject(ObjectType.STRUCT);

                JsonNode components = argNode.get("components");
                Iterator<JsonNode> componentsIterator = components.elements();

                while (componentsIterator.hasNext()) {
                    JsonNode innerArg = componentsIterator.next();

                    ABIObject innerObject = buildArgCodec(innerArg);

                    codecObject.getStructFields().add(innerObject);

                    if (innerObject.getType() == ObjectType.LIST || innerObject.isDynamic()) {
                        codecObject.setDynamic(true);
                    }
                }
            }

            boolean isDynamicArray = false;
            List<Integer> lengths = new LinkedList<Integer>();
            Matcher matcher = suffixPattern.matcher(type);
            while (matcher.find()) {
                String lengthStr = matcher.group();
                if (lengthStr.isEmpty()) {
                    isDynamicArray = true;
                    lengths.add(0);
                } else {
                    lengths.add(Integer.parseInt(lengthStr));
                }
            }

            if (lengths.size() > 0 && isDynamicArray) {
                ABIObject arrayObject = new ABIObject(ObjectType.LIST);
                arrayObject.setListValueType(codecObject);

                codecObject = arrayObject;
            } else {
                int total = 1;
                for (Integer length : lengths) {
                    total *= length;
                }
            }

            codecObject.setName(name);

            return codecObject;
        } catch (Exception e) {
            logger.error("Error", e);
        }

        return null;
    }

    public Contract loadABIFile(InputStream inputStream) {
        Contract contract = new Contract();

        try {
            JsonNode jsonNode = objectMapper.readTree(inputStream);

            if (!jsonNode.isArray()) {
                return null;
            }

            Iterator<JsonNode> iterator = jsonNode.elements();
            while (iterator.hasNext()) {
                JsonNode memberNode = iterator.next();

                String type = memberNode.get("type").asText();
                if (type.equals("function")) {
                    Function function = new Function();

                    String name = memberNode.get("name").asText();

                    if (memberNode.has("inputs")) {
                        JsonNode inputNode = memberNode.get("inputs");
                        function.setInput(buildCodec(inputNode));
                    }

                    if (memberNode.has("outputs")) {
                        JsonNode outputNode = memberNode.get("outputs");
                        function.setOutput(buildCodec(outputNode));
                    }

                    List<Function> functions = contract.getFunctions().get(name);
                    if (functions == null) {
                        contract.getFunctions().put(name, new LinkedList<Function>());
                        functions = contract.getFunctions().get(name);
                    }

                    functions.add(function);
                } else if (type.equals("event")) {
                    Event event = new Event();

                    String name = memberNode.get("name").asText();

                    if (memberNode.has("inputs")) {
                        JsonNode inputNode = memberNode.get("inputs");
                        event.setInput(buildCodec(inputNode));
                    }

                    contract.getEvents().put(name, event);
                }
            }
        } catch (IOException e) {
            logger.error("Error", e);
        }

        return contract;
    }

    private void errorReport(String path, String expected, String actual) throws Exception {
        String errorMessage =
                "Arguments mismatch: " + path + ", expected: " + expected + ", actual: " + actual;
        logger.error(errorMessage);
        throw new Exception(errorMessage);
    }

    private ABIObject encodeNode(String path, ABIObject template, JsonNode node) throws Exception {
        ABIObject abiObject = template.newObject();

        switch (abiObject.getType()) {
            case VALUE:
                {
                    if (!node.isValueNode()) {
                        errorReport(
                                path,
                                template.getValueType().toString(),
                                node.getNodeType().toString());
                    }

                    switch (template.getValueType()) {
                        case NUMERIC:
                            {
                                if (!node.isNumber()) {
                                    errorReport(
                                            path,
                                            template.getValueType().toString(),
                                            node.getNodeType().toString());
                                }

                                abiObject.setNumericValue(new Uint256(node.asLong()));

                                break;
                            }
                        case ADDRESS:
                            {
                                if (!node.isBinary()) {
                                    errorReport(
                                            path,
                                            template.getValueType().toString(),
                                            node.getNodeType().toString());
                                }

                                abiObject.setAddressValue(new Address(node.asText()));
                                break;
                            }
                        case BYTES:
                            {
                                if (!node.isBinary()) {
                                    errorReport(
                                            path,
                                            template.getValueType().toString(),
                                            node.getNodeType().toString());
                                }

                                byte[] bytes = node.asText().getBytes();

                                abiObject.setBytesValue(new Bytes(bytes.length, bytes));
                                break;
                            }
                    }
                    break;
                }
            case LIST:
                {
                    if (abiObject.getListType() == ListType.DYNAMIC) {
                        if (!node.isArray()) {
                            errorReport(path, "ARRAY", node.getNodeType().toString());
                        }

                        Iterator<JsonNode> iterator = node.iterator();

                        int i = 0;
                        while (iterator.hasNext()) {
                            JsonNode listNode = iterator.next();

                            abiObject
                                    .getListValues()
                                    .add(
                                            encodeNode(
                                                    path + ".<" + String.valueOf(i) + ">",
                                                    abiObject.getListValueType(),
                                                    listNode));
                        }
                    } else if (abiObject.getListType() == ListType.STRING) {
                        if (!node.isTextual()) {
                            errorReport(path, "STRING", node.getNodeType().toString());
                        }

                        abiObject.setStringValue(new Utf8String(node.asText()));
                    }
                    break;
                }
            case STRUCT:
                {
                    if (!node.isObject()) {
                        errorReport(path, "STRUCT", node.getNodeType().toString());
                    }

                    for (int i = 0; i < abiObject.getStructFields().size(); ++i) {
                        ABIObject field = abiObject.getStructFields().get(i);
                        JsonNode structNode = node.get(field.getName());

                        if (structNode == null) {
                            errorReport(
                                    path,
                                    template.getValueType().toString(),
                                    node.getNodeType().toString());
                        }

                        abiObject
                                .getStructFields()
                                .set(
                                        i,
                                        encodeNode(
                                                path + "." + field.getName(), field, structNode));
                    }

                    break;
                }
        }

        return abiObject;
    }

    public ABIObject encode(ABIObject template, List<String> inputs) throws Exception {
        ABIObject abiObject = template.newObject();

        if (inputs.size() != abiObject.getStructFields().size()) {
            errorReport(
                    "arguments size",
                    String.valueOf(abiObject.getStructFields().size()),
                    String.valueOf(inputs.size()));
        }

        for (int i = 0; i < abiObject.getStructFields().size(); ++i) {
            ABIObject argObject = abiObject.getStructFields().get(i).newObject();
            switch (argObject.getType()) {
                case VALUE:
                    {
                        switch (argObject.getValueType()) {
                            case NUMERIC:
                                {
                                    argObject.setNumericValue(
                                            new Uint256(Long.valueOf(inputs.get(i))));
                                    break;
                                }
                            case ADDRESS:
                                {
                                    argObject.setAddressValue(new Address(inputs.get(i)));
                                    break;
                                }
                            case BYTES:
                                {
                                    byte[] bytes = inputs.get(i).getBytes();
                                    argObject.setBytesValue(new Bytes(bytes.length, bytes));
                                    break;
                                }
                        }
                        break;
                    }
                case STRUCT:
                    {
                        JsonNode argNode = objectMapper.readTree(inputs.get(i).getBytes());
                        ABIObject structObject = encodeNode("ROOT", argObject, argNode);
                        argObject = structObject;
                        break;
                    }
                case LIST:
                    {
                        if (argObject.getListType().equals(ListType.STRING)) {
                            argObject.setStringValue(new Utf8String(inputs.get(i)));
                        } else {
                            JsonNode argNode = objectMapper.readTree(inputs.get(i).getBytes());
                            ABIObject listObject = encodeNode("ROOT", argObject, argNode);
                            argObject = listObject;
                        }
                        break;
                    }
            }

            abiObject.getStructFields().set(i, argObject);
        }

        return abiObject;
    }

    List<String> decode(String buffer) {
        return null;
    }
}
