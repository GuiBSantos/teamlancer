package sharktank.teamlancer.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sharktank.teamlancer.domain.user.dto.UserResponse;
import sharktank.teamlancer.domain.user.dto.UserUpdateRequest;
import sharktank.teamlancer.domain.user.entity.UserEntity;
import sharktank.teamlancer.domain.user.repository.UserRepository;
import sharktank.teamlancer.shared.exception.BusinessException;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public Optional<UserResponse> findByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(this::mapToUserResponse);
    }

    public Optional<UserResponse> updateUser(String email, UserUpdateRequest request) {
        return userRepository.findByEmail(email)
                .map(user -> {
                    if (request.getName() != null) {
                        user.setName(request.getName());
                    }
                    if (request.getLocation() != null) {
                        user.setLocation(request.getLocation());
                    }
                    if (request.getBio() != null) {
                        user.setBio(request.getBio());
                    }
                    if (request.getAvatarColor() != null) {
                        user.setAvatarColor(request.getAvatarColor());
                    }
                    return userRepository.save(user);
                })
                .map(this::mapToUserResponse);
    }

    private UserResponse mapToUserResponse(UserEntity user) {
        return UserResponse.builder()
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .location(user.getLocation())
                .bio(user.getBio())
                .avatarColor(user.getAvatarColor())
                .build();
    }
}
