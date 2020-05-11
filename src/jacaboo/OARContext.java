package jacaboo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import toools.io.file.RegularFile;

public class OARContext
{
	private List<String> nodeNames;
	private String jobId = null;
	private String jobName = null;
	
	public static final String OAR_SSH_NAME = "oarsh";
	
	public OARContext() throws IOException
	{
		this.nodeNames = new ArrayList<String>();
		if (System.getenv("OAR_NODEFILE") != null)
		{
			String nodefile = System.getenv("OAR_NODEFILE");
			RegularFile file = new RegularFile(nodefile);
			if (file.canRead())
			{
				for (String line : new String(file.getContent()).split("\n"))
				{
					line = line.trim();
					if ( ! line.isEmpty() && ! nodeNames.contains(line))
					{
						nodeNames.add(line);
					}
				}
			}
		}
		jobId = System.getenv("OAR_JOB_ID");
		if (jobId != null)
		{
			System.setProperty(SSHUtils.sshCommandNameProperty, OAR_SSH_NAME);
		}
		jobName = System.getenv("OAR_JOB_NAME");
	}
	
	public boolean runFromOAR()
	{
		return jobId != null;
	}

	public String getJobId()
	{
		return jobId;
	}
	
	public String getJobName()
	{
		return jobName;
	}
	
	public NodeNameSet getNodeNames()
	{
		return new NodeNameSet(nodeNames);
	}

	/**
	 * Returns a {@link NodeNameSet} structure filled with the names of the nodes provided by
	 * OAR manager, except the first one which is considered as the master node.
	 * @see OARContext#getMasterName()
	 * @see OARContext#getNodeNames()
	 */
	public NodeNameSet getSlaveNames()
	{
		return new NodeNameSet( nodeNames.subList(1, nodeNames.size()) );
	}
	
	/**
	 * Returns the name of the first computing node allocated by OAR for the current job.
	 * @see #getNodeNames()
	 */
	public String getMasterName()
	{
		return nodeNames.get(0);
	}

}
