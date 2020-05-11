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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import toools.extern.Proces;
import toools.io.IORuntimeException;
import toools.text.LineStreamListener;

public class SSHNode extends HardwareNode
{
	private final List<LineStreamListener> stdoutLinesListeners = new ArrayList<>();
	private final List<LineStreamListener> stderrLinesListeners = new ArrayList<>();
	private String loginName;
	private OutputStream stdin;
	private Process process;

	/**
	 * For serialization. Do not use.
	 */
	public SSHNode()
	{
	}

	public SSHNode(String nodeSpec) throws UnknownHostException
	{
		super();
		fromString(nodeSpec, System.getProperty("user.name"));
	}

	public SSHNode(String nodeSpec, String defaultUsername)
	{
		super();
		fromString(nodeSpec, defaultUsername);
	}

	protected SSHNode(InetAddress addr)
	{
		super(addr);
		this.loginName = System.getProperty("user.name");
		stdoutLinesListeners
				.add(new ToStreamLineReceiver(System.out, "> " + this + ": "));
		stderrLinesListeners
				.add(new ToStreamLineReceiver(System.err, "> " + this + ": "));
	}

	protected void fromString(String nodeSpec, String defaultUsername)
	{
		String comps[] = nodeSpec.split("@");

		switch (comps.length)
		{
		case 1:
			super.fromString(nodeSpec);
			this.loginName = defaultUsername;
			break;
		case 2:
			super.fromString(comps[1]);
			this.loginName = comps[0];
			break;
		default:
			throw new IORuntimeException(nodeSpec);
		}

		stdoutLinesListeners
				.add(new ToStreamLineReceiver(System.out, "> " + this.toString() + ": "));
		stderrLinesListeners
				.add(new ToStreamLineReceiver(System.err, "> " + this.toString() + ": "));
	}

	public String getLoginName()
	{
		return loginName;
	}

	public String getSSHName()
	{
		if (loginName == null)
		{
			return getInetAddress().getHostName();
		}
		else
		{
			return loginName + "@" + getInetAddress().getHostName();
		}
	}

	@Override
	public String toString()
	{
		return super.toString();
	}

	public List<LineStreamListener> getStdoutLinesListeners()
	{
		return stdoutLinesListeners;
	}

	public List<LineStreamListener> getStderrLinesListeners()
	{
		return stderrLinesListeners;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException
	{
		super.writeExternal(out);
		out.writeUTF(getLoginName());
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
	{
		super.readExternal(in);
		loginName = in.readUTF();
	}

	public void kill_9_1()
	{
		SSHUtils.execSh(2000, this, "kill -9 - 1");
	}

	public void exec(List<String> cmdLine) throws IOException
	{
		process = Runtime.getRuntime().exec(cmdLine.toArray(new String[0]));
		stdin = process.getOutputStream();

		forwardStreamBg(process.getInputStream(), stdoutLinesListeners);
		forwardStreamBg(process.getErrorStream(), stderrLinesListeners);
	}

	private void forwardStreamBg(InputStream source, List<LineStreamListener> out)
			throws IOException
	{
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					BufferedReader r = new BufferedReader(new InputStreamReader(source));

					while (true)
					{
						String line = r.readLine();

						if (line == null)
						{
							return;
						}
						else
						{
							for (LineStreamListener o : out)
							{
								o.newLine(line);
							}
						}
					}
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}).start();
	}

	public void stop()
	{
		if (isLocalhost())
		{
			if (stdin != null)
			{
				try
				{
					stdin.close();
				}
				catch (IOException e)
				{
				}
				finally
				{
					stdin = null;
				}
			}
		}
		else
		{
			if (process != null)
			{
				process.destroyForcibly();

				try
				{
					process.waitFor();
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
				
				process = null;
			}
		}
	}

	public void writeToStdin(String command) throws IOException
	{
		stdin.write((command + "\n").getBytes());
		stdin.flush();
	}

	public boolean isProcessTerminated()
	{
		return Proces.isTerminated(process);
	}
}
