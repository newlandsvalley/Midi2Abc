#!/bin/bash

##################################################################################
#
# transcode midi to abc
#
# usage: m2a.sh srcdir destdir tunename leadin notedur timesig rhythm key mode
#
###################################################################################

echo $@

EXPECTED_ARGS=9

if [ $# -ne $EXPECTED_ARGS ]
then
  echo "Usage: `basename $0` {srcdir destdir tunename leadin notedur timesig rhythm key mode}"
  exit $E_BADARGS
fi

# source
mididir=$1
if [ ! -d $mididir ]
then
  echo "$mididir not a directory" >&2   # Error message to stderr.
  exit 1
fi  

# destination
abcdir=$2
if [ ! -d $abcdir ]
then
  echo "$abcdir not a directory" >&2   
  exit 1
fi  

# source file
srcfile=${mididir}/${3}.mid
if [ ! -f $srcfile ]
then
  echo "no such file $srcfile" >&2  
  exit 1
fi  

# tune name
name=$3

# midi track number
trackno=$4


# lead-in duration
leadin=$5

# default note duration
notedur=$6

# rhythm
rhythm=$7

# key
key=$8

# mode
mode=$9


# transcode from .mid to .abc
miditoabc -t $trackno -l $leadin -d $notedur  -r $rhythm -k $key -m $mode -n $name -i $mididir/$3.mid -o $abcdir/$3.abc

retcode=$?
echo "midi2abc return: " $retcode

exit $retcode
