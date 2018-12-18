cd src
xcopy /Y . ..\test
cd ..\test
javac -cp . *.java
java -cp . Regressor -t set.txt
del *.java
del *.class
cd ..