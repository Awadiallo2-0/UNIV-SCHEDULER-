package com.univ.utils;

import com.univ.model.Reservation;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.io.FileOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PdfUtils {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public static void exporterReservationsVersPDF(List<Reservation> reservations, String cheminFichier) {
        if (reservations == null || reservations.isEmpty()) {
            System.err.println("⚠️ Aucune réservation à exporter.");
            return;
        }

        try {
            PdfWriter writer = new PdfWriter(new FileOutputStream(cheminFichier));
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            Paragraph title = new Paragraph("Rapport des Réservations")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(18)
                    .setBold();
            document.add(title);

            Paragraph date = new Paragraph("Généré le " + 
                    java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setFontSize(10);
            document.add(date);

            document.add(new Paragraph("\n"));

            Table table = new Table(UnitValue.createPercentArray(new float[]{15, 10, 10, 15, 20, 20, 10}))
                    .useAllAvailableWidth();

            String[] headers = {"Date", "Début", "Fin", "Salle", "Cours", "Enseignant", "Type"};
            for (String header : headers) {
                table.addHeaderCell(new Cell().add(new Paragraph(header).setBold()));
            }

            for (Reservation r : reservations) {
                table.addCell(r.getDate() != null ? r.getDate().format(DATE_FORMATTER) : "");
                table.addCell(r.getHeureDebut() != null ? r.getHeureDebut().format(TIME_FORMATTER) : "");
                table.addCell(r.getHeureFin() != null ? r.getHeureFin().format(TIME_FORMATTER) : "");
                table.addCell(r.getSalle() != null ? r.getSalle().getNumero() : "");
                if (r.getCours() != null) {
                    table.addCell(r.getCours().getNom() != null ? r.getCours().getNom() : "");
                    if (r.getCours().getEnseignant() != null) {
                        String nomComplet = r.getCours().getEnseignant().getPrenom() + " " + r.getCours().getEnseignant().getNom();
                        table.addCell(nomComplet);
                    } else {
                        table.addCell("");
                    }
                } else {
                    table.addCell("");
                    table.addCell("");
                }
                table.addCell(r.getType() != null ? r.getType().toString() : "");
            }

            document.add(table);
            document.close();

            System.out.println("✅ Fichier PDF créé : " + cheminFichier);

        } catch (Exception e) {
            System.err.println("❌ Erreur PDF : " + e.getMessage());
            e.printStackTrace();
        }
    }
}