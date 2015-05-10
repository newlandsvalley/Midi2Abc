#!/bin/bash

###################################################################################
# testm2a.sh
#
# m2a's usage: m2a.sh srcdir destdir tunename leadin notedur timesig rhythm key mode
#
####################################################################################

/var/data/midi2abc/scripts/midi2abc.sh /var/data/midi2abc/midi /var/data/midi2abc/abc lillasystern "(0%8)" "(1%8)" "(9,8)" Polska Dn Major
retcode=$?
echo "midi2abc return: " $retcode

exit $retcode

