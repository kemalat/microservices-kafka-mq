package de.oriontec.microservice.order.repository;

import de.oriontec.microservice.order.persistance.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

  @Query("SELECT u FROM User u WHERE LOWER(u.username) = LOWER(:username)")
  User findByUsernameCaseInsensitive(@Param("username") String username);

  @Query
  User findByEmail(String email);

  @Query
  User findByEmailAndActivationKey(String email, String activationKey);

  @Query
  User findByEmailAndResetPasswordKey(String email, String resetPasswordKey);

}
