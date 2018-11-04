cd src
xcopy /Y . ..\test
cd ..\test
javac -cp . *.java
java -cp . Scaler -a file1.txt file2.txt > data.txt
java -cp . Scaler -s data.txt < in.txt > out.txt
java -cp . Scaler -u data.txt < out.txt > out2.txt
cd ..