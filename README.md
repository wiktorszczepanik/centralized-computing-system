## Centralized Computing System (CCS)

### Overview

The CCS application is a computation **server** implemented in Java that provides three main functionalities: service discovery (*UDP*), client communication (*TCP*), and statistics reporting.
A **client** application discovers the server via broadcast, then connects and request arithmetic operations. The server processes these requests and returns results or error messages.
Project was developed as part of the SKJ course at PJATK.

### Server implementation

The application operates as a server that:

- Detects services – Through the *clientInit* method, the server listens on a *UDP* port, receiving messages from clients to detect the service. Upon receiving a "CCS DISCOVER" message, it responds with a "CCS FOUND" message to the sender's address.


- Performs calculations – Using the *clientDeal* method, the server waits for messages from clients and performs calculations based on the message content. The format for these messages is <OPERATION> <ARG1> <ARG2>. The server supports arithmetic operations (ADD, SUB, MUL, DIV) and returns either a result or an error depending on the input arguments.


- Reports statistics – Every 10 seconds, the server displays current statistics including the number of connections, performed operations, operation attempts (regardless of result), errors, and the sum of results.

Server runs using the command:
```Bash
java -jar CCS.jar <port>
```

### Client implementation

The client sends a UDP broadcast to discover the server and connects via TCP to request arithmetic operations like ADD, SUB, MUL or DIV. The client operates according to the protocol and can terminate the process at any time.

Client runs using the command:
```Bash
java -jar ClientCCS.jar <port>
```


### Handling Multiple Clients

The application is designed to handle multiple clients simultaneously. Each client is managed by a separate thread, allowing the server to process multiple requests at the same time.

### Requirements

The application is written in Java (JDK 1.8) and uses standard classes to handle TCP and UDP protocols. It runs on a local platform and requires the correct port number to be passed as an argument when starting the program.
