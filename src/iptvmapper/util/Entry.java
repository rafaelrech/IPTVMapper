package iptvmapper.util;

import java.util.ArrayList;

public class Entry {

    private String tvgId;
    private String tvgName;
    private String title;
    private String group;
    private String logo;
    private ArrayList<String> urls;


    public Entry(String tvgId, String tvgName, String title, String group, String logo) {
        this(tvgId, tvgName, title, group, logo, new ArrayList<String>());
    }

    public Entry(String tvgId, String tvgName, String title, String group, String logo, ArrayList<String> urls) {
        super();
        this.tvgId = tvgId;
        this.tvgName = tvgName;
        this.title = title;
        this.group = group;
        this.logo = logo;
        this.urls = urls;
    }

    public String getTvgId() {
        return tvgId;
    }

    public void setTvgId(String tvgId) {
        this.tvgId = tvgId;
    }

    public String getTvgName() {
        return tvgName;
    }

    public void setTvgName(String tvgName) {
        this.tvgName = tvgName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public ArrayList<String> getUrls() {
        return urls;
    }

    public void setUrls(ArrayList<String> urls) {
        this.urls = urls;
    }

    public void addUrl(String url) {
        if (this.urls == null) {
            this.urls = new ArrayList<String>();
        }
        this.urls.add(url);
    }

}
