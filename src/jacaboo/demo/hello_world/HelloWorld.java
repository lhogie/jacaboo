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

package jacaboo.demo.hello_world;

import java.util.List;

import jacaboo.RemoteMain;

public class HelloWorld extends RemoteMain
{
	@Override
	protected void main(List<String> argList)
	{
		for (int i = 0;; ++i)
		{
			System.out.println("Hello world! " + i + " on stdout ");
			System.err.println("Hello world! " + i + " on stderr");
		//	Threads.sleepMs((long) (Math.random() * 1000));

			if (Math.random() < 0.05)
				throw new IllegalStateException("some random error");
		}
	}

	@Override
	protected void stop()
	{
	}
}