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
import java.util.List;

public class WorkstationManager extends ResourceManager
{

	private final List<String> nodeNames = new ArrayList<>();

	public WorkstationManager(String... nodes)
	{
		super(null);

		for (String n : nodes)
		{
//			if (n.equals("localhost"))
//			{
//				nodeNames.add(HardwareNode.getLocalNode().getPublicInetAddress().getHostName());
//			}
//			else
//			{
				nodeNames.add(n);
//			}
		}
	}

	@Override
	public NodeNameSet bookNodes(int nbNode, int ppc, int durationS, String... args)
	{
		NodeNameSet r = new NodeNameSet();

		for (String n : nodeNames)
		{
			r.add(n);

			if (r.size() == nbNode)
				return r;
		}

		throw new IllegalArgumentException("not that many nodes in the manager");
	}

	@Override
	public String toString()
	{
		return "Workstations in LAN: " + nodeNames;
	}
}
