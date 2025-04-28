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

## Questions

1. Réaliser le processus de ClientWrite (programme java) qui permet d’émettre un
   message d’ajout d’une ligne de texte vers le broker RabbitMQ.

2. Réaliser le processus Replica (programme) qui permet de lire de sa file
   correspondante de RabbitMQ un message d’une ligne à ajouter pour son fichier
   dans son répertoire correspondant (local). Il y a 3 processus Replica qui
   auront le même code (à ne pas copier le code) mais avec un argument qui
   indique le numéro du processus. On aura alors le lancement de ces programmes
   en ligne de commande comme suit : Java Replica 1 Java Replica 2 Java Replica
   3

3. Vérifier que le fonctionnement des copies sur les différents réplicas est
   réalisé correctement.

4. Réaliser le code du processus ClientReader. Ce processus envoi un message de
   requête ‘Read Last’ (cette requête demande l’affichage de la dernière ligne
   du fichier texte). Puis le client à travers le même processus clientReader
   attend à la fin les messages des différents Replica. Puis il assure
   l’affichage.

5. Pour la question 4, il n’est pas nécessaire de lire les réponses de tous les
   Replica. Pour faire rapidement, on peut se contenter de la première réponse.
   On peut ici simuler la panne d’un de replica en arrêtant son exécution et
   puis on réalise la lecture du client pour voir comment la réplication permet
   d’assurer la disponibilité des données.

6. On va réaliser une simulation d’exécution dans laquelle, le client
   clientWriter écrit ces deux lignes de données : « 1 Texte message1 » puis « 2
   Texte message2 » . Ensuite, nous arrêtons Replica 2 (comme s’il est en
   panne). Puis le client clientWriter écrit ces deux lignes de données : « 3
   Texte message 3 » puis « 4 Texte message4 » . On remet en suite le processus
   Replica 2 comme s’il a repris son fonctionnement. Vérifier les 3 fichiers des
   3 replica . Le problème ici est que les 3 replica n’ont pas le même contenu
   de fichier Texte.

7. Créer une nouvelle version de ClientReaderV2 (il faut garder l’ancienne
   version pour l’éventuel validation) qui va faire une requête d’affichage
   totale du fichier Texte avec un message :‘Read All’ . Puis ce même processus
   va lire les lignes des 3 fichiers et puis va afficher les lignes qui
   apparaissent dans la majorité des propositions des 3 replica. Il ne faut pas
   oublier aussi de mettre une nouvelle version du processus Replica pour
   prendre en considération cette nouvelle requête du client. Ce processus envoi
   la totalité du fichier dont chaque message envoyé contient une seule ligne du
   fichier. Donc, la transmission se fait ligne par ligne.

## Project Components

### 1. ClientWriter (Java Program)

- Sends messages to RabbitMQ to **request the addition** of new lines into the
  replicated files.
- Each message will contain a line in the format:
    ```bash
    <line_number> <line_text>
    ```
- Example of two lines sent:
    - `1 Texte message1`
    - `2 Texte message2`

### 2. Replica (Java Program)

- Listens to a **dedicated RabbitMQ queue**.
- On receiving a **write request**:
    - Appends the received line into its local replica file.
- On receiving a **read request** (`Read Last` or `Read All`):
    - Responds accordingly (sending last line or sending the entire file
      line-by-line).
- **3 instances** of the Replica will be started, each representing a replica
  (Replica 1, Replica 2, Replica 3).

#### Launch Command:

```bash
java Replica <replica_number>
```

Example:

```bash
java Replica 1
```

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

2. **Start the 3 Replica processes**:

    ```bash
    java Replica 1
    java Replica 2
    java Replica 3
    ```

3. **Run the ClientWriter** to write initial data:

    - Send:
        - `1 Texte message1`
        - `2 Texte message2`

4. **Simulate a crash**:

    - **Stop Replica 2** manually to simulate a failure.

5. **Continue writing data**:

    - Send:
        - `3 Texte message3`
        - `4 Texte message4`

6. **Restart Replica 2**:

    - Relaunch:
        ```bash
        java Replica 2
        ```

7. **Verify the replicas**:

    - Check the three local replica files.
    - Notice that Replica 2 will have incomplete data (it missed some writes
      during downtime).

8. **Run ClientReader**:

    - Use `Read Last` to test the system's ability to retrieve the latest
      available data even if some replicas failed.

9. **Run ClientReaderV2**:
    - Use `Read All` to retrieve all file contents.
    - Apply majority voting to correct inconsistencies between replicas.

---

## Project Structure

```

├── ClientWriter.java
├── ClientReader.java
├── ClientReaderV2.java
├── Replica.java
├── /replica1/   (Folder for Replica 1)
│   └── file.txt
├── /replica2/   (Folder for Replica 2)
│   └── file.txt
├── /replica3/   (Folder for Replica 3)
│   └── file.txt
├── README.md
```

---

## Technical Requirements

- **Java 8 or higher**
- **RabbitMQ Server** installed and running
- Java dependencies:
    - RabbitMQ Java Client Library

## Additional Notes

- Ensure RabbitMQ Queues are properly named (ex: `write_queue`,
  `read_queue_replica1`, etc.).
- For each Replica, the correct folder must exist and be writable.
- Handle error cases such as:
    - RabbitMQ server down
    - Network delays
    - Replica process failure and recovery
- Extend easily to simulate real distributed environments if needed.

## Deliverables

- Full source code (all Java classes)
- README file
- Simulation screenshots
- Testing and results report
