package com.thermatk.java.tgemoji;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

import static com.thermatk.java.tgemoji.EmojiData.fixEmoji;

class Rect{
    public int left, top, right, bottom;
    Rect(int left, int top, int right, int bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }
}
class DrawableInfo {
    public Rect rect;
    public byte page;
    public byte page2;
    public int emojiIndex;
    public int row;
    public int col;

    public DrawableInfo(Rect r, byte p, byte p2, int index, int rw, int cl) {
        rect = r;
        page = p;
        page2 = p2;
        emojiIndex = index;

        // additional info
        // they are actually wrong in the Telegram code, so put them in reverse
        row = cl;
        col = rw;
    }
}

class PicInfo {
    public HashMap<String, DrawableInfo> drInfMap;
    public int totalRows;
    public int totalCols;
}
public class Main {
    public static final String basePath = "/home/thermatk/emojiGames/";
    public static HashMap<String, DrawableInfo> rects = new HashMap<>();

    public static HashMap<String, PicInfo> pics = new HashMap<>();
    public static HashMap<String, String> nameList = new HashMap<>();

    public static void main(String[] args) {
        readFixedNames();
        doTheMap();
        MapToPicMap();
        makeImgs();
        System.out.print("Done");
    }

    public static void makeImgs() {

        for (Map.Entry<String, PicInfo> entry: pics.entrySet()) {
            PicInfo pInfo = entry.getValue();
            try {

                int w = (pInfo.totalCols + 1) * 66;
                int h = (pInfo.totalRows + 1) * 66;
                BufferedImage combined = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

                Graphics g = combined.getGraphics();

                for (Map.Entry<String, DrawableInfo> drEntry: pInfo.drInfMap.entrySet()) {
                    String path = basePath + "twemoji-2.2.1/2/72x72/" + drEntry.getKey() + ".png";
                    File f = new File(path);
                    boolean exists = false;
                    if(f.exists()) {
                        exists = true;
                    } else {
                        // try a quick fe0f fix
                        path = path.replace("-fe0f-", "-");
                        path = path.replace("-fe0f", "");
                        f = new File(path);
                        if(f.exists()) {
                            exists = true;
                        }
                    }

                    if (exists) {
                        BufferedImage image72 = ImageIO.read(f);
                        BufferedImage image64 = resize(image72, 64,64);
                        g.drawImage(image64,drEntry.getValue().rect.left, drEntry.getValue().rect.top, null);
                    } else {
                        System.out.println("ERROR MISSING: " + drEntry.getKey());
                    }
                }

                ImageIO.write(combined, "PNG", new File(basePath+"imgs/"+entry.getKey()+".png"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void readFixedNames() {

        BufferedReader br = null;
        String line = "";
        try {
            br = new BufferedReader(new FileReader(basePath+"convList.txt"));
            while ((line = br.readLine()) != null) {
                String[] emojis = line.split(",");
                for (String emoji: emojis) {
                    String[] oneEm = emoji.split(":");
                    nameList.put(oneEm[0],oneEm[1]);
                }
            }
        } catch (IOException e) {

        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    // code initally from https://github.com/DrKLO/Telegram/blob/master/TMessagesProj/src/main/java/org/telegram/messenger/Emoji.java
    public static void doTheMap() {
        final int splitCount = 4;

        final int[][] cols = {
                {12, 12, 12, 12},
                {6, 6, 6, 6},
                {9, 9, 9, 9},
                {9, 9, 9, 9},
                {8, 8, 8, 7}
        };
        int emojiFullSize;
        int add = 2;

        emojiFullSize = 64;

        for (int j = 0; j < EmojiData.data.length; j++) {
            int count2 = (int) Math.ceil(EmojiData.data[j].length / (float) splitCount);
            int position;
            for (int i = 0; i < EmojiData.data[j].length; i++) {
                int page = i / count2;
                position = i - page * count2;
                int row = position % cols[j][page];
                int col = position / cols[j][page];
                Rect rect = new Rect(row * emojiFullSize + row * add, col * emojiFullSize + col * add, (row + 1) * emojiFullSize + row * add, (col + 1) * emojiFullSize + col * add);
                String name = nameList.get(fixEmoji(EmojiData.data[j][i]));
                if (name == null) {
                    // another fe0f fix
                    String fix = fixEmoji(EmojiData.data[j][i])+"\uFE0F";
                    name = nameList.get(fix);
                }
                rects.put(name, new DrawableInfo(rect, (byte) j, (byte) page, i, row, col)); // + row and col
            }
        }

    }

    public static void MapToPicMap() {
        for (Map.Entry<String, DrawableInfo> entry: rects.entrySet()) {

            DrawableInfo drInfo = entry.getValue();
            String emojiKey = entry.getKey();

            String filepage = drInfo.page + "_" + drInfo.page2;
            PicInfo picInfo = pics.get(filepage);
            if (picInfo != null) {
                // if
                picInfo.drInfMap.put(emojiKey,drInfo);
                if (drInfo.row > picInfo.totalRows) {
                    picInfo.totalRows = drInfo.row;
                }
                if (drInfo.col > picInfo.totalCols) {
                    picInfo.totalCols = drInfo.col;
                }
            } else {
                // No such key
                PicInfo newPic = new PicInfo();
                newPic.drInfMap = new HashMap<>();
                newPic.drInfMap.put(emojiKey,drInfo);
                newPic.totalRows = drInfo.row;
                newPic.totalCols = drInfo.col;
                pics.put(filepage,newPic);
            }
        }
    }
    // from http://stackoverflow.com/a/9417836
    public static BufferedImage resize(BufferedImage img, int newW, int newH) {
        Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = dimg.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();

        return dimg;
    }
}
