::BATCH file to windows
ECHO compiling

set BATDIR=%~dp0
set LIBDIR=%BATDIR%..\build\libs-all

java -Djava.ext.dirs=%LIBDIR% Compiler %*
