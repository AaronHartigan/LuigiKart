javac a3\*.java 
javac myGameEngine\*.java
javac NetworkingServer\*.java

dir /s /B *.java > sources.txt
> javac @sources.txt