package IO;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

//读取数据
public class DataRead {
    public static List<List<String>> getRecord(String url) {
        List<List<String>> record = new CopyOnWriteArrayList<List<String>>();
        try {
            String encoding = "UTF-8";//字符编码（解决中文乱码）
            File file = new File(url);
            if (file.isFile() && file.exists()) {
                InputStreamReader read = new InputStreamReader(new FileInputStream(file), encoding);
                BufferedReader br = new BufferedReader(read);
                String line = null;
                while ((line = br.readLine()) != null) {
                    String[] lineString = line.split("\t");
                    List<String> lineList = new ArrayList<String>();
                    for (int i = 0; i < lineString.length; i++) {
                        lineList.add(lineString[i]);
                    }
                    record.add(lineList);
                }
                read.close();
            }else {
                System.out.println("找不到指定文件！");
            }
        } catch (Exception e) {
            System.out.println("读取文件内容操作出错！");
            e.printStackTrace();
        }
        return record;
    }
}
