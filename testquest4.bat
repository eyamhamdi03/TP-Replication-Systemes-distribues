@echo off

REM Compile all Java files
echo Compiling Java files...
javac -cp ".;../libs/*" Replica.java ClientWriter.java ClientReader.java

REM Start Replicas
echo Starting Replica 1...
start cmd /k "java -cp .;../libs/* Replica 1"

timeout /t 1

echo Starting Replica 2...
start cmd /k "java -cp .;../libs/* Replica 2"

timeout /t 1

echo Starting Replica 3...
start cmd /k "java -cp .;../libs/* Replica 3"

timeout /t 2

REM Start ClientReader
echo Ready to start ClientReader manually if needed.
start cmd /k "java -cp .;../libs/* ClientReader"

pause
