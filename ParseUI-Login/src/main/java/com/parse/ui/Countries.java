package com.parse.ui;

import java.util.ArrayList;
import java.util.List;

public class Countries {
    private static Countries instance;
    private List<Country> mCountries;

    public static Countries get() {
        if (instance == null) {
            instance = new Countries();
        }
        return instance;
    }

    public List<Country> getCountries() {
        return mCountries;
    }

    public int getCountryPositionByInt(int resId) {
        int i = 0;
        for (Country country : mCountries) {
            if (country.getFlagResources() == resId) {
                return i;
            }
            i++;
        }
        return 0;
    }

    public int getCountryPositionByCode(String code) {
        int i = 0;
        for (Country country : mCountries) {
            if (country.getCode().equals(code)) {
                return i;
            }
            i++;
        }
        return 0;
    }

    public int getCountryPositionByName(String name) {
        int i = 0;
        for (Country country : mCountries) {
            if (country.getName().equals(name)) {
                return i;
            }
            i++;
        }
        return 0;
    }

    private Countries() {
        mCountries = new ArrayList<>();
        Country country;

        country = new Country();
        country.setCode("+1");
        country.setFlagResources(R.drawable.united_states);
        country.setName("United States");
        mCountries.add(country);

        country = new Country();
        country.setCode("+1");
        country.setFlagResources(R.drawable.canada);
        country.setName("Canada");
        mCountries.add(country);

        country = new Country();
        country.setCode("+54");
        country.setFlagResources(R.drawable.argentina);
        country.setName("Argentina");
        mCountries.add(country);

        country = new Country();
        country.setCode("+61");
        country.setFlagResources(R.drawable.australia);
        country.setName("Australia");
        mCountries.add(country);

        country = new Country();
        country.setCode("+43");
        country.setFlagResources(R.drawable.austria);
        country.setName("Austria");
        mCountries.add(country);

        country = new Country();
        country.setCode("+32");
        country.setFlagResources(R.drawable.belgium);
        country.setName("Belgium");
        mCountries.add(country);

        country = new Country();
        country.setCode("+55");
        country.setFlagResources(R.drawable.brazil);
        country.setName("Brazil");
        mCountries.add(country);

        country = new Country();
        country.setCode("+45");
        country.setFlagResources(R.drawable.denmark);
        country.setName("Denmark");
        mCountries.add(country);

        country = new Country();
        country.setCode("+33");
        country.setFlagResources(R.drawable.france);
        country.setName("France");
        mCountries.add(country);

        country = new Country();
        country.setCode("+49");
        country.setFlagResources(R.drawable.germany);
        country.setName("Germany");
        mCountries.add(country);

        country = new Country();
        country.setCode("+30");
        country.setFlagResources(R.drawable.greece);
        country.setName("Greece");
        mCountries.add(country);

        country = new Country();
        country.setCode("+852");
        country.setFlagResources(R.drawable.hong_kong);
        country.setName("Hong Kong");
        mCountries.add(country);

        country = new Country();
        country.setCode("+91");
        country.setFlagResources(R.drawable.india);
        country.setName("India");
        mCountries.add(country);

        country = new Country();
        country.setCode("+353");
        country.setFlagResources(R.drawable.ireland);
        country.setName("Ireland");
        mCountries.add(country);

        country = new Country();
        country.setCode("+972");
        country.setFlagResources(R.drawable.israel);
        country.setName("Israel");
        mCountries.add(country);

        country = new Country();
        country.setCode("+39");
        country.setFlagResources(R.drawable.italy);
        country.setName("Italy");
        mCountries.add(country);

        country = new Country();
        country.setCode("+81");
        country.setFlagResources(R.drawable.japan);
        country.setName("Japan");
        mCountries.add(country);

        country = new Country();
        country.setCode("+44");
        country.setFlagResources(R.drawable.jersey);
        country.setName("Jersey");
        mCountries.add(country);

        country = new Country();
        country.setCode("+352");
        country.setFlagResources(R.drawable.luxembourg);
        country.setName("Luxembourg");
        mCountries.add(country);

        country = new Country();
        country.setCode("+31");
        country.setFlagResources(R.drawable.netherlands);
        country.setName("Netherlands");
        mCountries.add(country);

        country = new Country();
        country.setCode("+47");
        country.setFlagResources(R.drawable.norway);
        country.setName("Norway");
        mCountries.add(country);

        country = new Country();
        country.setCode("+351");
        country.setFlagResources(R.drawable.portugal);
        country.setName("Portugal");
        mCountries.add(country);

        country = new Country();
        country.setCode("+1");
        country.setFlagResources(R.drawable.puerto_rico);
        country.setName("Puerto Rico");
        mCountries.add(country);

        country = new Country();
        country.setCode("+40");
        country.setFlagResources(R.drawable.romania);
        country.setName("Romania");
        mCountries.add(country);

        country = new Country();
        country.setCode("+34");
        country.setFlagResources(R.drawable.spain);
        country.setName("Spain");
        mCountries.add(country);

        country = new Country();
        country.setCode("+27");
        country.setFlagResources(R.drawable.south_africa);
        country.setName("South Africa");
        mCountries.add(country);

        country = new Country();
        country.setCode("+46");
        country.setFlagResources(R.drawable.sweden);
        country.setName("Sweden");
        mCountries.add(country);

        country = new Country();
        country.setCode("+41");
        country.setFlagResources(R.drawable.switzerland);
        country.setName("Switzerland");
        mCountries.add(country);

        country = new Country();
        country.setCode("+44");
        country.setFlagResources(R.drawable.united_kingdom);
        country.setName("United Kingdom");
        mCountries.add(country);
    }
}
