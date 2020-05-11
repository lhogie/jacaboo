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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import toools.extern.Proces;
import toools.extern.ProcessOutput;
import toools.io.file.AbstractFile;
import toools.io.file.Directory;
import toools.io.file.RegularFile;
import toools.text.TextUtilities;
import toools.thread.OneElementOneThreadProcessing;

public class NASCluster<N extends SSHNode> extends Cluster<N>
{
	private Set<Set<N>> nasGroups;
	private N sshFrontal;

	protected NASCluster(N frontal, Set<N> nodeSet)
	{
		super(nodeSet);

		this.sshFrontal = frontal;
	}

	public void start()
	{
		if (nasGroups != null)
			throw new IllegalStateException("cluster is already started");

		if (containsOnlyLocalhost())
		{
			nasGroups = new HashSet<>();
			NodeSet<N> singleSet = new NodeSet<>();
			singleSet.addAll(getNodes());
			nasGroups.add(singleSet);
		}
		else
		{
			if (sshFrontal != null)
			{
				new OneElementOneThreadProcessing<N>(getNodes())
				{

					@Override
					protected void process(N n) throws Throwable
					{
						// if there's not frontal to reach the node, ping it
						if ( ! n.isReacheable(getTimeoutInSecond() * 1000))
						{
							throw new IOException(n + " can't be reached");
							// discard(n, new IOException(n +
							// " can't be reached"));
						}
					}
				};
			}

			discoverSharedFileSystems();
			System.out.println("Found NAS groups: " + nasGroups);
		}
	}

	@Override
	public synchronized void discard(N node, Throwable reason)
	{
		super.discard(node, reason);

		if (nasGroups != null)
		{
			Iterator<Set<N>> groupIterator = nasGroups.iterator();

			while (groupIterator.hasNext())
			{
				Set<N> group = (NodeSet<N>) groupIterator.next();

				if (group.contains(node))
				{
					group.remove(node);

					if (group.isEmpty())
					{
						groupIterator.remove();
					}
				}
			}
		}
	}

	public int getTimeoutInSecond()
	{
		return 15;
	}

	public Set<Set<N>> getNASGroups()
	{
		return nasGroups;
	}

	public Set<N> findNASGroupOf(HardwareNode n)
	{
		for (Set<N> g : nasGroups)
		{
			if (g.contains(n))
			{
				return g;
			}
		}

		return null;
	}

	public Set<N> pickOneNodeInEveryNASGroup(Random r)
	{
		Set<N> s = new HashSet<>();

		for (Set<N> g : nasGroups)
		{
			s.add(g.iterator().next());
		}

		return s;
	}

	private void discoverSharedFileSystems()
	{
		System.out.println("Fetching distributed file systems among " + getNodes());
		final String prefix = "octojus-nas-";

		new OneElementOneThreadProcessing<N>(getNodes())
		{

			@Override
			protected void process(N n) throws Throwable
			{
				String filename = prefix + n.getInetAddress().getHostName();

				if (n.isLocalhost())
				{
					RegularFile f = new RegularFile(Directory.getHomeDirectory(),
							filename);

					f.create();
				}
				else
				{
					// try
					{
						List<String> output = SSHUtils.execSh(sshFrontal,
								getTimeoutInSecond(), n,
								"touch " + filename + " && mkdir -p "
										+ Binaries.jarDirectoryPathRelativeToHomedir());

						if ( ! output.isEmpty())
						{
							System.err.println(output);
							throw new IllegalStateException(
									"problem with terminal, please make sure that it is clean");
						}
					}
					// catch (Throwable e)
					{
						// discard(n, e);
					}
				}
			}
		};

		final Map<N, Set<N>> sets = Collections.synchronizedMap(new HashMap<N, Set<N>>());

		new OneElementOneThreadProcessing<N>(getNodes())
		{
			@Override
			protected void process(N n) throws Throwable
			{
				{
					Set<N> s = new NodeSet<N>();

					for (String name : listNodeNames(n))
					{
						String hostname = name.substring(prefix.length());
						for (N node : getNodes())
						{
							if (hostname
									.compareTo(node.getInetAddress().getHostName()) == 0)
								s.add(node);
						}
					}

					sets.put(n, s);
				}
			}

			List<String> listNodeNames(HardwareNode n)
			{
				if (n.isLocalhost())
				{
					List<String> l = new ArrayList<>();

					for (RegularFile f : Directory.getHomeDirectory()
							.getChildRegularFilesMatching(prefix + ".*"))
					{
						l.add(f.getName());
					}

					return l;
				}
				else
				{
					return SSHUtils.execSh(sshFrontal, getTimeoutInSecond(), (SSHNode) n,
							"ls " + prefix + "*");
				}
			}
		};

		this.nasGroups = new HashSet<Set<N>>(sets.values());

		new OneElementOneThreadProcessing<N>(pickOneNodeInEveryNASGroup(new Random()))
		{
			@Override
			protected void process(N n) throws Throwable
			{
				if (n.isLocalhost())
				{
					for (RegularFile f : Directory.getHomeDirectory()
							.getChildRegularFilesMatching(prefix + ".*"))
					{
						f.delete();
					}
				}
				else
				{
					{
						SSHUtils.execSh(sshFrontal, getTimeoutInSecond(), n,
								"rm -f " + prefix + "*");
					}
				}
			}
		};
	}

	protected boolean containsLocalhost(Set<N> nodeGroup)
	{
		boolean containsLocalhost = false;
		for (N node : nodeGroup)
			if (node.isLocalhost())
			{
				containsLocalhost = true;
				break;
			}
		return containsLocalhost;
	}

	public Set<N> whoHasFile(final String filename)
	{
		final Set<N> nodes = new HashSet<>();

		new OneElementOneThreadProcessing<N>(getNodes())
		{

			@Override
			protected void process(N node) throws Throwable
			{
				ProcessOutput r = Proces.rawExec("ssh", node.getSSHName(),
						"test -f " + filename);

				if (r.getReturnCode() == 0)
				{
					synchronized (nodes)
					{
						nodes.add(node);
					}
				}
			}
		};

		return nodes;
	}

	/**
	 * This method can be called by any node in the cluster. It will try to find
	 * another node from which it can download the file. If no such node was
	 * found, the method returns null.
	 * 
	 * @param filename
	 * @return
	 */
	public N obtainFromPeers(String filename, Set<N> peers)
	{
		for (N peer : peers)
		{
			System.out.println("Trying to obtain the file " + filename + " from node "
					+ peer.getSSHName());

			try
			{
				ProcessOutput r = Proces.rawExec("rsync", peer.getSSHName() + ":filename",
						filename);

				if (r.getReturnCode() == 0)
				{
					// the file was got from that node
					return peer;
				}
			}
			catch (IOException e)
			{
				System.err.println("failed");
			}
		}

		return null;
	}

	public void deploy(final AbstractFile f)
	{
		deploy(f, f.getParent().getNameRelativeTo(Directory.getHomeDirectory()));
	}

	public void deploy(final AbstractFile f, final String remoteDirectory)
	{
		if (remoteDirectory == null)
			throw new IllegalArgumentException();

		if (remoteDirectory.isEmpty())
			throw new IllegalArgumentException();

		// adds the final '/' required by rsync
		final String rsyncDestDirectory = remoteDirectory.endsWith("/") ? remoteDirectory
				: remoteDirectory + "/";

		new OneElementOneThreadProcessing<Set<N>>(getNASGroups())
		{
			@Override
			protected void process(Set<N> nasGroup) throws Throwable
			{
				// Check if one node in the group is the localhost
				if (containsLocalhost(nasGroup))
				{
					Directory targetDirectory = new Directory("$HOME/" + remoteDirectory);

					AbstractFile to = f instanceof RegularFile
							? new RegularFile(targetDirectory, f.getName())
							: new Directory(targetDirectory, f.getName());

					if ( ! to.exists())
					{
						if ( ! to.getParent().exists())
						{
							to.getParent().mkdirs();
						}

						f.createLink(to);
					}
				}
				else
				{
					// try to deploy the binaries to one node in the set
					// if this node fails, another node is chosen
					// while (!nasGroup.isEmpty())
					{
						N node = nasGroup.iterator().next();
						{
							System.out.println(f.getName() + " => " + node + ":"
									+ rsyncDestDirectory + " of group " + nasGroup);

							SSHUtils.execSh(sshFrontal, getTimeoutInSecond(), node,
									"if ! test -d '" + rsyncDestDirectory
											+ "'; then mkdir -p " + rsyncDestDirectory
											+ "; fi");

							if (f instanceof Directory)
							{
								@SuppressWarnings("unused")
								String rsyncOut = new String(Proces.exec("rsync", "-e",
										(getFrontal() != null
												? SSHUtils.sshCommandNameDefault + " "
														+ TextUtilities.concatene(
																SSHUtils.getSSHOptions(),
																" ")
														+ getFrontal().getSSHName() + " "
												: "")
												+ SSHUtils.getSSHCommandName() + " "
												+ SSHUtils.getSSHOptionsString(
														getTimeoutInSecond()),
										"--inplace", "--progress", "--delete",
										"--copy-links", "--copy-dirlinks", "--recursive",
										"-t", f.getPath() + "/",
										node.getSSHName() + ":" + rsyncDestDirectory));
							}
							else
							{
								@SuppressWarnings("unused")
								String rsyncOut = new String(Proces.exec("rsync", "-e",
										(getFrontal() != null
												? SSHUtils.sshCommandNameDefault + " "
														+ TextUtilities.concatene(
																SSHUtils.getSSHOptions(),
																" ")
														+ getFrontal().getSSHName() + " "
												: "")
												+ SSHUtils.getSSHCommandName() + " "
												+ SSHUtils.getSSHOptionsString(
														getTimeoutInSecond()),
										"--inplace", "--progress", "--copy-links", "-t",
										f.getPath(),
										node.getSSHName() + ":" + rsyncDestDirectory));
							}
						}
					}
				}
			}
		};
	}

	public N getFrontal()
	{
		return sshFrontal;
	}

	public Set<Set<N>> findGroups(Set<N> nodes)
	{
		Set<Set<N>> groups = new HashSet<>();

		for (N n : nodes)
		{
			groups.add(findNASGroupOf(n));
		}

		return groups;
	}
}
