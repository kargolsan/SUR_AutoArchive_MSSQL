package Modules.MSSQL.Models;

import java.sql.*;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.List;
import Modules.DateTime.DateTime;
import org.apache.logging.log4j.Logger;
import Modules.Tasks.Models.HourEntity;
import Modules.Tasks.Models.TaskEntity;
import Modules.Tasks.Models.TaskFactory;
import org.apache.logging.log4j.LogManager;
import Modules.Tasks.Models.DayOfWeekEntity;
import Modules.Tasks.Interfaces.ITaskFactory;
import Modules.MSSQL.Interfaces.IDBConnection;
import Modules.Translations.Models.Translation;
import Modules.Application.Interfaces.ILogService;
import Modules.Application.Models.Services.LogService;

/**
 * Created by Karol Golec on 10.08.2016.
 */
public class BackupDB {

    /**
     * logger for this class
     */
    private static Logger logger = LogManager.getLogger();

    /** translation */
    ResourceBundle trans;

    /** last hour of backup */
    static Integer lastHourBackup = 0;

    /** Service of log to table view */
    ILogService log = new LogService();

    /**
     * Constructor of class
     */
    public BackupDB(){
        trans = new Translation().getResourceBundle("Modules/MSSQL/Resources/Languages/mssql");
    }

    /**
     * Backup Databases from tasks
     *
     * @throws Exception
     */
    public void backup() throws Exception{

        if (backupIsDoubleInCurrentHour()){
            return;
        }

        ITaskFactory taskFactory = new TaskFactory();
        List<TaskEntity> tasks = taskFactory.getAll();

        for (TaskEntity task : tasks){

            if (!currentDayIsForBackup(task)) continue;

            if (!currentHourIsForBackup(task)) continue;

            if (!hasConnectionDB(task)) {
                log.addError(String.format(trans.getString("log.no_connection_database_of_task"), task.getId()));
                continue;
            }
            if (backupDBToFile(task)){
                log.addSuccess(String.format(trans.getString("log.completed_backup_database"), task.getId()));
            } else {
                log.addError(String.format(trans.getString("log.no_backup_database"), task.getId()));
            }
        }
    }

    /**
     * Check is task has been runner in current day
     *
     * @param task
     * @return true if task has current day
     */
    private Boolean currentDayIsForBackup(TaskEntity task){
        Set<DayOfWeekEntity> days = task.getDaysOfWeek();

        for (DayOfWeekEntity day : days){
            if (DateTime.getCurrentNumberDayInWeek() == day.getDayOfWeek()){
                return true;
            }
        }
        return false;
    }

    /**
     * Check is task has been runner in current hour
     *
     * @param task
     * @return true if task has current hour
     */
    private Boolean currentHourIsForBackup(TaskEntity task){
        Set<HourEntity> hours = task.getHours();

        for (HourEntity hour : hours){
            if (DateTime.getCurrentNumberHour() == hour.getHour()){
                return true;
            }
        }
        return false;
    }

    /**
     * Check backup is not double
     *
     * @return true if double, return false if not double backup in current hour
     */
    private Boolean backupIsDoubleInCurrentHour(){
        if (lastHourBackup == DateTime.getCurrentNumberHour()){
            return true;
        }
        lastHourBackup = DateTime.getCurrentNumberHour();
        return false;
    }

    /**
     * Check connection to database of MsSQL
     *
     * @param task with connection to database
     * @return true if has connection and return false if hasn't connection
     */
    private Boolean hasConnectionDB(TaskEntity task){
        IDBConnection dbConn = new DBConnection();
        return dbConn.hasConnection(
                task.getServer(),
                task.getInstance(),
                task.getDatabase(),
                task.getUserName(),
                task.getPassword()
        );
    }

    /**
     * Backup database to file
     *
     * @param task also TaskEntity
     * @return true if backup it has been completed successfully, false hasn't been completed
     */
    private Boolean backupDBToFile(TaskEntity task){
        Connection conn;

        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            conn = DriverManager.getConnection(genConnectionString(task), task.getUserName(), task.getPassword());
            Statement statement = conn.createStatement();
            statement.execute(genCommandSQLBackup(task));
            conn.close();
            return true;
        } catch (SQLException ex) {
            logger.error(ex.getMessage());
            return false;
        } catch (ClassNotFoundException ex) {
            logger.error(ex.getMessage());
            return false;
        }
    }

    /**
     * Get new name for wile of backup database
     *
     * @param task - TaskEntity
     * @return new name for file
     */
    private String genFileName(TaskEntity task) {
        return String.format("%1$s_%2$s_%3$s.ape",
                trans.getString("file.archive"),
                task.getDatabase(),
                DateTime.getDateTimeForFile()
        );
    }

    /**
     * Generate connection string for backup database
     *
     * @param task - TaskEntity
     * @return connection string
     */
    private String genConnectionString(TaskEntity task){
        return String.format("jdbc:jtds:sqlserver://%1$s;instance=%2$s;DatabaseName=%3$s",
                task.getServer(),
                task.getInstance(),
                task.getDatabase()
        );
    }

    /**
     * Generate command SQL for backup database
     *
     * @param task - TaskEntity
     * @return command of sql to backup database
     */
    private String genCommandSQLBackup(TaskEntity task){
        return String.format("BACKUP DATABASE %1$s TO DISK = '%2$s\\%3$s' ",
                task.getDatabase(),
                task.getSavePath(),
                genFileName(task)
        );
    }
}
