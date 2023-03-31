package org.homio.bundle.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Collections;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Lob;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.homio.bundle.api.converter.JSONConverter;
import org.homio.bundle.api.converter.StringSetConverter;
import org.homio.bundle.api.model.JSON;
import org.homio.bundle.api.util.Constants;
import org.springframework.security.crypto.password.PasswordEncoder;

@Entity
@Accessors(chain = true)
public final class UserEntity extends BaseEntity<UserEntity> {

    public static final String PREFIX = "u_";

    public static final UserEntity ANONYMOUS_USER = new UserEntity()
            .setRoles(Collections.emptySet());

    @Getter
    @Setter
    private String userId;

    @Getter
    @JsonIgnore
    private String password;

    @Getter
    @Setter
    private String lang = "en";

    @Getter
    @Setter
    @Enumerated(EnumType.STRING)
    private UserType userType = UserType.REGULAR;

    @Getter
    @Setter
    @Lob
    @Column(length = 1_000_000)
    @Convert(converter = JSONConverter.class)
    private JSON jsonData;

    @Getter
    @Setter
    @Column(nullable = false)
    @Convert(converter = StringSetConverter.class)
    private Set<String> roles;

    public boolean matchPassword(String password, PasswordEncoder passwordEncoder) {
        return this.password != null && (this.password.equals(password) ||
                (passwordEncoder != null && passwordEncoder.matches(password, this.password)));
    }

    public UserEntity setPassword(String password, PasswordEncoder passwordEncoder) {
        if (passwordEncoder != null) {
            try {
                passwordEncoder.upgradeEncoding(password);
            } catch (Exception ex) {
                password = passwordEncoder.encode(password);
            }
        }
        this.password = password;
        return this;
    }

    public boolean isAdmin() {
        return this.userType == UserType.REGULAR && this.roles != null && this.roles.contains(Constants.ADMIN_ROLE);
    }

    @Override
    public String getEntityPrefix() {
        return PREFIX;
    }

    @Override
    public String getDefaultName() {
        return "User";
    }

    public enum UserType {
        REGULAR, OTHER
    }
}
