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
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import toools.StopWatch;
import toools.StopWatch.UNIT;
import toools.extern.Proces;
import toools.extern.ProcessOutput;
import toools.io.file.Directory;
import toools.io.file.RegularFile;
import toools.reflect.ClassPath;
import toools.text.TextUtilities;
import toools.thread.OneElementOneThreadProcessing;

public abstract class JavaCluster<N extends JavaNode> extends NASCluster<N>
{
	private boolean useSDP = false;
	private int debugPortBase = - 1;
	private int maxMemorySizeInMegaBytes = - 1;

	public static final String debugPortBaseVariable = "BGRPH_DEBUGPORT_BASE";
	public static final String debugPortBaseProperty = "biggrph.debugPort.base";

	public static final String maxMemoryVariable = "BGRPH_MEMORY_MAX";
	public static final String maxMemoryProperty = "biggrph.memory.max";

	@SuppressWarnings("unchecked")
	public JavaCluster(String username, N frontal, NodeNameSet nodeNames)
			throws UnknownHostException
	{
		this(frontal, (Set<N>) toNodes(nodeNames, username));
	}

	@SuppressWarnings("unchecked")
	public JavaCluster(String username, N frontal, Collection<String> nodenames)
			throws UnknownHostException
	{
		this(frontal, (Set<N>) toNodes(nodenames, username));
	}

	private static Set<JavaNode> toNodes(Collection<String> nodenames, String username)
			throws UnknownHostException
	{
		Set<JavaNode> nodeSet = new HashSet<>();

		for (String nodename : nodenames)
		{
			nodeSet.add(new JavaNode(nodename, username));
		}

		return nodeSet;
	}

	protected JavaCluster(N frontal, Set<N> nodeSet) 
	{
		super(frontal, nodeSet);

		if (System.getenv(debugPortBaseVariable) != null)
		{
			debugPortBase = Integer.parseInt(System.getenv(debugPortBaseVariable));
			System.out.println("Setting debugPortBase=" + debugPortBase);
		}
		else if (System.getProperty(debugPortBaseProperty) != null)
		{
			debugPortBase = Integer.parseInt(System.getProperty(debugPortBaseProperty));
		}
		if (System.getenv(maxMemoryVariable) != null)
		{
			maxMemorySizeInMegaBytes = Integer.parseInt(System.getenv(maxMemoryVariable));
		}
		else if (System.getProperty(maxMemoryProperty) != null)
		{
			maxMemorySizeInMegaBytes = Integer
					.parseInt(System.getProperty(maxMemoryProperty));
		}
	}

	private boolean defaultJVMIsJava8(SSHNode node)
	{
		// the Bash command that checks if the default "java" command is a v1.8
		// JVM
		// it relies on the "--version" standard option of JVMs
		// works with Oracle JDK and Open JDK
		// exits 0 if the JDK is v1.8
		String shCommand = "which java >/dev/null && java -version 2>&1 | egrep -q -e '^.*version[[:space:]]+\"1\\.8\\..+\"$' ; exit $?";

		List<String> argList = new ArrayList<String>();

		if (getFrontal() != null)
		{
			argList.add(getFrontal().getSSHName());
			argList.addAll(SSHUtils.getSSHOptions());
			argList.add(SSHUtils.getSSHCommandName());
		}

		argList.addAll(SSHUtils.getSSHOptions());
		argList.add(node.getSSHName());
		argList.add("bash");
		argList.add("--posix");

		try
		{
			// System.out.println("running: " + shCommand + " on node " + node);
			String initialSSH = (getFrontal() != null ? SSHUtils.sshCommandNameDefault
					: SSHUtils.getSSHCommandName());
			ProcessOutput output = Proces.rawExec(initialSSH, shCommand.getBytes(),
					argList.toArray(new String[argList.size()]));
			return output.getReturnCode() == 0;
		}
		catch (IOException e)
		{
			return false;
		}
	}

	/**
	 * The JVM parameter MUST be a JVM compliant with BigGrph. So there's no
	 * need to check its version. File existence should suffice. Should we hence
	 * simplify the code?
	 * 
	 * @param node
	 * @param jvm
	 * @return
	 */
	private boolean checkJVM(SSHNode node, JVM jvm)
	{
		String shCommand = "[ -x ${HOME}/" + jvm.getCommand() + " ]; exit $?";
		List<String> argList = new ArrayList<String>();

		if (getFrontal() != null)
		{
			argList.add(getFrontal().getSSHName());
			argList.addAll(SSHUtils.getSSHOptions());
			argList.add(SSHUtils.getSSHCommandName());
		}

		argList.addAll(SSHUtils.getSSHOptions());
		argList.add(node.getSSHName());
		argList.add("bash");
		argList.add("--posix");
		try
		{
			// System.out.println("running: " + shCommand + " on node " + node);
			String initialSSH = (getFrontal() != null ? SSHUtils.sshCommandNameDefault
					: SSHUtils.getSSHCommandName());
			ProcessOutput output = Proces.rawExec(initialSSH, shCommand.getBytes(),
					argList.toArray(new String[argList.size()]));
			return output.getReturnCode() == 0;
		}
		catch (IOException e)
		{
			return false;
		}
	}

	public void enableDebugging(int debugPortBaseNumber)
	{
		debugPortBase = debugPortBaseNumber;
	}

	@Override
	public void start()
	{
		StopWatch sw = new StopWatch(UNIT.ms);
		super.start();

		// no specific JVM to use

		new OneElementOneThreadProcessing<N>(getNodes())
		{
			@Override
			protected void process(N node) throws Throwable
			{
				// if the node runs on the local computer
				if (node.isLocalhost())
				{
					// use the same Java as the one on the master
					String javaVersion = System.getProperty("java.version");

					if (javaVersion.startsWith("1.8."))
					{
						node.jvm = JVM.currentJava;
						System.out.println(
								"Node " + node + ": current JVM has correct version.");
					}
					else
					{
						throw new IllegalStateException(
								"unsupported JVM version " + javaVersion);
					}
				}
				else
				{
					// checks by SSH if the default java command on the
					// slave node complies
					if (defaultJVMIsJava8(node))
					{
						node.jvm = JVM.defaultJava;
						System.out.println(
								"Node " + node + ": default JVM has correct version.");
					}
					else
					{
						System.err.println("Node " + node
								+ ": default JVM not found or incorrect version.");
					}

				}
			}
		};

		// non-local nodes
		new OneElementOneThreadProcessing<N>(getNodes())
		{
			@Override
			protected void process(N node) throws Throwable
			{
				if (node.jvm == null)
				{
					if (checkJVM(node, JVM.java8oracle))
					{
						node.jvm = new JVM("${HOME}/" + JVM.java8oracle.getCommand());
						System.out.println(
								"Node " + node + ": found JVM " + node.jvm.getCommand());
					}
				}
			}
		};

		new OneElementOneThreadProcessing<Set<N>>(getNASGroups())
		{
			@Override
			protected void process(Set<N> group)
			{
				// if one node in the group has no jvm, setup by using a
				// downloaded one and use it for all nodes in the group.
				boolean noJVM = false;
				for (N node : group)
				{
					if (node.jvm == null)
					{
						noJVM = true;
						break;
					}
				}
				if (noJVM)
				{
					N installNode = group.iterator().next();
					JVM jvmToDownload = JVM.java8oracle;
					String shText = "cd ${HOME} && wget -c -nv "
							+ jvmToDownload.getDownloadOptions() + " "
							+ jvmToDownload.getDownloadLink() + " -O "
							+ jvmToDownload.getArchiveName() + " && tar xzf "
							+ jvmToDownload.getArchiveName();
					// System.out.println("running: " + shText + " on node " +
					// installNode);
					SSHUtils.execSh(getFrontal(), getTimeoutInSecond(), installNode,
							shText);
					JVM installedJvm = new JVM("${HOME}/" + jvmToDownload.getCommand());
					for (N node : group)
					{
						node.jvm = installedJvm;
						System.out.println("Node " + node + ": installed JVM is "
								+ node.jvm.getCommand());
					}
				}
			}
		};

		ClassPath clp = ClassPath.retrieveSystemClassPath();
		System.out
				.println("Deploying Java classes: " + (clp.sizeInBytes() / 1000) + "kb");
		System.out.println(clp);
		Binaries.ensureLocalSymLinksToClassPathEntriesAreProperlySet();
		System.out.println("Links are set");
		deploy(Binaries.localBinariesDir(), Binaries.jarDirectoryPathRelativeToHomedir());

		if (useSDP)
		{
			String spdConf = "";

			for (N n : getNodes())
			{
				spdConf += "bind " + n.getInetAddress().getHostAddress() + " *\n";
			}

			for (N n : getNodes())
			{
				spdConf += "connect " + n.getInetAddress().getHostAddress() + " *\n";
			}

			RegularFile spdFile = new RegularFile(Directory.getSystemTempDirectory(),
					"sdp.conf");
				spdFile.setContent(spdConf.getBytes());

			deploy(spdFile);
			spdFile.delete();
		}

		System.out.println(TextUtilities.box("Code deployment took " + sw));

		sw.reset();

		System.out.println("Running distributed application on nodes " + getNodes());

		new OneElementOneThreadProcessing<N>(getNodes())
		{
			@Override
			protected void process(N node) throws Throwable
			{
				runClass(node);
			}
		};

		System.out.println(TextUtilities.box("RPC services startup took " + sw));
	}

	public void stop()
	{
		for (SSHNode node : getNodes())
		{
			node.stop();
		}
	}

	public String getName()
	{
		return getClass().getName();
	}

	@Override
	public String toString()
	{
		StringBuilder b = new StringBuilder();

		for (N i : getNodes())
		{
			b.append(i.toString());
			b.append('\n');
		}

		return b.toString();
	}

	protected void runClass(final N targetNode) throws Throwable
	{
		if (targetNode.isLocalNode())
		{
			targetNode.run(getMainClass(targetNode).newInstance(),
					getMainClassParameters(targetNode));
		}
		else
		{
			List<String> cmdLine = new ArrayList<>();

			if ( ! targetNode.isLocalhost())
			{
				if (getFrontal() != null)
				{
					cmdLine.add(SSHUtils.getSSHCommandName());
					cmdLine.add("-o");
					cmdLine.add("ForwardX11=no");
					cmdLine.add(getFrontal().getSSHName());
				}

				cmdLine.add(SSHUtils.getSSHCommandName());
				cmdLine.addAll(SSHUtils.getSSHOptions(getTimeoutInSecond()));
				cmdLine.add(targetNode.getSSHName());
			}

			cmdLine.add("bash");
			cmdLine.add("--posix");

			targetNode.exec(cmdLine);

			String command = TextUtilities.concatene(getJavaCmdLineElements(targetNode),
					" ");
			System.out.println(targetNode + ": " + command);
			targetNode.writeToStdin(command);
		}
	}

	private List<String> getJavaCmdLineElements(JavaNode node)
	{
		List<String> argList = new ArrayList<>();
		argList.add(node.jvm.getCommand());

		List<String> vmParms = getRemoteVirtualMachineParameters(node);

		if (vmParms != null)
		{
			argList.addAll(vmParms);
		}

		argList.add(MainClassRunner.class.getName());

		Class<? extends RemoteMain> main = getMainClass(node);

		if (main != null)
		{
			argList.add("'" + main.getName() + "'");
			argList.add(Binaries.getApplicationName());
			List<String> ar = getMainClassParameters(node);

			if (ar != null)
			{
				argList.addAll(ar);
			}
		}

		return argList;
	}

	final static public String jvmParametersProperty = "biggrph.jvm.parameters";

	public List<String> getRemoteVirtualMachineParameters(SSHNode node)
	{
		List<String> argList = new ArrayList<String>();

		if (System.getProperty(jvmParametersProperty) != null)
		{
			argList.add(System.getProperty(jvmParametersProperty));
		}

		// performance options
		// this one dramatically slows down!!!
		// l.add("-XX:CompileThreshold=0");
		// argList.add("-XX:+AggressiveOpts");
		// argList.add("-XX:+UseFastAccessorMethods");

		// if the user has defined -Xmx programmatically
		if (maxMemorySizeInMegaBytes > 0)
		{
			argList.add("-Xmx" + maxMemorySizeInMegaBytes + "M");
			argList.add("-Xms" + maxMemorySizeInMegaBytes + "M");
		}

		if (useSDP)
		{
			// enable Infiniband
			argList.add("-Djava.net.preferIPv4Stack=true");
			argList.add("-Dcom.sun.sdp.debug");
			argList.add("-Dcom.sun.sdp.conf=$HOME/sdp.conf");
		}

		if (debugPortBase > 0)
		{
			/*
			 * See http://docs.oracle.com/javase/7/docs/technotes/guides/jpda/
			 * conninv .html for details of remote debugging with the jdwp
			 * library. The configuration below allows to debug from Eclipse
			 * using a debug launch configuration of type
			 * "Remote Java Application": set the Connection Type as
			 * "Standard (Socket Attach)".
			 */
			synchronized (this)
			{
				System.out.println(
						"Using debugPortBase=" + debugPortBase + " for node " + node);
				argList.add("-agentlib:jdwp=transport=dt_socket," + "address="
						+ node.getInetAddress().getHostAddress() + ":" + debugPortBase
						+ ",server=y,suspend=n");
				debugPortBase++;
			}
		}

		// if assertions are enabled locally, enable them remotely
		boolean assertsEnabled = false;
		assert (assertsEnabled = true) == true;
		argList.add(assertsEnabled ? "-ea" : "-da");

		argList.add("-classpath");
		argList.add("$(echo ${HOME}/" + Binaries.jarDirectoryPathRelativeToHomedir()
				+ "/* | sed 's/ /:/g')");
		return argList;
	}

	public List<String> getMainClassParameters(JavaNode n)
	{
		return new ArrayList<String>();
	}

	public int getMaxMemorySizeInMegaBytes()
	{
		return maxMemorySizeInMegaBytes;
	}

	public void setMaxMemorySizeInMegaBytes(int maxMemorySizeInMegaBytes)
	{
		if (maxMemorySizeInMegaBytes < 0)
			throw new IllegalArgumentException("invalid -Xmx value: "
					+ maxMemorySizeInMegaBytes + ". It must be > 0");

		this.maxMemorySizeInMegaBytes = maxMemorySizeInMegaBytes;
	}

	public abstract Class<? extends RemoteMain> getMainClass(JavaNode n);

}
