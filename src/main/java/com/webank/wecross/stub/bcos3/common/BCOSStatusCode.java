package com.webank.wecross.stub.bcos3.common;

/** BCOS stub status code and message definition */
public class BCOSStatusCode {
    public static final int Success = 0;

    public static final int InvalidParameter = 2000;
    public static final int UnrecognizedRequestType = 2001;
    public static final int FetchBlockHeaderFailed = 2002;
    public static final int InvalidEncodedBlockHeader = 2003;
    public static final int TransactionProofVerifyFailed = 2004;
    public static final int TransactionReceiptProofVerifyFailed = 2005;

    public static final int TransactionReceiptNotExist = 2010;
    public static final int TransactionNotExist = 2011;
    public static final int BlockNotExist = 2012;
    public static final int TransactionProofNotExist = 2013;
    public static final int TransactionReceiptProofNotExist = 2014;

    public static final int HandleSendTransactionFailed = 2021;
    public static final int HandleCallRequestFailed = 2022;
    public static final int HandleGetBlockNumberFailed = 2023;
    public static final int HandleGetBlockFailed = 2024;
    public static final int HandleGetTransactionProofFailed = 2025;
    public static final int RegisterContractFailed = 2027;

    public static final int CallNotSuccessStatus = 2030;
    public static final int SendTransactionNotSuccessStatus = 2031;

    public static final int ABINotExist = 2040;
    public static final int EncodeAbiFailed = 2041;
    public static final int MethodNotExist = 2042;

    public static final int UnsupportedRPC = 2050;
    public static final int UnclassifiedError = 2100;

    public static String getStatusMessage(int status) {
        String message = "";
        switch (status) {
            case Success:
                message = "success";
                break;
            case InvalidParameter:
                message = "invalid parameter";
                break;
            case UnrecognizedRequestType:
                message = "unrecognized request type";
            case TransactionReceiptNotExist:
                message = "transaction receipt not exist";
                break;
            case FetchBlockHeaderFailed:
                message = "fetch block header from block header manager failed";
                break;
            case InvalidEncodedBlockHeader:
                message = "invalid encoded block header";
                break;
            case TransactionProofVerifyFailed:
                message = " transaction verify failed";
                break;
            case TransactionReceiptProofVerifyFailed:
                message = " transaction receipt verify failed";
                break;
            case TransactionNotExist:
                message = "transaction not exist";
                break;
            case TransactionProofNotExist:
                message = "transaction proof not exist";
                break;
            case TransactionReceiptProofNotExist:
                message = "transaction receipt proof not exist";
                break;
            case BlockNotExist:
                message = "block not exist";
                break;
            default:
                message = "unrecognized status: " + status;
                break;
        }

        return message;
    }
}
