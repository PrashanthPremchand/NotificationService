package com.prashanth.microservices.service;


import com.prashanth.microservices.order.event.OrderPlacedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final JavaMailSender javaMailSender;

    @KafkaListener(topics = "order-placed")
    public void listen(OrderPlacedEvent orderPlacedEvent) {
        log.info("Received message from order-placed topic: {}", orderPlacedEvent);

        if (orderPlacedEvent == null ||
                orderPlacedEvent.getEmail() == null ||
                orderPlacedEvent.getFirstName() == null ||
                orderPlacedEvent.getLastName() == null ||
                orderPlacedEvent.getOrderNumber() == null) {
            log.error("Invalid OrderPlacedEvent received: {}", orderPlacedEvent);
            return;
        }

        if (!isValidEmail(orderPlacedEvent.getEmail().toString())) {
            log.error("Invalid email address: {}", orderPlacedEvent.getEmail());
            return;
        }

        MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
            messageHelper.setFrom("springshop@email.com");
            messageHelper.setTo(orderPlacedEvent.getEmail().toString());
            messageHelper.setSubject(String.format("Your Order with OrderNumber %s is placed successfully", orderPlacedEvent.getOrderNumber()));
            messageHelper.setText(String.format("""
                Hi %s %s,

                Your order with order number %s is now placed successfully.

                Best Regards,
                Spring Shop
                """,
                    orderPlacedEvent.getFirstName(),
                    orderPlacedEvent.getLastName(),
                    orderPlacedEvent.getOrderNumber()));
        };

        try {
            javaMailSender.send(messagePreparator);
            log.info("Order notification email sent successfully!");
        } catch (MailException e) {
            log.error("Error occurred while sending email", e);
            // Optionally handle retries or forward to a Dead Letter Topic
            throw new RuntimeException("Failed to send email notification", e);
        }
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$");
    }

}


