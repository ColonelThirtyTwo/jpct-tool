
package net.parrill.jpcttools;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.threed.jpct.Animation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author parri310623
 */

@Parameters(commandDescription = "Creates an array of animations")
public class AnimArgs
{
	public static final Map<String,Integer> interpolation_modes = new HashMap<String,Integer>();
	public static final Map<String,Integer> clamping_modes = new HashMap<String,Integer>();
	static
	{
		interpolation_modes.put("keyframe", Animation.KEYFRAMESONLY);
		interpolation_modes.put("linear", Animation.LINEAR);
		clamping_modes.put("clamp", Animation.USE_CLAMPING);
		clamping_modes.put("wrap", Animation.USE_WRAPPING);
	}
	
	@Parameter(description="InFolder")
	public List<String> in = null;
	
	@Parameter(names = {"--ext"}, description="File extension of the models")
	public String extension = "3ds";
	
	@Parameter(names = {"--interpolation"}, description="Interpolation method. [keyframe, linear]")
	public String interpolation = "linear";
	
	@Parameter(names = {"--clamping"}, description="Clamping mode. [clamp, wrap]")
	public String clamping = "wrap";
	
	@Parameter(names = {"--out"}, description="Output File")
	public String out = null;
}
