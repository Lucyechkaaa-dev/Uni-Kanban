package launcher;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Main {
	public static void main(String[] args) {
		CompileLauncher.main(args);
		SpringApplication.run(Main.class, args);
	}
}