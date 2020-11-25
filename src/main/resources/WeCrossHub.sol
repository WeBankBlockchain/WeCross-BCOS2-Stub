/*
*   v1.0.0
*   hub contract for WeCross
*   main entrance of interchain call
*/

pragma solidity >=0.4.22 <0.6.0;
pragma experimental ABIEncoderV2;

contract WeCrossHub {

    // string constant EVENT_TYPE = "INTERCHAIN";

    string constant NULL_FLAG  = "null";

    string constant VERSION    = "v1.0.0";

    string constant CALL_TYPE_QUERY  = "0";

    string constant CALL_TYPE_INVOKE = "1";

    uint256 increment = 0;

    uint256 currentIndex = 0;

    mapping(uint256 => string) requests;

    mapping(string => string[]) callbackResults;

    function getVersion() public pure
    returns(string memory)
    {
        return VERSION;
    }

    // get current uid
    function getIncrement() public view
    returns(uint256)
    {
        return increment;
    }

    // invoke other chain
    function interchainInvoke(string memory _path, string memory _method, string[] memory _args, string memory _callbackPath, string memory _callbackMethod) public
    returns(string memory uid)
    {
        return handleRequest(CALL_TYPE_INVOKE, _path, _method, _args, _callbackPath, _callbackMethod);
    }

    // query other chain, not support right now
    function interchainQuery(string memory _path, string memory _method, string[] memory _args, string memory _callbackPath, string memory _callbackMethod) public
    returns(string memory uid)
    {
        return handleRequest(CALL_TYPE_QUERY, _path, _method, _args, _callbackPath, _callbackMethod);
    }

    function handleRequest(string memory _callType, string memory _path, string memory _method, string[] memory _args, string memory _callbackPath, string memory _callbackMethod) private
    returns(string memory uid)
    {
        uid = uint256ToString(++increment);

        string[] memory reuqest = new string[](8);
        reuqest[0] = uid;
        reuqest[1] = _callType;
        reuqest[2] = _path;
        reuqest[3] = _method;
        reuqest[4] = serializeStringArray(_args);
        reuqest[5] = _callbackPath;
        reuqest[6] = _callbackMethod;
        reuqest[7] = addressToString(tx.origin);

        requests[increment] = serializeStringArray(reuqest);
    }

    function getInterchainRequests(uint256 _num) public view
    returns(string memory)
    {
        if(currentIndex == increment) {
            return NULL_FLAG;
        }

        uint256 num = _num < (increment - currentIndex) ? _num : (increment - currentIndex);

        string[] memory tempRequests = new string[](num);
        for(uint256 i = 0; i < num; i++){
            tempRequests[i] = requests[currentIndex+i+1];
        }

        return serializeStringArray(tempRequests);
    }

    function updateCurrentRequestIndex(uint256 _index) public
    {
        if(currentIndex < _index) {
            currentIndex = _index;
        }
    }

    // _result is json form of arrays
    function registerCallbackResult(string memory _uid, string memory _tid, string memory _seq, string memory _errorCode, string memory _errorMsg, string[] memory _result) public
    {
        string[5] memory result = [_tid, _seq, _errorCode, _errorMsg, serializeStringArray(_result)];
        callbackResults[_uid] = result;
    }

    function selectCallbackResult(string memory _uid) public view
    returns(string[] memory)
    {
        return callbackResults[_uid];
    }

    function serializeStringArray(string[] memory _arr) internal pure
    returns(string memory jsonStr)
    {
        uint len = _arr.length;
        if(len == 0) {
            return "[]";
        }

        jsonStr = '[';
        for (uint i = 0; i < len - 1; i++) {
            jsonStr = string(abi.encodePacked(jsonStr, '"'));
            jsonStr = string(abi.encodePacked(jsonStr, jsonEscape(_arr[i])));
            jsonStr = string(abi.encodePacked(jsonStr, '",'));
        }

        jsonStr = string(abi.encodePacked(jsonStr, '"'));
        jsonStr = string(abi.encodePacked(jsonStr, jsonEscape(_arr[len - 1])));
        jsonStr = string(abi.encodePacked(jsonStr, '"'));
        jsonStr = string(abi.encodePacked(jsonStr, ']'));
    }

    function jsonEscape(string memory _str) internal pure
    returns(string memory)
    {
        bytes memory bts = bytes(_str);
        uint256 len = bts.length;
        bytes memory temp = new bytes(len * 2);
        uint256 i = 0;
        uint256 j = 0;
        for(; j<len; j++) {
            if(bts[j] == '\\' || bts[j] == '"') {
                temp[i++] = '\\';
            }
            temp[i++] = bts[j];
        }

        bytes memory res = new bytes(i);
        for(j = 0; j < i; j++)
        {
            res[j] = temp[j];
        }
        return string(res);
    }

    function uint256ToString(uint256 _value) internal pure
    returns (string memory)
    {
        bytes32 result;
        if (_value == 0) {
            return "0";
        }

        while (_value > 0) {
            result = bytes32(uint(result) / (2 ** 8));
            result |= bytes32(((_value % 10) + 48) * 2 ** (8 * 31));
            _value /= 10;
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