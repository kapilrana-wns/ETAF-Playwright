

cd C:\Rajkumar\Selenium Gridsetup\selenium-server-4.10.0

::::Start the manager

start /b java -jar selenium-server-4.10.0.jar hub 

::::Start the nodes
start /b java -jar selenium-server-4.10.0.jar node --port 5555 --selenium-manager true
start /b java -jar selenium-server-4.10.0.jar node --port 6666 --selenium-manager true 




