# TP: Replication System in Distributed Systems

## Objective

This project aims to implement a **distributed replication system** where a
**text file** is replicated across multiple logical disks (represented by
directories).  
The replication will be managed using two different strategies:

- **Strong Consistency**: Favors consistency of data across replicas.
- **Availability-Consistency Compromise**: Accepts some temporary
  inconsistencies to increase availability.

Communication between processes will be done through **RabbitMQ**
(Message-Oriented Middleware, MoM).  
The simulation will involve three types of processes:

- **ClientWriter**: Sends write requests.
- **Replica**: Manages data storage and responds to read/write operations.
- **ClientReader**: Sends read requests and displays results.

## Project Components

### 1. ClientWriter (Java Program)

- Sends messages to RabbitMQ to **request the addition** of new lines into the
  replicated files.

### 2. Replica (Java Program)

- Listens to a **dedicated RabbitMQ queue**.
- On receiving a **write request**:
    - Appends the received line into its local replica file.
- On receiving a **read request** (`Read Last` or `Read All`):
    - Responds accordingly (sending last line or sending the entire file
      line-by-line).
- **3 instances** of the Replica will be started, each representing a replica
  (Replica 1, Replica 2, Replica 3).

### 3. ClientReader (Java Program)

- Sends a **read request** message to RabbitMQ:
- `Read Last`: Requests the last line from the replicas.
- Waits to receive responses from replicas.
- Displays the received lines.
- Initially, **only the first received reply** is used to display the result (to
  simulate failover and availability).

### 4. ClientReaderV2 (New Version)

- Sends a `Read All` request.
- Receives the full contents of the files from all replicas (each line is sent
  separately).
- Applies **majority voting** to determine the correct version of each line:
- Only lines that appear in the majority of replicas will be displayed.
- Helps in **resolving inconsistencies** caused by crashes/failures.

---

## Simulation Steps

1. **Start RabbitMQ Broker** if not already running.
2. **Run .bat files**

- for the first ClientReader version execute the `testquest3.bat` this file will
  compile and run the 3 Replicas , the ClientWriter and the ClientReader

- for the second ClientReaderV2 execute the `testquest4` this will compile and
  run the three replicas the clientWriter and the clientReaderV2

## Technical Requirements

- **Java 8 or higher**
- **RabbitMQ Server** installed and running
- Java dependencies:
- RabbitMQ Java Client Library

---

This work was realised by : `Eya Mhamdi` as a Distributed Systems Project .
