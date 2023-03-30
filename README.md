# Project "Socket Programing" for the CS494 course

### Tech stack
* Java (JDK 17)
* JavaFX
* Maven

### Architecture and Project structure
This project is composed of two modules (two subprojects): client and server. 
The architecture applied here is Client-Server, which means that there are 1 server and many clients.

![alt text](client-server-pattern.png 'Client-Server pattern')

So in this project, we need to run 1 instance of `server` and multiple instances of `client`.

The project structure includes two modules: _**server**_ and _**client**_. Consider these like two separate projects.

### Step-by-step guide to run
#### Prerequisite
* Maven
* JDK 17 or above (you may experiment with the older versions)

#### Steps to  run the project
1. At directory `./server`, run command `mvn clean install`
2. Do the same the _**client**_ module, at directory `./client`, run command `mvn clean install` 
3. Run the Main class at each module. Note: Run the `server` first, then the `client`.

After running, it should look something like this: 
![alt text](example-run.png 'Example of successful run')
