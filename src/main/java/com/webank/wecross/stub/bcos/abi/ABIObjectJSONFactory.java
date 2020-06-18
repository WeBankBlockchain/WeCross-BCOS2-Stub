package com.webank.wecross.stub.bcos.abi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.stub.bcos.abi.ABIObject.ObjectType;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.fisco.bcos.web3j.abi.datatypes.Address;
import org.fisco.bcos.web3j.abi.datatypes.Bytes;
import org.fisco.bcos.web3j.abi.datatypes.Utf8String;
import org.fisco.bcos.web3j.abi.datatypes.generated.Uint256;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ABIObjectJSONFactory {
    private Logger logger = LoggerFactory.getLogger(ABIObjectJSONFactory.class);
    private ObjectMapper objectMapper = new ObjectMapper();

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

            if (type.endsWith("[]")) {
                ABIObject arrayObject = new ABIObject(ObjectType.LIST);
                arrayObject.setListValueType(codecObject);

                codecObject = arrayObject;
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

    /*
    public static void main(String[] args) {
        ABIObject input1 = new ABIObject(new Uint256(100));
        ABIObject input2 = new ABIObject(ObjectType.LIST);

        ABIObject item1 = new ABIObject(ObjectType.STRUCT);
        item1.setDynamic(true);
        item1.getStructFields().add(new ABIObject(new Utf8String("Hello world!")));
        item1.getStructFields().add(new ABIObject(new Uint256(100)));
        item1.getStructFields().add(new ABIObject(ObjectType.LIST));

        item1.getStructFields().get(2).getListValues().add(new ABIObject(ObjectType.STRUCT));
        item1.getStructFields().get(2).getListValues().get(0).setDynamic(false);

        item1.getStructFields()
                .get(2)
                .getListValues()
                .get(0)
                .getStructFields()
                .add(new ABIObject(new Uint256(1)));
        item1.getStructFields()
                .get(2)
                .getListValues()
                .get(0)
                .getStructFields()
                .add(new ABIObject(new Uint256(2)));
        item1.getStructFields()
                .get(2)
                .getListValues()
                .get(0)
                .getStructFields()
                .add(new ABIObject(new Uint256(3)));

        item1.getStructFields()
                .get(2)
                .setListValueType(item1.getStructFields().get(2).getListValues().get(0));

        input2.getListValues().add(item1);
        input2.setListValueType(item1);

        ABIObject item2 = new ABIObject(ObjectType.STRUCT);
        item2.setDynamic(true);
        item2.getStructFields().add(new ABIObject(new Utf8String("Hello world2")));
        item2.getStructFields().add(new ABIObject(new Uint256(200)));
        item2.getStructFields().add(new ABIObject(ObjectType.LIST));

        item2.getStructFields().get(2).getListValues().add(new ABIObject(ObjectType.STRUCT));

        item2.getStructFields()
                .get(2)
                .getListValues()
                .get(0)
                .getStructFields()
                .add(new ABIObject(new Uint256(5)));
        item2.getStructFields()
                .get(2)
                .getListValues()
                .get(0)
                .getStructFields()
                .add(new ABIObject(new Uint256(6)));
        item2.getStructFields()
                .get(2)
                .getListValues()
                .get(0)
                .getStructFields()
                .add(new ABIObject(new Uint256(7)));

        item1.getStructFields()
                .get(2)
                .setListValueType(item2.getStructFields().get(2).getListValues().get(0));

        input2.getListValues().add(item2);

        ABIObject inputs = new ABIObject(ObjectType.STRUCT);
        inputs.setDynamic(false);
        inputs.setStructFields(new ArrayList<ABIObject>());

        inputs.getStructFields().add(input1);
        inputs.getStructFields().add(input2);
        inputs.getStructFields().add(new ABIObject(new Utf8String("Hello world!")));

        String result = inputs.encode();

        for (int i = 0; i < result.length(); ++i) {
            System.out.print(result.charAt(i));
            if ((i > 0) && ((i + 1) % 64 == 0)) {
                System.out.println("");
            }
        }

        System.out.println("--------------------------------");

        ABIObject decodeResult = inputs.decode(result);

        String result2 = decodeResult.encode();

        for (int i = 0; i < result2.length(); ++i) {
            System.out.print(result2.charAt(i));
            if ((i > 0) && ((i + 1) % 64 == 0)) {
                System.out.println("");
            }
        }

        // System.out.println("result2: " + result2);
    }
    */
}
