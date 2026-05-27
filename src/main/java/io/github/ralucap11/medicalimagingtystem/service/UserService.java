package io.github.ralucap11.medicalimagingtystem.service;

import io.github.ralucap11.medicalimagingtystem.dto.UserRequestDTO;
import io.github.ralucap11.medicalimagingtystem.dto.UserResponseDTO;
import io.github.ralucap11.medicalimagingtystem.entity.User;
import io.github.ralucap11.medicalimagingtystem.exception.ResourceAlreadyExists;
import io.github.ralucap11.medicalimagingtystem.exception.ResourceNotFoundException;
import io.github.ralucap11.medicalimagingtystem.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserService
{
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder)
    {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


    public UserResponseDTO getUserById(Long id)
    {
        if(!userRepository.existsById(id))
        {
            throw new ResourceNotFoundException("user not found!");
        }

        User user = userRepository.getReferenceById(id);

        return entityToDTO(user);
    }

    public List<UserResponseDTO> getAllUsers()
    {
        return userRepository.findAll()
                .stream()
                .map(this::entityToDTO)
                .toList();

    }

    public UserResponseDTO createUser(UserRequestDTO request)
    {
        if(userRepository.existsByEmail(request.getEmail()))
        {
            throw new ResourceAlreadyExists("user already exists");
        }

        User user = userRepository.save(dtoToEntity(request));

        return entityToDTO(user);
    }

    public UserResponseDTO updateUser(Long id, UserRequestDTO request)
    {
      User user = userRepository.findById(id)
              .orElseThrow(() -> new ResourceNotFoundException("user not found"));

         user.setLastName(request.getLastName());
         user.setFirstName(request.getFirstName());
         user.setEmail(request.getEmail());
         user.setPassword(request.getPassword());
         user.setRole(request.getRole());

         User updatedUser = userRepository.save(user);

         return entityToDTO(updatedUser);

    }

    public void deleteUser(Long id)
    {
        if(!userRepository.existsById(id))
        {
            throw new ResourceNotFoundException("user not found");
        }

        userRepository.deleteById(id);
    }

    private UserResponseDTO entityToDTO(User user) {
        UserResponseDTO response = new UserResponseDTO();

        response.setId(user.getId());
        response.setLastName(user.getLastName());
        response.setFirstName(user.getFirstName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());

        return response;
    }

    private User dtoToEntity(UserRequestDTO request)
    {
        User user = new User();

        user.setLastName(request.getLastName());
        user.setFirstName(request.getFirstName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setRole(request.getRole());

        return user;
    }
}
