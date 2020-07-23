/*
*   v1.0.0-rc4
*   proxy contract for WeCross
*   main entrance of all contract call
*/

pragma solidity >=0.5.0 <0.6.0;

pragma experimental ABIEncoderV2;

contract ParallelConfigPrecompiled
{
    function registerParallelFunctionInternal(address, string memory, uint256) public returns (int);
    function unregisterParallelFunctionInternal(address, string memory) public returns (int);
}

contract ParallelContract
{
    ParallelConfigPrecompiled precompiled = ParallelConfigPrecompiled(0x1006);

    function registerParallelFunction(string memory functionName, uint256 criticalSize) public
    {
        precompiled.registerParallelFunctionInternal(address(this), functionName, criticalSize);
    }

    function unregisterParallelFunction(string memory functionName) public
    {
        precompiled.unregisterParallelFunctionInternal(address(this), functionName);
    }

    function enableParallel() public;
    function disableParallel() public;
}

contract WeCrossProxy is ParallelContract {

    string constant version = "v1.0.0-rc4";

    // per step of transaction
    struct TransactionStep {
        string path;
        uint256 timestamp;
        address contractAddress;
        string func;
        bytes args;
    }

    // information of transaction
    struct TransactionInfo {
        string[] allPaths;  // all paths related to this transaction
        string[] paths;     // paths related to current chain
        address[] contractAddresses; // locked addressed in current chain
        uint8 status;    // 0-Start 1-Commit 2-Rollback
        uint256 startTimestamp;
        uint256 commitTimestamp;
        uint256 rollbackTimestamp;
        uint256[] seqs;   // sequence of each step
        uint256 stepNum;  // step number
    }

    struct ContractInfo {
        bool locked;     // isolation control, read-committed
        string path;
        string transactionID;
    }

    mapping(address => ContractInfo) lockedContracts;

    mapping(string => TransactionInfo) transactions;      // key: transactionID

    mapping(string => TransactionStep) transactionSteps;  // key: transactionID||seq

    /*
    * record all tansactionIDs
    * head: point to the current tansaction to be checked
    * tail: point to the next position for added tansaction
    */
    uint256 head = 0;
    uint256 tail = 0;
    string[] tansactionQueue;

    string constant revertFlag = "_revert";
    string constant nullFlag = "null";
    string constant successFlag = "0";
    byte   constant separator = '.';
    uint256 constant addressLen = 42;
    uint256 maxStep = 32;
    string[] pathList;

    CNSPrecompiled cns;
    constructor() public {
        cns = CNSPrecompiled(0x1004);
    }

    function getVersion(string[] memory _args) public pure
    returns(string[] memory)
    {    
        string[] memory result = new string[](1);
        result[0] = version;
        return result;
    }
    
    function getMaxStep(string[] memory _args) public view
    returns(string[] memory)
    {
        string[] memory result = new string[](1);
        result[0] = uint256ToString(maxStep);
        return result;
    }

    function setMaxStep(string[] memory _args) public
    {
        maxStep = stringToUint256(_args[0]);
    }

    function addPath(string[] memory _args) public
    {
        pathList.push(_args[0]);
    }

    function getPaths(string[] memory _args) public view
    returns (string[] memory)
    {
        return pathList;
    }

    function deletePathList(string[] memory _args) public
    {
        pathList.length = 0;
    }

    /*
    * deploy contract by contract binary code
    */
    function deployContract(bytes memory bin) public returns(address addr) {
        bool ok = false;
        assembly {
            addr := create(0,add(bin,0x20), mload(bin))
            ok := gt(extcodesize(addr),0)
        }
        require(ok, "deploy contract failed");
    }

    /**
    * deploy contract and register contract to cns
    */
    function deployContractWithRegisterCNS(string memory name, string memory version, bytes memory bin, string memory abi) public returns(address addr) {
        // deploy contract first
        addr = deployContract(bin);
        // register to cns
        registerCNS(name, version, addressToString(addr), abi);
    }

    /**
    * register contract to cns
    */
    function registerCNS(string memory name, string memory version, string memory addr, string memory abi) public {
        // check if version info exist ???
        int ret = cns.insert(name, version, addr, abi);
        require(1 == ret, "register cns failed");
    }

    /**
    * select cns by name
    */
    function selectByName(string memory name) public view returns(string memory) {
        return cns.selectByName(name);
    }

    /**
    * select cns by name and version
    */
    function selectByNameAndVersion(string memory name, string memory version) public view returns(string memory) {
        return cns.selectByNameAndVersion(name, version);
    }

    // constant call
    function constantCall(string memory _transactionID, string memory _path, string memory _func, bytes memory _args) public
    returns(bytes memory)
    {
        address addr = getAddressByPath(_path);

        if(sameString(_transactionID, "0")) {
             return callContract(addr, _func, _args);
        }

        if(!isExistedTransaction(_transactionID)) {
            revert("transaction id not found");
        }

        if(!sameString(lockedContracts[addr].transactionID, _transactionID)) {
            revert("unregistered contract");
        }
        
        if(!sameString(lockedContracts[addr].path, _path)) {
            revert("unregistered path");
        }

        return callContract(addr, _func, _args);
    }

    function enableParallel() public {
        registerParallelFunction("parallelSendTransaction(string,string,string,bytes)", 1);
        registerParallelFunction("parallelSendTransaction(string,string,string,string,bytes)", 2);
        registerParallelFunction("parallelSendTransaction(string,string,string,string,string,bytes)", 3);

        registerParallelFunction("parallelSendTransactionByAddress(string,address,string,bytes)", 1);
        registerParallelFunction("parallelSendTransactionByAddress(string,string,address,string,bytes)", 2);
    }

    function disableParallel() public {
        unregisterParallelFunction("parallelSendTransaction(string,string,string,bytes)");
        unregisterParallelFunction("parallelSendTransaction(string,string,string,string,bytes)");
        unregisterParallelFunction("parallelSendTransaction(string,string,string,string,string,bytes)");

        unregisterParallelFunction("parallelSendTransactionByAddress(string,address,string,bytes)");
        unregisterParallelFunction("parallelSendTransactionByAddress(string,string,address,string,bytes)");
    }

    /**
* Interface for parallel transactions
*/
    function parallelSendTransaction(string memory parallelTag0, string memory parallelTag1, string memory parallelTag2, string memory _path, string memory _func, bytes memory _args) public returns(bytes memory) {
        address addr = getAddressByPath(_path);
        return callContract(addr, _func, _args);
    }

    /**
    * Interface for parallel transactions
    */
    function parallelSendTransaction(string memory parallelTag0, string memory parallelTag1, string memory _path, string memory _func, bytes memory _args) public returns(bytes memory) {
        address addr = getAddressByPath(_path);
        return callContract(addr, _func, _args);
    }

    /**
    * Interface for parallel transactions
    */
    function parallelSendTransaction(string memory parallelTag, string memory _path, string memory _func, bytes memory _args) public returns(bytes memory) {
        address addr = getAddressByPath(_path);
        return callContract(addr, _func, _args);
    }

    function parallelSendTransactionByAddress(string memory parallelTag, address addr, string memory _func, bytes memory _args) public returns(bytes memory) {
        return callContract(addr, _func, _args);
    }

    function parallelSendTransactionByAddress(string memory parallelTag0, string memory parallelTag1, address addr, string memory _func, bytes memory _args) public returns(bytes memory) {
        return callContract(addr, _func, _args);
    }

    function sendTransactionByAddress(address addr, string memory _func, bytes memory _args) public returns(bytes memory) {
        return callContract(addr, _func, _args);
    }

    function sendTransaction(string memory _path, string memory _func, bytes memory _args) public returns(bytes memory) {
        address addr = getAddressByPath(_path);
        return callContract(addr, _func, _args);
    }

    // non-constant call
    function sendTransaction(string memory _transactionID, uint256 _seq, string memory _path, string memory _func, bytes memory _args) public
    returns(bytes memory)
    {
        address addr = getAddressByPath(_path);

        if(sameString(_transactionID, "0")) {
            if(lockedContracts[addr].locked) {
                revert("contract is locked by unfinished transaction");
            }
             return callContract(addr, _func, _args);
        }

        if(!isExistedTransaction(_transactionID)) {
            revert("transaction id not found");
        }

        if(transactions[_transactionID].status == 1) {
            revert("has committed");
        }

        if(transactions[_transactionID].status == 2) {
            revert("has rolledback");
        }

        if(!sameString(lockedContracts[addr].transactionID, _transactionID)) {
            revert("unregistered contract");
        }

        if(!sameString(lockedContracts[addr].path, _path)) {
            revert("unregistered path");
        }

        if(!isNewStep(_transactionID, _seq)) {
            revert("duplicate seq");
        }
        
        // recode step
        transactionSteps[getTransactionStepKey(_transactionID, _seq)] = TransactionStep(
            _path,
            now,
            addr,
            _func,
            _args
        );

        // recode seq
        uint256 num = transactions[_transactionID].stepNum;
        transactions[_transactionID].seqs[num] = _seq;
        transactions[_transactionID].stepNum = num + 1;

        return callContract(addr, _func, _args);
    }

    /*
    * @param transactionID || num || path1 || path2 || ...
    * the first num paths are related to current chain
    * result: 0-success
    */
    function startTransaction(string[] memory _args) public
    returns(string[] memory)
    {
        string[] memory res = new string[](1);
        res[0] = successFlag;
                
        uint256 len = _args.length;
        if(len < 4) {
            revert("invalid arguments");
        }
        
        uint256 num = stringToUint256(_args[1]);
        if((num == 0) || ((2*num+2) > len)) {
           revert("invalid arguments"); 
        }
        
        string memory transactionID = _args[0];
        
        if(isExistedTransaction(transactionID)) {
            revert("transaction existed");
        }

        address[] memory contracts = new address[](num);
        string[] memory allPaths = new string[](len-num-2);
        string[] memory paths = new string[](num);

        // recode ACL
        for(uint256 i = 0; i < num; i++) {
            paths[i] = _args[i+2];
            address addr = getAddressByPath(_args[i+2]);
            contracts[i] = addr;

            if(lockedContracts[addr].locked) {
                revert("contract conflict");
            }
            lockedContracts[addr].locked = true;
            lockedContracts[addr].path = _args[i+2];
            lockedContracts[addr].transactionID = transactionID;
        }
        
        for(uint256 i = 0; i < len-2-num; i++)
        {
            allPaths[i] = _args[i+2+num];
        }

        uint256[] memory temp = new uint256[](maxStep);
        // recode transaction
        transactions[transactionID] = TransactionInfo(
            allPaths,
            paths,
            contracts,
            0,
            now,
            0,
            0,
            temp,
            0
        );

        addTransaction(transactionID);
        
        return res;
    }

    /*
    *  @param transactionID
    * result: 0-success
    */
    function commitTransaction(string[] memory _args) public
    returns(string[] memory)
    {
        string[] memory res = new string[](1);
        res[0] = successFlag;
        
        if(_args.length != 1) {
            revert("invalid arguments");
        }
        
        string memory transactionID = _args[0];
        
        if(!isExistedTransaction(transactionID)) {
            revert("transaction id not found");
        }

        // has committed
        if(transactions[transactionID].status == 1) {
            return res;
        }

        if(transactions[transactionID].status == 2) {
            revert("has rolledback");
        }

        transactions[transactionID].commitTimestamp = now;
        transactions[transactionID].status = 1;

        deleteLockedContracts(transactionID);

        return res;
    }

    /*
    *  @param transactionID
    * result: 0-success
    */
    function rollbackTransaction(string[] memory _args) public
    returns(string[] memory)
    {
        string[] memory res = new string[](1);
        res[0] = successFlag;
        
        if(_args.length != 1) {
            revert("invalid arguments");
        }
        
        string memory transactionID = _args[0];
        
        if(!isExistedTransaction(transactionID)) {
            revert("transaction id not found");
        }


        if(transactions[transactionID].status == 1) {
            revert("has committed");
        }

        // has rolledback
        if(transactions[transactionID].status == 2) {
            return res;
        }

        uint256 stepNum = transactions[transactionID].stepNum;
        for(uint256 i = stepNum; i > 0; i--) {
            uint256 seq = transactions[transactionID].seqs[i-1];
            string memory key = getTransactionStepKey(transactionID, seq);

            string memory func = transactionSteps[key].func;
            address contractAddress = transactionSteps[key].contractAddress;
            bytes memory args = transactionSteps[key].args;

            // call revert function
           callContract(contractAddress, getRevertFunc(func, revertFlag), args);
        }

        transactions[transactionID].rollbackTimestamp = now;
        transactions[transactionID].status = 2;

        deleteLockedContracts(transactionID);

        return res;
    }

    /*
    *  @param transactionID
    * result with json form
    * "null": transaction not found
    * example:
    {
        "transactionID": "1",
        "status": 1,
        "allPaths":["a.b.c1","a.b.c2","a.b1.c3"],
        "paths": ["a.b.c1","a.b.c2"],
        "startTimestamp": "123",
        "commitTimestamp": "456",
        "rollbackTimestamp": "789",
        "transactionSteps": [{
                "seq": 0,
                "contract": "0x12",
                "path": "a.b.c1",
                "timestamp": "123",
                "func": "test1(string)",
                "args": "aaa"
            },
            {
                "seq": 1,
                "contract": "0x12",
                "path": "a.b.c2",
                "timestamp": "123",
                "func": "test2(string)",
                "args": "bbb"
            }
        ]
    }
    */
    function getTransactionInfo(string[] memory _args) public view
    returns(string[] memory)
    {
        string[] memory res = new string[](1);
        
        if(_args.length != 1) {
            revert("invalid arguments");
        }
        
        string memory transactionID = _args[0];
        
        if(!isExistedTransaction(transactionID)) {
            res[0] = nullFlag;
            return res;
        }
        
        uint256 len1 = transactions[transactionID].allPaths.length;
        string memory allPaths = string(abi.encodePacked("[", "\"", transactions[transactionID].allPaths[0], "\""));
        for(uint256 i = 1; i < len1; i++) {
            allPaths = string(abi.encodePacked(allPaths, ",", "\"", transactions[transactionID].allPaths[i], "\""));
        }
        allPaths = string(abi.encodePacked(allPaths, "]"));

        uint256 len2 = transactions[transactionID].paths.length;
        string memory paths = string(abi.encodePacked("[", "\"", transactions[transactionID].paths[0], "\""));
        for(uint256 i = 1; i < len2; i++) {
            paths = string(abi.encodePacked(paths, ",", "\"", transactions[transactionID].paths[i], "\""));
        }
        paths = string(abi.encodePacked(paths, "]"));
        
        res[0] = string(abi.encodePacked("{\"transactionID\":", "\"", transactionID, "\",",
            "\"status\":", uint256ToString(transactions[transactionID].status), ",",
            "\"allPaths\":", allPaths, ",",
            "\"paths\":", paths, ",",
            "\"startTimestamp\":", "\"", uint256ToString(transactions[transactionID].startTimestamp), "\",",
            "\"commitTimestamp\":", "\"", uint256ToString(transactions[transactionID].commitTimestamp), "\",",
            "\"rollbackTimestamp\":", "\"", uint256ToString(transactions[transactionID].rollbackTimestamp), "\",",
            transactionStepArrayToJson(transactionID, transactions[transactionID].seqs, transactions[transactionID].stepNum), "}")
            );
        return res;
    }

    // called by router to check transaction status
    function getLatestTransactionInfo() public view
    returns(string[] memory)
    {
        string[] memory res = new string[](1);
        
        string memory transactionID;
        
        if(head == tail) {
            res[0] = nullFlag;
            return res;
        } else {
            transactionID = tansactionQueue[uint256(head)];
        }

        string[] memory args = new string[](1);
        args[0] = transactionID;
        return getTransactionInfo(args);
    }
    
    // called by router to rollbach transaction
    function rollbackAndDeleteTransaction(string[] memory _args) public
    returns (string[] memory)
    {
        rollbackTransaction(_args);
        return deleteTransaction(_args[0]);
    }

    function getLatestTransaction() public view
    returns (string memory) 
    {
        if(head == tail) {
            return nullFlag;
        } else {
            return tansactionQueue[uint256(head)];
        }
    }
    
    function addTransaction(string memory _transactionID) internal
    {
        tail++;
        tansactionQueue.push(_transactionID);
    }
    
    function deleteTransaction(string memory _transactionID) internal
    returns (string[] memory)
    {
        string[] memory res = new string[](1);
        res[0] = successFlag;
        
        if(head == tail) {
            revert("delete nonexistent transaction");
        }
        
        if(!sameString(tansactionQueue[head], _transactionID)) {
            revert("delete unmatched transaction");
        }

        head++;
        return res;
    }
    
     // internal call
    function callContract(address _contractAddress, string memory _sig, bytes memory _args) internal
    returns(bytes memory result)
    {
        bytes memory sig = abi.encodeWithSignature(_sig);
        bool success;
        (success, result) = address(_contractAddress).call(abi.encodePacked(sig, _args));
        require(success, "Call traget contract failed!");
    }

    // retrive address from CNS
    function getAddressByPath(string memory _path) internal view
    returns (address)
    {
        string memory name = getNameByPath(_path);
        string memory strJson = cns.selectByName(name);

        bytes memory str = bytes(strJson);
        uint256 len = str.length;
        
        uint256 index = newKMP(str, bytes("\"sserdda\""));

        bytes memory addr = new bytes(addressLen);
        uint256 start = 0;
        for(uint256 i = index; i < len; i++) {
            if(str[i] == byte('0') && str[i+1] == byte('x')) {
                start = i;
                break;
            }
        }

        for(uint256 i = 0; i < addressLen; i++) {
            addr[i] = str[start + i];
        }

        return bytesToAddress(addr);
    }

    // input must be a valid path like "zone.chain.resource"
    function getNameByPath(string memory _path) internal pure
    returns (string memory)
    {
        bytes memory path = bytes(_path);
        uint256 len = path.length;
        uint256 nameLen = 0;
        uint256 index = 0;
        for(uint256 i = len - 1; i > 0; i--) {
            if(path[i] == separator) {
                index = i + 1;
                break;
            } else {
                nameLen++;
            }
        }

        bytes memory name = new bytes(nameLen);
        for(uint256 i = 0; i < nameLen; i++) {
            name[i] = path[index++];
        }

        return string(name);
    }

    // "transactionSteps": [{"seq": 0, "contract": "0x12","path": "a.b.c","timestamp": "123","func": "test1(string)","args": "aaa"},{"seq": 1, "contract": "0x12","path": "a.b.c","timestamp": "123","func": "test2(string)","args": "bbb"}]
    function transactionStepArrayToJson(string memory _transactionID, uint256[] memory _seqs, uint256 _len) internal view
    returns(string memory result)
    {
        if(_len == 0) {
            return "\"transactionSteps\":[]";
        }

        result = string(abi.encodePacked("\"transactionSteps\":[", transactionStepToJson(transactionSteps[getTransactionStepKey(_transactionID, _seqs[0])], _seqs[0])));
        for(uint256 i = 1; i < _len; i++) {
                    result = string(abi.encodePacked(result, ",", transactionStepToJson(transactionSteps[getTransactionStepKey(_transactionID, _seqs[i])], _seqs[i])));
        }

        return string(abi.encodePacked(result, "]"));
    }

    // {"seq": 0, "contract": "0x12","path": "a.b.c","timestamp": "123","func": "test2(string)","args": "bbb"}
    function transactionStepToJson(TransactionStep memory _step, uint256 _seq) internal pure
    returns(string memory)
    {
        return string(abi.encodePacked("{\"seq\":", uint256ToString(_seq), ",",
                "\"contract\":", "\"", addressToString(_step.contractAddress), "\",",
                "\"path\":", "\"", _step.path, "\",",
                "\"timestamp\":", "\"", uint256ToString(_step.timestamp), "\",",
                "\"func\":", "\"", _step.func, "\",",
                "\"args\":", "\"", bytesToHexString(_step.args), "\"}")
                );
    }

    function isExistedTransaction(string memory _transactionID) internal view
    returns (bool)
    {
        return transactions[_transactionID].startTimestamp != 0;
    }

    function isNewStep(string memory _transactionID, uint256 _seq) internal view
    returns(bool)
    {
        for(uint256 i = 0; i < transactions[_transactionID].stepNum; i++) {
            if(transactions[_transactionID].seqs[i] == _seq) {
                return false;
            }
        }
        return true;
    }

    function deleteLockedContracts(string memory _transactionID) internal
    {
        uint256 len = transactions[_transactionID].contractAddresses.length;
        for(uint256 i = 0; i < len; i++) {
            address contractAddress = transactions[_transactionID].contractAddresses[i];
            delete lockedContracts[contractAddress];
        }
    }

    /* a famous algorithm for finding substring
       match starts with tail, and the target must be "\"sserdda\""
    */
    function newKMP(bytes memory _str, bytes memory _target) internal pure
    returns (uint256)
    {
        int256 strLen = int256(_str.length);
        int256 tarLen = int256(_target.length);
        
        // next array for target "\"sserdda\""
        int8[9] memory nextArray = [-1,0,0,0,0,0,0,0,0];

        int256 i = strLen;
        int256 j = 0;

        while (i > 0 && j < tarLen) {
            if (j == -1 || _str[uint256(i-1)] == _target[uint256(j)]) {
                i--;
                j++;
            } else {
                j = int256(nextArray[uint256(j)]);
            }
        }

        if ( j == tarLen) {
            return uint256(i + tarLen);
        }

        return 0;
    }

    // func(string,uint256) => func_flag(string,uint256)
    function getRevertFunc(string memory _func, string memory _revertFlag) internal pure
    returns(string memory)
    {
        bytes memory funcBytes = bytes(_func);
        bytes memory flagBytes = bytes(_revertFlag);
        uint256 funcLen = funcBytes.length;
        uint256 flagLen = flagBytes.length;
        bytes memory newFunc = new bytes(funcLen + flagLen);

        byte c = byte('(');
        uint256 index = 0;
        uint256 point = 0;

        for(uint256 i = 0; i < funcLen; i++) {
            if(funcBytes[i] != c) {
                newFunc[index++] = funcBytes[i];
            } else {
                point = i;
                break;
            }
        }

        for(uint256 i = 0; i < flagLen; i++) {
            newFunc[index++] = flagBytes[i];
        }

        for(uint256 i = point; i < funcLen; i++) {
            newFunc[index++] = funcBytes[i];
        }

        return string(newFunc);
    }

    function getTransactionStepKey(string memory _transactionID, uint256 _seq) internal pure
    returns(string memory)
    {
        return string(abi.encodePacked(_transactionID, uint256ToString(_seq)));
    }
    
    function sameString(string memory _str1, string memory _str2) internal pure
    returns (bool)
    {
        return keccak256(abi.encodePacked(_str1)) == keccak256(abi.encodePacked(_str2));
    }

    function hexStringToBytes(string memory _hexStr) internal pure
    returns (bytes memory)
    {
        bytes memory bts = bytes(_hexStr);
        require(bts.length%2 == 0);
        bytes memory result = new bytes(bts.length/2);
        uint len = bts.length/2;
        for (uint i = 0; i < len; ++i) {
            result[i] = byte(fromHexChar(uint8(bts[2*i])) * 16 +
                fromHexChar(uint8(bts[2*i+1])));
        }
        return result;
    }

    function fromHexChar(uint8 _char) internal pure
    returns (uint8)
    {
        if (byte(_char) >= byte('0') && byte(_char) <= byte('9')) {
            return _char - uint8(byte('0'));
        }
        if (byte(_char) >= byte('a') && byte(_char) <= byte('f')) {
            return 10 + _char - uint8(byte('a'));
        }
        if (byte(_char) >= byte('A') && byte(_char) <= byte('F')) {
            return 10 + _char - uint8(byte('A'));
        }
    }

    function stringToUint256(string memory _str) public pure
    returns (uint256)
    {
        bytes memory bts = bytes(_str);
        uint256 result = 0;
        uint256 len = bts.length;
        for (uint256 i = 0; i < len; i++) {
            if (uint8(bts[i]) >= 48 && uint8(bts[i]) <= 57) {
                result = result * 10 + (uint8(bts[i]) - 48);
            }
        }
        return result;
    }
    
    function uint256ToString(uint256 _value) internal pure
    returns (string memory)
    {
        bytes32 result;
        if (_value == 0) {
            return "0";
        } else {
            while (_value > 0) {
                result = bytes32(uint(result) / (2 ** 8));
                result |= bytes32(((_value % 10) + 48) * 2 ** (8 * 31));
                _value /= 10;
            }
        }
        return bytes32ToString(result);
    }

    function bytes32ToString(bytes32 _bts32) internal pure
    returns (string memory)
    {

       bytes memory result = new bytes(_bts32.length);

       uint len = _bts32.length;
       for(uint i = 0; i < len; i++) {
           result[i] = _bts32[i];
       }

       return string(result);
    }

    function bytesToAddress(bytes memory _address) internal pure
    returns (address)
    {
        uint160 result = 0;
        uint160 b1;
        uint160 b2;
        for (uint i = 2; i < 2 + 2 * 20; i += 2) {
            result *= 256;
            b1 = uint160(uint8(_address[i]));
            b2 = uint160(uint8(_address[i + 1]));
            if ((b1 >= 97) && (b1 <= 102)) {
                b1 -= 87;
            } else if ((b1 >= 65) && (b1 <= 70)) {
                b1 -= 55;
            } else if ((b1 >= 48) && (b1 <= 57)) {
            b1 -= 48;
            }

            if ((b2 >= 97) && (b2 <= 102)) {
                b2 -= 87;
            } else if ((b2 >= 65) && (b2 <= 70)) {
                b2 -= 55;
            } else if ((b2 >= 48) && (b2 <= 57)) {
                b2 -= 48;
            }
            result += (b1 * 16 + b2);
        }
        return address(result);
    }

    function addressToString(address _addr) internal pure
    returns (string memory)
    {
        bytes memory result = new bytes(40);
        for (uint i = 0; i < 20; i++) {
            byte temp = byte(uint8(uint(_addr) / (2 ** (8 * (19 - i)))));
            byte b1 = byte(uint8(temp) / 16);
            byte b2 = byte(uint8(temp) - 16 * uint8(b1));
            result[2 * i] = convert(b1);
            result[2 * i + 1] = convert(b2);
        }
        return string(abi.encodePacked("0x", string(result)));
    }

    function bytesToHexString(bytes memory _bts) internal pure
    returns (string memory result)
    {
        uint256 len = _bts.length;
        bytes memory s = new bytes(len * 2);
        for (uint256 i = 0; i < len; i++) {
            byte befor = byte(_bts[i]);
            byte high = byte(uint8(befor) / 16);
            byte low = byte(uint8(befor) - 16 * uint8(high));
            s[i*2] = convert(high);
            s[i*2+1] = convert(low);
        }
        result = string(s);
    }
    
    function convert(byte _b) internal pure
    returns (byte)
    {
        if (uint8(_b) < 10) {
            return byte(uint8(_b) + 0x30);
        } else {
            return byte(uint8(_b) + 0x57);
        }
    }
}

contract CNSPrecompiled {
    
    function insert(string memory name, string memory version, string memory addr, string memory abiStr) public returns(int256);
    
    function selectByName(string memory name) public view returns (string memory);
    
    function selectByNameAndVersion(string memory name, string memory version) public view returns (string memory);
}
