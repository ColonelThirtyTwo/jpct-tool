
package net.parrill.jpcttools;

import com.threed.jpct.DeSerializer;
import com.threed.jpct.Loader;
import com.threed.jpct.Object3D;
import com.threed.jpct.Texture;
import com.threed.jpct.TextureManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import com.beust.jcommander.JCommander;
import com.threed.jpct.Animation;
import com.threed.jpct.Mesh;
import com.threed.jpct.OcTree;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 *
 * @author parri310623
 */
public class JpctTools
{
	public static void main(String[] args) throws IOException
	{
		JCommander jc = new JCommander();
		
		final CompileArgs compileargs = new CompileArgs();
		final AnimArgs animargs = new AnimArgs();
		
		jc.addCommand("compile", compileargs);
		jc.addCommand("animation", animargs);
		
		jc.parse(args);
		
		if("compile".equals(jc.getParsedCommand()))
		{
			for(String path : compileargs.files)
			{
				System.out.println("--- Loading "+path);
				String out = getNoExtension(path).concat(".jpct");
				try
				{
					Object3D[] objs = loadFile(path);
					System.out.println("--- Loaded "+objs.length+" objects.");
					Animation anim = null;
					if(compileargs.animfile != null)
					{
						try
						{
							anim = (Animation)(new ObjectInputStream(new FileInputStream(compileargs.animfile))).readObject();
						}
						catch (ClassNotFoundException ex)
						{
							throw new RuntimeException(ex);
						}

					}
					
					System.out.println("--- Compiling:");
					for(int i=0; i<objs.length; i++)
					{
						Object3D obj = objs[i];
						System.out.println("--  Compiling object "+i+": "+obj.getName());
						obj.calcBoundingBox();
						if(compileargs.octree)
						{
							System.out.println("-   Generating octree.");
							OcTree tree;
							if(compileargs.maxdepth > 0)
								tree = new OcTree(obj,compileargs.maxpolys,compileargs.maxdepth,OcTree.MODE_OPTIMIZED);
							else
								tree = new OcTree(obj,compileargs.maxpolys,OcTree.MODE_OPTIMIZED);
							
							obj.setOcTree(tree);
							System.out.println("-   Octree generated.");
						}
						if(anim != null)
						{
							System.out.println("-   Setting animation.");
							obj.setAnimationSequence(anim);
						}
						
						obj.build();
						obj.compileAndStrip();
						
						System.out.println("--  Done compiling object.");
					}

					System.out.println("--- Objects compiled, saving.");
					FileOutputStream outf = new FileOutputStream(new File(out));
					DeSerializer serial = new DeSerializer();
					serial.serializeArray(objs, outf, true);
					System.out.println("--- Compiled "+path+"\n");
				}
				catch(IOException ex)
				{
					throw new RuntimeException("couldnt read file", ex);
				}
			}
		}
		else if("animation".equals(jc.getParsedCommand()))
		{
			File dir = new File(animargs.in == null ? "./" : animargs.in.get(0));
			if(!dir.exists() || !dir.isDirectory())
			{
				System.err.println("-!  "+dir.getName()+" does not exist or is not a directory");
				return;
			}
			
			System.out.println("--- Creating animations for "+dir.getName());
			
			FileFilter subseqfilter = new FileFilter()
			{
				public boolean accept(File f)
				{
					boolean accept = f.isDirectory() && f.getName().matches("^\\d+_.+$");
					if(!accept)
						System.out.println("-!  Ignoring "+f.getName());
					return accept;
				}
			};
			
			FilenameFilter modelfilter = new FilenameFilter()
			{
				public boolean accept(File dir, String f)
				{
					return getExtension(f).equals(animargs.extension);
				}
			};
			
			Comparator<Object3D> objcomparator = new Comparator<Object3D>()
			{

				@Override
				public int compare(Object3D o1, Object3D o2)
				{
					return o1.getName().compareTo(o2.getName());
				}
				
			};
			
			File outputFile = new File(animargs.out == null ? dir.getName()+".jpctanim" : animargs.out);
			File[] subanimFiles = dir.listFiles(subseqfilter);
			Arrays.sort(subanimFiles);
			int framecount = 0;
			Map<Integer,Mesh[]> subanims = new TreeMap<Integer,Mesh[]>();
			Map<Integer,String> subanimnames = new HashMap<Integer,String>();
			for(int j=0; j<subanimFiles.length; j++)
			{
				File subseq = subanimFiles[j];
				String name = subseq.getName();
				System.out.println("--  Creating subsequence "+name);

				File[] models = subseq.listFiles(modelfilter);
				Arrays.sort(models);
				Mesh[] meshes;
				
				String[] models_str = subseq.list(modelfilter);
				Arrays.sort(models_str);
				if(Arrays.binarySearch(models_str, "frames."+animargs.extension) >= 0)
				{
					
					Object3D[] objs = loadFile(subseq.getPath()+File.separatorChar+"frames."+animargs.extension);
					Arrays.sort(objs, objcomparator);
					meshes = new Mesh[objs.length];
					for(int k=0; k<objs.length; k++)
					{
						framecount++;
						Object3D obj = objs[k];
						obj.build();

						Mesh m = obj.getMesh();
						m.setSerializeMethod(Mesh.SERIALIZE_VERTICES_ONLY);
						System.out.println("-   Keyframe "+k+" has "+m.getVertexCount()+" verticies ("+m.getUniqueVertexCount()+" unique).");
						m.strip();
						m.compress();
						meshes[k] = m;
					}
				}
				else
				{
					meshes = new Mesh[models.length];
					for(int k=0; k<models.length; k++)
					{
						framecount++;
						Object3D obj = loadFile(models[k].getPath())[0];
						obj.build();

						Mesh m = obj.getMesh();
						m.setSerializeMethod(Mesh.SERIALIZE_VERTICES_ONLY);
						System.out.println("-   Keyframe "+k+" has "+m.getVertexCount()+" verticies ("+m.getUniqueVertexCount()+" unique).");
						m.strip();
						m.compress();
						meshes[k] = m;
					}
				}
				subanims.put(j+1, meshes);
				subanimnames.put(j+1, name);
				System.out.println("--  End subsequence");
			}
			Animation anim = new Animation(framecount);
			for(Entry<Integer,Mesh[]> en : subanims.entrySet())
			{
				int i = anim.createSubSequence(subanimnames.get(en.getKey()));
				if(i != en.getKey()) System.err.println("-!  Subanimation "+en.getKey()+" was given sequence index "+i);
				Mesh[] meshes = en.getValue();
				for(int j=0; j<meshes.length; j++)
					anim.addKeyFrame(meshes[j]);
			}
			anim.strip();
			
			outputFile.delete();
			outputFile.createNewFile();
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(outputFile));
			out.writeObject(anim);
			out.close();
			System.out.println("--- Animations written to "+outputFile.getPath());
		}
		else
		{
			jc.usage();
		}
		
	}
	
	public static Object3D[] loadFile(String path) throws IOException
	{
		String ext = getExtension(path);
		String folder = getFolder(path);
		if(ext.equals("3ds"))
		{
			//System.out.println("Loading 3ds textures");
			String[] textures = Loader.readTextureNames3DS(path);
			for(String s : textures)
			{
				if(TextureManager.getInstance().containsTexture(s)) continue;
				File f = new File(folder.concat(s).concat(".png"));
				if(!f.exists()) { System.err.println("-!  Didn't find texture "+s+".png"); continue; }
				Texture t = new Texture(new FileInputStream(f));
				TextureManager.getInstance().addTexture(s, t);
			}
			System.out.println("Loading 3ds model: "+path);
			return Loader.load3DS(path, 1f);
		}
		else if(ext.equals("obj"))
			return Loader.loadOBJ(path, getNoExtension(path).concat(".mtl"), 1f);
		else
			throw new IllegalArgumentException("unknown model type: "+path);
	}
	
	public static String concatPath(String[] args, int start)
	{
		StringBuilder b = new StringBuilder();
		for(int i=start; i<args.length; i++)
			b.append(args[i]);
		return b.toString();
	}
	public static String getFolder(String path)
	{
		return path.substring(0, path.lastIndexOf(File.pathSeparatorChar)+1);
	}
	public static String getExtension(String path)
	{
		return path.substring(path.lastIndexOf('.')+1, path.length()).toLowerCase();
	}
	public static String getNoExtension(String path)
	{
		return path.substring(0, path.lastIndexOf('.'));
	}
	
	public static String Obj3DtoString(int i, Object3D obj)
	{
		StringBuilder s = new StringBuilder();
		s.append("Object ");
		s.append(i);
		s.append(": ");
		s.append(obj.getName());
		s.append(", ");
		s.append(obj.getMesh().getVertexCount());
		s.append(" verticies, ");
		s.append(obj.getMesh().getTriangleCount());
		s.append(" triangles. ");
		if(obj.getOcTree() == null) s.append("Has octree. ");
		
		return s.toString();
	}
}
