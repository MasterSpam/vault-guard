# Vault-Guard (Student Programming Project May 2024)
Welcome to ValtGuard, your secure password manager. VaultGuard is a Java-based platform that works like a virtual keychain to securely manage all your passwords. Create and store strong, unique passwords for any online application easily and securely.

## Overview

The `vault_guard` project is the result of a dedicated collaboration between the members of our Team. Our goal was to develop a robust and efficient Password Manager solution that is both secure and user-friendly. The project is written in Java and uses Gradle and Kotlin as a build tool. With a clear structure and well-documented code, we aim to facilitate the maintenance and further development of the project. We hope you enjoy using Vault Guard as much as we enjoyed developing it!


## Insight into the architecture
Our system architecture is designed to ensure a robust, scalable and maintainable software product.

Modularity and reusability:
We achieve a high degree of modularity by strictly separating models and views. The models, which represent the implementation of the logic, have minimal to no insight into the design and control of the views. Similarly, the views and the associated controllers are not informed about the data processing and storage in the models. This enables the entire user interface to be exchanged without any problems.

Extensibility:
We strive for high extensibility by ensuring a good encapsulation and clear division of responsibilities between model and view by applying the observer pattern. We adhere to a clear structure and modularity and keep important, currently unused methods in the code to facilitate future extensions, such as the method for removing listeners.

Data consistency:
By using a central main model, which is responsible for central data management, we can bundle all data relevant to the user in one place. This is also where the data is bundled and handled to be stored in the vault file. This improves transparency, even if it increases the size of the main model

Robustness and error handling:
The continuous use of automated JUnit tests gives a certain robustness and consistency even beyond changes and refactorings.

This revision presents the technical aspects of your system architecture more precisely and clearly, making it easier for readers to understand.



### Class Diagramm
In our class diagram, we have opted for a simple approach with little information. <br>
In this way, we try to prevent information overload and enable the user to familiarise themselves quickly. <br>
For this reason, we have only included the most important data fields and only strong dependencies. <br>
The stand-alone classes therefore only have weak relationships to other classes. <br>
We hope to achieve a good understanding of our classes and their implementation with this high level of abstraction.
![Class Diagramm](./diagrams/Vault-Guard-Class-Diagram.svg)

### Sequence Diagram of Login

[Sequence Diagram of Login](./diagrams/Vault_Guard_Login_Sequenz_Diagramm.drawio.png)

### Login Diagram

[Login Diagram](./diagrams/Vault_Guard_Login.drawio.png)

## Testing concept
[Tesing concept](./documents/Testing_Konzept_Vault_guard.docx)

## Pull request practice
Here are two examples of pull requests that show how our team discusses feedback and integrates it into the project code:

[Pull Request #75](https://github.zhaw.ch/PM2-IT23aWIN-fame-wahl-kars/team2-ctrl_C_ctrl_V-projekt2-vault_guard/pull/75)

[Pull Request #87](https://github.zhaw.ch/PM2-IT23aWIN-fame-wahl-kars/team2-ctrl_C_ctrl_V-projekt2-vault_guard/pull/87)

## Branching model
We use the Gitflow branching model to manage our branches. The main branches are `master` and `development`. The `master` branch contains the latest stable release, while the `development` branch is used for ongoing development. Feature branches are created from the `development` branch and merged back into it once the feature is complete.

[Network graph](https://github.zhaw.ch/PM2-IT23aWIN-fame-wahl-kars/team2-ctrl_C_ctrl_V-projekt2-vault_guard/network)

## Prerequisites and installation

Get started with VaultGuard in just a few steps:

Java 21 or newer must be installed on your system.

### Installation

1. Open a new directory and start a Git Bash console
   ```sh
   Right-click on the directory -> Git Bash here
   ```
2. Clone the repo
   ```sh
   git clone https://github.zhaw.ch/PM2-IT23aWIN-fame-wahl-kars/team2-ctrl_C_ctrl_V-projekt2-vault_guard.git
   ```
3. Switch to the project directory
   ```sh
   cd team2-ctrl_C_ctrl_V-projekt2-vault_guard.git
   ```
4. Start the programme
   ```sh
   gradlew run 
   ```




## Showcase
In this section, we present a predefined example of our program, VaultGuard.
You can just enter those credentials to log in and see how the program works.

Username: MaxMustermann

Password: VaultGuard

Have fun using Vault Guard!

## Contributors

- [@hoferlev](https://github.zhaw.ch/hoferlev)
- [@feuchmor](https://github.zhaw.ch/feuchmor)
- [@wyssjul1](https://github.zhaw.ch/wyssjul1)
- [@kryezleo](https://github.zhaw.ch/kryezleo)

## License

This project is licensed under the [MIT License](LICENSE).

Rediscover security with ValtGuard - your partner in the digital world!



