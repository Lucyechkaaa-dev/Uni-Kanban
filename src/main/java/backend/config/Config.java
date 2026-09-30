package backend.config;

import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Application and database configuration model.
 */
@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Config{

	String dbUrl;
	String dbUser;

	@ToString.Exclude
	String dbPass;
	String dbSchema;
	boolean dev;

	public Config(boolean dev){
		this.dev = dev;
	}

	@ToString.Include(name = "dbPass")
	private String maskedPass(){
		return "[HIDDEN]";
	}
}
