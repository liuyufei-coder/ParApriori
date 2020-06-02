package CoreCode;

import DataStructure.Mymap;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

class Scan3Task extends RecursiveAction {
    private static final int MAX = 200;

    private int start;
    private int end;

    public Scan3Task(int start, int end){
        this.start = start;
        this.end = end;
    }

    //判断新添加元素形成的候选集是否在新的频繁项集中
    private static boolean isNotHave(HashSet<String> hsSet, List<List<String>> nextFrequentItemSets) {
        List<String> tempList = new ArrayList<>();
        Iterator<String> itr = hsSet.iterator();
        while (itr.hasNext()) {
            String Item = (String) itr.next();
            tempList.add(Item);
        }
        for (int i = 0; i < nextFrequentItemSets.size(); i++) {
            if (tempList.equals(nextFrequentItemSets.get(i))) {
                return false;
            }
        }
        return true;
    }

    //求出两个项集所对应的事务的交集
    private static List<List<String>> getCommonItemSets(List<String> list1, List<String> list2, Map<List<String>, List<List<String>>> map) {
        List<List<String>> re_ItemSets1 = (List<List<String>>) map.get(list1);
        List<List<String>> re_ItemSets2 = (List<List<String>>) map.get(list2);
        re_ItemSets1.retainAll(re_ItemSets2);
        return re_ItemSets1;
    }

    @Override
    protected void compute() {
        if ((end - start) <= MAX) {
            for (int i = start; i < end; i++) {
                HashSet<String> hsSet = new HashSet<>();
                HashSet<String> hsSetCopy = new HashSet<>();
                List<String> Item_i = LastStep.ItemSetCopy.get(i);
                for (int k = 0; k < Item_i.size(); k++) {
                    hsSet.add(Item_i.get(k));
                }
                int len_brfore = hsSet.size();
                hsSetCopy = (HashSet<String>) hsSet.clone();
                for (int j = i + 1; j < LastStep.ItemSetCopy.size(); j++) {
                    List<String> TempList = new ArrayList<>();//存储两个项集连接后的List形式
                    hsSet = (HashSet<String>) hsSetCopy.clone();
                    List<String> Item_j = LastStep.ItemSetCopy.get(j);
                    for (int k = 0; k < Item_j.size(); k++) {
                        hsSet.add(Item_j.get(k));
                    }
                    int len_after = hsSet.size();
                    //判断是否可以连接
                    if (len_after == len_brfore + 1 && isNotHave(hsSet, parallelApriori.ItemSets)) {
                        //当可以连接并且与频繁项集中元素不重复时
                        int count = 0;
                        List<List<String>> CommonItemSets = getCommonItemSets(Item_i, Item_j, LastStep.oldMapCopy);
                        for (int l = 0; l < CommonItemSets.size(); l++) {
                            List<String> ItemSets = CommonItemSets.get(l);
                            int support = (Integer) parallelApriori.precord.get(ItemSets);
                            count += support;
                        }
                        if (count >= (parallelApriori.record.size() * parallelApriori.MIN_SUPPORT)) {
                            Iterator<String> itr = hsSet.iterator();
                            while (itr.hasNext()) {
                                String Item = (String) itr.next();
                                TempList.add(Item);
                            }
                            Mymap mymap = new Mymap(TempList, count);
                            parallelApriori.frequentMap.add(mymap);
                            parallelApriori.oldMap.put(TempList, CommonItemSets);
                            parallelApriori.ItemSets.add(TempList);
                        }
                    }
                }
            }
        }else {
            //任务分解
            int middle = (start + end)/2;
            Scan3Task left = new Scan3Task(start, middle);
            Scan3Task right = new Scan3Task(middle, end);
            //并行执行两个小任务
            invokeAll(left, right);
        }
    }
}

public class LastStep {
    static List<List<String>> ItemSetCopy = new CopyOnWriteArrayList<>();
    static Map<List<String>, List<List<String>>> oldMapCopy = new ConcurrentHashMap<>();

    //得出所有的频繁项集及支持度
    public static void getAllFrequent() {
        //创建线程池
        ForkJoinPool forkJoinPool = new ForkJoinPool(4);
        while (!parallelApriori.ItemSets.isEmpty()) {
            ItemSetCopy.clear();
            oldMapCopy.clear();
            ItemSetCopy.addAll(parallelApriori.ItemSets);
            oldMapCopy.putAll(parallelApriori.oldMap);
            parallelApriori.ItemSets.clear();
            parallelApriori.oldMap.clear();
            Scan3Task task = new Scan3Task(0, ItemSetCopy.size());
            forkJoinPool.invoke(task);
        }
        forkJoinPool.shutdown();
    }


}
