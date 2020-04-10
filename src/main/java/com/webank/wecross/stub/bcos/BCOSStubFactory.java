package com.webank.wecross.stub.bcos;

import com.webank.wecross.stub.Account;
import com.webank.wecross.stub.Connection;
import com.webank.wecross.stub.Driver;
import com.webank.wecross.stub.Stub;
import com.webank.wecross.stub.StubFactory;
import com.webank.wecross.stub.bcos.account.BCOSAccountFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stub("BCOS2.0")
public class BCOSStubFactory implements StubFactory {
    private static final Logger logger = LoggerFactory.getLogger(BCOSStubFactory.class);

    @Override
    public Driver newDriver() {
        return new BCOSDriver();
    }

    @Override
    public Connection newConnection(String path) {
        try {
            return BCOSConnectionFactory.build(path, null);
        } catch (Exception e) {
            logger.error(" newConnection, e: ", e);
            return null;
        }
    }

    @Override
    public Account newAccount(String name, String path) {
        try {
            return BCOSAccountFactory.build(
                    name, path.startsWith("classpath") ? path : "file:" + path);
        } catch (Exception e) {
            logger.error(" newAccount, e: ", e);
            return null;
        }
    }

    @Override
    public void generateAccount(String s, String[] strings) {}

    @Override
    public void generateConnection(String s, String[] strings) {}
}
