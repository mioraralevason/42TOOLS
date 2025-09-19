#!/bin/bash

# Aller dans le dossier du projet (adapter le chemin si besoin)
cd "$(dirname "$0")"

# Lancer Spring Boot avec Maven
mvn spring-boot:run
