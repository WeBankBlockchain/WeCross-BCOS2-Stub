package com.webank.wecross.stub.bcos.abi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Contract {
    private String name;
    private Map<String, List<Function>> functions = new HashMap<String, List<Function>>();
    private Map<String, Event> events = new HashMap<String, Event>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, List<Function>> getFunctions() {
        return functions;
    }

    public void setFunctions(Map<String, List<Function>> functions) {
        this.functions = functions;
    }

    public Map<String, Event> getEvents() {
        return events;
    }

    public void setEvents(Map<String, Event> events) {
        this.events = events;
    }
}
