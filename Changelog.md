### v1.4.0

(2024-03-01)

**新增**

- stub增加获取区块的接口，增加跨链获取区块的功能 https://github.com/WeBankBlockchain/WeCross-BCOS2-Stub/pull/207
- 新增获取区块时返回交易详细信息的功能 https://github.com/WeBankBlockchain/WeCross-BCOS2-Stub/pull/210
- 获取的交易增加时间戳字段 https://github.com/WeBankBlockchain/WeCross-BCOS2-Stub/pull/206

**更改**

- 更新版本依赖，修复安全问题 https://github.com/WeBankBlockchain/WeCross-BCOS2-Stub/pull/211

**修复**

- 修复Proxy合约在事务为空时获取事件异常的问题 https://github.com/WeBankBlockchain/WeCross-BCOS2-Stub/pull/208

### v1.3.0

(2023-03-15)

**更改**

* 使用依赖BCOS Java SDK 2.9.1版本代替Web3JSDK(v1.2.3)
* 更新gson、commons-io、snakeyaml版本以修复安全问题
* 去除netty和tcnative的依赖

### v1.2.1

(2021-12-15)

**修复**

* 修复log4j的漏洞，将其升级至2.15

### v1.2.0

(2021-08-20)

**更改**

* 升级版本号，与现有WeCross版本保持一致
* 完善 README 

### v1.1.1

(2021-04-02)

**更改**

* 升级web3sdk，修复偶现的断连问题
* 优化区块头验证代码结构

### v1.1.0

(2021-02-02)

**新增**

* 适配`FISCO BCOS 2.1-2.5`版本
* 支持国密`SSL`连接
* 部分依赖升级，详情参考`build.gradle`的修改

### v1.0.0

(2020-12-17)

**新增**

* 桥接合约：管理合约跨链调用请求
* Driver接口：getTransaction，获取交易详情
* 区块头验证：配置项新增验证者公钥列表，以验证区块头

**更改**

* 删除账户加载逻辑
* 代理合约自身的调用也经过代理合约统一入口
* 代理合约更改：
    * 发交易新增交易号UUID用于去重
    * 不允许call事务中的资源
    * 删除地址缓存，统一查CNS合约
    * 优化部分接口命名与定义

### v1.0.0-rc4

(2020-08-18)

**新增**

* 代理合约
  * WeCrossProxy代码及部署操作
  * 将代理合约作为调用入口调用其它合约
  * 通过代理合约控制其它合约的访问
  * 通过代理合约实现其它合约的部署和更新
* ABI编解码逻辑：支持调用多种变量类型的合约接口

### v1.0.0-rc3

(2020-06-15)

**更改**

* 操作区块链时，调用区块链的异步接口
* 适配异步Driver接口，向Router提供异步的call/sendTrnasaction

### v1.0.0-rc2

(2020-05-12)

**功能**
* 区块链适配
  * FISCO BCOS Stub配置加载
  * FISCO BCOS链状态数据查询
  * FISCO BCOS链上资源访问
  * FISCO BCOS交易解析
  * FISCO BCOS交易默克尔验证
* 账户功能
  * FISCO BCOS账户加载
  * 交易签名
