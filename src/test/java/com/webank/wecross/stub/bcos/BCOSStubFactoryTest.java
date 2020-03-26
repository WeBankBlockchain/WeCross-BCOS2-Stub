package com.webank.wecross.stub.bcos;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

import com.webank.wecross.stub.Account;
import com.webank.wecross.stub.Connection;
import com.webank.wecross.stub.Driver;
import com.webank.wecross.stub.bcos.account.BCOSAccount;
import java.util.Objects;
import org.junit.Test;

public class BCOSStubFactoryTest {

    private BCOSStubFactory stubFactory = new BCOSStubFactory();

    @Test
    public void newDriverTest() {
        Driver driver = stubFactory.newDriver();
        assertTrue(Objects.nonNull(driver));
        assertTrue(driver instanceof BCOSDriver);
    }

    @Test
    public void newConnectionTest() {
        Connection connection = stubFactory.newConnection("stub-sample-ut.toml");
        assertTrue(Objects.isNull(connection));
    }

    @Test
    public void newAccountTest() {
        Account account = stubFactory.newAccount("bcos", "classpath:/accounts/bcos");
        assertTrue(Objects.nonNull(account));
        assertTrue(account instanceof BCOSAccount);
        BCOSAccount bcosAccount = (BCOSAccount) account;
        assertEquals(
                bcosAccount.getCredentials().getAddress(),
                "0x4c9e341a015ce8200060a028ce45dfea8bf33e15");
        assertEquals(bcosAccount.getType(), "BCOS2.0");
        assertEquals(bcosAccount.getName(), "bcos");
    }

    @Test
    public void newAccountFailedTest() {
        Account account = stubFactory.newAccount("bcos_xxx", "classpath:/accounts/bcos_xxx");
        assertTrue(Objects.isNull(account));
    }
}
