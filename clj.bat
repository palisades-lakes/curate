@echo off
:: mcdonald.john.alan@gmail.com
:: 2021-03-15

::set GC=-XX:+AggressiveHeap -XX:+UseStringDeduplication 
set GC=

set TRACE=
::set TRACE=-XX:+PrintGCDetails -XX:+TraceClassUnloading -XX:+TraceClassLoading

set THRUPUT=-server
::set THRUPUT=

::set XMX=-Xms29g -Xmx29g -Xmn11g 
set XMX=-Xms12g -Xmx12g -Xmn5g 

set OPENS=--add-opens java.base/java.lang=ALL-UNNAMED
set CP=-cp ./src/scripts/clojure;lib/*

set JAVA="%JAVA_HOME%\bin\java"

set CMD=%JAVA% %THRUPUT% -ea %GC% %PROF% %XMX% %COMPRESSED% %TRACE% %OPENS% %CP% clojure.main %*
::echo %CMD%
%CMD%
