package com.webank.wecross.stub.bcos.abi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContractABIDefinition {

    private ABIDefinition constructor = null;
    private Map<String, List<ABIDefinition>> functions = new HashMap<>();
    private Map<String, List<ABIDefinition>> events = new HashMap<>();
    // method id => function
    private Map<String, ABIDefinition> methodIdToFunctions = new HashMap<>();
    // event signature id => event
    private Map<String, ABIDefinition> eventIdToEvents = new HashMap<>();

    public ABIDefinition getConstructor() {
        return constructor;
    }

    public void setConstructor(ABIDefinition constructor) {
        this.constructor = constructor;
    }

    public Map<String, List<ABIDefinition>> getFunctions() {
        return functions;
    }

    public void setFunctions(Map<String, List<ABIDefinition>> functions) {
        this.functions = functions;
    }

    public Map<String, List<ABIDefinition>> getEvents() {
        return events;
    }

    public void setEvents(Map<String, List<ABIDefinition>> events) {
        this.events = events;
    }

    public Map<String, ABIDefinition> getMethodIdToFunctions() {
        return methodIdToFunctions;
    }

    public void setMethodIdToFunctions(Map<String, ABIDefinition> methodIdToFunctions) {
        this.methodIdToFunctions = methodIdToFunctions;
    }

    public Map<String, ABIDefinition> getEventIdToEvents() {
        return eventIdToEvents;
    }

    public void setEventIdToEvents(Map<String, ABIDefinition> eventIdToEvents) {
        this.eventIdToEvents = eventIdToEvents;
    }

    public void addFunction(String name, ABIDefinition abiDefinition) {
        functions.putIfAbsent(name, new ArrayList<>());
        List<ABIDefinition> abiDefinitions = functions.get(name);
        abiDefinitions.add(abiDefinition);
        // ADD TO DO :
        //  calculate method id and add abiDefinition to methodIdToFunctions
    }

    public void addEvent(String name, ABIDefinition abiDefinition) {
        events.putIfAbsent(name, new ArrayList<>());
        List<ABIDefinition> abiDefinitions = events.get(name);
        abiDefinitions.add(abiDefinition);
        // ADD TO DO :
        //  calculate event id and add abiDefinition to eventIdToFunctions
    }

    public ABIDefinition getABIDefinitionByMethodId(String methodId) {
        return methodIdToFunctions.get(methodId);
    }

    public ABIDefinition getABIDefinitionByEventId(String eventId) {
        return eventIdToEvents.get(eventId);
    }
}
