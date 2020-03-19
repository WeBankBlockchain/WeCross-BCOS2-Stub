pragma solidity ^0.4.24;
pragma experimental ABIEncoderV2;

contract HelloWeCross {
    string[] ss;

    function set(string[] memory _ss) public returns (string[] memory) {
        ss = _ss;
        return _ss;
    }

    function echo(string[] memory ss) public returns(string[] memory) {
        return ss;
    }
}