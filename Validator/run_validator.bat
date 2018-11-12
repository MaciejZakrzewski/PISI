cd src
xcopy /Y . ..\test
cd ..\test
javac -cp . *.java
java -cp . Validator -g set.txt -d out > splits
java -cp . Validator -e validation_set.txt < out.txt > evaluation
java -cp . Validator -v evaluation.txt > hyperparameter
del *.java
del *.class
cd ..