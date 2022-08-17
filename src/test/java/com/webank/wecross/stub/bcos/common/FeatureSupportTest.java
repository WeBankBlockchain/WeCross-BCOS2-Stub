package com.webank.wecross.stub.bcos.common;

import org.fisco.bcos.sdk.channel.model.ChannelPrococolExceiption;
import org.fisco.bcos.sdk.channel.model.EnumNodeVersion;
import org.junit.Test;

import static junit.framework.TestCase.assertTrue;

public class FeatureSupportTest {
    @Test
    public void featSupport_271() throws ChannelPrococolExceiption {
        String nodeVersion = "2.7.1";
        EnumNodeVersion.Version version = EnumNodeVersion.getClassVersion(nodeVersion);

        assertTrue(FeatureSupport.isSupportGetBlockHeader(nodeVersion));
        assertTrue(FeatureSupport.isSupportGetTxProof(nodeVersion));
        assertTrue(FeatureSupport.isSupportVersion(nodeVersion));

        assertTrue(FeatureSupport.isSupportGetBlockHeader(version));
        assertTrue(FeatureSupport.isSupportGetTxProof(version));
        assertTrue(FeatureSupport.isSupportVersion(version));
    }

    @Test
    public void featSupport_240() throws ChannelPrococolExceiption {
        String nodeVersion = "2.4.0";
        EnumNodeVersion.Version version = EnumNodeVersion.getClassVersion(nodeVersion);

        assertTrue(!FeatureSupport.isSupportGetBlockHeader(version));
        assertTrue(FeatureSupport.isSupportGetTxProof(version));
        assertTrue(FeatureSupport.isSupportVersion(version));

        assertTrue(!FeatureSupport.isSupportGetBlockHeader(nodeVersion));
        assertTrue(FeatureSupport.isSupportGetTxProof(nodeVersion));
        assertTrue(FeatureSupport.isSupportVersion(nodeVersion));
    }

    @Test
    public void featSupport_210() throws ChannelPrococolExceiption {

        String nodeVersion = "2.1.0";
        EnumNodeVersion.Version version = EnumNodeVersion.getClassVersion(nodeVersion);

        assertTrue(!FeatureSupport.isSupportGetBlockHeader(version));
        assertTrue(!FeatureSupport.isSupportGetTxProof(version));
        assertTrue(FeatureSupport.isSupportVersion(version));

        assertTrue(!FeatureSupport.isSupportGetBlockHeader(nodeVersion));
        assertTrue(!FeatureSupport.isSupportGetTxProof(nodeVersion));
        assertTrue(FeatureSupport.isSupportVersion(nodeVersion));
    }

    @Test
    public void featSupport_200() throws ChannelPrococolExceiption {
        String nodeVersion = "2.0.0";
        EnumNodeVersion.Version version = EnumNodeVersion.getClassVersion(nodeVersion);
        assertTrue(!FeatureSupport.isSupportVersion(version));
        assertTrue(!FeatureSupport.isSupportVersion(nodeVersion));
    }
}
