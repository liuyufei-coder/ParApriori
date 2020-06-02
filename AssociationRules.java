package CoreCode;

import DataStructure.Mymap;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

class AssoRulesTask extends RecursiveAction {
    private static final int MAX = 100;

    private int start;
    private int end;

    public AssoRulesTask(int start, int end) {
        this.start = start;
        this.end = end;
    }

    //获得一个频繁项集的所有子集
    public  List<List<String>> getSubSet(List<String> set){
        List<List<String>> result = new ArrayList<>();	//用来存放子集的集合，如{{},{1},{2},{1,2}}
        int length = set.size();
        int num = length==0 ? 0 : 1<<(length);	//2的n次方，若集合set为空，num为0；若集合set有4个元素，那么num为16.

        //从0到2^n-1（[00...00]到[11...11]）
        for(int i = 1; i < num-1; i++){
            List<String> subSet = new ArrayList<>();

            int index = i;
            for(int j = 0; j < length; j++){
                if((index & 1) == 1){		//每次判断index最低位是否为1，为1则把集合set的第j个元素放到子集中
                    subSet.add(set.get(j));
                }
                index >>= 1;		//右移一位
            }

            result.add(subSet);		//把子集存储起来
        }
        return result;
    }

    public List<String> gets2set(List<String> tem, List<String> s1)//计算tem减去s1后的集合即为s2
    {
        List<String> result=new ArrayList<>();
        for(int i=0;i<tem.size();i++)//去掉s1中的所有元素
        {
            String t=tem.get(i);
            if(!s1.contains(t))
                result.add(t);
        }
        return  result;
    }

    public void isAssociationRules(List<String> s1,List<String> s2,List<String> tem)//判断是否为关联规则
    {
        double confidence;
        int counts1;
        int countTem;
        if(s1.size()!=0&&s1!=null&&tem.size()!=0&&tem!=null)
        {
            counts1= getCount(s1);
            countTem=getCount(tem);
            confidence=countTem*1.0/counts1;

            if(confidence >= parallelApriori.MIN_CONFIDENCE)
            {
                String s = s1.toString()+"=>>"+s2.toString();
                parallelApriori.AssoRules.put(s, confidence);
            }
        }
    }

    public int getCount(List<String> in)//根据频繁项集得到其支持度计数
    {
        int rt=0;
        List<String> freItem;
        for(int i=0;i<parallelApriori.frequentMap.size();i++)
        {
            Mymap tem=parallelApriori.frequentMap.get(i);
            freItem = tem.Item;
            if(freItem.containsAll(in) && in.containsAll(freItem)) {
                rt = tem.count;
                return rt;
            }
        }
        return rt;
    }

    @Override
    protected void compute() {
        if (end - start <= MAX) {
            for (int i = start; i < end; i++) {
                List<String> temp;
                temp = parallelApriori.frequentItemset.get(i);
                if (temp.size() > 1) {
                    List<String> tempclone = new ArrayList<>(temp);
                    List<List<String>> AllSubSet = getSubSet(tempclone);
                    for (int j = 0; j < AllSubSet.size(); j++) {
                        List<String> s1 = AllSubSet.get(j);
                        List<String> s2 = gets2set(temp, s1);
                        isAssociationRules(s1, s2, temp);
                    }
                }
            }
        }else {
            //任务分解
            int middle = (start + end)/2;
            AssoRulesTask left = new AssoRulesTask(start, middle);
            AssoRulesTask right = new AssoRulesTask(middle, end);
            //并行执行两个小任务
            invokeAll(left, right);
        }
    }
}

public class AssociationRules {

    //获得所有关联规则
    public void AssociationRulesMining() {
        ForkJoinPool forkJoinPool  = new ForkJoinPool(4);
        AssoRulesTask task = new AssoRulesTask(0, parallelApriori.frequentItemset.size());
        forkJoinPool.invoke(task);
        forkJoinPool.shutdown();
    }

}
