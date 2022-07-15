package com.itextpdf.bouncycastle.cms.jcajce;

import com.itextpdf.bouncycastle.cms.SignerInformationVerifierBC;
import com.itextpdf.bouncycastle.operator.OperatorCreationExceptionBC;
import com.itextpdf.commons.bouncycastle.cms.ISignerInformationVerifier;
import com.itextpdf.commons.bouncycastle.cms.jcajce.IJcaSimpleSignerInfoVerifierBuilder;

import java.security.cert.X509Certificate;
import java.util.Objects;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.operator.OperatorCreationException;

/**
 * Wrapper class for {@link JcaSimpleSignerInfoVerifierBuilder}.
 */
public class JcaSimpleSignerInfoVerifierBuilderBC implements IJcaSimpleSignerInfoVerifierBuilder {
    private final JcaSimpleSignerInfoVerifierBuilder verifierBuilder;

    /**
     * Creates new wrapper instance for {@link JcaSimpleSignerInfoVerifierBuilder}.
     *
     * @param verifierBuilder {@link JcaSimpleSignerInfoVerifierBuilder} to be wrapped
     */
    public JcaSimpleSignerInfoVerifierBuilderBC(JcaSimpleSignerInfoVerifierBuilder verifierBuilder) {
        this.verifierBuilder = verifierBuilder;
    }

    /**
     * Gets actual org.bouncycastle object being wrapped.
     *
     * @return wrapped {@link JcaSimpleSignerInfoVerifierBuilder}.
     */
    public JcaSimpleSignerInfoVerifierBuilder getVerifierBuilder() {
        return verifierBuilder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IJcaSimpleSignerInfoVerifierBuilder setProvider(String provider) {
        verifierBuilder.setProvider(provider);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ISignerInformationVerifier build(X509Certificate certificate) throws OperatorCreationExceptionBC {
        try {
            return new SignerInformationVerifierBC(verifierBuilder.build(certificate));
        } catch (OperatorCreationException e) {
            throw new OperatorCreationExceptionBC(e);
        }
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
        JcaSimpleSignerInfoVerifierBuilderBC that = (JcaSimpleSignerInfoVerifierBuilderBC) o;
        return Objects.equals(verifierBuilder, that.verifierBuilder);
    }

    /**
     * Returns a hash code value based on the wrapped object.
     */
    @Override
    public int hashCode() {
        return Objects.hash(verifierBuilder);
    }

    /**
     * Delegates {@code toString} method call to the wrapped object.
     */
    @Override
    public String toString() {
        return verifierBuilder.toString();
    }
}