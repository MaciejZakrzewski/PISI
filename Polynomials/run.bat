cd src
xcopy /Y . ..\test
cd ..\test
javac -cp . *.java
java -cp . Converter -n 1 -k 2 > description.txt
java -cp . Converter -d description.txt < in.txt > out.txt
cd ..