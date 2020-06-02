package IO;

import CoreCode.parallelApriori;
import DataStructure.Mymap;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class PrintResult {
    //输出所有的频繁项集及其支持度
    public void printFre() {
        System.out.println("所有频繁项集及支持度如下：");
        for (int i = 0; i < parallelApriori.frequentMap.size(); i++) {
            Mymap mymap = (Mymap) parallelApriori.frequentMap.get(i);
            List<String> freItemSet = mymap.Item;
            int support = mymap.count;

            parallelApriori.frequentItemset.add(freItemSet);//将频繁项集存入该List中，为求关联规则作准备

            System.out.println(freItemSet + " 支持度为：" + support);
        }
    }

    public Long gett(Long a, Long b) {
        return b - a;
    }

    //输出所有的关联规则及置信度
    public void printAsso() {
        System.out.println("所有关联规则如下：");
        Iterator<Map.Entry<String, Double>> entries = parallelApriori.AssoRules.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<String, Double> entry = entries.next();
            System.out.println(entry.getKey() + "  置信度为：" + entry.getValue());
        }
    }
}
