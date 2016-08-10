package Modules.Tasks.Models;

import java.util.*;
import Modules.DateTime.DateTime;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import Modules.Application.Models.TaskRow;
import Modules.Tasks.Interfaces.ITaskEntityConverter;

/**
 * Created by Karol Golec on 05.08.2016.
 */
public class TaskEntityConverter implements ITaskEntityConverter {

    /**
     * Get all task rows by tasks entities
     *
     * @param tasks also entities
     * @return observable list with task rows for table view in fxml file
     */
    public ObservableList<TaskRow> entitiesToRows(List<TaskEntity> tasks) {
        ObservableList<TaskRow> list;
        list = FXCollections.observableArrayList();
        for(TaskEntity task : tasks){
            String stringDays = daysOfWeekToString(task.getDaysOfWeek());
            String stringHours = hoursToString(task.getHours());

            TaskRow taskRow = new TaskRow(
                    task.getId(),
                    task.getServer(),
                    task.getInstance(),
                    task.getDatabase(),
                    task.getUserName(),
                    task.getPassword(),
                    task.getSavePath(),
                    task.getSavePathReserve(),
                    stringDays, stringHours);
            list.add(taskRow);
        }
        return list;
    }

    /**
     * Convert entity days of week to string
     *
     * @param days from DayOfWeekEntity
     * @return string with days
     */
    public static String daysOfWeekToString(Set<DayOfWeekEntity> days){
        String stringDays = "";

        List<DayOfWeekEntity> listDays = new ArrayList<DayOfWeekEntity>(days);

        Collections.sort(listDays,new Comparator<DayOfWeekEntity>(){
            public int compare(final DayOfWeekEntity a, DayOfWeekEntity b) {
                return Integer.signum(b.getDayOfWeek()-a.getDayOfWeek());
            }
        });

        for(DayOfWeekEntity day : listDays)
        {
            stringDays = String.format("%1$s, %2$s", DateTime.getNameDayInWeek(day.getDayOfWeek()), stringDays);
        }

        if (stringDays.contains(", "))
        {
            stringDays = stringDays.substring(0,stringDays.length()-2);
        }

        return stringDays;
    }

    /**
     * Convert entity hours to string
     *
     * @param hours from HourEntity
     * @return string with hours
     */
    public static String hoursToString(Set<HourEntity> hours){
        String stringHours = "";

        List<HourEntity> listHours = new ArrayList<HourEntity>(hours);

        Collections.sort(listHours,new Comparator<HourEntity>(){
            public int compare(final HourEntity a, HourEntity b) {
                return Integer.signum(b.getHour()-a.getHour());
            }
        });

        for(HourEntity hour : listHours)
        {
            stringHours = String.format("%1$s, %2$s", DateTime.addZeroToHour(hour.getHour()), stringHours);
        }

        if (stringHours.contains(", "))
        {
            stringHours = stringHours.substring(0,stringHours.length()-2);
        }

        return stringHours;
    }
}