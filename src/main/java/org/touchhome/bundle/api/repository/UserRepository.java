package org.touchhome.bundle.api.repository;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.touchhome.bundle.api.entity.UserEntity;
import org.touchhome.common.util.CommonUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.touchhome.bundle.api.util.Constants.*;
import static org.touchhome.common.util.CommonUtils.OBJECT_MAPPER;

@Log4j2
@Repository
public class UserRepository extends AbstractRepository<UserEntity> {

    private final PasswordEncoder passwordEncoder;

    public UserRepository(PasswordEncoder passwordEncoder) {
        super(UserEntity.class);
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public UserEntity getUser(String email) {
        return findSingleByField("userId", email);
    }

    @SneakyThrows
    @Transactional
    public void ensureUserExists() {
        List<UserEntity> users = listAll();
        if (users.isEmpty()) {
            log.info("Try create admin user");
            Path userPasswordFilePath = CommonUtils.getRootPath().resolve("user_password.conf");
            if (!Files.exists(userPasswordFilePath)) {
                throw new RuntimeException("Unable to start app without file user_password.conf");
            }
            UserPasswordFile userPasswordFile = OBJECT_MAPPER.readValue(userPasswordFilePath.toFile(), UserPasswordFile.class);
            UserEntity userEntity = new UserEntity()
                    .setEntityID(userPasswordFile.email)
                    .setPassword(userPasswordFile.password, passwordEncoder)
                    .setUserId(userPasswordFile.email)
                    .setRoles(new HashSet<>(Arrays.asList(ADMIN_ROLE, PRIVILEGED_USER_ROLE, GUEST_ROLE)));

            Path initPrivateKey = CommonUtils.getRootPath().resolve("init_private_key");
            if (Files.exists(initPrivateKey)) {
                userEntity.setKeystore(Files.readAllBytes(initPrivateKey));
            }
            save(userEntity);
            Files.delete(userPasswordFilePath);
            Files.deleteIfExists(initPrivateKey);
            log.info("Admin user created successfully");
        }
    }

    @Getter
    @Setter
    private static class UserPasswordFile {
        private String email;
        private String password;
    }
}
