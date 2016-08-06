package Modules.Tasks.Interfaces;

import Modules.Tasks.Models.DayOfWeekEntity;
import Modules.Tasks.Models.HourEntity;

import java.util.List;
import java.util.Set;

/**
 * Created by Karol Golec on 06.08.2016.
 */
public interface IHourConverter {

    /**
     * Convert array list to entities
     *
     * @param hours in list
     * @return Entities of HourEntity
     */
    Set<HourEntity> listToEntities(List<String> hours);
}
