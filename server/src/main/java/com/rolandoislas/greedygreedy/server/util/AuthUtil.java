package com.rolandoislas.greedygreedy.server.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.goebl.david.Webb;
import com.goebl.david.WebbException;
import com.rolandoislas.greedygreedy.core.data.Constants;
import com.rolandoislas.greedygreedy.core.util.Logger;
import org.bouncycastle.util.encoders.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

public class AuthUtil {
    private static JWTVerifier verifier;

    static {
        Webb webb = Webb.create();
        JSONObject keyset = webb.get(Constants.AUTH0_KEYSET).ensureSuccess().asJsonObject().getBody();
        try {
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
                    .build();
        } catch (Exception e) {
            Logger.exception(e);
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
}
