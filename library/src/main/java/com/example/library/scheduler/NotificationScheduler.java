package com.example.library.scheduler;

import com.example.library.model.Notification;
import com.example.library.repository.NotificationRepository;
import com.example.library.services.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final NotificationRepository notificationRepository;
    private final MailService mailService;

    @Scheduled(cron = "0 30 4 * * *", zone = "Europe/Warsaw")
    @Transactional
    public void sendDailyNotifications() {

        List<Notification> notifications =
                notificationRepository.findBySentFalse();

        Map<Long, List<Notification>> groupedNotifications =
                notifications.stream()
                        .collect(Collectors.groupingBy(
                                notification -> notification.getClient().getId()
                        ));

        groupedNotifications.forEach((clientId, clientNotifications) -> {

            Notification firstNotification = clientNotifications.get(0);

            String email = firstNotification.getClient().getEmail();

            String bookTitles = clientNotifications.stream()
                    .map(notification -> notification.getBook().getTitle())
                    .distinct()
                    .collect(Collectors.joining("\n- ", "- ", ""));

            String message = "Witaj! Pojawiły się nowe książki zgodne z Twoimi subskrypcjami:\n\n"
                    + bookTitles;

            mailService.sendConfirmation(email, message);

        });

    }
}