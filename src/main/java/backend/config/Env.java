package backend.config;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Environment configuration loader and accessor.
 */
@Log4j2
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Env{

	private static final String KEY_DB_URL = "DB_URL";
	private static final String KEY_DB_USER = "DB_USER";
	private static final String KEY_DB_PASS = "DB_PASS";
	private static final String KEY_DB_SCHEMA = "DB_SCHEMA";

	@Getter
	private static Config config = new Config();

	@Getter
	private static boolean dev = false;

	public static Config load(Map<String, String> envSource, boolean devMode){
		Objects.requireNonNull(envSource, "Environment map cannot be null");

		String dbUrl = envSource.get(KEY_DB_URL);
		String dbUser = envSource.get(KEY_DB_USER);
		String dbPass = envSource.get(KEY_DB_PASS);
		String dbSchema = envSource.get(KEY_DB_SCHEMA);

		List<String> missing = new ArrayList<>();
		if(dbUrl == null || dbUrl.isBlank()){
			missing.add(KEY_DB_URL);
		}
		if(dbUser == null || dbUser.isBlank()){
			missing.add(KEY_DB_USER);
		}
		if(dbPass == null){
			missing.add(KEY_DB_PASS);
		}

		if(!missing.isEmpty()){
			String errorMsg = "Missing required environment variable(s): " + String.join(", ", missing);
			log.error(errorMsg);
			throw new IllegalStateException(errorMsg);
		}

		Env.dev = devMode;

		Config loadedConfig = Config.builder()
				.dbUrl(dbUrl.trim())
				.dbUser(dbUser.trim())
				.dbPass(dbPass.trim())
				.dbSchema(dbSchema != null && !dbSchema.isBlank() ? dbSchema.trim() : null)
				.dev(devMode)
				.build();

		if(devMode){
			log.info("Environment variables loaded successfully [ DB_URL={}, DB_USER={}, DB_SCHEMA={}, DB_PASS={} ]",
					loadedConfig.getDbUrl(), loadedConfig.getDbUser(), loadedConfig.getDbSchema(), loadedConfig.getDbPass());
		}
		else{
			log.info("Environment variables loaded successfully [ DB_URL={}, DB_USER={}, DB_PASS=[Hidden] ]",
					loadedConfig.getDbUrl(), loadedConfig.getDbUser());
		}

		config = loadedConfig;
		return loadedConfig;
	}

	public static Config load(boolean devMode){
		return load(System.getenv(), devMode);
	}

	public static Config load(){
		return load(System.getenv(), dev);
	}

	public static void setup(boolean devMode){
		load(devMode);
	}

	public static void setup(){
		load(false);
	}
}
