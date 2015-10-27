
all:

IN=test.txt
run_ml:
	java -Xmx1600M -jar magyarlanc-2.0.jar -mode depparse -input $(IN) -output $(IN).out
#-Xmx2G
#-encoding ISO-8859-2

MLTestTok.class: MLTestTok.java
	javac -cp magyarlanc-2.0.jar MLTestTok.java

run_MLTestTok: MLTestTok.class
	java -cp .:magyarlanc-2.0.jar MLTestTok


MLTestMorph.class: MLTestMorph.java
	javac -cp magyarlanc-2.0.jar MLTestMorph.java

run_MLTestMorph: MLTestMorph.class
	java -cp .:magyarlanc-2.0.jar MLTestMorph


MLTestKRMorph.class: MLTestKRMorph.java
	javac -cp magyarlanc-2.0.jar MLTestKRMorph.java

run_MLTestKRMorph: MLTestKRMorph.class
	java -cp .:magyarlanc-2.0.jar MLTestKRMorph
