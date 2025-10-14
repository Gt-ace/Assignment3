# Installing Java Development Kit (JDK) 17

Welcome, aspiring developers! This guide will walk you through the process of installing Java Development Kit (JDK) version 17 on your macOS, Windows, or Linux machine. The JDK is essential for compiling and running Java applications, so let's get you set up!

## Why JDK 17?

JDK 17 is a Long-Term Support (LTS) release, meaning it will receive updates and support for an extended period. It's a stable and widely adopted version, perfect for learning and development.

## General Notes Before You Start

*   **Administrator/Root Privileges:** You will need administrator rights (Windows) or `sudo` privileges (macOS/Linux) to install the JDK and modify system environment variables.
*   **Choose Your Distribution:** While Oracle provides an official JDK, many developers prefer open-source distributions like Adoptium (formerly AdoptOpenJDK) for licensing flexibility and ease of use. This guide will primarily recommend Adoptium's Temurin distribution.
*   **Architecture:** Ensure you download the correct version for your computer's architecture (e.g., x64 for most modern Intel/AMD processors, ARM64/AArch64 for Apple Silicon Macs or some newer Linux devices).

---

## 1. Installation on macOS

### Option A: Using Homebrew (Recommended)

Homebrew is a popular package manager for macOS that simplifies software installation.

1.  **Install Homebrew (if you haven't already):**
    Open your Terminal (`Applications > Utilities > Terminal`) and run:
    ```bash
    /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
    ```
    Follow the on-screen instructions.

2.  **Install JDK 17:**
    Once Homebrew is installed, run:
    ```bash
    brew install openjdk@17
    ```

3.  **Link JDK 17:**
    Homebrew installs JDKs in a specific directory. You'll need to link it to your system's Java Virtual Machines directory. The command output after `brew install` will usually suggest this, but you can run:
    ```bash
    sudo ln -sfn /opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-17.jdk
    ```
    *(Note: For older Intel Macs, `/opt/homebrew/opt/` might be `/usr/local/opt/`)*

4.  **Set `JAVA_HOME` (Optional, but good practice):**
    Many Java applications and build tools rely on the `JAVA_HOME` environment variable.
    Open your shell configuration file (e.g., `~/.zshrc` for Zsh, `~/.bash_profile` or `~/.bashrc` for Bash) in a text editor:
    ```bash
    nano ~/.zshrc # Or ~/.bash_profile
    ```
    Add the following lines to the end of the file:
    ```bash
    export JAVA_HOME=$(/usr/libexec/java_home -v 17)
    export PATH=$JAVA_HOME/bin:$PATH
    ```
    Save the file (Ctrl+O, Enter, Ctrl+X for nano) and then apply the changes:
    ```bash
    source ~/.zshrc # Or source ~/.bash_profile
    ```

### Option B: Manual Installation (DMG Installer)

1.  **Download the JDK 17 Installer:**
    Go to the Adoptium website: [https://adoptium.net/temurin/releases/](https://adoptium.net/temurin/releases/)
    *   Select **Java 17** from the "Version" dropdown.
    *   Select **macOS** from the "Operating System" dropdown.
    *   Choose the **x64** (for Intel Macs) or **AArch64** (for Apple Silicon Macs) installer (usually a `.pkg` or `.dmg` file).

2.  **Run the Installer:**
    *   Locate the downloaded `.pkg` or `.dmg` file in your `Downloads` folder.
    *   Double-click it and follow the on-screen instructions. This is typically a straightforward "Continue" -> "Install" process.

3.  **Set `JAVA_HOME` (Optional, but good practice):**
    Open your Terminal and edit your shell configuration file (e.g., `~/.zshrc` or `~/.bash_profile`):
    ```bash
    nano ~/.zshrc # Or ~/.bash_profile
    ```
    Add the following lines:
    ```bash
    export JAVA_HOME=$(/usr/libexec/java_home -v 17)
    export PATH=$JAVA_HOME/bin:$PATH
    ```
    Save and apply the changes:
    ```bash
    source ~/.zshrc # Or source ~/.bash_profile
    ```

---

## 2. Installation on Windows

### Option A: Using the Installer (Recommended)

1.  **Download the JDK 17 Installer:**
    Go to the Adoptium website: [https://adoptium.net/temurin/releases/](https://adoptium.net/temurin/releases/)
    *   Select **Java 17** from the "Version" dropdown.
    *   Select **Windows** from the "Operating System" dropdown.
    *   Choose the **x64** installer (usually a `.msi` or `.exe` file).

2.  **Run the Installer:**
    *   Locate the downloaded `.msi` or `.exe` file in your `Downloads` folder.
    *   Double-click it.
    *   Follow the installation wizard. Crucially, ensure you select the options to:
        *   "Set JAVA_HOME environment variable"
        *   "Add to PATH"
        (Adoptium installers usually offer these options directly, making setup much easier!)

3.  **Verify Environment Variables (Optional, if installer handled it):**
    *   Search for "Environment Variables" in the Windows search bar and select "Edit the system environment variables."
    *   Click the "Environment Variables..." button.
    *   Under "System variables," check for `JAVA_HOME`. Its value should point to your JDK 17 installation directory (e.g., `C:\Program Files\Eclipse Adoptium\jdk-17.0.x.x-hotspot`).
    *   Also, check the `Path` variable. It should contain an entry like `%JAVA_HOME%\bin` or the direct path to your JDK's `bin` directory.

### Option B: Manual Installation (ZIP Archive) and Manual Environment Variables

This method is for advanced users or if you prefer not to use an installer.

1.  **Download the JDK 17 ZIP Archive:**
    Go to the Adoptium website: [https://adoptium.net/temurin/releases/](https://adoptium.net/temurin/releases/)
    *   Select **Java 17** from the "Version" dropdown.
    *   Select **Windows** from the "Operating System" dropdown.
    *   Choose the **x64** archive (usually a `.zip` file).

2.  **Extract the Archive:**
    *   Extract the contents of the downloaded `.zip` file to a stable location, e.g., `C:\Program Files\Java\jdk-17`. You'll end up with a folder like `C:\Program Files\Java\jdk-17.0.x.x-hotspot`.

3.  **Set `JAVA_HOME` Environment Variable:**
    *   Search for "Environment Variables" in the Windows search bar and select "Edit the system environment variables."
    *   Click the "Environment Variables..." button.
    *   Under "System variables," click "New...".
    *   For "Variable name," enter `JAVA_HOME`.
    *   For "Variable value," enter the path to your JDK installation directory (e.g., `C:\Program Files\Java\jdk-17.0.x.x-hotspot`). Click "OK."

4.  **Add JDK to `Path` Environment Variable:**
    *   In the "System variables" section, find the `Path` variable and select it.
    *   Click "Edit...".
    *   Click "New" and add `%JAVA_HOME%\bin`.
    *   Click "OK" on all open windows to save the changes.

---

## 3. Installation on Linux

### Option A: Using Your Distribution's Package Manager (Recommended)

This is the easiest and most secure way to install JDK on Linux, as it handles updates and dependencies automatically.

#### For Debian/Ubuntu (and derivatives):

1.  **Update package list:**
    ```bash
    sudo apt update
    ```

2.  **Install OpenJDK 17:**
    ```bash
    sudo apt install openjdk-17-jdk
    ```

3.  **Set Default Java Version (if multiple JDKs are installed):**
    ```bash
    sudo update-alternatives --config java
    sudo update-alternatives --config javac
    ```
    Follow the prompts to select JDK 17.

#### For Fedora/CentOS/RHEL (and derivatives):

1.  **Install OpenJDK 17:**
    ```bash
    sudo dnf install java-17-openjdk-devel # For Fedora/RHEL 8+
    # Or for older CentOS/RHEL: sudo yum install java-17-openjdk-devel
    ```

2.  **Set Default Java Version (if multiple JDKs are installed):**
    ```bash
    sudo alternatives --config java
    sudo alternatives --config javac
    ```
    Follow the prompts to select JDK 17.

### Option B: Manual Installation (Tarball)

1.  **Download the JDK 17 Tarball:**
    Go to the Adoptium website: [https://adoptium.net/temurin/releases/](https://adoptium.net/temurin/releases/)
    *   Select **Java 17** from the "Version" dropdown.
    *   Select **Linux** from the "Operating System" dropdown.
    *   Choose the **x64** or **AArch64** archive (usually a `.tar.gz` file).

2.  **Extract the Archive:**
    *   Create a directory for Java installations (e.g., `/opt/java`):
        ```bash
        sudo mkdir -p /opt/java
        ```
    *   Move the downloaded `.tar.gz` file to this directory and extract it:
        ```bash
        sudo mv ~/Downloads/OpenJDK17U-jdk_x64_linux_hotspot_17.0.x_x.tar.gz /opt/java/
        cd /opt/java
        sudo tar -xzf OpenJDK17U-jdk_x64_linux_hotspot_17.0.x_x.tar.gz
        ```
        *(Replace `OpenJDK17U-jdk_x64_linux_hotspot_17.0.x_x.tar.gz` with the actual filename)*
    *   Rename the extracted directory for simplicity (optional, but good practice):
        ```bash
        sudo mv jdk-17.0.x.x-hotspot jdk-17
        ```

3.  **Set `JAVA_HOME` and Update `PATH`:**
    Open your shell configuration file (e.g., `~/.bashrc`, `~/.zshrc`, or `/etc/profile` for system-wide) in a text editor:
    ```bash
    nano ~/.bashrc # Or ~/.zshrc
    ```
    Add the following lines to the end of the file:
    ```bash
    export JAVA_HOME=/opt/java/jdk-17
    export PATH=$JAVA_HOME/bin:$PATH
    ```
    Save the file and then apply the changes:
    ```bash
    source ~/.bashrc # Or source ~/.zshrc
    ```

4.  **Update System Alternatives (Optional, but recommended for system-wide use):**
    This step helps your system recognize the manually installed JDK as a valid Java option.
    ```bash
    sudo update-alternatives --install "/usr/bin/java" "java" "/opt/java/jdk-17/bin/java" 1
    sudo update-alternatives --install "/usr/bin/javac" "javac" "/opt/java/jdk-17/bin/javac" 1
    sudo update-alternatives --set java /opt/java/jdk-17/bin/java
    sudo update-alternatives --set javac /opt/java/jdk-17/bin/javac
    ```

---

## 4. Verify Your Installation

After following the steps for your operating system, open a **new** terminal or command prompt window (this is important to ensure environment variables are loaded) and run the following commands:

1.  **Check Java Runtime Environment (JRE) version:**
    ```bash
    java -version
    ```
    You should see output similar to this, indicating version 17:
    ```
    openjdk version "17.0.x" 202x-xx-xx
    OpenJDK Runtime Environment Temurin-17.0.x+x (build 17.0.x+x)
    OpenJDK 64-Bit Server VM Temurin-17.0.x+x (build 17.0.x+x, mixed mode, sharing)
    ```

2.  **Check Java Compiler (Javac) version:**
    ```bash
    javac -version
    ```
    You should see:
    ```
    javac 17.0.x
    ```

3.  **Check `JAVA_HOME` (if you set it):**
    *   **macOS/Linux:**
        ```bash
        echo $JAVA_HOME
        ```
    *   **Windows (Command Prompt):**
        ```cmd
        echo %JAVA_HOME%
        ```
    *   **Windows (PowerShell):**
        ```powershell
        $env:JAVA_HOME
        ```
    This should output the path to your JDK 17 installation directory.

If all these commands show version 17 and the correct `JAVA_HOME` path, congratulations! You've successfully installed Java JDK 17!

---

## Troubleshooting Tips

*   **"command not found" or wrong version:**
    *   Make sure you opened a *new* terminal/command prompt after setting environment variables.
    *   Double-check your `PATH` and `JAVA_HOME` variables for typos.
    *   For Linux/macOS, ensure you `source` your shell configuration file (e.g., `source ~/.bashrc`).
*   **Permissions issues:** Ensure you used `sudo` on Linux/macOS or ran the installer as an administrator on Windows.
*   **Multiple Java versions:** If you have multiple JDKs, ensure your `PATH` and `JAVA_HOME` point to JDK 17, or use `update-alternatives` (Linux) or `java_home -v` (macOS) to manage them.

If you encounter any issues, don't hesitate to reach out to your instructor or classmates for assistance. Happy coding!