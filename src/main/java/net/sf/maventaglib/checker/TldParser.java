/*
 * The MIT License
 * Copyright © 2004-2014 Fabrizio Giustina
 * Copyright © 2022-2024 Web-Legacy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package net.sf.maventaglib.checker;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import net.sf.maventaglib.util.XmlHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Reads a tld file and generates a Tld object. This "manual" parser takes in account different
 * versions of the tlds
 *
 * @author Fabrizio Giustina
 */
public final class TldParser {

    /**
     * don't instantiate.
     */
    private TldParser() {
        // unused
    }

    /**
     * Parse a Tld object from a document.
     *
     * @param tldDoc  Document for the parsed tld
     * @param tldName of the tld file
     *
     * @return Tld instance
     */
    public static Tld parse(Document tldDoc, String tldName) {
        String version;
        String shortname = null;

        //
        // descriptionGroup
        Map<String, String> description = new TreeMap<>();
        Map<String, String> displayName = new TreeMap<>();
        Map<String, Icon> icon = new TreeMap<>();

        String tlibversion = null;
        String uri = null;
        Set<Tag> tags = new TreeSet<>();
        Set<ElFunction> functions = new TreeSet<>();
        Set<TagFile> tagfiles = new TreeSet<>();

        Node taglib = tldDoc.getElementsByTagName("taglib").item(0);
        version = XmlHelper.getAttribute(taglib, "version");

        NodeList tagList = taglib.getChildNodes();
        for (int i = 0; i < tagList.getLength(); i++) {
            Node tagNode = tagList.item(i);

            if (tagNode != null && tagNode.getNodeName() != null) {
                switch (tagNode.getNodeName()) {
                    case "info":
                    case "description":
                        description.put(
                                XmlHelper.getAttribute(tagNode, "lang"),
                                XmlHelper.getTextContent(tagNode)
                        );
                        break;

                    case "display-name": {
                        final String lang = XmlHelper.getAttribute(tagNode, "lang");
                        Node child = tagNode.getFirstChild();
                        if (child != null) {
                            displayName.put(lang, child.getNodeValue());
                        }
                        break;
                    }

                    case "icon": {
                        icon.put(lang, parseIcon(tagNode));
                        break;
                    }

                    case "tlib-version":
                    case "tlibversion": {
                        Node child = tagNode.getFirstChild();
                        if (child != null) {
                            tlibversion = child.getNodeValue();
                        }
                        break;
                    }

                    case "shortname":
                    case "short-name": {
                        Node child = tagNode.getFirstChild();
                        if (child != null) {
                            shortname = child.getNodeValue();
                        }
                        break;
                    }

                    case "uri": {
                        Node child = tagNode.getFirstChild();
                        if (child != null) {
                            uri = child.getNodeValue();
                        }
                        break;
                    }

                    case "validator": {
                        //0:1 - validatorType
                        Tag tag = parseValidator(tagNode);
                        tags.add(tag);
                        break;
                    }

                    case "listener": {
                        // 0:n - listenerType
                        break;
                    }

                    case "tag": {
                        Tag tag = parseTag(tagNode);
                        tags.add(tag);
                        break;
                    }

                    case "tag-file": {
                        TagFile tag = parseTagFile(tagNode);
                        tagfiles.add(tag);
                        break;
                    }

                    case "function": {
                        ElFunction tag = parseFunction(tagNode);
                        functions.add(tag);
                        break;
                    }

                    case "taglib-extension": {
                        // 0:n - tld-extensionType
                        TagFile tag = parseTagFile(tagNode);
                        tagfiles.add(tag);
                        break;
                    }

                    default:
                        break;
                }
            }
        }

        Tld tld = new Tld(new LangElement<>(description, ""), new LangElement<>(displayName, ""),
                new LangElement<>(icon, null),
                name, shortname, tldName, tlibversion, uri, info, tags, tagfiles, functions);
        return tld;
    }

    /**
     * Parse a {@code tag-file} element.
     *
     * @param tagNode Node
     *
     * @return a Tag-File instance
     */
    private static Icon parseIcon(Node tagNode) {
        final Icon.Builder builder = new Icon.Builder();
        builder
                .lang(XmlHelper.getAttribute(tagNode, "lang"))
                .id(XmlHelper.getAttribute(tagNode, "id"));

        final NodeList tagAttributes = tagNode.getChildNodes();
        for (int k = 0; k < tagAttributes.getLength(); k++) {
            Node tagAttribute = tagAttributes.item(k);
            String nodeName = tagAttribute.getNodeName();

            if (nodeName != null) {
                switch (nodeName) {
                    case "small-icon":
                        builder.smallIcon(tagAttribute.getFirstChild().getNodeValue());
                        break;
                    case "large-icon":
                        builder.largeIcon(tagAttribute.getFirstChild().getNodeValue());
                        break;
                    default:
                        break;
                }
            }
        }

        return builder.build();
    }

    /**
     * Parse a {@code tag-file} element.
     *
     * @param tagNode Node
     *
     * @return a Tag-File instance
     */
    private static TagFile parseTagFile(Node tagNode) {
        TagFile tag = new TagFile();

        NodeList tagAttributes = tagNode.getChildNodes();

        for (int k = 0; k < tagAttributes.getLength(); k++) {
            Node tagAttribute = tagAttributes.item(k);
            String nodeName = tagAttribute.getNodeName();

            if (nodeName != null) {
                switch (nodeName) {
                    case "name":
                        // tag class name
                        tag.setName(tagAttribute.getFirstChild().getNodeValue());
                        break;
                    case "description":
                        tag.setDescription(XmlHelper.getTextContent(tagAttribute));
                        break;
                    case "path":
                        tag.setPath(tagAttribute.getFirstChild().getNodeValue());
                        break;
                    case "example":
                        tag.setExample(XmlHelper.getTextContent(tagAttribute));
                        break;
                    default:
                        break;
                }
            }
        }

        tag.setDeprecated(StringUtils.contains(tag.getDescription(), "@deprecated"));

        return tag;
    }

    /**
     * Parse a {@code function} element.
     *
     * @param tagNode Node
     *
     * @return a ElFunction instance
     */
    private static ElFunction parseFunction(Node tagNode) {
        ElFunction tag = new ElFunction();
        NodeList tagAttributes = tagNode.getChildNodes();

        for (int k = 0; k < tagAttributes.getLength(); k++) {
            Node tagAttribute = tagAttributes.item(k);
            String nodeName = tagAttribute.getNodeName();

            if (nodeName != null) {
                switch (nodeName) {
                    case "name":
                        tag.setName(tagAttribute.getFirstChild().getNodeValue());
                        break;
                    case "description":
                        tag.setDescription(XmlHelper.getTextContent(tagAttribute));
                        break;
                    case "example":
                        tag.setExample(XmlHelper.getTextContent(tagAttribute));
                        break;
                    case "function-class":
                        tag.setFunctionClass(
                                StringUtils.trim(XmlHelper.getTextContent(tagAttribute)));
                        break;
                    case "function-signature":
                        String signature = XmlHelper.getTextContent(tagAttribute);
                        tag.setFunctionSignature(signature);
                        tag.setParameters(StringUtils.substringBetween(signature, "(", ")"));
                        break;
                    default:
                        break;
                }
            }
        }

        tag.setDeprecated(StringUtils.contains(tag.getDescription(), "@deprecated"));

        return tag;
    }

    /**
     * Parse a {@code tag} element.
     *
     * @param tagNode Node
     *
     * @return a Tag instance
     */
    private static Tag parseTag(Node tagNode) {
        Tag tag = new Tag();
        Set<TagAttribute> attributes = new TreeSet<>();
        Set<TagVariable> variables = new TreeSet<>();
        NodeList tagAttributes = tagNode.getChildNodes();

        for (int k = 0; k < tagAttributes.getLength(); k++) {
            Node tagAttribute = tagAttributes.item(k);
            String nodeName = tagAttribute.getNodeName();

            if (nodeName != null) {
                switch (nodeName) {
                    case "name":
                        // tag class name
                        tag.setName(tagAttribute.getFirstChild().getNodeValue());
                        break;
                    case "description":
                    case "info":
                        tag.setDescription(XmlHelper.getTextContent(tagAttribute));
                        break;
                    case "tag-class":
                    case "tagclass":
                        tag.setTagClass(StringUtils.trim(
                                tagAttribute.getFirstChild().getNodeValue()));
                        break;
                    case "body-content":
                    case "bodycontent":
                        tag.setBodycontent(tagAttribute.getFirstChild().getNodeValue());
                        break;
                    case "example":
                        tag.setExample(XmlHelper.getTextContent(tagAttribute));
                        break;
                    case "tei-class":
                    case "teiclass":
                        // tei class name
                        tag.setTeiClass(StringUtils.trim(
                                tagAttribute.getFirstChild().getNodeValue()));
                        break;
                    case "attribute":
                        TagAttribute attribute = parseTagAttribute(tagAttribute);
                        attributes.add(attribute);
                        break;
                    case "variable":
                        TagVariable variable = parseTagVariable(tagAttribute);
                        variables.add(variable);
                        break;
                    default:
                        break;
                }
            }
            tag.setAttributes(attributes.toArray(new TagAttribute[0]));
            tag.setVariables(variables.toArray(new TagVariable[0]));
        }

        tag.setDeprecated(StringUtils.contains(tag.getDescription(), "@deprecated"));

        return tag;
    }

    /**
     * Parse an {@code attribute} element.
     *
     * @param tagAttribute Node
     *
     * @return TagAttribute instance
     */
    private static TagAttribute parseTagAttribute(Node tagAttribute) {
        TagAttribute attribute = new TagAttribute();

        NodeList attributeParams = tagAttribute.getChildNodes();
        for (int z = 0; z < attributeParams.getLength(); z++) {
            Node param = attributeParams.item(z);
            if (param.getNodeType() != Node.TEXT_NODE && param.hasChildNodes()
                    && param.getNodeName() != null) {

                switch (param.getNodeName()) {
                    case "name":
                        attribute.setName(param.getFirstChild().getNodeValue());
                        break;
                    case "type":
                        attribute.setType(StringUtils.trim(param.getFirstChild().getNodeValue()));
                        break;
                    case "description":
                        attribute.setDescription(XmlHelper.getTextContent(param));
                        break;
                    case "required":
                        attribute.setRequired(StringUtils.contains(StringUtils.lowerCase(
                                param.getFirstChild().getNodeValue()), "true"));
                        break;
                    case "rtexprvalue":
                        attribute.setRtexprvalue(StringUtils.contains(StringUtils.lowerCase(
                                param.getFirstChild().getNodeValue()), "true"));
                        break;
                    default:
                        break;
                }
            }
        }

        attribute.setDeprecated(StringUtils.contains(attribute.getDescription(), "@deprecated"));

        return attribute;
    }

    /**
     * Parse an {@code attribute} element.
     *
     * @param node Node
     *
     * @return TagAttribute instance
     */
    private static TagVariable parseTagVariable(Node node) {
        TagVariable variable = new TagVariable();

        NodeList attributeParams = node.getChildNodes();
        for (int z = 0; z < attributeParams.getLength(); z++) {
            Node param = attributeParams.item(z);
            if (param.getNodeType() != Node.TEXT_NODE && param.hasChildNodes()
                    && param.getNodeName() != null) {

                switch (param.getNodeName()) {
                    case "name-given":
                        variable.setNameGiven(param.getFirstChild().getNodeValue());
                        break;
                    case "name-from-attribute":
                        variable.setNameFromAttribute(param.getFirstChild().getNodeValue());
                        break;
                    case "variable-class":
                        variable.setType(param.getFirstChild().getNodeValue());
                        break;
                    case "scope":
                        variable.setScope(param.getFirstChild().getNodeValue());
                        break;
                    case "description":
                    case "info":
                        variable.setDescription(XmlHelper.getTextContent(param));
                        break;
                    default:
                        break;
                }
            }
        }

        variable.setDeprecated(StringUtils.contains(variable.getDescription(), "@deprecated"));

        return variable;
    }
}
