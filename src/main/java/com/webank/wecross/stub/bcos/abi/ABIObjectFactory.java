package com.webank.wecross.stub.bcos.abi;

import java.util.List;
import org.fisco.bcos.web3j.abi.datatypes.Address;
import org.fisco.bcos.web3j.abi.datatypes.Bool;
import org.fisco.bcos.web3j.abi.datatypes.Bytes;
import org.fisco.bcos.web3j.abi.datatypes.DynamicBytes;
import org.fisco.bcos.web3j.abi.datatypes.Utf8String;
import org.fisco.bcos.web3j.abi.datatypes.generated.Uint256;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ABIObjectFactory {

    private static final Logger logger = LoggerFactory.getLogger(ABIObjectFactory.class);

    public ABIObject createInputABIObject(ABIDefinition abiDefinition) {
        return createABIObject(
                abiDefinition.getName(), abiDefinition.getType(), abiDefinition.getInputs());
    }

    public ABIObject createOutputABIObject(ABIDefinition abiDefinition) {
        return createABIObject(
                abiDefinition.getName(), abiDefinition.getType(), abiDefinition.getOutputs());
    }

    public ABIObject createABIObject(
            String name, String type, List<ABIDefinition.NamedType> namedTypes) {
        try {
            ABIObject abiObject = new ABIObject(ABIObject.ObjectType.STRUCT);

            for (ABIDefinition.NamedType namedType : namedTypes) {
                abiObject.getStructFields().add(buildArgCodec(namedType));
            }

            logger.info(" name: {}", name);

            return abiObject;

        } catch (Exception e) {
            logger.error("namedTypes: {},  e", namedTypes, e);
        }

        return null;
    }

    public ABIObject buildArgCodec(ABIDefinition.NamedType namedType) {
        try {
            ABIObject codecObject = null;

            String type = namedType.getType();
            String name = namedType.getName();
            // boolean indexed = namedType.isIndexed();

            ABIDefinition.Type typeObj = new ABIDefinition.Type(type);
            String baseType = typeObj.getBaseType();

            if (baseType.startsWith("uint")) {
                codecObject = new ABIObject(new Uint256(0));
            } else if (baseType.startsWith("int")) {
                codecObject = new ABIObject(new Uint256(0));
            } else if (baseType.startsWith("bool")) {
                codecObject = new ABIObject(new Bool(false));
            } else if (baseType.startsWith("string")) {
                codecObject = new ABIObject(new Utf8String(""));
            } else if (baseType.equals("bytes")) {
                // bytes
                codecObject = new ABIObject(new DynamicBytes("".getBytes()));
            } else if (baseType.startsWith("bytes")) {
                // bytes<M>
                codecObject = new ABIObject(new Bytes(32, "".getBytes()));
            } else if (baseType.startsWith("address")) {
                codecObject = new ABIObject(new Address(""));
            } else if (baseType.startsWith("fixed") || baseType.startsWith("ufixed")) {
                throw new UnsupportedOperationException("Unsupported types:" + type);
            } else if (baseType.startsWith("tuple")) {
                codecObject = new ABIObject(ABIObject.ObjectType.STRUCT);

                if (namedType.getComponents() != null && !namedType.getComponents().isEmpty()) {
                    for (int i = 0; i < namedType.getComponents().size(); i++) {
                        ABIObject innerObject = buildArgCodec(namedType.getComponents().get(i));

                        codecObject.getStructFields().add(innerObject);

                        if (innerObject.getType() == ABIObject.ObjectType.LIST
                                || innerObject.isDynamic()) {
                            codecObject.setDynamic(true);
                        }
                    }
                }
            }

            if (typeObj.isList()) {
                // array type
                if (typeObj.isDynamicList()) {
                    ABIObject arrayObject = new ABIObject(ABIObject.ObjectType.LIST);
                    arrayObject.setListValueType(codecObject);
                    codecObject = arrayObject;
                } else {
                    int dimension = typeObj.multiDimension();

                    // TODO: static array support
                    throw new UnsupportedOperationException("Unsupported types:" + type);
                }
            }

            codecObject.setName(name);

            return codecObject;
        } catch (Exception e) {
            logger.error("Error", e);
        }

        return null;
    }
}
