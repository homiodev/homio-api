package org.touchhome.bundle.api.hquery;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.touchhome.bundle.api.condition.LinuxEnvironmentCondition;
import org.touchhome.bundle.api.condition.WindowsEnvironmentCondition;
import org.touchhome.bundle.api.hquery.api.HardwareQuery;

import java.io.File;

@Configuration
public class HQueryConfiguration implements ImportAware {

    private AnnotationAttributes scanBaseClassesPackage;

    @Override
    public void setImportMetadata(AnnotationMetadata metadata) {
        scanBaseClassesPackage = AnnotationAttributes.fromMap(metadata.getAnnotationAttributes(EnableHQuery.class.getName()));
    }

    @Bean
    public BeanFactoryPostProcessor beanFactoryPostProcessor(@Autowired(required = false) HardwareRepositoryFactoryPostHandler handler) {
        return new HardwareRepositoryFactoryPostProcessor(
                scanBaseClassesPackage.getString("scanBaseClassesPackage"),
                handler);
    }

    @Bean
    @Conditional(LinuxEnvironmentCondition.class)
    public HQueryExecutor linuxHQueryExecutor() {
        String pm = "apt";

        return new HQueryExecutor() {
            @Override
            public String[] getValues(HardwareQuery hardwareQuery) {
                return hardwareQuery.value();
            }

            @Override
            public String updateCommand(String cmd) {
                return cmd.contains("$PM") ? cmd.replace("$PM", pm) : cmd;
            }

            @Override
            @SneakyThrows
            public Process createProcess(String[] cmdParts, String[] env, File dir) {
                if (dir != null) {
                    return Runtime.getRuntime().exec(cmdParts, env, dir);
                } else if (cmdParts.length == 1) {
                    return Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", cmdParts[0]});
                } else {
                    return Runtime.getRuntime().exec(cmdParts);
                }
            }
        };
    }

    @Bean
    @Conditional(WindowsEnvironmentCondition.class)
    public HQueryExecutor winHQueryExecutor() {
        return new HQueryExecutor() {
            @Override
            public String[] getValues(HardwareQuery hardwareQuery) {
                return hardwareQuery.win();
            }

            @Override
            @SneakyThrows
            public Process createProcess(String[] cmdParts, String[] env, File dir) {
                if (dir != null) {
                    return Runtime.getRuntime().exec(cmdParts, env, dir);
                } else if (cmdParts.length == 1) {
                    return Runtime.getRuntime().exec("cmd.exe /C " + cmdParts[0]);
                } else {
                    return Runtime.getRuntime().exec(cmdParts);
                }
            }
        };
    }
}
