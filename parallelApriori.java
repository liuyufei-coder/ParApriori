package CoreCode;

import DataStructure.Mymap;
import IO.DataRead;
import IO.PrintResult;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

//并行关联规则挖掘
public class parallelApriori {
    public static double MIN_SUPPORT = 0.01;//最小支持度百分比
    public static double MIN_CONFIDENCE = 0.4;//最小置信度
    static boolean status = false;//当前状态
    static List<List<String>> record = new CopyOnWriteArrayList<List<String>>();//数据集
    static Map<List<String>, Integer> precord = new ConcurrentHashMap<List<String>, Integer>();//进行数据压缩过的线程安全的数据集
    public static List<List<String>> frequentItemset = new CopyOnWriteArrayList<>();//存放所有的频繁项集
    public static List<Mymap> frequentMap = new CopyOnWriteArrayList<Mymap>();//利用Mymap数据类型存储所有频繁项集及其对应支持度计数
    static Map<String, Integer> firstCandidate;//存放候选一项集
    static Map<List<String>, Integer> Item2Sets;//存放用于压缩候选二项集的二项集及其对应支持度
    static List<List<String>> ItemSets = new CopyOnWriteArrayList<List<String>>();//用于存放候选项集或者频繁项集，充当中介
    static Map<List<String>, List<List<String>>> oldMap = new ConcurrentHashMap<List<String>, List<List<String>>>();//创建HashMap用于存储频繁项集及其对应的各项记录
    public static Map<String, Double> AssoRules = new ConcurrentHashMap<>();//用于存放关联规则及对应置信度


    public void pApriori(String url){
        long a = System.currentTimeMillis();
        //获取数据
        record = DataRead.getRecord(url);

        //第一次扫描
        FirstScan fscan = new FirstScan();
        fscan.firstScan(0, record.size());
        firstCandidate = FirstScan.firstCandidate;
        Item2Sets = FirstScan.Item2Sets;

        //第二次扫描
        SecondScan secondScan = new SecondScan();
        //获得频繁一项集
        secondScan.getFrequent1Item(firstCandidate);
        //获得剪枝后的候选二项集
        secondScan.getSecondCandidate();
        //获得频繁二项集
        secondScan.getFrequent2ItemSets();

        //最后一步,获得所有频繁项集及支持度
        LastStep.getAllFrequent();

        //输出结果
        PrintResult printResult = new PrintResult();
        //输出所有频繁项集及支持度
        printResult.printFre();

        //获取所有满足条件的关联规则
        AssociationRules associationRules = new AssociationRules();
        associationRules.AssociationRulesMining();

        //输出所有关联规则
        printResult.printAsso();
        long b = System.currentTimeMillis();
        System.out.println("优化后Apriori算法运行时间：" + printResult.gett(a, b) + "ms");
    }


    //主函数，程序入口
    public static void main(String[] args) {
        String url = "C:\\Users\\tmp\\Desktop\\groceries2.txt";
        parallelApriori paApriori = new parallelApriori();
        paApriori.pApriori(url);
    }
}
