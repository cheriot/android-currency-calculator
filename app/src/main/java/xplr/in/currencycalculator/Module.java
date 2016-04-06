package xplr.in.currencycalculator;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

import org.greenrobot.eventbus.EventBus;

/**
 * Guice module.
 */
public class Module extends AbstractModule {
    @Override
    protected void configure() {
        // Ex. bind(Integer.class).toProvider(MyRandomNumberProvider.class);
        bind(EventBus.class).in(Singleton.class);
    }
}
