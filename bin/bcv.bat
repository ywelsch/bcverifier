mvn -q -f "%~dp0..\pom.xml" exec:java -Dexec.mainClass="de.unikl.bcverifier.Main" -Dexec.args="%*"