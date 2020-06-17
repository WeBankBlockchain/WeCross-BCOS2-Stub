package com.webank.wecross.stub.bcos.abi;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;

import org.fisco.bcos.web3j.abi.datatypes.Utf8String;
import org.fisco.bcos.web3j.abi.datatypes.generated.Uint256;

import com.webank.wecross.stub.bcos.abi.ABIObject.ObjectType;

public class ABIObjectFactory {
	public ABIObject build(InputStream inputStream) {
		return null;
	}
	
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
		item1.getStructFields().get(2).getListValues().get(0).getStructFields().add(new ABIObject(new Uint256(1)));
		item1.getStructFields().get(2).getListValues().get(0).getStructFields().add(new ABIObject(new Uint256(2)));
		item1.getStructFields().get(2).getListValues().get(0).getStructFields().add(new ABIObject(new Uint256(3)));
		
		input2.getListValues().add(item1);
		
		ABIObject item2 = new ABIObject(ObjectType.STRUCT);
		item2.setDynamic(true);
		item2.getStructFields().add(new ABIObject(new Utf8String("Hello world2")));
		item2.getStructFields().add(new ABIObject(new Uint256(200)));
		item2.getStructFields().add(new ABIObject(ObjectType.LIST));
		
		item2.getStructFields().get(2).getListValues().add(new ABIObject(ObjectType.STRUCT));
		item2.getStructFields().get(2).getListValues().get(0).setDynamic(false);
		item2.getStructFields().get(2).getListValues().get(0).getStructFields().add(new ABIObject(new Uint256(5)));
		item2.getStructFields().get(2).getListValues().get(0).getStructFields().add(new ABIObject(new Uint256(6)));
		item2.getStructFields().get(2).getListValues().get(0).getStructFields().add(new ABIObject(new Uint256(7)));
		
		input2.getListValues().add(item2);
		
		ABIObject inputs = new ABIObject(ObjectType.STRUCT);
		inputs.setDynamic(false);
		inputs.setStructFields(new ArrayList<ABIObject>());
		
		inputs.getStructFields().add(input1);
		inputs.getStructFields().add(input2);
		inputs.getStructFields().add(new ABIObject(new Utf8String("Hello world!")));
		
		String result = inputs.encode();
		
		for(int i=0; i<result.length(); ++i) {
			System.out.print(result.charAt(i));
			if((i > 0) && ((i+1) % 64 == 0)) {
				System.out.println("");
			}
		}
		
		// System.out.println("out: " + result);
	}
}
