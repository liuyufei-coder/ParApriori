package DataStructure;

import java.util.ArrayList;
import java.util.List;


//自定义一个数据类型，用于存放频繁项集及其对应的支持度
public class Mymap {
    public List<String> Item = new ArrayList<String>();
    public int count;
    static final int HASH_BITS = 0x7fffffff;

    public Mymap(List<String> Item, int count) {
        this.Item = Item;
        this.count = count;
    }

    public int hash() {
        int h = this.Item.hashCode();
        return (h ^ (h >>> 16)) & HASH_BITS;
    }

    @Override
    public boolean equals(Object obj) {
        Mymap newobj = (Mymap)obj;
        return newobj.hash() == this.hash();
    }
}
