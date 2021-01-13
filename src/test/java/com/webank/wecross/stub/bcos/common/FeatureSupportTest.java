package com.webank.wecross.stub.bcos.common;

import static junit.framework.TestCase.assertTrue;

import org.fisco.bcos.channel.protocol.ChannelPrococolExceiption;
import org.fisco.bcos.fisco.EnumNodeVersion;
import org.junit.Test;

public class FeatureSupportTest {
    @Test
    public void featSupport_271() throws ChannelPrococolExceiption {
        EnumNodeVersion.Version version = EnumNodeVersion.getClassVersion("2.7.1");
        assertTrue(FeatureSupport.isSupportGetBlockHeader(version));
        assertTrue(FeatureSupport.isSupportGetTxProof(version));
    }

    @Test
    public void featSupport_240() throws ChannelPrococolExceiption {
        EnumNodeVersion.Version version = EnumNodeVersion.getClassVersion("2.4.0");
        assertTrue(!FeatureSupport.isSupportGetBlockHeader(version));
        assertTrue(FeatureSupport.isSupportGetTxProof(version));
    }

    @Test
    public void featSupport_200() throws ChannelPrococolExceiption {
        EnumNodeVersion.Version version = EnumNodeVersion.getClassVersion("2.0.0");
        assertTrue(!FeatureSupport.isSupportGetBlockHeader(version));
        assertTrue(!FeatureSupport.isSupportGetTxProof(version));
    }
}
