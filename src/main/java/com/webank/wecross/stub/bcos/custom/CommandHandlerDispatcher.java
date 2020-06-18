package com.webank.wecross.stub.bcos.custom;

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

    public void initializeCommandMapper() {
        DeployContractHandler deployContractHandler = new DeployContractHandler();
        registerCommandHandler("deploy", deployContractHandler);

        RegisterCnsHandler registerCnsHandler = new RegisterCnsHandler();
        registerCommandHandler("register", registerCnsHandler);
        logger.info("initialize size: {}", commandMapper.size());
        logger.info("commands: {} ", commandMapper.keySet());
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
        } else {
            logger.trace("command: {}, handler: {}", command, commandHandler.getClass().getName());
        }

        return commandHandler;
    }
}
