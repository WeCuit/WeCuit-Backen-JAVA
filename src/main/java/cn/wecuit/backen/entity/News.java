package cn.wecuit.backen.entity;

import java.util.List;
import java.util.Map;

public class News {

    private String name;

    private String source;

    private List<Map<String,String>> tags;

    private String pattern;

    private String uriExp;

    private boolean sort = false;

    private int pullVer = 1;

    public News(){

    }

    public int getPullVer() {
        return pullVer;
    }

    public void setPullVer(int pullVer) {
        this.pullVer = pullVer;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public List<Map<String,String>> getTags() {
        return tags;
    }

    public void setTags(List<Map<String,String>> tags) {
        this.tags = tags;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public String getUriExp() {
        return uriExp;
    }

    public void setUriExp(String uriExp) {
        this.uriExp = uriExp;
    }

    public boolean isSort() {
        return sort;
    }

    public void setSort(boolean sort) {
        this.sort = sort;
    }

    @Override
    public String toString() {
        return "News{" +
                "name='" + name + '\'' +
                ", source='" + source + '\'' +
                ", tags=" + tags +
                ", pattern='" + pattern + '\'' +
                ", uriExp='" + uriExp + '\'' +
                ", sort=" + sort +
                ", pullVer=" + pullVer +
                '}';
    }
}
