package cn.zjiali.jd.monitor.db;

public class Config {

    private Long id;
    private String name;
    private String env;
    private String keyword;
    private String script;
    private String valueRegex;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public String getValueRegex() {
        return valueRegex;
    }

    public void setValueRegex(String valueRegex) {
        this.valueRegex = valueRegex;
    }

    @Override
    public String toString() {
        return "Config{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", env='" + env + '\'' +
                ", keyword='" + keyword + '\'' +
                ", script='" + script + '\'' +
                ", valueRegex='" + valueRegex + '\'' +
                '}';
    }
}
