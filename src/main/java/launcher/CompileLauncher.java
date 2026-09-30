package launcher;

import backend.config.Config;
import backend.config.Env;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CompileLauncher{

	public static void main(String[] args){
		launch(false);
	}

	public static void launch(boolean dev){
		Env.setup(dev);
		printBanner(Env.getConfig());
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
