# Load Balancer DirkDB (Round-Robin)

## Architecture
```
Client → Load Balancer (port 3949) → Serveur 1 (127.0.0.1:3948)
                                   → Serveur 2 (127.0.0.1:3950)
```

## Prérequis
- 2 instances DirkDB lancées avec réplication des données
- Ports 3948 et 3950 libres

## Démarrage

### 1. Lancer les serveurs DirkDB
```bash
# Terminal 1 - Serveur 1
java -cp target/classes sqlTsinjo.socket.server.ServerSocket

# Terminal 2 - Serveur 2 (modifier le port vers 3950 dans ServerSocket.java)
java -cp target/classes sqlTsinjo.socket.server.ServerSocket
```

### 2. Lancer le load balancer
```bash
# Terminal 3
java -cp target/classes sqlTsinjo.socket.proxy.ProxyCacheServer
```

### 3. Tester
```bash
# Les clients se connectent au port 3949 (load balancer)
telnet localhost 3949
```

## Configuration
Pour changer les IPs/ports des serveurs backend, modifier dans `ProxyCacheServer.java` :
```java
private static final Backend[] BACKENDS = {
    new Backend("IP_SERVEUR_1", PORT_1),
    new Backend("IP_SERVEUR_2", PORT_2)
};
```

## Comportement
- Round-robin par connexion
- Chaque connexion client reste sur le même serveur (sticky implicite)
- Transparent pour le client
- Logs : affiche "Connexion client -> IP:PORT" pour chaque nouvelle connexion

## Réplication
Assure-toi que la réplication est configurée entre les 2 serveurs pour que les données restent cohérentes.
