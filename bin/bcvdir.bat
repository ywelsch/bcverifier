set _FIRSTARG=%~1
shift
mvn -q -f "%~dp0..\pom.xml" exec:java -Dexec.mainClass="de.unikl.bcverifier.Main" -Dexec.args="-l %_FIRSTARG%\old %_FIRSTARG%\new -i %_FIRSTARG%\bpl\inv.bpl %1 %2 %3 %4 %5 %6 %7 %8 %9"