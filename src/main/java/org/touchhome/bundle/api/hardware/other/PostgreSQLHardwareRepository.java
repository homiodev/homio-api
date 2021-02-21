package org.touchhome.bundle.api.hardware.other;

import org.touchhome.bundle.api.hquery.api.HQueryParam;
import org.touchhome.bundle.api.hquery.api.HardwareQuery;
import org.touchhome.bundle.api.hquery.api.HardwareRepositoryAnnotation;

@HardwareRepositoryAnnotation
public interface PostgreSQLHardwareRepository {

    @HardwareQuery(name = "Install postgresql", value = "$PM install -y postgresql", printOutput = true, maxSecondsTimeout = 300)
    void installPostgreSQL();

    @HardwareQuery(name = "Get psql version", value = "psql --version")
    String getPostgreSQLVersion();

    @HardwareQuery(name = "Check psql is running", value = {"service", "postgresql", "status"}, printOutput = true)
    boolean isPostgreSQLRunning();

    @HardwareQuery(name = "Alter postgres password", value = "su - postgres -c \"psql -c \\\"ALTER USER postgres PASSWORD 'postgres';\\\"\"")
    void changePostgresPassword(@HQueryParam("pwd") String pwd);

    @HardwareQuery(name = "Start postgresql", value = "service postgresql start", printOutput = true)
    void startPostgreSQLService();

}









