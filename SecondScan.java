package CoreCode;

import DataStructure.Mymap;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

class Scan2Task extends RecursiveAction {
    private static final int MAX = 300;

    private int start;
    private int end;
    private List<String> ItemSet;
    private SecondScan lock;

    public Scan2Task(int start, int end, List<String> ItemSet, SecondScan lock) {
        this.start = start;
        this.end = end;
        this.ItemSet = ItemSet;
        this.lock = lock;
    }

    //检测当前记录是否包含所给项集
    public boolean isContained(List<String> itemSet, List<String> onerecord) {
        boolean flag = true;
        String item;
        for (int i = 0; i < itemSet.size(); i++) {
            item = itemSet.get(i);
            if (!onerecord.contains(item)) {
                flag = false;
            }
        }
        return flag;
    }

    //并行扫描压缩事务集
    @Override
    protected void compute() {
        List<String> record_i;
        int index, support;
        if ((end - start) <= MAX) {
            for (int i = start; i < end; i++) {
                record_i = SecondScan.precordKeyList.get(i);
                if (isContained(ItemSet, record_i)) {
                    support = parallelApriori.precord.get(record_i);
                    synchronized (lock) {
                        SecondScan.count += support;
                    }
                    SecondScan.Re_ItemSets.add(record_i);
                }
            }
        }else {
            //任务分解
            int middle = (start + end)/2;
            Scan2Task left = new Scan2Task(start, middle, ItemSet, lock);
            Scan2Task right = new Scan2Task(middle, end, ItemSet, lock);
            //并行执行两个小任务
            invokeAll(left, right);
        }
    }
}

public class SecondScan {
    //获取precord的键值List
    static Set<List<String>> precordKeySet = parallelApriori.precord.keySet();
    static List<List<String>> precordKeyList = Collections.synchronizedList(new ArrayList<List<String>>(precordKeySet));
    //记录每个候选集对应的支持度计数
    static volatile int count;
    //记录包含候选集的所有事物
    static List<List<String>> Re_ItemSets = new CopyOnWriteArrayList<>();

    //遍历firstCandidate，获取支持度大于给定值的一项集
    public void getFrequent1Item(Map<String, Integer> map) {
        Iterator entrys = map.entrySet().iterator();
        while (entrys.hasNext()) {
            Map.Entry entry = (Map.Entry) entrys.next();
            String key = (String)entry.getKey();
            Integer value = (Integer)entry.getValue();
            List<String> frequent1Item = new ArrayList<String>(1);
            if (value >= (parallelApriori.record.size() * parallelApriori.MIN_SUPPORT)) {
                frequent1Item.add(key);
                Mymap mymap = new Mymap(frequent1Item, value);
                parallelApriori.frequentMap.add(mymap);
            }
        }
    }

    //剪枝并获得候选二项集
    public void getSecondCandidate() {
        List<String> key = new ArrayList<>();
        int len = parallelApriori.frequentMap.size();
        String item1, item2;
        for (int i = 0; i < len - 1; i++) {
            item1 = parallelApriori.frequentMap.get(i).Item.get(0);
            for (int j = i + 1; j < len; j++) {
                List<String> item2Sets = new ArrayList<>(2);
                item2Sets.add(item1);
                item2 = parallelApriori.frequentMap.get(j).Item.get(0);
                item2Sets.add(item2);
                parallelApriori.ItemSets.add(item2Sets);
            }
        }
    }


    //获取频繁二项集及其对应的支持度与所有事务
    public void getFrequent2ItemSets() {
            ForkJoinPool forkJoinPool = new ForkJoinPool(4);
            SecondScan lock = new SecondScan();
            for (int i = 0; i < parallelApriori.ItemSets.size(); i++) {
                count = 0;
                Re_ItemSets.clear();
                List<String> ItemSet = parallelApriori.ItemSets.get(i);
                Scan2Task task = new Scan2Task(0, parallelApriori.precord.size(), ItemSet, lock);
                forkJoinPool.invoke(task);
                if (count >= parallelApriori.record.size() * parallelApriori.MIN_SUPPORT) {
                    Mymap mymap = new Mymap(ItemSet, count);
                    parallelApriori.frequentMap.add(mymap);
                    List<List<String>> Re_ItemSetsCopy = new ArrayList<>();
                    Re_ItemSetsCopy.addAll(Re_ItemSets);
                    parallelApriori.oldMap.put(ItemSet, Re_ItemSetsCopy);
                }else {
                    parallelApriori.ItemSets.remove(i);
                    i--;
                }
            }
            forkJoinPool.shutdown();
    }
}
