package me.trihung.schedule;

import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.log4j.Log4j2;
import me.trihung.enums.OrderStatus;
import me.trihung.repository.OrderRepository;
import me.trihung.repository.RefreshTokenRepository;
import me.trihung.repository.ReservationRepository;

@Component
@Log4j2
public class CleanupScheduler {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    
    @Autowired
    private ReservationRepository reservationRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    //Token clean
    @Scheduled(cron = "0 0 0/23 * * ?")
//    @Scheduled(cron = "0 * * * * ?")
    public void scheduleRefreshTokenCleanup() {
        log.info("----------start clean up refresh tokens----------");
        LocalDateTime now = LocalDateTime.now();
        refreshTokenRepository.deleteExpiredTokens(now);
        log.info("----------end clean up refresh tokens------------");

    }
    
    //Reservation clean
    @Scheduled(fixedRate = 60000) //mỗi 1p
    @Transactional
    public void cleanupExpiredReservations() {
        LocalDateTime now = LocalDateTime.now();
        int deleted = orderRepository.markOrdersAsFailedIfExpired(now, OrderStatus.FAILED);;
//        int deleted = reservationRepository.deleteExpiredReservations(now);
        if (deleted > 0) {
        	reservationRepository.deleteExpiredReservations(now);
        	System.out.println("deleted");
            log.info("Deleted {} expired reservations.", deleted);
        }
    }

}
