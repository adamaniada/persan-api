# GUIDE HEBERGEMENT DE LA PLATEFORME

## Pré-requis

### Commands

- sudo apt update
- sudo apt install openjdk-17-jdk
- sudo apt install maven
- sudo apt install postgresql postgresql-contrib

### Configs

- sudo nano /etc/systemd/system/persan-api-api.service
- bash ````
[Unit]
Description=Alliance SMS API Spring Boot Application
After=network.target

[Service]
User=persanAPIUser
Group=persanAPIUser
WorkingDirectory=/var/www/persan-api/persan-api-api/
ExecStart=/usr/bin/java -jar /var/www/persan-api/persan-api-api/persan-api-api-0.0.1-SNAPSHOT.jar
SuccessExitStatus=143
Restart=always
RestartSec=5
Environment="SPRING_PROFILES_ACTIVE=dev"
StandardOutput=append:/var/www/persan-api/persan-api-api/app.log
StandardError=append:/var/www/persan-api/persan-api-api/error.log

### Sécurisez les permissions sur les fichiers de logs

PermissionsStartOnly=true
StandardOutput=file:/var/www/persan-api/persan-api-api/app.log
StandardError=file:/var/www/persan-api/persan-api-api/error.log

[Install]
WantedBy=multi-user.target```

- sudo systemctl daemon-reload
- sudo systemctl start persan-api-api.service
- sudo systemctl enable persan-api-api.service
- sudo systemctl status persan-api-api.service

### CONFIGURATION

```bash
# Générer une paire de clés SSH (à exécuter sur votre machine locale)
ssh-keygen -t rsa -b 4096 -C "adama2mg@gmail.com" -f ./persan_api

# Sur le serveur, créer le répertoire .ssh et définir les permissions
mkdir -p ~/.ssh
chmod 700 ~/.ssh

# Ouvrir le fichier authorized_keys pour édition (ou utiliser cat pour ajouter directement)
nano ~/.ssh/authorized_keys

# Ajouter la clé publique au fichier authorized_keys (remplacez 'persan_api.pub' par le chemin de votre clé publique)
cat persan_api.pub >> ~/.ssh/authorized_keys

# Définir les permissions du fichier authorized_keys
chmod 600 ~/.ssh/authorized_keys

git add . && git commit -m "Commit 1.0.1" && git branch -M main && git push -u origin main
```

```bash
[Unit]
Description=Alliance SMS API Spring Boot Application
After=network.target

[Service]
User=persanAPIUser
Group=persanAPIUser
WorkingDirectory=/var/www/persan-api/persan-api-api/
ExecStart=/usr/bin/java -jar /var/www/persan-api/persan-api-api/persan-api-api-0.0.1-SNAPSHOT.jar
SuccessExitStatus=143
Restart=always
RestartSec=5
Environment="SPRING_PROFILES_ACTIVE=dev"
StandardOutput=append:/var/www/persan-api/persan-api-api/app.log
StandardError=append:/var/www/persan-api/persan-api-api/error.log

### Sécurisez les permissions sur les fichiers de logs

PermissionsStartOnly=true
StandardOutput=file:/var/www/persan-api/persan-api-api/app.log
StandardError=file:/var/www/persan-api/persan-api-api/error.log

[Install]
WantedBy=multi-user.target
```
