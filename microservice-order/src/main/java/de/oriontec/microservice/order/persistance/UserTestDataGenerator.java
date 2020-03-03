package de.oriontec.microservice.order.persistance;

import de.oriontec.microservice.order.repository.UserRepository;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserTestDataGenerator {

	private final UserRepository userRepository;

	@Autowired
	public UserTestDataGenerator(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@PostConstruct
	public void generateTestData() {

		User user =User.builder().
				fullname("ADMIN").username("admin").
				email("admin@mail.me").password("b8f57d6d6ec0a60dfe2e20182d4615b12e321cad9e2979e0b9f81e0d6eda78ad9b6dcfe53e4e22d1").
				activated(true).phone("'+123456789'").type(Authorities.ROLE_ADMIN).build();

		try {
			userRepository.save(user);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
