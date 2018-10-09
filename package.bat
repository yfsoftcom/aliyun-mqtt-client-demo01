@echo off
mkdir release
mkdir release\target
mkdir release\target\lib
copy run.bat release
copy target\*.jar release\target\
copy target\lib\*.jar release\target\lib\