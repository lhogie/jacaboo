package jacaboo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import toools.io.file.RegularFile;

/**
 * Represent the information received from the Torque resource manager when the
 * job is scheduled to run. The information is extracted from the environment variables
 * set by the Torque manager, see the
 * <a href="http://docs.adaptivecomputing.com/torque/4-0-2/Content/topics/commands/qsub.htm">
 * qsub command
 * </a> documentation
 * for a comprehensive list of these environment variables.<br>
 * This class does not extract all the information provided by Torque, instead it focuses on
 * the names of the computing nodes, the Id and name of the job and some useful directories.<br>
 * Typical usage is:<br>
 * 
 * <pre><code>
 * TorqueContext tc = new TorqueContext();
 * if ( ! tc.runFromTorque() ) {
 *     return;
 * }
 * NodeNameSet nodeNames = tc.getNodeNames();
 * BigGrphCluster cluster = BigGrphCluster.workstations(System.getProperty("user.name"),
 *                                                      nodeNames);
 * ...
 * </code></pre>
 * This class allows to split the set of computing nodes provided by Torque in two parts : the
 * master node which is the first node allocated by Torque, and the slaves which are all the
 * other nodes. When a job is executed by Torque, the commands specified in the job submission
 * file or as argument to the qsub command are executed on the master node only.<br>
 * This means that in a BigGrph java program, the TorqueContext object is created on this master
 * node, and all the other nodes will be used by BigGrph as slaves/computing nodes. The main()
 * program can choose to use or not the master node as a computing node by using either the
 * {@link TorqueContext#getNodeNames()} or the {@link TorqueContext#getSlaveNames()} function to
 * initialize the cluster.
 *    
 * @author nchleq
 *
 */
public class TorqueContext {
	
	private List<String> nodeNames;
	private String jobId;
	private String jobName;
	private String pbsWorkDir;
	private String pbsTmpDir;
	
	/**
	 * Creates and initializes a TorqueContext object. Information about the execution environment
	 * is extracted and stored in the returned object for later retrieval.
	 * @throws IOException if the file provided by Torque and containing the allocated nodes names
	 * cannot be opened and read.
	 * @see #getNodeNames()
	 * @see #getSlaveNames()
	 */
	public TorqueContext() throws IOException {
		this.nodeNames = new ArrayList<String>();
		String pbsNodeFile = System.getenv("PBS_NODEFILE");
		if (pbsNodeFile != null) {
			RegularFile file = new RegularFile(pbsNodeFile);
			if (file.canRead()) {
				for (String line : new String(file.getContent()).split("\n")) {
					line = line.trim();
					if (!line.isEmpty() && !nodeNames.contains(line)) {
						nodeNames.add(line);
					}
				}
			}
		}
		this.jobId = System.getenv("PBS_JOBID");
		this.jobName = System.getenv("PBS_JOBNAME");
		this.pbsWorkDir = System.getenv("PBS_O_WORKDIR");
		this.pbsTmpDir = System.getenv("TMPDIR");
	}

	/**
	 * Returns true if the program is running from Torque manager. The test is done by
	 * checking that the environment variable PBS_JOBID is defined and has a non empty value. 
	 * @return true if the program is running from Torque manager, false otherwise.
	 */
	public boolean runFromTorque() {
		return (jobId != null && ( ! jobId.isEmpty()));
	}
	
	/**
	 * Returns a {@link NodeNameSet} structure filled with all the names of the nodes provided by
	 * the Torque manager. Values are found by reading the file provided by Torque in the value
	 * of the environment variable PBS_NODEFILE. Duplicate names are removed. The returned set may
	 * have a different ordering than the one provided by Torque. 
	 * @see #getSlaveNames()
	 */
	public NodeNameSet getNodeNames() {
		return new NodeNameSet(nodeNames);
	}
	
	/**
	 * Returns a {@link NodeNameSet} structure filled with the names of the nodes provided by
	 * Torque manager, except the first one which is considered as the master node.
	 * @see TorqueContext#getMasterName()
	 * @see TorqueContext#getNodeNames()
	 */
	public NodeNameSet getSlaveNames() {
		return new NodeNameSet( nodeNames.subList(1, nodeNames.size()) );
	}
	
	/**
	 * Returns the name of the first computing node allocated by Torque for the current job.
	 * @see #getNodeNames()
	 */
	public String getMasterName() {
		return nodeNames.get(0);
	}
	
	/**
	 * Returns a {@link NodeNameSet} structure filled with the names of the nodes converted using the
	 * pattern and the replacement string. The conversion is done using {@link String#replaceAll(pattern, replacement)}.
	 * This can be used to change the name of hosts according to various rules used in the target cluster.
	 * <br>Example:<br>
	 * <pre>
	 * TorqueContext tc = new TorqueContext();
	 * NodeNameSet nodes = tc.convertNodeNames("\\.inria\\.fr$", ".ib.inria.fr");
	 * </pre>
	 * @param pattern
	 * @param replacement
	 * @return
	 * @see #getNodeNames()
	 * @see String#replaceAll(String, String)
	 */
	public NodeNameSet convertNodeNames(String pattern, String replacement) {
		NodeNameSet newNames = new NodeNameSet();
		for (String name : getNodeNames()) {
			String replaced = name.replaceAll(pattern, replacement);
			newNames.add(replaced);
		}
		return newNames;
	}
	
	/**
	 * Does the same as {@link #convertNodeNames(String, String)} on the slave names, ie. all node
	 * names except the first one specified by Torque.
	 */
	public NodeNameSet convertSlaveNames(String pattern, String replacement) {
		return getSlaveNames().replaceAll(pattern, replacement);
	}
	
	/**
	 * Returns the job Id provided by Torque. This is the value of the environment variable
	 * PBS_JOBID.
	 * 
	 */
	public String getJobId() {
		return jobId;
	}
	
	/**
	 * Returns the job name provided by Torque. This is the value of the environment variable
	 * PBS_JOBNAME.
	 * 
	 */
	public String getJobName() {
		return jobName;
	}
	
	/**
	 * Returns the path of the unique temporary directory assigned by Torque to the current job.
	 * The returned string is the value of the environment variable TMPDIR.
	 */
	public String getPbsTmpDir() {
		return pbsTmpDir;
	}
	
	/**
	 * Returns the path of the directory from which the qsub command was run. The returned string is
	 * the value of the environment variable PBS_O_WORKDIR.
	 */
	public String getPbsWorkDir() {
		return pbsWorkDir;
	}
	
	/**
	 * Returns the value of an environment variable. This function is only a call to {@link System#getenv(String)}.
	 */
	public String getVariable(String varName) {
		return System.getenv(varName);
	}
}
