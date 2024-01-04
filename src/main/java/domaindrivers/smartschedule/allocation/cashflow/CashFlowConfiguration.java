package domaindrivers.smartschedule.allocation.cashflow;


import domaindrivers.smartschedule.shared.EventsPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
class CashFlowConfiguration {

    @Bean
    CashFlowFacade cashFlowFacade(CashflowRepository cashflowRepository, EventsPublisher eventsPublisher, Clock clock) {
        return new CashFlowFacade(cashflowRepository, eventsPublisher, clock);
    }

}
