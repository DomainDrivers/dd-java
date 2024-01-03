package domaindrivers.smartschedule.allocation.cashflow;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
class CashFlowConfiguration {

    @Bean
    CashFlowFacade cashFlowFacade(CashflowRepository cashflowRepository) {
        return new CashFlowFacade(cashflowRepository);
    }

}
