package cz.wake.sussi.utils;

import java.io.File;
import java.io.FileInputStream;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class LoadingProperties {

    private String botToken;
    private String host, port, dbname, dbuser, dbpassword, minConnections, maxConnections, timeout, ipHubKey, beta, proxycheckKey,
                    dialogFlowApiKey, cmGuildID, navrhyDiskuzeID, navrhyHlasovaniID, ownerID, atPokecID, secretChannelAtsID;
    private boolean dialogFlowEnabled, metricsEnabled;
    private List<String> dialogFlowChannels;

    public LoadingProperties() {
        try {
            File configFile = new File("config.yml");

            FileInputStream fileInput = new FileInputStream(configFile);
            Properties properties = new Properties();
            properties.load(fileInput);
            fileInput.close();

            botToken = properties.getProperty("token");
            host = properties.getProperty("hostname");
            port = properties.getProperty("port");
            dbname = properties.getProperty("database");
            dbuser = properties.getProperty("username");
            dbpassword = properties.getProperty("password");
            minConnections = properties.getProperty("minimumConnections");
            maxConnections = properties.getProperty("maximumConnections");
            timeout = properties.getProperty("timeout");
            ipHubKey = properties.getProperty("ipHubKey");
            proxycheckKey = properties.getProperty("proxycheckKey");
            beta = properties.getProperty("isBeta");
            metricsEnabled = Boolean.parseBoolean(properties.getProperty("metrics.enabled", "false"));

            dialogFlowApiKey = properties.getProperty("dialogflow-api-key");
            dialogFlowEnabled = Boolean.parseBoolean(properties.getProperty("dialogflow-enabled", "false"));
            dialogFlowChannels = Collections.singletonList(properties.getProperty("dialogflow-channels"));

            cmGuildID = properties.getProperty("cm_guild");
            navrhyDiskuzeID = properties.getProperty("navrhy_diskuze");
            navrhyHlasovaniID = properties.getProperty("navrhy_hlasovani");
            ownerID = properties.getProperty("owner");
            atPokecID = properties.getProperty("at_pokec");
            secretChannelAtsID = properties.getProperty("secret_channel_ats");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getBotToken() {
        return botToken;
    }

    public String getHost() {
        return host;
    }

    public String getPort() {
        return port;
    }

    public String getDbname() {
        return dbname;
    }

    public String getDbuser() {
        return dbuser;
    }

    public String getDbpassword() {
        return dbpassword;
    }

    public int getMinConnections() {
        return Integer.valueOf(minConnections);
    }

    public int getMaxConnections() {
        return Integer.valueOf(maxConnections);
    }

    public int getTimeout() {
        return Integer.valueOf(timeout);
    }

    public String getIpHubKey() {
        return ipHubKey;
    }

    public Boolean isBeta() {
        return Boolean.parseBoolean(beta);
    }

    public String getDialogFlowApiKey() {
        return dialogFlowApiKey;
    }

    public boolean isDialogFlowEnabled() {
        return dialogFlowEnabled;
    }

    public String getProxycheckKey() {
        return proxycheckKey;
    }

    public List<String> getDialogFlowChannels() {
        return dialogFlowChannels;
    }

    public String getBeta() {
        return beta;
    }

    public boolean isMetricsEnabled() {
        return metricsEnabled;
    }

    public String getCmGuildID() {
        return cmGuildID;
    }

    public String getNavrhyDiskuzeID() {
        return navrhyDiskuzeID;
    }

    public String getOwnerID() {
        return ownerID;
    }

    public String getAtPokecID() {
        return atPokecID;
    }

    public String getSecretChannelAtsID() {
        return secretChannelAtsID;
    }

    public String getNavrhyHlasovaniID() {
        return navrhyHlasovaniID;
    }
}
