import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

import com.webank.wecross.stub.bcos.account.BCOSAccount;
import com.webank.wecross.stub.bcos.account.BCOSAccountFactory;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.junit.Test;

public class BCSOAccountFacrotyTest {
    @Test
    public void loadPemTest()
            throws IOException, CertificateException, UnrecoverableKeyException,
                    NoSuchAlgorithmException, KeyStoreException, NoSuchProviderException,
                    InvalidKeySpecException {
        Credentials credentials =
                BCOSAccountFactory.loadPemAccount(
                        "accounts/bcos1/0xde4247b42754e220256dbf51009c79d6736da6be.pem");
        assertEquals(credentials.getAddress(), "0xde4247b42754e220256dbf51009c79d6736da6be");
        assertFalse(credentials.getEcKeyPair().getPrivateKey().toString().isEmpty());
    }

    @Test
    public void loadP12Test()
            throws IOException, CertificateException, UnrecoverableKeyException,
                    NoSuchAlgorithmException, KeyStoreException, NoSuchProviderException,
                    InvalidKeySpecException {
        Credentials credentials =
                BCOSAccountFactory.loadP12Account(
                        "accounts/bcos/0x4c9e341a015ce8200060a028ce45dfea8bf33e15.p12", "123456");
        assertEquals(credentials.getAddress(), "0x4c9e341a015ce8200060a028ce45dfea8bf33e15");
        assertTrue(!credentials.getEcKeyPair().getPrivateKey().toString().isEmpty());
    }

    @Test
    public void buildAccountTest()
            throws IOException, CertificateException, UnrecoverableKeyException,
                    NoSuchAlgorithmException, KeyStoreException, NoSuchProviderException,
                    InvalidKeySpecException {

        BCOSAccount bcosAccount0 = BCOSAccountFactory.build("bcos", "classpath:/accounts/bcos");
        BCOSAccount bcosAccount1 = BCOSAccountFactory.build("bcos1", "classpath:/accounts/bcos1");

        assertEquals(
                bcosAccount0.getCredentials().getAddress(),
                "0x4c9e341a015ce8200060a028ce45dfea8bf33e15");
        assertEquals(
                bcosAccount1.getCredentials().getAddress(),
                "0xde4247b42754e220256dbf51009c79d6736da6be");

        assertEquals(bcosAccount0.getName(), "bcos");
        assertEquals(bcosAccount1.getName(), "bcos1");

        assertEquals(bcosAccount0.getType(), "BCOS2.0");
        assertEquals(bcosAccount1.getType(), "BCOS2.0");
    }
}
