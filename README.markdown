Midi2Abc
========

This is the web interface into the [miditoabc](https://github.com/newlandsvalley/miditoabc) service. It allows users to submit traditional tunes in midi format and to transcode them into [ABC notation](http://abcnotation.com/). It uses the [Play Framework](http://www.playframework.org/) and is written in Scala.

Current version: 0.8.0

Configuration
-------------

There are two configuration files which are standard for Play applications.  Firstly, the  _routes_ file which associates requests with actions and secondly _application.conf_ for the application itself.  The only application-specific settings here are references to the a working directory for transcoding and to the _musicrest_ web service which is used to attempt to produce scores from valid ABC:

      # tradtunestore specific configurations      
      data.home="/var/data/midi2abc"
      musicrest.server="www.tradtunedb.org.uk:8080/musicrest/"
      musicrest.username="midi2abc"
      musicrest.password="3ut3rp3a"


Dependencies
------------

*  [Play 2.3.8](http://www.playframework.org/download).
*  [Dispatch 0.11.2](http://dispatch.databinder.net/Dispatch.html) for proxying requests.

Blog
----

[Elucubrations](http://myelucubrations.blogspot.co.uk/2015/04/reverse-engineering-midi.html)




