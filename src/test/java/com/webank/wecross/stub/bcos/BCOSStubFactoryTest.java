package com.webank.wecross.stub.bcos;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

import com.webank.wecross.stub.Account;
import com.webank.wecross.stub.Connection;
import com.webank.wecross.stub.Driver;
import com.webank.wecross.stub.bcos.account.BCOSAccount;
import com.webank.wecross.stub.bcos.common.BCOSConstant;
import java.util.Objects;
import org.junit.Test;

public class BCOSStubFactoryTest {

    private BCOSStubFactory bcosSubFactory = new BCOSStubFactory();
    // private BCOSGMStubFactory bcosgmStubFactory = new BCOSGMStubFactory();

    @Test
    public void newConnectionTest() {
        Connection connection = bcosSubFactory.newConnection("./");
        assertTrue(Objects.isNull(connection));
    }

    @Test
    public void newDriverTest() {
        Driver driver = bcosSubFactory.newDriver();
        assertTrue(Objects.nonNull(driver));
        assertTrue(driver instanceof BCOSDriver);
    }

    @Test
    public void newAccountTest() {
        Account account = bcosSubFactory.newAccount("bcos", "classpath:/accounts/bcos");
        assertTrue(Objects.nonNull(account));
        assertTrue(account instanceof BCOSAccount);
        BCOSAccount bcosAccount = (BCOSAccount) account;
        assertEquals(
                bcosAccount.getCredentials().getAddress(),
                "0x4c9e341a015ce8200060a028ce45dfea8bf33e15");
        assertEquals(bcosAccount.getType(), BCOSConstant.BCOS_STUB_TYPE);
        assertEquals(bcosAccount.getName(), "bcos");
    }

    @Test
    public void newAccountFailedTest() {
        Account account = bcosSubFactory.newAccount("bcos_xxx", "classpath:/accounts/bcos_xxx");
        assertTrue(Objects.isNull(account));
    }

    @Test
    public void BCOSSubFactoryTypeTest() {
        System.out.println(bcosSubFactory.getStubType());
        assertTrue(bcosSubFactory.getStubType().equals(BCOSConstant.BCOS_STUB_TYPE));
        assertTrue(bcosSubFactory.getAlg().equals("secp256k1"));

        // assertTrue(bcosgmStubFactory.getStubType().equals(BCOSConstan..GM_BCOS_STUB_TYPE));
        // assertTrue(bcosgmStubFactory.getAlg().equals("sm2p256v1"));
    }
}
