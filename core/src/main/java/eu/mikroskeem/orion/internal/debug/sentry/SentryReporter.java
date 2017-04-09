package eu.mikroskeem.orion.internal.debug.sentry;

import com.getsentry.raven.Raven;
import com.getsentry.raven.RavenFactory;
import com.getsentry.raven.event.Event;
import com.getsentry.raven.event.EventBuilder;
import com.getsentry.raven.event.interfaces.ExceptionInterface;
import eu.mikroskeem.orion.OrionServerCore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.RegisteredListener;

import java.util.Arrays;

/**
 * @author Mark Vainomaa
 */
@RequiredArgsConstructor
@Slf4j
public class SentryReporter {
    private final OrionServerCore core;
    private Raven raven;

    private void initRaven() {
        if(raven != null) return;
        String dsn = core.getConfiguration().getSentry().getSentryDSN();
        if(dsn == null || dsn.length() == 0){
            log.warn("Please configure sentry DSN in Orion configuration!");
            return;
        }
        raven = RavenFactory.ravenInstance(dsn);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> raven.closeConnection(), "Sentry shutdown thread"));
    }

    /* For usual exceptions */
    public void reportException(Throwable e) {
        reportException("Exception caught", e);
    }

    public void reportException(String text, Throwable e) {
        initRaven();
        if(raven != null) {
            EventBuilder eventBuilder = new EventBuilder()
                    .withMessage(text)
                    .withLevel(Event.Level.ERROR)
                    .withLogger(SentryReporter.class.getName())
                    .withSentryInterface(new ExceptionInterface(e));
            raven.sendEvent(eventBuilder.build());
        }
    }

    /* For event passing exceptions */
    public void reportEventPassException(RegisteredListener listener, Throwable e,
                                         eu.mikroskeem.orion.api.events.Event event) {
        initRaven();
        if(raven != null) {
            EventBuilder eventBuilder = new EventBuilder()
                    .withMessage(String.format(
                            "Could not pass event %s to listener %s",
                            event.toString(),
                            listener.getListener()
                    ))
                    .withLevel(Event.Level.ERROR)
                    .withLogger(SentryReporter.class.getName())
                    .withSentryInterface(new ExceptionInterface(e))
                    .withExtra("Plugin", listener.getPlugin())
                    .withExtra("Event", event.toString());
            raven.sendEvent(eventBuilder.build());
        }
    }

    /* For command execution exceptions */
    public void reportCommandException(CommandSender sender, Command command, String label, String[] args, Throwable e) {
        initRaven();
        if(raven != null) {
            EventBuilder eventBuilder = new EventBuilder()
                    .withMessage(String.format(
                            "Could not handle command '%s'",
                            command.getName()
                    ))
                    .withLevel(Event.Level.ERROR)
                    .withLogger(SentryReporter.class.getName())
                    .withSentryInterface(new ExceptionInterface(e))
                    .withExtra("Command sender", sender)
                    .withExtra("Command", command)
                    .withExtra("Label", label)
                    .withExtra("Arguments", Arrays.toString(args));
            raven.sendEvent(eventBuilder.build());
        }
    }
}
