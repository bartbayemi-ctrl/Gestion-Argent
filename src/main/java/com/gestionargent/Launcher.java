package com.gestionargent;

/**
 * Point d'entrée utilisé pour lancer l'application, aussi bien via
 * "mvn javafx:run" que via le JAR exécutable ("java -jar ...").
 *
 * Cette classe NE DOIT PAS hériter de javafx.application.Application :
 * lorsqu'une classe qui hérite d'Application est utilisée directement comme
 * main-class en dehors d'un module Java, le runtime JavaFX refuse de
 * démarrer avec l'erreur "JavaFX runtime components are missing". Passer par
 * cette classe intermédiaire (le "classpath launcher trick") contourne le
 * problème.
 */
public class Launcher {
    public static void main(String[] args) {
        App.main(args);
    }
}
