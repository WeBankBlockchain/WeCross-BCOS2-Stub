package com.webank.wecross.stub.bcos.common;

import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.model.MerkleProofUnit;

import java.util.Arrays;
import java.util.List;

/**
 * @projectName: bcos2-stub
 * @package: com.webank.wecross.stub.bcos.common
 * @className: Merkle
 * @date: 2022/8/2 16:16
 */
public class Merkle {
    public static String calculateMerkleRoot(List<MerkleProofUnit> merkleProofUnits, String hash, CryptoSuite cryptoSuite) {
        if (merkleProofUnits == null) {
            return hash;
        }
        String result = hash;
        for (MerkleProofUnit merkleProofUnit : merkleProofUnits) {
            String left = splicing(merkleProofUnit.getLeft());
            String right = splicing(merkleProofUnit.getRight());
            String input = splicing("0x", left, result.substring(2), right);
            result = cryptoSuite.hash(input);
            //result = Hash.sha3(input);
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
