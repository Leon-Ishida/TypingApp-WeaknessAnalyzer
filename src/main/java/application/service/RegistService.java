package application.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import application.dto.RegistRequest;
import application.dto.RegistResponse;
import application.entity.UserEntity;
import application.exception.UserAlreadyExistsException;
import application.repository.UserRepository;

@Service
public class RegistService {
    final private UserRepository userRepository;
    final private BCryptPasswordEncoder encoder;

    public RegistService(UserRepository userRepository, BCryptPasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.encoder = encoder;
    }

    public RegistResponse regist(RegistRequest request) throws UserAlreadyExistsException {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new UserAlreadyExistsException("このメールアドレスは既に登録されています");
        }
        UserEntity entity = new UserEntity(request.userName(), request.email(), encoder.encode(request.password()));
        userRepository.save(entity);
        return new RegistResponse("success", "登録完了");
    }
}
