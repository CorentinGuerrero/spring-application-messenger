package io.github.project.messenger.example;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;

@Repository
public class UserRepository {
    private static final RowMapper<User> USER_ROW_MAPPER = (rs, rowNum) -> new User(
        new UserId(rs.getObject("id", UUID.class)),
        rs.getString("email"),
        rs.getString("display_name"),
        rs.getTimestamp("created_at").toInstant()
    );

    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(User user) {
        jdbcTemplate.update(
            """
            insert into app_users (id, email, display_name, created_at)
            values (?, ?, ?, ?)
            """,
            user.id().value(),
            user.email(),
            user.displayName(),
            Timestamp.from(user.createdAt())
        );
    }

    public Optional<User> findById(UserId userId) {
        return jdbcTemplate.query(
                "select id, email, display_name, created_at from app_users where id = ?",
                USER_ROW_MAPPER,
                userId.value()
            )
            .stream()
            .findFirst();
    }

    public int count() {
        Integer count = jdbcTemplate.queryForObject("select count(*) from app_users", Integer.class);
        return count == null ? 0 : count;
    }
}
