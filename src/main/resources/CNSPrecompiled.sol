pragma solidity >=0.5.0 <0.6.0;

contract CNSPrecompiled {

    function insert(string memory name, string memory version, string memory addr, string memory abiStr) public;
    
    function selectByName(string memory name) public view returns (string memory);
    
    function selectByNameAndVersion(string memory name, string memory version) public view returns (string memory);
    
    function getContractAddress(string memory name, string memory version) public view returns (string memory);
}
