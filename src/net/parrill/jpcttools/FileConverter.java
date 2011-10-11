
package net.parrill.jpcttools;

import java.io.File;

/**
 *
 * @author parri310623
 */
import com.beust.jcommander.IStringConverter;
public class FileConverter implements IStringConverter<File>
{
	@Override
	public File convert(String value)
	{
		return new File(value);
	}
}

