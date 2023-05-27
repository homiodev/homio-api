package org.homio.api.console;

import com.pivovarit.function.ThrowingSupplier;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.io.output.TeeOutputStream;
import org.homio.api.EntityContext;
import org.homio.api.ui.UI;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InlineLogsConsolePlugin implements ConsolePluginComplexLines {

    @Getter
    private final EntityContext entityContext;
    private final List<ConsolePluginComplexLines.ComplexString> values = new ArrayList<>();

    @Override
    public String getEntityID() {
        return "icl";
    }

    @Override
    public Collection<ComplexString> getComplexValue() {
        return values;
    }

    public void clear() {
        this.values.clear();
        entityContext.ui().sendNotification("-lines-icl", "CLEAR");
    }

    public void add(String value, boolean error) {
        ComplexString complexString =
                ComplexString.of(value, System.currentTimeMillis(), error ? UI.Color.PRIMARY_COLOR : null, null);
        values.add(complexString);
        entityContext.ui().sendNotification("-lines-icl", complexString.toString());
    }

    @SneakyThrows
    public <T> T consoleLogUsingStdout(ThrowingSupplier<T, Exception> throwingRunnable, Runnable finallyBlock) {
        this.clear();
        PrintStream savedOutStream = System.out;
        PrintStream savedErrStream = System.out;

        TeeOutputStream outOutputStream = new TeeOutputStream(savedOutStream, new StdoutOutputStream(false));
        TeeOutputStream errorOutputStream = new TeeOutputStream(savedErrStream, new StdoutOutputStream(true));

        try {
            System.setOut(new PrintStream(outOutputStream, true));
            System.setErr(new PrintStream(errorOutputStream, true));
            return throwingRunnable.get();
        } finally {
            System.setOut(savedOutStream);
            System.setErr(savedErrStream);
            finallyBlock.run();
        }
    }

    @RequiredArgsConstructor
    private class StdoutOutputStream extends ByteArrayOutputStream {
        private final boolean stdErr;

        String value = "";

        @Override
        public synchronized void write(int b) {
            super.write(b);
            if (b == '\n') {
                add(value, stdErr);
                value = "";
            } else {
                value += b;
            }
        }

        @Override
        public synchronized void write(byte[] b, int off, int len) {
            super.write(b, off, len);
            add(new String(b, off, len), stdErr);
        }
    }
}
