package com.itextpdf.model;

public interface IPropertyContainer<Type extends IPropertyContainer> {

    /**
     * Checks if this entity has the specified property. Compared to {@link #hasOwnProperty(Property)},
     * this method can check parent's properties, styles, etc, depending on the origin of the instance
     * @param property the property to be checked
     * @return {@code true} if this instance has given property, {@code false} otherwise
     */
    boolean hasProperty(Property property);

    /**
     * Checks if this entity has the specified property, i.e. if it was set to this very element earlier
     * @param property the property to be checked
     * @return {@code true} if this instance has given own property, {@code false} otherwise
     */
    boolean hasOwnProperty(Property property);

    /**
     * Gets the property from this entity. Compared to {@link #getOwnProperty(Property)},
     * this method can check parent's properties, styles, etc, depending on the origin of the instance
     * @param property the property to be retrieved
     * @return the value of the given property. {@code null} will be returned if the property value was not found
     */
    <T> T getProperty(Property property);

    /**
     * Gets own property from this entity. The property must have been set earlier to this entity.
     * If the property is not found, {@code null} will be returned.
     * @param property he property to be retrieved
     * @return the value of the given own property. {@code null} will be returned if the property value was not found
     */
    <T> T getOwnProperty(Property property);

    /**
     * Gets the default property from this entity.
     * @param property the property to be retrieved
     * @return the default property value. If the default property is not defined, {@code null} will be returned
     */
    <T> T getDefaultProperty(Property property);

    /**
     * Sets a property for this entity.
     * @param property the property to be set
     * @param value the value of the property
     * @return this element
     */
    <T extends Type> T setProperty(Property property, Object value);

    /**
     * Deleted the own property of this entity.
     * @param property the property to be deleted
     */
    void deleteOwnProperty(Property property);
}
