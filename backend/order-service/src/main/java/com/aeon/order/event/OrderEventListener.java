package com.aeon.order.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class OrderEventListener {

    private static final Logger log = LoggerFactory.getLogger(OrderEventListener.class);

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(OrderEvent event) {
        log.info("EVENT {} order={} user={} status={} total={}", event.type(), event.orderNumber(),
                event.userId(), event.status(), event.totalAmount());
    }
}
