package com.webank.wecross.stub.bcos.abi;

import java.util.LinkedList;
import java.util.List;

import org.fisco.bcos.web3j.abi.TypeEncoder;
import org.fisco.bcos.web3j.abi.datatypes.Address;
import org.fisco.bcos.web3j.abi.datatypes.Bytes;
import org.fisco.bcos.web3j.abi.datatypes.NumericType;
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

	private ObjectType type;
	private String name; // field name

	private ValueType valueType; // for value
	private NumericType numericValue;
	private Bytes bytesValue;
	private Address addressValue;

	private boolean isDynamicStruct;
	private List<ABIObject> structFields; // for struct

	private ABIObject listValueType; // for list
	private List<ABIObject> listValues; // for list
	
	public ABIObject(ObjectType type) {
		this.type = type;
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
	
	public String encode() {
		StringBuffer staticBuffer = new StringBuffer();
		StringBuffer dynamicBuffer = new StringBuffer();
		
		List<Offset> offsets = new LinkedList<Offset>();
		
		encode(false, offsets, staticBuffer, dynamicBuffer);
		
		// update offset
		for (Offset offset : offsets) {
			staticBuffer.replace(offset.getStaticOffset() * 2, offset.getStaticOffset() * 2 + 64,
					TypeEncoder.encode(new Uint256(staticBuffer.length() / 2 + offset.getDynamicOffset())));
		}

		return staticBuffer.toString() + dynamicBuffer.toString();
	}
	
	private void encode(boolean insideDynamic, List<Offset> offsets, StringBuffer staticBuffer, StringBuffer dynamicBuffer) {
		switch (type) {
		case VALUE: {
			StringBuffer encodedTarget = staticBuffer;
			if (insideDynamic) {
				encodedTarget = dynamicBuffer;
			}

			switch (valueType) {
			case NUMERIC: {
				encodedTarget.append(TypeEncoder.encode(numericValue));
				break;
			}
			case BYTES: {
				encodedTarget.append(TypeEncoder.encode(bytesValue));
				break;
			}
			case ADDRESS: {
				encodedTarget.append(TypeEncoder.encode(addressValue));
				break;
			}
			}
			break;
		}
		case STRUCT: {
			if(insideDynamic && isDynamicStruct) {
				Uint256 offset = new Uint256(dynamicBuffer.length() / 2 + 32);
				dynamicBuffer.append(TypeEncoder.encode(offset));
			}
			else if (!insideDynamic && isDynamicStruct){
				Offset offset = new Offset();
				offset.setStaticOffset(staticBuffer.length() / 2);
				offset.setDynamicOffset(dynamicBuffer.length() / 2);

				offsets.add(offset);
				
				Uint256 offsetNumber = new Uint256(0);
				staticBuffer.append(TypeEncoder.encode(offsetNumber));
			}
			
			for (ABIObject abiObject : structFields) {
				abiObject.encode(insideDynamic || isDynamicStruct, offsets, staticBuffer, dynamicBuffer);
			}
			break;
		}
		case LIST: {
			if (insideDynamic) {
				Uint256 offsetNumber = new Uint256(dynamicBuffer.length() / 2 + 32);
				dynamicBuffer.append(TypeEncoder.encode(offsetNumber));
			}
			else {
				Offset offset = new Offset();
				offset.setStaticOffset(staticBuffer.length() / 2);
				offset.setDynamicOffset(dynamicBuffer.length() / 2);

				offsets.add(offset);
				
				Uint256 offsetNumber = new Uint256(0);
				staticBuffer.append(TypeEncoder.encode(offsetNumber));
			}

			// array length
			Uint256 length = new Uint256(listValues.size());
			dynamicBuffer.append(TypeEncoder.encode(length));

			for (ABIObject abiObject : listValues) {
				abiObject.encode(true, offsets, staticBuffer, dynamicBuffer);
			}
			break;
		}
		}
	}

	public void decode(String input) {

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
