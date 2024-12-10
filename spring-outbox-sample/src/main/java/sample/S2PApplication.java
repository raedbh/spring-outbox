package sample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import io.github.raedbh.spring.outbox.jpa.OutboxJpaRepositoryFactoryBean;

/**
 * @author Raed Ben Hamouda
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableJpaRepositories(repositoryFactoryBeanClass = OutboxJpaRepositoryFactoryBean.class)
public class S2PApplication {

		public static void main(String[] args) {
				SpringApplication.run(S2PApplication.class, args);
		}
}
