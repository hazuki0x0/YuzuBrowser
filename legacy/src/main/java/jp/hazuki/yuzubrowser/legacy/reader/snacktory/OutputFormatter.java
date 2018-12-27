/*
 * Copyright (C) 2017 Hazuki
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jp.hazuki.yuzubrowser.legacy.reader.snacktory;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import jp.hazuki.yuzubrowser.legacy.utils.HtmlUtils;

/**
 * @author goose | jim
 * @author karussell
 *         <p>
 *         this class will be responsible for taking our top node and stripping out junk
 *         we don't want and getting it ready for how we want it presented to the user
 */
public class OutputFormatter {

    public static final int MIN_PARAGRAPH_TEXT = 50;
    private static final List<String> NODES_TO_REPLACE = Arrays.asList("strong", "b", "i");
    private Pattern unlikelyPattern = Pattern.compile("display:none|visibility:hidden");
    protected final int minParagraphText;
    protected final List<String> nodesToReplace;
    protected String nodesToKeepCssSelector = "p";
    private static final Pattern PASS_PATTERN = Pattern.compile("h\\d|p");
    private static final Pattern NEW_LINE = Pattern.compile("div|h\\d|table");
    private static final Pattern BOLD = Pattern.compile("b|em|strong");
    private static final Pattern HEADING = Pattern.compile("h\\d");

    public OutputFormatter() {
        this(MIN_PARAGRAPH_TEXT, NODES_TO_REPLACE);
    }

    public OutputFormatter(int minParagraphText) {
        this(minParagraphText, NODES_TO_REPLACE);
    }

    public OutputFormatter(int minParagraphText, List<String> nodesToReplace) {
        this.minParagraphText = minParagraphText;
        this.nodesToReplace = nodesToReplace;
    }

    /**
     * set elements to keep in output text
     */
    public void setNodesToKeepCssSelector(String nodesToKeepCssSelector) {
        this.nodesToKeepCssSelector = nodesToKeepCssSelector;
    }

    /**
     * takes an element and turns the P tags into \n\n
     */
    public String getFormattedText(Element topNode, String url) {
        removeNodesWithNegativeScores(topNode);

        StringBuilder sb = new StringBuilder();
        decode(topNode, sb, URI.create(url));

        while (endsWith(sb, "<br>")) {
            sb.setLength(sb.length() - 4);
        }

        String str = SHelper.innerTrim(sb.toString());
        if (str.length() > MIN_PARAGRAPH_TEXT)
            return str;

        // no subelements
        if (str.isEmpty() || !topNode.text().isEmpty() && str.length() <= topNode.ownText().length())
            str = topNode.html();

        // if jsoup failed to parse the whole html now parse this smaller 
        // snippet again to avoid html tags disturbing our text:
        return Jsoup.parse(str).text();
    }

    /**
     * Takes an element and returns a list of texts extracted from the P tags
     */
    public List<String> getTextList(Element topNode) {
        List<String> texts = new ArrayList<>();
        for (Element element : topNode.select(this.nodesToKeepCssSelector)) {
            if (element.hasText()) {
                texts.add(element.text());
            }
        }
        return texts;
    }

    /**
     * If there are elements inside our top node that have a negative gravity
     * score remove them
     */
    protected void removeNodesWithNegativeScores(Element topNode) {
        Elements gravityItems = topNode.select("*[gravityScore]");
        for (Element item : gravityItems) {
            int score = Integer.parseInt(item.attr("gravityScore"));
            if (score < 0 && !PASS_PATTERN.matcher(item.tagName()).find() && item.select("img").size() == 0)
                item.remove();
        }
    }

    protected void decode(Element node, StringBuilder sb, URI url) {
        for (Node child : node.childNodes()) {
            if (unlikely(child))
                continue;
            if (child instanceof TextNode) {
                TextNode textNode = (TextNode) child;
                String txt = textNode.text();
                sb.append(HtmlUtils.sanitize(txt));
            } else if (child instanceof Element) {
                Element element = (Element) child;
                if ("br".equals(element.tagName())) {
                    sb.append("<br>");
                    continue;
                } else if ("img".equals(element.tagName())) {
                    String imgUrl = child.absUrl("src");
                    sb.append("<img src=\"").append(imgUrl).append("\"><br>");
                    continue;
                } else if ("div".equals(element.tagName())) {
                    if (element.text().length() < MIN_PARAGRAPH_TEXT && element.select("img").size() == 0) {
                        continue;
                    }
                }

                boolean heading = HEADING.matcher(element.tagName()).find();
                boolean bold = BOLD.matcher(element.tagName()).find();

                if (heading) {
                    sb.append("<").append(element.tagName()).append(">");
                } else if (bold) {
                    sb.append("<b>");
                }

                decode((Element) child, sb, url);

                if (heading) {
                    sb.append("</").append(element.tagName()).append(">");
                } else if (bold) {
                    sb.append("</b>");
                }

                if (NEW_LINE.matcher(element.tagName()).find() && !endsWith(sb, "<br>")) {
                    sb.append("<br>");
                }
            }
        }

        if ("p".equals(node.tagName())) {
            if (endsWith(sb, "<br>")) {
                sb.append("<br>");
            } else {
                sb.append("<br><br>");
            }
        }
    }

    public static boolean endsWith(StringBuilder sb, String text) {
        if (sb.length() < text.length())
            return false;

        int sbLength = sb.length();
        int textLength = text.length();
        for (int i = 1; i <= textLength; i++) {
            if (text.charAt(textLength - i) != sb.charAt(sbLength - i))
                return false;
        }
        return true;
    }

    boolean unlikely(Node e) {
        if (e.attr("class") != null && e.attr("class").toLowerCase().contains("caption"))
            return true;

        String style = e.attr("style");
        String clazz = e.attr("class");
        if (unlikelyPattern.matcher(style).find() || unlikelyPattern.matcher(clazz).find())
            return true;
        return false;
    }

    void appendTextSkipHidden(Element e, StringBuilder accum) {
        for (Node child : e.childNodes()) {
            if (unlikely(child))
                continue;
            if (child instanceof TextNode) {
                TextNode textNode = (TextNode) child;
                String txt = textNode.text();
                accum.append(txt);
            } else if (child instanceof Element) {
                Element element = (Element) child;
                if (accum.length() > 0 && element.isBlock() && !lastCharIsWhitespace(accum))
                    accum.append(" ");
                else if (element.tagName().equals("br"))
                    accum.append("\n");
                appendTextSkipHidden(element, accum);
            }
        }
    }

    boolean lastCharIsWhitespace(StringBuilder accum) {
        if (accum.length() == 0)
            return false;
        return Character.isWhitespace(accum.charAt(accum.length() - 1));
    }

    protected String node2TextOld(Element el) {
        return el.text();
    }

    protected String node2Text(Element el) {
        StringBuilder sb = new StringBuilder(200);
        appendTextSkipHidden(el, sb);
        return sb.toString();
    }

    public OutputFormatter setUnlikelyPattern(String unlikelyPattern) {
        this.unlikelyPattern = Pattern.compile(unlikelyPattern);
        return this;
    }

    public OutputFormatter appendUnlikelyPattern(String str) {
        return setUnlikelyPattern(unlikelyPattern.toString() + "|" + str);
    }
}
