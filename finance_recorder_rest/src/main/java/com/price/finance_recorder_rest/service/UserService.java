package com.price.finance_recorder_rest.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;

import com.price.finance_recorder_rest.exceptions.CouldNotCreateRecordException;
import com.price.finance_recorder_rest.exceptions.ExceptionType;
import com.price.finance_recorder_rest.exceptions.NoRecordFoundException;
import com.price.finance_recorder_rest.persistence.MySQLDAO;
import com.price.finance_recorder_rest.persistence.UserEntity;


public class UserService 
{
	public UserDTO create(UserDTO dto)
	{
// Validate the required fields
//		dto.validateRequiredFields();

// Check if user already exists
		UserEntity entity = MySQLDAO.read_user(dto.getUsername());
        if (entity != null) 
            throw new CouldNotCreateRecordException(ExceptionType.RECORD_ALREADY_EXISTS.name());
// Generate secure public user id 
        String user_id = SecurityUtil.generate_user_id(30);
        dto.setUserId(user_id);
// Generate salt 
        String salt = SecurityUtil.get_salt(30);
// Generate secure password 
        String encrypted_password = SecurityUtil.generate_secure_password(dto.getPassword(), salt);
        dto.setSalt(salt);
        dto.setEncryptedPassword(encrypted_password);
// Write into MySQL
        UserDTO dto_rsp = MySQLDAO.create_user(dto);
        return dto_rsp;
	}

    public List<UserDTO> read(int start, int limit) 
    {
    	List<UserEntity> entity_list = MySQLDAO.read_users(start, limit);
		List<UserDTO> dto_list = new ArrayList<UserDTO>();
		for (Object entity : entity_list)
		{
			UserDTO dto = new UserDTO();
			BeanUtils.copyProperties(entity, dto);
			dto_list.add(dto);
		}
		return dto_list;
    }

    public UserDTO read_by_username(String username) 
    {
    	UserEntity entity = MySQLDAO.read_user(username);
    	if (entity == null)
    		throw new NoRecordFoundException(ExceptionType.NO_RECORD_FOUND.name());
		UserDTO dto = new UserDTO();
		BeanUtils.copyProperties(entity, dto);
		return dto;
    }

	public void update(UserDTO dto)
	{
// Generate salt 
        String salt = SecurityUtil.get_salt(30);
// Generate secure password 
        String encrypted_password = SecurityUtil.generate_secure_password(dto.getPassword(), salt);
        dto.setSalt(salt);
        dto.setEncryptedPassword(encrypted_password);
		
		MySQLDAO.update_user(dto);
	}

	public void delete(UserDTO dto)
	{
		MySQLDAO.delete_user(dto);
	}
}
