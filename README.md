"# Traducteur"  
# 📄 Traducteur Automatique de Documents PDF (Java JEE)

## 📝 Description
Cette application d'ingénierie linguistique est une application web d'entreprise permettant de téléverser un document **PDF en français** et de générer instantanément sa **traduction complète en anglais**, tout en s'efforçant de préserver la structure et la mise en page d'origine. 

Ce projet démontre ma capacité à manipuler des flux de fichiers complexes en Java, à orchestrer des services métier avec Spring, à utiliser un ORM pour la persistance, et à intégrer des API d'Intelligence Artificielle tierces.

### Fonctions clés :
* **Parsing de fichiers :** Extraction automatisée du texte brut et des structures contenus dans les fichiers PDF complexes.
* **Pipeline de Traduction IA :** Connecteur d'API REST robuste pour envoyer et traiter les données textuelles de manière sécurisée via l'API **DeepL**.
* **Persistance & Données :** Gestion de la session utilisateur et historique des traductions via l'ORM **Hibernate**.
* **Reconstruction & Visualisation :** Génération à la volée du nouveau document PDF traduit avec une interface de visualisation intégrée (Servlets / Beans).

## 🛠️ Stack Technique
* **Langage & Architecture :** Java JEE (Jakarta EE)
* **Frameworks :** Spring Beans (Injection de dépendances), JSF / PrimeFaces (Interface Utilisateur)
* **Persistance :** ORM Hibernate, PostgreSQL 
* **Gestionnaire de projet :** Maven (Fichier `pom.xml`)
* **Intégrations :** API REST DeepL Translation

## 🚀 Installation et Lancement
Pour cloner ce dépôt et exécuter le projet Java JEE en local sur votre environnement de développement :

1. **Cloner le projet de traduction :**
```bash
git clone https://github.com
cd Traducteur-PDF-IA
```

2. **Compiler le projet avec Maven :**
```bash
mvn clean package
```

3. **Lancer le serveur Tomcat intégré :**
```bash
mvn tomcat7:run
```
Accédez ensuite à l'application via votre navigateur à l'adresse locale configurée.

