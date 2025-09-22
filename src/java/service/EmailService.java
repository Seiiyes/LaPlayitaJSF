package service;

import model.Cliente;
import model.Venta;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class EmailService {

    private Properties mailProperties;

    public EmailService() {
        loadProperties();
    }

    private void loadProperties() {
        mailProperties = new Properties();
        // Busca el archivo desde la raíz del classpath
        try (InputStream input = EmailService.class.getClassLoader().getResourceAsStream("resources/mail.properties")) {
            if (input == null) {
                System.err.println("EmailService Error: No se pudo encontrar el archivo 'resources/mail.properties'. Asegúrese de que esté en la carpeta 'src/java/resources'.");
                throw new RuntimeException("Archivo de propiedades de correo no encontrado.");
            }
            mailProperties.load(input);
            System.out.println("EmailService: El archivo mail.properties fue cargado exitosamente.");
        } catch (IOException ex) {
            throw new RuntimeException("Error al leer el archivo de propiedades de correo.", ex);
        }
    }

    public void enviarFactura(Cliente cliente, Venta venta, byte[] pdfBytes) {
        final String username = mailProperties.getProperty("mail.smtp.user");
        final String password = mailProperties.getProperty("mail.smtp.password");

        Session session = Session.getInstance(mailProperties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(mailProperties.getProperty("mail.from")));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(cliente.getCorreo()));
            message.setSubject("Factura de su compra en La Playita - Venta N° " + venta.getIdVenta());

            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setContent(
                "<h1>¡Gracias por su compra!</h1>" +
                "<p>Hola " + cliente.getNombres() + ",</p>" +
                "<p>Adjuntamos la factura correspondiente a su compra realizada el " + venta.getFechaVenta() + ".</p>" +
                "<p>Total: $" + venta.getTotal() + "</p>" +
                "<br/>" +
                "<p>Atentamente,<br/>El equipo de La Playita</p>",
                "text/html; charset=utf-8"
            );

            MimeBodyPart attachmentPart = new MimeBodyPart();
            DataSource source = new ByteArrayDataSource(pdfBytes, "application/pdf");
            attachmentPart.setDataHandler(new DataHandler(source));
            attachmentPart.setFileName("factura_venta_" + venta.getIdVenta() + ".pdf");

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(textPart);
            multipart.addBodyPart(attachmentPart);

            message.setContent(multipart);

            System.out.println("EmailService: Intentando enviar correo a " + cliente.getCorreo() + "...");
            Transport.send(message);
            System.out.println("EmailService: ¡Correo enviado exitosamente!");

        } catch (AuthenticationFailedException authEx) {
            System.err.println("EmailService: ¡FALLO DE AUTENTICACIÓN! Verifique 'mail.smtp.user' y 'mail.smtp.password' en mail.properties.");
            System.err.println("EmailService: Si usa Gmail con 2FA, asegúrese de usar una Contraseña de Aplicación SIN ESPACIOS.");
            throw new RuntimeException("Fallo de autenticación al enviar correo.", authEx);
        } catch (MessagingException me) {
            System.err.println("EmailService: Error de mensajería. ¿Puede la aplicación conectarse a '" + mailProperties.getProperty("mail.smtp.host") + "'?");
            System.err.println("EmailService: Causa detallada: " + me.getMessage());
            throw new RuntimeException("Error de conexión o configuración al enviar factura.", me);
        } catch (Exception e) {
            System.err.println("EmailService: Ocurrió un error inesperado al construir o enviar el correo.");
            throw new RuntimeException("No se pudo enviar el correo de la factura.", e);
        }
    }
}
