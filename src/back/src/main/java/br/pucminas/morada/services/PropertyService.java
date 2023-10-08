package br.pucminas.morada.services;

import br.pucminas.morada.models.property.Property;
import br.pucminas.morada.models.user.User;
import br.pucminas.morada.models.user.UserRole;
import br.pucminas.morada.repositories.PropertyRepository;
import br.pucminas.morada.security.UserSpringSecurity;
import br.pucminas.morada.services.exceptions.AuthorizationException;
import br.pucminas.morada.services.exceptions.GenericException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PropertyService {

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private UserService userService;

    @Transactional
    public Property create(Property property) {

        User user = this.userService.findById(UserService.authenticated().getId());

        property.setId(null);
        property.setUser(user);

        return this.propertyRepository.save(property);

    }

    @Transactional
    public Property update(Property property) {

        Property propertyFound = this.findById(property.getId());
        propertyFound.setStatus(property.getStatus());

        return this.propertyRepository.save(propertyFound);

    }

    public Property findById(Long id) {

        Optional<Property> optionalProperty = this.propertyRepository.findById(id);
        UserSpringSecurity userSpringSecurity = UserService.authenticated();

        if (optionalProperty.isEmpty()) {

            if (userSpringSecurity.hasRole(UserRole.ADMIN)) {
                throw new GenericException(HttpStatus.NOT_FOUND, "Propriedade não encontrada.");
            }

            throw new AuthorizationException("Acesso negado.");

        } else {

            Property property = optionalProperty.get();

            if (!userSpringSecurity.hasRole(UserRole.ADMIN) && !property.getUser().getId().equals(userSpringSecurity.getId())) {
                throw new AuthorizationException("Acesso negado.");
            }

            return property;

        }

    }

    public List<Property> findAllByUser() {

        UserSpringSecurity userSpringSecurity = UserService.authenticated();
        return this.propertyRepository.findByUserId(userSpringSecurity.getId());

    }

}
