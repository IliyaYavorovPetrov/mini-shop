package com.example.minishop.app.users;

import com.example.minishop.app.users.dtos.UserRequestDTO;
import com.example.minishop.app.users.dtos.UserResponseDTO;
import com.example.minishop.app.users.entities.UserEntity;
import com.example.minishop.app.users.models.UserModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.EnumSet;

public class UserMapper {
    private static final Logger logger = LoggerFactory.getLogger(UserMapper.class);

    public static UserModel fromUserRequestDTOtoUserModel(UserRequestDTO userRequestDTO) {
        UserRoleType userRoleType = getRoleTypeFromStringOrDefault(userRequestDTO.role());

        return new UserModel(
                userRequestDTO.id(),
                userRequestDTO.name(),
                userRequestDTO.email(),
                userRequestDTO.imageURL(),
                userRoleType);
    }

    public static UserEntity fromResultSetToUserEntity(ResultSet resultSet, int rowNumber) throws SQLException {
        UserRoleType userRoleType = getRoleTypeFromStringOrDefault(resultSet.getString("role"));

        return new UserEntity(
                resultSet.getString("id"),
                resultSet.getString("name"),
                resultSet.getString("email"),
                resultSet.getString("image_url"),
                userRoleType
        );
    }

    private static UserRoleType getRoleTypeFromStringOrDefault(String roleType) {
        UserRoleType userRoleType = UserRoleType.CLIENT;

        if (EnumSet.allOf(UserRoleType.class).stream().noneMatch(role -> role.name().equals(roleType))) {
            logger.error("Invalid role: {}, by default is used {}", roleType, userRoleType.name());
            return userRoleType;
        }

        userRoleType = UserRoleType.valueOf(roleType);
        return userRoleType;
    }

    public static UserEntity fromUserModelToUserEntity(UserModel userModel) {
        return new UserEntity(
                userModel.getId(),
                userModel.getName(),
                userModel.getEmail(),
                userModel.getImageURL(),
                userModel.getRole()
        );
    }

    public static UserModel fromUserEntityToUserModel(UserEntity userEntity) {
        return new UserModel(
                userEntity.getId(),
                userEntity.getName(),
                userEntity.getEmail(),
                userEntity.getImageURL(),
                userEntity.getRole());
    }

    public static UserResponseDTO fromUserModelToUserResponseDTO(UserModel userModel) {
        return new UserResponseDTO(
                userModel.getId(),
                userModel.getName(),
                userModel.getEmail(),
                userModel.getImageURL(),
                userModel.getRole().name()
        );
    }
}
