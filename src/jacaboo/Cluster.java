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

import java.util.Collections;
import java.util.Set;

import toools.thread.OneElementOneThreadProcessing;

public class Cluster<N extends SSHNode>
{
	private final NodeSet<N> nodes = new NodeSet<N>();

	protected Cluster(Set<N> nodeSet)
	{
		nodes.addAll(nodeSet);
	}

	public Set<N> getNodes()
	{
		return Collections.unmodifiableSet(nodes);
	}

	public synchronized void discard(N n, Throwable reason)
	{
		if (reason.getMessage() == null)
		{
			reason.printStackTrace();
		}
		else
		{
			System.err.println("Discarding node " + n + ": " + reason.getMessage());
			// reason.printStackTrace();
		}

		// System.out.println("Discarding node " + n);
		nodes.remove(n);

		if (nodes.isEmpty())
			throw new IllegalStateException("no node remain in cluster");
	}

	public void add(N n)
	{
		nodes.add(n);
	}

	public boolean containsOnlyLocalhost()
	{
		boolean allLocalHost = true;

		for (N node : nodes)
		{
			if ( ! node.isLocalhost())
			{
				allLocalHost = false;
				break;
			}
		}

		return allLocalHost;
	}

	public void kill_9_1()
	{
		new OneElementOneThreadProcessing<N>(getNodes())
		{

			@Override
			protected void process(N n) throws Throwable
			{
				n.kill_9_1();
			}
		};
	}

}
