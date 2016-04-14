package xplr.in.currencycalculator.repositories;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import xplr.in.currencycalculator.models.CurrencyMeta;
import xplr.in.currencycalculator.sources.CurrencyMetaParser;
import xplr.in.currencycalculator.sources.CurrencyMetaSource;

/**
 * Created by cheriot on 4/14/16.
 */
public class CurrencyMetaRepository {

    private final HashMap<String, CurrencyMeta> metaByCode;

    @Inject
    public CurrencyMetaRepository(CurrencyMetaSource source, CurrencyMetaParser parser) {
        List<CurrencyMeta> metaList = parser.parse(source.get());
        metaByCode = new HashMap<>(metaList.size());
        for(CurrencyMeta meta : metaList) {
            metaByCode.put(meta.getCode(), meta);
        }
    }

    public CurrencyMeta findByCode(String code) {
        return metaByCode.get(code);
    }

    public Collection<CurrencyMeta> findAll() {
        return metaByCode.values();
    }
}
