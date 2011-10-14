
package net.parrill.jpcttools;

/**
 *
 * @author parri310623
 */

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import java.util.List;

@Parameters(commandDescription = "Compiles and serializes an object")
public class CompileArgs
{
	@Parameter(names = {"--octree"}, description = "Create octree")
	public boolean octree = false;
	
	@Parameter(names = {"--ocmaxpoly"}, description = "Octree maximum polygons per node")
	public int maxpolys = 50;
	
	@Parameter(names = {"--ocmaxdepth"}, description = "Octree max depth. <= 0 for no maximum")
	public int maxdepth = 0;

	@Parameter(names = "--animation", description = "The animation file")
	public String animfile = null;
	
	@Parameter(description = "The list of files to compile")
	public List<String> files;
}
