package com.webank.wecross.stub.test.abi;

import com.webank.wecross.stub.bcos.abi.ABIObject;
import com.webank.wecross.stub.bcos.abi.ABIObject.ObjectType;
import java.util.ArrayList;
import org.fisco.bcos.web3j.abi.datatypes.Utf8String;
import org.fisco.bcos.web3j.abi.datatypes.generated.Uint256;
import org.junit.Assert;
import org.junit.Test;

public class ABIObjectTest {
    @Test
    public void testEncode() {
        // int a, Info[] memory b, string memory c
        /*
         	 * {
             "0": "int256: a 100",
             "1": "tuple(string,int256,tuple(int256,int256,int256)[])[]: b Hello world!,100,1,2,3,Hello world2!,200,5,6,7",
             "2": "string: c Hello world!"
        }
         	 */

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

        Assert.assertEquals(
                "0000000000000000000000000000000000000000000000000000000000000064"
                        + "0000000000000000000000000000000000000000000000000000000000000060"
                        + "0000000000000000000000000000000000000000000000000000000000000300"
                        + "0000000000000000000000000000000000000000000000000000000000000002"
                        + "0000000000000000000000000000000000000000000000000000000000000040"
                        + "0000000000000000000000000000000000000000000000000000000000000160"
                        + "0000000000000000000000000000000000000000000000000000000000000060"
                        + "0000000000000000000000000000000000000000000000000000000000000064"
                        + "00000000000000000000000000000000000000000000000000000000000000a0"
                        + "000000000000000000000000000000000000000000000000000000000000000c"
                        + "48656c6c6f20776f726c64210000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000001"
                        + "0000000000000000000000000000000000000000000000000000000000000001"
                        + "0000000000000000000000000000000000000000000000000000000000000002"
                        + "0000000000000000000000000000000000000000000000000000000000000003"
                        + "0000000000000000000000000000000000000000000000000000000000000060"
                        + "00000000000000000000000000000000000000000000000000000000000000c8"
                        + "00000000000000000000000000000000000000000000000000000000000000a0"
                        + "000000000000000000000000000000000000000000000000000000000000000c"
                        + "48656c6c6f20776f726c64320000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000001"
                        + "0000000000000000000000000000000000000000000000000000000000000005"
                        + "0000000000000000000000000000000000000000000000000000000000000006"
                        + "0000000000000000000000000000000000000000000000000000000000000007"
                        + "000000000000000000000000000000000000000000000000000000000000000c"
                        + "48656c6c6f20776f726c64210000000000000000000000000000000000000000",
                result);

        /*
        for(int i=0; i<result.length(); ++i) {
        	System.out.print(result.charAt(i));
        	if((i > 0) && ((i+1) % 64 == 0)) {
        		System.out.println("");
        	}
        }

        System.out.println("--------------------------------");

        ABIObject decodeResult = inputs.decode(result);

        String result2 = decodeResult.encode();

        for(int i=0; i<result2.length(); ++i) {
        	System.out.print(result2.charAt(i));
        	if((i > 0) && ((i+1) % 64 == 0)) {
        		System.out.println("");
        	}
        }
        */
    }

    @Test
    public void testDecode() {
        // int a, Info[] memory b, string memory c
        /*
         	 * {
             "0": "int256: a 100",
             "1": "tuple(string,int256,tuple(int256,int256,int256)[])[]: b Hello world!,100,1,2,3,Hello world2!,200,5,6,7",
             "2": "string: c Hello world!"
        }
         	 */
        String encoded =
                "0000000000000000000000000000000000000000000000000000000000000064"
                        + "0000000000000000000000000000000000000000000000000000000000000060"
                        + "0000000000000000000000000000000000000000000000000000000000000300"
                        + "0000000000000000000000000000000000000000000000000000000000000002"
                        + "0000000000000000000000000000000000000000000000000000000000000040"
                        + "0000000000000000000000000000000000000000000000000000000000000160"
                        + "0000000000000000000000000000000000000000000000000000000000000060"
                        + "0000000000000000000000000000000000000000000000000000000000000064"
                        + "00000000000000000000000000000000000000000000000000000000000000a0"
                        + "000000000000000000000000000000000000000000000000000000000000000c"
                        + "48656c6c6f20776f726c64210000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000001"
                        + "0000000000000000000000000000000000000000000000000000000000000001"
                        + "0000000000000000000000000000000000000000000000000000000000000002"
                        + "0000000000000000000000000000000000000000000000000000000000000003"
                        + "0000000000000000000000000000000000000000000000000000000000000060"
                        + "00000000000000000000000000000000000000000000000000000000000000c8"
                        + "00000000000000000000000000000000000000000000000000000000000000a0"
                        + "000000000000000000000000000000000000000000000000000000000000000c"
                        + "48656c6c6f20776f726c64320000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000001"
                        + "0000000000000000000000000000000000000000000000000000000000000005"
                        + "0000000000000000000000000000000000000000000000000000000000000006"
                        + "0000000000000000000000000000000000000000000000000000000000000007"
                        + "000000000000000000000000000000000000000000000000000000000000000c"
                        + "48656c6c6f20776f726c64210000000000000000000000000000000000000000";

        ABIObject input1 = new ABIObject(new Uint256(0));
        ABIObject input2 = new ABIObject(ObjectType.LIST);

        ABIObject item1 = new ABIObject(ObjectType.STRUCT);
        item1.setDynamic(true);
        item1.getStructFields().add(new ABIObject(new Utf8String("")));
        item1.getStructFields().add(new ABIObject(new Uint256(0)));
        item1.getStructFields().add(new ABIObject(ObjectType.LIST));

        item1.getStructFields().get(2).getListValues().add(new ABIObject(ObjectType.STRUCT));
        item1.getStructFields().get(2).getListValues().get(0).setDynamic(false);

        item1.getStructFields()
                .get(2)
                .getListValues()
                .get(0)
                .getStructFields()
                .add(new ABIObject(new Uint256(0)));
        item1.getStructFields()
                .get(2)
                .getListValues()
                .get(0)
                .getStructFields()
                .add(new ABIObject(new Uint256(0)));
        item1.getStructFields()
                .get(2)
                .getListValues()
                .get(0)
                .getStructFields()
                .add(new ABIObject(new Uint256(0)));

        item1.getStructFields()
                .get(2)
                .setListValueType(item1.getStructFields().get(2).getListValues().get(0));

        input2.getListValues().add(item1);
        input2.setListValueType(item1);

        ABIObject item2 = new ABIObject(ObjectType.STRUCT);
        item2.setDynamic(true);
        item2.getStructFields().add(new ABIObject(new Utf8String("")));
        item2.getStructFields().add(new ABIObject(new Uint256(0)));
        item2.getStructFields().add(new ABIObject(ObjectType.LIST));

        item2.getStructFields().get(2).getListValues().add(new ABIObject(ObjectType.STRUCT));

        item2.getStructFields()
                .get(2)
                .getListValues()
                .get(0)
                .getStructFields()
                .add(new ABIObject(new Uint256(0)));
        item2.getStructFields()
                .get(2)
                .getListValues()
                .get(0)
                .getStructFields()
                .add(new ABIObject(new Uint256(0)));
        item2.getStructFields()
                .get(2)
                .getListValues()
                .get(0)
                .getStructFields()
                .add(new ABIObject(new Uint256(0)));

        item1.getStructFields()
                .get(2)
                .setListValueType(item2.getStructFields().get(2).getListValues().get(0));

        input2.getListValues().add(item2);

        ABIObject inputs = new ABIObject(ObjectType.STRUCT);
        inputs.setDynamic(false);
        inputs.setStructFields(new ArrayList<ABIObject>());

        inputs.getStructFields().add(input1);
        inputs.getStructFields().add(input2);
        inputs.getStructFields().add(new ABIObject(new Utf8String("")));

        ABIObject decoded = inputs.decode(encoded);

        Assert.assertEquals(encoded, decoded.encode());
    }
}
