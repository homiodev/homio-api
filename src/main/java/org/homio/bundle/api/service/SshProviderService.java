package org.homio.bundle.api.service;

import lombok.Getter;
import lombok.Setter;

public interface SshProviderService {
    SshSession openSshSession();

    void closeSshSession(String token);

    SessionStatusModel getSshStatus(String token);

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
