cd src
xcopy /Y . ..\test
cd ..\test
javac -cp . *.java
java -cp . Validator -g set.txt -d out > splits
java -cp . Trainer -t out/training_set1.txt -i data_in.txt -o data_out.txt < description_in.txt > description_out.txt
java -cp . Trainer -t out/training_set2.txt -i data_in.txt -o data_out.txt < description_in.txt > description_out.txt
java -cp . Trainer -t out/validation_set1.txt -i data_in.txt -o data_out.txt < description_in.txt > description_out.txt
java -cp . Trainer -t out/validation_set2.txt -i data_in.txt -o data_out.txt < description_in.txt > description_out.txt
java -cp . Validator -e validation_set.txt < out.txt > evaluation
java -cp . Validator -v evaluation.txt > hyperparameter
del *.java
del *.class
cd ..