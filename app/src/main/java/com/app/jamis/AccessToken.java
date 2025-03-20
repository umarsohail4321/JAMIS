package com.app.jamis;

import android.util.Log;


import com.google.auth.oauth2.GoogleCredentials;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class AccessToken {
    private static final String firebaseMessagingScope = "https://www.googleapis.com/auth/firebase.messaging";

    public static String getAccessToken() {
        try {
            String jsonString = "{\n" +
                    "  \"type\": \"service_account\",\n" +
                    "  \"project_id\": \"careercampus-3e43a\",\n" +
                    "  \"private_key_id\": \"06d95cacaaf87755bd9892a9482c3c0e7abb9b7f\",\n" +
                    "  \"private_key\": \"-----BEGIN PRIVATE KEY-----\\nMIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCyaQwayFpO0JHB\\ndvn+tf7w8kZu/sLT2d23rggS9+Uvjtf1KIYUk8lbG4miEV57g0F/U5uCciFIf4TZ\\nL7K+RbwZITpz1Dt+RfnPh8TSCwjY9BSnZljPwkOu4z8jnyyfridoshLS5rVxcJd6\\nxQw5yyrJgyWxQRCtV2Vqks7pal7RFOeFGt87YJ3wx1r0LsbPTCueqEETKo/kG0pi\\n+8doE2Sqp/ARZaTMMIZOsx2wZpzMNNLgasIJ6aZGNnlS/lEsXSM3Rdj+K1xYExQq\\nXYX+PcSwVGLbTpepRTexO3Cz+tLCNI/BAPTcJBKENDjsO4QPVQdO7sTsJBP1WkBU\\n4gdoy1cpAgMBAAECggEAIwcS1FNPv7edVlGIpaSPyDiY8YPdNoDI0h4+AZNgEOVH\\nqx9oq7OUTenrLMGiPje37Uq59nN362b16zo9KrRASOLIOiOKBrGwaTlrwB1ggE/z\\nmiYy+59VcIm3RMQkm7kyjZCgt32KVBpZ80Wqr9IetW+lP9bZcl79T15Bd3xl30Ca\\nH+ZGZRp+tl31a3qJwFvmv9ea8uOTYely5OEgk+i6WQhJPv8cJH/bk2e7+wn6bJ63\\nUNSOIyW4pjTNuxiGKEzdMO4O0iIH/oZJ+WLR7DpxzLAi4fpNTOJw12RAWBFOv8ae\\nBcMZ8QApHbvoVYQHfElW8+cIIYwPTA0FYQZyzPF/TQKBgQDrdiEvu5fUkjjw9OfK\\nDWUTZ5ReSWMp80+4oVeQSwS++eyXchJyxegmyLDqCNcpLZ+LDpnEEv2MkFLju8M2\\nX3pumiZNZ+SF56NsU4ithPy6pnTbuY7CZvvtlhHlSAnC8NWIhhuAo4qhKTr2g2Rs\\nCvdG03IRHEob5eSwgKc/Uav3wwKBgQDB+PZqsRaki3zZqfb2LIgR35vCRL6olP/C\\nkwZpgk0YX/ZAlSGXnt0xUe9aS/sE8mG2NfmFQyVnkH7khQVmlk+Q3XELLOQr669z\\no5BboE1k3EsDidImzu9s4yxqNYiNcErCJOOW9Q0JEDl2RQU7dxbWmgwmnpDLjRhh\\nh8sMyG2yowKBgQCSYL/E0tRSvAxWD4pBXA5n/86Gxwy0uaPn3bV36IssxmZEyA+b\\n9liaM8130MMhwFEcu5jwAfZJ+jbtiH6VQMn2FDKXkGzKqfQ4lb20UaZjyrN/HV4Y\\nkLkxPEgnx57554p38xcJ46F9Cwqy0YAOgsqy5yRUasfPjB2ddH2EqZtEzQKBgQC7\\n0zupLDlks7z9d9svnQdVZ1UH1FFaJI1T99IpsIa+p41oFAAnNSmybEh9Az3aGGoN\\nak5ngBFkK0L0POpx3/LDr+majKmU7e9C5osObjcDB12L25SnB5JHueJ0JcpISwfY\\n3qeSsU8iC5fYshTS9AolW52Qd9J1h/an2jn/8sRmSQKBgEQ5sBS46NwD7lqZ/xuo\\nK/V7w81EPPuOkkjtOqGuwpu5LUsRC2Ip7UeyFLFk3RKxXkdUMN77j2IG9xJRrUD9\\noLSZXvx/XMiGFF3/2+HpxuKGvODyvFexOIOnmMbJjIxLODLviL23kbp8d2qnqJMH\\nPPBA/emzNthrtYAb8ffzbmB4\\n-----END PRIVATE KEY-----\\n\",\n" +
                    "  \"client_email\": \"firebase-adminsdk-kk5l8@careercampus-3e43a.iam.gserviceaccount.com\",\n" +
                    "  \"client_id\": \"100799209000920444395\",\n" +
                    "  \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",\n" +
                    "  \"token_uri\": \"https://oauth2.googleapis.com/token\",\n" +
                    "  \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\",\n" +
                    "  \"client_x509_cert_url\": \"https://www.googleapis.com/robot/v1/metadata/x509/firebase-adminsdk-kk5l8%40careercampus-3e43a.iam.gserviceaccount.com\",\n" +
                    "  \"universe_domain\": \"googleapis.com\"\n" +
                    "}";
            InputStream stream = new ByteArrayInputStream(jsonString.getBytes(StandardCharsets.UTF_8));
            GoogleCredentials googleCredentials = GoogleCredentials.fromStream(stream).
                    createScoped(Arrays.asList(firebaseMessagingScope));
            googleCredentials.refresh();
            return googleCredentials.getAccessToken().getTokenValue();

        } catch (Exception e) {
            Log.e("error",""+e.getMessage());
            return null;
        }

    }
}
