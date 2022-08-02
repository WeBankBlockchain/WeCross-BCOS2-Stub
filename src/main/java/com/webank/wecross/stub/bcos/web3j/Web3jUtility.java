package com.webank.wecross.stub.bcos.web3j;

import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.stub.bcos.config.BCOSStubConfig;
import org.fisco.bcos.sdk.client.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

public class Web3jUtility {

    private static final Logger logger = LoggerFactory.getLogger(Web3jUtility.class);

    public static ThreadPoolTaskExecutor build(int threadNum, int queueCapacity, String name) {
        logger.info(
                " initializing Web3j ThreadPoolTaskExecutor, threadC: {}, threadName: {}",
                threadNum,
                name);
        // init default thread pool

        ThreadPoolTaskExecutor threadPool = new ThreadPoolTaskExecutor();
        threadPool.setCorePoolSize(threadNum);
        threadPool.setMaxPoolSize(threadNum);
        threadPool.setQueueCapacity(queueCapacity);
        threadPool.setThreadNamePrefix(name);
        threadPool.initialize();
        return threadPool;
    }

    /**
     * Initialize the client
     * <p>
     * TODO
     *
     * @param channelServiceConfig
     * @return Client object
     * @throws Exception
     */
    public static Client initClient(BCOSStubConfig.ChannelService channelServiceConfig)
            throws Exception {
        return null;
    }

    public static void checkCertExist(
            PathMatchingResourcePatternResolver resolver, String location, String key)
            throws WeCrossException {
        if (!resolver.getResource(location).exists()) {
            throw new WeCrossException(
                    WeCrossException.ErrorCode.DIR_NOT_EXISTS,
                    key + " does not exist, please check.");
        }
    }
}
