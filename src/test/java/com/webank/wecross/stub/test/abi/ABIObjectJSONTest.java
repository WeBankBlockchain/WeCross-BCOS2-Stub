package com.webank.wecross.stub.test.abi;

import com.webank.wecross.stub.bcos.abi.ABIObject;
import com.webank.wecross.stub.bcos.abi.ABIObjectJSONWrapper;
import com.webank.wecross.stub.bcos.abi.Contract;
import com.webank.wecross.stub.bcos.abi.Function;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Assert;
import org.junit.Test;

public class ABIObjectJSONTest {
    String abiDesc =
            "[\n"
                    + "        {\n"
                    + "                \"anonymous\": false,\n"
                    + "                \"inputs\": [\n"
                    + "                        {\n"
                    + "                                \"indexed\": false,\n"
                    + "                                \"internalType\": \"int256\",\n"
                    + "                                \"name\": \"a\",\n"
                    + "                                \"type\": \"int256\"\n"
                    + "                        },\n"
                    + "                        {\n"
                    + "                                \"components\": [\n"
                    + "                                        {\n"
                    + "                                                \"internalType\": \"string\",\n"
                    + "                                                \"name\": \"name\",\n"
                    + "                                                \"type\": \"string\"\n"
                    + "                                        },\n"
                    + "                                        {\n"
                    + "                                                \"internalType\": \"int256\",\n"
                    + "                                                \"name\": \"count\",\n"
                    + "                                                \"type\": \"int256\"\n"
                    + "                                        },\n"
                    + "                                        {\n"
                    + "                                                \"components\": [\n"
                    + "                                                        {\n"
                    + "                                                                \"internalType\": \"int256\",\n"
                    + "                                                                \"name\": \"a\",\n"
                    + "                                                                \"type\": \"int256\"\n"
                    + "                                                        },\n"
                    + "                                                        {\n"
                    + "                                                                \"internalType\": \"int256\",\n"
                    + "                                                                \"name\": \"b\",\n"
                    + "                                                                \"type\": \"int256\"\n"
                    + "                                                        },\n"
                    + "                                                        {\n"
                    + "                                                                \"internalType\": \"int256\",\n"
                    + "                                                                \"name\": \"c\",\n"
                    + "                                                                \"type\": \"int256\"\n"
                    + "                                                        }\n"
                    + "                                                ],\n"
                    + "                                                \"internalType\": \"struct Proxy.Item[]\",\n"
                    + "                                                \"name\": \"items\",\n"
                    + "                                                \"type\": \"tuple[]\"\n"
                    + "                                        }\n"
                    + "                                ],\n"
                    + "                                \"indexed\": false,\n"
                    + "                                \"internalType\": \"struct Proxy.Info[]\",\n"
                    + "                                \"name\": \"b\",\n"
                    + "                                \"type\": \"tuple[]\"\n"
                    + "                        },\n"
                    + "                        {\n"
                    + "                                \"indexed\": false,\n"
                    + "                                \"internalType\": \"string\",\n"
                    + "                                \"name\": \"c\",\n"
                    + "                                \"type\": \"string\"\n"
                    + "                        }\n"
                    + "                ],\n"
                    + "                \"name\": \"output1\",\n"
                    + "                \"type\": \"event\"\n"
                    + "        },\n"
                    + "        {\n"
                    + "                \"constant\": false,\n"
                    + "                \"inputs\": [\n"
                    + "                        {\n"
                    + "                                \"internalType\": \"int256\",\n"
                    + "                                \"name\": \"a\",\n"
                    + "                                \"type\": \"int256\"\n"
                    + "                        },\n"
                    + "                        {\n"
                    + "                                \"components\": [\n"
                    + "                                        {\n"
                    + "                                                \"internalType\": \"string\",\n"
                    + "                                                \"name\": \"name\",\n"
                    + "                                                \"type\": \"string\"\n"
                    + "                                        },\n"
                    + "                                        {\n"
                    + "                                                \"internalType\": \"int256\",\n"
                    + "                                                \"name\": \"count\",\n"
                    + "                                                \"type\": \"int256\"\n"
                    + "                                        },\n"
                    + "                                        {\n"
                    + "                                                \"components\": [\n"
                    + "                                                        {\n"
                    + "                                                                \"internalType\": \"int256\",\n"
                    + "                                                                \"name\": \"a\",\n"
                    + "                                                                \"type\": \"int256\"\n"
                    + "                                                        },\n"
                    + "                                                        {\n"
                    + "                                                                \"internalType\": \"int256\",\n"
                    + "                                                                \"name\": \"b\",\n"
                    + "                                                                \"type\": \"int256\"\n"
                    + "                                                        },\n"
                    + "                                                        {\n"
                    + "                                                                \"internalType\": \"int256\",\n"
                    + "                                                                \"name\": \"c\",\n"
                    + "                                                                \"type\": \"int256\"\n"
                    + "                                                        }\n"
                    + "                                                ],\n"
                    + "                                                \"internalType\": \"struct Proxy.Item[]\",\n"
                    + "                                                \"name\": \"items\",\n"
                    + "                                                \"type\": \"tuple[]\"\n"
                    + "                                        }\n"
                    + "                                ],\n"
                    + "                                \"internalType\": \"struct Proxy.Info[]\",\n"
                    + "                                \"name\": \"b\",\n"
                    + "                                \"type\": \"tuple[]\"\n"
                    + "                        },\n"
                    + "                        {\n"
                    + "                                \"internalType\": \"string\",\n"
                    + "                                \"name\": \"c\",\n"
                    + "                                \"type\": \"string\"\n"
                    + "                        }\n"
                    + "                ],\n"
                    + "                \"name\": \"test\",\n"
                    + "                \"outputs\": [\n"
                    + "                        {\n"
                    + "                                \"internalType\": \"int256\",\n"
                    + "                                \"name\": \"\",\n"
                    + "                                \"type\": \"int256\"\n"
                    + "                        }\n"
                    + "                ],\n"
                    + "                \"payable\": false,\n"
                    + "                \"stateMutability\": \"nonpayable\",\n"
                    + "                \"type\": \"function\"\n"
                    + "        },\n"
                    + "        {\n"
                    + "                \"constant\": false,\n"
                    + "                \"inputs\": [],\n"
                    + "                \"name\": \"test1\",\n"
                    + "                \"outputs\": [\n"
                    + "                        {\n"
                    + "                                \"internalType\": \"int256\",\n"
                    + "                                \"name\": \"a\",\n"
                    + "                                \"type\": \"int256\"\n"
                    + "                        },\n"
                    + "                        {\n"
                    + "                                \"components\": [\n"
                    + "                                        {\n"
                    + "                                                \"internalType\": \"string\",\n"
                    + "                                                \"name\": \"name\",\n"
                    + "                                                \"type\": \"string\"\n"
                    + "                                        },\n"
                    + "                                        {\n"
                    + "                                                \"internalType\": \"int256\",\n"
                    + "                                                \"name\": \"count\",\n"
                    + "                                                \"type\": \"int256\"\n"
                    + "                                        },\n"
                    + "                                        {\n"
                    + "                                                \"components\": [\n"
                    + "                                                        {\n"
                    + "                                                                \"internalType\": \"int256\",\n"
                    + "                                                                \"name\": \"a\",\n"
                    + "                                                                \"type\": \"int256\"\n"
                    + "                                                        },\n"
                    + "                                                        {\n"
                    + "                                                                \"internalType\": \"int256\",\n"
                    + "                                                                \"name\": \"b\",\n"
                    + "                                                                \"type\": \"int256\"\n"
                    + "                                                        },\n"
                    + "                                                        {\n"
                    + "                                                                \"internalType\": \"int256\",\n"
                    + "                                                                \"name\": \"c\",\n"
                    + "                                                                \"type\": \"int256\"\n"
                    + "                                                        }\n"
                    + "                                                ],\n"
                    + "                                                \"internalType\": \"struct Proxy.Item[]\",\n"
                    + "                                                \"name\": \"items\",\n"
                    + "                                                \"type\": \"tuple[]\"\n"
                    + "                                        }\n"
                    + "                                ],\n"
                    + "                                \"internalType\": \"struct Proxy.Info[]\",\n"
                    + "                                \"name\": \"b\",\n"
                    + "                                \"type\": \"tuple[]\"\n"
                    + "                        },\n"
                    + "                        {\n"
                    + "                                \"internalType\": \"string\",\n"
                    + "                                \"name\": \"c\",\n"
                    + "                                \"type\": \"string\"\n"
                    + "                        }\n"
                    + "                ],\n"
                    + "                \"payable\": false,\n"
                    + "                \"stateMutability\": \"nonpayable\",\n"
                    + "                \"type\": \"function\"\n"
                    + "        },\n"
                    + "        {\n"
                    + "                \"payable\": false,\n"
                    + "                \"stateMutability\": \"nonpayable\",\n"
                    + "                \"type\": \"fallback\"\n"
                    + "        }\n"
                    + "]";

    // int a, Info[] memory b, string memory c
    /*
    	 * {
        "0": "int256: a 100",
        "1": "tuple(string,int256,tuple(int256,int256,int256)[])[]: b Hello world!,100,1,2,3,Hello world2!,200,5,6,7",
        "2": "string: c Hello world!"
    }

    struct Item {
        int a;
        int b;
        int c;
    }

    struct Info {
        string name;
        int count;
        Item[] items;
    }

    event output1(int a, Info[] b, string c);

    function() external {

    }

    function test(int a, Inf\o[] memory b, string memory c) public returns(int) {
        // emit output1(a, b, c);
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

    @Test
    public void testLoadABIJSON() {
        ABIObjectJSONWrapper abiFactory = new ABIObjectJSONWrapper();

        Contract contract = abiFactory.loadABIFile(new ByteArrayInputStream(abiDesc.getBytes()));

        Assert.assertEquals(2, contract.getFunctions().size());
        Assert.assertEquals(1, contract.getEvents().size());

        List<Function> functions = contract.getFunctions().get("test");
        ABIObject obj = functions.get(0).getInput().decode(encoded);

        String buffer = obj.encode();

        Assert.assertEquals(encoded, buffer);
    }

    @Test
    public void testEncodeByJSON() throws Exception {
        ABIObjectJSONWrapper abiFactory = new ABIObjectJSONWrapper();

        Contract contract = abiFactory.loadABIFile(new ByteArrayInputStream(abiDesc.getBytes()));

        List<Function> functions = contract.getFunctions().get("test");
        ABIObject obj = functions.get(0).getInput();

        List<String> args = new ArrayList<String>();
        args.add("100");

        // [{"name": "Hello world!", "count": 100, "items": [{"a": 1, "b": 2, "c": 3}]}, {"name":
        // "Hello world2", "count": 200, "items": [{"a": 1, "b": 2, "c": 3}]}]
        args.add(
                "[{\"name\": \"Hello world!\", \"count\": 100, \"items\": [{\"a\": 1, \"b\": 2, \"c\": 3}]}, {\"name\": \"Hello world2\", \"count\": 200, \"items\": [{\"a\": 5, \"b\": 6, \"c\": 7}]}]");
        args.add("Hello world!");

        ABIObject encodedObj = abiFactory.encode(obj, args);

        Assert.assertEquals(encoded, encodedObj.encode());
    }

    @Test
    public void testBytesEncode() throws Exception {
        String proxyDesc =
                "[{\n"
                        + "	\"constant\": false,\n"
                        + "	\"inputs\": [{\n"
                        + "		\"name\": \"_args\",\n"
                        + "		\"type\": \"string[]\"\n"
                        + "	}],\n"
                        + "	\"name\": \"commitTransaction\",\n"
                        + "	\"outputs\": [{\n"
                        + "		\"name\": \"\",\n"
                        + "		\"type\": \"string[]\"\n"
                        + "	}],\n"
                        + "	\"payable\": false,\n"
                        + "	\"stateMutability\": \"nonpayable\",\n"
                        + "	\"type\": \"function\"\n"
                        + "}, {\n"
                        + "	\"constant\": false,\n"
                        + "	\"inputs\": [{\n"
                        + "		\"name\": \"_args\",\n"
                        + "		\"type\": \"string[]\"\n"
                        + "	}],\n"
                        + "	\"name\": \"setMaxStep\",\n"
                        + "	\"outputs\": [],\n"
                        + "	\"payable\": false,\n"
                        + "	\"stateMutability\": \"nonpayable\",\n"
                        + "	\"type\": \"function\"\n"
                        + "}, {\n"
                        + "	\"constant\": true,\n"
                        + "	\"inputs\": [{\n"
                        + "		\"name\": \"_args\",\n"
                        + "		\"type\": \"string[]\"\n"
                        + "	}],\n"
                        + "	\"name\": \"getPaths\",\n"
                        + "	\"outputs\": [{\n"
                        + "		\"name\": \"\",\n"
                        + "		\"type\": \"string[]\"\n"
                        + "	}],\n"
                        + "	\"payable\": false,\n"
                        + "	\"stateMutability\": \"view\",\n"
                        + "	\"type\": \"function\"\n"
                        + "}, {\n"
                        + "	\"constant\": false,\n"
                        + "	\"inputs\": [{\n"
                        + "		\"name\": \"_args\",\n"
                        + "		\"type\": \"string[]\"\n"
                        + "	}],\n"
                        + "	\"name\": \"rollbackTransaction\",\n"
                        + "	\"outputs\": [{\n"
                        + "		\"name\": \"\",\n"
                        + "		\"type\": \"string[]\"\n"
                        + "	}],\n"
                        + "	\"payable\": false,\n"
                        + "	\"stateMutability\": \"nonpayable\",\n"
                        + "	\"type\": \"function\"\n"
                        + "}, {\n"
                        + "	\"constant\": true,\n"
                        + "	\"inputs\": [],\n"
                        + "	\"name\": \"getLatestTransaction\",\n"
                        + "	\"outputs\": [{\n"
                        + "		\"name\": \"\",\n"
                        + "		\"type\": \"string\"\n"
                        + "	}],\n"
                        + "	\"payable\": false,\n"
                        + "	\"stateMutability\": \"view\",\n"
                        + "	\"type\": \"function\"\n"
                        + "}, {\n"
                        + "	\"constant\": false,\n"
                        + "	\"inputs\": [{\n"
                        + "		\"name\": \"_transactionID\",\n"
                        + "		\"type\": \"string\"\n"
                        + "	}, {\n"
                        + "		\"name\": \"_seq\",\n"
                        + "		\"type\": \"uint256\"\n"
                        + "	}, {\n"
                        + "		\"name\": \"_path\",\n"
                        + "		\"type\": \"string\"\n"
                        + "	}, {\n"
                        + "		\"name\": \"_func\",\n"
                        + "		\"type\": \"string\"\n"
                        + "	}, {\n"
                        + "		\"name\": \"_args\",\n"
                        + "		\"type\": \"bytes\"\n"
                        + "	}],\n"
                        + "	\"name\": \"sendTransaction\",\n"
                        + "	\"outputs\": [{\n"
                        + "		\"name\": \"\",\n"
                        + "		\"type\": \"bytes\"\n"
                        + "	}],\n"
                        + "	\"payable\": false,\n"
                        + "	\"stateMutability\": \"nonpayable\",\n"
                        + "	\"type\": \"function\"\n"
                        + "}, {\n"
                        + "	\"constant\": true,\n"
                        + "	\"inputs\": [{\n"
                        + "		\"name\": \"_args\",\n"
                        + "		\"type\": \"string[]\"\n"
                        + "	}],\n"
                        + "	\"name\": \"getVersion\",\n"
                        + "	\"outputs\": [{\n"
                        + "		\"name\": \"\",\n"
                        + "		\"type\": \"string[]\"\n"
                        + "	}],\n"
                        + "	\"payable\": false,\n"
                        + "	\"stateMutability\": \"pure\",\n"
                        + "	\"type\": \"function\"\n"
                        + "}, {\n"
                        + "	\"constant\": false,\n"
                        + "	\"inputs\": [{\n"
                        + "		\"name\": \"_args\",\n"
                        + "		\"type\": \"string[]\"\n"
                        + "	}],\n"
                        + "	\"name\": \"rollbackAndDeleteTransaction\",\n"
                        + "	\"outputs\": [{\n"
                        + "		\"name\": \"\",\n"
                        + "		\"type\": \"string[]\"\n"
                        + "	}],\n"
                        + "	\"payable\": false,\n"
                        + "	\"stateMutability\": \"nonpayable\",\n"
                        + "	\"type\": \"function\"\n"
                        + "}, {\n"
                        + "	\"constant\": true,\n"
                        + "	\"inputs\": [],\n"
                        + "	\"name\": \"getLatestTransactionInfo\",\n"
                        + "	\"outputs\": [{\n"
                        + "		\"name\": \"\",\n"
                        + "		\"type\": \"string[]\"\n"
                        + "	}],\n"
                        + "	\"payable\": false,\n"
                        + "	\"stateMutability\": \"view\",\n"
                        + "	\"type\": \"function\"\n"
                        + "}, {\n"
                        + "	\"constant\": true,\n"
                        + "	\"inputs\": [{\n"
                        + "		\"name\": \"_str\",\n"
                        + "		\"type\": \"string\"\n"
                        + "	}],\n"
                        + "	\"name\": \"stringToUint256\",\n"
                        + "	\"outputs\": [{\n"
                        + "		\"name\": \"\",\n"
                        + "		\"type\": \"uint256\"\n"
                        + "	}],\n"
                        + "	\"payable\": false,\n"
                        + "	\"stateMutability\": \"pure\",\n"
                        + "	\"type\": \"function\"\n"
                        + "}, {\n"
                        + "	\"constant\": false,\n"
                        + "	\"inputs\": [{\n"
                        + "		\"name\": \"_transactionID\",\n"
                        + "		\"type\": \"string\"\n"
                        + "	}, {\n"
                        + "		\"name\": \"_path\",\n"
                        + "		\"type\": \"string\"\n"
                        + "	}, {\n"
                        + "		\"name\": \"_func\",\n"
                        + "		\"type\": \"string\"\n"
                        + "	}, {\n"
                        + "		\"name\": \"_args\",\n"
                        + "		\"type\": \"bytes\"\n"
                        + "	}],\n"
                        + "	\"name\": \"constantCall\",\n"
                        + "	\"outputs\": [{\n"
                        + "		\"name\": \"\",\n"
                        + "		\"type\": \"bytes\"\n"
                        + "	}],\n"
                        + "	\"payable\": false,\n"
                        + "	\"stateMutability\": \"nonpayable\",\n"
                        + "	\"type\": \"function\"\n"
                        + "}, {\n"
                        + "	\"constant\": true,\n"
                        + "	\"inputs\": [{\n"
                        + "		\"name\": \"_args\",\n"
                        + "		\"type\": \"string[]\"\n"
                        + "	}],\n"
                        + "	\"name\": \"getMaxStep\",\n"
                        + "	\"outputs\": [{\n"
                        + "		\"name\": \"\",\n"
                        + "		\"type\": \"string[]\"\n"
                        + "	}],\n"
                        + "	\"payable\": false,\n"
                        + "	\"stateMutability\": \"view\",\n"
                        + "	\"type\": \"function\"\n"
                        + "}, {\n"
                        + "	\"constant\": true,\n"
                        + "	\"inputs\": [{\n"
                        + "		\"name\": \"_args\",\n"
                        + "		\"type\": \"string[]\"\n"
                        + "	}],\n"
                        + "	\"name\": \"getTransactionInfo\",\n"
                        + "	\"outputs\": [{\n"
                        + "		\"name\": \"\",\n"
                        + "		\"type\": \"string[]\"\n"
                        + "	}],\n"
                        + "	\"payable\": false,\n"
                        + "	\"stateMutability\": \"view\",\n"
                        + "	\"type\": \"function\"\n"
                        + "}, {\n"
                        + "	\"constant\": false,\n"
                        + "	\"inputs\": [{\n"
                        + "		\"name\": \"_args\",\n"
                        + "		\"type\": \"string[]\"\n"
                        + "	}],\n"
                        + "	\"name\": \"addPath\",\n"
                        + "	\"outputs\": [],\n"
                        + "	\"payable\": false,\n"
                        + "	\"stateMutability\": \"nonpayable\",\n"
                        + "	\"type\": \"function\"\n"
                        + "}, {\n"
                        + "	\"constant\": false,\n"
                        + "	\"inputs\": [{\n"
                        + "		\"name\": \"_args\",\n"
                        + "		\"type\": \"string[]\"\n"
                        + "	}],\n"
                        + "	\"name\": \"startTransaction\",\n"
                        + "	\"outputs\": [{\n"
                        + "		\"name\": \"\",\n"
                        + "		\"type\": \"string[]\"\n"
                        + "	}],\n"
                        + "	\"payable\": false,\n"
                        + "	\"stateMutability\": \"nonpayable\",\n"
                        + "	\"type\": \"function\"\n"
                        + "}, {\n"
                        + "	\"constant\": false,\n"
                        + "	\"inputs\": [{\n"
                        + "		\"name\": \"_args\",\n"
                        + "		\"type\": \"string[]\"\n"
                        + "	}],\n"
                        + "	\"name\": \"deletePathList\",\n"
                        + "	\"outputs\": [],\n"
                        + "	\"payable\": false,\n"
                        + "	\"stateMutability\": \"nonpayable\",\n"
                        + "	\"type\": \"function\"\n"
                        + "}, {\n"
                        + "	\"inputs\": [],\n"
                        + "	\"payable\": false,\n"
                        + "	\"stateMutability\": \"nonpayable\",\n"
                        + "	\"type\": \"constructor\"\n"
                        + "}]";

        ABIObjectJSONWrapper abiFactory = new ABIObjectJSONWrapper();

        Contract contract = abiFactory.loadABIFile(new ByteArrayInputStream(proxyDesc.getBytes()));

        List<Function> functions = contract.getFunctions().get("constantCall");
        ABIObject obj = functions.get(0).getInput();

        List<String> args = new ArrayList<String>();
        args.add("arg112345678901234567890123456789012345678901234567890");
        args.add("arg212345678901234567890123456789012345678901234567890");
        args.add("arg312345678901234567890123456789012345678901234567890");
        args.add("0x123456789874321");

        ABIObject encodedObj = abiFactory.encode(obj, args);
        String buffer = encodedObj.encode();

        List<String> decodeArgs = abiFactory.decode(obj, buffer);

        Assert.assertArrayEquals(args.toArray(), decodeArgs.toArray());
    }

    public static void main(String[] args) {
        Pattern pattern = Pattern.compile("\\[(\\d*)\\]");

        String str = "tuple[10]";

        Matcher matcher = pattern.matcher(str);

        System.out.println("start...");
        while (matcher.find()) {
            System.out.println("count: " + matcher.group(1));
        }
    }
}
