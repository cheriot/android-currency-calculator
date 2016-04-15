package xplr.in.currencycalculator.sources;

import com.bluelinelabs.logansquare.LoganSquare;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.inject.Inject;

import xplr.in.currencycalculator.models.CurrencyMeta;


/**
 * Created by cheriot on 4/14/16.
 */
public class CurrencyMetaParser {

    @Inject
    public CurrencyMetaParser() {
    }

    public List<CurrencyMeta> parse(InputStream inputStream) {
        try {
            try {
                return LoganSquare.parseList(inputStream, CurrencyMeta.class);
            } finally {
                inputStream.close();
            }
        } catch(IOException ioe) {
            throw new RuntimeException("Error parsing CurrencyMeta", ioe);
        }
    }
}
