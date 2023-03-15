package com.webank.wecross.stub.bcos.common;

import org.fisco.bcos.sdk.channel.model.ChannelPrococolExceiption;
import org.fisco.bcos.sdk.channel.model.EnumNodeVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FeatureSupport {

    private static final Logger logger = LoggerFactory.getLogger(FeatureSupport.class);

    /**
     * BCOS 2.6+ support get block header
     *
     * @param version
     * @return
     */
    public static boolean isSupportGetBlockHeader(String version) {

        if (version == null || "".equals(version)) { // default
            return true;
        }

        try {
            EnumNodeVersion.Version v = EnumNodeVersion.getClassVersion(version);
            return isSupportGetBlockHeader(v);
        } catch (ChannelPrococolExceiption e) {
            logger.info("version: {}, e: ", version, e);
            return true;
        }
    }

    /**
     * BCOS 2.4+ support get transaction/receipt proof
     *
     * @param version
     * @return
     */
    public static boolean isSupportGetTxProof(String version) {

        if (version == null || "".equals(version)) { // default
            return true;
        }

        try {
            EnumNodeVersion.Version v = EnumNodeVersion.getClassVersion(version);
            return isSupportGetTxProof(v);
        } catch (ChannelPrococolExceiption e) {
            logger.info("version: {}, e: ", version, e);
            return true;
        }
    }

    /**
     * BCOS 2.6+ support get block header
     *
     * @param version
     * @return
     */
    public static boolean isSupportGetBlockHeader(EnumNodeVersion.Version version) {
        return version.getMajor() == 2 && version.getMinor() >= 6;
    }

    /**
     * BCOS 2.4+ support get transaction/receipt proof
     *
     * @param version
     * @return
     */
    public static boolean isSupportGetTxProof(EnumNodeVersion.Version version) {
        return version.getMajor() == 2 && version.getMinor() >= 4;
    }

    /**
     * @param version
     * @return
     */
    public static boolean isSupportVersion(EnumNodeVersion.Version version) {
        return version.getMajor() >= 2 && version.getMinor() >= 1;
    }

    /**
     * @param version
     * @return
     */
    public static boolean isSupportVersion(String version) throws ChannelPrococolExceiption {
        EnumNodeVersion.Version v = EnumNodeVersion.getClassVersion(version);
        return isSupportVersion(v);
    }
}
