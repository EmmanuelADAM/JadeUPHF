/**
 * ***************************************************************
 * JADE - Java Agent DEvelopment Framework is a framework to develop
 * multi-agent systems in compliance with the FIPA specifications.
 * Copyright (C) 2000 CSELT S.p.A.
 * <p>
 * GNU Lesser General Public License
 * <p>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation,
 * version 2.1 of the License.
 * <p>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 * **************************************************************
 */
package jade.content.lang.sl;

/**
 * The vocabulary of the simbols used in the FIPA SL2 language
 *
 * @author Giovanni Caire - TILAB
 */
public interface SL2Vocabulary extends SL1Vocabulary {
    String IOTA = "iota";
    String ANY = "any";
    String ALL = "all";

    String IMPLIES = "implies";
    String IMPLIES_LEFT = "left";
    String IMPLIES_RIGHT = "right";

    String EQUIV = "equiv";
    String EQUIV_LEFT = "left";
    String EQUIV_RIGHT = "right";

    String EXISTS = "exists";
    String EXISTS_WHAT = "what";
    String EXISTS_CONDITION = "condition";

    String FORALL = "forall";
    String FORALL_WHAT = "what";
    String FORALL_CONDITION = "condition";

    String BELIEF = "B";
    String BELIEF_AGENT = "agent";
    String BELIEF_CONDITION = "condition";

    String UNCERTAINTY = "U";
    String UNCERTAINTY_AGENT = "agent";
    String UNCERTAINTY_CONDITION = "condition";

    String PERSISTENT_GOAL = "PG";
    String PERSISTENT_GOAL_AGENT = "agent";
    String PERSISTENT_GOAL_CONDITION = "condition";

    String INTENTION = "I";
    String INTENTION_AGENT = "agent";
    String INTENTION_CONDITION = "condition";

    String FEASIBLE = "feasible";
    String FEASIBLE_ACTION = "action";
    String FEASIBLE_CONDITION = "condition";

    String ACTION_SEQUENCE = ";";
    String ACTION_SEQUENCE_FIRST = "first";
    String ACTION_SEQUENCE_SECOND = "second";

    String ACTION_ALTERNATIVE = "|";
    String ACTION_ALTERNATIVE_FIRST = "first";
    String ACTION_ALTERNATIVE_SECOND = "second";
}
