package io.github.manoelcampos.java2ts.javadoc.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * A package element inside the xml-doclet output, mapped using JAXB.
 * @author Manoel Campos
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class PackageXml {
    /**
     * Creates a {@link PackageXml} (used by JAXB).
     */
    public PackageXml() {/**/}

    @XmlElement(name = "class")
    private List<TypeXml> classes = new ArrayList<>();

    @XmlElement(name = "interface")
    private List<TypeXml> interfaces = new ArrayList<>();

    @XmlElement(name = "enum")
    private List<TypeXml> enums = new ArrayList<>();

    /**
     * {@return all classes, interfaces and enums inside the package}
     */
    public Stream<TypeXml> types() {
        return Stream.of(classes, interfaces, enums).flatMap(List::stream);
    }
}
