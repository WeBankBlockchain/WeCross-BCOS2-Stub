pragma solidity >=0.4.22 <0.6.0;
pragma experimental ABIEncoderV2;

interface ILuyuSDK {
    ///// 跨链调用接口

    /*
     *   向目的资源发送交易：luyuSendTransaction
     */
    function luyuSendTransaction(
        string calldata path, // 目的资源路径
        string calldata method, // 目的资源方法
        string[] calldata args, // 目的资源参数
        string calldata luyuIdentity, // 当前交易者对应的一级账户身份（address）
        string calldata callbackMethod // 回调函数名
    ) external returns (uint256); // 返回值nonce，用于回调函数被调用时进行关联

    // 支持手动指定nonce
    function luyuSendTransaction(
        string calldata path, // 目的资源路径
        string calldata method, // 目的资源方法
        string[] calldata args, // 目的资源参数
        uint256 nonce, // 手动指定的nonce
        string calldata luyuIdentity, // 当前交易者对应的一级账户身份（address）
        string calldata callbackMethod // 回调函数名
    ) external returns (uint256); // 返回值nonce，用于回调函数被调用时进行关联

    /*
     *   查询目的资源状态：luyuCall
     */
    function luyuCall(
        string calldata path, // 目的资源路径
        string calldata method, // 目的资源方法
        string[] calldata args, // 目的资源参数
        string calldata luyuIdentity, // 当前交易者对应的一级账户身份（address）
        string calldata callbackMethod // 回调函数名
    ) external returns (uint256); // 返回值nonce，用于回调函数被调用时进行关联

    // 支持手动指定nonce
    function luyuCall(
        string calldata path, // 目的资源路径
        string calldata method, // 目的资源方法
        string[] calldata args, // 目的资源参数
        uint256 nonce, // 手动指定的nonce
        string calldata luyuIdentity, // 当前交易者对应的一级账户身份（address）
        string calldata callbackMethod // 回调函数名
    ) external returns (uint256); // 返回值nonce，用于回调函数被调用时进行关联

    ///// 内部事件（用于链插件响应）
    event LuyuSendTransaction(
        string path,
        string method,
        string[] args,
        uint256 nonce,
        string luyuIdentity,
        string callbackMethod,
        address sender // 交易发送者的二级账户身份，即：tx.origin
    );
    event LuyuCall(
        string path,
        string method,
        string[] args,
        uint256 nonce,
        string luyuIdentity,
        string callbackMethod,
        address sender // 交易发送者的二级账户身份，即：tx.origin
    );
}

contract LuyuContract is ILuyuSDK {
    uint256 nonceSeed = 0;

    function luyuSendTransaction(
        string memory path,
        string memory method,
        string[] memory args,
        string memory luyuIdentity,
        string memory callbackMethod
    ) public returns (uint256) {
        uint256 nonce = getNonce();
        emit LuyuSendTransaction(
            path,
            method,
            args,
            nonce,
            luyuIdentity,
            callbackMethod,
            tx.origin
        );
        return nonce;
    }

    function luyuSendTransaction(
        string memory path,
        string memory method,
        string[] memory args,
        uint256 nonce,
        string memory luyuIdentity,
        string memory callbackMethod
    ) public returns (uint256) {
        emit LuyuSendTransaction(
            path,
            method,
            args,
            nonce,
            luyuIdentity,
            callbackMethod,
            tx.origin
        );
        return nonce;
    }

    function luyuCall(
        string memory path,
        string memory method,
        string[] memory args,
        string memory luyuIdentity,
        string memory callbackMethod
    ) public returns (uint256) {
        uint256 nonce = getNonce();
        emit LuyuCall(
            path,
            method,
            args,
            nonce,
            luyuIdentity,
            callbackMethod,
            tx.origin
        );
        return nonce;
    }

    function luyuCall(
        string memory path,
        string memory method,
        string[] memory args,
        uint256 nonce,
        string memory luyuIdentity,
        string memory callbackMethod
    ) public returns (uint256) {
        emit LuyuCall(
            path,
            method,
            args,
            nonce,
            luyuIdentity,
            callbackMethod,
            tx.origin
        );
        return nonce;
    }

    function getNonce() private returns (uint256) {
        return ((nonceSeed++) % 100000) + getNow() * 100000;
    }

    function getNow() private view returns (uint256) {
        // 'now' in ehtereum return in seconds but millisecond in fisco bcos
        // Need to format to seconds
        return getSecondTime(now);
    }

    function getSecondTime(uint256 time) private pure returns (uint256) {
        if (time / 10000000000 != 0) {
            return getSecondTime(time / 10);
        } else {
            return time;
        }
    }
}
