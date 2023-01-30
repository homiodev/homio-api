package org.touchhome.bundle.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Type;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.touchhome.bundle.api.converter.JSONConverter;
import org.touchhome.bundle.api.converter.StringSetConverter;
import org.touchhome.bundle.api.model.JSON;
import org.touchhome.bundle.api.util.Constants;
import org.touchhome.common.util.SslUtil;

import javax.persistence.*;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

@Entity
@Accessors(chain = true)
public final class UserEntity extends BaseEntity<UserEntity> {

    public static final String PREFIX = "u_";

    public static final String ADMIN_USER = PREFIX + "user";

    public static final UserEntity ANONYMOUS_USER = new UserEntity()
            .setRoles(Collections.emptySet());

    @Getter
    @Setter
    private String userId;

    @Getter
    @JsonIgnore
    private String password;

    @Lob
    @Getter
    @Type(type = "org.hibernate.type.BinaryType")
    @JsonIgnore
    private byte[] keystore;

    @Getter
    @Setter
    private Date keystoreDate;

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

    public UserEntity setKeystore(byte[] keystore) {
        this.keystore = keystore;
        SslUtil.validateKeyStore(keystore, password);
        this.keystoreDate = new Date();
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
