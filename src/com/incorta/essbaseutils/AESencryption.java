package com.incorta.essbaseutils;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;

public class AESencryption{
	
    private static String ALGO = "AES";
    private static Cipher encCipher;
    
    public static String encrypt(String origString, Key secKey) throws Exception {
    	encCipher = Cipher.getInstance(ALGO);
        encCipher.init(Cipher.ENCRYPT_MODE, secKey);
        return new Base64().encodeToString(encCipher.doFinal(origString.getBytes()));
    }

    public static String decrypt(String encString, Key secKey) throws Exception {
        encCipher = Cipher.getInstance(ALGO);
        encCipher.init(Cipher.DECRYPT_MODE, secKey);
        return new String(encCipher.doFinal(Base64.decodeBase64(encString)));
    }
        
    public static Key generateKey(String key) throws Exception {
    	String rv = key;
    	for (int i=0; i<=15; i++){
    		rv+=key;
    	}
    	
    	return new SecretKeySpec(rv.substring(0,16).getBytes(), ALGO);
    }    
}