package org.homio.bundle.api.service;

import lombok.Getter;
import lombok.Setter;
import org.homio.bundle.api.entity.SshEntity;

public interface SshProviderService {

    SshSession openSshSession(SshEntity sshEntity);

    void closeSshSession(String token, SshEntity sshEntity);

    SessionStatusModel getSshStatus(String token, SshEntity sshEntity);

    @Getter
    @Setter
    class SshSession {
        String token;
        String tokenReadOnly;
        String ssh;
        String sshReadOnly;
    }

    @Getter
    @Setter
    class SessionStatusModel {
        private boolean closed;
        private String closed_at;
        private String created_at;
        private String disconnected_at;
        private String ssh_cmd_fmt;
        private String ws_url_fmt;
    }
}
