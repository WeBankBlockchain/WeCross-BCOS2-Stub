package com.webank.wecross.stub.abi;

import com.webank.wecross.stub.bcos.abi.ABICodecJsonWrapper;
import com.webank.wecross.stub.bcos.abi.ABIDefinitionFactory;
import com.webank.wecross.stub.bcos.abi.ABIObject;
import com.webank.wecross.stub.bcos.abi.ABIObjectFactory;
import com.webank.wecross.stub.bcos.abi.ContractABIDefinition;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class ContractTypeTest {
    String abiDesc =
            "[\n"
                    + "\t{\n"
                    + "\t\t\"constant\": false,\n"
                    + "\t\t\"inputs\": [\n"
                    + "\t\t\t{\n"
                    + "\t\t\t\t\"name\": \"_u\",\n"
                    + "\t\t\t\t\"type\": \"uint256[]\"\n"
                    + "\t\t\t},\n"
                    + "\t\t\t{\n"
                    + "\t\t\t\t\"name\": \"_b\",\n"
                    + "\t\t\t\t\"type\": \"bool[]\"\n"
                    + "\t\t\t},\n"
                    + "\t\t\t{\n"
                    + "\t\t\t\t\"name\": \"_addr\",\n"
                    + "\t\t\t\t\"type\": \"address[]\"\n"
                    + "\t\t\t},\n"
                    + "\t\t\t{\n"
                    + "\t\t\t\t\"name\": \"_bs32\",\n"
                    + "\t\t\t\t\"type\": \"bytes32[]\"\n"
                    + "\t\t\t},\n"
                    + "\t\t\t{\n"
                    + "\t\t\t\t\"name\": \"_s\",\n"
                    + "\t\t\t\t\"type\": \"string[]\"\n"
                    + "\t\t\t},\n"
                    + "\t\t\t{\n"
                    + "\t\t\t\t\"name\": \"_bs\",\n"
                    + "\t\t\t\t\"type\": \"bytes[]\"\n"
                    + "\t\t\t}\n"
                    + "\t\t],\n"
                    + "\t\t\"name\": \"setDynamicValue\",\n"
                    + "\t\t\"outputs\": [],\n"
                    + "\t\t\"payable\": false,\n"
                    + "\t\t\"stateMutability\": \"nonpayable\",\n"
                    + "\t\t\"type\": \"function\"\n"
                    + "\t},\n"
                    + "\t{\n"
                    + "\t\t\"constant\": false,\n"
                    + "\t\t\"inputs\": [\n"
                    + "\t\t\t{\n"
                    + "\t\t\t\t\"name\": \"_u\",\n"
                    + "\t\t\t\t\"type\": \"uint256[3]\"\n"
                    + "\t\t\t},\n"
                    + "\t\t\t{\n"
                    + "\t\t\t\t\"name\": \"_b\",\n"
                    + "\t\t\t\t\"type\": \"bool[3]\"\n"
                    + "\t\t\t},\n"
                    + "\t\t\t{\n"
                    + "\t\t\t\t\"name\": \"_addr\",\n"
                    + "\t\t\t\t\"type\": \"address[3]\"\n"
                    + "\t\t\t},\n"
                    + "\t\t\t{\n"
                    + "\t\t\t\t\"name\": \"_bs32\",\n"
                    + "\t\t\t\t\"type\": \"bytes32[3]\"\n"
                    + "\t\t\t},\n"
                    + "\t\t\t{\n"
                    + "\t\t\t\t\"name\": \"_s\",\n"
                    + "\t\t\t\t\"type\": \"string[3]\"\n"
                    + "\t\t\t},\n"
                    + "\t\t\t{\n"
                    + "\t\t\t\t\"name\": \"_bs\",\n"
                    + "\t\t\t\t\"type\": \"bytes[3]\"\n"
                    + "\t\t\t}\n"
                    + "\t\t],\n"
                    + "\t\t\"name\": \"setFixedValue\",\n"
                    + "\t\t\"outputs\": [],\n"
                    + "\t\t\"payable\": false,\n"
                    + "\t\t\"stateMutability\": \"nonpayable\",\n"
                    + "\t\t\"type\": \"function\"\n"
                    + "\t},\n"
                    + "\t{\n"
                    + "\t\t\"constant\": false,\n"
                    + "\t\t\"inputs\": [\n"
                    + "\t\t\t{\n"
                    + "\t\t\t\t\"name\": \"_u\",\n"
                    + "\t\t\t\t\"type\": \"uint256\"\n"
                    + "\t\t\t},\n"
                    + "\t\t\t{\n"
                    + "\t\t\t\t\"name\": \"_b\",\n"
                    + "\t\t\t\t\"type\": \"bool\"\n"
                    + "\t\t\t},\n"
                    + "\t\t\t{\n"
                    + "\t\t\t\t\"name\": \"_addr\",\n"
                    + "\t\t\t\t\"type\": \"address\"\n"
                    + "\t\t\t},\n"
                    + "\t\t\t{\n"
                    + "\t\t\t\t\"name\": \"_bs32\",\n"
                    + "\t\t\t\t\"type\": \"bytes32\"\n"
                    + "\t\t\t},\n"
                    + "\t\t\t{\n"
                    + "\t\t\t\t\"name\": \"_s\",\n"
                    + "\t\t\t\t\"type\": \"string\"\n"
                    + "\t\t\t},\n"
                    + "\t\t\t{\n"
                    + "\t\t\t\t\"name\": \"_bs\",\n"
                    + "\t\t\t\t\"type\": \"bytes\"\n"
                    + "\t\t\t}\n"
                    + "\t\t],\n"
                    + "\t\t\"name\": \"setValue\",\n"
                    + "\t\t\"outputs\": [],\n"
                    + "\t\t\"payable\": false,\n"
                    + "\t\t\"stateMutability\": \"nonpayable\",\n"
                    + "\t\t\"type\": \"function\"\n"
                    + "\t},\n"
                    + "\t{\n"
                    + "\t\t\"constant\": true,\n"
                    + "\t\t\"inputs\": [],\n"
                    + "\t\t\"name\": \"getDynamicValue\",\n"
                    + "\t\t\"outputs\": [\n"
                    + "\t\t\t{\n"
                    + "\t\t\t\t\"name\": \"_u\",\n"
                    + "\t\t\t\t\"type\": \"uint256[]\"\n"
                    + "\t\t\t},\n"
                    + "\t\t\t{\n"
                    + "\t\t\t\t\"name\": \"_b\",\n"
                    + "\t\t\t\t\"type\": \"bool[]\"\n"
                    + "\t\t\t},\n"
                    + "\t\t\t{\n"
                    + "\t\t\t\t\"name\": \"_addr\",\n"
                    + "\t\t\t\t\"type\": \"address[]\"\n"
                    + "\t\t\t},\n"
                    + "\t\t\t{\n"
                    + "\t\t\t\t\"name\": \"_bs32\",\n"
                    + "\t\t\t\t\"type\": \"bytes32[]\"\n"
                    + "\t\t\t},\n"
                    + "\t\t\t{\n"
                    + "\t\t\t\t\"name\": \"_s\",\n"
                    + "\t\t\t\t\"type\": \"string[]\"\n"
                    + "\t\t\t},\n"
                    + "\t\t\t{\n"
                    + "\t\t\t\t\"name\": \"_bs\",\n"
                    + "\t\t\t\t\"type\": \"bytes[]\"\n"
                    + "\t\t\t}\n"
                    + "\t\t],\n"
                    + "\t\t\"payable\": false,\n"
                    + "\t\t\"stateMutability\": \"view\",\n"
                    + "\t\t\"type\": \"function\"\n"
                    + "\t},\n"
                    + "\t{\n"
                    + "\t\t\"constant\": true,\n"
                    + "\t\t\"inputs\": [],\n"
                    + "\t\t\"name\": \"getFixedValue\",\n"
                    + "\t\t\"outputs\": [\n"
                    + "\t\t\t{\n"
                    + "\t\t\t\t\"name\": \"_u\",\n"
                    + "\t\t\t\t\"type\": \"uint256[3]\"\n"
                    + "\t\t\t},\n"
                    + "\t\t\t{\n"
                    + "\t\t\t\t\"name\": \"_b\",\n"
                    + "\t\t\t\t\"type\": \"bool[3]\"\n"
                    + "\t\t\t},\n"
                    + "\t\t\t{\n"
                    + "\t\t\t\t\"name\": \"_addr\",\n"
                    + "\t\t\t\t\"type\": \"address[3]\"\n"
                    + "\t\t\t},\n"
                    + "\t\t\t{\n"
                    + "\t\t\t\t\"name\": \"_bs32\",\n"
                    + "\t\t\t\t\"type\": \"bytes32[3]\"\n"
                    + "\t\t\t},\n"
                    + "\t\t\t{\n"
                    + "\t\t\t\t\"name\": \"_s\",\n"
                    + "\t\t\t\t\"type\": \"string[3]\"\n"
                    + "\t\t\t},\n"
                    + "\t\t\t{\n"
                    + "\t\t\t\t\"name\": \"_bs\",\n"
                    + "\t\t\t\t\"type\": \"bytes[3]\"\n"
                    + "\t\t\t}\n"
                    + "\t\t],\n"
                    + "\t\t\"payable\": false,\n"
                    + "\t\t\"stateMutability\": \"view\",\n"
                    + "\t\t\"type\": \"function\"\n"
                    + "\t},\n"
                    + "\t{\n"
                    + "\t\t\"constant\": true,\n"
                    + "\t\t\"inputs\": [],\n"
                    + "\t\t\"name\": \"getValue\",\n"
                    + "\t\t\"outputs\": [\n"
                    + "\t\t\t{\n"
                    + "\t\t\t\t\"name\": \"_u\",\n"
                    + "\t\t\t\t\"type\": \"uint256\"\n"
                    + "\t\t\t},\n"
                    + "\t\t\t{\n"
                    + "\t\t\t\t\"name\": \"_b\",\n"
                    + "\t\t\t\t\"type\": \"bool\"\n"
                    + "\t\t\t},\n"
                    + "\t\t\t{\n"
                    + "\t\t\t\t\"name\": \"_addr\",\n"
                    + "\t\t\t\t\"type\": \"address\"\n"
                    + "\t\t\t},\n"
                    + "\t\t\t{\n"
                    + "\t\t\t\t\"name\": \"_bs32\",\n"
                    + "\t\t\t\t\"type\": \"bytes32\"\n"
                    + "\t\t\t},\n"
                    + "\t\t\t{\n"
                    + "\t\t\t\t\"name\": \"_s\",\n"
                    + "\t\t\t\t\"type\": \"string\"\n"
                    + "\t\t\t},\n"
                    + "\t\t\t{\n"
                    + "\t\t\t\t\"name\": \"_bs\",\n"
                    + "\t\t\t\t\"type\": \"bytes\"\n"
                    + "\t\t\t}\n"
                    + "\t\t],\n"
                    + "\t\t\"payable\": false,\n"
                    + "\t\t\"stateMutability\": \"view\",\n"
                    + "\t\t\"type\": \"function\"\n"
                    + "\t}\n"
                    + "]";

    /*
    {
        "20965255": "getValue()",
            "ed4d0e39": "getDynamicValue()",
            "c1cee39a": "getFixedValue()",
            "dfed87e3": "setDynamicValue(uint256[],bool[],address[],bytes32[],string[],bytes[])",
            "63e5584b": "setFixedValue(uint256[3],bool[3],address[3],bytes32[3],string[3],bytes[3])",
            "11cfbe17": "setValue(uint256,bool,address,bytes32,string,bytes)"
    }*/

    private ContractABIDefinition contractABIDefinition = ABIDefinitionFactory.loadABI(abiDesc);

    @Test
    public void ContractFixedTypeDecodeTest() {
        ABIObject outputObject =
                ABIObjectFactory.createOutputObject(
                        contractABIDefinition.getFunctions().get("getFixedValue").get(0));

        String encoded =
                "00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001c000000000000000000000000000000000000000000000000000000000000002800000000000000000000000000000000000000000000000000000000000000060000000000000000000000000000000000000000000000000000000000000008000000000000000000000000000000000000000000000000000000000000000a00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000060000000000000000000000000000000000000000000000000000000000000008000000000000000000000000000000000000000000000000000000000000000a0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";
        ABICodecJsonWrapper abiCodecJsonWrapper = new ABICodecJsonWrapper();
        List<String> decodeResult = abiCodecJsonWrapper.decode(outputObject, encoded);

        Assert.assertEquals(decodeResult.get(0), "[ 0, 0, 0 ]");
        Assert.assertEquals(decodeResult.get(1), "[ false, false, false ]");
        Assert.assertEquals(
                decodeResult.get(2),
                "[ \"0x0000000000000000000000000000000000000000\", \"0x0000000000000000000000000000000000000000\", \"0x0000000000000000000000000000000000000000\" ]");
        Assert.assertEquals(
                decodeResult.get(3),
                "[ \"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=\", \"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=\", \"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=\" ]");
        Assert.assertEquals(decodeResult.get(4), "[ \"\", \"\", \"\" ]");
        Assert.assertEquals(decodeResult.get(5), "[ \"\", \"\", \"\" ]");
    }

    @Test
    public void ContractFixedTypeEncodeTest() throws IOException {
        ABIObject inputObject =
                ABIObjectFactory.createInputObject(
                        contractABIDefinition.getFunctions().get("setFixedValue").get(0));

        ABIObject outObject =
                ABIObjectFactory.createOutputObject(
                        contractABIDefinition.getFunctions().get("getFixedValue").get(0));

        List<String> params =
                Arrays.asList(
                        "[1,2,3]",
                        "[true,false,true]",
                        "[\"0xa\",\"0xb\",\"0xc\"]",
                        "[\"a\",\"b\",\"c\"]",
                        "[\"a\",\"b\",\"c\"]",
                        "[\"a\",\"b\",\"c\"]");

        ABICodecJsonWrapper abiCodecJsonWrapper = new ABICodecJsonWrapper();
        ABIObject encodeObject = abiCodecJsonWrapper.encode(inputObject, params);

        List<String> decodeResult = abiCodecJsonWrapper.decode(outObject, encodeObject.encode());

        Assert.assertEquals(decodeResult.get(0), "[ 1, 2, 3 ]");
        Assert.assertEquals(decodeResult.get(1), "[ true, false, true ]");
        Assert.assertEquals(
                decodeResult.get(2),
                "[ \"0x000000000000000000000000000000000000000a\", \"0x000000000000000000000000000000000000000b\", \"0x000000000000000000000000000000000000000c\" ]");
        // Assert.assertEquals(
        //        decodeResult.get(3),
        //        "[ \"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=\",
        // \"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=\",
        // \"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=\" ]");
        Assert.assertEquals(decodeResult.get(4), "[ \"a\", \"b\", \"c\" ]");
        // Assert.assertEquals(decodeResult.get(5), "[ \"a\", \"b\", \"c\" ]");
    }
}
