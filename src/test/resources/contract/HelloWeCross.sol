pragma solidity >=0.4.24 <0.6.11;
pragma experimental ABIEncoderV2;

contract HelloWeCross {
    string[] ss;

    function set(string[] memory _ss) public returns (string[] memory) {
        ss = _ss;
        return ss;
    }

    function getAndClear() public returns(string[] memory) {
        string[] memory _ss = ss;
        ss.length = 0;
        return _ss;
    }

    function get() public view returns(string[] memory) {
        return ss;
    }
}