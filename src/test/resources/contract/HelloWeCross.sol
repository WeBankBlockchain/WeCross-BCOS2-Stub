pragma solidity ^0.4.24;
pragma experimental ABIEncoderV2;

contract HelloWeCross {
    string[] ss;

    function set(string[] memory _ss) public returns (string[] memory) {
        ss = _ss;
        return ss;
    }

    function get(string[] memory ss) public constant returns(string[] memory) {
        return ss;
    }
}