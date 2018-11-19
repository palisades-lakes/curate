@echo off
:: palisades.lakes@gmail.com
:: 2018-09-03

::set GC=-XX:+AggressiveHeap -XX:+UseStringDeduplication 
set GC=

set COMPRESSED=
::set COMPRESSED=-XX:CompressedClassSpaceSize=3g 

set TRACE=
::set TRACE=-XX:+PrintGCDetails -XX:+TraceClassUnloading -XX:+TraceClassLoading

::set THRUPUT=-server -XX:+AggressiveOpts 
set THRUPUT=-server  -Xbatch
::set THRUPUT=

::set XMX=-Xms29g -Xmx29g -Xmn11g 
::set XMX=-Xms12g -Xmx12g -Xmn5g 
set XMX=-Xms12g -Xmx12g

set CP=-cp ./src/scripts/clojure;lib/*
set JAVA="%JAVA_HOME%\bin\java"

set CMD=%JAVA% %THRUPUT% -ea -dsa %GC% %XMX% %COMPRESSED% %TRACE% %CP% clojure.main %*
::echo %CMD%
%CMD%
