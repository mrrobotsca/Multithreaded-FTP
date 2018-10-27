import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.FileAlreadyExistsException;
import java.util.Scanner;
import java.io.BufferedInputStream;
import java.io.FileInputStream;


public class Server {


    public static void main(String[] args) {

        ServerSocket server = null;
        try {
            server = new ServerSocket(5050);
            server.setReuseAddress(true);
            // The main thread is just accepting new connections
            while (true) {
                Socket client = server.accept();

                System.out.println("Nouveau Client S'est Connecte " + client.getInetAddress().getHostAddress() + ":" + client.getPort());

                ClientHandler clientSock = new ClientHandler(client);
                new Thread(clientSock).start();

            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (server != null) {
                try {
                    server.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Gestion des clients

    private static class ClientHandler implements Runnable {
        //Initialisation parametres
        private final Socket clientSocket;
        private Path path;
        List<String> input2 = null;
        String command2 = "";
        Scanner tokenize2 = null;
        Path path2;


        ClientHandler(Socket socket) {
            //Initialisation parametres
            this.clientSocket = socket;
            path = Paths.get(System.getProperty("user.dir"));


        }

        // Commande permettant d’afficher à l’utilisateur tous
        //les dossiers et fichiers dans le répertoire courant de
        //l’utilisateur au niveau du serveur.

        private void ls(DataOutputStream sortieDonnee, Path path) throws Exception {

            try {
                File[] fichierEcout = new File(path.toString()).listFiles();
                for (File entry : fichierEcout) {

                    if (entry.isFile()) {

                        sortieDonnee.writeBytes("[Ficher ]  ");
                        sortieDonnee.writeBytes(entry.getName() + "\n");
                    } else if (entry.isDirectory()) {
                        sortieDonnee.writeBytes("[Dossier]  ");
                        sortieDonnee.writeBytes(entry.getName() + "\n");
                    } else {
                        sortieDonnee.writeBytes("[ Autre ]  ");
                        sortieDonnee.writeBytes(entry.getName() + "\n");
                    }
                }
                sortieDonnee.writeBytes("\n");

            } catch (IOException e) {
                e.printStackTrace();
            }


        }

        // Commande permettant la création d’un dossier au
        //niveau du serveur de stockage.

        private void mkdir(DataOutputStream sortieDonnee, List input, Path path) throws Exception {

            //Initialisation parametres local
            input2 = input;
            path2 = path;

            try {
                Files.createDirectory(path2.resolve(input2.get(1)));
                sortieDonnee.writeBytes("le dossier " + input2.get(1) + "a ete cree" + "\n");
            } catch (FileAlreadyExistsException c) {
                sortieDonnee.writeBytes("mkdir: Impossible de creer le dossier `" + input2.get(1) + "': Dossier existe deja" + "\n");

            }


        }

        //Commande permettant à l’utilisateur de se déplacer
        //vers un répertoire enfant ou parent

        private Path cd(DataOutputStream sortieDonnee, List input, Path path) throws Exception {
            //Initialisation parametres local
            input2 = input;
            path2 = path;

            try {
                //cd
                if (input2.size() == 1) {

                    sortieDonnee.writeBytes("vous etes dans le dossier " + path2.getFileName() + "\n");

                }
                //cd ..
                else if (input2.get(1).equals("..")) {
                    if (path2.getParent() != null) {
                        path2 = path2.getParent();

                    }
                    sortieDonnee.writeBytes("Vous etes dans le dossier " + path2.getFileName() + "\n");

                }
                //cd direction
                else {
                    //verifier si le fichier ou dossier existe ou pas
                    if (Files.notExists(path.resolve(input2.get(1)))) {
                        sortieDonnee.writeBytes(input2.get(1) + " : Ce Dossier n'existe pas" + "\n");
                    }
                    //verifier si c'est une repertoir ou pas
                    else if (Files.isDirectory(path2.resolve(input2.get(1)))) {
                        path2 = path2.resolve(input2.get(1));
                        sortieDonnee.writeBytes("Vous etes dans le dossier " + path2.getFileName() + "\n");
                    }
                    //verifier si c'est une fichier
                    else {
                        sortieDonnee.writeBytes("cd: " + input2.get(1) + ": Ceci n'est pas une dossier" + "\n");
                    }
                }
            } catch (Exception e) {
                sortieDonnee.writeBytes("cd: " + input2.get(1) + ": Une erreur s'est produite ! Ressayez de nouveau" + "\n");
            }

            return path2;

        }

        //Commande permettant le téléversement d’un
        //fichier, se trouvant dans le répertoire locale du
        //client, vers le serveur de stockage

        private void upload(BufferedReader Lecture, List input, DataInputStream bitEntree, Path path) throws Exception {

            //Initialisation parametres local
            input2 = input;
            path2 = path;


            long fileSize = Long.parseLong(Lecture.readLine());
            FileOutputStream fichier = new FileOutputStream(new File(path2.resolve(input2.get(1)).toString()));
            int compteur = 0;
            byte[] buff = new byte[6000];
            long bitRecu = 0;
            while (bitRecu < fileSize) {
                compteur = bitEntree.read(buff);
                fichier.write(buff, 0, compteur);
                bitRecu += compteur;
            }
            fichier.close();


        }

        //Commande permettant le téléchargement d’un
        //fichier, se trouvant dans le répertoire courant de
        //l’utilisateur au niveau du serveur de stockage, vers
        //le répertoire local du client.

        private void dowload(DataOutputStream sortieDonnee, List input, Path path) throws Exception {

            //Initialisation parametres local
            input2 = input;
            path2 = path;
            //verifier si le fichier ou dossier existe ou pas
            if (Files.notExists(path2.resolve(input2.get(1)))) {
                sortieDonnee.writeBytes("dowload: " + input2.get(1) + ": Ce Dossier n'existe pas" + "\n");
            }
            //verifier si c'est une repertoir ou pas
            else if (Files.isDirectory(path2.resolve(input2.get(1)))) {
                sortieDonnee.writeBytes("dowload: " + input2.get(1) + ": Ceci est une dossier" + "\n");
            }
            //transfer du fichier
            else {

                sortieDonnee.writeBytes("\n");

                File fichier = new File(path2.resolve(input2.get(1)).toString());
                long fileSize = fichier.length();

                //Envoit la taille du fichier 
                sortieDonnee.writeBytes(fileSize + "\n");
                Thread.sleep(100);

                byte[] buff = new byte[6000];
                try {
                    BufferedInputStream in = new BufferedInputStream(new FileInputStream(fichier));
                    int compteur = 0;
                    while ((compteur = in.read(buff)) > 0)
                        sortieDonnee.write(buff, 0, compteur);


                    in.close();
                } catch (Exception e) {
                    System.out.println("transfer error: " + input2.get(1));
                }
            }


        }

        // Message affiche sur le server apres chaque activite accepte du client 

        private void message(List input, String command, Scanner tokenize) {
            input2 = input;
            command2 = command;
            tokenize2 = tokenize;


            if (tokenize2.hasNext())
                input2.add(tokenize2.next());
            //recoit le 2em commande

            if (tokenize2.hasNext())
                input2.add(command2.substring(input2.get(0).length()).trim());
            tokenize2.close();
            System.out.print("[" + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort() + "]" + " Message recu du client: ");
            for (String s : input2)
                System.out.print(s + " ");
            System.out.println();

        }


        @Override
        public void run() {
            try {


                //Input
                InputStreamReader ClientEntree = new InputStreamReader(clientSocket.getInputStream());
                BufferedReader Lecture = new BufferedReader(ClientEntree);
                //Data
                DataInputStream bitEntree = new DataInputStream(clientSocket.getInputStream());
                //Output
                OutputStream sortie = clientSocket.getOutputStream();
                DataOutputStream sortieDonnee = new DataOutputStream(sortie);

                //main loop
                exitThread:
                while (true) {
                    try {
                        //Recheck chaque 10 millis pour eviter usage des ressources
                        while (!Lecture.ready())
                            Thread.sleep(10);

                        //Afficahge des entres des clients

                        List<String> input = new ArrayList<>();
                        String command = Lecture.readLine();
                        Scanner tokenize = new Scanner(command);

                        message(input, command, tokenize);

                        //selection des commande
                        switch (input.get(0)) {

                            case "ls":

                                ls(sortieDonnee, path);

                                break;

                            case "mkdir": //complet

                                mkdir(sortieDonnee, input, path);

                                break;

                            case "cd": //complete

                                path = cd(sortieDonnee, input, path);

                                break;

                            case "upload":

                                try {
                                    upload(Lecture, input, bitEntree, path);
                                } catch (Exception e) {
                                    System.out.println("Une erreur s'est produite lor du tranfer");
                                }

                                break;
                            case "download":

                                try {
                                    dowload(sortieDonnee, input, path);
                                } catch (Exception e) {

                                    System.out.println("Une erreur s'est produite lor du tranfer");

                                }
                                break;

                            case "quit": //complete

                                //Fermeture des sockets
                                clientSocket.close();

                                System.out.println("Le Client [" + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort() + "] a deconecte");

                                //Sortie du While loop
                                break exitThread;
                            default:
                                System.out.println("Erreur");
                                break;
                        }
                    } catch (Exception e) {
                        System.out.println("Erreur dans notre loup");
                        System.out.println(Thread.activeCount());
                        break;
                    }
                }

            } catch (Exception e) {
                System.out.println("Le system a recu une erreur lor de demarrage");
            } finally {
                try {

                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}