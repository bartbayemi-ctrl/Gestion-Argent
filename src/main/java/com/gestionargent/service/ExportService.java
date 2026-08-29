package com.gestionargent.service;

import com.gestionargent.model.Transaction;
import com.gestionargent.model.TypeTransaction;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Génère des rapports PDF et Excel à partir d'une liste de transactions.
 */
public class ExportService {

    private static final DateTimeFormatter FORMAT_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public void exporterPdf(List<Transaction> transactions, String titre, File fichier) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);
            PDType1Font policeTitre = PDType1Font.HELVETICA_BOLD;
            PDType1Font policeTexte = PDType1Font.HELVETICA;

            float margeGauche = 50;
            float y = 780;
            float hauteurLigne = 16;

            PDPageContentStream cs = new PDPageContentStream(document, page);
            cs.beginText();
            cs.setFont(policeTitre, 16);
            cs.newLineAtOffset(margeGauche, y);
            cs.showText(titre);
            cs.endText();
            y -= 30;

            double solde = 0;
            cs.setFont(policeTexte, 10);

            for (Transaction t : transactions) {
                if (y < 60) {
                    cs.close();
                    page = new PDPage();
                    document.addPage(page);
                    cs = new PDPageContentStream(document, page);
                    cs.setFont(policeTexte, 10);
                    y = 780;
                }
                double signe = t.getType() == TypeTransaction.DEPENSE ? -t.getMontant() : t.getMontant();
                solde += signe;
                String ligne = String.format(Locale.FRANCE, "%-12s %-25s %-15s %10.2f EUR",
                    t.getDate().format(FORMAT_DATE),
                    tronquer(t.getDescription(), 25),
                    tronquer(t.getCategorie().getNom(), 15),
                    signe);

                cs.beginText();
                cs.newLineAtOffset(margeGauche, y);
                cs.showText(ligne);
                cs.endText();
                y -= hauteurLigne;
            }

            y -= 10;
            cs.beginText();
            cs.setFont(policeTitre, 11);
            cs.newLineAtOffset(margeGauche, y);
            cs.showText(String.format(Locale.FRANCE, "Solde total : %.2f EUR", solde));
            cs.endText();
            cs.close();

            document.save(fichier);
        }
    }

    public void exporterExcel(List<Transaction> transactions, File fichier) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet feuille = workbook.createSheet("Transactions");

            CellStyle styleEntete = workbook.createCellStyle();
            Font policeEntete = workbook.createFont();
            policeEntete.setBold(true);
            styleEntete.setFont(policeEntete);

            Row entete = feuille.createRow(0);
            String[] colonnes = {"Date", "Description", "Catégorie", "Type", "Montant (EUR)"};
            for (int i = 0; i < colonnes.length; i++) {
                Cell cell = entete.createCell(i);
                cell.setCellValue(colonnes[i]);
                cell.setCellStyle(styleEntete);
            }

            int ligneIndex = 1;
            double solde = 0;
            for (Transaction t : transactions) {
                Row ligne = feuille.createRow(ligneIndex++);
                double signe = t.getType() == TypeTransaction.DEPENSE ? -t.getMontant() : t.getMontant();
                solde += signe;

                ligne.createCell(0).setCellValue(t.getDate().format(FORMAT_DATE));
                ligne.createCell(1).setCellValue(t.getDescription());
                ligne.createCell(2).setCellValue(t.getCategorie().getNom());
                ligne.createCell(3).setCellValue(t.getType().getLibelle());
                ligne.createCell(4).setCellValue(signe);
            }

            Row ligneSolde = feuille.createRow(ligneIndex + 1);
            ligneSolde.createCell(3).setCellValue("Solde total");
            ligneSolde.createCell(4).setCellValue(solde);

            for (int i = 0; i < colonnes.length; i++) {
                feuille.autoSizeColumn(i);
            }

            try (FileOutputStream fos = new FileOutputStream(fichier)) {
                workbook.write(fos);
            }
        }
    }

    private String tronquer(String texte, int longueurMax) {
        if (texte == null) return "";
        return texte.length() <= longueurMax ? texte : texte.substring(0, longueurMax - 1) + ".";
    }
}
