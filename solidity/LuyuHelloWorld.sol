pragma solidity >=0.4.22 <0.6.0;
pragma experimental ABIEncoderV2;

import "./LuyuSDK.sol";

contract LuyuHelloWorld is LuyuContract {
    function testSendTx(string memory arg) public returns (uint256) {
        string memory path = "payment.bcos_gm.HelloWorld";
        string memory method = "set";
        string[] memory args = new string[](1);
        args[0] = arg;
        string memory luyuIdentity = "0xtestluyuaddress";
        string memory callbackMethod = "HelloWorldSetCallback";
        uint256 nonce = luyuSendTransaction(
            path,
            method,
            args,
            luyuIdentity,
            callbackMethod
        );
        test = "";
        return nonce;
    }

    function HelloWorldSetCallback(uint256 nonce) public {
        test = "callback called";
        testCall();
    }

    function testCall() public returns (uint256) {
        string memory path = "payment.bcos_gm.HelloWorld";
        string memory method = "get";
        string[] memory args = new string[](0);
        string memory luyuIdentity = "0xtestluyuaddress";
        string memory callbackMethod = "HelloWorldGetCallback";
        uint256 nonce = luyuCall(
            path,
            method,
            args,
            luyuIdentity,
            callbackMethod
        );
        return nonce;
    }

    event Success(string);
    string test = "original";

    function HelloWorldGetCallback(uint256 nonce, string memory return0)
        public
    {
        test = return0;
    }

    function get() public view returns (string memory) {
        return test;
    }
}
