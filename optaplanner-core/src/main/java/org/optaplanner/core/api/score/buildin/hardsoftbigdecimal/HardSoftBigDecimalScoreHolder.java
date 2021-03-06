/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.core.api.score.buildin.hardsoftbigdecimal;

import java.math.BigDecimal;

import org.kie.api.runtime.rule.RuleContext;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.holder.AbstractScoreHolder;

/**
 * @see HardSoftBigDecimalScore
 */
public class HardSoftBigDecimalScoreHolder extends AbstractScoreHolder {

    protected BigDecimal hardScore = null;
    protected BigDecimal softScore = null;

    public HardSoftBigDecimalScoreHolder(boolean constraintMatchEnabled) {
        super(constraintMatchEnabled);
    }

    public BigDecimal getHardScore() {
        return hardScore;
    }

    public BigDecimal getSoftScore() {
        return softScore;
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    public void addHardConstraintMatch(RuleContext kcontext, final BigDecimal weight) {
        hardScore = (hardScore == null) ? weight : hardScore.add(weight);
        registerBigDecimalConstraintMatch(kcontext, 0, weight, new BigDecimalConstraintUndoListener() {
            @Override
            public void undo() {
                hardScore = hardScore.subtract(weight);
            }
        });
    }

    public void addSoftConstraintMatch(RuleContext kcontext, final BigDecimal weight) {
        softScore = (softScore == null) ? weight : softScore.add(weight);
        registerBigDecimalConstraintMatch(kcontext, 1, weight, new BigDecimalConstraintUndoListener() {
            @Override
            public void undo() {
                softScore = softScore.subtract(weight);
            }
        });
    }

    @Override
    public Score extractScore(int initScore) {
        return HardSoftBigDecimalScore.valueOf(initScore, hardScore == null ? BigDecimal.ZERO : hardScore,
                softScore == null ? BigDecimal.ZERO : softScore);
    }

}
