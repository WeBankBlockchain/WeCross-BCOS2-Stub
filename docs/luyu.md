# 陆羽跨链协议配置

## 配置

### 配置插件

将bcos2-stub-xxxx.jar和bcos2-stub-gm-xxxx.jar放置于陆羽协议路由的plugin目录下

### 配置接入链

给要接入的链取名字：如 bcos1

在陆羽协议的路由配置目录`chains/<chainName>`下，文件夹名即为链名（如链名为bcos1的配置文件目录为：`chains/bcos1`）

包含以下文件：

``` 
bcos1/
├── ca.crt
├── connection.toml
├── driver.toml
├── plugin.toml
├── sdk.crt
└── sdk.key
```

**plugin.toml**

```
[common]
    name = 'bcos1'
    type = 'BCOS2.0'
```

**driver.toml**

```
（空文件）
```

**connection.toml**

```
[chain]
    groupId = 1 # default 1
    chainId = 1 # default 1

[channelService]
    caCert = 'ca.crt'
    sslCert = 'sdk.crt'
    sslKey = 'sdk.key'
    gmConnectEnable = false
    gmCaCert = 'gm/gmca.crt'
    gmSslCert = 'gm/gmsdk.crt'
    gmSslKey = 'gm/gmsdk.key'
    gmEnSslCert = 'gm/gmensdk.crt'
    gmEnSslKey = 'gm/gmensdk.key'
    timeout = 300000  # ms, default 60000ms
    connectionsStr = ['127.0.0.1:20200']

[[resources]]
    name = "hello" # 为链上合约CNS名（需事先用FISCO BCOS的控制台为一个合约注册CNS的名字，请参考FISCO BCOS的文档）
    methods = ["set(1)", "get(0)"]

[[resources]]
    name = "test"
    methods = ["set(1)", "get(0)"]
```

**证书拷贝位置**

例如`nodes/127.0.0.1/sdk`下，直接拷贝下列文件即可

```
ca.crt  sdk.crt  sdk.key
```

**重启路由**

配置完成后，重启路由使其生效

``` bash
bash stop.sh && bash start.sh
```

### 配置跨链验证

该配置用于在非直连区块链的路由上，通过跨链验证机制验证直连路由返回消息的正确性。如：路由A直连了FISCO BCOS链，则在路由B上进行该配置。配置后，路由B会采用跨链验证机制，校验发往路由A的交易上链结果的正确性。

注：该配置是可选配置，若不配置则不开启

**配置目录**

对方接入的链名字：如 bcos1

在路由配置目录`chains/<chainName>`下，文件夹名即为链名（如链名为bcos1的配置文件目录为：`chains/bcos1`）

包含以下文件：

``` 
bcos1/
├── driver.toml
└── plugin.toml
```

**plugin.toml**

```
[common]
    name = 'bcos1'
    type = 'BCOS2.0'
```

**driver.toml**

```toml
[verifier]
        # 填写所有共识节点的公钥(nodeid)
        pubKey = [
            '223c3b37...',
            '314b54f5...',
            'b01ce1ed...',
            'fd655691...'
        ]
```

**重启路由**

配置完成后，重启路由使其生效

``` bash
bash stop.sh && bash start.sh
```
