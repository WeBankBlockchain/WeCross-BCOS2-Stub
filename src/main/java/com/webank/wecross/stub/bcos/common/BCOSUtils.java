package com.webank.wecross.stub.bcos.common;

import com.webank.wecross.stub.bcos.custom.DeployContractHandler;
import java.io.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BCOSUtils {
    private static final Logger logger = LoggerFactory.getLogger(DeployContractHandler.class);

    public static byte[] objectToBytes(Object obj) {
        byte[] bytes;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            oos.flush();
            bytes = bos.toByteArray();
            oos.close();
            bos.close();
        } catch (IOException e) {
            logger.error("object to bytes failed", e);
            return null;
        }
        return bytes;
    }

    public static Object bytesToObject(byte[] bytes) {
        Object obj;
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bis);
            obj = ois.readObject();
            ois.close();
            bis.close();
        } catch (IOException | ClassNotFoundException e) {
            logger.error("object to bytes failed", e);
            return null;
        }
        return obj;
    }
}
