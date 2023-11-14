package com.example.minishop.app.users;

import com.example.minishop.app.users.dtos.UserRequestDTO;
import com.example.minishop.app.users.entities.UserEntity;
import com.example.minishop.app.users.models.UserModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.example.minishop.app.users.UserMapper.*;


@Service
public class UsersService {
    private final Logger logger = LoggerFactory.getLogger(UsersService.class);

    private final UsersRepository usersRepository;

    public UsersService(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    public String createUser(UserRequestDTO userRequestDTO) {
        if (usersRepository.existsById(userRequestDTO.id()) == null) {
            return null;
        }

        if (usersRepository.existsById(userRequestDTO.id())) {
            logger.warn(String.format("User with id %s already exists", userRequestDTO.id()));
            return null;
        }

        UserModel userModel = fromUserRequestDTOtoUserModel(userRequestDTO);
        UserEntity userEntity = fromUserModelToUserEntity(userModel);

        return usersRepository.save(userEntity);
    }

    public UserModel getUserById(String id) {
        UserEntity userEntity = usersRepository.findById(id);

        if (userEntity == null) {
            return null;
        }

        return fromUserEntityToUserModel(userEntity);
    }

    public List<UserModel> getAllUsers() {
        List<UserEntity> userEntities = usersRepository.findAll();

        if (userEntities == null) {
            return null;
        }

        return userEntities.stream()
                .map(UserMapper::fromUserEntityToUserModel)
                .toList();
    }

    public String updateUser(UserRequestDTO userRequestDTO) {
        if (usersRepository.existsById(userRequestDTO.id()) == null) {
            return null;
        }

        if (!usersRepository.existsById(userRequestDTO.id())) {
            logger.warn(String.format("User with id %s doesn't exist", userRequestDTO.id()));
            return null;
        }

        UserModel userModel = fromUserRequestDTOtoUserModel(userRequestDTO);
        UserEntity userEntity = fromUserModelToUserEntity(userModel);

        return usersRepository.updateUser(userEntity);
    }

    public Boolean deleteUser(String id) {
        return usersRepository.deleteById(id);
    }
}
