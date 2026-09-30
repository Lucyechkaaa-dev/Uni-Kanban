package launcher;

import lombok.NoArgsConstructor;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"launcher", "controller", "backend"})
@NoArgsConstructor
public class Main{

	public static void main(String[] args){
		CompileLauncher.launch(false, args);
	}
}