#!/bin/bash

# Garante que o script corre na pasta onde está o ficheiro, 
# mesmo que o chames de outro sítio
cd "$(dirname "$0")"

# O teu comando de execução
java --module-path resources/javafx-sdk-25/lib \
     --add-modules javafx.controls,javafx.fxml,javafx.swing \
     -cp out Main