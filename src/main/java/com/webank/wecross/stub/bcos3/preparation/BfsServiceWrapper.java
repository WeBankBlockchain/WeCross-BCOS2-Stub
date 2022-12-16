package com.webank.wecross.stub.bcos3.preparation;

import com.webank.wecross.stub.bcos3.client.AbstractClientWrapper;
import com.webank.wecross.stub.bcos3.common.BCOSConstant;
import java.util.List;
import java.util.Objects;
import org.fisco.bcos.sdk.v3.contract.precompiled.bfs.BFSInfo;
import org.fisco.bcos.sdk.v3.contract.precompiled.bfs.BFSService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BfsServiceWrapper {

    private static final Logger logger = LoggerFactory.getLogger(BfsServiceWrapper.class);

    public static final int MAX_VERSION_LENGTH = 40;

    public static BFSInfo queryProxyBFSInfo(AbstractClientWrapper clientWrapper) {
        return queryBFSInfo(clientWrapper, BCOSConstant.BCOS_PROXY_NAME);
    }

    public static BFSInfo queryHubBFSInfo(AbstractClientWrapper clientWrapper) {
        return queryBFSInfo(clientWrapper, BCOSConstant.BCOS_HUB_NAME);
    }

    /** query cns to get address,abi of hub contract */
    private static BFSInfo queryBFSInfo(AbstractClientWrapper clientWrapper, String name) {
        BFSService bfsService =
                new BFSService(
                        clientWrapper.getClient(),
                        clientWrapper.getCryptoSuite().getCryptoKeyPair());
        String absolutePath = "/apps/" + name + "/latest";
        logger.info("get bfs info, absolutePath: {}", absolutePath);
        try {
            List<BFSInfo> bfsInfos = bfsService.listBFSInfo(absolutePath);
            if (Objects.isNull(bfsInfos) || bfsInfos.isEmpty()) {
                logger.warn("BFS info empty.");
                return null;
            }
            BFSInfo bfsInfo = bfsInfos.get(0);
            logger.info(
                    "bfs info, name: {}, version: {}, address: {}, abi: {}",
                    name,
                    bfsInfo.getFileName(),
                    bfsInfo.getAddress(),
                    bfsInfo.getAbi());
            return bfsInfo;

        } catch (Exception e) {
            logger.error("Query {} BFS info e: ", name, e);
            return null;
        }
    }
}
