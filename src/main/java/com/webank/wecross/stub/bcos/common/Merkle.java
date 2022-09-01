package com.webank.wecross.stub.bcos.common;

import java.util.Arrays;
import java.util.List;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.model.MerkleProofUnit;
import org.fisco.bcos.sdk.utils.Numeric;

public class Merkle {
    public static String calculateMerkleRoot(
            List<MerkleProofUnit> merkleProofUnits, String hash, CryptoSuite cryptoSuite) {
        if (merkleProofUnits == null) {
            return hash;
        }
        String result = hash;
        for (MerkleProofUnit merkleProofUnit : merkleProofUnits) {
            String left = splicing(merkleProofUnit.getLeft());
            String right = splicing(merkleProofUnit.getRight());
            String input = splicing("0x", left, result.substring(2), right);
            byte[] inputHash = cryptoSuite.hash(Numeric.hexStringToByteArray(input));
            result = Numeric.toHexString(inputHash);
        }
        return result;
    }

    private static String splicing(List<String> stringList) {
        StringBuilder result = new StringBuilder();
        for (String eachString : stringList) {
            result.append(eachString);
        }
        return result.toString();
    }

    private static String splicing(String... stringList) {
        return splicing(Arrays.<String>asList(stringList));
    }
}
