# <img src="https://github.com/user-attachments/assets/857e686c-be4d-4822-b07c-8fb8ab6197aa" width="20px" /> MoneyCount
A JavaFX application designed for money counting and management.

## 📋 Prerequisites

To run this project, you must have the following installed:

* **Java JDK 25** (or higher).
* **JavaFX SDK 25**:
    * The library must be placed manually inside the `resources` folder.
    * ⚠️ **Important:** The JavaFX version depends on the Operating System.
        * **Linux**: Place the Linux SDK inside `resources/javafx-sdk-25`.
        * **Windows**: Place the Windows SDK inside `resources/javafx-sdk-25`.

## 📂 Project Structure

| File/Folder | Description |
| :--- | :--- |
| `src/` | Application source code (`.java`). |
| `resources/` | External libraries (JavaFX SDK). |
| `out/` | Compiled files (`.class`). |
| `doc/` | Project documentation. |
| `diagram/` | Diagrams and schemes (UML/Architecture). |
| `MoneyCount.sh` | Execution script for **Linux**. |
| `MoneyCount.bat` | Execution script for **Windows**. |

---

## 🚀 How to Run

### 🐧 Linux

1. Ensure the **Linux** JavaFX SDK is inside the `resources` folder.
2. Grant execution permissions to the script (only needed once):
   ```bash
   chmod +x MoneyCount.sh
   ```
3. Run the application:
   ```bash
   ./MoneyCount.sh
   ```

### 🪟 Windows

1. Ensure the Windows JavaFX SDK is inside the `resources` folder (if you downloaded the project on Linux, you must replace the `javafx-sdk-25` folder with the Windows version).

2. Simply double-click the file:
   ```dos
   MoneyCount.bat
   ```

---

## 🛠️ Manual Commands (Development)

If you need to compile or run manually via terminal without using the automatic scripts:

### 🐧 Linux / Mac
**Compile:**
```bash
javac --module-path resources/javafx-sdk-25/lib \
      --add-modules javafx.controls,javafx.fxml,javafx.swing \
      -d out $(find src -name "*.java")
```

**Run:**
```bash
java --module-path resources/javafx-sdk-25/lib \
     --add-modules javafx.controls,javafx.fxml,javafx.swing \
     -cp out Main
```

### 🪟 Windows (PowerShell)
**Compile:**
```powershell
javac --module-path "resources/javafx-sdk-25/lib" --add-modules javafx.controls,javafx.fxml,javafx.swing -d out (Get-ChildItem -Path src -Recurse -Filter *.java).FullName
```

**Run:**
```powershell
java --module-path "resources/javafx-sdk-25/lib" --add-modules javafx.controls,javafx.fxml,javafx.swing -cp out Main
```


