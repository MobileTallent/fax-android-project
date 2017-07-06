package co.faxapp.model;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.Date;
import java.util.List;

@ParseClassName("FaxItem")
public class FaxItem extends ParseObject {

    public static ParseQuery<FaxItem> getQuery() {
        return ParseQuery.getQuery(FaxItem.class);
    }

    public long getPhaxioId() {
        return getLong("phaxioId");
    }

    public void setPhaxioId(long phaxioId) {
        put("phaxioId",phaxioId);
    }

    public List<String> getFiles() {
        return getList("files");
    }

    public void setFiles(List<String> files) {
        put("files",files);
    }

    public int getPages() {
        return getInt("pages");
    }

    public void setPages(int pages) {
        put("pages",pages);
    }

    public int getPaidPages() {
        return getInt("paidPages");
    }

    public void setPaidPages(int paidPages) {
        put("paidPages",paidPages);
    }

    public String getPhaxioStatus() {
        return getString("status");
    }

    public void setPhaxioStatus(String status) {
        put("status",status);
    }

    public String getPhoneCode() {
        return getString("phoneCode");
    }

    public void setPhoneCode(String phoneCode) {
        put("phoneCode",phoneCode);
    }

    public String getPhoneNumber() {
        return getString("phoneNumber");
    }

    public void setPhoneNumber(String phoneNumber) {
        put("phoneNumber",phoneNumber);
    }

    public String getContactName() {
        return getString("contactName");
    }

    public void setContactName(String contactName) {
        put("contactName",contactName);
    }

    public Date getSendDate() {
        return getDate("sendDate");
    }

    public void setSendDate(Date sendDate) {
        put("sendDate",sendDate);
    }


    public ParseUser getUser() {
        return getParseUser("user");
    }

    public void setUser(ParseUser value) {
        put("user", value);
    }
}
