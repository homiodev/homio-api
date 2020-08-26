package org.touchhome.bundle.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Type;
import org.json.JSONObject;
import org.touchhome.bundle.api.converter.JSONObjectConverter;
import org.touchhome.bundle.api.converter.StringSetConverter;
import org.touchhome.bundle.api.util.SslUtil;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

@Setter
@Entity
@Accessors(chain = true)
public class UserEntity extends BaseEntity<UserEntity> {

    public static final String PREFIX = "u_";

    public static final String ADMIN_USER = PREFIX + "user";

    @Getter
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
    private Date keystoreDate;

    @Getter
    private String lang = "en";

    @Getter
    @Enumerated(EnumType.STRING)
    private UserType userType = UserType.REGULAR;

    @Getter
    @Lob
    @Column(length = 1048576)
    @Convert(converter = JSONObjectConverter.class)
    private JSONObject jsonData;

    @Getter
    @Column(nullable = false)
    @Convert(converter = StringSetConverter.class)
    private Set<String> roles;

    public boolean matchPassword(String encodedPassword) {
        return this.password != null && this.password.equals(encodedPassword);
    }

    public boolean isPasswordNotSet() {
        return StringUtils.isEmpty(password);
    }

    public UserEntity setKeystore(byte[] keystore) {
        this.keystore = keystore;
        SslUtil.validateKeyStore(keystore, password);
        this.keystoreDate = new Date();
        return this;
    }

    public enum UserType {
        REGULAR, OTHER
    }
}
