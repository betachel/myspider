package com.betachel.pdf;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import com.betachel.model.LotteryResultModel;
import com.betachel.mybatis.service.LotteryResultService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.jsoup.helper.StringUtil;

/**
 * @author shiqiu
 * @date 2021/04/18
 */
public class LotteryResultPdfReadUtils {
    private static final String LOCAL_FILE_PATH = "D:\\documents\\yaohao\\";
    private static final String PAGE_DATA_SEPARATOR = "\u0001";
    private static final String FILE_SUFFIX = ".pdf";
    private static final String FILTER_TEXT = "序号";
    private static final String YAOHAO_RESULT = "摇号结果";

    public static List<LotteryResultModel> readPdf(File file) {
        if ( Objects.isNull(file) || !file.exists() ) {
            return null;
        }
        try {
            long startTime = System.currentTimeMillis();

            PDDocument pdDocument = PDDocument.load(file);
            PDFTextStripper textStripper = new PDFTextStripper();

            String fileName = file.getName();
            System.out.println(fileName + " , 总页数：" + pdDocument.getNumberOfPages());

            int page = 1, totalPage = pdDocument.getNumberOfPages();

            List<LotteryResultModel> models = new LinkedList<>();

            while ( page <= totalPage ) {
                textStripper.setStartPage(page);
                textStripper.setEndPage(page);

                String content = textStripper.getText(pdDocument);

                buildResult(content, fileName.replace(FILE_SUFFIX, ""), models);

                page++;
            }
            pdDocument.close();

            System.out.println("read pdf cost(ms) : " + ( System.currentTimeMillis() - startTime ));

            return models;

        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<LotteryResultModel> readPdf(String filePath) {
        if ( StringUtil.isBlank(filePath) ) {
            return null;
        }
        List<LotteryResultModel> models = readPdf(new File(filePath));

        return models;
    }

    private static List<LotteryResultModel> buildResult(String content, String title, List<LotteryResultModel> models) {
        if ( StringUtil.isBlank(content) ) {
            return null;
        }
        String[] datas = content.split("\r\n");
        if ( Objects.isNull(datas) || datas.length == 0 ) {
            return null;
        }

        for ( int index = 0; index < datas.length; index++ ) {
            String data = datas[index];
            if ( StringUtil.isBlank(data) ) {
                continue;
            }
            String[] strings = data.split(" ");
            if ( Objects.isNull(strings) || strings.length < 2 ) {
                continue;
            }
            String orderStr = strings[0];
            String registerStr = strings[1];
            if ( StringUtil.isBlank(orderStr) || StringUtil.isBlank(registerStr) || FILTER_TEXT.equalsIgnoreCase(orderStr) ) {
                continue;
            }
            int i = 0;
            for ( ; i < registerStr.length(); i++ ) {
                char ch = registerStr.charAt(i);
                if ( Character.isLetter(ch) ) {
                    continue;
                }
                break;
            }
            String registerOrderStr = registerStr.substring(i);
            String buildingName = registerStr.substring(0, i);
            Integer lotteryOrder = Integer.parseInt(orderStr);
            Integer registerOrder = Integer.parseInt(registerOrderStr);
            LotteryResultModel lotteryResultModel = new LotteryResultModel();
            lotteryResultModel.setLotteryOrder(lotteryOrder);
            lotteryResultModel.setLotteryTitle(title);
            lotteryResultModel.setBuildingName(buildingName);
            lotteryResultModel.setRegisterOrder(registerOrder);
            lotteryResultModel.setRegisterText(registerStr);

            models.add(lotteryResultModel);

        }

        return models;
    }

    public static void main(String[] args) {
       /* String filePath = LOCAL_FILE_PATH + "“潮悦前城5幢、11幢、22幢”公开销售公证摇号结果" + FILE_SUFFIX;
        List<LotteryResultModel> models = readPdf(filePath);

        System.out.println("pdf file models size : " + models.size());

        LotteryResultService lotteryResultService = new LotteryResultService();
        int saveCount = lotteryResultService.save(models);

        System.out.println("saveCount : " + saveCount);*/

        File file = new File(LOCAL_FILE_PATH);

        if ( !file.exists() || !file.isDirectory() ) {
            return;
        }
        int total = 0, saveTotal = 0;
        for ( File file1 : file.listFiles() ) {
            if ( !file1.getName().endsWith(YAOHAO_RESULT + FILE_SUFFIX) ) {
                continue;
            }
            List<LotteryResultModel> models1 = readPdf(file1);
            total += models1.size();
            LotteryResultService lotteryResultService1 = new LotteryResultService();
            saveTotal += lotteryResultService1.save(models1);

        }

        System.out.println("read total : " + total);
        System.out.println("save total : " + saveTotal);

    }

}
