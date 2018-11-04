cd src
xcopy /Y . ..\test
cd ..\test
javac -cp . *.java
java -cp . Converter -n 2 -k 2 > description.txt
java -cp . Converter -d description_test.txt < in_conv.txt > out_conv.txt
cd ..