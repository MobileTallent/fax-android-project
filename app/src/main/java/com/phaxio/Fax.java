package com.phaxio;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.phaxio.exception.PhaxioException;
import com.phaxio.status.FaxStatus;
import com.phaxio.util.PagedList;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Fax {

    public static final String HTML = "html";
    public static final String TEXT = "text";
    public static final String URL = "url";

    public static final String SENT = "sent";
    public static final String RECEIVED = "received";

    private long id;
    private int numPages;
    private int cost;
    private String direction;
    private FaxStatus status;
    private boolean isTest;
    private Date requestedAt;
    private Date completedAt;
    private List<FaxRecipient> recipients;

    public static final String PDF = "p";
    public static final String SMALL_THUMBNAIL = "s";
    public static final String LARGEL_THUMBNAIL = "l";

    public Fax() {

    }

    public Fax(JsonObject fax) {
        mapJsonToSelf(fax);
    }

    /**
     * @param phoneNumbers
     * @param files
     * @param options
     * @return long The faxId of the fax being sent
     * @throws PhaxioException
     */
    public static long send(List<String> phoneNumbers, List<File> files, Map<String, Object> options) throws PhaxioException {
        HashMap<String, InputStream> inputStreamMap = new HashMap();

        for (File file : files) {
            try {
                inputStreamMap.put(file.getAbsolutePath(), new FileInputStream(file));
            } catch (FileNotFoundException e) {
                throw new PhaxioException("Could not open file", e);
            }
        }

        return send(phoneNumbers, inputStreamMap, options);
    }

    public static long send(List<String> phoneNumbers, Map<String, InputStream> inputStreamMap, Map<String, Object> options) throws PhaxioException {
        if (options == null) {
            options = new HashMap<String, Object>();
        }

        int i = 0;
        for (String number : phoneNumbers) {
            options.put("to[" + i + "]", number);
            i++;
        }

        if (inputStreamMap != null) {
            i = 0;
            for (Map.Entry<String, InputStream> entry : inputStreamMap.entrySet()) {
                options.put("filename[" + i + "]", new UploadInputStream(entry.getValue(), entry.getKey()));
                i++;
            }
        }

        JsonObject json = Phaxio.doRequest("send", options, "POST");
        return json.get("data").getAsJsonObject().get("faxId").getAsLong();
    }

    public static InputStream getFile(Fax fax, String type) throws PhaxioException {
        return getFile(String.valueOf(fax.getId()), type);
    }

    public static InputStream getFile(String id, String type) throws PhaxioException {
        Map<String, Object> options = new HashMap<String, Object>();

        options.put("id", id);
        if (type != null) options.put("type", type);

        return Phaxio.doRequestForInputStream("faxFile", options, "POST");
    }

    public static void cancel(Fax fax) throws PhaxioException {
        cancel(String.valueOf(fax.getId()));
    }

    public static void cancel(String id) throws PhaxioException {
        Map<String, Object> options = new HashMap<String, Object>();
        options.put("id", id);

        Phaxio.doRequest("faxCancel", options, "POST");
    }

    public static PagedList list(Map<String, Object> options) throws PhaxioException {
        return list(null, null, options);
    }

    public static PagedList list(Date start, Date end, Map<String, Object> options) throws PhaxioException {
        PagedList<Fax> list = new PagedList<Fax>();

        if (options == null) {
            options = new HashMap<String, Object>();
        }

        if (start != null) {
            options.put("start", start.getTime() / 1000);
        }

        if (end != null) {
            options.put("end", end.getTime() / 1000);
        }

        JsonObject result = Phaxio.doRequest("faxList", options, "POST");
        JsonObject paging = result.get("paging").getAsJsonObject();

        list.setMaxPerPage(paging.get("max_per_page").getAsInt());
        list.setPage(paging.get("page").getAsInt());
        list.setTotalPages(paging.get("total_pages").getAsInt());
        list.setTotalResults(paging.get("total_results").getAsInt());

        List<Fax> faxes = new ArrayList<Fax>();
        for (JsonElement element : result.get("data").getAsJsonArray()) {
            Fax fax = new Fax();
            fax.mapJsonToSelf(element.getAsJsonObject());
            faxes.add(fax);
        }
        list.setElements(faxes);

        return list;
    }


    public static Fax status(String id) throws PhaxioException {
        Map<String, Object> options = new HashMap<String, Object>();
        options.put("id", id);

        JsonObject result = Phaxio.doRequest("faxStatus", options, "POST");
        Fax fax = new Fax();
        fax.mapJsonToSelf(result.get("data").getAsJsonObject());
        return fax;
    }


    /**
     * @param file    A PDF File that you'd like to test receiving
     * @param options
     * @return boolean True if operation was successful
     * @throws PhaxioException
     */
    public static boolean testReceive(File file, Map<String, Object> options) throws PhaxioException {
        if (options == null) {
            options = new HashMap<String, Object>();
        }

        options.put("filename", file);

        JsonObject json = Phaxio.doRequest("testReceive", options, "POST");
        return json.get("success").getAsBoolean();
    }

    void mapJsonToSelf(JsonObject object) {
        if (object.has("id")) this.id = object.get("id").getAsLong();
        if (object.has("num_pages")) this.numPages = object.get("num_pages").getAsInt();
        if (object.has("direction")) this.direction = object.get("direction").getAsString();
        if (object.has("status"))
            this.status = FaxStatus.fromApiName(object.get("status").getAsString());
        if (object.has("is_test")) this.isTest = object.get("is_test").getAsBoolean();
        if (object.has("requested_at"))
            this.requestedAt = new Date(object.get("requested_at").getAsLong() * 1000);
        if (object.has("completed_at"))
            this.completedAt = new Date(object.get("completed_at").getAsLong() * 1000);
        if (object.has("cost")) this.cost = object.get("cost").getAsInt();

        if (object.has("recipients")) {
            this.recipients = new ArrayList<FaxRecipient>();

            JsonArray array = object.get("recipients").getAsJsonArray();
            for (JsonElement el : array) {
                FaxRecipient recip = new FaxRecipient();
                recip.mapJsonToSelf((JsonObject) el);
                this.recipients.add(recip);
            }
        }
    }

    public Date getCompletedAt() {
        return completedAt;
    }

    public int getCost() {
        return cost;
    }

    public String getDirection() {
        return direction;
    }

    public long getId() {
        return id;
    }

    public boolean isTest() {
        return isTest;
    }

    public int getNumPages() {
        return numPages;
    }

    public List<FaxRecipient> getRecipients() {
        return recipients;
    }

    public Date getRequestedAt() {
        return requestedAt;
    }

    public FaxStatus getStatus() {
        return status;
    }
}
