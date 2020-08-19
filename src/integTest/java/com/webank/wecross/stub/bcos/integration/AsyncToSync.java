package com.webank.wecross.stub.bcos.integration;

import java.util.concurrent.Semaphore;

class AsyncToSync  {
    AsyncToSync() {
        try {
            semaphore.acquire(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public Semaphore getSemaphore() {
        return semaphore;
    }

    public void setSemaphore(Semaphore semaphore) {
        this.semaphore = semaphore;
    }

    public Semaphore semaphore = new Semaphore(1, true);
};