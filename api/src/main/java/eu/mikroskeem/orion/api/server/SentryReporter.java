package eu.mikroskeem.orion.api.server;

/**
 * Exposes Orion's Sentry integration for plugins
 *
 * @author Mark Vainomaa
 */
public interface SentryReporter {
    /**
     * Report exception to Sentry
     *
     * @param e Exception to report
     */
    void reportException(Throwable e);

    /**
     * Report exception to Sentry with extra text
     *
     * @param text Extra text
     * @param e Exception to report
     */
    void reportException(String text, Throwable e);
}
