Here is the English version of your project documentation:

***

# 🚀 Stateless C-Autograder (CS 240 & CS 680)

## 📌 Overview
A **stateless** code evaluation platform (autograder) designed to validate assignments for the CS 240 (C Programming) course. The system allows students to submit their code and files through a simple web interface to check if their logic and file manipulation are correct before their official submission.

The core of the system will be developed in **Java**, serving as a hands-on project for the System Design course (CS 680), with a strong focus on infrastructure orchestration, processing queues, and containerization.

## ⚙️ Architecture & Execution Flow
The system follows the *KISS* (Keep It Simple, Stupid) principle and operates as an isolated input/output pipeline:
1. **Upload:** The student accesses the webpage (no login required), selects the assignment (e.g., `hw03`), and uploads the `.c` file.
2. **Isolation:** The Java backend receives the request and uses the Docker API to spawn an ephemeral container (Ubuntu).
3. **Execution:** The student's code and test files (inputs) are mounted into the container. The system compiles the code via GCC and runs the binary.
4. **Validation:** The student's output (generated files or `stdout`) is compared against the official answer key using native tools (e.g., `diff`).
5. **Termination:** The result log is returned to the student on the screen. The container and all residual files are immediately destroyed.

## 🛠️ Tech Stack & Infrastructure
* **Backend Language:** Java (Spring Boot or Javalin) - A strategic choice to facilitate the implementation of object-oriented Design Patterns.
* **Execution Engine:** Docker (orchestrated via the `docker-java` library).
* **Target Compilation:** GCC (GNU Compiler Collection) running in Linux containers.
* **Hosting:** Personal home lab via **Proxmox** (VM or LXC).
* **Network Exposure & Security:** **Cloudflare Tunnels** (`cloudflared`). Ensures secure external access via HTTPS without the need to open residential router ports, mitigating network attacks.
* **Storage:** Stateless (no database). No student data or grades are persisted.

## 🛡️ Security & Resilience
Since this is a public API compiling third-party code, the system implements:
* **Rate Limiting:** Cloudflare rules to prevent the same IP from making dozens of requests per minute.
* **Processing Queue:** A strict limit on the maximum number of containers running simultaneously to avoid exhausting server resources (i5/16GB). Excess requests are placed in a "Waiting" state.
* **Strict Timeouts:** Containers are forcibly terminated ("killed") by the Java API if the C execution exceeds a predetermined time limit (preventing infinite loops and system freezes).
* **Container Limits:** CPU and RAM consumption restrictions enforced directly in the Docker creation configurations.

## 📐 Applicable Design Patterns (CS 680 Focus)
To align with the academic requirements of the CS 680 course, the Java backend will utilize:
* **Facade Pattern:** To abstract the underlying complexity of communicating with the Docker Daemon.
* **Strategy Pattern:** To define different evaluation methods at runtime (e.g., `DiffFileStrategy`, `RegexStdoutStrategy`).
* **Command Pattern:** To encapsulate each student submission as an executable object within the processing queue (Worker Queue).