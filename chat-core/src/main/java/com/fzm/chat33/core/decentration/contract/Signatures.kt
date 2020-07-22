package com.fzm.chat33.core.decentration.contract

import com.fuzamei.common.ext.bytes2Hex
import com.fzm.chat33.core.manager.CipherManager

/**
 * @author zhengjy
 * @since 2020/02/12
 * Description:
 */
/**
 * 合约接口签名验证方法
 *
 * @param map           需要参加签名的参数
 * @param privateKey    私钥
 */
fun Map<String, Any>.sign(privateKey: String): String {
    val sb = StringBuilder()
    val sorted = keys.toList().sorted()
    for (key in sorted) {
        if (sorted.indexOf(key) != 0) {
            sb.append("&")
        }
        sb.append("${key}=${get(key)}")
    }
    return CipherManager.sign(sb.toString(), privateKey).bytes2Hex()
}

///**
// * BTC/BTY公钥转地址
// *
// * @param publicKey 公钥
// */
//fun calculateAddress(publicKey: ByteArray): String {
//    try {
//        val sha256Bytes: ByteArray = publicKey.sha256Bytes()
//        //2.将第1步结果进行RIPEMD160哈希
//        val digest = RIPEMD160Digest()
//        digest.update(sha256Bytes, 0, sha256Bytes.size)
//        val ripemd160Bytes = ByteArray(digest.digestSize)
//        digest.doFinal(ripemd160Bytes, 0)
//        //3.将BTC地址版本号(00)加在第2步结果签名
//        val result = "00" + Hex.toHexString(ripemd160Bytes)
//        //4.将第3步结果进行双SHA256哈希
//        val firstHash: ByteArray = result.hex2Bytes().sha256Bytes()
//        val doubleHash: ByteArray = firstHash.sha256Bytes()
//        //5.取第4步结果的前4字节，并加在第3步结果后面
//        val checksum: String = Hex.toHexString(doubleHash).substring(0, 8)
//        val checkStr = result + checksum
//        val address: String = checkStr.hex2Bytes().base58EncodeStr()
//        println("zhengjy:address:${address}")
//        return address
//    } catch (e: Exception) {
//        e.printStackTrace()
//        return ""
//    }
//}

//val spec = ECParameterSpec(
//        EllipticCurve(
//                ECFieldFp(BigInteger("115792089237316195423570985008687907853269984665640564039457584007908834671663")),
//                BigInteger.valueOf(0L),
//                BigInteger.valueOf(7L)
//        ),
//        ECPoint(
//                BigInteger("79be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f81798", 16),
//                BigInteger("483ada7726a3c4655da4fbfc0e1108a8fd17b448a68554199c47d08ffb10d4b8", 16)
//        ),
//        BigInteger("115792089237316195423570985008687907852837564279074904382605163141518161494337"),
//        1
//)
//val keySpec = ECPrivateKeySpec(BigInteger(privateKey, 16), spec)

// 地址：1JoFzozbxvst22c2K7MBYwQGjCaMZbC5Qm
// 公钥：02be5910fb49cb6e36f939080afd583be3676db96e7ed5177df6a401e1419f7375
// 私钥：16d7ccefb9d1abc06e2e6d6567105a1f24f2e069569360ada51837d45ec21b71
