# sends the KILL signal to all $USER's processes on the given hostnames, in parallel

if [ -z $1 ]
then
  echo Syntax: killCluster.sh [host1] [host2] ... hostN
else
  while [ ! -z $1 ]
  do
    ssh $1 kill -9 -1 &
    shift
  done
fi

