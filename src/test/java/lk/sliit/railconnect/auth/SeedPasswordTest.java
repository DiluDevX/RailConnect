package lk.sliit.railconnect.auth;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class SeedPasswordTest {
    private static final String DEMO_PASSWORD_HASH =
            "$2a$10$1AgBVwWQgV3jrx4G2/.7j.K0luewoslGnhagCdm8gpviJ6aSt0mla";

    @Test
    void seededPasswordMatchesDocumentedDemoPassword() {
        assertThat(new BCryptPasswordEncoder().matches("password", DEMO_PASSWORD_HASH)).isTrue();
    }
}
