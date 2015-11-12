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

        int diaLlegada = (previousStandstill instanceof TimeWindowedCustomer)
                ? ((TimeWindowedCustomer) previousStandstill).getDiaLlegada() : 0;


        TimeWindowedCustomer shadowCustomer = sourceCustomer;

        Long arrivalTime = calculateArrivalTime(shadowCustomer, departureTime, diaLlegada);
        while (shadowCustomer != null && ObjectUtils.notEqual(shadowCustomer.getArrivalTime(), arrivalTime)) {
            scoreDirector.beforeVariableChanged(shadowCustomer, "arrivalTime");
            shadowCustomer.setArrivalTime(arrivalTime);
            scoreDirector.afterVariableChanged(shadowCustomer, "arrivalTime");
            departureTime = shadowCustomer.getDepartureTime();
            diaLlegada = shadowCustomer.getDiaLlegada();//////////////////
            shadowCustomer = shadowCustomer.getNextCustomer();
            arrivalTime = calculateArrivalTime(shadowCustomer, departureTime, diaLlegada);
        }
    }

	private Long calculateArrivalTime(TimeWindowedCustomer customer, Long previousDepartureTime,
			int previousDiaLlegada) {

		if (customer == null) {
			return null;
		}
		
		long[] day_s = new long[] { 0, 1440000, 2880000, 4320000, 5760000, 7200000, 8640000, 12960000, 14400000,
				10080000, 11520000, 15840000 };
		long llegada = 0; // Inicialización llegada

		
		if (previousDepartureTime == null) {
			// PreviousStandstill is the Vehicle, so we leave from the Depot at
			// the best suitable time

			customer.setDiaLlegada(customer.getDiaInicio());
			long a = day_s[customer.getDiaInicio()] + customer.getReadyTime();
			long b = customer.getDistanceFromPreviousStandstill();
			llegada = Math.max(a, b);
		} else {
			llegada = previousDepartureTime + customer.getDistanceFromPreviousStandstill();
		}

		
		
		int dia_llegada = previousDiaLlegada;

		if (dia_llegada < customer.getDiaInicio()){ //Si se llega antes del dia en que empieza el rango entonces se mueve al primer momento del rango
			dia_llegada = customer.getDiaInicio();
			customer.setDiaLlegada(customer.getDiaInicio());
			llegada = day_s[dia_llegada] + customer.getReadyTime();
			return llegada;
		}
		
		long dueTime = day_s[dia_llegada] + customer.getDueTime();
		long readyTime = day_s[dia_llegada] + customer.getReadyTime();

		if (llegada > dueTime) { //Si se llega despues del fin de la ventana entonces se empuja al siguiente dia
			customer.setDiaLlegada(dia_llegada + 1);
			llegada = day_s[dia_llegada + 1] + customer.getReadyTime();
			return llegada;
		}
		else {
			customer.setDiaLlegada(dia_llegada);
			return llegada;

		}
    }

}
