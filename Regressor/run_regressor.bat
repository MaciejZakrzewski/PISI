cd src
xcopy /Y . ..\test
cd ..\test
javac -cp . *.java
java -cp . Regressor -t ..\datasets\set2.txt < ..\datasets\in2.txt > out1.txt
del *.java
del *.class
cd ..