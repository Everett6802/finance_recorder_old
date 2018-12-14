package com.price.finance_recorder_rest.service;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import com.price.finance_recorder_rest.exceptions.AuthenticationException;
import com.price.finance_recorder_rest.namebinding.AuthenticationFilter;


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
    public static String generate_uuid() 
    {
        String returnValue = UUID.randomUUID().toString().replaceAll("-", "");
        return returnValue;
    }

    private static String generate_random_string(int length) 
    {
        StringBuilder returnValue = new StringBuilder(length);
        for (int i = 0; i < length; i++)
            returnValue.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        return new String(returnValue);
    }

    public static String generate_user_id(int length) 
    {
        return generate_random_string(length);
    }
    
//    public static String generateEmailverificationToken(int length) 
//    {
//        return generate_random_string(length);
//    }

    public static String get_salt(int length) 
    {
        return generate_random_string(length);
    }

    public static String generate_secure_password(String password, String salt) 
    {
        String returnValue = null;
        byte[] securePassword = hash(password.toCharArray(), salt.getBytes());
        returnValue = Base64.getEncoder().encodeToString(securePassword);
        return returnValue;
    }

    private static byte[] hash(char[] password, byte[] salt) 
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
    
    private static byte[] encrypt(String securePassword, String access_token_material) throws InvalidKeySpecException 
    {
        return hash(securePassword.toCharArray(), access_token_material.getBytes());
    }
    
    public static String get_encrypted_access_token_base64_encoded(String salt, String user_id, String encrypted_assword) throws InvalidKeySpecException
    {
        String access_token_material = user_id + salt;
        byte[] encrypted_access_token = encrypt(encrypted_assword, access_token_material);
        return Base64.getEncoder().encodeToString(encrypted_access_token);
    }
}
