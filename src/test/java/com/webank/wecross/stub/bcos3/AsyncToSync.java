package com.webank.wecross.stub.bcos3;

import java.util.concurrent.Semaphore;

public class AsyncToSync {
    public AsyncToSync() {
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
}
