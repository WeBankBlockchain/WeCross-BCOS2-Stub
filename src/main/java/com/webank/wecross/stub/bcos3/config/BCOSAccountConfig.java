package com.webank.wecross.stub.bcos3.config;

public class BCOSAccountConfig {
    private String accountFile;
    private String passwd;
    private String type;

    public void setAccountFile(String accountFile) {
        this.accountFile = accountFile;
    }

    public void setPasswd(String passwd) {
        this.passwd = passwd;
    }

    public String getAccountFile() {
        return accountFile;
    }

    public String getPasswd() {
        return passwd;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "BCOSAccountConfig{"
                + "accountFile='"
                + accountFile
                + '\''
                + ", passwd='"
                + passwd
                + '\''
                + ", type='"
                + type
                + '\''
                + '}';
    }
}
