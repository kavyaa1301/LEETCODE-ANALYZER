package com.leetcode.analyzer.report;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.leetcode.analyzer.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Map;

/**
 * Generates a PDF progress report using iText 5.
 */
public class PDFReportGenerator implements ReportGenerator {

    private static final Logger logger = LoggerFactory.getLogger(PDFReportGenerator.class);
    private static final BaseColor PRIMARY   = new BaseColor(41, 128, 185);
    private static final BaseColor SECONDARY = new BaseColor(52, 73, 94);
    private static final BaseColor LIGHT     = new BaseColor(236, 240, 241);

    /**
     * Generates a PDF progress report at the specified output path.
     *
     * @param user       the user profile to report on
     * @param stats      the user's computed statistics
     * @param outputPath the file path to write to, or null to use a default name
     * @throws IOException if the PDF cannot be created or written
     */
    @Override
    public void generateReport(UserProfile user, Statistics stats, String outputPath) throws IOException {
        String path = (outputPath != null) ? outputPath : "report_" + user.getUsername() + ".pdf";
        logger.info("Generating PDF report to {}", path);

        try {
            Document doc = new Document(PageSize.A4, 50, 50, 60, 60);
            PdfWriter.getInstance(doc, new FileOutputStream(path));
            doc.open();

            // ── Title ──────────────────────────────────────────────────────
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 22, Font.BOLD, PRIMARY);
            Font headFont  = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, SECONDARY);
            Font bodyFont  = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL, BaseColor.BLACK);
            Font smallFont = new Font(Font.FontFamily.HELVETICA, 9,  Font.NORMAL, BaseColor.GRAY);

            Paragraph title = new Paragraph("LeetCode Progress Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            doc.add(title);

            Paragraph sub = new Paragraph("User: " + user.getUsername(), bodyFont);
            sub.setAlignment(Element.ALIGN_CENTER);
            sub.setSpacingAfter(20);
            doc.add(sub);

            // ── Summary table ──────────────────────────────────────────────
            doc.add(new Paragraph("Overview", headFont));
            PdfPTable summary = new PdfPTable(2);
            summary.setWidthPercentage(100);
            summary.setSpacingBefore(10);
            summary.setSpacingAfter(20);

            addRow(summary, "Total Solved",    String.valueOf(stats.getTotalSolved()),   bodyFont);
            addRow(summary, "Total Attempted", String.valueOf(stats.getTotalAttempted()), bodyFont);
            addRow(summary, "Success Rate",    String.format("%.1f%%", stats.getOverallSuccessRate() * 100), bodyFont);
            addRow(summary, "Avg Attempts",    String.format("%.2f", stats.getAverageAttempts()), bodyFont);
            addRow(summary, "Strongest Category", stats.getStrongestCategory() != null ? stats.getStrongestCategory() : "N/A", bodyFont);
            addRow(summary, "Weakest Category",   stats.getWeakestCategory()   != null ? stats.getWeakestCategory()   : "N/A", bodyFont);
            doc.add(summary);

            // ── Difficulty table ───────────────────────────────────────────
            doc.add(new Paragraph("Performance by Difficulty", headFont));
            PdfPTable diffTable = new PdfPTable(2);
            diffTable.setWidthPercentage(60);
            diffTable.setSpacingBefore(10);
            diffTable.setSpacingAfter(20);
            diffTable.setHorizontalAlignment(Element.ALIGN_LEFT);
            Map<Difficulty, Integer> byDiff = stats.getSolvedByDifficulty();
            if (byDiff != null) {
                for (Difficulty d : Difficulty.values()) {
                    addRow(diffTable, d.getDisplayName(), String.valueOf(byDiff.getOrDefault(d, 0)), bodyFont);
                }
            }
            doc.add(diffTable);

            // ── Category table ─────────────────────────────────────────────
            doc.add(new Paragraph("Category Success Rates", headFont));
            PdfPTable catTable = new PdfPTable(3);
            catTable.setWidthPercentage(100);
            catTable.setSpacingBefore(10);

            // Header row
            for (String h : new String[]{"Category", "Solved", "Success Rate"}) {
                PdfPCell cell = new PdfPCell(new Phrase(h, new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, BaseColor.WHITE)));
                cell.setBackgroundColor(PRIMARY);
                cell.setPadding(6);
                catTable.addCell(cell);
            }

            Map<Category, Double> rates = stats.getSuccessRateByCategory();
            Map<Category, Integer> solvedMap = stats.getSolvedByCategory();
            if (rates != null) {
                rates.entrySet().stream()
                     .sorted(Map.Entry.<Category, Double>comparingByValue().reversed())
                     .forEach(e -> {
                         catTable.addCell(new PdfPCell(new Phrase(e.getKey().getDisplayName(), bodyFont)));
                         int cnt = solvedMap != null ? solvedMap.getOrDefault(e.getKey(), 0) : 0;
                         catTable.addCell(new PdfPCell(new Phrase(String.valueOf(cnt), bodyFont)));
                         catTable.addCell(new PdfPCell(new Phrase(String.format("%.1f%%", e.getValue() * 100), bodyFont)));
                     });
            }
            doc.add(catTable);

            // Footer
            Paragraph footer = new Paragraph("Generated: " + java.time.LocalDateTime.now(), smallFont);
            footer.setAlignment(Element.ALIGN_RIGHT);
            footer.setSpacingBefore(30);
            doc.add(footer);

            doc.close();
            logger.info("PDF report saved to {}", path);
            System.out.println("PDF report saved to: " + path);

        } catch (DocumentException e) {
            throw new IOException("Failed to generate PDF: " + e.getMessage(), e);
        }
    }

    private void addRow(PdfPTable table, String key, String value, Font font) {
        PdfPCell keyCell = new PdfPCell(new Phrase(key, font));
        keyCell.setBackgroundColor(LIGHT);
        keyCell.setPadding(5);
        table.addCell(keyCell);

        PdfPCell valCell = new PdfPCell(new Phrase(value, font));
        valCell.setPadding(5);
        table.addCell(valCell);
    }
}
