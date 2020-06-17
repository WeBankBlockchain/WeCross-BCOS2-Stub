package com.webank.wecross.stub.bcos.abi;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.fisco.bcos.web3j.abi.TypeEncoder;
import org.fisco.bcos.web3j.abi.datatypes.Address;
import org.fisco.bcos.web3j.abi.datatypes.Bytes;
import org.fisco.bcos.web3j.abi.datatypes.NumericType;
import org.fisco.bcos.web3j.abi.datatypes.Utf8String;
import org.fisco.bcos.web3j.abi.datatypes.generated.Uint256;

public class ABIObject {
	public enum ObjectType {
		VALUE, STRUCT, LIST,
	};

	public enum ValueType {
		NUMERIC, BYTES, ADDRESS,
	}

	private class Offset {
		private int staticFieldOffset;
		private int dynamicOffset;

		public int getStaticOffset() {
			return staticFieldOffset;
		}

		public void setStaticOffset(int staticOffset) {
			this.staticFieldOffset = staticOffset;
		}

		public int getDynamicOffset() {
			return dynamicOffset;
		}

		public void setDynamicOffset(int dynamicOffset) {
			this.dynamicOffset = dynamicOffset;
		}
	}

	private class Buffer {
		private List<Offset> offsets = new LinkedList<Offset>();
		private StringBuffer buffer = new StringBuffer();

		public List<Offset> getOffsets() {
			return offsets;
		}

		public void setOffsets(List<Offset> offsets) {
			this.offsets = offsets;
		}

		public StringBuffer getBuffer() {
			return buffer;
		}

		public void setBuffer(StringBuffer buffer) {
			this.buffer = buffer;
		}
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
	private ABIObject listValueType; // for list
	private List<ABIObject> listValues; // for list

	public ABIObject(ObjectType type) {
		this.type = type;
		switch (type) {
		case VALUE: {
			break;
		}
		case STRUCT: {
			structFields = new LinkedList<ABIObject>();
			break;
		}
		case LIST: {
			listValues = new LinkedList<ABIObject>();
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
		this.type = ObjectType.LIST;
		this.listValues = new ArrayList<ABIObject>();

		String value = string.getValue();
		for (int i = 0; i < value.length(); i += 32) {
			int start = i;
			int end = i + 32;

			if (end > value.length()) {
				end = value.length();
			}

			String substr = value.substring(start, end);
			this.listValues.add(new ABIObject(new Bytes(substr.length(), substr.getBytes())));
		}

		this.listLength = value.length();
	}

	public String encode() {
		StringBuffer fixedBuffer = new StringBuffer();
		StringBuffer dynamicBuffer = new StringBuffer();

		switch (type) {
		case VALUE: {
			switch (valueType) {
			case NUMERIC: {
				fixedBuffer.append(TypeEncoder.encode(numericValue));
				break;
			}
			case BYTES: {
				fixedBuffer.append(TypeEncoder.encode(bytesValue));
				break;
			}
			case ADDRESS: {
				fixedBuffer.append(TypeEncoder.encode(addressValue));
				break;
			}
			}
			break;
		}
		case STRUCT:{
			for (ABIObject abiObject : structFields) {
				if(abiObject.isDynamicStruct || abiObject.type.equals(ObjectType.LIST)) {
					Uint256 offsetNumber = new Uint256(32 * structFields.size() + dynamicBuffer.length() / 2);
					fixedBuffer.append(TypeEncoder.encode(offsetNumber));

					dynamicBuffer.append(abiObject.encode());
				}
				else {
					fixedBuffer.append(abiObject.encode());
				}
			}

			break;
		}
		case LIST: {
			Uint256 length = null;
			if (listLength > 0) {
				length = new Uint256(listLength);
			} else {
				length = new Uint256(listValues.size());
			}
			fixedBuffer.append(TypeEncoder.encode(length));

			for (ABIObject abiObject : listValues) {
				if(abiObject.isDynamicStruct || abiObject.type.equals(ObjectType.LIST)) {
					Uint256 offsetNumber = new Uint256(32 * listValues.size() + dynamicBuffer.length() / 2);
					fixedBuffer.append(TypeEncoder.encode(offsetNumber));
					
					dynamicBuffer.append(abiObject.encode());
				}
				else {
					fixedBuffer.append(abiObject.encode());
				}
			}
			break;
		}
		}
		
		return fixedBuffer.toString() + dynamicBuffer.toString();
	}

	public void decode(String input) {
		switch (type) {
		case VALUE: {
			break;
		}
		case STRUCT: {
			break;
		}
		case LIST: {
			break;
		}
		}
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
