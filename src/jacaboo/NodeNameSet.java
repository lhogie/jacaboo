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
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import toools.collections.Collections;
import toools.collections.ListSet;
import toools.io.file.RegularFile;

@SuppressWarnings("serial")
public class NodeNameSet extends ListSet<String>
{
	public NodeNameSet()
	{

	}

	public NodeNameSet(String username, List<String> names)
	{
		this(names);
	}

	public NodeNameSet(List<String> names)
	{
		this.addAll(names);
	}

	public NodeNameSet(String... names)
	{
		for (String n : names)
		{
			add(n);
		}
	}

	public NodeNameSet replaceAll(String pattern, String replacement)
	{
		NodeNameSet newSet = new NodeNameSet();
		
		for (String name : this)
		{
			newSet.add(name.replaceAll(pattern, replacement));
		}
		
		return newSet;
	}

	public NodeNameSet minus(String... nodesToRemove)
	{
		for (String s : nodesToRemove)
		{
			remove(s);
		}

		return this;
	}

	public static NodeNameSet loadFile(RegularFile f) throws IOException
	{
		NodeNameSet s = new NodeNameSet();

		for (String line : new String(f.getContent()).split("\n"))
		{
			line = line.trim();

			if ( ! line.isEmpty())
			{
				s.add(line);
			}

		}

		return s;
	}

	public NodeNameSet subset(int n, Random random)
	{
		NodeNameSet r = new NodeNameSet();

		if (random == null)
		{
			Iterator<String> i = iterator();

			while (r.size() < n)
			{
				r.add(i.next());
			}
		}
		else
		{
			while (r.size() < n)
			{
				r.add(Collections.pickRandomObject(this, random));
			}
		}

		return r;
	}
}
