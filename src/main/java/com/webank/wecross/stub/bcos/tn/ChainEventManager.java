package com.webank.wecross.stub.bcos.tn;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.stub.ObjectMapperFactory;
import com.webank.wecross.stub.Path;
import com.webank.wecross.stub.bcos.preparation.CnsService;
import com.webank.wecross.stub.bcos.web3j.AbstractWeb3jWrapper;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.fisco.bcos.channel.client.Service;
import org.fisco.bcos.channel.event.filter.EventLogUserParams;
import org.fisco.bcos.channel.event.filter.ServiceEventLogPushCallback;
import org.fisco.bcos.channel.event.filter.TopicTools;
import org.fisco.bcos.web3j.precompile.cns.CnsInfo;
import org.fisco.bcos.web3j.tx.txdecode.LogResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trustnet.protocol.network.Resource;

public class ChainEventManager {
    private static Logger logger = LoggerFactory.getLogger(ChainEventManager.class);
    private ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
    private AbstractWeb3jWrapper web3jWrapper;
    private List<ChainEventCallback> chainCallEventCallbacks = new LinkedList<>();
    private List<ChainEventCallback> chainSendTransactionEventCallbacks = new LinkedList<>();
    private ReadWriteLock lock = new ReentrantReadWriteLock();

    public ChainEventManager(AbstractWeb3jWrapper web3jWrapper) {
        this.web3jWrapper = web3jWrapper;
    }

    public interface ChainEventCallback {
        void onEvent(String name, byte[] bytes);
    }

    public void addSendTransactionEventCallback(ChainEventCallback callback) {
        try {
            lock.writeLock().lock();
            chainSendTransactionEventCallbacks.add(callback);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void addCallEventCallback(ChainEventCallback callback) {
        try {
            lock.writeLock().lock();
            chainCallEventCallbacks.add(callback);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void registerEvent(Resource resource) throws Exception {
        String name = Path.decode(resource.getPath()).getResource();
        if (name == null) {
            throw new Exception("Empty resource name of " + resource.toString());
        }

        CnsInfo cnsInfo = CnsService.queryCnsInfo(web3jWrapper, name);
        if (cnsInfo == null) {
            logger.warn("Register event of {} failed. Could not get CNS.", resource.getPath());
            return;
        }

        Service service = web3jWrapper.getService();

        registerSendTransactionEvent(name, service, cnsInfo.getAddress());
        registerCallEvent(name, service, cnsInfo.getAddress());
    }

    private void registerSendTransactionEvent(
            String resourceName, Service service, String address) {
        EventLogUserParams params = new EventLogUserParams();
        params.setFromBlock("latest");
        params.setToBlock("latest");
        params.setAddresses(new ArrayList<String>());
        params.getAddresses().add(address);
        ArrayList<Object> topics = new ArrayList<>();
        topics.add(
                TopicTools.stringToTopic(
                        "TnSendTransaction(string,string,string[],uint256,string,string,address)"));
        // topics.add(TopicTools.stringToTopic("TnCall(string,string,string[],uint256,string,string)"));
        params.setTopics(topics);

        class EventCallback extends ServiceEventLogPushCallback {
            @Override
            public void onPushEventLog(int status, List<LogResult> logs) {
                logger.debug("On chain submit event, status: {}, logs: {}", status, logs);
                if (status != 0) {
                    return;
                }

                for (LogResult log : logs) {
                    try {
                        String logStr = objectMapper.writeValueAsString(log.getLog());
                        byte[] logBytes = logStr.getBytes(StandardCharsets.UTF_8);
                        try {
                            lock.readLock().lock();
                            for (ChainEventCallback callback : chainSendTransactionEventCallbacks) {
                                callback.onEvent(resourceName, logBytes);
                            }
                        } finally {
                            lock.readLock().unlock();
                        }
                    } catch (Exception e) {
                        logger.error("handle submit event exception: ", e);
                        continue;
                    }
                }
            }
        }

        service.registerEventLogFilter(params, new EventCallback());
    }

    private void registerCallEvent(String resourceName, Service service, String address) {
        EventLogUserParams params = new EventLogUserParams();
        params.setFromBlock("latest");
        params.setToBlock("latest");
        params.setAddresses(new ArrayList<String>());
        params.getAddresses().add(address);
        ArrayList<Object> topics = new ArrayList<>();
        topics.add(
                TopicTools.stringToTopic(
                        "TnCall(string,string,string[],uint256,string,string,address)"));
        params.setTopics(topics);

        class EventCallback extends ServiceEventLogPushCallback {
            @Override
            public void onPushEventLog(int status, List<LogResult> logs) {
                logger.debug("On chain call event, status: {}, logs: {}", status, logs);
                if (status != 0) {
                    return;
                }

                for (LogResult log : logs) {
                    try {
                        String logStr = objectMapper.writeValueAsString(log.getLog());
                        byte[] logBytes = logStr.getBytes(StandardCharsets.UTF_8);
                        try {
                            lock.readLock().lock();
                            for (ChainEventCallback callback : chainCallEventCallbacks) {
                                callback.onEvent(resourceName, logBytes);
                            }
                        } finally {
                            lock.readLock().unlock();
                        }
                    } catch (Exception e) {
                        logger.error("handle call event exception: ", e);
                        continue;
                    }
                }
            }
        }

        service.registerEventLogFilter(params, new EventCallback());
    }
}
