package co.faxapp.util;

import com.itextpdf.text.pdf.PdfReader;
import com.parse.ParseACL;
import com.parse.ParseUser;
import com.phaxio.Fax;
import com.phaxio.exception.PhaxioException;
import com.phaxio.status.FaxStatus;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import co.faxapp.db.HelperFactory;
import co.faxapp.model.FaxEntity;
import co.faxapp.model.FaxItem;

public class Tools {
    private static final String TAG = Tools.class.getName();
    public static final String dateFormat = "yyyy-MM-dd'T'HH:mm:ssZ";

    public static List<String> getListFromString(String paths) {
        if (paths == null || paths.length() == 0) {
            return new ArrayList<>();
        } else if (!paths.contains("::")) {
            ArrayList<String> list = new ArrayList<>();
            list.add(paths);
            return list;
        } else {
            String[] arr = paths.split("::");
            ArrayList<String> list = new ArrayList<>(Arrays.asList(arr));
//            Log.i(TAG, "list=" + list);
            return list;
        }
    }

    public static String getStringFromList(List<String> list) {
        String result = "";
        for (int i = 0; i < list.size(); i++) {
            String s = list.get(i);
            result += s;
            if (list.size() - i > 1) {
                result += "::";
            }
        }
//        Log.i(TAG, "result=" + result);
        return result;
    }

    public static String getFileNameFromPath(String path) {
        File f = new File(path);
        return f.getName();
    }

    public static String fileExt(String url) {
        if (url.indexOf("?") > -1) {
            url = url.substring(0, url.indexOf("?"));
        }
        if (url.lastIndexOf(".") == -1) {
            return null;
        } else {
            String ext = url.substring(url.lastIndexOf("."));
            if (ext.indexOf("%") > -1) {
                ext = ext.substring(0, ext.indexOf("%"));
            }
            if (ext.indexOf("/") > -1) {
                ext = ext.substring(0, ext.indexOf("/"));
            }
            return ext.toLowerCase();
        }
    }

    public static int getFaxPagesCount(FaxEntity entity) {
        List<String> list = getListFromString(entity.getFilesPaths());
        int result = 0;
        for (String s : list) {
            if (s.toLowerCase().endsWith(".pdf")) {
                result += getPdfPagesCount(s);
            } else {
                result += 1;
            }
        }
        return result;
    }

    public static boolean inLimitFilesSize(FaxEntity entity) {
        List<String> list = getListFromString(entity.getFilesPaths());
        long result = 0;
        for (String s : list) {
            File file = new File(s);
            long fileSizeInBytes = file.length();
            long fileSizeInKB = fileSizeInBytes / 1024;
            long fileSizeInMB = fileSizeInKB / 1024;
            result += fileSizeInMB;
        }
        return result < 20;
    }

    public static int getPdfPagesCount(String path) {
        try {
            PdfReader reader = new PdfReader(path);
            return reader.getNumberOfPages();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return -1;
    }


    public static void checkAndFixFaxAttachments(FaxEntity entity) {
        if (entity.getCreateDate() == null) {
            return;
        }
        List<String> paths = Tools.getListFromString(entity.getFilesPaths());
        Iterator<String> i = paths.iterator();
        boolean needSave = false;
        while (i.hasNext()) {
            String s = i.next(); // must be called before you can call i.remove()
            File f = new File(s);
            if (!f.exists()) {
                i.remove();
                needSave = true;
            }
        }
        if (needSave) {
            try {
                HelperFactory.getHelper().getFaxEntityDao().update(entity);
            } catch (SQLException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
    }

    public static FaxStatus getFaxStatus(long phaxioId) {
        try {
            Fax fax = Fax.status(Long.toString(phaxioId));
            return fax.getStatus();
        } catch (PhaxioException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return null;
    }

//
//    public static long getWorldTime() {
//        try {
//            URL url = new URL("http://www.timeapi.org/utc/now");
//            InputStream is = url.openStream();
//            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
//            String line = reader.readLine();
//            Log.i(TAG, line);
//            SimpleDateFormat sdf = new SimpleDateFormat(Tools.dateFormat);
//            Date d = sdf.parse(line);
//            return d.getTime();
//        } catch (Exception e) {
//            Log.e(TAG, e.getMessage(), e);
//            return new Date().getTime();
//        }
//    }

    public static FaxItem createFaxItem(FaxEntity faxEntity) {
        FaxItem faxItem = new FaxItem();
        faxItem.setACL(new ParseACL(ParseUser.getCurrentUser()));
        faxItem.setUser(ParseUser.getCurrentUser());

        faxItem.setPhaxioId(faxEntity.getPhaxioId());
        faxItem.setFiles(getListFromString(faxEntity.getFilesPaths()));
        faxItem.setPages(getFaxPagesCount(faxEntity));
        faxItem.setPaidPages(faxEntity.getPaidPagesCount());
        if (faxEntity.getStatus()==0) {
            faxItem.setPhaxioStatus("created");
        } else if (faxEntity.getStatus()==1) {
            faxItem.setPhaxioStatus("in progress");
        } else if (faxEntity.getStatus()==2) {
            faxItem.setPhaxioStatus("success");
        } else if (faxEntity.getStatus()==3) {
            faxItem.setPhaxioStatus("failure");
        }
        faxItem.setPhoneCode(faxEntity.getCode());
        faxItem.setPhoneNumber(faxEntity.getPhoneNumber());
        if (faxEntity.getContactName()!=null) {
            faxItem.setContactName(faxEntity.getContactName());
        }
        faxItem.setSendDate(faxEntity.getSendDate());
        return faxItem;
    }

//    public static int getTxtPages(String s) {
//        File f = new File(s);
//        if (f.exists()) {
//            try {
//                Document pdfDoc = new Document();
//                pdfDoc.open();
//                pdfDoc.setMarginMirroring(true);
//                pdfDoc.setMargins(36, 72, 108, 180);
//                pdfDoc.topMargin();
//                Font myfont = new Font();
//                myfont.setStyle(Font.NORMAL);
//                myfont.setSize(10);
//                pdfDoc.add(new Paragraph("\n"));
//
//                FileInputStream fis = new FileInputStream(f);
//                DataInputStream in = new DataInputStream(fis);
//                InputStreamReader isr = new InputStreamReader(in);
//                BufferedReader br = new BufferedReader(isr);
//                String strLine;
//                while ((strLine = br.readLine()) != null) {
//                    Paragraph para = new Paragraph(strLine + "\n", myfont);
//                    para.setAlignment(Element.ALIGN_JUSTIFIED);
//                    pdfDoc.add(para);
//                }
//                return pdfDoc.getPageNumber();
//            } catch (Exception e) {
//                Log.e(TAG,e.getMessage(),e);
//                return 0;
//            }
//        } else {
//            return 0;
//        }
//    }

}
