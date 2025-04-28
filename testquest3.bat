@echo off

echo Starting Replica 1...
start cmd /k "javac -cp ../libs/*;. Replica.java && java -cp ../libs/*;. Replica 1"

timeout /t 1

echo Starting Replica 2...
start cmd /k "javac -cp ../libs/*;. Replica.java && java -cp ../libs/*;. Replica 2"

timeout /t 1

echo Starting Replica 3...
start cmd /k "javac -cp ../libs/*;. Replica.java && java -cp ../libs/*;. Replica 3"

timeout /t 2

echo Starting ClientWriter...
start cmd /k "javac -cp ../libs/*;. ClientWriter.java && java -cp ../libs/*;. ClientWriter"
echo Starting ClientReader...

start cmd /k "javac -cp ../libs/*;. ClientReader.java && java -cp .;../libs/* ClientReader"
