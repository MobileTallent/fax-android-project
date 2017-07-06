package co.faxapp.model;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

@ParseClassName("FavoriteNumber")
public class SavedNumber extends ParseObject {

    public String getName() {
        return getString("name");
    }

    public void setName(String name) {
        put("name",name);
    }

    public String getNumber() {
        return getString("number");
    }

    public void setNumber(String number) {
        put("number",number);
    }

    public ParseUser getUser() {
        return getParseUser("user");
    }

    public void setUser(ParseUser value) {
        put("user", value);
    }

    public static ParseQuery<SavedNumber> getQuery() {
        return ParseQuery.getQuery(SavedNumber.class);
    }
}
