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

import java.io.IOException;

import jacaboo.JavaCluster;
import jacaboo.JavaNode;
import jacaboo.NodeSet;
import jacaboo.RemoteMain;

public class Demo
{
	public static void main(String[] args) throws IOException
	{
		NodeSet<JavaNode> ns = new NodeSet<JavaNode>();

		ns.add(new JavaNode("musclotte"));
		ns.add(new JavaNode("srv-coati"));

		JavaCluster<JavaNode> a = new JavaCluster<JavaNode>(null, ns)
		{
			@Override
			public Class<? extends RemoteMain> getMainClass(JavaNode n)
			{
				return HelloWorld.class;
			}
		};

		a.start();
	}
}
