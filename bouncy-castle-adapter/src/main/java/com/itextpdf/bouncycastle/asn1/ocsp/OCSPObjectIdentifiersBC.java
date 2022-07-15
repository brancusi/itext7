package com.itextpdf.bouncycastle.asn1.ocsp;

import com.itextpdf.bouncycastle.asn1.ASN1ObjectIdentifierBC;
import com.itextpdf.commons.bouncycastle.asn1.IASN1ObjectIdentifier;
import com.itextpdf.commons.bouncycastle.asn1.ocsp.IOCSPObjectIdentifiers;

import java.util.Objects;
import org.bouncycastle.asn1.ocsp.OCSPObjectIdentifiers;

/**
 * Wrapper class for {@link OCSPObjectIdentifiers}.
 */
public class OCSPObjectIdentifiersBC implements IOCSPObjectIdentifiers {
    private static final OCSPObjectIdentifiersBC INSTANCE = new OCSPObjectIdentifiersBC(null);

    private static final IASN1ObjectIdentifier ID_PKIX_OCSP_BASIC =
            new ASN1ObjectIdentifierBC(OCSPObjectIdentifiers.id_pkix_ocsp_basic);

    private static final IASN1ObjectIdentifier ID_PKIX_OCSP_NONCE =
            new ASN1ObjectIdentifierBC(OCSPObjectIdentifiers.id_pkix_ocsp_nonce);

    private static final IASN1ObjectIdentifier ID_PKIX_OCSP_NOCHECK =
            new ASN1ObjectIdentifierBC(OCSPObjectIdentifiers.id_pkix_ocsp_nocheck);

    private final OCSPObjectIdentifiers ocspObjectIdentifiers;

    /**
     * Creates new wrapper instance for {@link OCSPObjectIdentifiers}.
     *
     * @param ocspObjectIdentifiers {@link OCSPObjectIdentifiers} to be wrapped
     */
    public OCSPObjectIdentifiersBC(OCSPObjectIdentifiers ocspObjectIdentifiers) {
        this.ocspObjectIdentifiers = ocspObjectIdentifiers;
    }

    /**
     * Gets wrapper instance.
     *
     * @return {@link OCSPObjectIdentifiersBC} instance.
     */
    public static OCSPObjectIdentifiersBC getInstance() {
        return INSTANCE;
    }

    /**
     * Gets actual org.bouncycastle object being wrapped.
     *
     * @return wrapped {@link OCSPObjectIdentifiers}.
     */
    public OCSPObjectIdentifiers getOCSPObjectIdentifiers() {
        return ocspObjectIdentifiers;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IASN1ObjectIdentifier getIdPkixOcspBasic() {
        return ID_PKIX_OCSP_BASIC;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IASN1ObjectIdentifier getIdPkixOcspNonce() {
        return ID_PKIX_OCSP_NONCE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IASN1ObjectIdentifier getIdPkixOcspNoCheck() {
        return ID_PKIX_OCSP_NOCHECK;
    }

    /**
     * Indicates whether some other object is "equal to" this one. Compares wrapped objects.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OCSPObjectIdentifiersBC that = (OCSPObjectIdentifiersBC) o;
        return Objects.equals(ocspObjectIdentifiers, that.ocspObjectIdentifiers);
    }

    /**
     * Returns a hash code value based on the wrapped object.
     */
    @Override
    public int hashCode() {
        return Objects.hash(ocspObjectIdentifiers);
    }

    /**
     * Delegates {@code toString} method call to the wrapped object.
     */
    @Override
    public String toString() {
        return ocspObjectIdentifiers.toString();
    }
}