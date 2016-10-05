package com.vss.dev;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PasswordAuth {
	private byte[] salt = new byte[16];
	private Base64.Encoder enc;
	private SecretKeyFactory f;
	private KeySpec spec;
	private static final String ALGORITHM = "PBKDF2WithHmacSHA1";

	public PasswordAuth() throws NoSuchAlgorithmException{
		salt = "asegtsexashgaeeg".getBytes();
		f = SecretKeyFactory.getInstance(ALGORITHM);
		enc = Base64.getEncoder();

	}
	public String encode(String pw) throws InvalidKeySpecException{
		spec = new PBEKeySpec(pw.toCharArray(), salt, 65536, 128);
		return "$84$"+enc.encodeToString(f.generateSecret(spec).getEncoded());
	}
	public static void main(String[] args){
		PasswordAuth pa;
		try {
			pa = new PasswordAuth();
			System.out.println(pa.encode("salasana"));
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
