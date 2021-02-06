package org.touchhome.bundle.api.repository;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.touchhome.bundle.api.entity.UserEntity;

@Repository
public class UserRepository extends AbstractRepository<UserEntity> {

    public UserRepository() {
        super(UserEntity.class);
    }

    @Transactional(readOnly = true)
    public UserEntity getUser(String email) {
        return findSingleByField("userId", email);
    }
}
