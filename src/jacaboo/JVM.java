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

public class JVM
{
	public static final JVM validatedJVM = new JVM("http://www.i3s.unice.fr/~hogie/octojus/jvm",
			"jre-8u45-linux-i586.tar.gz",
			"jre1.8.0_45/bin/java");
	
	// See http://blog.kdecherf.com/2012/04/12/oracle-i-download-your-jdk-by-eating-magic-cookies/ for details
	// about the download options.
	public static final JVM java8oracle
		= new JVM("http://download.oracle.com/otn-pub/java/jdk/8u51-b16",
					"jre-8u51-linux-x64.tar.gz",
					"--no-cookies  --header 'Cookie: oraclelicense=accept-securebackup-cookie;'",
					"jre1.8.0_51/bin/java");

	public static final JVM defaultJava = new JVM("java");
	
	public static final JVM currentJava = new JVM(System.getProperty("java.home") + "/bin/java");
	
	private final String command;
	private final String archiveName;
	private final String url;
	private final String downloadOptions;

	public JVM(String cmd)
	{
		this(null, null, cmd);
	}
	
	private JVM(String url, String filename, String cmd)
	{
		this(url, filename, "", cmd);
	}

	private JVM(String url, String filename, String downloadOpts, String cmd)
	{
		this.archiveName = filename;
		this.command = cmd;
		this.url = url;
		this.downloadOptions = downloadOpts;
	}

	public String getCommand()
	{
		return command;
	}

	public String getDownloadLink()
	{
		return url + "/" + archiveName;
	}
	
	public String getDownloadOptions()
	{
		return downloadOptions;
	}

	public String getArchiveName()
	{
		return archiveName;
	}
}
