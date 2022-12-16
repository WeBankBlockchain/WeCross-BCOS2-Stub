package com.webank.wecross.stub.bcos3.performance.hellowecross;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.codec.datatypes.DynamicArray;
import org.fisco.bcos.sdk.v3.codec.datatypes.Function;
import org.fisco.bcos.sdk.v3.codec.datatypes.Type;
import org.fisco.bcos.sdk.v3.codec.datatypes.TypeReference;
import org.fisco.bcos.sdk.v3.codec.datatypes.Utf8String;
import org.fisco.bcos.sdk.v3.codec.datatypes.generated.tuples.generated.Tuple1;
import org.fisco.bcos.sdk.v3.contract.Contract;
import org.fisco.bcos.sdk.v3.crypto.CryptoSuite;
import org.fisco.bcos.sdk.v3.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.v3.model.CryptoType;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fisco.bcos.sdk.v3.model.callback.TransactionCallback;
import org.fisco.bcos.sdk.v3.transaction.model.exception.ContractException;

@SuppressWarnings("unchecked")
public class HelloWeCross extends Contract {
    public static final String[] BINARY_ARRAY = {
        "608060405234801561001057600080fd5b50610703806100206000396000f3fe608060405234801561001057600080fd5b50600436106100415760003560e01c8063108c2da11461004657806352e66db8146100645780636d4ce63c14610077575b600080fd5b61004e61007f565b60405161005b9190610497565b60405180910390f35b61004e610072366004610579565b610168565b61004e610258565b6060600080805480602002602001604051908101604052809291908181526020016000905b828210156101505783829060005260206000200180546100c390610692565b80601f01602080910402602001604051908101604052809291908181526020018280546100ef90610692565b801561013c5780601f106101115761010080835404028352916020019161013c565b820191906000526020600020905b81548152906001019060200180831161011f57829003601f168201915b5050505050815260200190600101906100a4565b5050505090506000806101639190610331565b919050565b805160609061017e906000906020850190610352565b506000805480602002602001604051908101604052809291908181526020016000905b8282101561024d5783829060005260206000200180546101c090610692565b80601f01602080910402602001604051908101604052809291908181526020018280546101ec90610692565b80156102395780601f1061020e57610100808354040283529160200191610239565b820191906000526020600020905b81548152906001019060200180831161021c57829003601f168201915b5050505050815260200190600101906101a1565b505050509050919050565b60606000805480602002602001604051908101604052809291908181526020016000905b8282101561032857838290600052602060002001805461029b90610692565b80601f01602080910402602001604051908101604052809291908181526020018280546102c790610692565b80156103145780601f106102e957610100808354040283529160200191610314565b820191906000526020600020905b8154815290600101906020018083116102f757829003601f168201915b50505050508152602001906001019061027c565b50505050905090565b508054600082559060005260206000209081019061034f91906103af565b50565b82805482825590600052602060002090810192821561039f579160200282015b8281111561039f578251805161038f9184916020909101906103cc565b5091602001919060010190610372565b506103ab9291506103af565b5090565b808211156103ab5760006103c3828261044c565b506001016103af565b8280546103d890610692565b90600052602060002090601f0160209004810192826103fa5760008555610440565b82601f1061041357805160ff1916838001178555610440565b82800160010185558215610440579182015b82811115610440578251825591602001919060010190610425565b506103ab929150610482565b50805461045890610692565b6000825580601f10610468575050565b601f01602090049060005260206000209081019061034f91905b5b808211156103ab5760008155600101610483565b6000602080830181845280855180835260408601915060408160051b87010192508387016000805b8381101561052457888603603f1901855282518051808852835b818110156104f4578281018a01518982018b015289016104d9565b8181111561050457848a838b0101525b50601f01601f1916969096018701955093860193918601916001016104bf565b509398975050505050505050565b634e487b7160e01b600052604160045260246000fd5b604051601f8201601f1916810167ffffffffffffffff8111828210171561057157610571610532565b604052919050565b6000602080838503121561058c57600080fd5b823567ffffffffffffffff808211156105a457600080fd5b8185019150601f86818401126105b957600080fd5b8235828111156105cb576105cb610532565b8060051b6105da868201610548565b918252848101860191868101908a8411156105f457600080fd5b87870192505b83831015610684578235868111156106125760008081fd5b8701603f81018c136106245760008081fd5b8881013560408882111561063a5761063a610532565b61064b828901601f19168c01610548565b8281528e828486010111156106605760008081fd5b828285018d83013760009281018c01929092525083525091870191908701906105fa565b9a9950505050505050505050565b600181811c908216806106a657607f821691505b602082108114156106c757634e487b7160e01b600052602260045260246000fd5b5091905056fea2646970667358221220d7e25f4710b5b6b4b643c64fe4dce1e08c0c949b67e0d5fde75c46b1d833a05664736f6c634300080b0033"
    };

    public static final String BINARY =
            org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", BINARY_ARRAY);

    public static final String[] SM_BINARY_ARRAY = {
        "608060405234801561001057600080fd5b50610703806100206000396000f3fe608060405234801561001057600080fd5b50600436106100415760003560e01c806305f505be14610046578063299f7f9d14610064578063656fa89a1461006c575b600080fd5b61004e61007f565b60405161005b9190610497565b60405180910390f35b61004e610168565b61004e61007a366004610579565b610241565b6060600080805480602002602001604051908101604052809291908181526020016000905b828210156101505783829060005260206000200180546100c390610692565b80601f01602080910402602001604051908101604052809291908181526020018280546100ef90610692565b801561013c5780601f106101115761010080835404028352916020019161013c565b820191906000526020600020905b81548152906001019060200180831161011f57829003601f168201915b5050505050815260200190600101906100a4565b5050505090506000806101639190610331565b919050565b60606000805480602002602001604051908101604052809291908181526020016000905b828210156102385783829060005260206000200180546101ab90610692565b80601f01602080910402602001604051908101604052809291908181526020018280546101d790610692565b80156102245780601f106101f957610100808354040283529160200191610224565b820191906000526020600020905b81548152906001019060200180831161020757829003601f168201915b50505050508152602001906001019061018c565b50505050905090565b8051606090610257906000906020850190610352565b506000805480602002602001604051908101604052809291908181526020016000905b8282101561032657838290600052602060002001805461029990610692565b80601f01602080910402602001604051908101604052809291908181526020018280546102c590610692565b80156103125780601f106102e757610100808354040283529160200191610312565b820191906000526020600020905b8154815290600101906020018083116102f557829003601f168201915b50505050508152602001906001019061027a565b505050509050919050565b508054600082559060005260206000209081019061034f91906103af565b50565b82805482825590600052602060002090810192821561039f579160200282015b8281111561039f578251805161038f9184916020909101906103cc565b5091602001919060010190610372565b506103ab9291506103af565b5090565b808211156103ab5760006103c3828261044c565b506001016103af565b8280546103d890610692565b90600052602060002090601f0160209004810192826103fa5760008555610440565b82601f1061041357805160ff1916838001178555610440565b82800160010185558215610440579182015b82811115610440578251825591602001919060010190610425565b506103ab929150610482565b50805461045890610692565b6000825580601f10610468575050565b601f01602090049060005260206000209081019061034f91905b5b808211156103ab5760008155600101610483565b6000602080830181845280855180835260408601915060408160051b87010192508387016000805b8381101561052457888603603f1901855282518051808852835b818110156104f4578281018a01518982018b015289016104d9565b8181111561050457848a838b0101525b50601f01601f1916969096018701955093860193918601916001016104bf565b509398975050505050505050565b63b95aa35560e01b600052604160045260246000fd5b604051601f8201601f1916810167ffffffffffffffff8111828210171561057157610571610532565b604052919050565b6000602080838503121561058c57600080fd5b823567ffffffffffffffff808211156105a457600080fd5b8185019150601f86818401126105b957600080fd5b8235828111156105cb576105cb610532565b8060051b6105da868201610548565b918252848101860191868101908a8411156105f457600080fd5b87870192505b83831015610684578235868111156106125760008081fd5b8701603f81018c136106245760008081fd5b8881013560408882111561063a5761063a610532565b61064b828901601f19168c01610548565b8281528e828486010111156106605760008081fd5b828285018d83013760009281018c01929092525083525091870191908701906105fa565b9a9950505050505050505050565b600181811c908216806106a657607f821691505b602082108114156106c75763b95aa35560e01b600052602260045260246000fd5b5091905056fea264697066735822122080b22fd449e0943a79ab2eca73dc6eb9e2b5bdd11d510271a5a02e37b23bdc6a64736f6c634300080b0033"
    };

    public static final String SM_BINARY =
            org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", SM_BINARY_ARRAY);

    public static final String[] ABI_ARRAY = {
        "[{\"conflictFields\":[{\"kind\":0},{\"kind\":4,\"value\":[0]}],\"inputs\":[],\"name\":\"get\",\"outputs\":[{\"internalType\":\"string[]\",\"name\":\"\",\"type\":\"string[]\"}],\"selector\":[1833756220,698318749],\"stateMutability\":\"view\",\"type\":\"function\"},{\"conflictFields\":[{\"kind\":0},{\"kind\":4,\"value\":[0]}],\"inputs\":[],\"name\":\"getAndClear\",\"outputs\":[{\"internalType\":\"string[]\",\"name\":\"\",\"type\":\"string[]\"}],\"selector\":[277622177,99943870],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"conflictFields\":[{\"kind\":0},{\"kind\":4,\"value\":[0]}],\"inputs\":[{\"internalType\":\"string[]\",\"name\":\"_ss\",\"type\":\"string[]\"}],\"name\":\"set\",\"outputs\":[{\"internalType\":\"string[]\",\"name\":\"\",\"type\":\"string[]\"}],\"selector\":[1390833080,1701816474],\"stateMutability\":\"nonpayable\",\"type\":\"function\"}]"
    };

    public static final String ABI = org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", ABI_ARRAY);

    public static final String FUNC_GET = "get";

    public static final String FUNC_GETANDCLEAR = "getAndClear";

    public static final String FUNC_SET = "set";

    protected HelloWeCross(String contractAddress, Client client, CryptoKeyPair credential) {
        super(getBinary(client.getCryptoSuite()), contractAddress, client, credential);
    }

    public static String getBinary(CryptoSuite cryptoSuite) {
        return (cryptoSuite.getCryptoTypeConfig() == CryptoType.ECDSA_TYPE ? BINARY : SM_BINARY);
    }

    public static String getABI() {
        return ABI;
    }

    public List get() throws ContractException {
        final Function function =
                new Function(
                        FUNC_GET,
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(
                                new TypeReference<DynamicArray<Utf8String>>() {}));
        List<Type> result = (List<Type>) executeCallWithSingleValueReturn(function, List.class);
        return convertToNative(result);
    }

    public TransactionReceipt getAndClear() {
        final Function function =
                new Function(
                        FUNC_GETANDCLEAR,
                        Arrays.<Type>asList(),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return executeTransaction(function);
    }

    public String getAndClear(TransactionCallback callback) {
        final Function function =
                new Function(
                        FUNC_GETANDCLEAR,
                        Arrays.<Type>asList(),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForGetAndClear() {
        final Function function =
                new Function(
                        FUNC_GETANDCLEAR,
                        Arrays.<Type>asList(),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return createSignedTransaction(function);
    }

    public Tuple1<List<String>> getGetAndClearOutput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getOutput();
        final Function function =
                new Function(
                        FUNC_GETANDCLEAR,
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(
                                new TypeReference<DynamicArray<Utf8String>>() {}));
        List<Type> results =
                this.functionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple1<List<String>>(
                convertToNative((List<Utf8String>) results.get(0).getValue()));
    }

    public TransactionReceipt set(List<String> _ss) {
        final Function function =
                new Function(
                        FUNC_SET,
                        Arrays.<Type>asList(
                                new DynamicArray<Utf8String>(
                                        Utf8String.class,
                                        org.fisco.bcos.sdk.v3.codec.Utils.typeMap(
                                                _ss, Utf8String.class))),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return executeTransaction(function);
    }

    public String set(List<String> _ss, TransactionCallback callback) {
        final Function function =
                new Function(
                        FUNC_SET,
                        Arrays.<Type>asList(
                                new DynamicArray<Utf8String>(
                                        Utf8String.class,
                                        org.fisco.bcos.sdk.v3.codec.Utils.typeMap(
                                                _ss, Utf8String.class))),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForSet(List<String> _ss) {
        final Function function =
                new Function(
                        FUNC_SET,
                        Arrays.<Type>asList(
                                new DynamicArray<Utf8String>(
                                        Utf8String.class,
                                        org.fisco.bcos.sdk.v3.codec.Utils.typeMap(
                                                _ss, Utf8String.class))),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return createSignedTransaction(function);
    }

    public Tuple1<List<String>> getSetInput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function =
                new Function(
                        FUNC_SET,
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(
                                new TypeReference<DynamicArray<Utf8String>>() {}));
        List<Type> results =
                this.functionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple1<List<String>>(
                convertToNative((List<Utf8String>) results.get(0).getValue()));
    }

    public Tuple1<List<String>> getSetOutput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getOutput();
        final Function function =
                new Function(
                        FUNC_SET,
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(
                                new TypeReference<DynamicArray<Utf8String>>() {}));
        List<Type> results =
                this.functionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple1<List<String>>(
                convertToNative((List<Utf8String>) results.get(0).getValue()));
    }

    public static HelloWeCross load(
            String contractAddress, Client client, CryptoKeyPair credential) {
        return new HelloWeCross(contractAddress, client, credential);
    }

    public static HelloWeCross deploy(Client client, CryptoKeyPair credential)
            throws ContractException {
        return deploy(
                HelloWeCross.class,
                client,
                credential,
                getBinary(client.getCryptoSuite()),
                getABI(),
                null,
                null);
    }
}
