package backend.utils;

import backend.config.Config;
import backend.config.Env;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

@Log4j2
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HibernateFactory{

	private static volatile SessionFactory sessionFactory;

	public static SessionFactory getSessionFactory(){
		if(sessionFactory == null){
			synchronized(HibernateFactory.class){
				if(sessionFactory == null){
					try{
						Configuration configuration = new Configuration().configure("hibernate.cfg.xml");

						Config cfg = Env.getConfig();
						String schema = null;
						if(cfg != null && cfg.getDbSchema() != null && !cfg.getDbSchema().isBlank()){
							schema = cfg.getDbSchema();
						}
						else if(System.getenv("DB_SCHEMA") != null && !System.getenv("DB_SCHEMA").isBlank()){
							schema = System.getenv("DB_SCHEMA");
						}

						if(cfg != null && cfg.getDbUrl() != null && !cfg.getDbUrl().isBlank()){
							String url = cfg.getDbUrl();
							if(schema == null && url.contains("currentSchema=")){
								int start = url.indexOf("currentSchema=") + "currentSchema=".length();
								int end = url.indexOf('&', start);
								schema = end != -1 ? url.substring(start, end) : url.substring(start);
							}

							if(schema != null && !schema.isBlank()){
								String cleanSchema = schema.trim().replace("%22", "").replace("\"", "");
								if(!url.contains("currentSchema=") && !url.contains("searchpath=")){
									url = url.contains("?") ? url + "&currentSchema=%22" + cleanSchema + "%22" : url + "?currentSchema=%22" + cleanSchema + "%22";
								}
								configuration.setProperty("hibernate.default_schema", "\"" + cleanSchema + "\"");
							}

							configuration.setProperty("hibernate.connection.url", url);
							configuration.setProperty("hibernate.connection.username", cfg.getDbUser());
							configuration.setProperty("hibernate.connection.password", cfg.getDbPass());
						}
						else if(schema != null && !schema.isBlank()){
							String cleanSchema = schema.trim().replace("%22", "").replace("\"", "");
							configuration.setProperty("hibernate.default_schema", "\"" + cleanSchema + "\"");
						}

						String defSchema = configuration.getProperty("hibernate.default_schema");
						if(defSchema != null && defSchema.startsWith("${") && defSchema.endsWith("}")){
							configuration.getProperties().remove("hibernate.default_schema");
						}

						sessionFactory = configuration.buildSessionFactory();
						log.info("Hibernate SessionFactory initialized successfully from hibernate.cfg.xml");
					}
					catch(Exception e){
						log.error("Failed to initialize Hibernate SessionFactory", e);
						throw new ExceptionInInitializerError(e);
					}
				}
			}
		}
		return sessionFactory;
	}

	public static void shutdown(){
		if(sessionFactory != null && !sessionFactory.isClosed()){
			sessionFactory.close();
			log.info("Hibernate SessionFactory closed");
		}
	}
}
