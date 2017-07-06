package co.faxapp.model;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

@ParseClassName("UserProfile")
public class UserProfile extends ParseObject {

    public void setFreePages(int freePages) {
        put("freePages",freePages);
    }

    public void setPaidPages(int paidPages) {
        put("paidPages",paidPages);
    }

    public int getFreePages() {
        return getInt("freePages");
    }

    public int getPaidPages() {
        return getInt("paidPages");
    }

    public boolean isLocked() {
        return getBoolean("locked");
    }

    public void setLocked(boolean b) {
        put("locked",b);
    }

    public ParseUser getUser() {
        return getParseUser("user");
    }

    public void setUser(ParseUser value) {
        put("user", value);
    }

    public static ParseQuery<UserProfile> getQuery() {
        return ParseQuery.getQuery(UserProfile.class);
    }
}
