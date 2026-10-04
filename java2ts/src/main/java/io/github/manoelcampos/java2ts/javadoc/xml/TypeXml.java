package io.github.manoelcampos.java2ts.javadoc.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;

import java.util.ArrayList;
import java.util.List;

/**
 * A class, interface or enum element inside the xml-doclet output, mapped using JAXB.
 * @author Manoel Campos
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class TypeXml extends DocumentedXml {
    /**
     * Creates a {@link TypeXml} (used by JAXB).
     */
    public TypeXml() {/**/}

    /** The canonical name of the type (such as {@code com.company.Outer.Inner}). */
    @XmlAttribute
    private String qualified = "";

    @XmlElement(name = "field")
    private List<MemberXml> fields = new ArrayList<>();

    @XmlElement(name = "method")
    private List<MemberXml> methods = new ArrayList<>();

    /**
     * {@return the canonical name of the type (such as {@code com.company.Outer.Inner})}
     * The xml-doclet includes type parameters in the name of generic types (such as {@code com.company.Page<T>}),
     * which are removed.
     */
    public String getQualified() {
        final int genericsStart = qualified.indexOf('<');
        return genericsStart < 0 ? qualified : qualified.substring(0, genericsStart);
    }

    /**
     * {@return the type fields}
     */
    public List<MemberXml> getFields() {
        return fields;
    }

    /**
     * {@return the type methods}
     */
    public List<MemberXml> getMethods() {
        return methods;
    }
}
