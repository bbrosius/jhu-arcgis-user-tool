package org.jhu.gis.usertool;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.client.util.Key;
import org.jhu.gis.usertool.model.LoginCredential;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Class to communicate with the JHU Arcgis online portal.
 * This class handles all the REST calls to ArcGIS Online.
 */
public class ArcGISOnlineService {

    private final static String BASE_URL = "https://www.arcgis.com";
    static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    static final JsonFactory JSON_FACTORY = new JacksonFactory();

    private Token token;
    private HttpRequestFactory requestFactory;
    public ArcGISOnlineService(LoginCredential credential) throws Exception {
        javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(
        new javax.net.ssl.HostnameVerifier(){

            public boolean verify(String hostname,
                    javax.net.ssl.SSLSession sslSession) {
                return true;
            }
        });

        requestFactory = HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
            public void initialize(HttpRequest request) {
                request.setParser(new JsonObjectParser(JSON_FACTORY));
            }
        });

        GenericUrl tokenUrl = new GenericUrl(
            BASE_URL + "/sharing/rest/generateToken");
        tokenUrl.put("username", credential.getUserName());
        tokenUrl.put("password", credential.getPassword());
        tokenUrl.put("referer", "https://www.gisanddata.maps.arcgis.com");
        tokenUrl.put("f", "json");

        HttpRequest request = requestFactory.buildPostRequest(tokenUrl, null);

        HttpResponse response = request.execute();

        token = response.parseAs(Token.class);

        if (token.token == null) {
            throw new IllegalArgumentException("Invalid username and password, please try again.");
        }
    }

    public Users getNewUsersFromPastWeek() throws IOException {
        GregorianCalendar today = new GregorianCalendar();
        GregorianCalendar lastWeek = new GregorianCalendar();
        lastWeek.add(Calendar.WEEK_OF_YEAR, -1);

        GenericUrl userUrl = new GenericUrl(BASE_URL + "/sharing/rest/community/users/");
        userUrl.put("q", "created : [000000" + lastWeek.getTimeInMillis() + "000 TO 000000" + today.getTimeInMillis() + "000] AND orgid:0MSEUqKaxRlEPj5g");
        userUrl.put("token", token.token);
        userUrl.put("num", 100);
        userUrl.put("f", "json");

        HttpRequest userRequest = requestFactory.buildGetRequest(userUrl);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType("application/json");
        userRequest.setHeaders(headers);

        Users newUsers = userRequest.execute().parseAs(Users.class);

        //In case more than 100 users are created loop through and get the rest
        if (newUsers.count > 100) {
            int loopCount = (int)Math.ceil(newUsers.count / 100.0);
            for (int i = 0; i < loopCount; i++) {
                int offset = 100 + (i * 100);
                userUrl.put("start", offset);
                userRequest = requestFactory.buildGetRequest(userUrl);

                headers = new HttpHeaders();
                headers.setContentType("application/json");
                userRequest.setHeaders(headers);

                Users userSet = userRequest.execute().parseAs(Users.class);

                newUsers.getUsers().addAll(userSet.getUsers());
            }
        }

        return newUsers;
    }

    public Users getUsersInactiveForOneYear() throws IOException {
        GenericUrl userUrl = new GenericUrl(BASE_URL + "/sharing/rest/community/users/");

        GregorianCalendar lastYear = new GregorianCalendar();
        lastYear.add(Calendar.YEAR, -1);

        GregorianCalendar lastYearWindow = new GregorianCalendar();
        lastYearWindow.add(Calendar.YEAR, -10);

        userUrl.put("q",
                    "created : [000000" + lastYearWindow.getTimeInMillis() +
                        "000 TO 000000" + lastYear.getTimeInMillis() +
                        "000] AND orgid:0MSEUqKaxRlEPj5g");

        userUrl.put("token", token.token);
        userUrl.put("num", 100);
        userUrl.put("f", "json");

        HttpRequest userRequest = requestFactory.buildGetRequest(userUrl);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType("application/json");
        userRequest.setHeaders(headers);

        Users oldUsers = userRequest.execute().parseAs(Users.class);

        //In case more than 100 users are created loop through and get the rest
        if (oldUsers.count > 100) {
            int loopCount = (int)Math.ceil(oldUsers.count / 100.0);
            for (int i = 0; i < loopCount; i++) {
                int offset = 10 + (i * 100) + 1;
                userUrl.put("start", offset);
                userRequest = requestFactory.buildGetRequest(userUrl);

                headers = new HttpHeaders();
                headers.setContentType("application/json");
                userRequest.setHeaders(headers);

                Users userSet = userRequest.execute().parseAs(Users.class);

                oldUsers.getUsers().addAll(userSet.getUsers());
            }
        }

        Users inactiveUsers = new Users();
        inactiveUsers.users = new ArrayList<>();

        //Since we can't query for last login date we need to loop through the users and check the last login date.
        for (User oldUser : oldUsers.getUsers()) {
            GenericUrl userProfileUrl = new GenericUrl(BASE_URL + "/sharing/rest/community/users/" + oldUser.userName);
            userProfileUrl.put("token", token.token);
            userProfileUrl.put("f", "json");

            HttpRequest userProfileRequest = requestFactory.buildGetRequest(userProfileUrl);
            headers = new HttpHeaders();
            headers.setContentType("application/json");
            userRequest.setHeaders(headers);


            User oldUserProfile = userProfileRequest.execute().parseAs(User.class);
            if (oldUserProfile.lastLogin < lastYear.getTimeInMillis()) {
                inactiveUsers.getUsers().add(oldUserProfile);
            }
        }
        return inactiveUsers;
    }

    public static class Token {
        @Key("token")
        public String token;
    }

    public static class Users {
        @Key("results")
        private List<User> users;

        public List<User> getUsers() {
            return users;
        }

        @Key("total")
        public int count;
    }

    public static class User extends GenericJson {
        @Key("username")
        public String userName;

        @Key("fullName")
        public String fullName;

        @Key("created")
        public Long createdDate;

        @Key("lastLogin")
        public Long lastLogin;
    }
}
