# Web services

**Environnement : Linux (bash)**
Sur Windows, les équivalents sont : scripts `.bat` au lieu de `.sh`, `%VAR%` au lieu de `$VAR`,
séparateur de classpath `;` au lieu de `:`, et antislash `\` dans les chemins.

---

## Prérequis communs

```bash
# Debian/Ubuntu : JDK, curl, Node.js, Python
sudo apt update
sudo apt install -y openjdk-17-jdk curl unzip python3 python3-pip nodejs npm
```

| Outil | Utilisé par | Lien |
|---|---|---|
| **OpenJDK 11 ou 17** (`java`, `javac`, `jar`) | SOAP, REST | https://adoptium.net ou paquet `openjdk-17-jdk` |
| **Apache Axis2 1.8.x** (distribution binaire) | SOAP | https://axis.apache.org/axis2/java/core/download.cgi |
| **Apache Tomcat 9** (⚠ pas Tomcat 10/11, voir remarque REST) | REST (Jersey), SOAP optionnel | https://tomcat.apache.org/download-90.cgi (tar.gz) |
| **Restlet 2.4.3 JEE** (`org.restlet.jar`) | REST (variante TP) | zip fourni par l'équipe du cours (lien dans la page du TP Restlet) |
| **Node.js 18+** avec `npm` | GraphQL | https://nodejs.org ou paquets `nodejs npm` |
| **Python 3.8+** avec `pip` | gRPC | préinstallé en général + `python3-pip` |
| **curl** | tests REST/SOAP | paquet `curl` |

Variables d'environnement (à ajouter à la fin de `~/.bashrc`, puis `source ~/.bashrc`) :
```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64        # adapter selon votre JDK
export AXIS2_HOME=$HOME/outils/axis2-1.8.2                 # décompresser le zip Axis2 ici
export CATALINA_HOME=$HOME/outils/apache-tomcat-9.0.85     # décompresser le tar.gz Tomcat ici
```

Rendre les scripts Axis2 exécutables :
```bash
chmod +x $AXIS2_HOME/bin/*.sh
```

---

## 1. SOAP / WSDL (`soap/`)

**Dépendances** : JDK et Apache Axis2. Tomcat n'est pas nécessaire : le script `axis2server.sh`
fourni avec Axis2 suffit. Un navigateur suffit pour les premiers tests.

**Deux services** : `HelloWorld` (approche bottom-up, code d'abord) et `Addition` (approche top-down, WSDL d'abord).

### 1.A - HelloWorld (bottom-up)

1. Créer la structure (depuis le dossier `soap/`) :
```bash
mkdir -p build/hello build/META-INF
cp hello/HelloWorld.java build/hello/
cp services.xml build/META-INF/
```
2. Compiler et empaqueter :
```bash
javac -encoding UTF-8 -d build build/hello/HelloWorld.java
jar -cf HelloWorldService.aar -C build .
```
3. Déployer : copier l'archive dans le dépôt Axis2 :
```bash
cp HelloWorldService.aar $AXIS2_HOME/repository/services/
```
4. Lancer le serveur Axis2 :
```bash
$AXIS2_HOME/bin/axis2server.sh
```
5. Tester dans le navigateur (ou `curl`) :
```
http://localhost:8080/axis2/services/listServices                    <- le service doit apparaître
http://localhost:8080/axis2/services/HelloWorldService/sayHello      <- renvoie "Hello world!"
http://localhost:8080/axis2/services/HelloWorldService?wsdl          <- le contrat généré
```

### 1.B - Addition (top-down, celui du rapport)

1. Générer le squelette serveur à partir du WSDL (depuis le dossier `soap/`) :
```bash
$AXIS2_HOME/bin/wsdl2java.sh -uri Addition.wsdl -p calculette -d adb -s -ss -sd
```
   (`-ss` = squelette serveur, `-sd` = services.xml généré ; le code arrive dans `src/calculette/`)
2. Compléter le squelette `src/calculette/AdditionServiceSkeleton.java` dans la méthode `additionner` :
```java
int a = additionner0.getEntier1();
int b = additionner0.getEntier2();
calculette.AdditionnerResponse resp = new calculette.AdditionnerResponse();
resp.setResultat1(a + b);
return resp;
```
3. Compiler et créer le `.aar` (le `services.xml` généré doit être déplacé dans `META-INF`) :
```bash
javac -encoding UTF-8 -cp "$AXIS2_HOME/lib/*" -d build/classes src/calculette/*.java
mkdir -p build/classes/META-INF
cp resources/services.xml build/classes/META-INF/
jar -cf AdditionService.aar -C build/classes .
```
4. Déployer puis (re)lancer le serveur :
```bash
cp AdditionService.aar $AXIS2_HOME/repository/services/
$AXIS2_HOME/bin/axis2server.sh
```
5. Vérifier le contrat en ligne : `http://localhost:8080/axis2/services/AdditionService?wsdl`
   (on peut aussi tester la requête `RequeteSOAP.xml` avec SoapUI - outil optionnel).
6. Générer et exécuter le client typé `AdditionClient.java` :
```bash
$AXIS2_HOME/bin/wsdl2java.sh -uri http://localhost:8080/axis2/services/AdditionService?wsdl -d adb -p calculette
javac -encoding UTF-8 -cp ".:$AXIS2_HOME/lib/*" -d build calculette/*.java AdditionClient.java
java -cp "build:$AXIS2_HOME/lib/*" AdditionClient
```
Résultat attendu : `Resultat = 42`

---

## 2. REST (`rest/`)

**Dépendances** :
- JDK et **Tomcat 9** pour l'API `users` (Jersey).
  ⚠ **Important** : le code utilise `javax.ws.rs.*` (Java EE). Tomcat 10/11 utilise `jakarta.*` : l'API ne fonctionnera PAS dessus. Prenez Tomcat 9.
- Jersey 2.x, bundle **`jaxrs-ri`** (zip) : https://eclipse-ee4j.github.io/jersey.github.io/download.html → copier **tous les .jar du bundle** dans `rest/lib/` (au minimum : jersey-server, jersey-container-servlet-core, jersey-common, jersey-client, jackson-*, javax.ws.rs-api).
- Pour la variante Restlet : `org.restlet.jar` uniquement (serveur autonome, pas besoin de Tomcat).

### 2.A - API `users` avec Jersey (celle du rapport)

1. Placer les .jar du bundle Jersey dans `rest/lib/`.
2. Compiler et construire l'archive `rest.war` (structure créée par ces commandes) :
```bash
cd rest/
mkdir -p web/WEB-INF/lib web/WEB-INF/classes
cp lib/*.jar web/WEB-INF/lib/
javac -encoding UTF-8 -cp "lib/*" -d web/WEB-INF/classes rest/UsersService.java
cp web.xml web/WEB-INF/
jar -cf rest.war -C web .
```
3. Déployer et démarrer Tomcat :
```bash
cp rest.war $CATALINA_HOME/webapps/
$CATALINA_HOME/bin/startup.sh
```
4. Tester avec les commandes :
```bash
curl -i http://localhost:8080/rest/users/2
curl -i http://localhost:8080/rest/users
curl -i -X POST http://localhost:8080/rest/users \
     -H "Content-Type: application/json" \
     -d '{"nom": "Charlie", "email": "charlie@example.com"}'
curl -i http://localhost:8080/rest/users/99
```
Résultats attendus : `200 OK` avec JSON, `201 Created` + en-tête `Location`, `404 Not Found`.

### 2.B - « Hello world » avec Restlet (variante du TP)

```bash
javac -encoding UTF-8 -cp org.restlet.jar -d classes firstREST/HelloWorld.java
java -cp "classes:org.restlet.jar" firstREST.HelloWorld
```
Puis ouvrir `http://localhost:8182/hello` → renvoie `hello, world` (serveur autonome, port 8182, pas besoin de Tomcat).

---

## 3. GraphQL (`graphql/`)

**Dépendances** : Node.js 18+ et npm. Rien d'autre (les librairies s'installent avec npm).

⚠ Versions à respecter : `express-graphql` ne fonctionne pas avec `graphql@16`+. La commande ci-dessous fixe des versions compatibles.

1. Installer :
```bash
npm init -y
npm install express@4 express-graphql@0.12 graphql@15
```
2. Lancer le serveur :
```bash
node server.js
```
3. Ouvrir `http://localhost:3000/graphql` (interface GraphiQL) et tester les requêtes suivante :
```graphql
{ hello }
{ getUser(id: "2") { nom email } }
```
Réponse attendue pour la 2e : `{"data":{"getUser":{"nom":"Bob","email":"bob@example.com"}}}`

---

## 4. gRPC (`grpc/`)

**Dépendances** : Python 3.8+ et pip.

1. Installer les librairies :
```bash
python3 -m pip install grpcio grpcio-tools
```
   (si besoin : `python3 -m pip install --user grpcio grpcio-tools` sans droits administrateur)
2. Générer le code à partir du contrat :
```bash
python3 -m grpc_tools.protoc -I . --python_out=. --grpc_python_out=. hello.proto
```
   → crée `hello_pb2.py` et `hello_pb2_grpc.py`
3. Lancer le serveur dans un terminal :
```bash
python3 server.py
```
4. Lancer le client dans un autre terminal :
```bash
python3 client.py
```
Résultat attendu : `Client received: Hello, World!`
