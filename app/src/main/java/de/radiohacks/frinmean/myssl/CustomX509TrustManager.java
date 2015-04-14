package de.radiohacks.frinmean.myssl;


import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import de.radiohacks.frinmean.FrinmeanApplication;

public class CustomX509TrustManager implements X509TrustManager {

    private final X509TrustManager originalX509TrustManager;
    private final KeyStore trustStore;

    /**
     * @param trustStore A KeyStore containing the server certificate that should be trusted
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     */
    public CustomX509TrustManager(KeyStore trustStore) throws NoSuchAlgorithmException, KeyStoreException {
        this.trustStore = trustStore;

        TrustManagerFactory originalTrustManagerFactory = TrustManagerFactory.getInstance("X509");
        originalTrustManagerFactory.init((KeyStore) null);

        TrustManager[] originalTrustManagers = originalTrustManagerFactory.getTrustManagers();
        originalX509TrustManager = (X509TrustManager) originalTrustManagers[0];
    }
    /*public void checkClientTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
    }*/

    /**
     * No-op. Never invoked by client, only used in server-side implementations
     *
     * @return
     */
    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws java.security.cert.CertificateException {
    }



    /* @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws java.security.cert.CertificateException {
        try {
            originalX509TrustManager.checkServerTrusted(chain, authType);
        } catch (CertificateException originalException) {
            try {
                X509Certificate[] reorderedChain = reorderCertificateChain(chain);
                CertPathValidator validator = CertPathValidator.getInstance("PKIX");
                CertificateFactory factory = CertificateFactory.getInstance("X509");
                CertPath certPath = factory.generateCertPath(Arrays.asList(reorderedChain));
                PKIXParameters params = new PKIXParameters(trustStore);
                params.setRevocationEnabled(false);
                validator.validate(certPath, params);
            } catch (Exception ex) {
                throw originalException;
            }
        }

    } */

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }

    /**
     * Puts the certificate chain in the proper order, to deal with out-of-order
     * certificate chains as are sometimes produced by Apache's mod_ssl
     *
     * @param chain the certificate chain, possibly with bad ordering
     * @return the re-ordered certificate chain
     */
    private X509Certificate[] reorderCertificateChain(X509Certificate[] chain) {

        X509Certificate[] reorderedChain = new X509Certificate[chain.length];
        List<X509Certificate> certificates = Arrays.asList(chain);

        int position = chain.length - 1;
        X509Certificate rootCert = findRootCert(certificates);
        reorderedChain[position] = rootCert;

        X509Certificate cert = rootCert;
        while ((cert = findSignedCert(cert, certificates)) != null && position > 0) {
            reorderedChain[--position] = cert;
        }

        return reorderedChain;
    }

    /**
     * A helper method for certificate re-ordering.
     * Finds the root certificate in a possibly out-of-order certificate chain.
     *
     * @param certificates the certificate change, possibly out-of-order
     * @return the root certificate, if any, that was found in the list of certificates
     */
    private X509Certificate findRootCert(List<X509Certificate> certificates) {
        X509Certificate rootCert = null;

        for (X509Certificate cert : certificates) {
            X509Certificate signer = findSigner(cert, certificates);
            if (signer == null || signer.equals(cert)) { // no signer present, or self-signed
                rootCert = cert;
                break;
            }
        }

        return rootCert;
    }

    /**
     * A helper method for certificate re-ordering.
     * Finds the first certificate in the list of certificates that is signed by the sigingCert.
     */
    private X509Certificate findSignedCert(X509Certificate signingCert, List<X509Certificate> certificates) {
        X509Certificate signed = null;

        for (X509Certificate cert : certificates) {
            Principal signingCertSubjectDN = signingCert.getSubjectDN();
            Principal certIssuerDN = cert.getIssuerDN();
            if (certIssuerDN.equals(signingCertSubjectDN) && !cert.equals(signingCert)) {
                signed = cert;
                break;
            }
        }

        return signed;
    }

    /**
     * A helper method for certificate re-ordering.
     * Finds the certificate in the list of certificates that signed the signedCert.
     */
    private X509Certificate findSigner(X509Certificate signedCert, List<X509Certificate> certificates) {
        X509Certificate signer = null;

        for (X509Certificate cert : certificates) {
            Principal certSubjectDN = cert.getSubjectDN();
            Principal issuerDN = signedCert.getIssuerDN();
            if (certSubjectDN.equals(issuerDN)) {
                signer = cert;
                break;
            }
        }

        return signer;
    }

    public void checkServerTrusted(java.security.cert.X509Certificate[] certs,
                                   String authType) throws CertificateException {

        // Here you can verify the servers certificate. (e.g. against one which is stored on mobile device)

        InputStream inStream = null;
        try {
            inStream = FrinmeanApplication.loadCertAsInputStream();
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate ca = (X509Certificate)
                    cf.generateCertificate(inStream);
            inStream.close();

            for (X509Certificate cert : certs) {
                // // Verifing by public key
                cert.verify(ca.getPublicKey());
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Untrusted Certificate!");
        } finally {
            try {
                assert inStream != null;
                inStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

/*    public X509Certificate[] getAcceptedIssuers() {
        return null;
    }
*/
}


