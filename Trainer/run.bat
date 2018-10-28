cd src
xcopy /Y . ..\test
cd ..\test
javac -cp . *.java
java -cp . Trainer -t train_set.txt -i data_in.txt -o data_out.txt < description_in.txt > description_out.txt
cd ..