import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

public class EmailService {
	public static void sendEmail(List<AttendanceRecord> listLate, List<AttendanceRecord> listMissAttendance) {
		//  cấu hình email
		String from = "huanruaxz@gmail.com"; // Email nguồn
		String password = "gmxdlzhuoenlbtei"; // Mật khẩu ứng dụng
		String toEmail = "huan.dq171001@gmail.com"; // Email đích
		String subject = "Falcon Notification";
		String body = "<h1 style=\"color: #007BFF;\">FalconGameStudio</h1>";

		if(CollectionUtils.isNotEmpty(listLate)) {
			body += "<p style=\"font-size: 18px;\">Đi muộn về sớm vào: </p>"
					+ listObjectToHTML(listLate);
		}

		if(CollectionUtils.isNotEmpty(listMissAttendance)) {
			body += "<p style=\"font-size: 18px;\">Quên chấm công vào: </p>"
					+ listObjectToHTML(listMissAttendance);
		}
		// Cấu hình thông tin SMTP
		Properties props = new Properties();
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.port", "587");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.ssl.protocols", "TLSv1.2");
		// Xác thực bằng mật khẩu ứng dụng
		Session session = Session.getInstance(props, new javax.mail.Authenticator() {
				@Override
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(from, password);
				}
			});

		try {
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(from));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
			message.setSubject(subject);
			// Thiết lập định dạng email
			MimeBodyPart mimeBodyPart = new MimeBodyPart();
			mimeBodyPart.setContent(body, "text/html; charset=utf-8");
			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(mimeBodyPart);
			message.setContent(multipart);
			// Gửi email
			Transport.send(message);
		} catch (MessagingException e) {
			throw new RuntimeException(e);
		}
	}

	public static String listObjectToHTML(List<AttendanceRecord> objectList) {
		StringBuilder htmlBuilder = new StringBuilder();
		htmlBuilder.append("<ul>");

		for (AttendanceRecord object : objectList) {
			htmlBuilder.append("<li>");
			htmlBuilder.append("<span>Date: ").append(object.getDate()).append("</span>");
			htmlBuilder.append("<span>, Arrival Time: ").append(object.getArrivalTime()).append("</span>");
			htmlBuilder.append("<span>, Leave Time: ").append(object.getLeaveTime()).append("</span>");
			htmlBuilder.append("</li>");
		}

		htmlBuilder.append("</ul>");

		return htmlBuilder.toString();
	}
}
