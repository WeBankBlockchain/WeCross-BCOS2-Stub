package com.webank.wecross.stub.bcos;

import static junit.framework.TestCase.assertTrue;

import com.webank.wecross.stub.Account;
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
    public void newAccountTest() {
        Account account = stubFactory.newAccount("bcos", "classpath:/accounts/bcos");
        assertTrue(Objects.nonNull(account));
        assertTrue(account instanceof BCOSAccount);
    }

    @Test
    public void newAccountFailedTest() {
        Account account = stubFactory.newAccount("bcos_xxx", "classpath:/accounts/bcos_xxx");
        assertTrue(Objects.isNull(account));
    }
}
