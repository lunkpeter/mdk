package gov.nasa.jpl.mbee.alfresco;

import gov.nasa.jpl.mbee.viewedit.ViewEditUtils;
import gov.nasa.jpl.mbee.web.JsonRequestEntity;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;

public class ExportUtility {

    public static boolean send(String url, String json) {
        PostMethod pm = new PostMethod(url);
        GUILog gl = Application.getInstance().getGUILog();
        try {
            gl.log("[INFO] Sending...");
            pm.setRequestHeader("Content-Type", "application/json;charset=utf-8");
            pm.setRequestEntity(JsonRequestEntity.create(json));
            HttpClient client = new HttpClient();
            ViewEditUtils.setCredentials(client, url);
            int code = client.executeMethod(pm);
            if (ViewEditUtils.showErrorMessage(code))
                return false;
            String response = pm.getResponseBodyAsString();
            //gl.log(response);
            if (response.equals("NotFound"))
                gl.log("[ERROR] There are some views that are not exported yet, export the views first, then the comments");
            else if (response.equals("ok"))
                gl.log("[INFO] Export Successful.");
            else
                gl.log(response);
            return true;

        } catch (Exception ex) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            gl.log(sw.toString()); // stack trace as a string
            ex.printStackTrace();
            return false;
        } finally {
            pm.releaseConnection();
        }
    }
}