package de.oriontec.microservice.order.service;


import de.oriontec.microservice.order.exception.UserNotActivatedException;
import de.oriontec.microservice.order.persistance.User;
import de.oriontec.microservice.order.repository.UserRepository;
import java.util.ArrayList;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@Component("userDetailsService")
public class UserDetailService implements org.springframework.security.core.userdetails.UserDetailsService {

  private final Logger logger = LoggerFactory.getLogger(UserDetailService.class);

  @Autowired
  private UserRepository engineUserRepository;

  @Override
  @Transactional
  public UserDetails loadUserByUsername(final String login) {

    logger.debug("Authenticating {}", login);
    String lowercaseLogin = login.toLowerCase();

    User userFromDatabase;
    if (lowercaseLogin.contains("@")) {
      userFromDatabase = engineUserRepository.findByEmail(lowercaseLogin);
    } else {
      userFromDatabase = engineUserRepository.findByUsernameCaseInsensitive(lowercaseLogin);
    }

    if (userFromDatabase == null) {
      throw new UsernameNotFoundException("ServiceUser " + lowercaseLogin + " was not found in the database");
    } else if (!userFromDatabase.isActivated()) {
      throw new UserNotActivatedException("ServiceUser " + lowercaseLogin + " is not activated");
    }

    Collection<GrantedAuthority> grantedAuthorities = new ArrayList<>();

    GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(userFromDatabase.getType().toString());
    grantedAuthorities.add(grantedAuthority);

    return new org.springframework.security.core.userdetails.User(userFromDatabase.getUsername(),
        userFromDatabase.getPassword(), grantedAuthorities);

  }

}
