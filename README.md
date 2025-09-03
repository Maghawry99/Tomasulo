# Tomasulo’s Algorithm Simulator

Java implementation of Tomasulo’s algorithm for instruction scheduling and out-of-order execution.  
Built as part of the Computer Architecture course at GUC.

---

## Features
- Issue, execute, and write-back stages
- Reservation stations for instructions
- Register renaming to handle data hazards
- Integer and floating-point execution units with basic latency
- Sample input provided in `instructions.txt`

---

## Tech
- Java 17
- Object-oriented design (classes for instructions, registers, reservation stations, caches)

---

## Project Structure
- `App.java` – main program
- `Instruction.java` – defines operations
- `Reservation.java` – reservation stations
- `Register.java` / `FileReg.java` – register file
- `IntCache.java` / `FloatCache.java` – execution units
- `instructions.txt` – example input




