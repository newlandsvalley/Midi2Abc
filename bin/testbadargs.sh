#!/bin/bash

###################################################################################
# testm2a.sh
#
# m2a's usage: m2a.sh srcdir destdir tunename leadin notedur timesig rhythm key mode
#
####################################################################################

/var/data/midi2abc/scripts/midi2abc.sh foo bar baz
retcode=$?
echo "midi2abc return: " $retcode

exit $retcode

