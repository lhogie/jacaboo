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

import toools.text.TextUtilities;
import toools.thread.Threads;

public abstract class ResourceManager
{

	private final SSHNode frontal;

	public ResourceManager(SSHNode frontal)
	{
		this.frontal = frontal;
	}

	public SSHNode getFrontal()
	{
		return frontal;
	}

	protected void runWarningThread(final int durationS)
	{
		new Thread(new Runnable()
		{

			@Override
			public void run()
			{
				Threads.sleepMs(durationS * 1000);
				TextUtilities
						.box("Warning, your reservation just expires now! Your process may be killed at any time, resulting in unpredictable behavior of your application");
			}
		}).start();
	}

	public NodeNameSet bookNodes(int nbNode, int durationS)
	{
		return bookNodes(nbNode, 1, durationS);
	}

	public abstract NodeNameSet bookNodes(int nbNode, int ppn, int durationS, String... properties);

}
