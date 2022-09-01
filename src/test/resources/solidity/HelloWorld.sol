pragma solidity >=0.5.0 <0.6.0;

contract HelloWorld {
    string name = "HelloWorld!";

    constructor(string memory n) public {
        name = n;
    }

    function get() public view returns (string memory) {
        return name;
    }

    function set(string memory n) public {
        name = n;
    }

    function get1(string memory s) public pure returns (string memory) {
        return s;
    }

    function get2(string memory s1, string memory s2) public pure returns (string memory) {
        return string(abi.encodePacked(s1, s2));
    }
}
