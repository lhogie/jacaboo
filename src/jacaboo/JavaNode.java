package jacaboo;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

public class JavaNode extends SSHNode
{
	private RemoteMain runningMain;
	public JVM jvm;

	/**
	 * For serialization. Do not use.
	 */
	public JavaNode()
	{
	}

	public JavaNode(String nodeSpec) throws UnknownHostException
	{
		super(nodeSpec);
	}

	public JavaNode(String nodeSpec, String defaultUsername)
	{
		super(nodeSpec, defaultUsername);
	}
	
	protected JavaNode(InetAddress addr)
	{
		super(addr);
	}
	
	public void run(RemoteMain r, List<String> parms)
	{
		if (this.runningMain != null)
			throw new IllegalStateException("a Java program is already running");
		
		this.runningMain = r;
		r.main(parms);
	}
	
	public void stop()
	{
		if (isLocalNode())
		{
			if (runningMain != null)
			{
				runningMain.stop();
				runningMain = null;
			}
		}
		else
		{
			super.stop();
		}
	}

}
