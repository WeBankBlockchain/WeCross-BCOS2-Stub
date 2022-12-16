package com.webank.wecross.stub.bcos3.common;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

import java.io.IOException;
import java.util.Objects;
import org.junit.Test;

public class BCOSTomlTest {
    @Test
    public void loadTomlTest() throws IOException {
        String file = "stub-sample-ut.toml";
        BCOSToml bcosToml = new BCOSToml(file);
        assertEquals(bcosToml.getPath(), file);
        assertTrue(Objects.nonNull(bcosToml.getToml()));
    }
}
