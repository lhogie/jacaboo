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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import toools.reflect.Clazz;

public class MainClassRunner
{
	static RemoteMain mainObject = null;

	public static void main(String[] args)
			throws ClassNotFoundException, NoSuchMethodException
	{
		final List<String> argList = new ArrayList<String>(Arrays.asList(args));

		String userMainClassName = argList.remove(0);
		Binaries.setApplicationName(argList.remove(0));
		@SuppressWarnings("unchecked")
		final Class<? extends RemoteMain> userMainClass = Clazz
				.findClassOrFail(userMainClassName);

		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					mainObject = userMainClass.newInstance();
					mainObject.main(argList);
				}
				catch (Throwable e)
				{
					if (e.getCause() == null)
					{
						e.printStackTrace();
					}
					else
					{
						e.getCause().printStackTrace();
					}
				}
			}
		}).start();

		try
		{
			System.in.read();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (Throwable t)
		{
			// client closes the SSH connection (the client program quit)
			// t.printStackTrace();
		}
		// System.err.println("MainClassRunner: stdin closed, stop and exit.");
		if (mainObject != null)
			mainObject.stop();
		try
		{
			Thread.sleep(1000);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		System.exit(0);
	}

}
