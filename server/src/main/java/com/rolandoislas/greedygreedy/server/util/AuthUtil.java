package com.rolandoislas.greedygreedy.server.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.goebl.david.Webb;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.rolandoislas.greedygreedy.core.data.Constants;
import com.rolandoislas.greedygreedy.core.util.GreedyException;
import com.rolandoislas.greedygreedy.core.util.Logger;
import com.rolandoislas.greedygreedy.server.GreedySparkServerApi;
import org.bouncycastle.util.encoders.Base64;
import org.json.JSONObject;
import spark.Request;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;

public class AuthUtil {
    private static JWTVerifier verifier;

    static {
        Webb webb = Webb.create();
        try {
            JSONObject keyset = webb.get(Constants.AUTH0_KEYSET).ensureSuccess().asJsonObject().getBody();
            byte[] publicKeyBytes = Base64.decode(keyset.getJSONArray("keys").getJSONObject(0).getJSONArray("x5c")
                    .getString(0));

            ByteArrayInputStream bytesStream = new ByteArrayInputStream(publicKeyBytes);
            CertificateFactory cf = CertificateFactory.getInstance("X509");
            X509Certificate certificate =
                    (X509Certificate) cf.generateCertificate(bytesStream);
            RSAPublicKey publicKey = (RSAPublicKey) certificate.getPublicKey();
            Algorithm algorithm = Algorithm.RSA256(publicKey);
            verifier = JWT.require(algorithm)
                    .withIssuer(Constants.AUTH0_DOMAIN)
                    .withAudience(Constants.AUTH0_AUDIENCE)
                    .withClaim("scope", Constants.AUTH0_SCOPE)
                    .build();
        } catch (Exception e) {
            Logger.exception(e);
            e.printStackTrace();
            System.exit(e.hashCode());
        }
    }

    public static boolean verify(String token) {
        try {
            verifier.verify(token);
        }
        catch (JWTVerificationException e) {
            return false;
        }
        return true;
    }

    public static boolean verify(Request request) {
        return verify(extractToken(request));
    }

    public static String extractToken(Request request) {
        if (!request.headers().contains("Authentication"))
            return "";
        String[] auth = request.headers("Authentication").split(" ");
        if (auth.length == 2 && !auth[1].isEmpty())
            return auth[1];
        return "";
    }

    public static String getOauthId(String token) {
        try {
            DecodedJWT jwt = verifier.verify(token);
            return jwt.getSubject();
        }
        catch (JWTVerificationException e) {
            return null;
        }
    }

    public static String getName(String token) {
        String noName = "Best Player Ever";
        JsonObject userInfo;
        try {
            userInfo = new Gson().fromJson(GreedySparkServerApi.getUserInfo(token), JsonObject.class);
        }
        catch (JsonSyntaxException | GreedyException e) {
            return noName;
        }
        if (userInfo == null) {
            return noName;
        }
        if (userInfo.has("nickname") && userInfo.get("nickname").getAsString().length() >= 3)
            return userInfo.get("nickname").getAsString();
        else if (userInfo.get("sub").getAsString().startsWith("google"))
            return "A Google User";
        else if (userInfo.get("sub").getAsString().startsWith("facebook"))
            return "A Facebook User";
        else {
            return noName;
        }
    }
}
