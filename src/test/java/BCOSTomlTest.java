import static junit.framework.TestCase.assertEquals;

import com.webank.wecross.stub.bcos.common.BCOSToml;
import java.io.IOException;
import org.junit.Test;

public class BCOSTomlTest {
    @Test
    public void loadTomlTest() throws IOException {
        String file = "stub-sample.toml";
        BCOSToml bcosToml = new BCOSToml(file);
        assertEquals(bcosToml.getPath(), file);
    }
}
