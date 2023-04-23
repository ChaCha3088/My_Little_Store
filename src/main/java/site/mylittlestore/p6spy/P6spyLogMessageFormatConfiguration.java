package site.mylittlestore.p6spy;

import com.p6spy.engine.spy.P6SpyOptions;
import org.springframework.context.annotation.Configuration;
import site.mylittlestore.p6spy.CustomP6spySqlFormat;

import javax.annotation.PostConstruct;

/**
 * query multi line custom configure
 */
@Configuration
public class P6spyLogMessageFormatConfiguration {
    @PostConstruct
    public void setLogMessageFormat() {
        P6SpyOptions.getActiveInstance().setLogMessageFormat(CustomP6spySqlFormat.class.getName());
    }
}