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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import toools.extern.Proces;
import toools.text.TextUtilities;

public class SSHUtils
{
	public static final String sshCommandNameProperty = "biggrph.ssh.command";
	public static final String sshCommandNameDefault = "ssh";

	public static String getSSHCommandName()
	{
		return System.getProperty(sshCommandNameProperty, sshCommandNameDefault);
	}

	public static List<String> getSSHOptions()
	{
		return new ArrayList<String>(Arrays.asList(getSSHOptionsArray()));
	}

	public static List<String> getSSHOptions(int timeoutInSecond)
	{
		List<String> options = getSSHOptions();
		options.add("-o");
		options.add("ConnectTimeout=" + timeoutInSecond);
		return options;
	}

	/**
	 * SSH options that are always given when SSHing cluster nodes.
	 * 
	 * @return
	 */
	public static String[] getSSHOptionsArray()
	{
		String options[] = { "-o", "ForwardX11=no", "-o", "StrictHostKeyChecking=no",
				"-o", "BatchMode=yes" };
		return options;
	}


	public static String getSSHOptionsString(int timeoutInSecond)
	{
		String options = "";
		for (String option : getSSHOptions(timeoutInSecond))
		{
			options += option + " ";
		}
		return options;
	}

	public static List<String> execSh(SSHNode sshFrontal, int timeoutInSecond, SSHNode n,
			String shText)
	{
		if (sshFrontal == null)
		{
			return execSh(timeoutInSecond, n, shText);
		}
		else
		{
			List<String> args = getSSHOptions(timeoutInSecond);
			args.add(0, sshFrontal.getSSHName());
			args.add(getSSHCommandName());
			args.add(n.getSSHName());
			args.add("bash");
			args.add("--posix");
			byte[] r = Proces.exec(sshCommandNameDefault, shText.getBytes(),
					args.toArray(new String[0]));
			return r.length == 0 ? new ArrayList<String>()
					: TextUtilities.splitInLines(new String(r));
		}
	}

	public static List<String> execSh(int timeoutInSecond, SSHNode n, String shText)
	{
		List<String> args = getSSHOptions(timeoutInSecond);
		args.add(n.getSSHName());
		args.add("bash");
		args.add("--posix");
		byte[] r = Proces.exec(getSSHCommandName(), shText.getBytes(),
				args.toArray(new String[0]));
		return r.length == 0 ? new ArrayList<String>()
				: TextUtilities.splitInLines(new String(r));
	}

}
