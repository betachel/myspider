package com.betachel.spider;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.pipeline.ConsolePipeline;
import us.codecraft.webmagic.processor.PageProcessor;

/**
 * @author shiqiu
 * @date 2021/04/11
 */
public class HZYaohaoSpider implements PageProcessor {

    private Site site = Site.me()
        .setRetryTimes(1)
        .setSleepTime(100)
        .setUserAgent(
            "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.182 "
                + "Safari/537.36")
        .addCookie("Hm_lvt_dc99a9c71f9765d5a29ad86b83069d23", "1618131489");

    private static final String HSOT = "https://www.hz-notary.com";
    private static final String PAGE_TYPE = "pageType";
    private static final String PAGE_TYPE_DETAIL = "detail";
    private static final String PAGE_TYPE_CONTENT = "content";
    private static final String DETAIL_HREF = "/lottery/detail?";
    private static final String DETAIL_CONTENT_HREF = "/lottery/detail_content?";
    private static final String YAOHAO_RESULT = "摇号结果";
    private static final String DENGJI_HUIZONG = "登记汇总表";
    private static final String LOCAL_FILE_PATH = "D:\\documents\\yaohao\\";
    private static final String DEFAULT_FILE_NAME = "defaultYaohao.pdf";
    private static final String PAGE_NUM = "page.pageNum";
    private static final String INDEX_PATH = "/lottery/index";
    private static final int MAX_PAGE = 5;

    @Override
    public void process(Page page) {

        String url = page.getUrl().get();

        boolean isIndex = url.contains(INDEX_PATH);
        String pageType = page.getRequest().getExtra(PAGE_TYPE);

        if ( isIndex ) {
            processIndexPage(page);
        } else if ( PAGE_TYPE_DETAIL.equalsIgnoreCase(pageType) ) {
            processDetailPage(page);
        } else if ( PAGE_TYPE_CONTENT.equalsIgnoreCase(pageType) ) {
            processDetailContentPage(page);
        }

    }

    private void processDetailPage(Page page) {
        Elements elements = page.getHtml().getDocument().getElementsByClass("data-list");
        Element element = null;
        if ( CollectionUtils.isEmpty(elements) || Objects.isNull(element = elements.first()) ) {
            return;
        }
        Elements aElements = element.getElementsByTag("a");
        if ( Objects.isNull(aElements) || aElements.isEmpty() ) {
            return;
        }

        Set<String> filterText = new HashSet<>(4);
        filterText.add(YAOHAO_RESULT);
        filterText.add(DENGJI_HUIZONG);
        for ( Element aElement : aElements ) {
            parseElement(page, aElement, filterText);
        }
    }

    private void processDetailContentPage(Page page) {
        String url = page.getUrl().get();
        System.out.println("pageDetailContentUrl : " + url);

        Elements elements = page.getHtml().getDocument().getElementsByClass("detail_content");
        Element element = null;
        if ( CollectionUtils.isEmpty(elements) || Objects.isNull(element = elements.first()) ) {
            return;
        }
        Elements spanElements = element.getElementsByTag("span");
        Element spanElement = CollectionUtils.isEmpty(spanElements) ? null : spanElements.get(0);

        Elements aElements = element.getElementsByTag("a");
        Element aElement = CollectionUtils.isEmpty(aElements) ? null : aElements.get(0);

        String name = Objects.nonNull(spanElement) ? spanElement.text() : "";
        String href = Objects.nonNull(aElement) ? aElement.attr("href") : "";

        downloadFile(href, name.trim() + ".pdf");

    }

    private void processIndexPage(Page page) {
        Elements elements = page.getHtml().getDocument().getElementsByClass("data-list");
        Element element = null;
        if ( CollectionUtils.isEmpty(elements) || Objects.isNull(element = elements.first()) ) {
            return;
        }
        Elements aElements = element.getElementsByTag("a");
        if ( CollectionUtils.isEmpty(aElements) ) {
            return;
        }
        for ( Element aElement : aElements ) {
            parseElement(page, aElement, null);
        }
        String url = page.getUrl().get();
        String temp = url.replace(HSOT + INDEX_PATH + "?" + PAGE_NUM + "=", "");
        if ( StringUtil.isBlank(temp) || !"1".equals(temp.trim()) ) {
            return;
        }
        element = page.getHtml().getDocument().getElementById("pageSelect");

        elements = Objects.nonNull(element) ? element.getElementsByTag("option") : null;
        if ( CollectionUtils.isEmpty(elements) ) {
            return;
        }
        element = elements.last();
        if ( Objects.isNull(element) ) {
            return;
        }
        String maxPageNumStr = element.attr("value");
        if ( StringUtil.isBlank(maxPageNumStr) ) {
            return;
        }
        try {
            Integer maxPageNum = Integer.valueOf(maxPageNumStr);

            maxPageNum = Objects.nonNull(maxPageNum) && maxPageNum <= MAX_PAGE ? maxPageNum : MAX_PAGE;

            int pageNum = 2;
            while ( pageNum <= maxPageNum ) {
                String url2 = HSOT + INDEX_PATH + "?" + PAGE_NUM + "=" + pageNum;
                pageNum++;
                page.addTargetRequest(url2);
            }

        } catch (Exception e) {
            System.err.println("processIndexPage error : " + e.getMessage());
        }
    }

    private void parseElement(Page page, Element aElement, Set<String> filterText) {
        if ( Objects.isNull(aElement) ) {
            return;
        }
        String name = Optional.ofNullable(aElement.text()).orElseGet(() -> "");

        boolean isNeedProcess = CollectionUtils.isEmpty(filterText) || filterText.stream().anyMatch(
            it -> name.contains(it));

        if ( !isNeedProcess ) {
            return;
        }
        String href = aElement.attr("href");

        if ( StringUtil.isBlank(href) ) {
            return;
        }
        String pageType = "";
        if ( href.contains(DETAIL_HREF) ) {
            pageType = PAGE_TYPE_DETAIL;
        } else if ( href.contains(DETAIL_CONTENT_HREF) ) {
            pageType = PAGE_TYPE_CONTENT;
        }
        Request request = new Request(HSOT + href);
        request.putExtra(PAGE_TYPE, pageType);
        request.setMethod("GET");
        page.addTargetRequest(request);

    }

    private void downloadFile(String link, String fileName) {
        if ( StringUtil.isBlank(link) ) {
            return;
        }
        fileName = StringUtil.isBlank(fileName) ? DEFAULT_FILE_NAME : fileName.trim();
        InputStream inputStream = null;
        ByteArrayOutputStream outputStream = null;
        FileOutputStream fileOutputStream = null;

        try {
            URL url = new URL(link);
            URLConnection con = url.openConnection();
            inputStream = con.getInputStream();
            outputStream = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int len = 0;
            while ( ( len = inputStream.read(buf) ) != -1 ) {
                outputStream.write(buf, 0, len);
            }
            File file = new File(LOCAL_FILE_PATH + fileName);
            fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(outputStream.toByteArray());
        } catch (Exception e) {
            System.out.println("downloadFile error : " + e.toString());
        } finally {
            try {
                if ( Objects.nonNull(inputStream) ) {
                    inputStream.close();
                }
                if ( Objects.nonNull(outputStream) ) {
                    outputStream.close();
                }
                if ( Objects.nonNull(fileOutputStream) ) {
                    fileOutputStream.close();
                }
            } catch (Exception e) {}
        }
    }

    @Override
    public Site getSite() {
        return site;
    }

    public static void main(String[] args) {
        Spider.create(new HZYaohaoSpider())
            .addUrl(HSOT + INDEX_PATH + "?" + PAGE_NUM + "=1")
            .thread(1)
            .addPipeline(new ConsolePipeline())
            .run();
    }
}
