/*
 * Copyright 2015 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.examples.vehiclerouting.domain.timewindowed.solver;

import org.apache.commons.lang3.ObjectUtils;
import org.optaplanner.core.impl.domain.variable.listener.VariableListener;
import org.optaplanner.core.impl.score.director.ScoreDirector;
import org.optaplanner.examples.vehiclerouting.domain.Customer;
import org.optaplanner.examples.vehiclerouting.domain.Standstill;
import org.optaplanner.examples.vehiclerouting.domain.timewindowed.TimeWindowedCustomer;

import java.util.logging.Logger;


// TODO When this class is added only for TimeWindowedCustomer, use TimeWindowedCustomer instead of Customer
public class ArrivalTimeUpdatingVariableListener implements VariableListener<Customer> {
  private static Logger LOGGER = Logger.getLogger("InfoLogging");
    public void beforeEntityAdded(ScoreDirector scoreDirector, Customer customer) {
        // Do nothing
    }

    public void afterEntityAdded(ScoreDirector scoreDirector, Customer customer) {
        if (customer instanceof TimeWindowedCustomer) {
            updateArrivalTime(scoreDirector, (TimeWindowedCustomer) customer);
        }
    }

    public void beforeVariableChanged(ScoreDirector scoreDirector, Customer customer) {
        // Do nothing
    }

    public void afterVariableChanged(ScoreDirector scoreDirector, Customer customer) {
        if (customer instanceof TimeWindowedCustomer) {
            updateArrivalTime(scoreDirector, (TimeWindowedCustomer) customer);
        }
    }

    public void beforeEntityRemoved(ScoreDirector scoreDirector, Customer customer) {
        // Do nothing
    }

    public void afterEntityRemoved(ScoreDirector scoreDirector, Customer customer) {
        // Do nothing
    }

    protected void updateArrivalTime(ScoreDirector scoreDirector, TimeWindowedCustomer sourceCustomer) {
        Standstill previousStandstill = sourceCustomer.getPreviousStandstill();
        Long departureTime = (previousStandstill instanceof TimeWindowedCustomer)
                ? ((TimeWindowedCustomer) previousStandstill).getDepartureTime() : null;
        TimeWindowedCustomer shadowCustomer = sourceCustomer;

        Long arrivalTime = calculateArrivalTime(shadowCustomer, departureTime);
        while (shadowCustomer != null && ObjectUtils.notEqual(shadowCustomer.getArrivalTime(), arrivalTime)) {
            scoreDirector.beforeVariableChanged(shadowCustomer, "arrivalTime");
            shadowCustomer.setArrivalTime(arrivalTime);
            scoreDirector.afterVariableChanged(shadowCustomer, "arrivalTime");
            departureTime = shadowCustomer.getDepartureTime();
            shadowCustomer = shadowCustomer.getNextCustomer();
            arrivalTime = calculateArrivalTime(shadowCustomer, departureTime);
        }
    }

    private Long calculateArrivalTime(TimeWindowedCustomer customer, Long previousDepartureTime) {

//    	int range_s = 0;
//    	int range_e = 0;
    	
//    	long fecha_inicio_rango = day_s[range_s] + customer.getReadyTime();
//    	long fecha_fin_rango = day_s[range_e] + customer.getReadyTime();


    	long[] day_s = new long[]{0, 1440000, 2880000, 4320000, 5760000, 7200000, 8640000, 12960000, 14400000, 10080000, 11520000, 15840000};
    	long[] day_e = new long[]{1439999, 2879999, 4319999, 5759999, 7199999, 8639999, 10079999, 11519999, 12959999, 14399999, 15839999, 17279999};

    	int dia_llegada = 0;// customer.getPreviousStandstill().dia_llegada()
    	long llegada = 0;
    	
    	if (customer == null) {
            return null;
        }
        if (previousDepartureTime == null) {
            // PreviousStandstill is the Vehicle, so we leave from the Depot at the best suitable time


    		//customer.setdia_llegada(0)
        	long a = customer.getReadyTime();
            long b = customer.getDistanceFromPreviousStandstill();
        	llegada = Math.max(a, b);
        }
        else
        {
        llegada = previousDepartureTime + customer.getDistanceFromPreviousStandstill();
        }

        if (llegada > day_s[dia_llegada] + customer.getDueTime()) {
//customer.setdia_llegada(dia_llegada + 1)
        	return day_s[dia_llegada + 1] + customer.getReadyTime();

        }
        else {
    		//customer.setdia_llegada(dia_llegada)		
        	return day_s[dia_llegada] + llegada;

        }
    }

}
