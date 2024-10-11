# Airport Streaming App

## Description

L'**Airport Streaming App** est une application Java qui utilise Apache Spark Streaming pour récupérer des données de vol en temps réel depuis l'API OpenSky Network. Les données sont ensuite stockées dans un serveur MinIO. L'application est conçue pour effectuer des appels API toutes les 2 heures à partir de juin 2024.

## Technologies Utilisées

- **Java**: Langage de programmation principal pour le développement de l'application.
- **Apache Spark Streaming**: Utilisé pour le traitement des flux de données en temps réel.
- **MinIO**: Serveur de stockage d'objets compatible avec Amazon S3, utilisé pour stocker les données récupérées.
- **Apache HttpClient**: Utilisé pour effectuer des requêtes HTTP vers l'API OpenSky Network.
- **Jackson**: Bibliothèque pour le traitement des données JSON.
- **SLF4J**: API de logging utilisée pour enregistrer les informations et les erreurs.

## Fonctionnalités

- Récupération des données de vol toutes les 2 heures.
- Stockage des données JSON dans un serveur MinIO.
- Configuration de l'année de début via un paramètre système.

## Prérequis

- **Java 8** ou supérieur
- **Apache Spark** installé localement
- **MinIO** installé et configuré localement
- Accès à l'API OpenSky Network

## Installation et Exécution en Local

### Étape 1: Cloner le dépôt

```console
git clone https://github.com/votre-utilisateur/airport-streaming-app.git
cd airport-streaming-app
```

### Étape 2: Configurer MinIO

1. Téléchargez et installez MinIO depuis [leur site officiel](https://min.io/download).
2. Démarrez le serveur MinIO:

```console
minio server /data
```

3. Configurez les identifiants d'accès (par défaut: `minioadmin:minioadmin`).

### Étape 3: Configurer Spark

Assurez-vous que Spark est installé et configuré correctement. Vous pouvez télécharger Spark depuis [leur site officiel](https://spark.apache.org/downloads.html).

### Étape 4: Exécuter l'application

Compilez et exécutez l'application avec Maven ou votre IDE préféré. Assurez-vous de passer l'année de début en tant que paramètre système:

```console
mvn clean install
java -Dapi.start.year=2024 -jar target/airport-streaming-app.jar
```

### Étape 5: Vérifier les logs

Les logs de l'application fourniront des informations sur le statut des appels API et le stockage des données dans MinIO.

## Configuration

- **API_URL**: URL de l'API OpenSky Network.
- **MINIO_BUCKET**: Nom du bucket MinIO où les données seront stockées.
- **api.start.year**: Année de début pour les appels API, configurable via un paramètre système.

## Contribuer

Les contributions sont les bienvenues! Veuillez soumettre une pull request ou ouvrir une issue pour discuter des changements que vous souhaitez apporter.

## Licence

Ce projet est sous licence MIT. Voir le fichier [LICENSE](LICENSE) pour plus de détails.