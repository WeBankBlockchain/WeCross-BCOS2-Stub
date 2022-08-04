package com.webank.wecross.stub.bcos.common;

import org.fisco.bcos.sdk.crypto.hash.Hash;
import org.fisco.bcos.sdk.crypto.hash.Keccak256;
import org.fisco.bcos.sdk.model.MerkleProofUnit;

import java.util.Arrays;
import java.util.List;

/**
 * @projectName: bcos2-stub
 * @package: com.webank.wecross.stub.bcos.common
 * @className: Merkle
 * @author: lbhan2
 * @description: Merkle
 * @date: 2022/8/2 16:16
 * @Copyright: 2021 www.iflytek.com Inc. All rights reserved.
 * 注意：本内容仅限于科大讯飞股份有限公司内部传阅，禁止外泄以及用于其他的商业目的
 */
public class Merkle {
    public static String calculateMerkleRoot(List<MerkleProofUnit> merkleProofUnits, String hash) {
        if (merkleProofUnits == null) {
            return hash;
        }
        String result = hash;
        for (MerkleProofUnit merkleProofUnit : merkleProofUnits) {
            String left = splicing(merkleProofUnit.getLeft());
            String right = splicing(merkleProofUnit.getRight());
            String input = splicing("0x", left, result.substring(2), right);
            result = Hash.sha3(input);
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
