package launcher;

import backend.config.Config;
import backend.config.Env;
import backend.ui.ConsoleMenu;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;

@Log4j2
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CompileLauncher{

	public static void main(String[] args){
		launch(false, args);
	}

	public static void launch(boolean dev, String[] args){
		Env.setup(dev);
		printBanner(Env.getConfig());
		SpringApplication.run(Main.class, args);
		ConsoleMenu.init();
	}

	public static void printBanner(Config cfg){
		String maskedPass = (cfg.getDbPass() != null && !cfg.getDbPass().isEmpty())
				? "*".repeat(Math.min(cfg.getDbPass().length(), 8))
				: "(not set)";

		System.out.printf("""
						----------------- Launcher -----------------
						DB url ------ %s
						DB user ----- %s
						DB pass ----- %s (hidden)
						--------------------------------------------
						""",
				cfg.getDbUrl(),
				cfg.getDbUser(),
				maskedPass
		);
	}
}
