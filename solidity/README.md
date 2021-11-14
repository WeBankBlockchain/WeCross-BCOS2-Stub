# 陆羽跨链协议 FISCO BCOS Solidity SDK

基于[LuyuSDK.sol](LuyuSDK.sol)，使用Solidity语言从**链上**发起跨链调用。

## 使用方法

### 引用

在代码中引用`LuyuSDK.sol`，并启用`ABIEncoderV2`。

``` javascript
pragma experimental ABIEncoderV2;

import "./LuyuSDK.sol";
```

将合约定义为`LuyuContract`

``` javas
contract SDKExample is LuyuContract {

}
```

### 调用

在进行上述操作后，所开发的合约被定义为`LuyuContract`，使其具备了跨链调用能力，便可以使用以下函数发起跨链调用。调用包含：发交易、查状态。

**发交易**

``` javascript
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
```

**查状态**

``` javascript
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
```

**回调**

当跨链调用在对方链上被执行后，其返回值会以回调函数的形式调用至当前合约里事先指定的回调函数中。回调函数定义规则如下

* 回调函数名：任意

* 回调参数：
  * 参数1（固定）：uint256 nonce（用于与跨链调用进行关联）
  * 参数2（可选）：返回值0，类型与返回值对应
  * 参数3（可选）：返回值1，类型与返回值对应
  * 参数4（可选）：返回值2，类型与返回值对应
  * ... 参数个数不限（不考虑编程语言本身限制的情况下）

```javascript
function func1Callback(
        uint256 nonce,
        string memory result0,
        int256 result1,
        string memory result2
    ) public {
         // callback 处理逻辑   
    }
```

**注意事项**

* 当发起跨链调用后却很久未收到回调时，可能存在的状态如下，应用需对此超时情况进行处理。
  * 请求未到达对方链
  * 在对方链上执行失败
  * 在对方链已上链，但本地未收到返回

* 本方案的调用基于区块链事件通知，某些区块链会出现重复通知的情况，因此需保证以下几点。若开发中很难保证，可换方案，采用陆羽协议的链下SDK发送跨链请求。
  * 对方链的被调用接口是幂等的
  * 当前合约的回调函数是幂等的

## 举例

* Example：[SDKExample.sol](SDKExample.sol)
* HelloWorld：[LuyuHelloWorld.sol](LuyuHelloWorld.sol) 、[HelloWorld.sol](HelloWorld.sol)

## 原理

LuyuSDK.sol 提供的函数被调用后会抛出以下事件，事件被插件捕获，将相应参数解析后采用与链下SDK相同的方式调用至目的链。目的链执行后，返回值以回调函数的形式原路返回，最终调用回当前合约。

``` javascript
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
```

