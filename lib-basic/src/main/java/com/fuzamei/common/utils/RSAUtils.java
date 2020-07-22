package com.fuzamei.common.utils;

import com.fuzamei.common.FzmFramework;
import com.fuzamei.commonlib.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

/**
 * @author
 * @since login pw 加解密
 */
public final class RSAUtils
{
	private static String RSA = "RSA";
	/* 密钥内容 base64 code */
	private static String PUCLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDalGDNz4TMaMeF9zDZTK1IZj6X\n" +
			"eyDNzrF6oyQM4bkmWF9AQy4Wnvsh8oRwTu3S+tBxQ5fc9xgMgPfsYSXnIbcNd2u8\n" +
			"TAO3t0LAVhcwcQHv8Y6kyFuxSrmqasxzkzEnD6gddwH+rja7ZMRP9LTIPZSv7ps3\n" +
			"jIY06naxHoO+pvua8wIDAQAB";
	private static String PRIVATE_KEY = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBANqUYM3PhMxox4X3\n" +
			"MNlMrUhmPpd7IM3OsXqjJAzhuSZYX0BDLhae+yHyhHBO7dL60HFDl9z3GAyA9+xh\n" +
			"Jechtw13a7xMA7e3QsBWFzBxAe/xjqTIW7FKuapqzHOTMScPqB13Af6uNrtkxE/0\n" +
			"tMg9lK/umzeMhjTqdrEeg76m+5rzAgMBAAECgYAxf3JgiQLgq0Wrn6uvOb7v6z93\n" +
			"XEm0cX8db35FcIRWG6MOUWhJdR0Xteyp5iXTJjs3uv/T6RzIEBiUrfdAXREvjxTm\n" +
			"emvyfwyEbEs80F+1PolDveBgEksBzn3ZFwrVgg501TW1R2sN4F5z0wl7KZO/cwaM\n" +
			"xtgOF60L8miFWWfJqQJBAP5pzkmcslqPaRVYg8MIeR8hbw6PT4OT2NRh6/8CA1J8\n" +
			"Paoerdkm1Zs0YQKUBJI7XpZNthj1BPXrnR/cCFyBcX8CQQDb8VxMFcKJnjgjVxtK\n" +
			"R9FphgbxUpEZW4guqpEneqGX4OjlhzZRTfk4YnpC/QmC+Caaw+AO5uJNdkyH4ljr\n" +
			"q+iNAkEAhAO1Nn9oERPvjFME9DQ2XZAEx8JXmUgjsHkrc4TA4pFzkg4fWdoL52Bz\n" +
			"olnaUWMbPtUOU774lv9u5fRQJmhI0wJADL8CEE66hWsr6bBknntnAWyI/ndAfW22\n" +
			"iK3N17Hdp4WbOaIKXc/c/42FdFhhsrta0WFEnNh4iPCwIrfVW2MNCQJAHuMJYXx3\n" +
			"KvHJxsjGOxfhKkM9NDhqGLCe/Y/1KHXfKwDEYJh0is+IkKmYD57tI936ciJQzCFu\n" +
			"p+FEQTw3Rc0EYA==";

	/**
	 *  加密
	 * @param data pw
	 * @return
     */
	public static String encrypt(String data){
		try {
			if (data == null){
				return null;
			}
			// 从字符串中得到公钥
			PublicKey publicKey = loadPublicKey(PUCLIC_KEY);
			// 从文件中得到公钥
			//InputStream inPublic = getResources().getAssets().open("rsa_public_key.pem");
			//PublicKey publicKey = RSAUtils.loadPublicKey(inPublic);
			// 加密
			byte[] encryptByte = RSAUtils.encryptData(data.getBytes(), publicKey);
			// 为了方便观察吧加密后的数据用base64加密转一下，要不然看起来是乱码,所以解密是也是要用Base64先转换
			String afterencrypt = Base64Utils.encode(encryptByte);
			return afterencrypt;
		}catch (Exception e){

		}
		return null;
	}

	/**
	 *  解密
	 * @param data 加密后的数据
	 * @return
     */
	public static String decrypt(String data){
		try {
			if (data == null){
				return null;
			}
			// 从字符串中得到私钥
			PrivateKey privateKey = loadPrivateKey(PRIVATE_KEY);
			// 从文件中得到私钥
			//InputStream inPrivate = getResources().getAssets().open("pkcs8_rsa_private_key.pem");
			//PrivateKey privateKey = RSAUtils.loadPrivateKey(inPrivate);
			// 因为RSA加密后的内容经Base64再加密转换了一下，所以先Base64解密回来再给RSA解密
			byte[] decryptByte = RSAUtils.decryptData(Base64Utils.decode(data), privateKey);
			String decryptStr = new String(decryptByte);
			return decryptStr;
		}catch (Exception e){

		}
		return null;
	}

	/**
	 * 随机生成RSA密钥对(默认密钥长度为1024)
	 * 
	 * @return
	 */
	public static KeyPair generateRSAKeyPair()
	{
		return generateRSAKeyPair(1024);
	}

	/**
	 * 随机生成RSA密钥对
	 * 
	 * @param keyLength
	 *            密钥长度，范围：512～2048<br>
	 *            一般1024
	 * @return
	 */
	public static KeyPair generateRSAKeyPair(int keyLength)
	{
		try
		{
			KeyPairGenerator kpg = KeyPairGenerator.getInstance(RSA);
			kpg.initialize(keyLength);
			return kpg.genKeyPair();
		} catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 用公钥加密 <br>
	 * 每次加密的字节数，不能超过密钥的长度值减去11
	 * 
	 * @param data
	 *            需加密数据的byte数据
	 * @param publicKey
	 *            公钥
	 * @return 加密后的byte型数据
	 */
	public static byte[] encryptData(byte[] data, PublicKey publicKey)
	{
		try
		{
			Cipher cipher = Cipher.getInstance(RSA);
			// 编码前设定编码方式及密钥
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
			// 传入编码数据并返回编码结果
			return cipher.doFinal(data);
		} catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 用私钥解密
	 * 
	 * @param encryptedData
	 *            经过encryptedData()加密返回的byte数据
	 * @param privateKey
	 *            私钥
	 * @return
	 */
	public static byte[] decryptData(byte[] encryptedData, PrivateKey privateKey)
	{
		try
		{
			Cipher cipher = Cipher.getInstance(RSA);
			cipher.init(Cipher.DECRYPT_MODE, privateKey);
			return cipher.doFinal(encryptedData);
		} catch (Exception e)
		{
			return null;
		}
	}

	/**
	 * 通过公钥byte[](publicKey.getEncoded())将公钥还原，适用于RSA算法
	 * 
	 * @param keyBytes
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 */
	public static PublicKey getPublicKey(byte[] keyBytes) throws NoSuchAlgorithmException,
			InvalidKeySpecException
	{
		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance(RSA);
		PublicKey publicKey = keyFactory.generatePublic(keySpec);
		return publicKey;
	}

	/**
	 * 通过私钥byte[]将公钥还原，适用于RSA算法
	 * 
	 * @param keyBytes
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 */
	public static PrivateKey getPrivateKey(byte[] keyBytes) throws NoSuchAlgorithmException,
			InvalidKeySpecException
	{
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance(RSA);
		PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
		return privateKey;
	}

	/**
	 * 使用N、e值还原公钥
	 * 
	 * @param modulus
	 * @param publicExponent
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 */
	public static PublicKey getPublicKey(String modulus, String publicExponent)
			throws NoSuchAlgorithmException, InvalidKeySpecException
	{
		BigInteger bigIntModulus = new BigInteger(modulus);
		BigInteger bigIntPrivateExponent = new BigInteger(publicExponent);
		RSAPublicKeySpec keySpec = new RSAPublicKeySpec(bigIntModulus, bigIntPrivateExponent);
		KeyFactory keyFactory = KeyFactory.getInstance(RSA);
		PublicKey publicKey = keyFactory.generatePublic(keySpec);
		return publicKey;
	}

	/**
	 * 使用N、d值还原私钥
	 * 
	 * @param modulus
	 * @param privateExponent
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 */
	public static PrivateKey getPrivateKey(String modulus, String privateExponent)
			throws NoSuchAlgorithmException, InvalidKeySpecException
	{
		BigInteger bigIntModulus = new BigInteger(modulus);
		BigInteger bigIntPrivateExponent = new BigInteger(privateExponent);
		RSAPublicKeySpec keySpec = new RSAPublicKeySpec(bigIntModulus, bigIntPrivateExponent);
		KeyFactory keyFactory = KeyFactory.getInstance(RSA);
		PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
		return privateKey;
	}

	/**
	 * 从字符串中加载公钥
	 * 
	 * @param publicKeyStr
	 *            公钥数据字符串
	 * @throws Exception
	 *             加载公钥时产生的异常
	 */
	public static PublicKey loadPublicKey(String publicKeyStr) throws Exception
	{
		try
		{
			byte[] buffer = Base64Utils.decode(publicKeyStr);
			KeyFactory keyFactory = KeyFactory.getInstance(RSA);
			X509EncodedKeySpec keySpec = new X509EncodedKeySpec(buffer);
			return keyFactory.generatePublic(keySpec);
		} catch (NoSuchAlgorithmException e)
		{
			throw new Exception(FzmFramework.getString(R.string.basic_error_alg_negation));
		} catch (InvalidKeySpecException e)
		{
			throw new Exception(FzmFramework.getString(R.string.basic_error_public_key));
		} catch (NullPointerException e)
		{
			throw new Exception(FzmFramework.getString(R.string.basic_error_public_key_empty));
		}
	}

	/**
	 * 从字符串中加载私钥<br>
	 * 加载时使用的是PKCS8EncodedKeySpec（PKCS#8编码的Key指令）。
	 * 
	 * @param privateKeyStr
	 * @return
	 * @throws Exception
	 */
	public static PrivateKey loadPrivateKey(String privateKeyStr) throws Exception
	{
		try
		{
			byte[] buffer = Base64Utils.decode(privateKeyStr);
			// X509EncodedKeySpec keySpec = new X509EncodedKeySpec(buffer);
			PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(buffer);
			KeyFactory keyFactory = KeyFactory.getInstance(RSA);
			return keyFactory.generatePrivate(keySpec);
		} catch (NoSuchAlgorithmException e)
		{
			throw new Exception(FzmFramework.getString(R.string.basic_error_alg_negation));
		} catch (InvalidKeySpecException e)
		{
			throw new Exception(FzmFramework.getString(R.string.basic_error_public_key));
		} catch (NullPointerException e)
		{
			throw new Exception(FzmFramework.getString(R.string.basic_error_public_key_empty));
		}
	}

	/**
	 * 从文件中输入流中加载公钥
	 * 
	 * @param in
	 *            公钥输入流
	 * @throws Exception
	 *             加载公钥时产生的异常
	 */
	public static PublicKey loadPublicKey(InputStream in) throws Exception
	{
		try
		{
			return loadPublicKey(readKey(in));
		} catch (IOException e)
		{
			throw new Exception(FzmFramework.getString(R.string.basic_error_public_key_input_stream));
		} catch (NullPointerException e)
		{
			throw new Exception(FzmFramework.getString(R.string.basic_error_public_key_input_stream_empty));
		}
	}

	/**
	 * 从文件中加载私钥
	 *
	 *            私钥文件名
	 * @return 是否成功
	 * @throws Exception
	 */
	public static PrivateKey loadPrivateKey(InputStream in) throws Exception
	{
		try
		{
			return loadPrivateKey(readKey(in));
		} catch (IOException e)
		{
			throw new Exception(FzmFramework.getString(R.string.basic_error_private_key_input_stream));
		} catch (NullPointerException e)
		{
			throw new Exception(FzmFramework.getString(R.string.basic_error_private_key_input_stream_empty));
		}
	}

	/**
	 * 读取密钥信息
	 * 
	 * @param in
	 * @return
	 * @throws IOException
	 */
	private static String readKey(InputStream in) throws IOException
	{
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String readLine = null;
		StringBuilder sb = new StringBuilder();
		while ((readLine = br.readLine()) != null)
		{
			if (readLine.charAt(0) == '-')
			{
				continue;
			} else
			{
				sb.append(readLine);
				sb.append('\r');
			}
		}

		return sb.toString();
	}

	/**
	 * 打印公钥信息
	 * 
	 * @param publicKey
	 */
	public static void printPublicKeyInfo(PublicKey publicKey)
	{
		RSAPublicKey rsaPublicKey = (RSAPublicKey) publicKey;
		System.out.println("----------RSAPublicKey----------");
		System.out.println("Modulus.length=" + rsaPublicKey.getModulus().bitLength());
		System.out.println("Modulus=" + rsaPublicKey.getModulus().toString());
		System.out.println("PublicExponent.length=" + rsaPublicKey.getPublicExponent().bitLength());
		System.out.println("PublicExponent=" + rsaPublicKey.getPublicExponent().toString());
	}

	public static void printPrivateKeyInfo(PrivateKey privateKey)
	{
		RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) privateKey;
		System.out.println("----------RSAPrivateKey ----------");
		System.out.println("Modulus.length=" + rsaPrivateKey.getModulus().bitLength());
		System.out.println("Modulus=" + rsaPrivateKey.getModulus().toString());
		System.out.println("PrivateExponent.length=" + rsaPrivateKey.getPrivateExponent().bitLength());
		System.out.println("PrivatecExponent=" + rsaPrivateKey.getPrivateExponent().toString());

	}

}
