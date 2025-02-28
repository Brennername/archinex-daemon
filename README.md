# Archinex: Intelligent File Storage and Versioning

Archinex is a project aimed at creating an intelligent file storage and versioning system. It focuses on optimizing storage space through dynamic refactoring, deduplication, and efficient indexing, while providing a seamless user experience.

Some of this readme is roadmap that hasn't yet been implemented. But describing it helps me think about the problem.

## Features

* **Dynamic Refactoring:** Identifies and factors out common data blocks across files to minimize redundancy.
* **Deduplication:** Stores identical data blocks only once, regardless of how many files use them.
* **Efficient Indexing:** Enables fast file reconstruction from factored blocks and diff patches.
* **Versioning:** Maintains a complete history of file changes, allowing retrieval of previous versions.
* **Userspace Filesystem (FUSE):** Provides a virtual filesystem that mirrors the storage, allowing transparent access to files.
* **Checksum Verification:** Ensures data integrity through robust checksumming.
* **Configurable Parameters:** Allows fine-tuning of block sizes, refactoring thresholds, and other settings.
* **PostgreSQL Metadata Storage:** Uses PostgreSQL for reliable and efficient metadata management.

## Getting Started

1.  **Prerequisites:**
    * Java Development Kit (JDK) 17 or later
    * PostgreSQL database
    * FUSE (Filesystem in Userspace) libraries
    * Maven
2.  **Clone the Repository:**
    ```bash
    git clone [repository URL]
    cd Archinex
    ```
3.  **Configure PostgreSQL:**
    * Set up your PostgreSQL database and create a database for Archinex.
    * Configure the database connection details in the `ArchinexConfig` class (or other configuration file).
4.  **Build the Project with Maven:**
    ```bash
    mvn clean package
    ```
    This will create a JAR file in the `target` directory.
5.  **Run Archinex:**

    * **Running the JAR:**
        ```bash
        java -jar target/archinex-<version>.jar [arguments]
        ```
        Replace `<version>` with the actual version of your JAR file.
        You will need to add arguments to the command to run the daemon, and to specify the mounting point of the FUSE filesystem.
        Example (This needs to be adjusted per your implementation):
        ```bash
        java -jar target/archinex-1.0-SNAPSHOT.jar --fuse-mount /mnt/archinex --config archinex.properties --daemon
        ```
        * `--fuse-mount /mnt/archinex`: Specifies the mount point for the FUSE filesystem.
        * `--config archinex.properties`: Specifies the configuration file.
        * `--daemon`: Starts the Archinex daemon.
    * **Running Directly from Maven (for Development):**
        ```bash
        mvn exec:java -Dexec.mainClass="com.danielremsburg.archinex.core.ArchinexDaemon" -Dexec.args="--fuse-mount /mnt/archinex --config archinex.properties --daemon"
        ```
        
## Contributing

Contributions are welcome! Please feel free to submit pull requests or open issues to suggest improvements or report bugs.

## License

TBD

---

## Archinex Whitepaper: Theory and Design

### Introduction

Archinex is designed to address the growing need for efficient and intelligent file storage solutions. Traditional file systems often lead to significant data redundancy, especially in environments with numerous similar files or frequently updated documents. Archinex tackles this problem by employing advanced techniques for data deduplication and dynamic refactoring.

### Core Concepts

1.  **Block Segmentation and Deduplication:**
    * Files are divided into variable-sized blocks.
    * Each block is hashed using a cryptographic hash function (e.g., SHA-256).
    * Identical blocks are stored only once, significantly reducing storage overhead.
2.  **Dynamic Refactoring:**
    * Archinex periodically analyzes the stored blocks to identify similar but not identical data.
    * Diffing algorithms are used to create patches that represent the differences between similar blocks.
    * These patches are stored alongside the base blocks, allowing for efficient reconstruction of variations.
3.  **Indexing and Metadata Management:**
    * A robust metadata store (PostgreSQL) maintains an index of all files, blocks, and patches.
    * This index enables rapid file reconstruction and version retrieval.
    * Metadata includes file paths, sizes, timestamps, checksums, and block references.
4.  **Versioning and Diffing:**
    * Archinex maintains a complete version history of files.
    * Diff patches are used to represent changes between versions, minimizing storage requirements.
    * Users can retrieve any previous version of a file.
5.  **Userspace Filesystem (FUSE):**
    * A FUSE-based virtual filesystem provides a seamless user interface.
    * Users can interact with files as if they were stored in a traditional file system.
    * Archinex handles the reconstruction and storage of files in the background.

### Design Considerations

* **Performance:**
    * Efficient indexing and caching are crucial for fast file access.
    * Asynchronous operations are used to minimize latency.
* **Data Integrity:**
    * Checksums are used to ensure data integrity.
    * Robust error handling mechanisms are implemented.
* **Scalability:**
    * The system is designed to handle large volumes of data and a high number of files.
    * PostgreSQL allows for scaling the metadata.
* **Security:**
    * Secure hashing algorithms are used.
    * Access control mechanisms are implemented.

### Future Directions

* **Cloud Integration:**
    * Implement cloud synchronization and backup.
* **Advanced Refactoring Algorithms:**
    * Explore more sophisticated algorithms for identifying and factoring out similar data.
* **Optimized Block Segmentation:**
    * Implement adaptive block sizes based on file content.

---

## Related Links and Resources

* **Project Website:** https://github.com/Brennername/archinex-daemon
* **FUSE (Filesystem in Userspace):** [https://libfuse.github.io/](https://libfuse.github.io/)
* **PostgreSQL:** [https://www.postgresql.org/](https://www.postgresql.org/)
* **Java NIO.2:** [https://docs.oracle.com/javase/tutorial/essential/io/fileio.html](https://docs.oracle.com/javase/tutorial/essential/io/fileio.html)
* **Maven:** [https://maven.apache.org/](https://maven.apache.org/)
* **SLF4J:** [https://www.slf4j.org/](https://www.slf4j.org/)
* **Java UUID:** [https://docs.oracle.com/javase/8/docs/api/java/util/UUID.html](https://docs.oracle.com/javase/8/docs/api/java/util/UUID.html)