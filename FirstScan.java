package CoreCode;


import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.*;
import java.lang.reflect.Method;


class Scan1Task extends RecursiveAction {
    private static final int MAX = 400;

    private int start;
    private int end;

    public Scan1Task(int start, int end) {
        this.start = start;
        this.end = end;
    }

    //获取二项集插入在Hashmap中的数组索引（在获取第一个待插入List时会报异常，但结果无误，如果出现问题，可将第一个List单独开来）
    public static int gethashcode(int hashcode) {
        Class Chp = FirstScan.Item2Sets.getClass();
        Method method = null;
        Field field = null;
        int sphash = 0, length = 16;
        if (!FirstScan.Item2Sets.isEmpty()) {
            try {
                method = Chp.getDeclaredMethod("spread", int.class);
                method.setAccessible(true);
                Object obj = method.invoke(FirstScan.Item2Sets, hashcode);
                sphash = (Integer)obj;
                field = Chp.getDeclaredField("table");
                field.setAccessible(true);
                Object[] obj2 = (Object[]) field.get(FirstScan.Item2Sets);
                length = obj2.length;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return sphash % (length - 1);
        }else {
            return 0;
        }
    }

    //判断HashMap中对应索引是否存在元素
    public boolean isExist(int index) {
        Class Chp = FirstScan.Item2Sets.getClass();
        Field field = null;
        Object[] obj = null;
        if (!FirstScan.Item2Sets.isEmpty()) {
            try {
                field = Chp.getDeclaredField("table");
                field.setAccessible(true);
                obj = (Object[])field.get(FirstScan.Item2Sets);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return (obj[index] == null)? false :true;
        }
        return false;
    }

    //获取HashMap对应索引处的键值
    public static List<String> getIndexKey(int index) {
        Class Chp = FirstScan.Item2Sets.getClass();
        Field field = null;
        Field key = null;
        Object[] obj = null;
        Object ConHashMapNode = null;
        List<String> KEY = null;
        try {
            ConHashMapNode = Class.forName("java.util.concurrent.ConcurrentHashMap$Node");
            key = ((Class) ConHashMapNode).getDeclaredField("key");
            field = Chp.getDeclaredField("table");
            field.setAccessible(true);
            key.setAccessible(true);
            obj = (Object[])field.get(FirstScan.Item2Sets);
            KEY = (ArrayList<String>)key.get(obj[index]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return KEY;
    }

    @Override
    protected void compute() {
        if ((end - start) <= MAX) {
            for (int i = start; i < end; i++) {
                List<String> affair = parallelApriori.record.get(i);
                //第一步，将该事务存入precord中
                if (!parallelApriori.precord.containsKey(affair)) {
                    parallelApriori.precord.put(affair, 1);
                }else {
                    int oldValue = parallelApriori.precord.get(affair);
                    parallelApriori.precord.put(affair, oldValue + 1);
                }
                //第二步，获得候选一项集及其对应支持度
                    for (int j = 0; j < affair.size(); j++) {
                        if (!FirstScan.firstCandidate.containsKey(affair.get(j))) {
                            FirstScan.firstCandidate.put(affair.get(j), 1);
                        }else {
                            FirstScan.firstCandidate.put(affair.get(j), FirstScan.firstCandidate.get(affair.get(j)) + 1);
                        }
                }
                //第三步，生成每条事务对应的二项集，并存储在HashMap中
                if (parallelApriori.status) {
                    for (int k = 0; k < affair.size(); k++) {
                        for (int l = k + 1; l < affair.size(); l++) {
                            List<String> item2Set = new ArrayList<String>(2);
                            item2Set.add(0, affair.get(k));
                            item2Set.add(1, affair.get(l));
                            int index = gethashcode(item2Set.hashCode());
                            if (isExist(index)) {
                                item2Set = getIndexKey(index);
                                int oldValue = (Integer)FirstScan.Item2Sets.get(item2Set);
                                int newValue = oldValue + 1;
                                FirstScan.Item2Sets.put(item2Set, newValue);
                            }else {
                                FirstScan.Item2Sets.put(item2Set, 1);
                            }
                        }
                    }
                }
            }
        } else {
            //任务分解
            int middle = (start + end)/2;
            Scan1Task left = new Scan1Task(start, middle);
            Scan1Task right = new Scan1Task(middle, end);
            //并行执行两个小任务
            invokeAll(left, right);
        }
    }
}

public class FirstScan {
    static Map<String, Integer> firstCandidate = new ConcurrentHashMap<String, Integer>();
    static Map<List<String>, Integer> Item2Sets = new ConcurrentHashMap<List<String>, Integer>();

    //创建线程池，获取第一次扫描的结果
    public void firstScan(int START, int END) {
        ForkJoinPool forkJoinPool = new ForkJoinPool(4);
        Scan1Task task = new Scan1Task(START, END);
        forkJoinPool.invoke(task);
        forkJoinPool.shutdown();
    }
}


