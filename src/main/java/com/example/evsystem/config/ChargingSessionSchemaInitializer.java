package com.example.evsystem.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class ChargingSessionSchemaInitializer {

    @Bean
    @ConditionalOnProperty(name = "schema.init.charging-session-status.enabled", havingValue = "true", matchIfMissing = true)
    ApplicationRunner chargingSessionConstraintUpdater(JdbcTemplate jdbcTemplate) {
        return args -> {
            jdbcTemplate.execute("update charging_sessions set status = 'ACTIVE' where status = 'OCCUPIED'");
            jdbcTemplate.execute("update charging_sessions set status = 'COMPLETED' where status in ('AVAILABLE', 'OFFLINE')");
            jdbcTemplate.execute("alter table charging_sessions drop constraint if exists charging_sessions_status_check");
            jdbcTemplate.execute(
                    "alter table charging_sessions add constraint charging_sessions_status_check " +
                            "check (status in ('ACTIVE', 'COMPLETED'))"
            );
            jdbcTemplate.execute("alter table reservations drop constraint if exists reservations_status_check");
            jdbcTemplate.execute(
                    "alter table reservations add constraint reservations_status_check " +
                            "check (status in ('ACTIVE', 'IN_PROGRESS', 'CANCELLED', 'COMPLETED', 'EXPIRED'))"
            );
        };
    }
}
