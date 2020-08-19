package com.webank.wecross.stub.bcos.custom;

import com.webank.wecross.stub.bcos.AsyncCnsService;
import com.webank.wecross.stub.bcos.common.BCOSConstant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandHandlerDispatcher {

    private static final Logger logger = LoggerFactory.getLogger(CommandHandlerDispatcher.class);

    private Map<String, CommandHandler> commandMapper = new HashMap<>();

    public Map<String, CommandHandler> getCommandMapper() {
        return commandMapper;
    }

    public void setCommandMapper(Map<String, CommandHandler> commandMapper) {
        this.commandMapper = commandMapper;
    }

    public CommandHandlerDispatcher(AsyncCnsService asyncCnsService) {
        this.asyncCnsService = asyncCnsService;
    }

    private AsyncCnsService asyncCnsService;

    public AsyncCnsService getAsyncCnsService() {
        return asyncCnsService;
    }

    public void setAsyncCnsService(AsyncCnsService asyncCnsService) {
        this.asyncCnsService = asyncCnsService;
    }

    public void initializeCommandMapper() {

        registerCommandHandler(
                BCOSConstant.CUSTOM_COMMAND_DEPLOY,
                new DeployContractHandler(getAsyncCnsService()));
        registerCommandHandler(
                BCOSConstant.CUSTOM_COMMAND_REGISTER, new RegisterCnsHandler(getAsyncCnsService()));

        logger.info("list custom commands: {} ", commandMapper.keySet());
    }

    /**
     * register command handler by command name
     *
     * @param command
     * @param commandHandler
     */
    public void registerCommandHandler(String command, CommandHandler commandHandler) {
        commandMapper.putIfAbsent(command, commandHandler);
        logger.info("command: {}, handler: {} ", command, commandHandler.getClass().getName());
    }

    /**
     * @param command
     * @return command handler
     */
    public CommandHandler matchCommandHandler(String command) {
        CommandHandler commandHandler = commandMapper.get(command);
        if (Objects.isNull(commandHandler)) {
            logger.warn("command '{}' not found", command);
        } else if (logger.isTraceEnabled()) {
            logger.trace("command: {}, handler: {}", command, commandHandler.getClass().getName());
        }

        return commandHandler;
    }
}
