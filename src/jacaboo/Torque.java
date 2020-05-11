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

import java.net.UnknownHostException;
import java.util.List;

import toools.extern.Proces;
import toools.io.file.RegularFile;

public class Torque extends ResourceManager
{

	public Torque(HardwareNode frontal)
	{
		super((SSHNode) frontal);
	}

	@Override
	public TorqueReservation bookNodes(int nbNode, int ppc, int durationS,
			String... properties)
	{
		String jobName = String.valueOf(Math.abs(Math.random()));
		String nodesFileName = jobName + "-nodes.lst";
		String pbsText = "#PBS -N " + jobName + "\n#PBS -o /dev/null\n"
				+ pbs_l(nbNode, ppc, properties) + "\n#PBS -l walltime="
				+ createWallTimeString(durationS) + "\nuniq $PBS_NODEFILE $HOME/"
				+ nodesFileName + " && sleep " + durationS;
		RegularFile pbsFile = new RegularFile(jobName + ".pbs");
		pbsFile.setContent(pbsText.getBytes());
		// System.out.println(pbsText);
		Proces.exec("scp", pbsFile.getPath(),
				((SSHNode) getFrontal()).getSSHName() + ":");

		// the PBS file is now on the Torque frontal, remove it from here
		pbsFile.delete();

		List<String> out = SSHUtils.execSh(3, getFrontal(), "qsub " + pbsFile.getName());
		// System.out.println("qsub stdout: " + out);
		String jobPBS_ID = out.get(0).replaceAll("\\..*", "");
		System.out.println("waiting for node availability...");
		List<String> nodes = SSHUtils.execSh(3, getFrontal(),
				"while [ ! -f " + nodesFileName + " ]; do  sleep 0.1; done; cat "
						+ nodesFileName + "; rm -f " + nodesFileName + " "
						+ pbsFile.getName());

		runWarningThread(durationS);

		if (nodes.size() != nbNode)
			throw new IllegalStateException("only got nodes " + nodes);

		return new TorqueReservation(getFrontal().getLoginName(), nodes, jobPBS_ID);
	}

	private String pbs_l(int nbNodes, int ppc, String... extra)
	{
		String r = "#PBS -l nodes=" + nbNodes + ":ppn=" + ppc;

		if (extra.length > 0)
		{
			for (int i = 0; i < extra.length; ++i)
			{
				r += ":";
				r += extra[i];
			}
		}

		return r;
	}

	private static String createWallTimeString(int nbS)
	{
		int nbH = nbS / 3600;
		nbS -= (nbH * 3600);
		int nbM = nbS / 60;
		nbS -= nbM * 60;
		return nbH + ":" + nbM + ":" + nbS;
	}

	@Override
	public String toString()
	{
		return "Torque using frontal node: "
				+ getFrontal().getInetAddress().getHostName();
	}

	public void kill(String jobID) throws UnknownHostException
	{
		SSHUtils.execSh(3, getFrontal(), "qdel " + jobID);
	}
}
