# 陆羽协议配置及测试

## 配置

### 配置插件

将bcos2-stub-xxxx.jar和bcos2-stub-gm-xxxx.jar放置于陆羽协议路由的plugin目录下

### 配置接入链

给要接入的链取名字：如 bcos1

在陆羽协议的路由配置目录`chains/<chainName>`下，文件夹名即为链名（如链名为bcos1的配置文件目录为：`chains/bcos1`）

包含以下文件：

``` 
bcos/
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
[common]
    name = 'bcos1'
    type = 'BCOS2.0' # BCOS2.0 or GM_BCOS2.0

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
    name = "hello"
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

## 调试方法

启动路由后

**查询资源**

`POST`

`http://localhost:8250/sys/listResources`

``` json
{
	"version":"1",
	"data":{
		"ignoreRemote": true
	}
}
```

**发送交易**

`POST`

`localhost:8250/resource/<zoneName>/<chainName>/<resourceName>/sendTransaction`

``` json
{
	"version":"1.0.0",
	"data":{
		"path": "payment.bcos.HelloWorld",
		"method": "set",
		"args": ["aaaaaaa"],
		"nonce":123456,
		"luyuSign": [  102, -20, 99, 2, -87, 36, -103, 84, -53, -90, -24, 27, 32, -43, 116, 100, -103,
                        -36, -120, -53, 74, -38, -75, 27, -128, -24, 70, 88, 89, 61, -44, -81],
        "sender": "aabbccddeeff"
		
	}
}
```

正确返回

``` json
{
    "version": "1.0.0",
    "errorCode": 0,
    "message": "Success",
    "data": {
        "result": [],
        "code": 0,
        "message": "success",
        "path": "payment.bcos.HelloWorld",
        "method": "set",
        "args": [
            "aaaaaaa"
        ],
        "transactionHash": "0xefac362c387924a3d50d592c6730868f4130073f342d910cc0c7da0d9c73f302",
        "transactionBytes": "",
        "blockNumber": 33,
        "version": null
    }
}
```

**查询状态**

`POST`

`localhost:8250/resource/<zoneName>/<chainName>/<resourceName>/call`

``` json
{
	"version":"1.0.0",
	"data":{
		"path": "payment.bcos.HelloWorld",
		"method": "get",
		"args": [],
		"nonce":123456,
		"luyuSign": [  102, -20, 99, 2, -87, 36, -103, 84, -53, -90, -24, 27, 32, -43, 116, 100, -103,
                        -36, -120, -53, 74, -38, -75, 27, -128, -24, 70, 88, 89, 61, -44, -81],
        "sender": "aabbccddeeff"
	}
}
```

正确返回

``` json
{
    "version": "1.0.0",
    "errorCode": 0,
    "message": "Success",
    "data": {
        "result": [
            "aaaaaaa"
        ],
        "code": 0,
        "message": "success",
        "path": "payment.bcos.HelloWorld",
        "method": "get",
        "args": [],
        "version": null
    }
}
```

