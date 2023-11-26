package com.example.minishop.app.users;

import com.example.minishop.app.users.dtos.UserRequestDTO;
import com.example.minishop.app.users.dtos.UserResponseDTO;
import com.example.minishop.app.users.models.UserModel;
import com.example.minishop.base.BaseController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

import static com.example.minishop.app.users.UserMapper.fromUserModelToUserResponseDTO;

@RestController
@RequestMapping("/users")
public class UsersController extends BaseController {
    private final UsersService usersService;

    public UsersController(UsersService usersService) {
        this.usersService = usersService;
    }

    @PostMapping
    public ResponseEntity<String> createUser(@RequestBody UserRequestDTO userRequestDTO) {
        String userID = usersService.createUser(userRequestDTO);
        if (userID == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(userID);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable String id) {
        Optional<UserModel> userModel = usersService.getUserById(id);
        return userModel.map(model -> ResponseEntity.ok().body(fromUserModelToUserResponseDTO(model))).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        List<UserModel> userModels = usersService.getAllUsers();
        if (userModels == null) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }

        List<UserResponseDTO> userResponseDTOs = userModels.stream()
                .map(UserMapper::fromUserModelToUserResponseDTO)
                .toList();
        return ResponseEntity.ok().body(userResponseDTOs);
    }

    @PutMapping
    public ResponseEntity<String> updateUser(@RequestBody UserRequestDTO userRequestDTO) {
        String userID = usersService.updateUser(userRequestDTO);
        if (userID == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        return ResponseEntity.ok().body(userID);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        Boolean status = usersService.deleteUser(id);
        if (status == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        if (!status) {
            return ResponseEntity.status(HttpStatus.ACCEPTED).build();
        }

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}