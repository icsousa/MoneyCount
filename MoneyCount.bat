@echo off
REM Muda para a pasta onde este ficheiro está (para não dar erro de caminhos)
cd /d "%~dp0"

REM Compila o código (opcional, se já tiveres a pasta 'out' compilada, podes apagar esta linha)
javac --module-path "resources\javafx-sdk-25\lib" --add-modules javafx.controls,javafx.fxml,javafx.swing -d out src\*.java

REM Executa o programa
REM 'javaw' (com w) serve para não ficar uma janela preta aberta atrás do programa
start "" javaw --module-path "resources\javafx-sdk-25\lib" --add-modules javafx.controls,javafx.fxml,javafx.swing -cp out Main