# École Polytechnique de Montréal Département
# Génie Informatique et Génie Logiciel
# INF3405 – Réseaux Informatiques



# Description Mise en contexte

Vous en avez marre de ne plus avoir d’espace de stockage sur votre compte Dropbox ou Google Drive. À chaque fois que vous essayer de mettre un fichier sur le nuage, les géants de l’infonuagique essaient de vous soutirer de l’argent de vos petites poches pour vous vendre encore plus de stockage. La vie d’étudiant étant rude, et sachant que vous n’avez pas d’argent à donner à ces compagnies, vous choisissez de vous révolter. Étant étudiant à Polytechnique Montréal et surtout, expert en réseau informatique avant même d’avoir fini le cours, vous choisissez de développer votre propre application client-serveur permettant de stocker n’importe quel type de fichier sur un serveur de stockage. Votre grand-mère, une fidèle supporter, vous encourage dans votre quête et vous fait don de son bon vieux Pentium 3. Vous choisissez de l’utiliser comme serveur de stockage.

Pour l’instant, vous prévoyez qu’une simple interface console​, car vous voulez assurer uniquement le fonctionnement de votre application client-serveur et une interface graphique n’est pas une de vos priorités.

 # Requis :
Vous aurez à faire deux produits, un serveur, et un client pour communiquer avec le serveur.

Au démarrage du serveur, celui-ci demande à l’utilisateur d’entrer les informations suivantes : adresse IP du poste sur lequel s’exécute le serveur, port d’écoute (​un port entre 5000 et 5050 uniquement​). Une vérification doit être faite pour s’assurer que l’utilisateur a bien entré des données cohérentes. Par exemple, le serveur doit détecter que l’adresse 7x7.-202.666.888 et le port -9999 sont incohérents. Il devra aussi s’assurer que l’adresse IP entrée est bien sur quatre octets.

Au lancement du client, celui-ci demande à l’utilisateur d’entrer l’adresse IP du serveur, le port du serveur (​entre 5000 et 5050​). Le client doit vérifier la validité du format de l’adresse IP et du port entré par l’utilisateur (de la même façon que le fait le serveur) et afficher une erreur en cas d’invalidité. Ces informations sont utilisées pour tenter de se connecter au serveur. Une fois la connexion établie entre le client et le serveur de stockage, le client peut, ​à tout moment​ lancer une des cinq commande suivante :

# cd
# ls
# upload
# download
# mkdir
# exit

Quand le serveur reçoit une commande la part d’un client, il doit afficher dans sa console les
informations suivantes : [Adresse IP client : Port client - Date et Heure (min, sec)] : <Commande> par exemple :
[132.207.29.​107​:​42975​ - 2018-09-15@13:02:01] : upload allo.docx
[132.207.29.​107​:​42975​ - 2018-09-15@13:02:05] : ls
