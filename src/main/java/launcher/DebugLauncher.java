package launcher;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DebugLauncher{

	public static void main(String[] args){
		CompileLauncher.launch(true);
		System.out.println("Debug mode enabled");
	}
}
