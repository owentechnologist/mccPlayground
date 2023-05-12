package com.redislabs.sa.ot.mccpg;

import com.google.gson.Gson;
import com.redislabs.sa.ot.util.JedisConnectionHelper;
import org.json.JSONObject;
import redis.clients.jedis.search.FieldName;
import redis.clients.jedis.search.IndexDefinition;
import redis.clients.jedis.search.IndexOptions;
import redis.clients.jedis.search.Schema;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
/**
 * To invoke locate the file of interest and provide the path to it like this:
 * mvn compile exec:java -Dexec.cleanupDaemonThreads=false -Dexec.args="--host 192.168.1.20 --port 10400 --filepath /Users/owentaylor/wip/java/mccPlayground/main/resources/mcc.json"
 * A sample search query that returns only the matching description for a given id is:
 * FT.SEARCH idx_mcc "@mccid:9950" return 3 '$.mccver1[?(@.id =~ "(?i)9950")].description' AS MATCHING_DESCRIPTION LIMIT 0 2
 *
**/
public class Main {
    static String host = "localhost";
    static int port = 6379;
    static String username = "default";
    static String password = "";
    static String filepath = "";
    static JedisConnectionHelper connectionHelper = null;
    static int pipeBatchSize = 200;
    static String PREFIX_FOR_SEARCH = "mcc";
    static String INDEX_1_NAME = "idx_mcc";

    public static void main(String[] args){

        if (args.length > 0) {
            ArrayList<String> argList = new ArrayList<>(Arrays.asList(args));
            if (argList.contains("--host")) {
                int index = argList.indexOf("--host");
                host = argList.get(index + 1);
            }
            if (argList.contains("--port")) {
                int index = argList.indexOf("--port");
                port = Integer.parseInt(argList.get(index + 1));
            }
            if (argList.contains("--user")) {
                int index = argList.indexOf("--user");
                username = argList.get(index + 1);
            }
            if (argList.contains("--password")) {
                int index = argList.indexOf("--password");
                password = argList.get(index + 1);
            }
            if (argList.contains("--filepath")) {
                int index = argList.indexOf("--filepath");
                filepath = argList.get(index + 1);
            }
            if (argList.contains("--pipebatchsize")) {
                int index = argList.indexOf("--pipebatchsize");
                pipeBatchSize = Integer.parseInt(argList.get(index + 1));
            }
        }
        try{
            connectionHelper = new JedisConnectionHelper(host,port,username,password,2);
            loadMCCFileIntoRedisJSON(filepath,connectionHelper);
            createRedisSearchIndex(connectionHelper);
        }catch(Throwable t){t.printStackTrace();}
    }


    static void createRedisSearchIndex(JedisConnectionHelper connectionHelper){
        try{
            connectionHelper.getPooledJedis().ftDropIndex(INDEX_1_NAME);
        }catch(Throwable t){t.printStackTrace();}
        Schema schema = new Schema().addField(new Schema.Field(FieldName.of("$.mccver1[*].description").as("description"), Schema.FieldType.TEXT))
        .addSortableTextField("$.mccver1[*].id",1.0).as("mccid");
        IndexDefinition indexDefinition = new IndexDefinition(IndexDefinition.Type.JSON)
                .setPrefixes(new String[]{PREFIX_FOR_SEARCH});
        connectionHelper.getPooledJedis().ftCreate(INDEX_1_NAME, IndexOptions.defaultOptions().setDefinition(indexDefinition), schema);
    }

    static void loadMCCFileIntoRedisJSON(String path,JedisConnectionHelper connectionHelper)throws Throwable{
        String mccjson = new String(Files.readAllBytes(Paths.get(path)));
        Gson json = new Gson();
        Map<?, ?> map = json.fromJson(mccjson, Map.class);
        JSONObject obj = new JSONObject(map);
        connectionHelper.getPooledJedis().jsonSet("mccjson", obj);
    }

}
