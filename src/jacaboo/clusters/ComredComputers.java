package jacaboo.clusters;

import jacaboo.NodeNameSet;

public class ComredComputers extends NodeNameSet
{
	public ComredComputers()
	{
		addAll(new CoatiComputers());
		addAll(new AosteComputers());
	}
}
