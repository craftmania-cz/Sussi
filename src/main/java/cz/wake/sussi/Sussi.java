package cz.wake.sussi;

import ai.api.AIConfiguration;
import ai.api.AIDataService;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import cz.wake.sussi.commands.CommandHandler;
import cz.wake.sussi.listeners.BoosterListener;
import cz.wake.sussi.listeners.CraftManiaArchiveListener;
import cz.wake.sussi.listeners.DialogFlowListener;
import cz.wake.sussi.listeners.MainListener;
import cz.wake.sussi.metrics.Metrics;
import cz.wake.sussi.objects.ats.ATSManager;
import cz.wake.sussi.objects.notes.NoteManager;
import cz.wake.sussi.runnable.StatusChanger;
import cz.wake.sussi.sql.SQLManager;
import cz.wake.sussi.utils.LoadingProperties;
import cz.wake.sussi.utils.SussiLogger;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.LogManager;

import static net.dv8tion.jda.internal.utils.JDALogger.getLog;

public class Sussi {

    private static Sussi instance;
    private MainListener events;
    private static JDA jda;
    private SQLManager sql;
    private CommandHandler ch = new CommandHandler();
    public static final String PREFIX = ",";
    public static long startUp;
    public static String API_URL = "";
    private static String ipHubKey = "";
    private static boolean isBeta = true;
    private static final Map<String, Logger> LOGGERS;
    public static final Logger LOGGER;
    public static NoteManager noteManager;
    public static ATSManager atsManager;

    static {
        new File("logs/latest.log").renameTo(new File("logs/log-" + getCurrentTimeStamp() + ".log"));
        LOGGERS = new ConcurrentHashMap<>();
        LOGGER = getLog(Sussi.class);
    }

    public static void main(String[] args) throws LoginException, InterruptedException {
        // Startup info
        SussiLogger.infoMessage("Now will Sussi wake up!");

        // Config
        SussiLogger.infoMessage("Loading config...");
        LoadingProperties config = new LoadingProperties();
        ipHubKey = config.getIpHubKey();
        isBeta = config.isBeta();

        EventWaiter waiter = new EventWaiter();

        startUp = System.currentTimeMillis();

        // Dialogflow
        AIConfiguration aiConfig = new AIConfiguration(config.getDialogFlowApiKey());
        AIDataService aiDataService = new AIDataService(aiConfig);

        // Connecting to Discord API
        SussiLogger.infoMessage("Connecting to Discord API...");
        jda = new JDABuilder(AccountType.BOT)
                .setToken(config.getBotToken())
                .addEventListeners(new MainListener(waiter), new CraftManiaArchiveListener())
                .addEventListeners(waiter)
                .addEventListeners(new DialogFlowListener(aiDataService))
                .addEventListeners(new BoosterListener(waiter))
                .setActivity(Activity.playing("Načítám se..."))
                .build().awaitReady();

        // Register commands
        (instance = new Sussi()).init();

        // Metrics
        if (config.isMetricsEnabled()) {
            SussiLogger.infoMessage("Metrics enabled, now starts...");
            Metrics.setup();
        }

        // isBeta and MySQL
        if (!isBeta) {
            SussiLogger.infoMessage("Connection to MySQL...");

            try {
                (instance = new Sussi()).initDatabase();
                SussiLogger.greatMessage("Sussi is successful connected to MySQL.");
                SussiLogger.infoMessage("Sussi will run as PRODUCTION bot.");
                isBeta = false;
            } catch (Exception e) {
                SussiLogger.dangerMessage("During connection to MySQL, error has occurred:");
                e.printStackTrace();
                System.exit(-1);
            }
        } else {
            SussiLogger.warnMessage("Database is off, Sussi will not load and save anything!");
            SussiLogger.warnMessage("Sussi is running as BETA bot! Some functions will not work!");
        }

        // StatusChanger
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new StatusChanger(), 10, 60000);

        if (!isBeta) noteManager = new NoteManager();

        atsManager = new ATSManager();
        /*while (LogManager.getLogManager().getLoggerNames().hasMoreElements()) {
            String logger = LogManager.getLogManager().getLoggerNames().nextElement();
            //LogManager.getLogManager().getLogger(logger).setLevel(Level.SEVERE);
        }*/
        SchedulerFactory schedulerFactory = new StdSchedulerFactory();
        try {
            Scheduler scheduler = schedulerFactory.getScheduler();
            JobDetail job = JobBuilder.newJob(ATSManager.class)
                    .withIdentity("atsEvaluation")
                    .build();
            CronTrigger ITrigger = TriggerBuilder.newTrigger()
                    .forJob("atsEvaluation")
                    .withSchedule(CronScheduleBuilder.monthlyOnDayAndHourAndMinute(5, 8, 0))
                    .build();
            scheduler.start();
            scheduler.scheduleJob(job, ITrigger);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    public static Sussi getInstance() {
        return instance;
    }

    public MainListener getEvents() {
        return events;
    }

    public static JDA getJda() {
        return jda;
    }

    public CommandHandler getCommandHandler() {
        return ch;
    }

    private void init() {
        ch.register();
    }

    public static long getStartUp() {
        return startUp;
    }

    private void initDatabase() {
        sql = new SQLManager(this);
    }

    public SQLManager getSql() {
        return sql;
    }

    public static String getCurrentTimeStamp() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        Date now = new Date();
        return sdfDate.format(now);
    }

    public static String getApiUrl(){
        return API_URL;
    }

    public static String getIpHubKey() {
        return ipHubKey;
    }

    public static NoteManager getNoteManager() {
        return noteManager;
    }

    public static ATSManager getATSManager() {
        return atsManager;
    }
}
