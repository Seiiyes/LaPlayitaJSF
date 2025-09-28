package service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import model.Cliente;
import model.DetalleVenta;
import model.Producto;
import model.Venta;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimplePdfExporterConfiguration;

import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FacturaService implements Serializable {

    /**
     * Genera un PDF de factura usando iText directamente.
     * @param venta La venta registrada
     * @param detalles Los detalles de la venta
     * @param cliente El cliente de la venta
     * @param productosDelCarrito Un mapa para buscar los nombres y precios de los productos
     * @return Un byte array con el contenido del PDF
     * @throws DocumentException Si ocurre un error con iText
     * @throws IOException Si no se encuentra el logo
     */
    public byte[] generarFacturaConIText(Venta venta, List<DetalleVenta> detalles, Cliente cliente, Map<Integer, Producto> productosDelCarrito) throws DocumentException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, baos);

        document.open();

        // Fuentes
        Font fontTitulo = new Font(Font.HELVETICA, 18, Font.BOLD);
        Font fontSubtitulo = new Font(Font.HELVETICA, 12, Font.BOLD);
        Font fontNormal = new Font(Font.HELVETICA, 10, Font.NORMAL);
        Font fontBold = new Font(Font.HELVETICA, 10, Font.BOLD);

        // Logo
        try {
            ServletContext servletContext = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
            URL logoUrl = servletContext.getResource("/resources/img/la-playita-logo.png");
            if (logoUrl != null) {
                Image logo = Image.getInstance(logoUrl);
                logo.scaleToFit(100, 100);
                logo.setAlignment(Image.ALIGN_LEFT);
                document.add(logo);
            }
        } catch (Exception e) {
            System.err.println("Logo no encontrado, continuando sin él.");
        }

        // Título
        Paragraph titulo = new Paragraph("Factura de Venta", fontTitulo);
        titulo.setAlignment(Paragraph.ALIGN_CENTER);
        titulo.setSpacingAfter(20);
        document.add(titulo);

        // Datos del Cliente y Factura
        PdfPTable tablaDatos = new PdfPTable(2);
        tablaDatos.setWidthPercentage(100);
        tablaDatos.setSpacingAfter(20);

        PdfPCell cellCliente = new PdfPCell();
        cellCliente.setBorder(PdfPCell.NO_BORDER);
        cellCliente.addElement(new Paragraph("Cliente:", fontSubtitulo));
        cellCliente.addElement(new Paragraph(cliente.getNombres() + " " + cliente.getApellidos(), fontNormal));
        cellCliente.addElement(new Paragraph("Documento: " + cliente.getDocumento(), fontNormal));
        tablaDatos.addCell(cellCliente);

        PdfPCell cellFactura = new PdfPCell();
        cellFactura.setBorder(PdfPCell.NO_BORDER);
        cellFactura.addElement(new Paragraph("Factura Nro: " + venta.getIdVenta(), fontSubtitulo));
        cellFactura.addElement(new Paragraph("Fecha: " + new SimpleDateFormat("dd/MM/yyyy").format(venta.getFechaVenta()), fontNormal));
        cellFactura.addElement(new Paragraph("Hora: " + new SimpleDateFormat("HH:mm:ss").format(venta.getHoraVenta()), fontNormal));
        tablaDatos.addCell(cellFactura);

        document.add(tablaDatos);

        // Tabla de Detalles de Venta
        Paragraph subtituloDetalles = new Paragraph("Detalles de la Venta", fontSubtitulo);
        subtituloDetalles.setSpacingAfter(10);
        document.add(subtituloDetalles);

        PdfPTable tablaDetalles = new PdfPTable(4);
        tablaDetalles.setWidthPercentage(100);
        tablaDetalles.setWidths(new float[]{4, 1, 2, 2});

        PdfPCell cellHeader;
        cellHeader = new PdfPCell(new Phrase("Producto", fontBold));
        cellHeader.setBackgroundColor(Color.LIGHT_GRAY);
        tablaDetalles.addCell(cellHeader);
        cellHeader = new PdfPCell(new Phrase("Cant.", fontBold));
        cellHeader.setBackgroundColor(Color.LIGHT_GRAY);
        tablaDetalles.addCell(cellHeader);
        cellHeader = new PdfPCell(new Phrase("Precio Unit.", fontBold));
        cellHeader.setBackgroundColor(Color.LIGHT_GRAY);
        tablaDetalles.addCell(cellHeader);
        cellHeader = new PdfPCell(new Phrase("Subtotal", fontBold));
        cellHeader.setBackgroundColor(Color.LIGHT_GRAY);
        tablaDetalles.addCell(cellHeader);

        DecimalFormat df = new DecimalFormat("#,##0.00");
        for (DetalleVenta detalle : detalles) {
            Producto producto = productosDelCarrito.get(detalle.getIdProducto());
            if (producto != null) {
                tablaDetalles.addCell(new Phrase(producto.getNombreProducto(), fontNormal));
                tablaDetalles.addCell(new Phrase(String.valueOf(detalle.getCantidad()), fontNormal));
                tablaDetalles.addCell(new Phrase("$ " + df.format(producto.getPrecioUnitario()), fontNormal));
                tablaDetalles.addCell(new Phrase("$ " + df.format(detalle.getSubtotal()), fontNormal));
            }
        }
        document.add(tablaDetalles);

        // Total
        Paragraph total = new Paragraph("Total a Pagar: $ " + df.format(venta.getTotal()), fontSubtitulo);
        total.setAlignment(Paragraph.ALIGN_RIGHT);
        total.setSpacingBefore(20);
        document.add(total);

        document.close();
        return baos.toByteArray();
    }


    public byte[] generarFacturaPDF(Venta venta, List<DetalleVenta> detalles, Cliente cliente) throws JRException {
        // Ruta al archivo JRXML (o .jasper compilado)
        ServletContext servletContext = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
        InputStream reportStream = servletContext.getResourceAsStream("/reports/factura.jrxml");

        if (reportStream == null) {
            throw new JRException("No se encontró el archivo factura.jrxml en /reports/");
        }

        // Compilar el reporte si es JRXML (en producción se usaría el .jasper precompilado)
        JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

        // Parámetros del reporte
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("LOGO_PATH", servletContext.getRealPath("/resources/img/la-playita-logo.png"));
        parameters.put("CLIENTE_NOMBRE", cliente.getNombres() + " " + cliente.getApellidos());
        parameters.put("CLIENTE_DOCUMENTO", cliente.getDocumento());
        parameters.put("VENTA_ID", String.valueOf(venta.getIdVenta()));
        parameters.put("VENTA_FECHA", new SimpleDateFormat("dd/MM/yyyy").format(venta.getFechaVenta()));
        
        // Formatear el total para el reporte
        DecimalFormat df = new DecimalFormat("#,##0.00");
        parameters.put("VENTA_TOTAL", "$ " + df.format(venta.getTotal()));

        // Fuente de datos para la tabla de detalles (productos)
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(detalles);

        // Llenar el reporte
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

        // Exportar a PDF
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JRPdfExporter exporter = new JRPdfExporter();
        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(baos));
        SimplePdfExporterConfiguration configuration = new SimplePdfExporterConfiguration();
        exporter.setConfiguration(configuration);
        exporter.exportReport();

        return baos.toByteArray();
    }
}
