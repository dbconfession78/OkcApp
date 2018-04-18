import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by stuartkuredjian on 6/3/15.
 */
public class SearchMatcher {
    private int cycleLimit = 0;
    private int maxLimit = 0;
    private Boolean isReplacing = false;
    private RunManager runManager;
    private Utilities utils = new Utilities();
    private String searchURL;
    private HashMap<String, String> searchSettingsMap;
    private AccountManager accountMgr;
    private Boolean shouldThrow = false;
    String value;
    String next;
    private Iterator iterator;
    private int maxProfiles = 41;
    private boolean isFreshRun = true;
    private String finalJSON = "";
    private String group0 = "";
    private String group1 = "";
    private String group2 = "";

    public SearchMatcher(AccountManager accountMgr, HashMap<String, String> searchSettingsMap) {
        runManager = accountMgr.mainView.runManager;
        isFreshRun = runManager.getIsFreshRun();
        this.accountMgr = accountMgr;
        this.searchSettingsMap = searchSettingsMap;

        try {
            maxLimit = Integer.parseInt(searchSettingsMap.get("limit"));

            if(maxLimit > 40) {
                cycleLimit = 40;
                maxLimit = maxLimit - cycleLimit;
            } else {
                cycleLimit = maxLimit;
            }

            generateJsonURL(cycleLimit);
        } catch (Exception e) {
            utils.println("FAILED TO CREATE SEARCH URL: the selected item, \"" + value + "\" from the \"" + next + "\" combobox must be defined in searchMatcher\n");
        }
    }

    public String generateJsonURL(int profileCount) throws Exception {
        if(profileCount > 40) {
            profileCount = 40;
        }
        prepForJSON();
        searchURL = "https://www.okcupid.com/apitun/match/search?&access_token=" + accountMgr.getAuthCode() + "&";
        finalJSON = "_json={";
        String initialJSON ="_json={" +

                "\"order_by\":\"\"," +
                "\"debug\":0," +
                "\"gentation\":[]," +
                "\"gender_tags\":\"\"," +
                "\"orientation_tags\":\"\"," +
                "\"minimum_age\":\"\"," +
                "\"maximum_age\":\"\"," +
                "\"locid\":\"\"," +
                "\"radius\":\"\"," +
                "\"lquery\":\"\"," +
                "\"location\":{" +
                    "\"popularity\":\"\"," +
                    "\"longitude\":\"\"," +
                    "\"metro_area\":\"\"," +
                    "\"city_name\":\"\"," +
                    "\"locid\":\"\"," +
                    "\"latitude\":\"\"," +
                    "\"display_state\":\"\"," +
                    "\"state_code\":\"\"," +
                    "\"default_radius\":\"\"," +
                    "\"density\":\"\"," +
                    "\"postal_code\":\"\"," +
                    "\"country_code\":\"\"" +
                "}," +
                "\"located_anywhere\":\"\"," +
                "\"last_login\":\"\"," +
                "\"i_want\":\"\"," +
                "\"they_want\":\"\"," +
                "\"minimum_height\":\"\"," +
                "\"maximum_height\":\"\"," +
                "\"minimum_attractiveness\":\"\"," +
                "\"maximum_attractiveness\":\"\"," +
                "\"bodytype\":[]," +
                "\"languages\":0," +
                "\"speaks_my_language\":0," +
                "\"ethnicity\":[]," +
                "\"religion\":[]," +
                "\"availability\":\"\"," +
                "\"monogamy\":\"\"," +
                "\"looking_for\":[]," +
                "\"smoking\":[]," +
                "\"drinking\":[]," +
                "\"drugs\":[]," +
                "\"questions\":[]," +
                "\"answers\":[]," +
                "\"personality_filters\":{}," +
                "\"education\":[]," +
                "\"children\":[]," +
                "\"interest_ids\":[]," +
                "\"username\":\"\"," +
                "\"tagOrder\":[]," +
                "\"save_search\":\"\"";

        Pattern p = Pattern.compile("\"([0-9a-z_]+)\":([\\[\\]\"]+)");
        Matcher m = p.matcher(initialJSON);
        while(m.find()) {
            group0 = m.group(0);
            group1 = m.group(1);
            group2 = m.group(2);

            if(searchSettingsMap.containsKey((group1))) {
                if(searchSettingsMap.get(group1).equals("")) {
                    continue;
                }
            } else {
                if(group2.equals("\"\"")) {
                    continue;
                }
                if(group2.startsWith("[")) {
                    arrayTranscription(group1);
                    continue;
                }
            }

            if(!finalJSON.endsWith("{")) {
                finalJSON += ",";
            }

            finalJSON += "\"" + group1 + "\":";

            if(group2.startsWith("[")) {
                finalJSON += "[" + searchSettingsMap.get(group1) + "]";
            } else {
                finalJSON += "\"" + searchSettingsMap.get(group1) + "\"";
            }
        }
        finalJSON += ",\"limit\":\"" + profileCount + "\"";
        finalJSON += "}";
        searchURL += finalJSON;

        return searchURL;
    }

    private void arrayTranscription(String key) {

        ArrayList<String> arrayList = new ArrayList<>();
        if(group1.equals(key)) {
            switch (key) {
                case "gentation":
                    arrayList.add("34");
                    arrayList.add("17");

                case "ethnicity":
                    arrayList.add("asian");
                    arrayList.add("black");
                    arrayList.add("hispanic_latin");
                    arrayList.add("indian");
                    arrayList.add("middle_eastern");
                    arrayList.add("native_american");
                    arrayList.add("pacific_islander");
                    arrayList.add("white");
                    arrayList.add("other");

                case "bodytype":
                    arrayList.add("thin");
                    arrayList.add("fit");
                    arrayList.add("average");
                    arrayList.add("jacked");
                    arrayList.add("overweight");
                    arrayList.add("a_little_extra");
                    arrayList.add("full_figured");
                    arrayList.add("curvy");
                    break;

                case "religion":
                    arrayList.add("agnotsticism");
                    arrayList.add("atheism");
                    arrayList.add("buddhism");
                    arrayList.add("christianity");
                    arrayList.add("catholicism");
                    arrayList.add("other");
                    arrayList.add("islam");
                    arrayList.add("judaism");
                    arrayList.add("hinduism");
                    break;

                case "looking_for":
                    arrayList.add("new_friends");
                    arrayList.add("short_term_dating");
                    arrayList.add("casual_sex");
                    arrayList.add("long_term_dating");
                    break;

                case "drinking":
                    arrayList.add("not_at_all");
                    arrayList.add("socially");
                    arrayList.add("rarely");
                    arrayList.add("very_often");
                    arrayList.add("often");
                    arrayList.add("desperately");
                    break;

                case "smoking":
                    arrayList.add("no");
                    arrayList.add("sometimes");
                    arrayList.add("when_drinking");
                    arrayList.add("trying_to_quit");
                    arrayList.add("yes");
                    break;

                case "drugs":
                    arrayList.add("never");
                    arrayList.add("sometimes");
                    arrayList.add("often");
                    break;

                case "question":
                    break;

                case "answers":
                    break;

                case "education":
                    arrayList.add("high_schol");
                    arrayList.add("two_year_college");
                    arrayList.add("college_university");
                    arrayList.add("post_grad");
                    break;

                case "children":
                    arrayList.add("doesnt_have");
                    arrayList.add("doesnt_want");
                    arrayList.add("has_one_or_more");
                    arrayList.add("might_want");
                    arrayList.add("wants_kids");
                    break;

                case "radius":
                    if(searchSettingsMap.get("radius").equals("Anywhere")) {

                    }

                case "interest_ids":
                    break;
                default: break;
            }

            String item = "";
            for (int i = 0; i < arrayList.size(); ) {
                item = arrayList.get(i);
                if (searchSettingsMap.containsKey(item)) {
                    if (searchSettingsMap.get(item).equals("false")) {
                        arrayList.remove(item);
                    } else {
                        i++;
                    }
                } else {
                    arrayList.remove(item);
                }
            }
            if (!arrayList.isEmpty()) {
                if (!finalJSON.endsWith("{")) {
                    finalJSON += ",";
                }
                finalJSON += "\"" + group1 + "\":[";
                for (int i = 0; i < arrayList.size(); i++) {
                    if (!finalJSON.endsWith("[")) {
                        finalJSON += ",";
                    }
                    finalJSON += "\"" + arrayList.get(i) + "\"";
                }
                finalJSON += "]";
            }
        }
    }

    private ArrayList prepArrayforJSON(ArrayList<String> array) {
        int originalCount = array.size();
        for (int i = 0; i < originalCount; ) {
            String item = array.get(i);
            String original = item;
            Boolean modified = false;
            for (int j = 0; j < item.length(); j++) {
                if(Character.isUpperCase(item.charAt(j))) {
                    modified = true;
                    String character = String.valueOf(item.charAt(j));
                    item = item.replaceFirst(character, "_" + character.toLowerCase());
                }
            }
            if(modified == false) {
                i++;
            }
            array.add(item);
            array.remove(original);
        }
         return array;
    }

    private void prepForJSON() {
        searchSettingsMap = cleanBlankValues();
        transcribeLocatedAnywhere();
        pruneMap();
        removeCamelCase();
        searchSettingsMap = utils.addUnderscores(searchSettingsMap);
        transcribeHeight();
        transcribelastLogin();
        transcribeGenderFilter();
        transcribeAttractiveness();
        transcribeBodyType();
        transcribeEthnicity();
        transcribeReligion();
        transcribeEducation();
        transcribeSmokes();
        transcribeDrinks();
        transcribeDrugs();
        searchSettingsMap.replace("order_by", searchSettingsMap.get("order_by").toUpperCase());

    }

    private void transcribeDrugs() {
        String drugs = "";
        HashMap<String, String> drugsMap = new HashMap<String, String>();
        drugsMap.put("never", searchSettingsMap.get("never"));
        drugsMap.put("often", searchSettingsMap.get("often_drugs"));
        drugsMap.put("sometimes", searchSettingsMap.get("sometimes_drugs"));

        Boolean isFirst = true;
        iterator = drugsMap.keySet().iterator();
        while(iterator.hasNext()) {
            String key = String.valueOf(iterator.next());
            String value = drugsMap.get(key);

            if(value.equals("true")) {
                if(!isFirst) {
                    drugs += ",";
                }
                drugs += "\"" + key + "\"";
                isFirst = false;
            }
        }
        searchSettingsMap.put("drugs", drugs);
    }

    private void transcribeDrinks() {
        String drinks = "";
        HashMap<String, String> drinksMap = new HashMap<String, String>();
        drinksMap.put("not_at_all", searchSettingsMap.get("not_at_all"));
        drinksMap.put("socially", searchSettingsMap.get("socially"));
        drinksMap.put("rarely", searchSettingsMap.get("rarely"));
        drinksMap.put("very_often", searchSettingsMap.get("very_often"));
        drinksMap.put("desperately", searchSettingsMap.get("desperately"));
        drinksMap.put("often", searchSettingsMap.get("often_drinks"));

        Boolean isFirst = true;
        iterator = drinksMap.keySet().iterator();
        while(iterator.hasNext()) {
            String key = String.valueOf(iterator.next());
            String value = drinksMap.get(key);

            if(value.equals("true")) {
                if(!isFirst) {
                    drinks += ",";
                }
                drinks += "\"" + key + "\"";
                isFirst = false;
            }
        }
        searchSettingsMap.put("drinks", drinks);
    }

    private void transcribeSmokes() {
        String smokes = "";
        HashMap<String, String> smokesMap = new HashMap<String, String>();
        smokesMap.put("yes", searchSettingsMap.get("yes"));
        smokesMap.put("no", searchSettingsMap.get("no"));
        smokesMap.put("when_drinking", searchSettingsMap.get("when_drinking"));
        smokesMap.put("trying_to_quit", searchSettingsMap.get("trying_to_quit"));
        smokesMap.put("sometimes", searchSettingsMap.get("sometimes_smokes"));

        Boolean isFirst = true;
        iterator = smokesMap.keySet().iterator();
        while(iterator.hasNext()) {
            String key = String.valueOf(iterator.next());
            String value = smokesMap.get(key);

            if(value.equals("true")) {
                if(!isFirst) {
                    smokes += ",";
                }
                smokes += "\"" + key + "\"";
                isFirst = false;
            }
        }
        searchSettingsMap.put("smokes", smokes);
    }

    private void transcribeEducation() {
        String education = "";
        HashMap<String, String> educationMap = new HashMap<String, String>();
        educationMap.put("high_school", searchSettingsMap.get("high_school"));
        educationMap.put("two_year_college", searchSettingsMap.get("two_year_college"));
        educationMap.put("college_university", searchSettingsMap.get("college_university"));
        educationMap.put("post_grad", searchSettingsMap.get("post_grad"));

        Boolean isFirst = true;
        iterator = educationMap.keySet().iterator();
        while(iterator.hasNext()) {
            String key = String.valueOf(iterator.next());
            String value = educationMap.get(key);

            if(value.equals("true")) {
                if(!isFirst) {
                    education += ",";
                }
                education += "\"" + key + "\"";
                isFirst = false;
            }
        }
        searchSettingsMap.put("education", education);
    }

    private void transcribeReligion() {
        String religion = "";
        HashMap<String, String> religionMap = new HashMap<String, String>();
        religionMap.put("agnosticism", searchSettingsMap.get("agnosticism"));
        religionMap.put("atheism", searchSettingsMap.get("atheism"));
        religionMap.put("buddhism", searchSettingsMap.get("buddhism"));
        religionMap.put("catholicism", searchSettingsMap.get("catholicism"));
        religionMap.put("christianity", searchSettingsMap.get("christianity"));
        religionMap.put("hinduism", searchSettingsMap.get("hinduism"));
        religionMap.put("judaism", searchSettingsMap.get("judaism"));
        religionMap.put("islam", searchSettingsMap.get("islam"));
        religionMap.put("other", searchSettingsMap.get("other_religion"));

        Boolean isFirst = true;
        iterator = religionMap.keySet().iterator();
        while(iterator.hasNext()) {
            String key = String.valueOf(iterator.next());
            String value = religionMap.get(key);

            if(value.equals("true")) {
                if(!isFirst) {
                    religion += ",";
                }
                religion += "\"" + key + "\"";
                isFirst = false;
            }
        }
        searchSettingsMap.put("religion", religion);
    }

    private void transcribeEthnicity() {
        String ethnicity = "";
        HashMap<String, String> ethnicityMap = new HashMap<String, String>();
        ethnicityMap.put("white", searchSettingsMap.get("white"));
        ethnicityMap.put("asian", searchSettingsMap.get("asian"));
        ethnicityMap.put("black", searchSettingsMap.get("black"));
        ethnicityMap.put("hispanic_latin", searchSettingsMap.get("hispanic_latin"));
        ethnicityMap.put("indian", searchSettingsMap.get("indian"));
        ethnicityMap.put("middle_eastern", searchSettingsMap.get("middle_eastern"));
        ethnicityMap.put("native_american", searchSettingsMap.get("native_american"));
        ethnicityMap.put("pacific_islander", searchSettingsMap.get("pacific_islander"));
        ethnicityMap.put("other", searchSettingsMap.get("other_ethnicity"));
        
        Boolean isFirst = true;
        iterator = ethnicityMap.keySet().iterator();
        while(iterator.hasNext()) {
            String key = String.valueOf(iterator.next());
            String value = ethnicityMap.get(key);

            if(value.equals("true")) {
                if(!isFirst) {
                    ethnicity += ",";
                }
                ethnicity += "\"" + key + "\"";
                isFirst = false;
            }
        }
        searchSettingsMap.put("ethnicity", ethnicity);
    }

    private void transcribeBodyType() {
        String bodyType = "";
        HashMap<String, String> bodyTypeMap = new HashMap<String, String>();
        bodyTypeMap.put("thin", searchSettingsMap.get("thin"));
        bodyTypeMap.put("fit", searchSettingsMap.get("fit"));
        bodyTypeMap.put("average", searchSettingsMap.get("average_body"));
        bodyTypeMap.put("curvy", searchSettingsMap.get("curvy"));
        bodyTypeMap.put("jacked", searchSettingsMap.get("jacked"));
        bodyTypeMap.put("full_figured", searchSettingsMap.get("full_figured"));
        bodyTypeMap.put("a_little_extra", searchSettingsMap.get("a_little_extra"));
        bodyTypeMap.put("overweight", searchSettingsMap.get("overweight"));

        Boolean isFirst = true;
        iterator = bodyTypeMap.keySet().iterator();
        while(iterator.hasNext()) {
            String key = String.valueOf(iterator.next());
            String value = bodyTypeMap.get(key);

            if(value.equals("true")) {
                if(!isFirst) {
                    bodyType += ",";
                }
                bodyType += "\"" + key + "\"";
                isFirst = false;
            }
        }
        searchSettingsMap.put("bodytype", bodyType);
    }

    private void transcribeAttractiveness() {
        Boolean average = false;
        Boolean aboveAverage = false;
        Boolean hot = false;
        if(searchSettingsMap.get("average_attractiveness").equals("true")) {
            average = true;
        }
        if(searchSettingsMap.get("above_average").equals("true")) {
            aboveAverage = true;
        }

        if(searchSettingsMap.get("hot").equals("true")) {
            hot = true;
        }

        if(average && !aboveAverage && !hot) {
            searchSettingsMap.put("minimum_attractiveness", "4000");
            searchSettingsMap.put("maximum_attractiveness", "6000");
        }

        if(!average && aboveAverage && !hot) {
            searchSettingsMap.put("minimum_attractiveness", "6000");
            searchSettingsMap.put("maximum_attractiveness", "8000");
        }

        if(!average && !aboveAverage && hot) {
            searchSettingsMap.put("minimum_attractiveness", "8000");
            searchSettingsMap.put("maximum_attractiveness", "10000");
        }

        if(average && aboveAverage && !hot) {
            searchSettingsMap.put("minimum_attractiveness", "4000");
            searchSettingsMap.put("maximum_attractiveness", "8000");
        }

        if(!average && aboveAverage && hot) {
            searchSettingsMap.put("minimum_attractiveness", "6000");
            searchSettingsMap.put("maximum_attractiveness", "10000");
        }

        if(average && aboveAverage && hot) {
            searchSettingsMap.put("minimum_attractiveness", "4000");
            searchSettingsMap.put("maximum_attractiveness", "10000");
        }

        if(average && !aboveAverage && hot) {
            searchSettingsMap.put("minimum_attractiveness", "4000");
            searchSettingsMap.put("maximum_attractiveness", "10000");
        }
    }

    private void pruneMap() {
        iterator = searchSettingsMap.keySet().iterator();
        while(iterator.hasNext()) {
            String key = String.valueOf(iterator.next());
            String value = searchSettingsMap.get(key);

            if(value.equals("") || value==null || value.equals("0")) {
                searchSettingsMap.remove(key);
                iterator = searchSettingsMap.keySet().iterator();
            }
        }
    }

    private void removeCamelCase() {
        iterator = searchSettingsMap.keySet().iterator();
        while(iterator.hasNext()) {
            Boolean replace = false;
            String key = String.valueOf(iterator.next());
            String originalKey = key;
            String value = searchSettingsMap.get(key);
            for (int i = 0; i < key.length(); i++) {

                Character character = key.charAt(i);
                if(character.isUpperCase(key.charAt(i))) {
                    key = key.replaceFirst(String.valueOf(character), " " + character.toString().toLowerCase());
                    replace = true;
                }
            }
            if(replace) {
                searchSettingsMap.remove(originalKey);
                searchSettingsMap.put(key, value);
                iterator = searchSettingsMap.keySet().iterator();
            }
        }
    }

    private void transcribeGenderFilter() {
        String orientation = searchSettingsMap.get("orientation");
        String gentation = "";
        String genderTags = "";
        switch(orientation) {
            case "women_who_like_men" :
                gentation = "34";
                genderTags = "1";
                break;

            case "men_who_like_women" :
                gentation = "17";
                genderTags = "2";
                break;
        }
        searchSettingsMap.put("gentation", gentation);
        searchSettingsMap.put("gender_tags", genderTags);
    }

    private void transcribelastLogin() {
        String lastLogin = searchSettingsMap.get("last_login");
        switch(lastLogin) {
            case "online_now" : searchSettingsMap.replace("last_login", "3600"); break;
            case "past_day" : searchSettingsMap.replace("last_login", "86400"); break;
            case "past_week" : searchSettingsMap.replace("last_login", "604800"); break;
            case "past_month" : searchSettingsMap.replace("last_login", "2678400"); break;
            case "past_year" : searchSettingsMap.replace("last_login", "31536000"); break;
        }
    }

    private HashMap<String, String> cleanBlankValues() {
        Iterator iterator = searchSettingsMap.keySet().iterator();
        while(iterator.hasNext()) {
            String mapKey = String.valueOf(iterator.next());
            if(searchSettingsMap.get(mapKey).startsWith("any") || searchSettingsMap.get(mapKey).equals("0") || searchSettingsMap.get(mapKey) == null) {
                searchSettingsMap.replace(mapKey, "");
                iterator = searchSettingsMap.keySet().iterator();
            }
        }

        return searchSettingsMap;
    }

    public String getSearchURL() {
        return this.searchURL;
    }


    public Boolean getShouldThrow() {
        return shouldThrow;
    }

    public void transcribeLocatedAnywhere() {
        String radius = searchSettingsMap.get("radius");
        String locatedAnywhere = "";
        if(radius.equals("")) {
            locatedAnywhere = "1";
        } else {
            locatedAnywhere = "0";
        }
        searchSettingsMap.put("located_anywhere", locatedAnywhere);
    }

    public void transcribeHeight() {
        if(isFreshRun) {
            String minHeight = searchSettingsMap.get("minimum_height");
            String maxHeight = searchSettingsMap.get("maximum_height");
            String[] heights = new String[]{minHeight, maxHeight};
            // if highest and lowest heights are selected, eliminate heaight from JSON
            if(!(minHeight.equals("5'0\"") && maxHeight.equals("6'4\""))) {
                for (int i = 0; i < 2; i++) {
                    String height = heights[i];
                    switch (height) {
                        case "5'0\"":
                            heights[i] = "15240";
                            break;
                        case "5'1\"":
                            heights[i] = "15495";
                            break;
                        case "5'2\"":
                            heights[i] = "15748";
                            break;
                        case "5'3\"":
                            heights[i] = "16002";
                            break;
                        case "5'4\"":
                            heights[i] = "16256";
                            break;
                        case "5'5\"":
                            heights[i] = "16510";
                            break;
                        case "5'6\"":
                            heights[i] = "16764";
                            break;
                        case "5'7\"":
                            heights[i] = "17018";
                            break;
                        case "5'8\"":
                            heights[i] = "17272";
                            break;
                        case "5'9\"":
                            heights[i] = "17526";
                            break;
                        case "5'10\"":
                            heights[i] = "17780";
                            break;
                        case "5'11\"":
                            heights[i] = "18034";
                            break;
                        case "6'0\"":
                            heights[i] = "18288";
                            break;
                        case "6'1\"":
                            heights[i] = "18542";
                            break;
                        case "6'2\"":
                            heights[i] = "18796";
                            break;
                        case "6'3\"":
                            heights[i] = "19050";
                            break;
                        case "6'4\"":
                            heights[i] = "19304";
                            break;
                        default:
                            shouldThrow = true;
                    }
                }
            } else {
                heights[0] = "";
                heights[1] = "";
            }
            searchSettingsMap.replace("minimum_height", heights[0]);
            searchSettingsMap.replace("maximum_height", heights[1]);
        }
    }

    public void setIsFreshRun(boolean isFreshRun) {
        this.isFreshRun = isFreshRun;
    }
}
