package com.webank.wecross.stub.bcos.abi;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.fisco.bcos.web3j.abi.TypeDecoder;
import org.fisco.bcos.web3j.abi.TypeEncoder;
import org.fisco.bcos.web3j.abi.TypeReference;
import org.fisco.bcos.web3j.abi.datatypes.Address;
import org.fisco.bcos.web3j.abi.datatypes.Bytes;
import org.fisco.bcos.web3j.abi.datatypes.NumericType;
import org.fisco.bcos.web3j.abi.datatypes.StaticArray;
import org.fisco.bcos.web3j.abi.datatypes.Utf8String;
import org.fisco.bcos.web3j.abi.datatypes.generated.Bytes32;
import org.fisco.bcos.web3j.abi.datatypes.generated.Uint256;

public class ABIObject {
    public enum ObjectType {
        VALUE,
        STRUCT,
        LIST,
    };

    public enum ValueType {
        NUMERIC,
        BYTES,
        ADDRESS,
    }

    public enum ListType {
        DYNAMIC,
        FIXED,
        STRING,
    }

    private ObjectType type;
    private String name; // field name

    private ValueType valueType; // for value
    private NumericType numericValue;
    private Bytes bytesValue;
    private Address addressValue;

    private boolean isDynamicStruct;
    private List<ABIObject> structFields; // for struct

    private int listLength = 0; // >0 for string
    private ListType listType;
    private ABIObject listValueType; // for list
    private List<ABIObject> listValues; // for list

    public ABIObject(ObjectType type) {
        this.type = type;
        switch (type) {
            case VALUE:
                {
                    break;
                }
            case STRUCT:
                {
                    structFields = new LinkedList<ABIObject>();
                    break;
                }
            case LIST:
                {
                    listValues = new LinkedList<ABIObject>();
                    listType = ListType.DYNAMIC;
                    break;
                }
        }
    }

    public ABIObject(NumericType number) {
        this.type = ObjectType.VALUE;
        this.valueType = ValueType.NUMERIC;
        this.numericValue = number;
    }

    public ABIObject(Bytes bytes) {
        this.type = ObjectType.VALUE;
        this.valueType = ValueType.BYTES;
        this.bytesValue = bytes;
    }

    public ABIObject(Address address) {
        this.type = ObjectType.VALUE;
        this.valueType = ValueType.ADDRESS;
        this.addressValue = address;
    }

    public ABIObject(Utf8String string) {
        setStringValue(string);
    }

    // clone itself
    public ABIObject newObject() {
        ABIObject abiObject = new ABIObject(this.type);
        abiObject.valueType = this.valueType;

        abiObject.name = this.name;
        abiObject.valueType = this.valueType;
        if (this.numericValue != null) {
            abiObject.numericValue = new Uint256(this.numericValue.getValue());
        }

        if (this.bytesValue != null) {
            abiObject.bytesValue =
                    new Bytes(this.bytesValue.getValue().length, this.bytesValue.getValue());
        }
        if (this.addressValue != null) {
            abiObject.addressValue = new Address(this.addressValue.toUint160());
        }
        abiObject.isDynamicStruct = this.isDynamicStruct;

        if (this.structFields != null) {
            for (ABIObject obj : this.structFields) {
                abiObject.structFields.add(obj.newObject());
            }
        }

        abiObject.listLength = this.listLength;
        abiObject.listType = this.listType;
        if (this.listValueType != null) {
            abiObject.listValueType = this.listValueType.newObject();
        }

        if (this.listValues != null) {
            for (ABIObject obj : this.listValues) {
                abiObject.listValues.add(obj.newObject());
            }
        }

        return abiObject;
    }

    public String encode() {
        StringBuffer fixedBuffer = new StringBuffer();
        StringBuffer dynamicBuffer = new StringBuffer();

        switch (type) {
            case VALUE:
                {
                    switch (valueType) {
                        case NUMERIC:
                            {
                                fixedBuffer.append(TypeEncoder.encode(numericValue));
                                break;
                            }
                        case BYTES:
                            {
                                fixedBuffer.append(TypeEncoder.encode(bytesValue));
                                break;
                            }
                        case ADDRESS:
                            {
                                fixedBuffer.append(TypeEncoder.encode(addressValue));
                                break;
                            }
                    }
                    break;
                }
            case STRUCT:
                {
                    for (ABIObject abiObject : structFields) {
                        if (abiObject.isDynamicStruct || abiObject.type.equals(ObjectType.LIST)) {
                            Uint256 offsetNumber =
                                    new Uint256(
                                            32 * structFields.size() + dynamicBuffer.length() / 2);
                            fixedBuffer.append(TypeEncoder.encode(offsetNumber));

                            dynamicBuffer.append(abiObject.encode());
                        } else {
                            fixedBuffer.append(abiObject.encode());
                        }
                    }

                    break;
                }
            case LIST:
                {
                    if (!listType.equals(ListType.FIXED)) {
                        Uint256 length = null;
                        if (listType.equals(ListType.STRING)) {
                            length = new Uint256(listLength);
                        } else {
                            length = new Uint256(listValues.size());
                        }
                        fixedBuffer.append(TypeEncoder.encode(length));
                    }

                    for (ABIObject abiObject : listValues) {
                        if (abiObject.isDynamicStruct || abiObject.type.equals(ObjectType.LIST)) {
                            Uint256 offsetNumber =
                                    new Uint256(
                                            32 * listValues.size() + dynamicBuffer.length() / 2);
                            fixedBuffer.append(TypeEncoder.encode(offsetNumber));

                            dynamicBuffer.append(abiObject.encode());
                        } else {
                            fixedBuffer.append(abiObject.encode());
                        }
                    }
                    break;
                }
        }

        return fixedBuffer.toString() + dynamicBuffer.toString();
    }

    public ABIObject decode(String input) {
        ABIObject abiObject = newObject();

        switch (type) {
            case VALUE:
                {
                    switch (valueType) {
                        case NUMERIC:
                            {
                                abiObject.setNumericValue(
                                        ((List<Uint256>)
                                                        TypeDecoder.decodeStaticArray(
                                                                        input,
                                                                        0,
                                                                        new TypeReference<
                                                                                StaticArray<
                                                                                        Uint256>>() {}.getType(),
                                                                        1)
                                                                .getValue())
                                                .get(0));
                                break;
                            }
                        case BYTES:
                            {
                                abiObject.setBytesValue(
                                        ((List<Bytes32>)
                                                        TypeDecoder.decodeStaticArray(
                                                                        input,
                                                                        0,
                                                                        new TypeReference<
                                                                                StaticArray<
                                                                                        Bytes32>>() {}.getType(),
                                                                        1)
                                                                .getValue())
                                                .get(0));
                                break;
                            }
                        case ADDRESS:
                            {
                                abiObject.setAddressValue(
                                        ((List<Address>)
                                                        TypeDecoder.decodeStaticArray(
                                                                        input,
                                                                        0,
                                                                        new TypeReference<
                                                                                StaticArray<
                                                                                        Address>>() {}.getType(),
                                                                        1)
                                                                .getValue())
                                                .get(0));
                                break;
                            }
                    }
                    break;
                }
            case STRUCT:
                {
                    for (int i = 0; i < structFields.size(); ++i) {
                        ABIObject structObject = abiObject.structFields.get(i);
                        // ABIObject structItem = null;

                        if (structObject.isDynamicStruct
                                || structObject.type.equals(ObjectType.LIST)) {
                            Uint256 offset =
                                    ((List<Uint256>)
                                                    TypeDecoder.decodeStaticArray(
                                                                    input.substring(i * 32 * 2),
                                                                    0,
                                                                    new TypeReference<
                                                                            StaticArray<
                                                                                    Uint256>>() {}.getType(),
                                                                    1)
                                                            .getValue())
                                            .get(0);

                            // abiObject.structFields.set(i,
                            // structObject.decode(input.substring(offset.getValue().intValue() *
                            // 2)));
                            ABIObject item =
                                    structObject.decode(
                                            input.substring(offset.getValue().intValue() * 2));
                            abiObject.structFields.set(i, item);
                        } else {
                            // abiObject.structFields.set(i, structObject.decode(input.substring(i *
                            // 32 * 2)));
                            ABIObject item = structObject.decode(input.substring(i * 32 * 2));
                            abiObject.structFields.set(i, item);
                        }
                    }
                    break;
                }
            case LIST:
                {
                    ABIObject listObject = listValueType;
                    Uint256 length =
                            ((List<Uint256>)
                                            TypeDecoder.decodeStaticArray(
                                                            input,
                                                            0,
                                                            new TypeReference<
                                                                    StaticArray<
                                                                            Uint256>>() {}.getType(),
                                                            1)
                                                    .getValue())
                                    .get(0);

                    int loopLength = 0;

                    if (listType.equals(ListType.STRING)) {
                        abiObject.setListLength(length.getValue().intValue());
                        loopLength =
                                length.getValue().intValue() / 32
                                        + (((length.getValue().intValue() + 32) % 32) > 0 ? 1 : 0);
                    } else {
                        loopLength = length.getValue().intValue();
                    }

                    abiObject.getListValues().clear();

                    for (int i = 0; i < loopLength; ++i) {
                        ABIObject listItem = null;

                        if (listObject.isDynamicStruct || listObject.type.equals(ObjectType.LIST)) {
                            Uint256 offset =
                                    ((List<Uint256>)
                                                    TypeDecoder.decodeStaticArray(
                                                                    input.substring(
                                                                            ((i + 1) * 32) * 2),
                                                                    0,
                                                                    new TypeReference<
                                                                            StaticArray<
                                                                                    Uint256>>() {}.getType(),
                                                                    1)
                                                            .getValue())
                                            .get(0);

                            listItem =
                                    listObject.decode(
                                            input.substring(
                                                    (offset.getValue().intValue() + 32) * 2));
                        } else {
                            listItem = listObject.decode(input.substring((i + 1) * 32 * 2));
                        }

                        abiObject.getListValues().add(listItem);
                    }
                    break;
                }
        }

        return abiObject;
    }

    public ObjectType getType() {
        return type;
    }

    public void setType(ObjectType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ValueType getValueType() {
        return valueType;
    }

    public NumericType getNumericValue() {
        return numericValue;
    }

    public void setNumericValue(NumericType numericValue) {
        this.type = ObjectType.VALUE;
        this.valueType = ValueType.NUMERIC;
        this.numericValue = numericValue;
    }

    public Bytes getBytesValue() {
        return bytesValue;
    }

    public void setBytesValue(Bytes bytesValue) {
        this.type = ObjectType.VALUE;
        this.valueType = ValueType.BYTES;
        this.bytesValue = bytesValue;
    }

    public Address getAddressValue() {
        return addressValue;
    }

    public void setAddressValue(Address addressValue) {
        this.type = ObjectType.VALUE;
        this.valueType = ValueType.ADDRESS;
        this.addressValue = addressValue;
    }

    public void setStringValue(Utf8String string) {
        this.type = ObjectType.LIST;
        this.listType = ListType.STRING;
        this.listValues = new ArrayList<ABIObject>();
        this.listValueType = new ABIObject(new Bytes(2, "0x".getBytes()));

        String value = string.getValue();
        this.listLength = value.length();

        for (int i = 0; i < value.length(); i += 32) {
            int start = i;
            int end = i + 32;

            if (end > value.length()) {
                end = value.length();
            }

            String substr = value.substring(start, end);
            this.listValues.add(new ABIObject(new Bytes(substr.length(), substr.getBytes())));
        }
    }

    public boolean isDynamic() {
        return isDynamicStruct;
    }

    public void setDynamic(boolean isDynamic) {
        this.isDynamicStruct = isDynamic;
    }

    public List<ABIObject> getStructFields() {
        return structFields;
    }

    public void setStructFields(List<ABIObject> structFields) {
        this.type = ObjectType.STRUCT;
        this.structFields = structFields;
    }

    public int getListLength() {
        return listLength;
    }

    public void setListLength(int listLength) {
        this.listLength = listLength;
    }

    public ListType getListType() {
        return listType;
    }

    public void setListType(ListType listType) {
        this.listType = listType;
    }

    public ABIObject getListValueType() {
        return listValueType;
    }

    public void setListValueType(ABIObject listValueType) {
        this.listValueType = listValueType;
    }

    public List<ABIObject> getListValues() {
        return listValues;
    }

    public void setListValues(List<ABIObject> listValues) {
        this.isDynamicStruct = true;
        this.type = ObjectType.LIST;
        this.listValues = listValues;
    }
}
