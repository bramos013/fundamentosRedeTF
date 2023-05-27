package roteador.util;

import roteador.Roteador;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileReader {
    public static List<String> readLinesFromFile(String fileName) throws IOException {
        ArrayList<String> lines = new ArrayList<>();

        try (BufferedReader inputFile = new BufferedReader(new java.io.FileReader(fileName))) {
            String line;
            while ((line = inputFile.readLine()) != null) {
                lines.add(line);
            }

            return lines;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Roteador.class.getName()).log(Level.SEVERE, null, ex);
            return Collections.emptyList();
        }
    }
}
