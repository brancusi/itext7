package com.itextpdf.bouncycastle.asn1;

import com.itextpdf.commons.bouncycastle.asn1.IASN1Enumerated;

import org.bouncycastle.asn1.ASN1Enumerated;

/**
 * Wrapper class for {@link ASN1Enumerated}.
 */
public class ASN1EnumeratedBC extends ASN1PrimitiveBC implements IASN1Enumerated {
    /**
     * Creates new wrapper instance for {@link ASN1Enumerated}.
     *
     * @param asn1Enumerated {@link ASN1Enumerated} to be wrapped
     */
    public ASN1EnumeratedBC(ASN1Enumerated asn1Enumerated) {
        super(asn1Enumerated);
    }

    /**
     * Creates new wrapper instance for {@link ASN1Enumerated}.
     *
     * @param i int value to create {@link ASN1Enumerated} to be wrapped
     */
    public ASN1EnumeratedBC(int i) {
        super(new ASN1Enumerated(i));
    }

    /**
     * Gets actual org.bouncycastle object being wrapped.
     *
     * @return wrapped {@link ASN1Enumerated}.
     */
    public ASN1Enumerated getASN1Enumerated() {
        return (ASN1Enumerated) getEncodable();
    }
}