set CLASSPATH=%CLASSPATH%;C:\jade\lib\jade.jar;C:\Users\Yoshimitsu\Desktop\diplom\demo\src\main\java
javac ManagerAgent.java
javac SatelliteAgent.java
java jade.Boot -gui -agents "manager:org.leti.agents.ManagerAgent(satellite1.satellite2:(100.200.600.500));satellite1:org.leti.agents.SatelliteAgent;satellite2:org.leti.agents.SatelliteAgent"

