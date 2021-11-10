pragma solidity >=0.4.22 <0.6.0;
pragma experimental ABIEncoderV2;

import "./LuyuSDK.sol";

contract SDKExample is LuyuContract {
    function sendTxToFunc1() public returns (uint256) {
        string memory path = "payment.chain.resource";
        string memory method = "func1";
        string[] memory args = new string[](2);
        args[0] = "aaa";
        args[1] = "bbb";
        string memory luyuIdentity = "0xtestluyuaddress";
        string memory callbackMethod = "func1Callback";
        uint256 nonce = luyuSendTransaction(
            path,
            method,
            args,
            luyuIdentity,
            callbackMethod
        );
        return nonce;
    }

    function func1Callback(
        uint256 nonce,
        string memory result0,
        int256 result1,
        string memory result2
    ) public {
        // callback would be called if success
        // params: nonce of the coresponding sdk request followed by result
    }

    function callToFunc2() public returns (uint256) {
        string memory path = "payment.chain.resource";
        string memory method = "func2";
        string[] memory args = new string[](2);
        args[0] = "aaa";
        args[1] = "bbb";
        string memory luyuIdentity = "0xtestluyuaddress";
        string memory callbackMethod = "func2Callback";

        uint256 nonce = luyuCall(
            path,
            method,
            args,
            luyuIdentity,
            callbackMethod
        );

        return nonce;
    }

    function func2Callback(
        uint256 nonce,
        int256 result0,
        string memory result1
    ) public {
        // callback would be called if success
        // params: nonce of the coresponding sdk request followed by result
    }
}
