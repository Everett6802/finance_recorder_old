package com.price.finance_recorder_rest.service;

import com.price.finance_recorder_rest.exceptions.AuthenticationException;
import com.price.finance_recorder_rest.exceptions.ExceptionType;
import com.price.finance_recorder_rest.persistence.MySQLDAO;

public class UserService 
{
	public UserDTO create(UserDTO dto)
	{
// Validate the required fields
//		dto.validateRequiredFields();

// Check if user already exists
//        UserDTO existingUser = this.getUserByUserName(user.getEmail());
//        if (existingUser != null) {
//            throw new CouldNotCreateRecordException(ExceptionType.RECORD_ALREADY_EXISTS.name());
//        }

// Generate secure public user id 
        String user_id = SecurityUtil.generateUserId(30);
        dto.setUserId(user_id);

// Generate salt 
        String salt = SecurityUtil.getSalt(30);
// Generate secure password 
        String encryptedPassword = SecurityUtil.generateSecurePassword(dto.getPassword(), salt);
        dto.setSalt(salt);
        dto.setEncryptedPassword(encryptedPassword);
//        user.setEmailVerificationStatus(false);
//        user.setEmailVerificationToken(SecurityUtil.generateEmailverificationToken(30));
        MySQLDAO.create_user(dto);
        return dto;
	}
//	public AuthenticationDTO authenticate(String username, String password) throws AuthenticationException 
//	{
//        AuthenticationDTO storedUser = AuthenticationService.getUserByUserName(username); // User name must be unique in our system
//
//        if (storedUser == null) {
//            throw new AuthenticationException(ExceptionType.AUTHENTICATION_FAILED.get_exception_message());
//        }
//
//        String encryptedPassword = null;
//
//        encryptedPassword = new UserProfileUtils().
//                generateSecurePassword(password, storedUser.getSalt());
//
//        boolean authenticated = false;
//        if (encryptedPassword != null && encryptedPassword.equalsIgnoreCase(storedUser.getEncryptedPassword())) {
//            if (username != null && username.equalsIgnoreCase(storedUser.getEmail())) {
//                authenticated = true;
//            }
//        }
//
//        if (!authenticated) {
//            throw new AuthenticationException(ExceptionType.AUTHENTICATION_FAILED.getExceptionMessage());
//        }
//
//        return storedUser;
//	}
//
//    public String issueAccessToken(AuthenticationDTO userProfile) throws AuthenticationException 
//    {
//        String returnValue = null;
//
//        String newSaltAsPostfix = userProfile.getSalt();
//        String accessTokenMaterial = userProfile.getUserId() + newSaltAsPostfix;
//
//        byte[] encryptedAccessToken = null;
//        try {
//            encryptedAccessToken = new UserProfileUtils().encrypt(userProfile.getEncryptedPassword(), accessTokenMaterial);
//        } catch (InvalidKeySpecException ex) {
//            Logger.getLogger(AuthenticationServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
//            throw new AuthenticationException("Faled to issue secure access token");
//        }
//
//        String encryptedAccessTokenBase64Encoded = Base64.getEncoder().encodeToString(encryptedAccessToken);
//
//        // Split token into equal parts
//        int tokenLength = encryptedAccessTokenBase64Encoded.length();
//
//        String tokenToSaveToDatabase = encryptedAccessTokenBase64Encoded.substring(0, tokenLength / 2);
//        returnValue = encryptedAccessTokenBase64Encoded.substring(tokenLength / 2, tokenLength);
//
//        userProfile.setToken(tokenToSaveToDatabase);
//        // Update the token into database
//        updateUserProfile(userProfile);
//
//        return returnValue;
//    }
//
//    public void resetSecurityCridentials(String password, AuthenticationDTO userProfile) 
//    {
//        // Generate a new salt
//        UserProfileUtils userUtils = new UserProfileUtils();
//        String salt = userUtils.getSalt(30);
//        
//        // Generate a new password 
//        String securePassword = userUtils.generateSecurePassword(password, salt);
//        userProfile.setSalt(salt);
//        userProfile.setEncryptedPassword(securePassword);
//        
//        // Update user profile 
//        updateUserProfile(userProfile);
// 
//    }
}
