package com.itextpdf.bouncycastlefips.cert;

import com.itextpdf.bouncycastlefips.asn1.x500.X500NameBCFips;
import com.itextpdf.bouncycastlefips.operator.ContentSignerBCFips;
import com.itextpdf.commons.bouncycastle.asn1.x500.IX500Name;
import com.itextpdf.commons.bouncycastle.cert.IX509CRLHolder;
import com.itextpdf.commons.bouncycastle.cert.IX509v2CRLBuilder;
import com.itextpdf.commons.bouncycastle.operator.IContentSigner;

import java.math.BigInteger;
import java.util.Date;
import java.util.Objects;
import org.bouncycastle.cert.X509v2CRLBuilder;

/**
 * Wrapper class for {@link X509v2CRLBuilder}.
 */
public class X509v2CRLBuilderBCFips implements IX509v2CRLBuilder {
    private final X509v2CRLBuilder builder;

    /**
     * Creates new wrapper instance for {@link X509v2CRLBuilder}.
     *
     * @param builder {@link X509v2CRLBuilder} to be wrapped
     */
    public X509v2CRLBuilderBCFips(X509v2CRLBuilder builder) {
        this.builder = builder;
    }

    /**
     * Creates new wrapper instance for {@link X509v2CRLBuilder}.
     *
     * @param x500Name X500Name wrapper to create {@link X509v2CRLBuilder}
     * @param date     Date to create {@link X509v2CRLBuilder}
     */
    public X509v2CRLBuilderBCFips(IX500Name x500Name, Date date) {
        this(new X509v2CRLBuilder(((X500NameBCFips) x500Name).getX500Name(), date));
    }

    /**
     * Gets actual org.bouncycastle object being wrapped.
     *
     * @return wrapped {@link X509v2CRLBuilder}.
     */
    public X509v2CRLBuilder getBuilder() {
        return builder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IX509v2CRLBuilder addCRLEntry(BigInteger bigInteger, Date date, int i) {
        builder.addCRLEntry(bigInteger, date, i);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IX509v2CRLBuilder setNextUpdate(Date nextUpdate) {
        builder.setNextUpdate(nextUpdate);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IX509CRLHolder build(IContentSigner signer) {
        return new X509CRLHolderBCFips(builder.build(((ContentSignerBCFips) signer).getContentSigner()));
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
        X509v2CRLBuilderBCFips that = (X509v2CRLBuilderBCFips) o;
        return Objects.equals(builder, that.builder);
    }

    /**
     * Returns a hash code value based on the wrapped object.
     */
    @Override
    public int hashCode() {
        return Objects.hash(builder);
    }

    /**
     * Delegates {@code toString} method call to the wrapped object.
     */
    @Override
    public String toString() {
        return builder.toString();
    }
}