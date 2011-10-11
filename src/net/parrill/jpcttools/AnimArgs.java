
package net.parrill.jpcttools;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import java.util.List;

/**
 *
 * @author parri310623
 */

@Parameters(commandDescription = "Creates an array of animations")
public class AnimArgs
{
	@Parameter(description="InFolder")
	public List<String> in = null;
	
	@Parameter(names = {"-ext"}, description="File extension of the models")
	public String extension = "3ds";
	
	@Parameter(names = {"-out"}, description="Output File")
	public String out = null;
}
