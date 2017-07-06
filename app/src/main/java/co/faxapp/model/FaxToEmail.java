package co.faxapp.model;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

@ParseClassName("FaxToEmail")
public class FaxToEmail extends ParseObject {

    public ParseUser getUser() {
        return getParseUser("user");
    }

    public void setUser(ParseUser value) {
        put("user", value);
    }

    public static ParseQuery<FaxToEmail> getQuery() {
        return ParseQuery.getQuery(FaxToEmail.class);
    }

    public String getEmail() {
        return getString("email");
    }

    public void setEmail(String email) {
        put("email", email);
    }

    public String getPhone() {
        return getString("phone");
    }

    public void setPhone(String phone) {
        put("phone", phone);
    }

    public String getPaymentDate() {
        return getString("paymentDate");
    }

    public void setPaymentDate(String paymentDate) {
        put("paymentDate", paymentDate);
    }
}
