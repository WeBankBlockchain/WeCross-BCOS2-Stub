package com.webank.wecross.stub.bcos3.integration;

import com.webank.wecross.stub.Connection;
import com.webank.wecross.stub.ResourceInfo;

import java.util.List;

public class ConnectionEventHandlerImplMock implements Connection.ConnectionEventHandler {

    private List<ResourceInfo> resourceInfos;

    @Override
    public void onResourcesChange(List<ResourceInfo> resourceInfos) {
        this.resourceInfos = resourceInfos;
    }

    public List<ResourceInfo> getResourceInfos() {
        return resourceInfos;
    }

    public void setResourceInfos(List<ResourceInfo> resourceInfos) {
        this.resourceInfos = resourceInfos;
    }
}
