package nh.demo.plantify.owner;

import nh.demo.plantify.care.CareService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ApplicationModuleTest(verifyAutomatically = false)
@ActiveProfiles("test")
public class OwnerModuleTest {

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    void test_irgendwas() {
        assertThat(applicationContext.getBeansOfType(OwnerRepository.class))
            .isNotEmpty();

        assertThat(applicationContext.getBeansOfType(CareService.class))
            .isEmpty();
    }

    @Test
    void migrates_only_the_owner_module() {
        assertThat(tableExists("owner_schema", "owners")).isTrue();

        assertThat(tableExists("care_schema", "care_tasks")).isFalse();
    }

    private boolean tableExists(String schema, String table) {
        var count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM information_schema.tables WHERE LOWER(table_schema) = ? AND LOWER(table_name) = ?",
            Integer.class,
            schema,
            table
        );
        return count > 0;
    }
}
