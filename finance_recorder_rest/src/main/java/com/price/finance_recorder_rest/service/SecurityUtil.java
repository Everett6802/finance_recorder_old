package com.price.finance_recorder_rest.service;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;
import java.util.UUID;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;


public class SecurityUtil 
{
    private static final Random RANDOM = new SecureRandom();
    private static final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int ITERATIONS = 10000;
    private static final int KEY_LENGTH = 256;
        
/*
 * A UUID (Universal Unique Identifier) is a 128-bit number used to uniquely identify some object 
 * or entity on the Internet.
 * UUID is either guaranteed to be different or is, at least, extremely likely to be different from
 *  any other UUID generated. 
*/
    public static String generateUUID() 
    {
        String returnValue = UUID.randomUUID().toString().replaceAll("-", "");
        return returnValue;
    }

    private static String generateRandomString(int length) 
    {
        StringBuilder returnValue = new StringBuilder(length);
        for (int i = 0; i < length; i++)
            returnValue.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        return new String(returnValue);
    }

    public static String generateUserId(int length) 
    {
        return generateRandomString(length);
    }
    
    public static String generateEmailverificationToken(int length) 
    {
        return generateRandomString(length);
    }

    public static String getSalt(int length) 
    {
        return generateRandomString(length);
    }

    public static String generateSecurePassword(String password, String salt) 
    {
        String returnValue = null;
        byte[] securePassword = hash(password.toCharArray(), salt.getBytes());
        returnValue = Base64.getEncoder().encodeToString(securePassword);
        return returnValue;
    }

    public static byte[] hash(char[] password, byte[] salt) 
    {
        PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
        Arrays.fill(password, Character.MIN_VALUE);
        try 
        {
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            return skf.generateSecret(spec).getEncoded();
        } 
        catch (NoSuchAlgorithmException e) 
        {
            throw new AssertionError("Error while hashing a password: " + e.getMessage(), e);
        } 
        catch (InvalidKeySpecException e) 
        {
            throw new AssertionError("Error while hashing a password: " + e.getMessage(), e);
        } 
        finally 
        {
            spec.clearPassword();
        }
    }
    
    public static byte[] encrypt(String securePassword, String accessTokenMaterial) throws InvalidKeySpecException 
    {
        return hash(securePassword.toCharArray(), accessTokenMaterial.getBytes());
    }
}
