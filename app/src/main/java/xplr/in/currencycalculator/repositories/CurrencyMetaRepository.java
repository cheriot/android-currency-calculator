package xplr.in.currencycalculator.repositories;

import android.text.TextUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import xplr.in.currencycalculator.models.CurrencyMeta;
import xplr.in.currencycalculator.sources.CurrencyMetaParser;
import xplr.in.currencycalculator.sources.CurrencyMetaSource;
import xplr.in.currencycalculator.sources.ResRawSource;

/**
 * Created by cheriot on 4/14/16.
 */
@Singleton
public class CurrencyMetaRepository {

    private final HashMap<String, CurrencyMeta> metaByCode;

    @Inject
    public CurrencyMetaRepository(CurrencyMetaSource source, CurrencyMetaParser parser, ResRawSource resRawSource) {
        List<CurrencyMeta> metaList = parser.parse(source.get());
        metaByCode = new HashMap<>(metaList.size());
        for(CurrencyMeta meta : metaList) {
            String name = meta.getResourceName();
            int resourceId = resRawSource.getResourceIdFromName(name);
            meta.setFlagResourceId(resourceId);
            metaByCode.put(meta.getCode(), meta);
        }
    }

    public CurrencyMeta findByCountryCode(String countryCode) {
        if(countryCode == null || TextUtils.isEmpty(countryCode.trim())) return null;
        countryCode = countryCode.trim();

        for(CurrencyMeta meta : findAll()) {
            for(CurrencyMeta.Country cmc : meta.getCountries()) {
                if(cmc.getCode().equalsIgnoreCase(countryCode)) return meta;
            }
        }
        return null;
    }

    public CurrencyMeta findByCode(String code) {
        return metaByCode.get(code);
    }

    public Collection<CurrencyMeta> findAll() {
        return metaByCode.values();
    }
}
