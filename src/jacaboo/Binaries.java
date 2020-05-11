/////////////////////////////////////////////////////////////////////////////////////////
// 
//                 Université de Nice Sophia-Antipolis  (UNS) - 
//                 Centre National de la Recherche Scientifique (CNRS)
//                 Copyright © 2015 UNS, CNRS All Rights Reserved.
// 
//     These computer program listings and specifications, herein, are
//     the property of Université de Nice Sophia-Antipolis and CNRS
//     shall not be reproduced or copied or used in whole or in part as
//     the basis for manufacture or sale of items without written permission.
//     For a license agreement, please contact:
//     <mailto: licensing@sattse.com> 
//
//
//
//     Author: Luc Hogie – Laboratoire I3S - luc.hogie@unice.fr
//
//////////////////////////////////////////////////////////////////////////////////////////


package jacaboo;

import java.io.File;
import java.io.IOException;

import toools.io.file.AbstractFile;
import toools.io.file.Directory;
import toools.io.file.RegularFile;
import toools.reflect.ClassContainer;
import toools.reflect.ClassPath;

public class Binaries
{
	private static String applicationName = Binaries.class.getPackage().getName();
	private static Directory applicationDir;
	private static Directory localBinariesDir;

	public static String getApplicationName()
	{
		if (applicationName == null)
			throw new IllegalStateException("you should define the application name");
		
		return applicationName;
	}
	
	public static void setApplicationName(String s)
	{
		applicationName = s;
	}

	private final static String getJarDirectoryName()
	{
		return "jars";
	}

	final static String jarDirectoryPathRelativeToHomedir()
	{
		return applicationName + "/" + getJarDirectoryName();
	}

	final static String jarDirectoryAbsolutePath()
	{
		return new Directory(localDir(), getJarDirectoryName()).getPath();
	}
	
	public final static Directory localDir()
	{
		if (applicationDir == null)
		{
			applicationDir = new Directory(Directory.getHomeDirectory(), applicationName);
		}

		return applicationDir;
	}

	final static Directory localBinariesDir()
	{
		if (localBinariesDir == null)
		{
			localBinariesDir = new Directory(localDir(), getJarDirectoryName());
		}

		return localBinariesDir;
	}

	static void ensureLocalSymLinksToClassPathEntriesAreProperlySet()
	{
		@SuppressWarnings("hiding")
		Directory localBinariesDir = localBinariesDir();
		
		if (localBinariesDir.exists())
		{
			if (!localBinariesDir.isEmpty())
			{
				for (AbstractFile f : localBinariesDir.getChildFiles())
				{
					if (!f.isSymbolicLink())
						throw new IllegalStateException("should be a symbolic link to a classpath entry: " + f.getPath());
					
					f.delete();
				}
			}

			if (!localBinariesDir.isEmpty())
				throw new IllegalStateException();
		}
		else
		{
			localBinariesDir.mkdirs();
		}

		for (ClassContainer e : ClassPath.retrieveSystemClassPath())
		{
			AbstractFile entryFile = e.getFile();
			String linkName = entryFile instanceof RegularFile ? entryFile.getName() : entryFile.getPath().replace(File.separatorChar, '_');
			RegularFile linkTarget = new RegularFile(localBinariesDir, linkName);

			if (!linkTarget.exists())
			{
				try
				{
					entryFile.createLink(linkTarget);
				}
				catch (IOException e1)
				{
					throw new IllegalStateException(e1);
				}
			}
		}
	}
}
