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
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

import toools.io.IORuntimeException;

/**
 * Base class of the hierarchy of classes used to represent computing nodes of a
 * cluster. An HardwareNode has an {@link InetAddress} as its unique
 * characteristics.
 * 
 * @author Luc Hogie
 * @author Nicolas Chleq
 */
public class HardwareNode implements Comparable<HardwareNode>, Externalizable
{
	private InetAddress ipAddress;

	// cache for limited access to the DNS and network configuration of the host
	private int isLocalhost = - 1;

	/**
	 * For serialization. Do not use.
	 */
	public HardwareNode()
	{
	}

	protected HardwareNode(String nodeName) throws UnknownHostException
	{
		fromString(nodeName);
	}

	public boolean isReacheable(int timeoutMs)
	{
		try
		{
			return getInetAddress().isReachable(timeoutMs);
		}
		catch (IOException e)
		{
			return false;
		}
	}

	protected void fromString(String nodeSpec)
	{
		try
		{
			this.ipAddress = InetAddress.getByName(nodeSpec);
		}
		catch (UnknownHostException e)
		{
			throw new IORuntimeException(e);
		}
	}

	protected HardwareNode(InetAddress addr)
	{
		this.ipAddress = addr;
	}

	public InetAddress getInetAddress()
	{
		return ipAddress;
	}

	protected void setIpAddress(InetAddress ipAddress)
	{
		this.ipAddress = ipAddress;
	}

	@Override
	public String toString()
	{
		return getInetAddress().getHostName();
	}

	@Override
	public boolean equals(Object o)
	{
		return (o instanceof HardwareNode) && 
				getInetAddress().equals(((HardwareNode) o).getInetAddress());
	}

//	public boolean equals(HardwareNode o)
//	{
//		return o != null && o.getInetAddress().equals(getInetAddress());
//	}

	protected boolean isLocalNode()
	{
		return false;
	}

	/**
	 * Tests if the node represent the current computer host. The test is valid
	 * when
	 * <ul>
	 * <li>The InetAddress associated to the node is the address of the loopback
	 * interface (eg. 127.0.0.1)
	 * <li>The InetAddress of the node is the same as one the network interface
	 * of the host.
	 * </ul>
	 * 
	 * @return true if the node
	 */
	public final boolean isLocalhost()
	{
		// if not already computed
		if (isLocalhost == - 1)
		{
			try
			{
				boolean lb = getInetAddress().isLoopbackAddress();

				
				if (lb || NetworkInterface.getByInetAddress(getInetAddress()) != null)
				{
					isLocalhost = 1;
				}
				else
				{
					isLocalhost = 0;
				}
			}
			catch (SocketException e)
			{
				return false;
			}
		}

		return isLocalhost == 1;
	}

	@Override
	public int compareTo(HardwareNode o)
	{
		return getInetAddress().getHostName().compareTo(o.getInetAddress().getHostName());
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException
	{
		out.writeObject(ipAddress);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
	{
		ipAddress = (InetAddress) in.readObject();
	}

}
