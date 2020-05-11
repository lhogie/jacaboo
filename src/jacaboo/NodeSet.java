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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.HashSet;

public class NodeSet<N extends HardwareNode> extends HashSet<N> implements Externalizable
{
	static final long serialVersionUID = 0L;

	@Override
	public boolean remove(Object o)
	{
		if ( ! super.remove(o))
			throw new IllegalArgumentException(o + " is not in");

		return true;
	}

	@Override
	public boolean add(N n)
	{
		if (n == null)
			throw new IllegalArgumentException();

		if (contains(n))
			throw new IllegalArgumentException(n + " is already in: " + this);

		super.add(n);
		return true;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException
	{
		out.writeInt(size());

		for (N n : this)
		{
			out.writeObject(n);
		}
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
	{
		int size = in.readInt();

		for (int i = 0; i < size; ++i)
		{
			@SuppressWarnings("unchecked")
			N n = (N) in.readObject();
			assert n != null;
			add(n);
		}
	}

	public Collection<String> toNodeNames()
	{
		Collection<String> r = new HashSet<>();

		for (N n : this)
		{
			if (n instanceof SSHNode)
			{
				r.add(((SSHNode) n).getSSHName());
			}
			else
			{
				r.add(n.getInetAddress().getHostName());
			}
		}

		return r;
	}
}
