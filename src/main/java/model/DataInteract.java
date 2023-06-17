package model;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFPicture;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DataInteract {
    public static void main(String[] args) {
        try {
            List<Question> questions = getQuestionsFromTxtFile("C:/Users/ACER/Desktop/New Text Document (3).txt");
//            //Category cat = new Category("1", "testQuiz");
            DBInteract dbi = new DBInteract();
//            dbi.createNewCategory(null, "1","testCat");
//            dbi.createNewCategory("testCat","2","testSubCat");
//
//            assert questions != null;
//            for (Question q : questions) {
//                dbi.insertQuestion(q,"testCat");
//            }
//
//            questions = dbi.getQuestionsBelongToCategory("testCat");
////
////
//            Quiz quiz = new Quiz();
//            quiz.setQuizName("CNXH_GK_1");
//            quiz.setQuizDescription("Đề thi giữa kì Chủ nghĩa xã hội khoa học - Đề 1");
//            quiz.setTimeLimit(60);
//            dbi.createNewQuiz(quiz);
//            dbi.addQuestionToQuiz("testQuiz","1");
//            dbi.addQuestionToQuiz("testQuiz","2");
//            questions = dbi.getQuestionBelongToQuiz("testQuiz");
//            for (Question q:questions) {
//                q.showQ();
//            }
//            dbi.createNewCategory("testCat","3","anotherSubCat");
//            dbi.createNewCategory(null,"4","anotherCat");
//            List<Category> categories = dbi.getAllNonSubCategories();
//            for (Category c: categories) {
//                for (Question q:c.getQuestions()) {
//                    q.showQ();
//                }
//            }


            Quiz quiz = new Quiz();
            quiz.setQuizName("CNXH_GK_1");
            quiz.setQuizDescription("Đề thi giữa kì Chủ nghĩa xã hội khoa học - Đề 1");
            quiz.setTimeLimit(60);
            dbi.createNewCategory(null,"1","CNXH");
            dbi.createNewCategory("CNXH","2","CNXH_Dễ");
            dbi.createNewCategory("CNXH","3","CNXH_Khó");

            for (int i=0;i<10;i++) {
                dbi.insertQuestion(questions.get(i),"CNXH_Dễ");
            }
            for (int i=10;i<15;i++) {
                dbi.insertQuestion(questions.get(i),"CNXH_Khó");
            }
            dbi.createNewQuiz(quiz);
            List<Integer> a = new ArrayList<>();
            for (int i=1;i<16;i++) a.add(i);
            Random random = new Random();
            for (int i=0;i<10;i++) {
                int x = random.nextInt(a.size());
                dbi.addQuestionToQuiz("CNXH_GK_1",String.valueOf(a.get(x)));
                a.remove(x);
            }

            quiz.setQuizName("CNXH_GK_2");
            quiz.setQuizDescription("Đề thi giữa kì Chủ nghĩa xã hội khoa học - Đề 2");
            quiz.setTimeLimit(60);
            dbi.createNewQuiz(quiz);
            a = new ArrayList<>();
            for (int i=1;i<16;i++) a.add(i);
            for (int i=0;i<10;i++) {
                int x = random.nextInt(a.size());
                dbi.addQuestionToQuiz("CNXH_GK_2",String.valueOf(a.get(x)));
                a.remove(x);
            }


        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    public static List<Question> getQuestionsFromDocFile(String path) throws Exception{
        try (FileInputStream fis = new FileInputStream(path);
             XWPFDocument doc = new XWPFDocument(fis);) {

            List<String> lines = new ArrayList<>();
            List<Image> images = new ArrayList<>();

            for (XWPFParagraph paragraph : doc.getParagraphs()) {
                List<XWPFRun> runs = paragraph.getRuns();
                String line = "";
                Image image = null;
                for (XWPFRun run : runs) {
                    if (run.getText(0) != null) line = line + run.getText(0);
                    List<XWPFPicture> pictures = run.getEmbeddedPictures();
                    if (pictures.size() > 0) {
                        byte[] pictureByte = pictures.get(0).getPictureData().getData();
                        image = new Image(new ByteArrayInputStream(pictureByte));
                    }
                }
                lines.add(line);
                images.add(image);
            }
            doc.close();

            checkAikenFormat(lines);
            List<Question> questions = new ArrayList<>();
            int n = lines.size();
            int i = 0;

            do {
                while (i < n && (lines.get(i).equals("") || lines.get(i).equals("null"))) i++;
                if (i == n) break;
                Question q = new Question();

                int index = lines.get(i).indexOf(':');
                q.setQuestionName(lines.get(i).substring(0, index));
                q.setQuestionText(lines.get(i).substring(index + 2));

                if (images.get(i) != null) q.setQuestionImage(images.get(i));

                List<String> options = new ArrayList<String>();
                List<Image> optionImages = new ArrayList<>();
                while (++i < n && (lines.get(i) != null && !lines.get(i).equals(""))) {
                    options.add(lines.get(i));
                    optionImages.add(images.get(i));
                }
                q.setOptionImages(optionImages);
                List<Character> ans = new ArrayList<>();
                String s = options.get(options.size() - 1);
                for (int j = 8; j < s.length(); j++) {
                    if (s.charAt(j) != ' ' && s.charAt(j) != ',') ans.add(s.charAt(j));
                }
                options.remove(options.size() - 1);
                q.setOptions(options);
                q.setAns(ans);

                questions.add(q);

            } while (i < n);
            return questions;
        }
    }

    public static List<Question> getQuestionsFromTxtFile(String path) throws Exception{
        List<String> lines = new ArrayList<>();
        try (FileReader fr = new FileReader(path);
            BufferedReader br = new BufferedReader(fr);) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
            checkAikenFormat(lines);
            List<Question> questions = new ArrayList<>();
            int n = lines.size();
            int i = 0;

            do {
                while (i < n && (lines.get(i).equals("") || lines.get(i).equals("null"))) i++;
                if (i == n) break;
                Question q = new Question();

                int index = lines.get(i).indexOf(':');
                q.setQuestionName(lines.get(i).substring(0, index));
                q.setQuestionText(lines.get(i).substring(index + 2));
                q.setQuestionImage(null);

                List<String> options = new ArrayList<String>();
                List<Image> optionImages = new ArrayList<>();
                while (++i < n && (lines.get(i) != null && !lines.get(i).equals(""))) {
                    options.add(lines.get(i));
                    optionImages.add(null);
                }
                q.setOptionImages(optionImages);

                List<Character> ans = new ArrayList<>();
                String s = options.get(options.size() - 1);
                for (int j = 8; j < s.length(); j++) {
                    if (s.charAt(j) != ' ' && s.charAt(j) != ',') ans.add(s.charAt(j));
                }
                options.remove(options.size() - 1);
                q.setOptions(options);
                q.setAns(ans);

                questions.add(q);

            } while (i < n);
            return questions;
        }
    }

    public static byte[] changeImageToBytes(Image img) throws IOException {
        if (img == null) return null;
        BufferedImage bImage = SwingFXUtils.fromFXImage(img, null);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(bImage, "png", outputStream);
        return outputStream.toByteArray();
    }

    public static Image changeBytesToImage(byte[] bytes) {
        return new Image(new ByteArrayInputStream(bytes));
    }

    public static void checkAikenFormat(List<String> lines) throws ReadFileException{
        int n = lines.size();
        int i = 0;

        do {
            while (i < n && (lines.get(i).equals("") || lines.get(i).equals("null"))) i++;
            if (i == n) break;

            if (!lines.get(i).contains(": ") || lines.get(i).indexOf(": ") == 0) {
                throw new ReadFileException("Error at line " + (i+1) + ": Question does not have name");
                //return false;
            }

            List<Character> optionLabels = new ArrayList<>();
            while (++i < n && !lines.get(i).equals("null") && !lines.get(i).equals("")) {
                if (lines.get(i).indexOf(". ") != 1 && lines.get(i).indexOf(": ") != 6) {
                    throw new ReadFileException("Error at line " + (i+1) + ": Answer label error");
                    //return false;
                }
                optionLabels.add(lines.get(i).charAt(0));
            }
            if (optionLabels.size() <= 2) {
                throw new ReadFileException("Error at line " + i + ": Not enough option");
            }
            optionLabels.remove(optionLabels.size()-1);
            String s = lines.get(i-1);
            if (s.indexOf("ANSWER: ") != 0) {
                throw new ReadFileException("Error at line " + i + ": Question does not have answer");
            }
            for (int j = 8; j < s.length(); j++) {
                if (s.charAt(j) != ' ' && s.charAt(j) != ',') {
                    if (!optionLabels.contains(s.charAt(j))) {
                        throw new ReadFileException("Error at line " + i + ": Answer not in options");
                    }
                }
            }
        } while (i < n);
    }
}
