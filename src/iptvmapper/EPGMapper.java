package iptvmapper;

import iptvmapper.util.Entry;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.Normalizer;
import java.util.*;

public class EPGMapper {

    private static final long serialVersionUID = 32L;
    private static final String GROUPS_FILE = "groups.properties";
    private static final String BLACKLIST = "blacklist.properties";
    private static final String WHITELIST = "whitelist.properties"; // whitelist de titles para groups da blacklist
    private static final String MAPPING_FILE = "mappings.properties";
    private static final String EPG_FILE = "epgs.properties";
    private static final String MAPPING_PROP = "chn";
    private static final String MAPPING_MAP = "epg";


    private SortedMap<String, Entry> entries = new TreeMap<String, Entry>();

    public static void main(String args[]) {
        new EPGMapper().doIt();
    }

    private String getPropMappingValue(String mappingFile, String propName, String mappingName, String propValue) {
        try {
            Properties properties = new Properties();
            properties.load(EPGMapper.class.getClassLoader().getResourceAsStream(mappingFile));
            for (int i = 0; true; i++) {
                String prop = properties.getProperty(propName + "." + i);
                if (prop == null) {
                    break;
                }
                if (prop.equalsIgnoreCase(propValue)) {
                    return properties.getProperty(mappingName + "." + i);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getPropertyValue(String file, String prop) {
        try {
            Properties properties = new Properties();
            properties.load(EPGMapper.class.getClassLoader().getResourceAsStream(file));
            return properties.getProperty(prop);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String removeAcentos(String str) {
        str = Normalizer.normalize(str, Normalizer.Form.NFD);
        str = str.replaceAll("[^\\p{ASCII}]", "");
        str = str.replaceAll(" ", ".");
        str = str.replaceAll("\\(", "").replaceAll("\\)", "");
        return str;
    }

    public void doIt() {

        int blackListCounter = 0;
        int canaisCounter = 0;
        try {
            int responseCode = -1;
            ArrayList<String> urls = new ArrayList<String>();
            urls.add("https://cld.pt/dl/download/e09f6d90-100c-4161-be00-7d527235a05e/RafaelRech.m3u");
            for (String strUrl : urls) {
                URL url = new URL(strUrl);
                HttpURLConnection con = null;
                con = (HttpURLConnection) url.openConnection();
                con.setConnectTimeout(1 * 1000);
                con.setRequestMethod("GET");
                con.connect();
                responseCode = con.getResponseCode();
                System.out.println("Calling: " + strUrl);
                System.out.println(String.format("Response: %d <br>", responseCode));
                if (responseCode == 200) {
                    BufferedReader br = new BufferedReader(new InputStreamReader((con.getInputStream())));
                    // StringBuilder sb = new StringBuilder();
                    // String prevLine = new String();
                    String tvgId = new String();
                    String tvgName = new String();
                    String logo = new String();
                    String group = new String();
                    String title = new String();
                    String line;
                    while ((line = br.readLine()) != null) {
                        if ((line.indexOf("#") > -1 && line.indexOf("#EXTINF") < 0) || line.startsWith("##")
                                || line.trim().length() < 1) {
                            System.out.println(line);
                            continue;
                        }

                        if (line.startsWith("#EXTINF")) {
                            tvgId = "";
                            tvgName = "";
                            logo = "";
                            group = "";
                            title = "";

                            int commaidx = line.lastIndexOf(",");
                            title = line.substring(commaidx + 1).trim();
                            line = line.substring(8, commaidx).trim();
                            line = line.substring(line.indexOf(" ")).trim();

                            int eqIdx = line.indexOf("=");
                            while (eqIdx > -1) {
                                String propName = line.substring(0, eqIdx).trim();
                                int endIndex = line.indexOf("\"", eqIdx + 2);
                                String provValue = line.substring(eqIdx + 2, endIndex).trim();
                                switch (propName) {
                                    case "tvg-id":
                                        tvgId = provValue;
                                        break;
                                    case "tvg-name":
                                        tvgName = provValue;
                                        break;
                                    case "tvg-logo":
                                        logo = provValue;
                                        break;
                                    case "group-title":
                                        group = provValue;
                                        break;
                                }
                                line = line.substring(endIndex + 1).trim();
                                eqIdx = line.indexOf("=");
                            }
                        } else {

                            boolean isBlocked = false;
                            String blocked = "";
                            // BLACKLIST - GROUP
                            if (group.length() > 0) {
                                blocked = getPropertyValue(BLACKLIST, removeAcentos(group.toLowerCase()));
                                if (blocked != null && blocked.equalsIgnoreCase("group")) {
                                    String whilelist = getPropertyValue(WHITELIST, removeAcentos(title.toLowerCase()));
                                    if (whilelist == null) {
//                                        System.out.println(String.format("REMOVENDO %s POIS O GRUPO FAZ PARTE DA BLACKLIST", title));
                                        isBlocked = true;
                                    }
                                }
                            }
                            // BLACKLIST - TITLE
                            blocked = getPropertyValue(BLACKLIST, removeAcentos(title.toLowerCase()));
                            if (blocked != null && blocked.equalsIgnoreCase("title")) {
//                                System.out.println(String.format("REMOVENDO %s POIS O TITLE FAZ PARTE DA BLACKLIST", title));
                                isBlocked = true;
                            }
                            // BLACKLIST tvg-id
                            if (tvgId.length() > 0) {
                                blocked = getPropertyValue(BLACKLIST, removeAcentos(tvgId.toLowerCase()));
                                if (blocked != null && blocked.equalsIgnoreCase("title")) {
//                                    System.out.println(String.format("REMOVENDO %s POIS O ID FAZ PARTE DA BLACKLIST", title));
                                    isBlocked = true;
                                }
                            }
                            // BLACKLIST tvg-name
                            if (tvgId.length() > 0) {
                                blocked = getPropertyValue(BLACKLIST, removeAcentos(tvgName.toLowerCase()));
                                if (blocked != null && blocked.equalsIgnoreCase("title")) {
//                                    System.out.println(String.format("REMOVENDO %s POIS O NAME FAZ PARTE DA BLACKLIST", title));
                                    isBlocked = true;
                                }
                            }

                            if (isBlocked) {
                                //System.out.println(title);
                                blackListCounter++;
                                continue;
                            }

                            // epg
                            String epgId = getPropertyValue(EPG_FILE, removeAcentos(title.toLowerCase()));
                            if (epgId != null) {
                                tvgId = epgId;
                                tvgName = "";
                            }

                            if (group.toLowerCase().indexOf("esportes (pt)") > -1 ||
                                    group.toLowerCase().indexOf("espanha") >= 0 ||
                                    group.toLowerCase().indexOf("frança") >= 0 ||
                                    group.toLowerCase().indexOf("reino unido") >= 0) {
                                group = "FAV-ESPORTES EURO";
                            } else {
                                if (group.toLowerCase().indexOf("infantil") > -1 ||
                                        title.toLowerCase().indexOf("cartoon") >= 0
                                        || title.toLowerCase().indexOf("gloob") >= 0
                                        || title.toLowerCase().indexOf("nick") >= 0
                                        || title.toLowerCase().indexOf("disney") >= 0
                                        || title.toLowerCase().indexOf("kids") >= 0) {
                                    group = "FAV-INFANTIL";
                                } else {
                                    if (group.toLowerCase().indexOf("document") > -1 ||
                                            title.toLowerCase().indexOf("animal") >= 0
                                            || title.toLowerCase().indexOf("discovery") >= 0
                                            || title.toLowerCase().indexOf("history") >= 0
                                            || title.toLowerCase().indexOf("h2") >= 0
                                            || title.toLowerCase().indexOf("geo") >= 0
                                            || title.toLowerCase().indexOf("a&e") >= 0
                                            || title.toLowerCase().indexOf("gnt") >= 0
                                            || title.toLowerCase().indexOf("fox life") >= 0
                                            || title.toLowerCase().indexOf("multishow") >= 0
                                            || title.toLowerCase().indexOf("tlc") >= 0) {
                                        group = "FAV-VARIEDADES";
                                    } else {
                                        if (group.toLowerCase().indexOf("filmes e séries (pt)") > -1
                                                || title.toLowerCase().indexOf("tvcine") >= 0) {
                                            group = "FAV-FILMES PT";
                                        } else {
                                            if (group.toLowerCase().indexOf("esportes") > -1) {
                                                group = "FAV-ESPORTES";
                                            } else {
                                                if (title.toLowerCase().indexOf("sport") >= 0
                                                        || title.toLowerCase().indexOf("maxx") >= 0
                                                        || title.toLowerCase().indexOf("espn") >= 0
                                                        || title.toLowerCase().indexOf("combate") >= 0
                                                        || title.toLowerCase().indexOf("premiere") >= 0
                                                        ) {
                                                    group = "FAV-ESPORTES";
                                                } else {
                                                    if (title.toLowerCase().indexOf("globo") > -1 || title.toLowerCase().indexOf("rbs") > -1) {
                                                        group = "FAV-TV ABERTA";
                                                    } else {
                                                        if (title.toLowerCase().indexOf("telecine") == 0
                                                                || title.toLowerCase().indexOf("hbo") == 0
                                                                || title.toLowerCase().indexOf("cinemax") == 0
                                                                || title.toLowerCase().indexOf("fox") == 0
                                                                || title.toLowerCase().indexOf("max") == 0
                                                                || title.toLowerCase().indexOf("fx") == 0
                                                                || title.toLowerCase().indexOf("space") == 0
                                                                || title.toLowerCase().indexOf("tnt hd") == 0
                                                                || title.toLowerCase().indexOf("paramount") == 0
                                                                || title.toLowerCase().indexOf("megapix") == 0
                                                                || title.toLowerCase().indexOf("syfy") == 0
                                                                ) {
                                                            group = "FAV-FILMES";
                                                        } else {
                                                            if (title.toLowerCase().indexOf("tnt series") == 0
                                                                    || title.toLowerCase().indexOf("axn") == 0
                                                                    || title.toLowerCase().indexOf("universal") >= 0
                                                                    || title.toLowerCase().indexOf("tbs") == 0
                                                                    || title.toLowerCase().indexOf("tcm") == 0
                                                                    || title.toLowerCase().indexOf("warner") == 0
                                                                    || title.toLowerCase().indexOf("sony") == 0
                                                                    ) {
                                                                group = "FAV-SERIES";
                                                            } else {
                                                                if (title.toLowerCase().indexOf("multishow") == 0 || title.toLowerCase().indexOf("hbo") == 0
                                                                        || title.toLowerCase().indexOf("tlc") == 0
                                                                        || title.toLowerCase().indexOf("food") == 0
                                                                        || title.toLowerCase().indexOf("viva") == 0
                                                                        || title.toLowerCase().indexOf("gnt") == 0) {
                                                                    group = "FAV-VARIEDADES";
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

/*
                            System.out.println(String.format(
                                    "#EXTINF:-1 tvg-id=\"%s\" tvg-name=\"%s\" tvg-logo=\"%s\" group-title=\"%s\",%s",
                                    tvgId, tvgName, logo, group, title));
                            System.out.println(line);
*/

canaisCounter++;
                            // prevLine = "";
                            Entry entry = null;
                            if (entries.containsKey(title)) {
                                entry = entries.get(title);
                                if (entry.getGroup().trim().isEmpty()) {
                                    entry.setGroup(group.toUpperCase().trim());
                                }
                                if (entry.getLogo().trim().isEmpty()) {
                                    entry.setLogo(logo.toUpperCase().trim());
                                }
                            } else {
                                entry = new Entry(tvgId, tvgName, title, group, logo);
                            }
                            if (!entry.getUrls().contains(line)) {
                                entry.addUrl(line);
                            }
                            entries.put(title, entry);
                        }
                    }
                }
            }

            System.out.println("#EXTM3U\n");
            System.out.println("\n");
            for (String title : entries.keySet()) {
                Entry entry = entries.get(title);
                for (String url : entry.getUrls()) {
                    System.out.println(String.format(
                            "#EXTINF:-1 tvg-id=\"%s\" tvg-name=\"%s\" tvg-logo=\"%s\" group-title=\"%s\",%s",
                            entry.getTvgId(), entry.getTvgName(), entry.getLogo(), entry.getGroup(), entry.getTitle()));
                    System.out.println(line);

                    System.out.println(String.format("#EXTINF:0 tvg-logo=\"%s\" group-title=\"%s\", %s\n", entry.getLogo(),
                            entry.getGroup(), entry.getName()));
                    System.out.println(String.format("%s\n\n", url));
                }
            }

            System.out.println(String.format("Canais: %d\tRemovidos: %d", canaisCounter, blackListCounter));
        } catch (Exception exc)

        {
            System.err.println("<B><font color='red'>Exception</font></b>");
            System.err.println(exc.getMessage());
            exc.printStackTrace();
        }
    }

}
