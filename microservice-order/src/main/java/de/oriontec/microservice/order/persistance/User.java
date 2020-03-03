package de.oriontec.microservice.order.persistance;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder(toBuilder = true)
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class User {

  @Id
  @Column(updatable = false, nullable = false)
  private String username;
  private String fullname;
  private String password;
  private String email;
  private String phone;
  private boolean activated;
  private String activationKey;
  private String resetPasswordKey;
  @Enumerated(EnumType.STRING)
  private Authorities type;

}
