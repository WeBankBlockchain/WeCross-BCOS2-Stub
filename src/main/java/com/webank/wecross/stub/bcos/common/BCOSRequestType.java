package com.webank.wecross.stub.bcos.common;

/** Request type */
public class BCOSRequestType {
    public static final int CALL = 1000;
    public static final int SEND_TRANSACTION = 1001;
    public static final int GET_BLOCK_NUMBER = 1002;
    public static final int GET_BLOCK_HEADER = 1003;
    public static final int GET_TRANSACTION_PROOF = 1004;
    public static final int DEPLOY_CONTRACT = 1005;
    public static final int REGISTER_CONTRACT = 1006;
}
