package co.faxapp.model;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

@DatabaseTable(tableName = "FaxTable")
public class FaxEntity {

    @DatabaseField(columnName = "_id", generatedId = true)
    private long id;

    @DatabaseField(dataType = DataType.DATE)
    private Date createDate;

    @DatabaseField(dataType = DataType.DATE)
    private Date updateDate;

    @DatabaseField(dataType = DataType.DATE)
    private Date sendDate;

    @DatabaseField()
    private String filesPaths;

    @DatabaseField
    private String phoneNumber;

    @DatabaseField
    private String contactName;

    @DatabaseField
    private String code;

    @DatabaseField
    private int status; //0 - created, 1-in progress, 2-success, 3-failure

    @DatabaseField
    private int paidPagesCount=0;

    @DatabaseField
    private long phaxioId;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public Date getSendDate() {
        return sendDate;
    }

    public void setSendDate(Date sendDate) {
        this.sendDate = sendDate;
    }

    public String getFilesPaths() {
        return filesPaths;
    }

    public void setFilesPaths(String filesPaths) {
        status=0;
        sendDate=null;
        this.filesPaths = filesPaths;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        status=0;
        sendDate=null;
        this.phoneNumber = phoneNumber;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getPaidPagesCount() {
        return paidPagesCount;
    }

    public void setPaidPagesCount(int paidPagesCount) {
        this.paidPagesCount = paidPagesCount;
    }

    public long getPhaxioId() {
        return phaxioId;
    }

    public void setPhaxioId(long phaxioId) {
        this.phaxioId = phaxioId;
    }
}
