package com.webank.wecross.stub.bcos.abi;

import java.io.InputStream;
import java.util.ArrayList;

import org.fisco.bcos.web3j.abi.datatypes.generated.Uint256;

import com.webank.wecross.stub.bcos.abi.ABIObject.ObjectType;

public class ABIObjectFactory {
	public ABIObject build(InputStream inputStream) {
		return null;
	}
	
	public static void main(String[] args) {
		ABIObject number = new ABIObject(new Uint256(100));
		
		ABIObject arrayObject = new ABIObject(ObjectType.LIST);
		arrayObject.setListValues(new ArrayList<ABIObject>());
		
		ABIObject innerArrayObject = new ABIObject(ObjectType.LIST);
		innerArrayObject.setListValues(new ArrayList<ABIObject>());
		innerArrayObject.getListValues().add(new ABIObject(new Uint256(1)));
		innerArrayObject.getListValues().add(new ABIObject(new Uint256(2)));
		innerArrayObject.getListValues().add(new ABIObject(new Uint256(3)));
		
		arrayObject.getListValues().add(innerArrayObject);
		
		ABIObject structObject = new ABIObject(ObjectType.STRUCT);
		structObject.setDynamic(false);
		structObject.setStructFields(new ArrayList<ABIObject>());
		
		structObject.getStructFields().add(number);
		structObject.getStructFields().add(arrayObject);
		
		String result = structObject.encode();
		
		for(int i=0; i<result.length(); ++i) {
			System.out.print(result.charAt(i));
			if((i > 0) && ((i+1) % 64 == 0)) {
				System.out.println("...");
			}
		}
		
		// System.out.println("out: " + result);
	}
}
