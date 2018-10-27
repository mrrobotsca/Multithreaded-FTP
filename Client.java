import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;


public class Client {

    // Pattern for the IP address
    // Source: https://stackoverflow.com/questions/5667371/validate-ipv4-address-in-java
    private final Pattern PATTERN = Pattern.compile(
            "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

    // Initialisation des parametres

    private static final String runtime = "ftp >";
    private static final String message_sortie = "La session est fini. ByeBye!";
    private Path path;
    List<String> input2;
    Path path2;


    private Client() {
    }

    // Shortcut affichage message

    private void Message(String message) {
        System.out.println(message);

    }


    //Vérifier la validité de l’adresse IP saisie (uniquement le format)

    private boolean ValiderIpAddresse(final String ip) {
        return PATTERN.matcher(ip).matches();

    }
    // Affichage pour les commands entrees errones

    private void invalid() {
        System.out.println("Entree Invalid");
        System.out.println("Besoin d'aide ? Tapez `help' pour plus d'information.");
    }

    //Vérifier la validité de l’adresse IP saisie (uniquement le format) et le numéro de port (entre 5000 et 5050)

    private boolean ValiderPort(final int port) {

        if (port >= 5000 && port <= 5500) {
            return true;
        } else {
            return false;
        }
    }

    // Valider les entre dans la ligne de commande. Au cas ou les entrees sont errone le programme affiche seulment Erreur et continue

    private void ValiderEntree(List inputs, int a) {

        if (inputs.size() < 1 && a == 1) {
            invalid();
        } else if (inputs.size() < 2 && a == 2) {
            invalid();
        }
    }

    //Ping avec le bon ip addresse et tester la connection

    private String pingIp(Boolean yes, String host) {


        while (yes) {

            Message("Veuillez S.V.P Entrer l'address IP Server:");
            Scanner Lecture = new Scanner(System.in);

            host = Lecture.next();


            while (!ValiderIpAddresse(host)) {

                Message("Mauvais IP Address. Essayez de nouveau:");
                host = Lecture.next();

            }

            break;

        }

        return host;

    }

    //Ping avec le bon port et tester la connection

    private int pingPort(Boolean yes, int port) {

        Scanner Lecture = new Scanner(System.in);


        while (yes) {


            Message("Veuillez S.V.P Entrer le port de connection Server:");

            port = Lecture.nextInt();

            while (!ValiderPort(port)) {

                Message("Mauvais Port Address. Essayez de nouveau:");
                port = Lecture.nextInt();

            }
            System.out.println("Laissez Moi Voir Si Je Peux Vous Connecter 0_0 ...");
            break;
        }

        return port;

    }

    // Command put pour televerser les fichiers

    private void upload(DataOutputStream sortieDonnee, List inputs, Path path) throws Exception {

        // Initialisation des parametres locaux
        input2 = inputs;
        path2 = path;

        //Verifie si le path c'est une Dossier ou fichier
        if (!Files.isDirectory(path2.resolve(input2.get(1))) && Files.notExists(path2.resolve(input2.get(1)))) {

            System.out.println(inputs.get(1) + ": Ce ficher n'existe pas !");
        }

        //Transefer du bon fichier
        else {

            //Envoit la command au server
            sortieDonnee.writeBytes("upload " + inputs.get(1) + "\n");
            File fichier = new File(path2.resolve(input2.get(1)).toString());
            long taileFicher = fichier.length();

            //Envoitt la taille du fichier au fichier
            sortieDonnee.writeBytes(taileFicher + "\n");
            Thread.sleep(100);

            byte[] buffer1 = new byte[6000];
            try {
                BufferedInputStream in = new BufferedInputStream(new FileInputStream(fichier));
                int compteur = 0;
                while ((compteur = in.read(buffer1)) > 0)
                    sortieDonnee.write(buffer1, 0, compteur);
                in.close();

                System.out.println("Le fichier a ete transfere avec du succes ! ");
            } catch (Exception e) {
                System.out.println("Une erreur s'est produite lors du transfere fichier : " + inputs.get(1));
            }
        }


    }

    // Command put pour telecharger les fichiers

    private void download(DataOutputStream sortieDonnee, List inputs, BufferedReader Lecture, DataInputStream bitEntree) throws Exception {

        // Initialisation des parametres locaux
        input2 = inputs;


        //Envoit la command au server
        sortieDonnee.writeBytes("download " + input2.get(1) + "\n");

        //Erreur messages
        String get_line;
        if (!(get_line = Lecture.readLine()).equals("")) {
            System.out.println(get_line);
        }

        //Transefer du bon fichier
        long fileSize = Long.parseLong(Lecture.readLine());
        FileOutputStream ficher = new FileOutputStream(new File(input2.get(1)));
        int compteur = 0;
        byte[] buff = new byte[6000];
        long bitRecu = 0;
        while (bitRecu < fileSize) {
            compteur = bitEntree.read(buff);
            ficher.write(buff, 0, compteur);
            bitRecu += compteur;
        }
        ficher.close();


    }

    // Main Menu

    private void connection() {
        // Initialisation des parametres locaux
        String host = null;
        int port = 0;
        Boolean yes = true;
        path = Paths.get(System.getProperty("user.dir"));
        host = pingIp(yes, host);
        port = pingPort(yes, port);

        // Creation de server
        try (Socket socket = new Socket(host, port)) {

            boolean quit = false;

            InputStreamReader ServerEntree = new InputStreamReader(socket.getInputStream());
            BufferedReader Lecture = new BufferedReader(ServerEntree);
            //Donne
            DataInputStream bitEntree = new DataInputStream(socket.getInputStream());
            //Sortie
            OutputStream sortie = socket.getOutputStream();
            DataOutputStream sortieDonnee = new DataOutputStream(sortie);
            //les entree du client
            Scanner input = new Scanner(System.in);
            String command;
            System.out.println(path.getFileName());

            System.out.println("Vous Etes Bien Connecte Au Server");


            do {

                System.out.print(runtime);
                command = input.nextLine();
                command = command.trim();

                //On met tout les parametres dans que les clients entre dans Arrayliste
                List<String> inputs = new ArrayList<String>();
                Scanner tokenize = new Scanner(command);
                //Recoit les commandes
                if (tokenize.hasNext())
                    inputs.add(tokenize.next());
                //Recoit les commandes secondaires
                if (tokenize.hasNext())
                    inputs.add(command.substring(inputs.get(0).length()).trim());
                tokenize.close();

                //Evites les entrees vides ou les "entree " sans donnees
                if (inputs.isEmpty())
                    continue;

                switch (inputs.get(0)) {

                    case "cd":

                        //Verifie les entres
                        ValiderEntree(inputs, 2);

                        //Envoit la commande
                        if (inputs.size() == 1)
                            sortieDonnee.writeBytes("cd" + "\n");
                        else
                            sortieDonnee.writeBytes("cd " + inputs.get(1) + "\n");

                        //Message recu du server
                        String cd_line;
                        if (!(cd_line = Lecture.readLine()).equals(""))
                            System.out.println(cd_line);

                        break;

                    case "ls":

                        //Verifie les entres
                        ValiderEntree(inputs, 1);

                        //Envoit la commande
                        sortieDonnee.writeBytes("ls" + "\n");

                        //Message recu du server
                        String ls_line;
                        while (!(ls_line = Lecture.readLine()).equals(""))
                            System.out.println(ls_line);

                        break;

                    case "mkdir":
                        //Verifie les entres
                        ValiderEntree(inputs, 2);

                        //Envoit la commande
                        sortieDonnee.writeBytes("mkdir " + inputs.get(1) + "\n");

                        //Message recu du server
                        String CreeDoss;
                        if (!(CreeDoss = Lecture.readLine()).equals(""))
                            System.out.println(CreeDoss);

                        break;

                    case "upload":

                        //Verifie les entres
                        ValiderEntree(inputs, 2);

                        //Envoit la commande
                        try {
                            upload(sortieDonnee, inputs, path);
                        } catch (Exception e) {
                            System.out.println("Une Erreur s'est produite , Veuillez ressayez de nouveau ");
                            continue;
                        }

                        break;

                    case "download":

                        //Verifie les entres
                        ValiderEntree(inputs, 2);

                        try {
                            download(sortieDonnee, inputs, Lecture, bitEntree);
                        } catch (Exception e) {
                            System.out.println("Une Erreur s'est produite , Veuillez ressayez de nouveau ");
                            continue;
                        }

                        break;

                    case "quit":

                        //Verifie les entres
                        ValiderEntree(inputs, 1);

                        //Envoit la commande
                        sortieDonnee.writeBytes("quit" + "\n");
                        quit = true;

                        break;
                }

            } while (!quit);

            System.out.println(message_sortie);

        } catch (Exception e) {
            System.out.println("Une erreur est procurre !!!");
        }

    }

    public static void main(String[] args) {

        Client client = new Client();
        client.connection();

    }
}