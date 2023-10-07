package br.pucminas.morada.services;

import br.pucminas.morada.models.User;
import br.pucminas.morada.models.UserVerification;
import br.pucminas.morada.models.enums.UserRole;
import br.pucminas.morada.repositories.UserVerificationRepository;
import br.pucminas.morada.security.UserSpringSecurity;
import br.pucminas.morada.services.exceptions.AuthorizationException;
import br.pucminas.morada.services.exceptions.GenericException;
import org.hibernate.ObjectNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserVerificationService {

    @Autowired
    private UserVerificationRepository userVerificationRepository;

    @Autowired
    private UserService userService;

    @Transactional
    public UserVerification create(UserVerification userVerification) {

        User user = this.userService.findById(UserService.authenticated().getId());

        userVerification.setId(null);
        userVerification.setUser(user);

        return this.userVerificationRepository.save(userVerification);
    }

    @Transactional
    public UserVerification update(UserVerification userVerification) {

        UserVerification userVerificationFound = this.findById(userVerification.getId());

        userVerificationFound.setStatus(userVerification.getStatus());

        return this.userVerificationRepository.save(userVerification);

    }

    public UserVerification findById(Long id) {

        Optional<UserVerification> optionalUserVerification = this.userVerificationRepository.findById(id);
        UserSpringSecurity userSpringSecurity = UserService.authenticated();

        if (optionalUserVerification.isEmpty()) {

            if (userSpringSecurity.hasRole(UserRole.ADMIN)) {
                throw new GenericException(HttpStatus.NOT_FOUND, "Verificação de usuário não encontrada.");
            }

            throw new AuthorizationException("Acesso negado.");

        } else {

            UserVerification userVerification = optionalUserVerification.get();

            if (!userSpringSecurity.hasRole(UserRole.ADMIN) && !userVerification.getUser().getId().equals(userSpringSecurity.getId())) {
                throw new AuthorizationException("Acesso negado.");
            }

            return userVerification;

        }

    }

    public List<UserVerification> findAllByUser() {

        UserSpringSecurity userSpringSecurity = UserService.authenticated();
        return this.userVerificationRepository.findByUserId(userSpringSecurity.getId());

    }

}
